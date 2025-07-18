package app.models.location;

import app.core.validation.FilterNode;
import app.core.validation.ValidationChain;
import app.core.validation.validators.LongValidator;
import app.core.validation.validators.PositiveNumber;
import app.core.validation.validators.Required;

public class Department extends Location {
    @ValidationChain(label = "Workstation",
            fieldName = "workstationId",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class),
                    @FilterNode(PositiveNumber.class)
            })
    private long workStationId;
    private String workStation;

    public long getWorkStationId() {
        return workStationId;
    }

    public void setWorkStationId(long workStationId) {
        this.workStationId = workStationId;
    }

    public String getWorkStation() {
        return workStation;
    }

    public void setWorkStation(String workStation) {
        this.workStation = workStation;
    }
}
