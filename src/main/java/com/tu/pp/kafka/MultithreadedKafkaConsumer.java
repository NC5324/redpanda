package com.tu.pp.kafka;

import com.tu.pp.repository.UserRepository;
import com.tu.pp.repository.UserSubscriptionRepository;
import com.tu.pp.service.MailService;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultithreadedKafkaConsumer implements Runnable {

    private static final String KAFKA_TOPIC = "test";

    private final KafkaConsumer<String, String> consumer;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Map<TopicPartition, SubscriptionRenewalTask> activeTasks = new HashMap<>();
    private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private long lastCommitTime = System.currentTimeMillis();

    private final Logger log = LoggerFactory.getLogger(MultithreadedKafkaConsumer.class);

    private final MailService emailService;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    public MultithreadedKafkaConsumer(
        Properties config,
        MailService emailService,
        UserRepository userRepository,
        UserSubscriptionRepository subscriptionRepository
    ) {
        consumer = new KafkaConsumer<>(config);
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singleton(KAFKA_TOPIC));
            while (!stopped.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                handleFetchedRecords(records);
                checkActiveTasks();
                commitOffsets();
            }
        } catch (WakeupException we) {
            if (!stopped.get()) throw we;
        } finally {
            consumer.close();
        }
    }

    private void handleFetchedRecords(ConsumerRecords<String, String> records) {
        if (records.count() > 0) {
            List<TopicPartition> partitionsToPause = new ArrayList<>();
            records
                .partitions()
                .forEach(partition -> {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                    SubscriptionRenewalTask task = new SubscriptionRenewalTask(
                        partitionRecords,
                        emailService,
                        userRepository,
                        subscriptionRepository
                    );
                    partitionsToPause.add(partition);
                    executor.submit(task);
                    activeTasks.put(partition, task);
                });
            consumer.pause(partitionsToPause);
        }
    }

    private void commitOffsets() {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - lastCommitTime > 5000) {
                if (!offsetsToCommit.isEmpty()) {
                    consumer.commitSync(offsetsToCommit);
                    offsetsToCommit.clear();
                }
                lastCommitTime = currentTimeMillis;
            }
        } catch (Exception e) {
            log.error("Failed to commit offsets!", e);
        }
    }

    private void checkActiveTasks() {
        List<TopicPartition> finishedTasksPartitions = new ArrayList<>();
        activeTasks.forEach((partition, task) -> {
            if (task.isFinished()) finishedTasksPartitions.add(partition);
            long offset = task.getCurrentOffset();
            if (offset > 0) offsetsToCommit.put(partition, new OffsetAndMetadata(offset));
        });
        finishedTasksPartitions.forEach(activeTasks::remove);
        consumer.resume(finishedTasksPartitions);
    }

    public void stopConsuming() {
        stopped.set(true);
        consumer.wakeup();
    }
}
