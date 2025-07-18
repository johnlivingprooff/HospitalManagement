DROP FUNCTION getMedicineForPrescription;

CREATE FUNCTION getMedicineForPrescription(IN "prescriptionId" bigint)
    RETURNS TABLE(id bigint, quantity bigint, name text, expiring boolean, runningLow boolean)
    LANGUAGE 'sql'
AS $BODY$
    select id, quantity, name || ' (' || generic_name || ')' as name, expiring, runningLow
    from medicines
    where id not in (
        select medicine_id
        from prescription_drugs
        where prescription_id = $1
    );
$BODY$;