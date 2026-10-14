package com.injecapstone.capstonebackend.user.domain;

public enum LearningLevel {

    INTRODUCTORY("입문"),
    BEGINNER("초보"),
    FAMILIAR("익숙");

    private final String displayName;

    LearningLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}