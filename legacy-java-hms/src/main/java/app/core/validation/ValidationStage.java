package app.core.validation;

/**
 * <p>
 * Validation stage provides a way to perform contextual (selective) validation of post data fields, by using
 * the same validation schema. This is more efficient than the previous
 * mechanism where each stage had to have its own schema file.
 * </p>
 */
public enum ValidationStage {
    All,
    Create,
    Update
}
