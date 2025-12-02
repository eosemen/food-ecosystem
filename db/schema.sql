CREATE DATABASE IF NOT EXISTS food_delivery_db;
USE food_delivery_db;

-- Enterprises (e.g., "Boston Food Delivery", "New York Eats")
CREATE TABLE IF NOT EXISTS enterprises (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL -- 'Restaurant', 'Delivery'
);

-- Organizations within Enterprises (e.g., "Admin", "Kitchen", "DeliveryTeam")
CREATE TABLE IF NOT EXISTS organizations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    enterprise_id INT,
    FOREIGN KEY (enterprise_id) REFERENCES enterprises(id) ON DELETE CASCADE
);

-- Users (System Admins, Enterprise Admins, Managers, Customers, Delivery Men)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL, -- In a real app, hash this!
    role VARCHAR(50) NOT NULL, -- 'SystemAdmin', 'EnterpriseAdmin', 'Manager', 'Customer', 'DeliveryMan'
    name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    organization_id INT,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE SET NULL
);

-- Menu Items for Restaurants (Assuming each Restaurant is an Organization or managed by a Manager User)
-- For simplicity, let's link menu items to a 'Restaurant' Organization.
CREATE TABLE IF NOT EXISTS menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL, -- Links to organizations(id) where type='Restaurant'
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    FOREIGN KEY (restaurant_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Orders (Work Requests)
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL, -- Links to users(id)
    restaurant_id INT NOT NULL, -- Links to organizations(id)
    delivery_man_id INT, -- Links to users(id)
    status VARCHAR(50) NOT NULL, -- 'Placed', 'Accepted', 'Cooking', 'ReadyForPickup', 'OutForDelivery', 'Delivered'
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    delivery_address TEXT,
    comment TEXT,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (restaurant_id) REFERENCES organizations(id),
    FOREIGN KEY (delivery_man_id) REFERENCES users(id)
);

-- Order Items
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Initial Seed Data
INSERT INTO users (username, password, role, name) VALUES ('sysadmin', 'sysadmin', 'SystemAdmin', 'System Administrator');

-- Seed: Enterprises
INSERT INTO enterprises (name, type) VALUES ('Boston Food Delivery', 'Restaurant');
INSERT INTO enterprises (name, type) VALUES ('New York Eats', 'Restaurant');

-- Seed: Organizations for each enterprise
INSERT INTO organizations (name, type, enterprise_id) VALUES ('Kitchen Staff', 'Kitchen', 1);
INSERT INTO organizations (name, type, enterprise_id) VALUES ('Admin Team', 'Admin', 1);
INSERT INTO organizations (name, type, enterprise_id) VALUES ('Delivery Fleet', 'DeliveryTeam', 2);

-- Seed: Users (roles: Manager, Customer, DeliveryMan)
INSERT INTO users (username, password, role, name, phone, email, organization_id) VALUES ('manager1', 'manager1', 'Manager', 'Jane Smith', '555-1111', 'jane@example.com', 2);
INSERT INTO users (username, password, role, name, phone, email, organization_id) VALUES ('customer1', 'customer1', 'Customer', 'John Doe', '555-2222', 'john@example.com', NULL);
INSERT INTO users (username, password, role, name, phone, email, organization_id) VALUES ('delivery1', 'delivery1', 'DeliveryMan', 'Bob Driver', '555-3333', 'bob@example.com', 3);
-- Seed an Enterprise Admin for enterprise 1
INSERT INTO users (username, password, role, name, phone, email, organization_id) VALUES ('entadmin1', 'entadmin1', 'EnterpriseAdmin', 'Boston Admin', '555-5555', 'admin@bostonfd.com', 2);

-- Seed: Menu Items for restaurant of enterprise 1 (assuming organization is used for restaurant linkage)
INSERT INTO menu_items (restaurant_id, name, price, description) VALUES (1, 'Burger', 9.99, 'Beef burger with fries');
INSERT INTO menu_items (restaurant_id, name, price, description) VALUES (1, 'Pizza', 12.99, 'Cheese pizza (Large)');
INSERT INTO menu_items (restaurant_id, name, price, description) VALUES (2, 'Sushi Platter', 24.99, 'Fresh assorted sushi');

-- Seed orders and order_items to demonstrate CRUD (work requests)
INSERT INTO orders (customer_id, restaurant_id, status, delivery_address, comment) VALUES (2, 1, 'Placed', '123 Main St', 'No onions');
INSERT INTO orders (customer_id, restaurant_id, status, delivery_address, comment) VALUES (2, 2, 'Placed', '123 Main St', 'Extra napkins');
INSERT INTO orders (customer_id, restaurant_id, status, delivery_address, comment) VALUES (1, 1, 'Delivered', '456 Elm St', 'Thanks!');

INSERT INTO order_items (order_id, menu_item_name, price, quantity) VALUES (1, 'Burger', 9.99, 1);
INSERT INTO order_items (order_id, menu_item_name, price, quantity) VALUES (2, 'Sushi Platter', 24.99, 1);
INSERT INTO order_items (order_id, menu_item_name, price, quantity) VALUES (3, 'Burger', 9.99, 2);

-- Work Requests table (formal inter-enterprise requests)
CREATE TABLE IF NOT EXISTS work_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50), -- 'OrderRequest', 'SupplyRequest', 'DeliveryAssignment'
    sender_enterprise_id INT,
    receiver_enterprise_id INT,
    related_order_id INT,
    status VARCHAR(50),
    message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_enterprise_id) REFERENCES enterprises(id) ON DELETE SET NULL,
    FOREIGN KEY (receiver_enterprise_id) REFERENCES enterprises(id) ON DELETE SET NULL
);

-- Add a dedicated Delivery enterprise and seed it
INSERT INTO enterprises (name, type) VALUES ('Quick Delivery Service', 'Delivery');
INSERT INTO organizations (name, type, enterprise_id) VALUES ('Dispatch Team', 'DeliveryTeam', 3);
INSERT INTO users (username, password, role, name, phone, email, organization_id) VALUES ('delivery2', 'delivery2', 'DeliveryMan', 'Alice Courier', '555-4444', 'alice@example.com', 4);

-- Sample work requests
INSERT INTO work_requests (type, sender_enterprise_id, receiver_enterprise_id, related_order_id, status, message) VALUES ('DeliveryAssignment', 1, 3, 1, 'New', 'Please handle delivery.');

-- Add indexes for quick lookup by receiver and sender
CREATE INDEX idx_work_requests_receiver ON work_requests (receiver_enterprise_id);
CREATE INDEX idx_work_requests_sender ON work_requests (sender_enterprise_id);
