package app.daos.location;

import app.models.location.District;
import app.models.location.DistrictMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(DistrictMapper.class)
public interface DistrictDao {
    @SqlUpdate("insert into district (code, regionId, name, created, modified, system, hidden, active) " +
            "values (:code, :regionId, :name, :created, :modified, false, false, :active)")
    @GetGeneratedKeys
    long addDistrict(@BindBean District district);

    @SqlQuery("select * from district where id = :id")
    District getDistrictById(@Bind("id") long id);

    @SqlUpdate("update district set code = :code, regionId = :regionId, name = :name, modified = :modified, active = :active where id = :id")
    void updateDistrict(@BindBean District district);

    @SqlQuery("select d.*, r.name region from district d join region r on r.id = d.regionId where d.hidden = false order by d.modified desc")
    List<District> getDistricts();

    @SqlQuery("select d.*, r.name region from district d join region r on r.id = d.regionId where d.hidden = false and d.active = true order by d.modified desc")
    List<District> getActiveDistricts();
}
