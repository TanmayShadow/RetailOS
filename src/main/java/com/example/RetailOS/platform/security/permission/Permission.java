package com.example.RetailOS.platform.security.permission;

public enum Permission {

    // Product
    PRODUCT_VIEW,
    PRODUCT_CREATE,
    PRODUCT_UPDATE,
    PRODUCT_DELETE,

    // Customer
    CUSTOMER_VIEW,
    CUSTOMER_CREATE,
    CUSTOMER_UPDATE,
    CUSTOMER_DELETE,

    // Supplier
    SUPPLIER_VIEW,
    SUPPLIER_CREATE,
    SUPPLIER_UPDATE,
    SUPPLIER_DELETE,

    // Inventory
    INVENTORY_VIEW,
    INVENTORY_UPDATE,

    // Billing
    BILL_CREATE,
    BILL_CANCEL,
    BILL_VIEW,

    // User Management
    USER_VIEW,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,

    // Reports
    REPORT_VIEW
}