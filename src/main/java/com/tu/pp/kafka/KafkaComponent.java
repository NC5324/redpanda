package com.tu.pp.kafka;

import com.tu.pp.config.KafkaProperties;
import com.tu.pp.domain.User;
import com.tu.pp.domain.UserSubscription;
import com.tu.pp.repository.UserRepository;
import com.tu.pp.repository.UserSubscriptionRepository;
import com.tu.pp.service.MailService;
import com.tu.pp.web.rest.RedpandaKafkaResource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KafkaComponent {

    private static final Logger log = LoggerFactory.getLogger(RedpandaKafkaResource.class);

    private static final String KAFKA_TOPIC = "test";

    private final KafkaProperties kafkaProperties;
    private final KafkaProducer<String, String> producer;
    private final KafkaConsumer<String, String> consumer;

    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    private final ExecutorService executor;
    private final Map<TopicPartition, SubscriptionRenewalTask> activeTasks;
    private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit;
    private long lastCommitTime = System.currentTimeMillis();

    private final MailService emailService;

    public KafkaComponent(
        KafkaProperties kafkaProperties,
        UserRepository userRepository,
        UserSubscriptionRepository subscriptionRepository,
        MailService emailService
    ) {
        this.kafkaProperties = kafkaProperties;
        this.producer = new KafkaProducer<>(kafkaProperties.getProducerProps());
        this.consumer = new KafkaConsumer<>(kafkaProperties.getConsumerProps());

        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;

        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.activeTasks = new HashMap<>();
        this.offsetsToCommit = new HashMap<>();

        this.emailService = emailService;
    }

    @Scheduled(cron = "59 * * * * ?")
    public void publish() {
        List<UserSubscription> subscriptions = subscriptionRepository.findAllActive();
        subscriptions.forEach(this::produceRecord);
    }

    private void produceRecord(final UserSubscription subscription) {
        User user = Objects.requireNonNull(subscription.getUser());
        producer.send(new ProducerRecord<>(KAFKA_TOPIC, String.valueOf(subscription.getId()), String.valueOf(user.getId())));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void consume() {
        Properties config = new Properties();
        config.putAll(kafkaProperties.getConsumerProps());
        new MultithreadedKafkaConsumer(config, emailService, userRepository, subscriptionRepository);
    }
}
