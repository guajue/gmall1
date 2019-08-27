package com.guajue.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guajue.gmall.bean.CartInfo;
import com.guajue.gmall.bean.SkuInfo;
import com.guajue.gmall.cart.handler.CartCookieHandler;
import com.guajue.gmall.service.CartService;
import com.guajue.gmall.service.ManageService;
import com.guajue.gmall.webutil.util.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {


    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    /**
     * 将商品加入购物车
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        CartInfo cartInfo = null;

        // 获取userId，skuId，skuNum
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        String userId = (String) request.getAttribute("userId");

        //判断是否登录
        if(userId != null && userId.length() > 0){//说明用户已经登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {// 说明用户没有登录没有登录放到cookie中
            cartCookieHandler.addToCart(request, response, skuId, userId, Integer.parseInt(skuNum));
        }
        // 取得sku信息对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    /**
     * 将显示购物车列表（判断是否登录，没登录取cookie的，登录了取用户的）
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response) {

        //获取userID
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = new ArrayList<CartInfo>();
        //判断用户是否登录
        if(userId != null && userId.length() > 0){//已登录，从数据库中取
            //查看cookie中是否有购物车
            List<CartInfo> cartInfos = cartCookieHandler.cartList(request);
            if(cartInfos != null && cartInfos.size() == 0){
                //合并购物车
                cartList=cartService.mergeToCartList(cartInfos,userId);
                //清空cookie
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                cartList = cartService.cartList(userId);
            }
        }else{//未登录，从cookie中取
            cartList = cartCookieHandler.cartList(request);
        }

        request.setAttribute("cartList",cartList);
        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){

        // 获取userId，skuId，skuNum
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId=(String) request.getAttribute("userId");

        //判断是否登录
        if(userId != null && userId.length() > 0){//说明用户已经登录
            cartService.checkCart(skuId,isChecked,userId);
        }else {
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){

        //获取用户ID
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cookieHandlerCartList = cartCookieHandler.cartList(request);

        //合并购物车到数据库和redis
        if (cookieHandlerCartList!=null && cookieHandlerCartList.size()>0){
            cartService.mergeToCartList(cookieHandlerCartList, userId);
            cartCookieHandler.deleteCartCookie(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }
}
