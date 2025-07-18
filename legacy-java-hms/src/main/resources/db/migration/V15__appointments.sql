CREATE TABLE IF NOT EXISTS appointments (
	id bigserial PRIMARY KEY,
	patient_id bigint NOT NULL REFERENCES patient (id),
	doctor_id bigint NOT NULL REFERENCES doctors (id),
	created timestamp WITH time ZONE NOT NULL,
	schedule timestamp WITH time ZONE NOT NULL,
	createdBy bigint NOT NULL REFERENCES account (id),
	details TEXT,
	expired boolean NOT NULL,
	cancelled boolean NOT NULL
);