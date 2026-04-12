package com.example.quizapp.app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Subject.java
 * ─────────────────────────────────────────────────────
 * Place this file in:
 *   app/src/main/java/com/example/quizapp/app/models/
 * ─────────────────────────────────────────────────────
 * Maps to the "subjects" table in Supabase.
 * Gson uses @SerializedName to match JSON field names.
 */
public class Subject {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    public int    getId()   { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; } // used in Spinners/Lists
}