DROP DATABASE IF EXISTS smarttrip_db;
CREATE DATABASE smarttrip_db;
USE smarttrip_db;
SET FOREIGN_KEY_CHECKS = 0;
\. smarttrip_db_real.sql
\. smarttrip_db_merge.sql
\. merge_db.sql
SET FOREIGN_KEY_CHECKS = 1;
