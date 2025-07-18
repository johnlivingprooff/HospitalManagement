package app.services.location;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.location.DepartmentDao;
import app.daos.location.DistrictDao;
import app.daos.location.RegionDao;
import app.daos.location.WorkstationDao;
import app.models.location.Department;
import app.models.location.District;
import app.models.location.Region;
import app.models.location.WorkStation;
import app.services.audit.AuditService;

import java.util.List;
import java.util.Locale;

@ServiceDescriptor
@SuppressWarnings("unused")
public final class LocationService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public LocationService(Configuration configuration) {
        super(configuration);
    }

    public void addRegion(Region region, AuditService.LogEntry entry) {
        region.setId(withDao(RegionDao.class).addRegion(region));
        auditLog(entry.getSubject() + " added region " + region.toString(), entry);
    }

    public void updateRegion(Region region, AuditService.LogEntry entry) {
        withDao(RegionDao.class).updateRegion(region);
        auditLog(entry.getSubject() + " updated region " + region.toString(), entry);
    }

    public Region getRegionById(long id) {
        return withDao(RegionDao.class).getRegionById(id);
    }

    public List<Region> getRegions(boolean activeOnly) {
        if (activeOnly) {
            return withDao(RegionDao.class).getActiveRegions();
        } else {
            return withDao(RegionDao.class).getRegions();
        }
    }


    public void addDistrict(District district, AuditService.LogEntry entry) {
        district.setId(withDao(DistrictDao.class).addDistrict(district));
        auditLog(entry.getSubject() + " added district " + district.toString(), entry);
    }

    public void updateDistrict(District district, AuditService.LogEntry entry) {
        withDao(DistrictDao.class).updateDistrict(district);
        auditLog(entry.getSubject() + " updated district " + district.toString(), entry);
    }

    public District getDistrictById(long id) {
        return withDao(DistrictDao.class).getDistrictById(id);
    }

    public List<District> getDistricts(boolean activeOnly) {
        if (activeOnly) {
            return withDao(DistrictDao.class).getActiveDistricts();
        } else {
            return withDao(DistrictDao.class).getDistricts();
        }
    }


    public void addDepartment(Department department, AuditService.LogEntry entry) {
        department.setId(withDao(DepartmentDao.class).addDepartment(department));
        auditLog(entry.getSubject() + " added department " + department.toString(), entry);
    }

    public void updateDepartment(Department department, AuditService.LogEntry entry) {
        withDao(DepartmentDao.class).updateDepartment(department);
        auditLog(entry.getSubject() + " updated department " + department.toString(), entry);
    }

    public Department getDepartmentById(long id) {
        return withDao(DepartmentDao.class).getDepartmentById(id);
    }

    public List<Department> getDepartments(boolean activeOnly) {
        if (activeOnly) {
            return withDao(DepartmentDao.class).getActiveDepartments();
        } else {
            return withDao(DepartmentDao.class).getDepartments();
        }
    }


    public void addWorkStation(WorkStation workStation, AuditService.LogEntry entry) {
        workStation.setId(withDao(WorkstationDao.class).addWorkstation(workStation));
        auditLog(entry.getSubject() + " added workstation " + workStation.toString(), entry);
    }

    public void updateWorkStation(WorkStation workStation, AuditService.LogEntry entry) {
        withDao(WorkstationDao.class).updateWorkstation(workStation);
        auditLog(entry.getSubject() + " updated workstation " + workStation.toString(), entry);
    }

    public WorkStation getWorkStationById(long id) {
        return withDao(WorkstationDao.class).getWorkstationById(id);
    }

    public List<WorkStation> getWorkStations(boolean activeOnly) {
        if (activeOnly) {
            return withDao(WorkstationDao.class).getActiveWorkstations();
        } else {
            return withDao(WorkstationDao.class).getWorkstations();
        }
    }


    private boolean isLocationCodeInUse(String table, String code) {
        String sql;
        sql = "select exists(select 1 from %s where lower(code) = lower(:code) limit 1) as inUse";
        sql = String.format(Locale.US, sql, table);
        return (Boolean) openHandle().open().createQuery(sql).bind("code", code).first().get("inUse");
    }

    public boolean isRegionCodeInUse(String code) {
        return isLocationCodeInUse("region", code);
    }

    public boolean isDistrictCodeInUse(String code) {
        return isLocationCodeInUse("district", code);
    }

    public boolean isWorkstationCodeInUse(String code) {
        return isLocationCodeInUse("workstation", code);
    }

    public boolean isDepartmentCodeInUse(String code) {
        return isLocationCodeInUse("department", code);
    }
}
