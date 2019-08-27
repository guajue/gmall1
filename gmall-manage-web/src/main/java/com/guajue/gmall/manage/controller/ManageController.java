package com.guajue.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guajue.gmall.bean.BaseSaleAttr;
import com.guajue.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class ManageController {


    @Reference
    private ManageService manageService;

    @RequestMapping(value = "index" )
    public String index(){
        return "index";
    }


    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return   manageService.getBaseSaleAttrList();
    }
}