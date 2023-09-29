/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.entity;

import de.conxult.pa.annotation.PaHistoryTable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @param <T>
 *
 * @author joerg
 */
@MappedSuperclass
@PaHistoryTable
@Getter @Setter @Accessors(chain = true)
public class VersionEntity<T extends VersionEntity>
  extends    BaseEntity<T>
  implements Serializable {

  @Column(name = "updated_at")
  OffsetDateTime updatedAt;

  @Column(name = "updated_by")
  UUID updatedBy;

  @Column(name = "version")
  int version = 0;

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
