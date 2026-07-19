package com.example.RetailOS.platform.security.permission;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {

    // Platform (website) administrator — not tied to any shop.
    SUPER_ADMIN(Set.of(
            Permission.PLAN_VIEW_ALL,
            Permission.PLAN_CREATE,
            Permission.PLAN_UPDATE,
            Permission.PLAN_DELETE,

            Permission.SUBSCRIPTION_VIEW_ALL,
            Permission.SUBSCRIPTION_UPDATE
    )),

    OWNER(Set.of(
            Permission.PRODUCT_VIEW,
            Permission.PRODUCT_CREATE,
            Permission.PRODUCT_UPDATE,
            Permission.PRODUCT_DELETE,

            Permission.CUSTOMER_VIEW,
            Permission.CUSTOMER_CREATE,
            Permission.CUSTOMER_UPDATE,
            Permission.CUSTOMER_DELETE,

            Permission.SUPPLIER_VIEW,
            Permission.SUPPLIER_CREATE,
            Permission.SUPPLIER_UPDATE,
            Permission.SUPPLIER_DELETE,

            Permission.INVENTORY_VIEW,
            Permission.INVENTORY_UPDATE,

            Permission.BILL_CREATE,
            Permission.BILL_CANCEL,
            Permission.BILL_VIEW,

            Permission.USER_VIEW,
            Permission.USER_CREATE,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,

            Permission.REPORT_VIEW
    )),

    MANAGER(Set.of(
            Permission.PRODUCT_VIEW,
            Permission.PRODUCT_CREATE,
            Permission.PRODUCT_UPDATE,

            Permission.CUSTOMER_VIEW,
            Permission.CUSTOMER_CREATE,

            Permission.INVENTORY_VIEW,
            Permission.INVENTORY_UPDATE,

            Permission.BILL_CREATE,
            Permission.BILL_VIEW,

            Permission.REPORT_VIEW
    )),

    CASHIER(Set.of(
            Permission.PRODUCT_VIEW,
            Permission.CUSTOMER_VIEW,

            Permission.BILL_CREATE,
            Permission.BILL_VIEW
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
