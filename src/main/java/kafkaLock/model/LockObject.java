package kafkaLock.model;

import java.util.concurrent.CountDownLatch;

public class LockObject {
    private CountDownLatch lockLatch = new CountDownLatch(1);

    private CountDownLatch unlockLatch = new CountDownLatch(1);
    private LockMsg lockMsg;

    public LockObject() {
    }

    public LockObject(LockMsg lockData) {
        this.lockMsg = lockData;
    }

    public CountDownLatch getLockLatch() {
        return lockLatch;
    }

    public void setLockLatch(CountDownLatch lockLatch) {
        this.lockLatch = lockLatch;
    }

    public CountDownLatch getUnlockLatch() {
        return unlockLatch;
    }

    public void setUnlockLatch(CountDownLatch unlockLatch) {
        this.unlockLatch = unlockLatch;
    }

    public LockMsg getLockMsg() {
        return lockMsg;
    }

    public void setLockMsg(LockMsg lockMsg) {
        this.lockMsg = lockMsg;
    }
}
