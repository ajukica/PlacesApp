package com.example.placesapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.placesapp.database.LokacijaDAO;
import com.example.placesapp.database.Lokacije;

@Database(entities = {Lokacije.class},version = 1)
public abstract  class LokacijeDatabase extends RoomDatabase {
    public abstract LokacijaDAO lokacijaDAO();
}
