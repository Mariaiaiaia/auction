CREATE SCHEMA IF NOT EXISTS test;

CREATE TABLE IF NOT EXISTS usr (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS item (
    id SERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    image VARCHAR(255),
    is_sold BOOLEAN NOT NULL,
    auction_id BIGINT,
    seller BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS auction (
    id SERIAL PRIMARY KEY,
    item BIGINT NOT NULL,
    seller BIGINT NOT NULL,
    starting_price DECIMAL(19,2) NOT NULL,
    current_price DECIMAL(19,2),
    bidder BIGINT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    finished BOOLEAN NOT NULL,
    public_access BOOLEAN
);

CREATE TABLE IF NOT EXISTS bid (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    auction_id BIGINT NOT NULL,
    bid_amount DECIMAL(19, 2) NOT NULL
);

ALTER SEQUENCE usr_id_seq RESTART WITH 1;
ALTER SEQUENCE item_id_seq RESTART WITH 1;
ALTER SEQUENCE auction_id_seq RESTART WITH 1;
ALTER SEQUENCE bid_id_seq RESTART WITH 1;
SET timezone = 'Asia/Jerusalem';

INSERT INTO usr (first_name, last_name, password, email, role)
VALUES
    ('Harry', 'Potter', '$2a$10$S.6HnNc5DdqfzTom9vFEQOR8J9ek2SGEFnvxKxs/StaGfzBw9LW7G', 'harryp@gmail.com', 'user'),
    ('Ron', 'Weasley', '$2a$10$Eroa0QP2ZrfsIpBQj.wm3eIn9hvISJqRgz9NBLEGVEAYjjl93AW8i', 'ronw@gmail.com', 'user')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO item (item_name, description, image, is_sold, auction_id, seller)
VALUES
    ('Laptop', 'Gaming laptop with RTX 3070', 'image1.jpg', FALSE, NULL, 1),
    ('Phone', 'New smartphone with great camera', 'image2.jpg', FALSE, 1, 1),
    ('Bike', 'Used mountain bike', 'image3.jpg', TRUE, 3, 1);

INSERT INTO auction (item, seller, starting_price, current_price, bidder, start_date, end_date, finished, public_access)
VALUES
(101, 1, 100.00, 120.00, 2, '2025-04-15 10:00:00', NOW() + INTERVAL '50 minutes', false, false),
(102, 2, 150.00, NULL, NULL, '2025-04-14 09:00:00', NOW() + INTERVAL '55 minutes', false, true),
(103, 1, 200.00, NULL, NULL, NOW() + INTERVAL '30 minutes', NOW() + INTERVAL '55 minutes', false, false),
(104, 1, 300.00, 350.00, 2, '2025-04-12 15:00:00', '2025-07-18 15:00:00', false, true),
(105, 2, 80.00, 90.00, 6, '2025-04-15 12:00:00', '2025-07-19 18:00:00', false, false),
(106, 1, 60.00, NULL, NULL, '2025-04-16 09:00:00', NOW() + INTERVAL '3 minutes', false, false),
(107, 1, 500.00, 600.00, 3, '2025-04-10 10:00:00', '2025-07-11 10:00:00', true, false),
(108, 2, 500.00, 600.00, 1, '2025-04-10 10:00:00', '2025-07-11 10:00:00', true, true);

INSERT INTO bid (user_id, auction_id, bid_amount) VALUES
(1, 8, 550.00),
(2, 4, 350.00),
(5, 4, 450.00),
(6, 4, 650.00),
(2, 8, 650.00),
(4, 8, 750.00),
(1, 8, 600.00),
(1, 6, 600.00),
(4, 5, 600.00);

