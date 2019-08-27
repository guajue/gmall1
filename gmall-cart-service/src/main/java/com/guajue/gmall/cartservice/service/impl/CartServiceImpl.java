package com.guajue.gmall.cartservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.guajue.gmall.bean.CartInfo;
import com.guajue.gmall.bean.SkuInfo;
import com.guajue.gmall.cartservice.constant.CartConst;
import com.guajue.gmall.cartservice.mapper.CartInfoMapper;
import com.guajue.gmall.service.CartService;
import com.guajue.gmall.service.ManageService;
import com.guajue.gmall.serviceutil.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl  implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        //先查询是否有购物车
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);

        //根据skuId查询SkuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        if(cartInfoExist != null){//更新购物车

            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);//更新数量
            cartInfoExist.setSkuPrice(skuInfo.getPrice());//更新最新价格
            //更新到数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);

        }else{//没有，新增购物项

            cartInfoExist = new CartInfo();
            cartInfoExist.setSkuId(skuId);
            cartInfoExist.setUserId(userId);
            cartInfoExist.setSkuNum(skuNum);//数量
            cartInfoExist.setImgUrl(skuInfo.getSkuDefaultImg());//图片
            cartInfoExist.setSkuPrice(skuInfo.getPrice());//最新价格
            cartInfoExist.setCartPrice(skuInfo.getPrice());//购物车价
            cartInfoExist.setSkuName(skuInfo.getSkuName());
            cartInfoMapper.insertSelective(cartInfoExist);
        }

        //将购物车保存到redis
        // 构建key user:userid:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        String cartJson = JSON.toJSONString(cartInfoExist);
        jedis.hset(userCartKey,skuId,cartJson);

        // 更新购物车过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }

    @Override
    public List<CartInfo> cartList(String userId) {

        List<CartInfo> cartList = new ArrayList<>();

        //查询redis是否有购物车
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartJSONList = jedis.hvals(userCartKey);

        if(cartJSONList != null && cartJSONList.size() > 0){
            for (String s:
                    cartJSONList) {
                CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartList.add(cartInfo);
            }
            cartList.sort(new Comparator<CartInfo>(){

                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o1.getId()),Long.parseLong(o2.getId()));
                }
            });
        }

        if(cartList == null && cartList.size() == 0){//说明redis中没有该购物车

            cartList = loadCartCache(userId);
        }

        jedis.close();
        return cartList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfos, String userId) {

        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);

        //将cookie中购物车加入数据库
        for (CartInfo c:
        cartInfos) {

            boolean isMath = false;//用于判断该用户购物车中是否有商品的变量
            for (CartInfo cartInfo:
                    cartInfoListDB) {
                if(c.getSkuId().equals(cartInfo.getSkuId())){
                    Integer num = c.getSkuNum() > cartInfo.getSkuNum() ? c.getSkuNum() : (c.getSkuNum() == cartInfo.getSkuNum() ? c.getSkuNum() : cartInfo.getSkuNum());
                    cartInfo.setSkuNum(num);
                    cartInfoMapper.updateByPrimaryKey(cartInfo);
                    isMath = true;
                }
            }
            if(!isMath){
                c.setUserId(userId);
                cartInfoMapper.insertSelective(c);
            }

        }
        
        //保证登录后选中状态不会丢失
        List<CartInfo> cartInfoDB = loadCartCache(userId);
        for (CartInfo cart:
                cartInfoDB) {

            for (CartInfo c:
                    cartInfos) {
                if(cart.getSkuId().equals(c.getSkuId())){
                    if("1".equals(c.getIsChecked())){
                        cart.setIsChecked(c.getIsChecked());
                        checkCart(cart.getSkuId(),cart.getIsChecked(),userId);
                    }
                }
            }
        }


        return loadCartCache(userId);
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        //改变redis选中状态
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckdJson  = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);

        //新增到用户已选中的状态中
        String userCheckdKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        if("1".equals(isChecked)){
            jedis.hset(userCheckdKey,skuId,cartCheckdJson);
        }else{
            jedis.hdel(userCheckdKey,skuId);
        }

        jedis.close();

    }

    /**
     * 购物车查询，在数据库中查找
     * @param userId
     * @return
     */
    public  List<CartInfo> loadCartCache(String userId){

        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        if(cartInfoList == null && cartInfoList.size() == 0){
            return null;
        }

        //将cartInfoList放入缓存中
        Map<String,String> map = new HashMap(cartInfoList.size());
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        for (CartInfo c:
        cartInfoList) {
            String cartString = JSON.toJSONString(c);
            map.put(c.getSkuId(),cartString);
        }

        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }
}
