-- SQL script to create a trigger for automatic user registration messages

CREATE OR REPLACE FUNCTION log_new_user_registration()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO messages (content, sent_at, user_id)
    VALUES (NEW.full_name || ' registered', CURRENT_TIMESTAMP, NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if it exists to avoid errors
DROP TRIGGER IF EXISTS tr_after_user_insert ON users;

CREATE TRIGGER tr_after_user_insert
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION log_new_user_registration();
