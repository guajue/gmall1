package com.guajue.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.guajue.gmall.bean.UserAddress;
import com.guajue.gmall.service.UserAddressService;
import com.guajue.gmall.usermanage.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {
    
    @Autowired
    private UserAddressMapper userAddressMapper;
    
    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        
        //创建Example
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);

        return userAddressMapper.selectByExample(example);
    }
}
