package com.guajue.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.guajue.gmall.bean.UserAddress;
import com.guajue.gmall.service.UserAddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    @Reference
    private UserAddressService userAddressService;

    @GetMapping("trade/{userId}")
    public List<UserAddress> listAddressByUserId(@PathVariable  String userId) {
        return userAddressService.getUserAddressList(userId);
    }

}
