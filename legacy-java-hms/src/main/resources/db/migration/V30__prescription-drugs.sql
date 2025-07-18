CREATE OR REPLACE FUNCTION public.getMedicineForPrescription(IN "prescriptionId" bigint)
    RETURNS TABLE(id bigint, quantity bigint, name text)
    LANGUAGE 'sql'
AS $BODY$
    select id, quantity, name || ' (' || generic_name || ')' as name
    from medicines
    where id not in (
        select medicine_id
        from prescription_drugs
        where prescription_id = $1
    );
$BODY$;