
package com.alekhya.resume_service.kafka.producer;

import com.alekhya.resume_service.kafka.event.ResumeAnalysisEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ResumeAnalysisProducer {

    private static final Logger log =
            LoggerFactory.getLogger(ResumeAnalysisProducer.class);

    private final KafkaTemplate<String, ResumeAnalysisEvent> kafkaTemplate;

    @Value("${app.kafka.topics.resume-analysis-requests}")
    private String topic;

    public ResumeAnalysisProducer(
            KafkaTemplate<String, ResumeAnalysisEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, ResumeAnalysisEvent>>
    publish(ResumeAnalysisEvent event) {

        log.info("Publishing resume analysis request for resume ID: {}",
                event.resumeId());

        CompletableFuture<SendResult<String, ResumeAnalysisEvent>> future =
                kafkaTemplate.send(
                        topic,
                        event.resumeId().toString(),
                        event
                );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Failed to publish resume analysis request for resume ID: {}",
                        event.resumeId(),
                        ex
                );
            } else {
                log.info(
                        "Successfully published event. Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }
        });

        return future;
    }
}
