drop function public."createPrescriptionMedicineHistory";

-- This is called when a prescription is dispensed
create function public."createPrescriptionMedicineHistory" (in prescriptionId bigint)
    returns bigint
    language plpgsql
as  $$
begin
insert into dispensed_medicine (
    prescription_id,
    medicine_id,
    medicine_name,
    prescribed,
    dispensed,
    price,
    total,
    notes
)
select
    prescription_id,
    medicine_id,
    medicineName,
    quantity,
    actualQuantity,
    selling_price,
    totalCost,
    notes
from
    prescription_drugs_v
where
    prescription_id = $1;
    return 1;
end;
$$;