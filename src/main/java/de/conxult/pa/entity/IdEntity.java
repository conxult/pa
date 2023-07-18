/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.entity;

import jakarta.persistence.PrePersist;
import java.io.Serializable;
import java.util.UUID;

/**
 * @param <T>
 *
 * @author joerg
 */
public interface IdEntity<T extends IdEntity>
  extends Serializable {

  public T setId(UUID id);
  public UUID getId();

  @PrePersist
  default public void onPrePersist() {
    if (getId() == null) {
      setId(generateId());
    }
  }

  public static UUID generateId() {
    return UUID.randomUUID();
  }
}
