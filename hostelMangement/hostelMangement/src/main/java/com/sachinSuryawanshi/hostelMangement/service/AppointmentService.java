package com.sachinSuryawanshi.hostelMangement.service;

import com.sachinSuryawanshi.hostelMangement.DTO.AppointmentRequestDto;
import com.sachinSuryawanshi.hostelMangement.entity.Appointment;
import com.sachinSuryawanshi.hostelMangement.entity.Doctor;
import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.entity.type.AppointmentStatus;
import com.sachinSuryawanshi.hostelMangement.exception.BusinessRuleException;
import com.sachinSuryawanshi.hostelMangement.exception.ResourceNotFoundException;
import com.sachinSuryawanshi.hostelMangement.repository.AppointmentRepository;
import com.sachinSuryawanshi.hostelMangement.repository.DoctorRepository;
import com.sachinSuryawanshi.hostelMangement.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    // a doctor can't have two scheduled appointments closer than this
    private static final long SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments(AppointmentStatus status) {
        if (status == null) {
            return appointmentRepository.findAllByOrderByAppointmentTimeAsc();
        }
        return appointmentRepository.findByStatusOrderByAppointmentTimeAsc(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsOfPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsOfDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }
        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeAsc(doctorId);
    }

    @Transactional
    public Appointment createNewAppointment(AppointmentRequestDto request) {
        if (request.doctorId() == null || request.patientId() == null) {
            throw new IllegalArgumentException("doctorId and patientId are required");
        }
        if (request.appointmentTime() == null || request.appointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("appointmentTime must be in the future");
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));
        ensureDoctorIsFree(doctor, request.appointmentTime());

        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setReason(request.reason());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        patient.getAppointments().add(appointment); // keep both sides in sync
        doctor.getAppointments().add(appointment);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment reAssignAppointmentToAnotherDoctor(Long appointmentId, Long doctorId) {
        Appointment appointment = getAppointment(appointmentId);
        ensureScheduled(appointment);

        Doctor newDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        if (appointment.getDoctor().getId().equals(doctorId)) {
            throw new BusinessRuleException("Appointment " + appointmentId + " is already with doctor " + doctorId);
        }
        ensureDoctorIsFree(newDoctor, appointment.getAppointmentTime());

        appointment.getDoctor().getAppointments().remove(appointment);
        appointment.setDoctor(newDoctor);
        newDoctor.getAppointments().add(appointment);
        return appointment;
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = getAppointment(appointmentId);
        ensureScheduled(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointment;
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId) {
        Appointment appointment = getAppointment(appointmentId);
        ensureScheduled(appointment);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointment;
    }

    private void ensureScheduled(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException("Appointment " + appointment.getId() + " is already " + appointment.getStatus());
        }
    }

    private void ensureDoctorIsFree(Doctor doctor, LocalDateTime time) {
        boolean busy = appointmentRepository.existsByDoctorIdAndStatusAndAppointmentTimeBetween(
                doctor.getId(),
                AppointmentStatus.SCHEDULED,
                time.minusMinutes(SLOT_MINUTES - 1),
                time.plusMinutes(SLOT_MINUTES - 1));
        if (busy) {
            throw new BusinessRuleException("Doctor " + doctor.getId() + " already has an appointment around " + time);
        }
    }
}
