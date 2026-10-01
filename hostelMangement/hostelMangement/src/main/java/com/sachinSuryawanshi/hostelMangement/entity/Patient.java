package com.sachinSuryawanshi.hostelMangement.entity;

import com.sachinSuryawanshi.hostelMangement.entity.type.BloodGroupType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@Getter
@Setter
@Table(
        //name = "patient_tbl",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_patient_emails",columnNames = {"email"}),
                @UniqueConstraint(name = "unique_patient_name_birthdate",columnNames = {"name","birthDate"})
        },
        indexes = {
                @Index(name = "idx_patient_birth_data",columnList = "birthDate")
        }
)
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(name = "patient_name",unique = true,nullable = false)
    @Column(unique = true,nullable = false,length = 20)
    private String name;

    private LocalDate birthDate;

    @ToString.Exclude
    @Column(unique = true,nullable = false)
    private String email;

    private String gender;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private BloodGroupType bloodGroup;

    // "has" - patient owns the insurance_id FK, insurance lives and dies with the patient
    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "insurance_id", unique = true)
    private Insurance insurance;

    // "books"
    @ToString.Exclude
    @OneToMany(mappedBy = "patient", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();
}
