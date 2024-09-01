CREATE TABLE if NOT EXISTS employee (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(512) NOT NULL,
    email VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL,
    role JSONB,
    access_token VARCHAR(512),
    refresh_token VARCHAR(512),
    access_token_expiration_date TIMESTAMP,
    refresh_token_expiration_date TIMESTAMP
);

CREATE TABLE if NOT EXISTS warehouse (
    id SERIAL PRIMARY KEY,
    warehouse_name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    occupied_area INT NOT NULL,
    products JSONB
);

CREATE TABLE if NOT EXISTS product (
    id SERIAL PRIMARY KEY,
    product_name VARCHAR(50) NOT NULL,
    product_size INT NOT NULL,
    status VARCHAR(50) NOT NULL
);