package app.models.medical;

import app.core.annotations.HtmlFieldDisplay;
import app.models.Contexts;
import lib.gintec_rdl.jbeava.validation.annotations.Filter;

import java.time.LocalDateTime;

@HtmlFieldDisplay(label = "getName()", value = "getId()")
public class Procedure {

    public enum ProcedureType {
        Lab,
        Dental,
        Surgery,
        Consultation
    }

    @Filter(
            filters = {"required", "long"},
            contexts = {Contexts.UPDATE}
    )
    private long id;

    @Filter(
            label = "Procedure name",
            filters = {"trim", "required", "length(1,255)"}
    )
    private String name;

    @Filter(
            label = "Cost of procedure",
            filters = {"required", "double", "range(0,999999999)"}
    )
    private double cost;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProcedureType procedureType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ProcedureType getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(ProcedureType procedureType) {
        this.procedureType = procedureType;
    }

    @Override
    public String toString() {
        return "{id=" + id + ", type=" + procedureType + "}";
    }
}
