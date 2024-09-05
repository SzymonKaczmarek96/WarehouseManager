ALTER TABLE product
DROP COLUMN IF EXISTS status;

CREATE TABLE if NOT EXISTS product_stored (
    id SERIAL PRIMARY KEY,
    lot_number VARCHAR(64) NOT NULL,
    delivery_number VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    quantity BIGINT NOT NULL
    );

CREATE TABLE if NOT EXISTS stock (
    id SERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);