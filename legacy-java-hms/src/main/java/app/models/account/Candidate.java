package app.models.account;

import app.core.annotations.HtmlFieldDisplay;
import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.validators.LongValidator;
import app.core.validation.validators.Required;

@HtmlFieldDisplay(label = "getName()", value = "getAccountId()")
public class Candidate implements Validatable {

    @ValidationChain(
            label = "Account",
            fieldName = "",
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class)
            }
    )
    private long accountId;
    private String name;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName() + " (" + getAccountId() + ")";
    }
}
