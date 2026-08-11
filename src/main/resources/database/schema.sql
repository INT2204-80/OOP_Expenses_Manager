CREATE TABLE IF NOT EXISTS wallets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    balance DOUBLE NOT NULL,
    wallet_type ENUM('CASH', 'BANK', 'EWALLET') NOT NULL,
    bank_name VARCHAR(255),
    account_number VARCHAR(255),
    provider VARCHAR(255),
    currency VARCHAR(10) DEFAULT 'VND'
);

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount DOUBLE NOT NULL,
    date DATE NOT NULL,
    note TEXT,
    category_id INT,
    wallet_id INT,
    transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL,
    source VARCHAR(255),
    payment_method VARCHAR(255),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);
