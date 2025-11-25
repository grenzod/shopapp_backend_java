CREATE DATABASE IF NOT EXISTS shopapp;
USE shopapp;

CREATE TABLE roles (
    id INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);
INSERT INTO roles (id, name) VALUES
(1, 'USER'),
(2, 'ADMIN');

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(10) NOT NULL,
    address VARCHAR(200) DEFAULT '',
    password VARCHAR(100) NOT NULL DEFAULT '',
    created_at DATETIME,
    updated_at DATETIME,
    is_active TINYINT(1) DEFAULT 1,
    date_of_birth DATE,
    facebook_account_id VARCHAR(50) DEFAULT '',
    google_account_id VARCHAR(50) DEFAULT '',
    email VARCHAR(100) DEFAULT '',
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_date DATETIME,
    is_mobile tinyint(1) DEFAULT 0,
    revoked TINYINT(1) NOT NULL,
    expired TINYINT(1) NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE social_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(20) NOT NULL COMMENT 'Tên nhà cung cấp social network',
    provider_id VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL COMMENT 'Email tài khoản',
    name VARCHAR(100) NOT NULL COMMENT 'Tên người dùng',
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Tên danh mục, vd: đồ điện tử'
);

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(350) COMMENT 'Tên sản phẩm',
    price FLOAT NOT NULL CHECK (price >= 0),
    thumbnail VARCHAR(300) DEFAULT '',
    description LONGTEXT,
    created_at DATETIME,
    updated_at DATETIME,
    quantity INT,
    color VARCHAR(20) DEFAULT '',
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_images (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    image_url VARCHAR(300),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(100) DEFAULT '',
    email VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'processing', 'complete', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending' COMMENT 'Trạng thái đơn hàng',
    total_money FLOAT CHECK(total_money >= 0),
    active TINYINT(1) DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    price FLOAT CHECK(price >= 0),
    number_of_products INT CHECK(number_of_products > 0),
    total_money FLOAT CHECK(total_money >= 0),
    color VARCHAR(20) DEFAULT '',
    status VARCHAR(50) DEFAULT '',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Bảng quản lý phương thức thanh toán
CREATE TABLE payment_methods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT 'Tên phương thức thanh toán',
    code VARCHAR(20) NOT NULL COMMENT 'Mã phương thức: cod, bank_transfer, momo, vnpay',
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO payment_methods (name, code) VALUES
('Thanh toán khi nhận hàng', 'cod'),
('VNPay', 'vnpay');

-- Bảng quản lý thanh toán
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method_id INT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
    status ENUM('pending', 'processing', 'completed', 'failed', 'cancelled', 'refunded') DEFAULT 'pending',
    payment_code VARCHAR(100) COMMENT 'Mã giao dịch từ cổng thanh toán',
    payment_date DATETIME NULL,
    shipping_address VARCHAR(200),
    expected_payment_date DATE COMMENT 'Ngày dự kiến thanh toán với COD',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    notes TEXT COMMENT 'Ghi chú thanh toán',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

-- Bảng lịch sử thanh toán
CREATE TABLE payment_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payment_id INT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);
