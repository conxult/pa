/*
 * Copyright by https://conxult.de
 */
package de.conxult.pa.annotation.test.entity;

import de.conxult.pa.annotation.PaHistoryTable;
import de.conxult.pa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */

@Table(schema = "some", name = "somes")
@PaHistoryTable
@Getter @Setter @Accessors(chain = true)
public class SomeEntity
    extends BaseEntity<SomeEntity> {

    @Id
    @Column(name = "id")
    UUID           id;

    @Column(name = "name")
    String         name;

    @Column(name = "last_login")
    OffsetDateTime lastLogin;
}
