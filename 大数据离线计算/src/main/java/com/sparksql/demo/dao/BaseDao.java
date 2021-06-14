package com.sparksql.demo.dao;

import com.sparksql.demo.pojo.Hive2Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ytc
 * @ClassName BaseDao
 * @Description TODO
 * @date 2021/06/10
 */
@Repository
public class BaseDao {

    @Autowired
    private Hive2Connection connection;

    public ResultSet execute(PreparedStatement preparedStatement,String database,String sql,ResultSet resultSet){
        try {
            if(database!=null){
                preparedStatement=connection.getConnection().prepareStatement("use "+database);
                preparedStatement.execute();
            }
            resultSet=connection.getConnection().prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }
}
