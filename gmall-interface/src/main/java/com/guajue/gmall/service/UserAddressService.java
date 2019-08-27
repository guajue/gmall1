package com.guajue.gmall.service;

import com.guajue.gmall.bean.UserAddress;

import java.util.List;

public interface UserAddressService {


    /**
     * 根据用户ID查询地址
     * @param userId
     * @return
     */
    public List<UserAddress> getUserAddressList(String userId);
}
