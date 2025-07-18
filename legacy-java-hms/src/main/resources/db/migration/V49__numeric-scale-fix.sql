alter table bills
	alter column balance set data type numeric(20,2),
	alter column paid set data type numeric(20,2);

alter table admission_rates
	alter column rate set data type numeric(20,2);

alter table dispensed_medicine
	alter column price set data type numeric(20,2),
	alter column total set data type numeric(20,2);