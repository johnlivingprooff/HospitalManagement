delete from role_permission
where roleKey = 'Administrator'
and permissionKey in (
    'AccessDentistry',
    'AccessDentalProcedures',
    'WriteDentalProcedures',
    'ReadDentalSurgeries',
    'WriteDentalSurgeries'
);