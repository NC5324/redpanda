package com.tu.pp.service;

import com.tu.pp.domain.*; // for static metamodels
import com.tu.pp.domain.UserSubscription;
import com.tu.pp.repository.UserSubscriptionRepository;
import com.tu.pp.service.criteria.UserSubscriptionCriteria;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link UserSubscription} entities in the database.
 * The main input is a {@link UserSubscriptionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link UserSubscription} or a {@link Page} of {@link UserSubscription} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class UserSubscriptionQueryService extends QueryService<UserSubscription> {

    private final Logger log = LoggerFactory.getLogger(UserSubscriptionQueryService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionQueryService(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    /**
     * Return a {@link List} of {@link UserSubscription} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<UserSubscription> findByCriteria(UserSubscriptionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<UserSubscription> specification = createSpecification(criteria);
        return userSubscriptionRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link UserSubscription} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<UserSubscription> findByCriteria(UserSubscriptionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<UserSubscription> specification = createSpecification(criteria);
        return userSubscriptionRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(UserSubscriptionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<UserSubscription> specification = createSpecification(criteria);
        return userSubscriptionRepository.count(specification);
    }

    /**
     * Function to convert {@link UserSubscriptionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<UserSubscription> createSpecification(UserSubscriptionCriteria criteria) {
        Specification<UserSubscription> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), UserSubscription_.id));
            }
            if (criteria.getCancelled() != null) {
                specification = specification.and(buildSpecification(criteria.getCancelled(), UserSubscription_.cancelled));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), UserSubscription_.createdBy));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), UserSubscription_.lastModifiedBy));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), UserSubscription_.createdDate));
            }
            if (criteria.getLastModifiedDate() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), UserSubscription_.lastModifiedDate));
            }
            if (criteria.getUserId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getUserId(), root -> root.join(UserSubscription_.user, JoinType.LEFT).get(User_.id))
                    );
            }
        }
        return specification;
    }
}
