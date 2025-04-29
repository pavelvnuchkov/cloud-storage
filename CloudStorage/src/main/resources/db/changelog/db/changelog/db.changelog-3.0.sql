  -- changeSet pavel:1
CREATE TABLE user_file (
id BIGSERIAL PRIMARY KEY,
user_email varchar(255) ,
file_name varchar(255) ,
file_path varchar(255) NOT NULL,
file_size BIGINT NOT NULL,
FOREIGN KEY (user_email) REFERENCES "user_data" (email));