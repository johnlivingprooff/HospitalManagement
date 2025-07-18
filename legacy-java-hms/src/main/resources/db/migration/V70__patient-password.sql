-- LOL, forgot password column
alter table patient
    add column "password" text not null default '';