package com.guajue.gmall.usermanage.controller;


import com.guajue.gmall.bean.UserInfo;
import com.guajue.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    @ResponseBody
    @GetMapping("findAll")
    public List<UserInfo> findAll() {
        return userInfoService.getUserInfoList();
    }


}
