package kafkaLock.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

class KafkaErrorHandler implements CommonErrorHandler {

    @Override
    public boolean handleOne(@NotNull Exception exception, @NotNull ConsumerRecord<?, ?> record, @NotNull Consumer<?, ?> consumer, @NotNull MessageListenerContainer container) {
        handle(exception, consumer);
        return true;
    }

    @Override
    public void handleOtherException(@NotNull Exception exception, @NotNull Consumer<?, ?> consumer, @NotNull MessageListenerContainer container, boolean batchListener) {
        handle(exception, consumer);
    }

    private void handle(Exception exception, Consumer<?, ?> consumer) {
        exception.printStackTrace();
        if (exception instanceof RecordDeserializationException ex) {
            consumer.seek(ex.topicPartition(), ex.offset() + 1L);
            consumer.commitSync();
        }
    }
}