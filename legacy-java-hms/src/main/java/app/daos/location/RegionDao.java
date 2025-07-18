package app.daos.location;

import app.models.location.Region;
import app.models.location.RegionMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(RegionMapper.class)
public interface RegionDao {
    @SqlUpdate("insert into region (code, name, created, modified, system, hidden, active) " +
            "values (:code, :name, :created, :modified, false, false, :active)")
    @GetGeneratedKeys
    long addRegion(@BindBean Region region);

    @SqlQuery("select * from region where id = :id")
    Region getRegionById(@Bind("id") long id);

    @SqlUpdate("update region set code = :code, name = :name, modified = :modified, active = :active where id = :id")
    void updateRegion(@BindBean Region region);

    @SqlQuery("select * from region where hidden = false order by modified desc")
    List<Region> getRegions();

    @SqlQuery("select * from region where hidden = false and active = true order by modified desc")
    List<Region> getActiveRegions();
}
