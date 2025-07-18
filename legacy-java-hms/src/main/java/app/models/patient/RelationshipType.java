package app.models.patient;

import app.core.annotations.HtmlFieldDisplay;

@HtmlFieldDisplay(label = "name()", value = "name()")
public enum RelationshipType {
    Child,
    Parent,
    Friend,
    Sibling,
    Uncle,
    Aunt,
    Cousin,
    Spouse,
    Guardian,
    Acquaintance,
    Other;

    public static final RelationshipType[] VALUES = RelationshipType.values();
}
