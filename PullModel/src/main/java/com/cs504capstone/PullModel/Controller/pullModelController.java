package com.cs504capstone.PullModel.Controller;

import com.cs504capstone.PullModel.Domain.Product;
import com.cs504capstone.PullModel.service.Function;
import com.cs504capstone.PullModel.service.MySQLAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by zhujian on 8/9/17.
 */

@RestController
public class pullModelController {

    static private String mysqlHost = "127.0.0.1:3306";
    static private String mysqlDb = "Monitor_System";
    static private String mysqlUser = "root";
    static private String mysqlPass = "root";
    static MySQLAccess mysql;

    @RequestMapping(value = "/Product", method = RequestMethod.GET)
    public List<Product> findReducedProduct(
            @RequestParam(name = "email")String email,
            @RequestParam(name = "category",required = false)String category) throws Exception {
        Function f = new Function(mysqlHost,mysqlDb,mysqlUser,mysqlPass);
        return f.getPriceReducedProd(email,category);
    }


    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public List<String> getCateogry() throws Exception {
        Function f = new Function(mysqlHost,mysqlDb,mysqlUser,mysqlPass);
        return f.getCategories();
    }

    @RequestMapping(value = "/userInterest", method = RequestMethod.GET)
    public Map<String,List<String>> getALlUser() throws Exception {
        Function f = new Function(mysqlHost,mysqlDb,mysqlUser,mysqlPass);
        return f.getUserInterests();
    }




}