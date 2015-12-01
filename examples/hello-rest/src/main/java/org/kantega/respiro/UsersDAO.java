/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by helaar on 19.10.2015.
 */
public class UsersDAO {

    private final DataSource dataSource;
    private final String SQL = "select name from hello_users where username = ?";

    public UsersDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public String findName(String username){

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQL)){

            statement.setString(1,username.toUpperCase());
            ResultSet rs  = statement.executeQuery();
            if( rs.next())
                return rs.getString(1);
            else
                throw new RuntimeException(String.format("Username '%s' not found", username));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
