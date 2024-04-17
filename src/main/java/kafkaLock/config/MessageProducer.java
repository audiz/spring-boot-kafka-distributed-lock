package kafkaLock.config;

import kafkaLock.model.LockMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("message.lock.topic.name")
@Component
public class MessageProducer {

    @Value(value = "${message.lock.topic.name}")
    private String topicName;
    private final KafkaTemplate<String, LockMsg> kafkaLockTemplate;

    public MessageProducer(KafkaTemplate<String, LockMsg> kafkaLockTemplate) {
        this.kafkaLockTemplate = kafkaLockTemplate;
    }

    public void send(LockMsg message) {
        kafkaLockTemplate.send(topicName, message.getLockId(), message);
    }
}
