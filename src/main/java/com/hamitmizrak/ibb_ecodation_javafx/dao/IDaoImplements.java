package com.hamitmizrak.ibb_ecodation_javafx.dao;

import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonDBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IDaoImplements<T> extends ILogin, IGenericsMethod {

    // CREATE
    Optional<T> create(T t);

    // LIST
    Optional<List<T>> list();

    // FIND
    Optional<T> findByName(String name);

    Optional<T> findById(int id);

    // UPDATE
    Optional<T> update(int id, T t);

    // DELETE
    Optional<T> delete(int id);

    /// ////////////////////////////////////////////////////////////////
    // Gövdeli Method
    default Connection iDaoImplementsDatabaseConnection() {
        // Singleton DB
        return SingletonDBConnection.getInstance().getConnection();

        // Singleton Config
        //return SingletonPropertiesDBConnection.getInstance().getConnection();
    }
}
