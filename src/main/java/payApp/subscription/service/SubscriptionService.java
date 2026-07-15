package payApp.subscription.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payApp.subscription.model.Subscription;
import payApp.subscription.model.SubscriptionPeriod;
import payApp.subscription.model.SubscriptionStatus;
import payApp.subscription.model.SubscriptionType;
import payApp.subscription.repository.SubscriptionRepository;
import payApp.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription createDefaultSubscription(User user) {

        Subscription subscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(LocalDateTime.now())
                .expiryOn(LocalDateTime.now().plusMonths(1))
                .build();

        return subscriptionRepository.save(subscription);
    }
}
