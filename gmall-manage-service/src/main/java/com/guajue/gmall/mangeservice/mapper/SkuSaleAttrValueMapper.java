package com.guajue.gmall.mangeservice.mapper;

import com.guajue.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    /**
     * 做json串
     * @param spuId
     * @return
     */
    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
