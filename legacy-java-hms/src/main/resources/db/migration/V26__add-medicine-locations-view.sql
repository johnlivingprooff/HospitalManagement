
CREATE VIEW medicine_locations
AS
    SELECT *
    FROM medicine_location
    WHERE deleted = false;