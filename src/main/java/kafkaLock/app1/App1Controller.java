package kafkaLock.app1;

import jakarta.annotation.PreDestroy;
import kafkaLock.lock.KafkaLock;
import kafkaLock.lock.KafkaLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Test controller runs threads with locks
 * */
@RestController
public class App1Controller {
    private static final Logger logger = LoggerFactory.getLogger(App1Controller.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private KafkaLockFactory kafkaLockFactory;

    private int int1 = 0;
    private int int2 = 0;
    private int int3 = 0;
    private int int4 = 0;

    private Thread t1;
    private Thread t2;
    private Thread t3;
    private Thread t4;

    @PreDestroy
    public void destroy() {
        t1.stop();
        t2.stop();
        t3.stop();
        t4.stop();
    }

    @GetMapping("test")
    public String test() {
        new Thread(() -> {
            try {
                start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        return "ok";
    }

    public void start() throws Exception {
        t1 = new Thread(() -> {
            KafkaLock lock = kafkaLockFactory.getLock("lockId1");
            for (int i = 1; i <= 1000; i++) {
                processLock1(lock, "1_" + i);
            }
        });

        t2 = new Thread(() -> {
            KafkaLock lock = kafkaLockFactory.getLock("lockId2");
            for (int i = 1; i <= 1000; i++) {
                processLock2(lock, "2_" + i);
            }
        });

        t3 = new Thread(() -> {
            KafkaLock lock = kafkaLockFactory.getLock("lockId3");
            for (int i = 1; i <= 1000; i++) {
                processLock3(lock, "3_" + i);
            }
        });

        t4 = new Thread(() -> {
            KafkaLock lock = kafkaLockFactory.getLock("lockId4");
            for (int i = 1; i <= 1000; i++) {
                processLock4(lock, "4_" + i);
            }
        });


        var timestamp = System.currentTimeMillis();
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        logger.error("Result int is - {} {} {} {}, time = {}", int1, int2, int3, int4, (System.currentTimeMillis() - timestamp));
        int1 = 0;
        int2 = 0;
        int3 = 0;
        int4 = 0;
    }

    private void processLock1(KafkaLock lock, String name) {
        try {
            lock.lock();

            /*ResponseEntity<Integer> response = restTemplate.getForEntity("http://localhost:8083/getInt", Integer.class);
            int i = response.getBody() + 1;
            restTemplate.getForEntity("http://localhost:8083/setInt/" + i, String.class);*/

            logger.error("locked" + name);
            //System.out.println("locked" + name);
            int1++;
            //Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            logger.error("unlocked" + name);
            //System.out.println("unlocked" + name);
        }
    }

    private void processLock2(KafkaLock lock, String name) {
        try {
            lock.lock();
            logger.error("locked" + name);
            int2++;
            //Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            logger.error("unlocked" + name);
        }
    }

    private void processLock3(KafkaLock lock, String name) {
        try {
            lock.lock();
            logger.error("locked" + name);
            int3++;
            //Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            logger.error("unlocked" + name);
        }
    }

    private void processLock4(KafkaLock lock, String name) {
        try {
            lock.lock();
            logger.error("locked" + name);
            int4++;
            //Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            logger.error("unlocked" + name);
        }
    }

}
