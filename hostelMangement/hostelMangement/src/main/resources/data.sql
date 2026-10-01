-- Seed data. Every insert is guarded with NOT EXISTS so the script can run on every startup
-- (spring.sql.init.mode=always) without duplicating rows.

-- PATIENT
INSERT INTO patient (name, birth_date, email, gender, blood_group, created_at)
SELECT v.name, v.birth_date::date, v.email, v.gender, v.blood_group, CURRENT_TIMESTAMP
FROM (VALUES
    ('Sachin','2003-05-15','sachin@gmail.com','Male','O_POSITIVE'),
    ('Omkar','2002-11-20','omkar@gmail.com','Male','A_POSITIVE'),
    ('Priya','2004-02-10','priya@gmail.com','Female','B_POSITIVE'),
    ('Amit','2001-08-25','amit@gmail.com','Male','AB_NEGATIVE'),
    ('Neha','2003-12-05','neha@gmail.com','Female','O_NEGATIVE')
) AS v(name, birth_date, email, gender, blood_group)
WHERE NOT EXISTS (SELECT 1 FROM patient p WHERE p.email = v.email);

-- INSURANCE
INSERT INTO insurance (policy_number, provider, valid_until, created_at)
SELECT v.policy_number, v.provider, v.valid_until::date, CURRENT_TIMESTAMP
FROM (VALUES
    ('POL-1001','Star Health','2027-12-31'),
    ('POL-1002','HDFC Ergo','2028-06-30'),
    ('POL-1003','ICICI Lombard','2027-03-31')
) AS v(policy_number, provider, valid_until)
WHERE NOT EXISTS (SELECT 1 FROM insurance i WHERE i.policy_number = v.policy_number);

-- PATIENT "has" INSURANCE (only link the first patient with that email, and only if still unlinked)
UPDATE patient p
SET insurance_id = i.id
FROM (VALUES
    ('sachin@gmail.com','POL-1001'),
    ('priya@gmail.com','POL-1002'),
    ('neha@gmail.com','POL-1003')
) AS v(email, policy_number)
JOIN insurance i ON i.policy_number = v.policy_number
WHERE p.id = (SELECT MIN(p2.id) FROM patient p2 WHERE p2.email = v.email)
  AND p.insurance_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM patient p3 WHERE p3.insurance_id = i.id);

-- DOCTOR
INSERT INTO doctor (name, specialization, email, created_at)
SELECT v.name, v.specialization, v.email, CURRENT_TIMESTAMP
FROM (VALUES
    ('Dr. Rakesh Mehta','Cardiology','rakesh.mehta@hospital.com'),
    ('Dr. Sneha Kulkarni','Dermatology','sneha.kulkarni@hospital.com'),
    ('Dr. Arjun Nair','Orthopedics','arjun.nair@hospital.com'),
    ('Dr. Kavita Rao','Cardiology','kavita.rao@hospital.com')
) AS v(name, specialization, email)
WHERE NOT EXISTS (SELECT 1 FROM doctor d WHERE d.email = v.email);

-- DEPARTMENT (+ "head_of")
INSERT INTO department (name, head_doctor_id, created_at)
SELECT v.name, d.id, CURRENT_TIMESTAMP
FROM (VALUES
    ('Cardiology','rakesh.mehta@hospital.com'),
    ('Dermatology','sneha.kulkarni@hospital.com'),
    ('Orthopedics','arjun.nair@hospital.com')
) AS v(name, head_email)
JOIN doctor d ON d.email = v.head_email
WHERE NOT EXISTS (SELECT 1 FROM department dep WHERE dep.name = v.name);

-- DOCTOR_DEPARTMENT ("assigned_to")
INSERT INTO doctor_department (doctor_id, department_id)
SELECT d.id, dep.id
FROM (VALUES
    ('rakesh.mehta@hospital.com','Cardiology'),
    ('kavita.rao@hospital.com','Cardiology'),
    ('sneha.kulkarni@hospital.com','Dermatology'),
    ('arjun.nair@hospital.com','Orthopedics'),
    ('kavita.rao@hospital.com','Orthopedics')
) AS v(doctor_email, department_name)
JOIN doctor d ON d.email = v.doctor_email
JOIN department dep ON dep.name = v.department_name
WHERE NOT EXISTS (SELECT 1 FROM doctor_department dd WHERE dd.doctor_id = d.id AND dd.department_id = dep.id);

-- APPOINTMENT ("books" / "handles")
INSERT INTO appointment (appointment_time, reason, status, doctor_id, patient_id)
SELECT v.appointment_time::timestamp, v.reason, v.status, d.id,
       (SELECT MIN(p.id) FROM patient p WHERE p.email = v.patient_email)
FROM (VALUES
    ('2026-11-02 10:00:00','Chest pain','SCHEDULED','rakesh.mehta@hospital.com','sachin@gmail.com'),
    ('2026-11-02 11:00:00','Skin rash','SCHEDULED','sneha.kulkarni@hospital.com','priya@gmail.com'),
    ('2026-11-03 09:30:00','Knee pain','SCHEDULED','arjun.nair@hospital.com','omkar@gmail.com'),
    ('2026-11-04 14:00:00','Routine heart check-up','SCHEDULED','kavita.rao@hospital.com','neha@gmail.com'),
    ('2026-09-15 12:00:00','Back pain follow-up','COMPLETED','arjun.nair@hospital.com','amit@gmail.com')
) AS v(appointment_time, reason, status, doctor_email, patient_email)
JOIN doctor d ON d.email = v.doctor_email
WHERE NOT EXISTS (
    SELECT 1 FROM appointment a
    WHERE a.doctor_id = d.id AND a.appointment_time = v.appointment_time::timestamp
);
