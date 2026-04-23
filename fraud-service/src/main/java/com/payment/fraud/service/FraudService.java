package com.payment.fraud.service;

import com.payment.fraud.dto.FraudScore;
import com.payment.fraud.dto.TransactionEvent;
import com.payment.fraud.kafka.FraudScoreProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudService {

    private final StringRedisTemplate redisTemplate;
    private final FraudScoreProducer fraudScoreProducer;

    @Value("${fraud.velocity-window-seconds}")
    private int velocityWindowSeconds;

    @Value("${fraud.velocity-threshold}")
    private int velocityThreshold;

    @Value("${fraud.high-amount-threshold-cents}")
    private long highAmountThresholdCents;

    @Value("${fraud.unusual-hour-start}")
    private int unusualHourStart;

    @Value("${fraud.unusual-hour-end}")
    private int unusualHourEnd;

    public void assessFraud(TransactionEvent event) {
        log.info("Assessing fraud for transaction {}",
                event.getTransactionId());

        int score = 0;
        List<String> signals = new ArrayList<>();

        // Signal 1 — velocity check using Redis
        int velocity = getAndIncrementVelocity(event.getCardNumber());
        if (velocity > velocityThreshold) {
            score += 40;
            signals.add("High velocity: " + velocity
                    + " transactions in last "
                    + velocityWindowSeconds + " seconds");
        }

        // Signal 2 — unusual hour (midnight to 6am)
        int hour = LocalDateTime.now().getHour();
        if (hour >= unusualHourStart && hour < unusualHourEnd) {
            score += 20;
            signals.add("Unusual hour: " + hour + ":00");
        }

        // Signal 3 — high amount
        if (event.getAmountInCents() > highAmountThresholdCents) {
            score += 25;
            signals.add("High amount: " + event.getAmountInCents()
                    + " cents");
        }

        // Signal 4 — low balance after transaction
        if (event.getBalanceInCents() != null
                && event.getBalanceInCents() < 10000) {
            score += 15;
            signals.add("Low remaining balance: "
                    + event.getBalanceInCents() + " cents");
        }

        // Cap score at 100
        score = Math.min(score, 100);

        FraudScore fraudScore = new FraudScore(
                event.getTransactionId(),
                event.getCardNumber(),
                event.getAmountInCents(),
                score,
                signals,
                LocalDateTime.now()
        );

        log.info("Transaction {} fraud score: {} signals: {}",
                event.getTransactionId(), score, signals);

        fraudScoreProducer.publishScore(fraudScore);
    }

    private int getAndIncrementVelocity(String cardNumber) {
        String key = "velocity:" + cardNumber;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, velocityWindowSeconds, TimeUnit.SECONDS);
        }

        return count.intValue();
    }
}