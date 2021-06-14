package com.sparksql.demo.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sparksql.demo.dao.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author ytc
 * @ClassName QueryApi
 * @Description TODO
 * @date 2021/06/10
 */
@RestController
@RequestMapping("sql")
public class SQLApi {

    @Autowired
    private BaseDao baseDao;

    @PostMapping("/databases")
    public List<String> showDatabases(){
        String sql="show databases";
        PreparedStatement preparedStatement=null;
        ResultSet rs=null;
        rs =baseDao.execute(preparedStatement,null,sql, rs);
        List<String> databases=new ArrayList<>();
        try{
            while(rs.next()){
                databases.add(rs.getString(1));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(rs!=null) {
                    rs.close();
                }
                if(preparedStatement!=null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return databases;
    }

    @PostMapping("/tables")
    public List<String> showTables(String database){
        String sql="show tables";
        PreparedStatement preparedStatement=null;
        ResultSet rs=null;
        List<String> tables=new ArrayList<>();
        rs =baseDao.execute(preparedStatement,database, sql, rs);
        try{
            while(rs.next()){
                tables.add(rs.getString(1));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(rs!=null) {
                    rs.close();
                }
                if(preparedStatement!=null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tables;
    }

    @PostMapping("/query")
    public JSONArray query(String database,String sql){
        JSONArray list=new JSONArray();
        ResultSet rs=null;
        PreparedStatement preparedStatement=null;
        rs =baseDao.execute(preparedStatement,database, sql, rs);
        Map<String, String> map = new LinkedHashMap<>();
        String name;
        try{
            int col=rs.getMetaData().getColumnCount();
            for(int i=1;i<=col;i++){
                map.put(rs.getMetaData().getColumnLabel(i),"");
            }
            while(rs.next()){
                for(int i=1;i<=col;i++){
                    name=rs.getMetaData().getColumnLabel(i);
                    map.put(name,rs.getString(name));
                }
                list.add(JSON.toJSON(map));
            }
            if(list.size()==0)
                list.add(JSON.toJSON(map));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(rs!=null) {
                    rs.close();
                }
                if(preparedStatement!=null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

}
