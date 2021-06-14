package com.sparksql.demo.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author ytc
 * @ClassName indexApi
 * @Description TODO
 * @date 2021/06/11
 */
@Controller
public class indexApi {
    @RequestMapping("/db")
    public String welcome(){
        return "db";
    }
}
