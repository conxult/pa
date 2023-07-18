/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @param <T>
 *
 * @author joerg
 */
@MappedSuperclass
public class BaseEntity<T extends BaseEntity>
  implements Serializable {

  @Column(name = "created_at", nullable = false, updatable = false)
  OffsetDateTime createdAt;

  @Column(name = "created_by")
  UUID createdBy;

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public T setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return (T)this;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public T setCreatedBy(UUID createdBy) {
    this.createdBy = createdBy;
    return (T)this;
  }

  @PrePersist
  public void onPrePersist() {
    if (getCreatedAt() == null) {
      setCreatedAt(OffsetDateTime.now());
    }
  }
}
