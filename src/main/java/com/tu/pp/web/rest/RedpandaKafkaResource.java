package com.tu.pp.web.rest;

import com.tu.pp.config.KafkaProperties;
import com.tu.pp.domain.User;
import com.tu.pp.domain.UserSubscription;
import com.tu.pp.repository.UserSubscriptionRepository;
import java.util.List;
import java.util.Objects;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redpanda-kafka")
public class RedpandaKafkaResource {

    private static final String KAFKA_TOPIC = "subscriptions";
    private final Logger log = LoggerFactory.getLogger(RedpandaKafkaResource.class);

    private final UserSubscriptionRepository subscriptionRepository;

    private KafkaProducer<String, String> producer;

    public RedpandaKafkaResource(UserSubscriptionRepository subscriptionRepository, KafkaProperties kafkaProperties) {
        this.subscriptionRepository = subscriptionRepository;
        this.producer = new KafkaProducer<>(kafkaProperties.getProducerProps());
    }

    @GetMapping("/publish")
    public ResponseEntity<?> publish() {
        List<UserSubscription> subscriptions = subscriptionRepository.findAllActive(PageRequest.of(0, 1000));
        subscriptions.forEach(this::produceRecord);

        return ResponseEntity.ok().build();
    }

    private void produceRecord(final UserSubscription subscription) {
        User user = Objects.requireNonNull(subscription.getUser());
        producer.send(new ProducerRecord<>(KAFKA_TOPIC, String.valueOf(subscription.getId()), String.valueOf(user.getId())));
    }
}
