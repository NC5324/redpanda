package com.tu.pp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tu.pp.IntegrationTest;
import com.tu.pp.domain.User;
import com.tu.pp.domain.UserSubscription;
import com.tu.pp.repository.UserSubscriptionRepository;
import com.tu.pp.service.criteria.UserSubscriptionCriteria;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link UserSubscriptionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UserSubscriptionResourceIT {

    private static final Boolean DEFAULT_CANCELLED = false;
    private static final Boolean UPDATED_CANCELLED = true;

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_MODIFIED_BY = "AAAAAAAAAA";
    private static final String UPDATED_LAST_MODIFIED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_LAST_MODIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_MODIFIED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/user-subscriptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserSubscriptionMockMvc;

    private UserSubscription userSubscription;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserSubscription createEntity(EntityManager em) {
        UserSubscription userSubscription = new UserSubscription().cancelled(DEFAULT_CANCELLED);
        return userSubscription;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserSubscription createUpdatedEntity(EntityManager em) {
        UserSubscription userSubscription = new UserSubscription().cancelled(UPDATED_CANCELLED);
        return userSubscription;
    }

    @BeforeEach
    public void initTest() {
        userSubscription = createEntity(em);
    }

    @Test
    @Transactional
    void createUserSubscription() throws Exception {
        int databaseSizeBeforeCreate = userSubscriptionRepository.findAll().size();
        // Create the UserSubscription
        restUserSubscriptionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isCreated());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeCreate + 1);
        UserSubscription testUserSubscription = userSubscriptionList.get(userSubscriptionList.size() - 1);
        assertThat(testUserSubscription.getCancelled()).isEqualTo(DEFAULT_CANCELLED);
    }

    @Test
    @Transactional
    void createUserSubscriptionWithExistingId() throws Exception {
        // Create the UserSubscription with an existing ID
        userSubscription.setId(1L);

        int databaseSizeBeforeCreate = userSubscriptionRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserSubscriptionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCreatedByIsRequired() throws Exception {
        int databaseSizeBeforeTest = userSubscriptionRepository.findAll().size();
        // set the field null
        userSubscription.setCreatedBy(null);

        // Create the UserSubscription, which fails.

        restUserSubscriptionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastModifiedByIsRequired() throws Exception {
        int databaseSizeBeforeTest = userSubscriptionRepository.findAll().size();
        // set the field null
        userSubscription.setLastModifiedBy(null);

        // Create the UserSubscription, which fails.

        restUserSubscriptionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllUserSubscriptions() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userSubscription.getId().intValue())))
            .andExpect(jsonPath("$.[*].cancelled").value(hasItem(DEFAULT_CANCELLED.booleanValue())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].lastModifiedBy").value(hasItem(DEFAULT_LAST_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));
    }

    @Test
    @Transactional
    void getUserSubscription() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get the userSubscription
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL_ID, userSubscription.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(userSubscription.getId().intValue()))
            .andExpect(jsonPath("$.cancelled").value(DEFAULT_CANCELLED.booleanValue()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.lastModifiedBy").value(DEFAULT_LAST_MODIFIED_BY))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(DEFAULT_LAST_MODIFIED_DATE.toString()));
    }

    @Test
    @Transactional
    void getUserSubscriptionsByIdFiltering() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        Long id = userSubscription.getId();

        defaultUserSubscriptionShouldBeFound("id.equals=" + id);
        defaultUserSubscriptionShouldNotBeFound("id.notEquals=" + id);

        defaultUserSubscriptionShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultUserSubscriptionShouldNotBeFound("id.greaterThan=" + id);

        defaultUserSubscriptionShouldBeFound("id.lessThanOrEqual=" + id);
        defaultUserSubscriptionShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCancelledIsEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where cancelled equals to DEFAULT_CANCELLED
        defaultUserSubscriptionShouldBeFound("cancelled.equals=" + DEFAULT_CANCELLED);

        // Get all the userSubscriptionList where cancelled equals to UPDATED_CANCELLED
        defaultUserSubscriptionShouldNotBeFound("cancelled.equals=" + UPDATED_CANCELLED);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCancelledIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where cancelled not equals to DEFAULT_CANCELLED
        defaultUserSubscriptionShouldNotBeFound("cancelled.notEquals=" + DEFAULT_CANCELLED);

        // Get all the userSubscriptionList where cancelled not equals to UPDATED_CANCELLED
        defaultUserSubscriptionShouldBeFound("cancelled.notEquals=" + UPDATED_CANCELLED);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCancelledIsInShouldWork() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where cancelled in DEFAULT_CANCELLED or UPDATED_CANCELLED
        defaultUserSubscriptionShouldBeFound("cancelled.in=" + DEFAULT_CANCELLED + "," + UPDATED_CANCELLED);

        // Get all the userSubscriptionList where cancelled equals to UPDATED_CANCELLED
        defaultUserSubscriptionShouldNotBeFound("cancelled.in=" + UPDATED_CANCELLED);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCancelledIsNullOrNotNull() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where cancelled is not null
        defaultUserSubscriptionShouldBeFound("cancelled.specified=true");

        // Get all the userSubscriptionList where cancelled is null
        defaultUserSubscriptionShouldNotBeFound("cancelled.specified=false");
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedByIsEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdBy equals to DEFAULT_CREATED_BY
        defaultUserSubscriptionShouldBeFound("createdBy.equals=" + DEFAULT_CREATED_BY);

        // Get all the userSubscriptionList where createdBy equals to UPDATED_CREATED_BY
        defaultUserSubscriptionShouldNotBeFound("createdBy.equals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdBy not equals to DEFAULT_CREATED_BY
        defaultUserSubscriptionShouldNotBeFound("createdBy.notEquals=" + DEFAULT_CREATED_BY);

        // Get all the userSubscriptionList where createdBy not equals to UPDATED_CREATED_BY
        defaultUserSubscriptionShouldBeFound("createdBy.notEquals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedByIsInShouldWork() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdBy in DEFAULT_CREATED_BY or UPDATED_CREATED_BY
        defaultUserSubscriptionShouldBeFound("createdBy.in=" + DEFAULT_CREATED_BY + "," + UPDATED_CREATED_BY);

        // Get all the userSubscriptionList where createdBy equals to UPDATED_CREATED_BY
        defaultUserSubscriptionShouldNotBeFound("createdBy.in=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdBy is not null
        defaultUserSubscriptionShouldBeFound("createdBy.specified=true");

        // Get all the userSubscriptionList where createdBy is null
        defaultUserSubscriptionShouldNotBeFound("createdBy.specified=false");
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedByContainsSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdBy contains DEFAULT_CREATED_BY
        defaultUserSubscriptionShouldBeFound("createdBy.contains=" + DEFAULT_CREATED_BY);

        // Get all the userSubscriptionList where createdBy contains UPDATED_CREATED_BY
        defaultUserSubscriptionShouldNotBeFound("createdBy.contains=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedByNotContainsSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdBy does not contain DEFAULT_CREATED_BY
        defaultUserSubscriptionShouldNotBeFound("createdBy.doesNotContain=" + DEFAULT_CREATED_BY);

        // Get all the userSubscriptionList where createdBy does not contain UPDATED_CREATED_BY
        defaultUserSubscriptionShouldBeFound("createdBy.doesNotContain=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedByIsEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedBy equals to DEFAULT_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldBeFound("lastModifiedBy.equals=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the userSubscriptionList where lastModifiedBy equals to UPDATED_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldNotBeFound("lastModifiedBy.equals=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedBy not equals to DEFAULT_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldNotBeFound("lastModifiedBy.notEquals=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the userSubscriptionList where lastModifiedBy not equals to UPDATED_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldBeFound("lastModifiedBy.notEquals=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedByIsInShouldWork() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedBy in DEFAULT_LAST_MODIFIED_BY or UPDATED_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldBeFound("lastModifiedBy.in=" + DEFAULT_LAST_MODIFIED_BY + "," + UPDATED_LAST_MODIFIED_BY);

        // Get all the userSubscriptionList where lastModifiedBy equals to UPDATED_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldNotBeFound("lastModifiedBy.in=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedBy is not null
        defaultUserSubscriptionShouldBeFound("lastModifiedBy.specified=true");

        // Get all the userSubscriptionList where lastModifiedBy is null
        defaultUserSubscriptionShouldNotBeFound("lastModifiedBy.specified=false");
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedByContainsSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedBy contains DEFAULT_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldBeFound("lastModifiedBy.contains=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the userSubscriptionList where lastModifiedBy contains UPDATED_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldNotBeFound("lastModifiedBy.contains=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedByNotContainsSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedBy does not contain DEFAULT_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldNotBeFound("lastModifiedBy.doesNotContain=" + DEFAULT_LAST_MODIFIED_BY);

        // Get all the userSubscriptionList where lastModifiedBy does not contain UPDATED_LAST_MODIFIED_BY
        defaultUserSubscriptionShouldBeFound("lastModifiedBy.doesNotContain=" + UPDATED_LAST_MODIFIED_BY);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdDate equals to DEFAULT_CREATED_DATE
        defaultUserSubscriptionShouldBeFound("createdDate.equals=" + DEFAULT_CREATED_DATE);

        // Get all the userSubscriptionList where createdDate equals to UPDATED_CREATED_DATE
        defaultUserSubscriptionShouldNotBeFound("createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdDate not equals to DEFAULT_CREATED_DATE
        defaultUserSubscriptionShouldNotBeFound("createdDate.notEquals=" + DEFAULT_CREATED_DATE);

        // Get all the userSubscriptionList where createdDate not equals to UPDATED_CREATED_DATE
        defaultUserSubscriptionShouldBeFound("createdDate.notEquals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdDate in DEFAULT_CREATED_DATE or UPDATED_CREATED_DATE
        defaultUserSubscriptionShouldBeFound("createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE);

        // Get all the userSubscriptionList where createdDate equals to UPDATED_CREATED_DATE
        defaultUserSubscriptionShouldNotBeFound("createdDate.in=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where createdDate is not null
        defaultUserSubscriptionShouldBeFound("createdDate.specified=true");

        // Get all the userSubscriptionList where createdDate is null
        defaultUserSubscriptionShouldNotBeFound("createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedDate equals to DEFAULT_LAST_MODIFIED_DATE
        defaultUserSubscriptionShouldBeFound("lastModifiedDate.equals=" + DEFAULT_LAST_MODIFIED_DATE);

        // Get all the userSubscriptionList where lastModifiedDate equals to UPDATED_LAST_MODIFIED_DATE
        defaultUserSubscriptionShouldNotBeFound("lastModifiedDate.equals=" + UPDATED_LAST_MODIFIED_DATE);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedDate not equals to DEFAULT_LAST_MODIFIED_DATE
        defaultUserSubscriptionShouldNotBeFound("lastModifiedDate.notEquals=" + DEFAULT_LAST_MODIFIED_DATE);

        // Get all the userSubscriptionList where lastModifiedDate not equals to UPDATED_LAST_MODIFIED_DATE
        defaultUserSubscriptionShouldBeFound("lastModifiedDate.notEquals=" + UPDATED_LAST_MODIFIED_DATE);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedDateIsInShouldWork() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedDate in DEFAULT_LAST_MODIFIED_DATE or UPDATED_LAST_MODIFIED_DATE
        defaultUserSubscriptionShouldBeFound("lastModifiedDate.in=" + DEFAULT_LAST_MODIFIED_DATE + "," + UPDATED_LAST_MODIFIED_DATE);

        // Get all the userSubscriptionList where lastModifiedDate equals to UPDATED_LAST_MODIFIED_DATE
        defaultUserSubscriptionShouldNotBeFound("lastModifiedDate.in=" + UPDATED_LAST_MODIFIED_DATE);
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByLastModifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList where lastModifiedDate is not null
        defaultUserSubscriptionShouldBeFound("lastModifiedDate.specified=true");

        // Get all the userSubscriptionList where lastModifiedDate is null
        defaultUserSubscriptionShouldNotBeFound("lastModifiedDate.specified=false");
    }

    @Test
    @Transactional
    void getAllUserSubscriptionsByUserIsEqualToSomething() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            user = UserResourceIT.createEntity(em);
            em.persist(user);
            em.flush();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        userSubscription.setUser(user);
        userSubscriptionRepository.saveAndFlush(userSubscription);
        Long userId = user.getId();

        // Get all the userSubscriptionList where user equals to userId
        defaultUserSubscriptionShouldBeFound("userId.equals=" + userId);

        // Get all the userSubscriptionList where user equals to (userId + 1)
        defaultUserSubscriptionShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultUserSubscriptionShouldBeFound(String filter) throws Exception {
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userSubscription.getId().intValue())))
            .andExpect(jsonPath("$.[*].cancelled").value(hasItem(DEFAULT_CANCELLED.booleanValue())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].lastModifiedBy").value(hasItem(DEFAULT_LAST_MODIFIED_BY)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));

        // Check, that the count call also returns 1
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultUserSubscriptionShouldNotBeFound(String filter) throws Exception {
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingUserSubscription() throws Exception {
        // Get the userSubscription
        restUserSubscriptionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewUserSubscription() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();

        // Update the userSubscription
        UserSubscription updatedUserSubscription = userSubscriptionRepository.findById(userSubscription.getId()).get();
        // Disconnect from session so that the updates on updatedUserSubscription are not directly saved in db
        em.detach(updatedUserSubscription);
        updatedUserSubscription.cancelled(UPDATED_CANCELLED);

        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedUserSubscription.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedUserSubscription))
            )
            .andExpect(status().isOk());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
        UserSubscription testUserSubscription = userSubscriptionList.get(userSubscriptionList.size() - 1);
        assertThat(testUserSubscription.getCancelled()).isEqualTo(UPDATED_CANCELLED);
        assertThat(testUserSubscription.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testUserSubscription.getLastModifiedBy()).isEqualTo(UPDATED_LAST_MODIFIED_BY);
        assertThat(testUserSubscription.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testUserSubscription.getLastModifiedDate()).isEqualTo(UPDATED_LAST_MODIFIED_DATE);
    }

    @Test
    @Transactional
    void putNonExistingUserSubscription() throws Exception {
        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();
        userSubscription.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, userSubscription.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchUserSubscription() throws Exception {
        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();
        userSubscription.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUserSubscription() throws Exception {
        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();
        userSubscription.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateUserSubscriptionWithPatch() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();

        // Update the userSubscription using partial update
        UserSubscription partialUpdatedUserSubscription = new UserSubscription();
        partialUpdatedUserSubscription.setId(userSubscription.getId());

        partialUpdatedUserSubscription.cancelled(UPDATED_CANCELLED);

        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUserSubscription))
            )
            .andExpect(status().isOk());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
        UserSubscription testUserSubscription = userSubscriptionList.get(userSubscriptionList.size() - 1);
        assertThat(testUserSubscription.getCancelled()).isEqualTo(UPDATED_CANCELLED);
        assertThat(testUserSubscription.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testUserSubscription.getLastModifiedBy()).isEqualTo(DEFAULT_LAST_MODIFIED_BY);
        assertThat(testUserSubscription.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testUserSubscription.getLastModifiedDate()).isEqualTo(DEFAULT_LAST_MODIFIED_DATE);
    }

    @Test
    @Transactional
    void fullUpdateUserSubscriptionWithPatch() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();

        // Update the userSubscription using partial update
        UserSubscription partialUpdatedUserSubscription = new UserSubscription();
        partialUpdatedUserSubscription.setId(userSubscription.getId());

        partialUpdatedUserSubscription.cancelled(UPDATED_CANCELLED);

        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUserSubscription))
            )
            .andExpect(status().isOk());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
        UserSubscription testUserSubscription = userSubscriptionList.get(userSubscriptionList.size() - 1);
        assertThat(testUserSubscription.getCancelled()).isEqualTo(UPDATED_CANCELLED);
        assertThat(testUserSubscription.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testUserSubscription.getLastModifiedBy()).isEqualTo(UPDATED_LAST_MODIFIED_BY);
        assertThat(testUserSubscription.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testUserSubscription.getLastModifiedDate()).isEqualTo(UPDATED_LAST_MODIFIED_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingUserSubscription() throws Exception {
        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();
        userSubscription.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, userSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUserSubscription() throws Exception {
        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();
        userSubscription.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUserSubscription() throws Exception {
        int databaseSizeBeforeUpdate = userSubscriptionRepository.findAll().size();
        userSubscription.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userSubscription))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserSubscription in the database
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteUserSubscription() throws Exception {
        // Initialize the database
        userSubscriptionRepository.saveAndFlush(userSubscription);

        int databaseSizeBeforeDelete = userSubscriptionRepository.findAll().size();

        // Delete the userSubscription
        restUserSubscriptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, userSubscription.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserSubscription> userSubscriptionList = userSubscriptionRepository.findAll();
        assertThat(userSubscriptionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
