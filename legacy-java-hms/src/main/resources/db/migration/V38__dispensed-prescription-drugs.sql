create table if not exists dispensed_medicine (
    id bigserial primary key,
    prescription_id bigint not null references prescriptions (id) match full,
    medicine_id bigint not null references medicine (id) match full,
    -- Because this must reflect the patient's invoice, we will copy over data as it were at that point in time.
    -- We only keep the foreign key link for generating reports
    medicine_name text not null,
    -- prescribed medicine quantity
    prescribed bigint not null,
    -- actual quantity dispensed
    dispensed bigint not null,
    price numeric not null,
    total numeric not null,
    notes text
);

-- This is called when a prescription is dispensed
create function public."createPrescriptionMedicineHistory" (in prescriptionId bigint)
    returns void
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
    prescription_id = $1
;
end;
$$;