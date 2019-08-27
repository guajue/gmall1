package com.guajue.gmall.service;

import com.guajue.gmall.bean.SkuLsInfo;
import com.guajue.gmall.bean.SkuLsParams;
import com.guajue.gmall.bean.SkuLsResult;

public interface ListService {

    /**
     * 保存sku到ES
     * @param skuLsInfo
     */
    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    /**
     * 动态查询sku
     * @param skuLsParams
     * @return
     */
    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 利用redis计算热点
     * @param skuId
     */
    public void incrHotScore(String skuId);
}