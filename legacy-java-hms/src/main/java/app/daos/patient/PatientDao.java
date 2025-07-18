package app.daos.patient;

import app.models.patient.*;
import app.models.account.Sex;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Date;
import java.util.List;

@RegisterMapper({PatientMapper.class, IdentificationMapper.class, PatientIdsMapper.class})
public interface PatientDao {

    @SqlQuery("select * " +
            "from Patient " +
            "where (lower(FirstName) = :fname and lower(LastName) = :lname and DateOfBirth = :dob and Sex = :sex)")
    List<Patient> search(
            @Bind("fname") String firstName,
            @Bind("lname") String lastName,
            @Bind("dob") Date dateOfBirth,
            @Bind("sex") Sex sex
    );

    @SqlQuery("select p.* from patientids pid join patient p on p.id = pid.patientid where pid.idnumber = :id limit 1")
    Patient findByIdNumber(@Bind("id") String idNumber);

    @SqlQuery("INSERT INTO patient(mrn, email, phone, address, firstName, lastName, dob, type, status, sex, bloodGroup, created, " +
            "idNumber, idType, idExpiration, " +
            "modified, createdBy, picture, nationality, active, password) " +
            "VALUES (upper(substring(:lastName, 1, 2) || substring(:firstName, 1, 2) || to_hex(nextval('patient_id_seq'))), " +
            ":email, :phone, :address, :firstName, :lastName, :dob, :type, :status, :sex, :bloodGroup, :created, " +
            ":idNumber, :idType, :idExpiration, " +
            ":modified, :createdBy, :picture, :nationality, :active, :password) returning *")
    PatientIds add(@BindBean Patient patient);

    @SqlUpdate("INSERT INTO PatientIds (patientId, idType, idNumber, issued, expires, created) " +
            "VALUES (:patientId, :idType, :idNumber, :issued, :expires, :created)")
    @GetGeneratedKeys
    long addPatientId(@BindBean Identification id);

    @SqlUpdate("INSERT INTO PatientGuardian (firstName, lastName, address, phone1, " +
            "phone2, relationshipType, patientId, created) " +
            "VALUES (:firstName, :lastName, :address, :phone1, " +
            ":phone2, :relationshipType, :patientId, :created)")
    @GetGeneratedKeys
    long addPatientNextOfKin(@BindBean NextOfKin kin);

    @SqlUpdate("INSERT INTO PatientInsurance (insurer, membershipId, notes, patientId, created) " +
            "VALUES (:insurer, :membershipId, :notes, :patientId, :created)")
    @GetGeneratedKeys
    long addPatientInsurance(@BindBean Insurance insurance);

    @SqlUpdate("INSERT INTO PatientEmployment(employer, employeeId, patientId, created) " +
            "VALUES (:employer, :employeeId, :patientId, :created)")
    @GetGeneratedKeys
    long addPatientEmployment(@BindBean PatientEmployment employment);

    @SqlQuery("SELECT * FROM Patient WHERE id = :id LIMIT 1")
    Patient findById(@Bind("id") long id);

    @SqlQuery("SELECT * FROM PatientIds WHERE PatientId = :id")
    List<Identification> getPatientIds(@Bind("id") long patientId);

    @SqlUpdate("update patient set password = :password, email = :email, firstName = :firstName, lastName = :lastName, dob = :dob, " +
            "sex = :sex, bloodGroup = :bloodGroup, modified = :modified, status = :status, picture = :picture, nationality = :nationality, " +
            "idNumber = :idNumber, idType = :idType, idExpiration = :idExpiration, type = :type, active = :active " +
            "where id = :id")
    void updatePatient(@BindBean Patient subject);

    @SqlQuery("select * from patient where type = :type order by modified desc")
    List<Patient> getPatientsByType(@Bind("type") PatientType inpatient);
}
