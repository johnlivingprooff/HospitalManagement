alter table medicine_expiration
    add column notifyExpiration boolean not null default false,
    add column notifyStockLevel boolean not null default false,
    add column notificationEmail text not null default '';