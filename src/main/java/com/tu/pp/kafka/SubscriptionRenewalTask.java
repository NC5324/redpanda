package com.tu.pp.kafka;

import com.tu.pp.domain.User;
import com.tu.pp.domain.UserSubscription;
import com.tu.pp.repository.UserRepository;
import com.tu.pp.repository.UserSubscriptionRepository;
import com.tu.pp.service.MailService;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionRenewalTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(SubscriptionRenewalTask.class);

    private final List<ConsumerRecord<String, String>> records;
    private volatile boolean stopped = false;
    private volatile boolean started = false;
    private final CompletableFuture<Long> completion = new CompletableFuture<>();
    private volatile boolean finished = false;
    private final ReentrantLock startStopLock = new ReentrantLock();
    private final AtomicLong currentOffset = new AtomicLong(-1);

    private final MailService emailService;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    public SubscriptionRenewalTask(
        final List<ConsumerRecord<String, String>> records,
        final MailService mailService,
        UserRepository userRepository,
        final UserSubscriptionRepository subscriptionRepository
    ) {
        this.records = records;
        this.emailService = mailService;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public void run() {
        startStopLock.lock();
        if (stopped) {
            return;
        }
        started = true;
        startStopLock.unlock();

        records.forEach(this::consumeRecord);

        finished = true;
        completion.complete(currentOffset.get());
    }

    private void consumeRecord(final ConsumerRecord<String, String> record) {
        if (stopped) return;

        Long subId = Long.valueOf(record.key());
        Long userId = Long.valueOf(record.value());

        UserSubscription subscription = Objects.requireNonNull(subscriptionRepository.findById(subId).orElse(null));
        User user = Objects.requireNonNull(userRepository.findById(userId).orElse(null));

        log.debug("Renewing subscription '{}'", subscription);
        // Imagine there is payment processing

        emailService.sendSubscriptionRenewalMail(user, subscription);
        currentOffset.set(record.offset() + 1);
    }

    public long getCurrentOffset() {
        return currentOffset.get();
    }

    public void stop() {
        startStopLock.lock();
        this.stopped = true;
        if (!started) {
            finished = true;
            completion.complete(-1L);
        }
        startStopLock.unlock();
    }

    public long waitForCompletion() {
        try {
            return completion.get();
        } catch (InterruptedException | ExecutionException e) {
            return -1;
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
