CREATE TABLE permissions (
  id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  app_key VARCHAR(100) NOT NULL,
  role VARCHAR(50) NOT NULL,
  permission VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME
);
