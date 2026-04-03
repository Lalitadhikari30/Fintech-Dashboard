package com.fintech.dashboard.model.enums;

/**
 * User roles that define access levels in the system.
 *
 * VIEWER  → Read-only access to financial records
 * ANALYST → Viewer privileges + dashboard analytics
 * ADMIN   → Full access: CRUD on records + user management
 */
public enum Role {
    VIEWER,
    ANALYST,
    ADMIN
}
