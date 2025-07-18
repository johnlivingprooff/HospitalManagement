package app.models.location;

import app.core.validation.FilterNode;
import app.core.validation.ValidationChain;
import app.core.validation.validators.*;

public class WorkStation extends Location {

    @ValidationChain(label = "District", filters = {
            @FilterNode(Required.class),
            @FilterNode(LongValidator.class),
            @FilterNode(PositiveNumber.class)
    })
    private long districtId;

    @ValidationChain(label = "Address", filters = {
            @FilterNode(Required.class),
            @FilterNode(Text.class),
            @FilterNode(value = Length.class, parameters = {"3", "100"})
    })
    private String address;

    private String district;

    public long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(long districtId) {
        this.districtId = districtId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
