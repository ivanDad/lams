SET FOREIGN_KEY_CHECKS=0;
DROP DATABASE IF EXISTS @dbname@;
CREATE DATABASE @dbname@ DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS=1;

USE @dbname@;

SET FOREIGN_KEY_CHECKS=1;