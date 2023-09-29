/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.entity;

import de.conxult.pa.annotation.PaHistoryTable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
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
@Getter @Setter @Accessors(chain = true)
@PaHistoryTable
public class BaseEntity<T extends BaseEntity>
  implements Serializable {

    static public final UUID UUID_ZERO = new UUID(0, 0);
    static public final UUID UUID_FFFF = new UUID(-1, -1);

    @Column(name = "created_at", nullable = false)
    OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    UUID createdBy;

    @Column(name = "updated_at")
    OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    UUID updatedBy;

    public T setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        return (T) this;
    }

    public T setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
        return (T) this;
    }

    @PrePersist
    public void onPrePersist() {
        if (getCreatedBy() == null) {
            setCreatedBy(UUID_ZERO);
        }
        setCreatedAt(OffsetDateTime.now());
        if (this instanceof IdEntity idEntity && idEntity.getId() == null) {
            idEntity.setId(UUID.randomUUID());
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        if (getUpdatedBy() == null) {
            setUpdatedBy(UUID_ZERO);
        }
        setUpdatedAt(OffsetDateTime.now());
    }
}
