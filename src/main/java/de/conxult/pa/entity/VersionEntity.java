/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;

/**
 * @param <T>
 *
 * @author joerg
 */
@MappedSuperclass
@Getter
public class VersionEntity<T extends VersionEntity>
  extends    BaseEntity<T>
  implements Serializable {

  @Column(name = "updated_at", nullable = false, updatable = false)
  OffsetDateTime updatedAt;

  @Column(name = "updated_by")
  UUID updatedBy;

  @Column(name = "version")
  int version;

  public T setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return (T)this;
  }

  public T setUpdatedBy(UUID updatedBy) {
    this.updatedBy = updatedBy;
    return (T)this;
  }

  @PreUpdate
  public void onPreUpdate() {
    setUpdatedAt(OffsetDateTime.now());
  }
}
