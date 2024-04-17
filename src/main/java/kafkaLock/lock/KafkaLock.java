package kafkaLock.lock;

import kafkaLock.config.MessageProducer;
import kafkaLock.model.LockMsg;
import kafkaLock.model.LockObject;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KafkaLock {
    private final String lockId;
    private final String hash;
    private final MessageProducer messageProducer;

    private static final int DEFAULT_TIMEOUT = 1000 * 60 * 5;

    public KafkaLock(String lockId, MessageProducer messageProducer) {
        this.lockId = lockId;
        this.hash = UUID.randomUUID().toString();
        this.messageProducer = messageProducer;
    }

    public void lock() throws InterruptedException {
        // submit our lock and wait for response
        var lockData = new LockMsg(true, lockId, hash);
        LockObject lockObject = new LockObject(lockData);

        KafkaLockFactory.localLockMap.put(hash, lockObject);
        messageProducer.send(lockData);
        if (!lockObject.getLockLatch().await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            // Failed
            KafkaLockFactory.localLockMap.remove(hash);
            KafkaLockFactory.otherAppLatch.remove(hash);
            throw new RuntimeException("Lock failed");
        }

        // check other apps lathes released
        CountDownLatch countDownLatch = KafkaLockFactory.otherAppLatch.get(hash);
        if (countDownLatch != null) {
            if (!countDownLatch.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                KafkaLockFactory.localLockMap.remove(hash);
                KafkaLockFactory.otherAppLatch.remove(hash);
                throw new RuntimeException("Lock failed");
            }
            KafkaLockFactory.otherAppLatch.remove(hash);
        }

        // finally lock
    }

    public void unlock() {
        var lockObject = KafkaLockFactory.localLockMap.get(hash);
        messageProducer.send(new LockMsg(false, lockId, hash));
        try {
            if (!lockObject.getUnlockLatch().await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                // failed
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
