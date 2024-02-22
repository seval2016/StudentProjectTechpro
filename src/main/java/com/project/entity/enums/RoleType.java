package com.project.entity.enums;

public enum RoleType {
    //Admin, Teacher, Student, Manager, Assistant_manager
    ADMIN("Admin"),

    TEACHER("Teacher"),

    STUDENT("Student"),

    MANAGER("Dean"),

    ASSISTANT_MANAGER("ViceDean");

    public final String name;

    RoleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
