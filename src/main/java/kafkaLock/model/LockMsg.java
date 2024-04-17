package kafkaLock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LockMsg {

    @JsonProperty("isLock")
    private boolean isLock;

    @JsonProperty("lockId")
    private String lockId;

    @JsonProperty("hash")
    private String hash;

    public LockMsg() {
    }

    public LockMsg(boolean isLock, String lockId, String hash) {
        this.isLock = isLock;
        this.lockId = lockId;
        this.hash = hash;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "LockMsg{" +
                "isLock=" + isLock +
                ", lockId='" + lockId + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
