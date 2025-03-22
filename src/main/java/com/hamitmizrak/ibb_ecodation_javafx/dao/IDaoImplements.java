package com.hamitmizrak.ibb_ecodation_javafx.dao;

import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonDBConnection;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;

import java.sql.Connection;
import java.util.Optional;

public interface IDaoImplements<T> extends ICrud,IGenericsMethod,ILogin {


    /// ////////////////////////////////////////////////////////////////
    // Gövdeli Method
    default Connection iDaoImplementsDatabaseConnection() {
        // Singleton DB
        return SingletonDBConnection.getInstance().getConnection();

        // Singleton Config
        //return SingletonPropertiesDBConnection.getInstance().getConnection();
    }

    // CRUD
    // CREATE
    Optional<UserDTO> create(UserDTO userDTO);
}
