-- 1. Create the application database if it doesn't exist
CREATE DATABASE IF NOT EXISTS auth_bookstore;

-- 2. FIX: Create the user account forcing the native connection plugin
CREATE USER 'auth_app_user'@'%' IDENTIFIED WITH mysql_native_password BY 'authappsecurepass123';

-- 3. Grant the required permissions for Flyway DDL executions and runtime actions
GRANT ALL PRIVILEGES ON `auth_bookstore`.* TO 'auth_app_user'@'%';

-- 4. Provision bookstore user and permissions for the main bookstore database (if needed)
-- CREATE DATABASE IF NOT EXISTS bookstore;
-- CREATE USER 'bookuser'@'%' IDENTIFIED WITH mysql_native_password BY 'bookpass123';
-- GRANT ALL PRIVILEGES ON `bookstore`.* TO 'bookuser'@'%';

-- 4. Flush to apply everything instantly and clear the authorization cache
FLUSH PRIVILEGES;
