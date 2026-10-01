package com.sachinSuryawanshi.hostelMangement.repository;

import com.sachinSuryawanshi.hostelMangement.entity.Appointment;
import com.sachinSuryawanshi.hostelMangement.entity.type.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // fetch doctor + patient in the same query, the list view shows both names
    @EntityGraph(attributePaths = {"doctor", "patient"})
    List<Appointment> findAllByOrderByAppointmentTimeAsc();

    @EntityGraph(attributePaths = {"doctor", "patient"})
    List<Appointment> findByStatusOrderByAppointmentTimeAsc(AppointmentStatus status);

    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentTimeAsc(Long doctorId);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    boolean existsByDoctorIdAndStatusAndAppointmentTimeBetween(Long doctorId, AppointmentStatus status,
                                                               LocalDateTime from, LocalDateTime to);

    boolean existsByDoctorId(Long doctorId);
}
