package com.sachinSuryawanshi.hostelMangement;

import com.sachinSuryawanshi.hostelMangement.DTO.AppointmentRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.DepartmentRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.DoctorRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.InsuranceRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.PatientRequestDto;
import com.sachinSuryawanshi.hostelMangement.entity.Appointment;
import com.sachinSuryawanshi.hostelMangement.entity.Department;
import com.sachinSuryawanshi.hostelMangement.entity.Doctor;
import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.entity.type.AppointmentStatus;
import com.sachinSuryawanshi.hostelMangement.entity.type.BloodGroupType;
import com.sachinSuryawanshi.hostelMangement.exception.BusinessRuleException;
import com.sachinSuryawanshi.hostelMangement.repository.AppointmentRepository;
import com.sachinSuryawanshi.hostelMangement.repository.InsuranceRepository;
import com.sachinSuryawanshi.hostelMangement.service.AppointmentService;
import com.sachinSuryawanshi.hostelMangement.service.DepartmentService;
import com.sachinSuryawanshi.hostelMangement.service.DoctorService;
import com.sachinSuryawanshi.hostelMangement.service.InsuranceService;
import com.sachinSuryawanshi.hostelMangement.service.PatientService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// every test is rolled back, so nothing is left in the database
@SpringBootTest
@Transactional
public class HospitalServiceTests {

    @Autowired private PatientService patientService;
    @Autowired private DoctorService doctorService;
    @Autowired private DepartmentService departmentService;
    @Autowired private InsuranceService insuranceService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private InsuranceRepository insuranceRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private EntityManager entityManager;

    private final long unique = System.nanoTime();

    private Patient newPatient(String suffix) {
        return patientService.createPatient(new PatientRequestDto(
                "P" + (unique % 1_000_000) + suffix, "Male", LocalDate.of(2000, 1, 1),
                "p" + unique + suffix + "@test.com", BloodGroupType.A_POSITIVE));
    }

    private Doctor newDoctor(String suffix) {
        return doctorService.createDoctor(new DoctorRequestDto(
                "Dr " + suffix, "Cardiology", "d" + unique + suffix + "@test.com"));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testAssignAndRemoveInsurance() {
        Patient patient = newPatient("a");

        insuranceService.assignInsuranceToPatient(patient.getId(),
                new InsuranceRequestDto("TEST-" + unique, "Star Health", LocalDate.now().plusYears(1)));
        flushAndClear();

        Patient reloaded = patientService.getPatient(patient.getId());
        assertNotNull(reloaded.getInsurance());
        assertEquals("Star Health", reloaded.getInsurance().getProvider());

        insuranceService.removeInsuranceFromPatient(patient.getId());
        flushAndClear();

        assertNull(patientService.getPatient(patient.getId()).getInsurance());
        assertTrue(insuranceRepository.findByPolicyNumber("TEST-" + unique).isEmpty(), "orphan insurance should be deleted");
    }

    @Test
    public void testDuplicatePolicyNumberRejected() {
        Patient p1 = newPatient("a");
        Patient p2 = newPatient("b");
        InsuranceRequestDto policy = new InsuranceRequestDto("DUP-" + unique, "HDFC", LocalDate.now().plusYears(1));

        insuranceService.assignInsuranceToPatient(p1.getId(), policy);
        flushAndClear();

        assertThrows(BusinessRuleException.class, () -> insuranceService.assignInsuranceToPatient(p2.getId(), policy));
    }

    @Test
    public void testCreateAppointmentAndPreventDoubleBooking() {
        Patient patient = newPatient("a");
        Doctor doctor = newDoctor("a");
        LocalDateTime time = LocalDateTime.now().plusDays(3).withNano(0);

        Appointment appointment = appointmentService.createNewAppointment(
                new AppointmentRequestDto(time, "Checkup", doctor.getId(), patient.getId()));
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());

        // same doctor, 10 minutes later -> overlaps the 30 minute slot
        assertThrows(BusinessRuleException.class, () -> appointmentService.createNewAppointment(
                new AppointmentRequestDto(time.plusMinutes(10), "Other", doctor.getId(), patient.getId())));

        // a past appointment time is rejected
        assertThrows(IllegalArgumentException.class, () -> appointmentService.createNewAppointment(
                new AppointmentRequestDto(LocalDateTime.now().minusDays(1), "Late", doctor.getId(), patient.getId())));
    }

    @Test
    public void testReassignCancelAndComplete() {
        Patient patient = newPatient("a");
        Doctor d1 = newDoctor("a");
        Doctor d2 = newDoctor("b");
        LocalDateTime time = LocalDateTime.now().plusDays(5).withNano(0);

        Appointment appointment = appointmentService.createNewAppointment(
                new AppointmentRequestDto(time, "Fever", d1.getId(), patient.getId()));

        appointmentService.reAssignAppointmentToAnotherDoctor(appointment.getId(), d2.getId());
        flushAndClear();
        assertEquals(d2.getId(), appointmentService.getAppointment(appointment.getId()).getDoctor().getId());

        appointmentService.cancelAppointment(appointment.getId());
        flushAndClear();
        assertEquals(AppointmentStatus.CANCELLED, appointmentService.getAppointment(appointment.getId()).getStatus());

        // a cancelled appointment can't be completed, and the slot is free again for d2
        assertThrows(BusinessRuleException.class, () -> appointmentService.completeAppointment(appointment.getId()));
        Appointment rebooked = appointmentService.createNewAppointment(
                new AppointmentRequestDto(time, "Fever again", d2.getId(), patient.getId()));
        appointmentService.completeAppointment(rebooked.getId());
        assertEquals(AppointmentStatus.COMPLETED, rebooked.getStatus());
    }

    @Test
    public void testDepartmentHeadAndMembers() {
        Doctor head = newDoctor("head");
        Doctor member = newDoctor("member");

        Department department = departmentService.createDepartment(new DepartmentRequestDto("Dept" + unique, head.getId()));
        departmentService.addDoctorToDepartment(department.getId(), member.getId());
        flushAndClear();

        Department reloaded = departmentService.getDepartment(department.getId());
        assertEquals(head.getId(), reloaded.getHeadDoctor().getId());
        assertEquals(2, reloaded.getDoctors().size(), "head is added as a member automatically");

        // a doctor can head only one department
        assertThrows(BusinessRuleException.class,
                () -> departmentService.createDepartment(new DepartmentRequestDto("Other" + unique, head.getId())));
        // the head can't be removed from their own department
        assertThrows(BusinessRuleException.class,
                () -> departmentService.removeDoctorFromDepartment(department.getId(), head.getId()));

        departmentService.changeHeadDoctor(department.getId(), member.getId());
        departmentService.removeDoctorFromDepartment(department.getId(), head.getId());
        flushAndClear();

        reloaded = departmentService.getDepartment(department.getId());
        assertEquals(member.getId(), reloaded.getHeadDoctor().getId());
        assertEquals(1, reloaded.getDoctors().size());
    }

    @Test
    public void testDeleteDoctorRules() {
        Doctor head = newDoctor("head");
        Doctor withAppointment = newDoctor("busy");
        Doctor free = newDoctor("free");
        Patient patient = newPatient("a");

        Department department = departmentService.createDepartment(new DepartmentRequestDto("Dept" + unique, head.getId()));
        departmentService.addDoctorToDepartment(department.getId(), free.getId());
        appointmentService.createNewAppointment(new AppointmentRequestDto(
                LocalDateTime.now().plusDays(2), "Test", withAppointment.getId(), patient.getId()));
        flushAndClear();

        assertThrows(BusinessRuleException.class, () -> doctorService.deleteDoctor(head.getId()));
        assertThrows(BusinessRuleException.class, () -> doctorService.deleteDoctor(withAppointment.getId()));

        doctorService.deleteDoctor(free.getId()); // also removes the doctor_department row
        flushAndClear();
        assertEquals(1, departmentService.getDepartment(department.getId()).getDoctors().size());
    }

    @Test
    public void testDeletePatientCascades() {
        Patient patient = newPatient("a");
        Doctor doctor = newDoctor("a");
        insuranceService.assignInsuranceToPatient(patient.getId(),
                new InsuranceRequestDto("DEL-" + unique, "Star Health", LocalDate.now().plusYears(1)));
        Appointment appointment = appointmentService.createNewAppointment(new AppointmentRequestDto(
                LocalDateTime.now().plusDays(1), "Test", doctor.getId(), patient.getId()));
        flushAndClear();

        patientService.deletePatient(patient.getId());
        flushAndClear();

        assertFalse(appointmentRepository.existsById(appointment.getId()));
        assertTrue(insuranceRepository.findByPolicyNumber("DEL-" + unique).isEmpty());
    }
}
