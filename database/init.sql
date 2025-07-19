-- Initialize the HMS database
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create admin user if not exists (compatible with existing system)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        -- This will be handled by SQLAlchemy migrations
        -- Just ensure the database is ready
        SELECT 1;
    END IF;
END $$;
