package com.tu.pp.service;

import com.tu.pp.domain.UserSubscription;
import com.tu.pp.repository.UserSubscriptionRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link UserSubscription}.
 */
@Service
@Transactional
public class UserSubscriptionService {

    private final Logger log = LoggerFactory.getLogger(UserSubscriptionService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionService(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    /**
     * Save a userSubscription.
     *
     * @param userSubscription the entity to save.
     * @return the persisted entity.
     */
    public UserSubscription save(UserSubscription userSubscription) {
        log.debug("Request to save UserSubscription : {}", userSubscription);
        return userSubscriptionRepository.save(userSubscription);
    }

    /**
     * Partially update a userSubscription.
     *
     * @param userSubscription the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<UserSubscription> partialUpdate(UserSubscription userSubscription) {
        log.debug("Request to partially update UserSubscription : {}", userSubscription);

        return userSubscriptionRepository
            .findById(userSubscription.getId())
            .map(existingUserSubscription -> {
                if (userSubscription.getCancelled() != null) {
                    existingUserSubscription.setCancelled(userSubscription.getCancelled());
                }
                if (userSubscription.getCreatedBy() != null) {
                    existingUserSubscription.setCreatedBy(userSubscription.getCreatedBy());
                }
                if (userSubscription.getLastModifiedBy() != null) {
                    existingUserSubscription.setLastModifiedBy(userSubscription.getLastModifiedBy());
                }
                if (userSubscription.getCreatedDate() != null) {
                    existingUserSubscription.setCreatedDate(userSubscription.getCreatedDate());
                }
                if (userSubscription.getLastModifiedDate() != null) {
                    existingUserSubscription.setLastModifiedDate(userSubscription.getLastModifiedDate());
                }

                return existingUserSubscription;
            })
            .map(userSubscriptionRepository::save);
    }

    /**
     * Get all the userSubscriptions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<UserSubscription> findAll(Pageable pageable) {
        log.debug("Request to get all UserSubscriptions");
        return userSubscriptionRepository.findAll(pageable);
    }

    /**
     * Get one userSubscription by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<UserSubscription> findOne(Long id) {
        log.debug("Request to get UserSubscription : {}", id);
        return userSubscriptionRepository.findById(id);
    }

    /**
     * Delete the userSubscription by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete UserSubscription : {}", id);
        userSubscriptionRepository.deleteById(id);
    }
}
