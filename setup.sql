CREATE DATABASE stock_ims CHARACTER SET utf8mb4;
CREATE USER 'stock_app'@'localhost' IDENTIFIED BY 'VOTRE_MOT_DE_PASSE';
GRANT ALL PRIVILEGES ON stock_ims.* TO 'stock_app'@'localhost';
