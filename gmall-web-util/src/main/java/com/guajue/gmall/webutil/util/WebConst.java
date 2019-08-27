package com.guajue.gmall.webutil.util;

public interface WebConst {
    //登录页面
    public final static String LOGIN_ADDRESS="http://passport.guajue.com/index";
    //认证接口
    public final static String VERIFY_ADDRESS="http://passport.guajue.com/verify";
    //cookie的有效时间：默认给7天
    public final static int COOKIE_MAXAGE=7*24*3600;
}