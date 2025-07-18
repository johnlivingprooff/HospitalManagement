package app.daos.location;

import app.models.location.WorkStation;
import app.models.location.WorkstationMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(WorkstationMapper.class)
public interface WorkstationDao {
    @SqlUpdate("insert into workstation (code, districtId, name, address, created, modified, system, hidden, active) " +
            "values (:code, :districtId, :name, :address, :created, :modified, false, false, :active)")
    @GetGeneratedKeys
    long addWorkstation(@BindBean WorkStation workStation);

    @SqlQuery("select * from workstation where id = :id")
    WorkStation getWorkstationById(@Bind("id") long id);

    @SqlUpdate("update workstation set code = :code, districtId = :districtId, name = :name, address = :address, modified = :modified, active = :active where id = :id")
    void updateWorkstation(@BindBean WorkStation workStation);

    @SqlQuery("select w.*, d.name district from workstation w join district d on d.id = w.districtId where w.hidden = false order by w.modified desc")
    List<WorkStation> getWorkstations();

    @SqlQuery("select w.*, d.name district from workstation w join district d on d.id = w.districtId where w.hidden = false and w.active = true order by w.modified desc")
    List<WorkStation> getActiveWorkstations();
}
