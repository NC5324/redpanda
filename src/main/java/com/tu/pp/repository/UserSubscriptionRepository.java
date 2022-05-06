package com.tu.pp.repository;

import com.tu.pp.domain.UserSubscription;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the UserSubscription entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long>, JpaSpecificationExecutor<UserSubscription> {
    @Query("select userSubscription from UserSubscription userSubscription where userSubscription.user.login = ?#{principal.username}")
    List<UserSubscription> findByUserIsCurrentUser();

    @Query("select userSubscription from UserSubscription userSubscription where userSubscription.cancelled = false")
    List<UserSubscription> findAllActive();
}
