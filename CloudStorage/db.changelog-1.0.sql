  -- changeSet pavel:1
CREATE TABLE "user_data" (
email varchar(255) PRIMARY KEY ,
password varchar(255) );

CREATE TABLE user_file (
id BIGSERIAL PRIMARY KEY,
user_email varchar(255) ,
file_name varchar(255) ,
file_path varchar(255) NOT NULL,
file_size BIGINT NOT NULL,
FOREIGN KEY (user_email) REFERENCES "user" (email));


CREATE TABLE token_blocked (
id varchar(255) PRIMARY KEY ,
token varchar(255) NOT NULL);


INSERT INTO "user" ("email", "password")
values ('pavel', '123'),
        ('katia', '234');