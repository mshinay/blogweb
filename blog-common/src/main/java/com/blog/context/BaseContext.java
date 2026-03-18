package com.blog.context;

public class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_STATUS = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void setCurrentRole(Integer role) {
        CURRENT_ROLE.set(role);
    }

    public static Integer getCurrentRole() {
        return CURRENT_ROLE.get();
    }

    public static void setCurrentStatus(Integer status) {
        CURRENT_STATUS.set(status);
    }

    public static Integer getCurrentStatus() {
        return CURRENT_STATUS.get();
    }

    public static void removeCurrentId() {
        CURRENT_ID.remove();
    }

    public static void removeCurrentRole() {
        CURRENT_ROLE.remove();
    }

    public static void removeCurrentStatus() {
        CURRENT_STATUS.remove();
    }

    public static void clear() {
        removeCurrentId();
        removeCurrentRole();
        removeCurrentStatus();
    }

}
