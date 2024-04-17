package kafkaLock.lock;

import kafkaLock.config.MessageProducer;
import kafkaLock.model.LockMsg;
import kafkaLock.model.LockObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Component
public class KafkaLockFactory {
    // Hash
    public static final Map<String, LockObject> localLockMap = new ConcurrentHashMap<>();
    // Key
    public static final Map<String, LinkedList<LockMsg>> otherAppLocks = new ConcurrentHashMap<>();
    // Hash
    public static final Map<String, CountDownLatch> otherAppLatch = new ConcurrentHashMap<>();
    // prev hash => next hash
    public static final Map<String, String> otherAppPrevHash = new ConcurrentHashMap<>();

    private final MessageProducer messageProducer;

    public KafkaLockFactory(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public KafkaLock getLock(String lockId) {
        return new KafkaLock(lockId, messageProducer);
    }

    @KafkaListener(topics = "${message.lock.topic.name}", groupId = "${message.lock.listener.groupId}", containerFactory = "lockMsgKafkaListenerContainerFactory")
    public void listenLocking(@Payload LockMsg message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        //System.out.println("Received LockMsg: " + message + ", partition = " + partition);
        if (message.isLock()) {
            lockHandler(message);
        } else {
            unlockHandler(message);
        }
    }

    private static void lockHandler(LockMsg message) {
        LockObject lockObject = localLockMap.get(message.getHash());
        otherAppLocks.compute(message.getLockId(), (s, linkedList) -> {
            if (linkedList == null) {
                linkedList = new LinkedList<>();
            }
            String prevHash = null;
            if (linkedList.size() > 0 && linkedList.getLast() != null) {
                prevHash = linkedList.getLast().getHash();
            }
            if (lockObject != null) {
                if (lockObject.getLockMsg().getHash().equals(message.getHash())) {
                    if (prevHash != null) {
                        otherAppPrevHash.put(prevHash, message.getHash());
                        otherAppLatch.computeIfAbsent(message.getHash(), hash -> new CountDownLatch(1));
                    }
                    lockObject.getLockLatch().countDown();
                }
            }
            linkedList.add(message);
            return linkedList;
        });
    }

    private static void unlockHandler(LockMsg message) {
        otherAppPrevHash.computeIfPresent(message.getHash(), (prevHash, nextHash) -> {
            otherAppLatch.computeIfPresent(nextHash, (s, countDownLatch) -> {
                countDownLatch.countDown();
                return countDownLatch;
            });
            return null;
        });

        otherAppLocks.computeIfPresent(message.getLockId(), (key, linkedList) -> {
            ListIterator<LockMsg> listIter = linkedList.listIterator(0);
            while (listIter.hasNext()) {
                var lockData = listIter.next();
                // for our locked hash release it
                if (message.getHash().equals(lockData.getHash())) {
                    listIter.remove();
                    var lockObject = localLockMap.get(message.getHash());
                    if (lockObject != null) {
                        localLockMap.remove(message.getHash());
                        lockObject.getUnlockLatch().countDown();
                    }
                    break;
                }
            }
            return linkedList;
        });
    }
}
