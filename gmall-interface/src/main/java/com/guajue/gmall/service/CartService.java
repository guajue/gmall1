package com.guajue.gmall.service;

import com.guajue.gmall.bean.CartInfo;

import java.util.ArrayList;
import java.util.List;

public interface CartService {

    /**
     * 将物品加入购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(String skuId, String userId, Integer skuNum);

    /**
     * 显示购物车
     * @param userId
     * @return
     */
    public List<CartInfo> cartList(String userId);

    /**
     * 合并购物车
     * @param cartInfos
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartInfos, String userId);

    /**
     * 改变选中状态
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);
}