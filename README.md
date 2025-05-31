# 🛒 Inventory Management System

A scalable Inventory & Point of Sale (POS) system built using **Spring Boot Microservices Architecture** with **JWT Authentication** and **PostgreSQL**.

---

## ✨ Features

- 🔐 JWT-based authentication & authorization
- 📦 Product management with categories, variants, barcodes, batch & serial tracking
- 📊 Inventory tracking, aging, and alerts
- 🧾 Purchase & supplier management
- 🛍️ Sales & in-store Point of Sale interface
- 🧑‍🤝‍🧑 Customer profiles, loyalty, and purchase history
- 📈 Comprehensive reporting and analytics
- 🌐 Multi-branch/location inventory and operations support
- ⚙️ Settings module for tax, locale, permissions, and more

---

## 🧱 Tech Stack

| Layer          | Technology                    |
|----------------|-------------------------------|
| Backend        | Spring Boot, Spring Security  |
| Database       | PostgreSQL                    |
| Authentication | JWT, Spring Security          |
| Communication  | REST APIs                     |
| Architecture   | Microservices (via REST, OpenFeign) |
| Tools          | Lombok, MapStruct (optional)|

---

## 📦 Planned Functional Modules

### ✅ Core Modules
- **Authentication & Authorization**: JWT with role/permission-based access
- **Product Management**: Categories, variants, barcode/QR code generation, batch & serial tracking
- **Inventory Control**: Stock level tracking, low stock alerts, inventory aging

### 🧾 Purchasing & Supplier Management
- Supplier database
- Purchase orders
- Goods receiving
- Cost management

### 🛍️ Sales & Point of Sale (POS)
- In-store POS interface
- Invoice generation
- Receipt printing
- Support for multiple payment methods

### 🧑‍🤝‍🧑 Customer Management
- Customer profiles
- Loyalty points and personalized discounts
- Purchase history

### 📊 Reports & Analytics
- Sales reports (daily, monthly, custom)
- Top-selling products
- Inventory aging reports
- Profit/Loss analysis

### 🏬 Multi-Branch / Location Support
- Branch-wise inventory & operations
- Stock transfers between branches
- Per-branch configuration

### ⚙️ Configuration & Settings
- Tax rules
- Currency and locale settings
- Permissions and role management

---

## 📂 Microservices Overview

- **Auth Service**: Manages user registration, login, JWT generation & validation
- **Product Service**: Manages products, categories, variants, and barcodes
- **Inventory Service**: Handles stock movement and batch/serial tracking
- **Supplier Service**: Manages supplier info and purchase orders
- **Sales Service**: Manages sales, POS operations, and invoicing
- **Customer Service**: Customer profiles, loyalty programs, and history
- **Reporting Service**: Handles all reports and analytics
- **Branch Service**: Multi-location inventory and stock transfers
- **Config Service**: Manages system-wide configurations

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- PostgreSQL

