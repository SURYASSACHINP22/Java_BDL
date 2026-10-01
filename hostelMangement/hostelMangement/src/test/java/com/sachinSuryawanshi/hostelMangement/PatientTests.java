package com.sachinSuryawanshi.hostelMangement;

import com.sachinSuryawanshi.hostelMangement.DTO.BloodGroupCountResponseEntity;
import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.entity.type.BloodGroupType;
import com.sachinSuryawanshi.hostelMangement.repository.PatientRepository;
import com.sachinSuryawanshi.hostelMangement.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class PatientTests {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientService patientService;

    @Test
    public void testPatientRegistration(){
        List<Patient> patientList = patientRepository.findAll();
        System.out.println(patientList);

        long unique = System.currentTimeMillis();
        Patient p1 = new Patient();
        p1.setName("Patient" + unique % 1_000_000);
        p1.setEmail("patient" + unique + "@example.com");
        p1.setBirthDate(LocalDate.of(2000, 1, 1));
        p1.setGender("Male");
        p1.setBloodGroup(BloodGroupType.O_POSITIVE);
        patientRepository.save(p1);
    }

    @Test
    public void testPatientMethods(){
//        Patient p1 = patientService.getPatientById(1l);
        Patient p1 = patientRepository.findByBirthDate(LocalDate.of(1998,3,15));

        System.out.println(p1);
    }

    @Test
    public void testPatientMethodsJqlQuery(){
        List<Patient> p1 = patientRepository.findByBloodgroup(BloodGroupType.A_POSITIVE);
        for(Patient p: p1){
            System.out.println(p);
        }

        List<Patient> p2 = patientRepository.findByBornAfterDate(LocalDate.of(1992,3,14));
        for(Patient p: p2){
            System.out.println(p);
        }
    }

    @Test
    public void testBloodGroupCount(){
        List<Object[]> bloodGroupList = patientRepository.findByBloodgroupType();
        for(Object[] row: bloodGroupList){
            System.out.println(row[0] + " " + row[1]);
        }
    }

    @Test
    public void testBloodGroupCountProjection(){
        List<BloodGroupCountResponseEntity> bloodGroupList = patientRepository.findByBloodgroup();
        for(BloodGroupCountResponseEntity b: bloodGroupList){
            System.out.println(b);
        }
    }

    @Test
    public void testFindAllPatientsNativeQuery(){
        Page<Patient> patientList = patientRepository.findAllPatients(PageRequest.of(0, 5));
        System.out.println("Page " + patientList.getNumber() + " of " + patientList.getTotalPages() + ", total patients: " + patientList.getTotalElements());
        for(Patient p: patientList){
            System.out.println(p);
        }
    }

    @Test
    public void testUpdateNameWithId(){
        long unique = System.currentTimeMillis();
        Patient p1 = new Patient();
        p1.setName("Old" + unique % 1_000_000);
        p1.setEmail("update" + unique + "@example.com");
        p1.setBirthDate(LocalDate.of(1999, 6, 10));
        p1.setGender("Female");
        p1.setBloodGroup(BloodGroupType.B_POSITIVE);
        p1 = patientRepository.save(p1);

        int rowsUpdated = patientRepository.updateNameWithId("New" + unique % 1_000_000, p1.getId());
        System.out.println("Rows updated: " + rowsUpdated);

        Patient updated = patientRepository.findById(p1.getId()).orElseThrow();
        System.out.println(updated);
    }
}
