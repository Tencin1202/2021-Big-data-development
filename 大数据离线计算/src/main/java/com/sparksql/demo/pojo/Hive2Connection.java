package com.sparksql.demo.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.*;

/**
 * @author ytc
 * @ClassName SparkSession
 * @Description TODO
 * @date 2021/06/09
 */
@Data
@Component
public class Hive2Connection {

    private Connection connection;
    private String user;
}
