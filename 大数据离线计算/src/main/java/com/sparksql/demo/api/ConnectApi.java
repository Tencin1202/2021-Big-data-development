package com.sparksql.demo.api;

import com.sparksql.demo.pojo.Hive2Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.Properties;

/**
 * @author ytc
 * @ClassName ConnectApi
 * @Description TODO
 * @date 2021/06/09
 */
@RestController
@RequestMapping("/connect")
public class ConnectApi {

    @Autowired
    private Hive2Connection connection;

    @PostMapping("login")
    public String getConnection(String url, String user, String password){
        if(connection.getConnection()!=null) {
            return connection.getUser();
        }
        try{
            Properties properties=new Properties();
            properties.setProperty("driverClassName", "org.apache.hive.jdbc.HiveDriver");
            properties.setProperty("user", user);
            properties.setProperty("password", password);
            connection.setConnection(DriverManager.getConnection(url, properties));
            connection.setUser(user);
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
        return connection.getUser();
    }

    @PostMapping("/logout")
    public String disConnection(){
        if(connection.getConnection()!=null){
            try {
                connection.getConnection().close();
                connection.setConnection(null);
                connection.setUser(null);
            } catch (SQLException e) {
                e.printStackTrace();
                return "error";
            }
        }
        return "ok";
    }
    @PostMapping("/user")
    public String getUser(){
        if(connection.getConnection()!=null){
            return connection.getUser();
        }else{
            return "未登录";
        }
    }
}
