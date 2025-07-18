package app.models.location;

import app.core.validation.FilterNode;
import app.core.validation.ValidationChain;
import app.core.validation.validators.LongValidator;
import app.core.validation.validators.PositiveNumber;
import app.core.validation.validators.Required;

public class District extends Location {

    @ValidationChain(
            label = "Region",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class),
                    @FilterNode(PositiveNumber.class)
            }
    )
    private long regionId;

    private String region;

    public long getRegionId() {
        return regionId;
    }

    public void setRegionId(long regionId) {
        this.regionId = regionId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
