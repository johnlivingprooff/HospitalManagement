create table medicine_alert_log (
    last_stock_level_alert_at timestamptz not null,
    last_expiration_alert_at timestamptz not null
);

insert into medicine_alert_log (last_stock_level_alert_at, last_expiration_alert_at)
    values ('1970-01-01 00:00', '1970-01-01 00:00');