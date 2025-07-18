package app.daos.location;

import app.models.location.Department;
import app.models.location.DepartmentMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(DepartmentMapper.class)
public interface DepartmentDao {
    @SqlUpdate("insert into department (code, workStationId, name, created, modified, system, hidden, active) " +
            "values (:code, :workStationId, :name, :created, :modified, false, false, :active)")
    @GetGeneratedKeys
    long addDepartment(@BindBean Department department);

    @SqlQuery("select * from department where id = :id")
    Department getDepartmentById(@Bind("id") long id);

    @SqlUpdate("update department set code = :code, workStationId = :workStationId, name = :name, modified = :modified, active = :active where id = :id")
    void updateDepartment(@BindBean Department department);

    @SqlQuery("select d.*, w.name workStation from department d join workstation w on w.id = d.workStationId where d.hidden = false order by d.modified desc")
    List<Department> getDepartments();

    @SqlQuery("select d.*, w.name workStation from department d join workstation w on w.id = d.workStationId where d.hidden = false and d.active = true order by d.modified desc")
    List<Department> getActiveDepartments();
}
