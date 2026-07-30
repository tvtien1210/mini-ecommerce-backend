-- # TẠO DATABASE SHOP

-- tạo database cho hệ thống bán hàng
CREATE DATABASE IF NOT EXISTS shop_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- sử dụng database vừa tạo
USE shop_db;



-- # BẢNG ROLE

-- role dùng để phân quyền trong hệ thống
-- ví dụ: CUSTOMER / STAFF / ADMIN

CREATE TABLE roles(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,      -- id của role
    name VARCHAR(50) NOT NULL UNIQUE,       -- tên role (không trùng)
    description VARCHAR(255) NOT NULL               -- mô tả role
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;


-- thêm dữ liệu role mặc định
INSERT INTO roles (name, description) VALUES
('ROLE_CUSTOMER','Khách hàng'),
('ROLE_STAFF','Nhân viên bán hàng'),
('ROLE_ADMIN','Quản trị hệ thống');


-- # BẢNG USER

-- bảng lưu tài khoản đăng nhập

CREATE TABLE users(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,      -- id user
    username VARCHAR(50) NOT NULL UNIQUE,   -- tên đăng nhập
    password VARCHAR(255) NOT NULL,         -- mật khẩu (bcrypt)
    email VARCHAR(100) UNIQUE,              -- email
    full_name VARCHAR(100),                 -- họ tên
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP  -- ngày tạo
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;



-- thêm dữ liệu user demo
-- password đã được mã hoá bằng bcrypt

INSERT INTO users (username,password,email,full_name) VALUES
('thaonhi','$2a$10$eO/Fmfv6hDA2MfWeFi2tKeLChM7HQzOewkL0Jh/.f5o32tj3BwNXG','thaonhi@gmail.com','Trần Thảo Nhi'),
('lananh','$2a$10$eO/Fmfv6hDA2MfWeFi2tKeLChM7HQzOewkL0Jh/.f5o32tj3BwNXG','lananh@gmail.com','Nguyễn Lan Anh'),
('chantaro','$2a$10$eO/Fmfv6hDA2MfWeFi2tKeLChM7HQzOewkL0Jh/.f5o32tj3BwNXG','chantaro@gmail.com','Chan Taro');



-- # BẢNG USERS_ROLES

-- bảng trung gian cho quan hệ MANY TO MANY
-- user (n) ------ (n) role

CREATE TABLE users_roles(

    user_id BIGINT,   -- id user
    role_id BIGINT,   -- id role

    -- khóa chính gồm 2 cột
    PRIMARY KEY (user_id, role_id),

    -- khóa ngoại liên kết tới bảng users
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,

    -- khóa ngoại liên kết tới bảng role
    FOREIGN KEY (role_id) REFERENCES roles(id)
    ON DELETE CASCADE,

    -- PRIMARY KEY (user_id, role_id) = index cho (user_id, role_id)
    -- Thêm index cho role_id cho tối ưu
    INDEX idx_role_id(role_id)

) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;



-- dữ liệu phân quyền
-- users nào có role nào

INSERT INTO users_roles(user_id,role_id) VALUES
(1,1),        -- thaonhi  -> CUSTOMER
(2,2),        -- lananh   -> STAFF
(3,2),(3,3);  -- chantaro -> ADMIN, STAFF



-- # BẢNG CATEGORY

-- category (1) ------ (n) product
-- mỗi category có nhiều product

CREATE TABLE category(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,   -- id category
    name VARCHAR(100) NOT NULL,          -- tên category
    description VARCHAR(255) NOT NULL,            -- mô tả
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP

) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;



-- dữ liệu category demo
INSERT INTO category(name,description) VALUES
('iPhone','Các loại iPhone'),
('iPad','Các loại iPad'),
('MacBook','Các loại MacBook'),
('Accessories','Các loại phụ kiện');



-- # BẢNG PRODUCT

-- Category (1) ───< (n) Product
-- n product thuộc về 1 category

CREATE TABLE product(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    category_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    price DECIMAL(10,2) NOT NULL CHECK(price > 0),

    stock INT NOT NULL DEFAULT 0,              -- tổng tồn kho

    reserved_stock INT NOT NULL DEFAULT 0,     -- hàng đang giữ

    version BIGINT NOT NULL DEFAULT 0,         -- dùng cho optimistic locking

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    -- khóa ngoại
    FOREIGN KEY (category_id) REFERENCES category(id)
    ON DELETE RESTRICT,

    -- index
    INDEX idx_category_id (category_id)

) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;


-- # BẢNG ORDERS

-- 1 user có nhiều order
-- user (1) ------ (n) orders

CREATE TABLE orders(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,    -- id đơn hàng
    user_id BIGINT NOT NULL,                 -- khách đặt hàng

    -- dùng enum dạng string
    status VARCHAR(50) NOT NULL,

    total_price DECIMAL(12,2) NOT NULL,   -- tổng tiền đơn

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    -- khóa ngoại tới users
    FOREIGN KEY (user_id) REFERENCES users(id)
    -- chặn xoá users nếu vẫn còn orders
    ON DELETE RESTRICT,

    -- thêm index cho foreign key user_id
    INDEX idx_user_id (user_id),

    -- thêm index cho status để query nhanh
    INDEX idx_status (status)

)ENGINE=INNODB DEFAULT CHARSET=utf8mb4;



-- # BẢNG ORDER_ITEM

-- chi tiết sản phẩm trong đơn hàng

-- 1 order ------ n order_item ------ 1 product

CREATE TABLE order_item(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,     -- đơn hàng
    product_id BIGINT NOT NULL,   -- sản phẩm

    quantity INT NOT NULL,     -- số lượng
    price DECIMAL(10,2) NOT NULL,  -- giá tại thời điểm mua

    -- khóa ngoại tới orders
    FOREIGN KEY (order_id) REFERENCES orders(id)
    -- Chặn xoá order nếu đã có order_item
    -- Không cho phép xóa bản ghi ở bảng cha (orders)
    -- Nếu vẫn còn bản ghi liên quan ở bảng con (order_items)
    ON DELETE RESTRICT,
    INDEX idx_order_id(order_id),

    -- khóa ngoại tới product
    FOREIGN KEY (product_id) REFERENCES product(id)
    ON DELETE RESTRICT,
    INDEX idx_product_id(product_id)

)ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- RESTRICT
-- Không cho xóa nếu có liên kết
-- CASCADE
-- Xóa order → tự xóa luôn order_items
-- SET NULL
-- Xóa order → order_id trong order_items = NULL

-- # BẢNG CART

CREATE TABLE cart(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED', 'EXPIRED')),
    total_price DECIMAL(12,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- ON DELETE CASCADE: Khi xoá user (bảng cha) → MySQL tự động xoá cart (bảng con)
    CONSTRAINT fk_cart_user
        -- Mỗi user_id chỉ được xuất hiện 1 lần duy nhất trong bảng cart
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    -- UNIQUE(user_id) -> là index
    -- 1 user chỉ có 1 cart: nghĩa là 1 cart chỉ insert được 1 user_id duy nhất, không trùng nhau
    -- UNIQUE KEY uq_user_id(user_id)

    -- KHÔNG unique user_id nữa → vì:
    -- vừa thêm status
    -- 1 user có nhiều cart (history_status)
    -- chỉ có 1 cart ACTIVE

)ENGINE = INNODB DEFAULT CHARSET=utf8mb4;

--  CONSTRAINT fk_cart_user : ràng buộc foreign key của cart và user
--  ON UPDATE CURRENT_TIMESTAMP: nghĩa là
--	Khi INSERT → updated_at = thời điểm tạo
--	Khi UPDATE row → updated_at tự động = thời điểm sửa

-- # BẢNG CART_ITEM

CREATE TABLE cart_item(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price DECIMAL(12,2) NOT NULL,

    INDEX idx_cart_id(cart_id),
    INDEX idx_product_id(product_id),

    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,

    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT,

    -- Không cho phép trùng cặp (cart_id, product_id) trong bảng cart_item
    -- cùng 1 sản phẩm bị lặp 2 dòng → dữ liệu bẩn (sẽ có method update ở Java sau để cộng dồn quantity nếu add thêm cùng 1 product vào cart)
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id)

    ) ENGINE = INNODB DEFAULT CHARSET = utf8mb4;

-- Thêm cột version vào bảng product
-- ALTER TABLE product ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- # BẢNG PAYMENT

CREATE TABLE payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    order_id BIGINT NOT NULL,

    txn_ref VARCHAR(100) NOT NULL,

    amount DECIMAL NOT NULL,

    status VARCHAR(20) NOT NULL,
    -- PENDING, SUCCESS, FAILED, EXPIRED

    expired_at DATETIME,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_txn_ref UNIQUE (txn_ref),

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
)ENGINE = INNODB DEFAULT CHARSET = utf8mb4;
