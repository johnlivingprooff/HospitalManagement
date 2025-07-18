package app.models.auth;

import app.core.validation.FilterNode;
import app.core.validation.Validatable;
import app.core.validation.ValidationChain;
import app.core.validation.ValidationStage;
import app.core.validation.validators.*;
import app.models.Subject;

public class PasswordReset implements Validatable {

    @ValidationChain(
            label = "Login ID",
            stage = ValidationStage.Create,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Text.class),
                    @FilterNode(value = Length.class, parameters = {"3", "50"})
            }
    )
    private String loginId;

    @ValidationChain(
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Text.class),
                    @FilterNode(value = Length.class, parameters = {"100", "300"})
            }
    )
    private String token;

    @ValidationChain(
            label = "Password",
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(Text.class),
                    @FilterNode(value = Length.class, parameters = {"8", "15"})
            }
    )
    private String password;

    @ValidationChain(
            fieldName = "subject",
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(LongValidator.class),
                    @FilterNode(PositiveNumber.class)
            }
    )
    private long subjectId;

    @ValidationChain(
            label = "Subject",
            fieldName = "type",
            stage = ValidationStage.Update,
            filters = {
                    @FilterNode(Required.class),
                    @FilterNode(value = SubjectTypeValidator.class, parameters = {SubjectTypeValidator.FLAG_USE_NAME})
            }
    )
    private Subject.SubjectType subjectType;
}
