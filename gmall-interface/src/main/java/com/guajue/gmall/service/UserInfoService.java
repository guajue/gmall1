package com.guajue.gmall.service;

import com.guajue.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {


    /**
     * 显示所有用户
     * @return
     */
    List<UserInfo> getUserInfoList();

    /**
     * 登录验证
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 验证用户是否登录
     * @param userId
     * @return
     */
    public UserInfo verify(String userId);
}