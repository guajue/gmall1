package com.guajue.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.guajue.gmall.bean.CartInfo;
import com.guajue.gmall.bean.SkuInfo;
import com.guajue.gmall.service.ManageService;
import com.guajue.gmall.webutil.util.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";

    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManageService manageService;

    /**
     * 未登录的时候，把购物车保存到cookie中
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum){

        CartInfo cartInfo = null;

        //查询sku
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        //先查询cookie里是否有购物车，isDecoder:购物车可能有中文，所以要序列化
        String cartCookie = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();

        boolean flag = false;//用于判断购物车中是否有该SKU
        if(cartCookie != null) {//cookie已有购物车，查询是否有对应的商品，有就更改，没有就罢了
            cartInfoList = JSON.parseArray(cartCookie, CartInfo.class);

            for (int i = 0; i < cartInfoList.size(); i++) {
                cartInfo = cartInfoList.get(i);
                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setSkuNum(skuNum + cartInfo.getSkuNum());
                    cartInfo.setSkuPrice(skuInfo.getPrice());
                    flag = true;
                    break;
                }
            }
        }
            if(!flag){//如果cookie中没有购物车或者购物车中没有该类商品
                cartInfo = new CartInfo();
                cartInfo.setSkuId(skuId);
                cartInfo.setCartPrice(skuInfo.getPrice());
                cartInfo.setSkuPrice(skuInfo.getPrice());
                cartInfo.setSkuName(skuInfo.getSkuName());
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

                cartInfo.setUserId(userId);
                cartInfo.setSkuNum(skuNum);
                cartInfoList.add(cartInfo);

            }

            //将购物车放入cookie中
            String newCartJson  = JSON.toJSONString(cartInfoList);

            CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

    }

    public List<CartInfo> cartList(HttpServletRequest request) {

        String cartCookie = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartList = JSON.parseArray(cartCookie, CartInfo.class);

        return cartList;
    }

    public void deleteCartCookie(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        
        //取出cookie中所有的购物车
        List<CartInfo> cartInfos = cartList(request);

        for (int i = 0; i <cartInfos.size() ; i++) {
            if(cartInfos.get(i).getSkuId().equals(skuId)){
                cartInfos.get(i).setIsChecked(isChecked);
            }
        }

        //重新保存到cookie里
        String cartListJson = JSON.toJSONString(cartInfos);

        CookieUtil.setCookie(request,response,cookieCartName,cartListJson,COOKIE_CART_MAXAGE,true);

    }
}
