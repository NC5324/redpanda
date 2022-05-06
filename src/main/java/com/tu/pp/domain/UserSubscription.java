package com.tu.pp.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A UserSubscription.
 */
@Entity
@Table(name = "user_subscription")
public class UserSubscription extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "cancelled")
    private Boolean cancelled;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public UserSubscription id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCancelled() {
        return this.cancelled;
    }

    public UserSubscription cancelled(Boolean cancelled) {
        this.setCancelled(cancelled);
        return this;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserSubscription user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserSubscription)) {
            return false;
        }
        return id != null && id.equals(((UserSubscription) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserSubscription{" +
            "id=" + id +
            ", cancelled=" + cancelled +
            '}';
    }
}
