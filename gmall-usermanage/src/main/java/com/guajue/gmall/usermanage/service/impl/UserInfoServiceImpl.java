package com.guajue.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.guajue.gmall.bean.UserInfo;
import com.guajue.gmall.service.UserInfoService;
import com.guajue.gmall.serviceutil.util.RedisUtil;
import com.guajue.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;


@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> getUserInfoList() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        
        //将用户的密码做加密处理
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        System.out.println(password);
        //验证用户密码
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        //判断是否正确
        if(info != null){//正确，将用户信息存入redis
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));

            jedis.close();
            return info;
        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        // 去缓存中查询是否有redis
        Jedis jedis = redisUtil.getJedis();
        String key = userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(key);
        // 延长时效
        jedis.expire(key,userKey_timeOut);
        if (userJson!=null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return  userInfo;
        }
        return  null;
    }
}
