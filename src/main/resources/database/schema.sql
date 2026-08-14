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
    transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL,
    icon VARCHAR(255),
    color VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
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
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_period VARCHAR(50),
    passed_periods INT DEFAULT 0,
    recurring_end_date DATE,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE TABLE IF NOT EXISTS budgets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    limit_amount DOUBLE NOT NULL,
    current_spent DOUBLE DEFAULT 0.0,
    category_id INT,
    period VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    wallet_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);
