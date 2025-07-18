alter table medicine
    add column name text not null;

CREATE OR REPLACE VIEW medicines
 AS
select m.*, c.name as categoryName
from medicine m
join medicine_category c on c.id = m.category
where m.deleted = false;

CREATE OR REPLACE FUNCTION "addMedicine" (
        IN _category bigint,
        IN _location text,
        IN _purchase_price numeric,
        IN _selling_price numeric,
        IN _quantity numeric,
        IN _generic_name text,
        IN _updated timestamp with time zone,
        IN _expires date,
        IN _deleted boolean,
        IN _name text
    ) RETURNS bigint LANGUAGE 'plpgsql'
AS $BODY$
declare
medicine_id bigint;
begin

insert into medicine ("name", category, "location", purchase_price, selling_price,
    quantity, generic_name, updated, expires, deleted)
values (_name, _category, _location, _purchase_price, _selling_price,
    _quantity, _generic_name, _updated, _expires, _deleted)
returning id into medicine_id;

return medicine_id;
end;
$BODY$;


CREATE OR REPLACE FUNCTION "updateMedicine" (
        IN _id bigint,
        IN _category bigint,
        IN _location text,
        IN _purchase_price numeric,
        IN _selling_price numeric,
        IN _quantity numeric,
        IN _generic_name text,
        IN _updated timestamp with time zone,
        IN _expires date,
        IN _deleted boolean,
        IN _name text
    ) RETURNS void LANGUAGE 'plpgsql'
AS $BODY$
begin
update medicine
set "name" = _name,
    updated = _updated,
    expires = _expires,
    deleted = _deleted,
    category = _category,
    quantity = _quantity,
    "location" = _location,
    generic_name = _generic_name,
    selling_price = _selling_price,
    purchase_price = _purchase_price
where
    id = _id;
end;
$BODY$;