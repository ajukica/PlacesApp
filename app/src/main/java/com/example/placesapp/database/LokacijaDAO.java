package com.example.placesapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LokacijaDAO {
    @Insert
    void insertAll(Lokacije... lokacije);
    @Query("SELECT * FROM lokacije")
    List<Lokacije> getAllLokacije();



    @Query("DELETE FROM lokacije WHERE id = :id")
     void delete(long id);

}
