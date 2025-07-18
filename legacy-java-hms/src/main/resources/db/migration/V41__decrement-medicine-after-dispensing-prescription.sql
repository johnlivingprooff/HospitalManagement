create function updateMedicineStockQuantity(in prescriptionId bigint)
returns bigint
language plpgsql
AS $$
BEGIN
    update medicine
    set quantity = greatest(0, medicine.quantity - t.actualQuantity)
    from (select medicine_id, actualQuantity from prescription_drugs_v where prescription_id = $1) as t
    where medicine.id = t.medicine_id;
    RETURN 1;
END;
$$;