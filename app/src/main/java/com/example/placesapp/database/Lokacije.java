package com.example.placesapp.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Lokacije {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name="lokacija")
    public String name;

    public Lokacije(String name) {
        this.name = name;
    }
}
