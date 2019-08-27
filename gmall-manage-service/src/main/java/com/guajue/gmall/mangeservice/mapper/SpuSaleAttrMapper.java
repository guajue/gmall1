package com.guajue.gmall.mangeservice.mapper;

import com.guajue.gmall.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    /**
     *根据SPU ID查询所有SpuSaleAttr宝包括所有SpuSaleAttrVelue
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    /**
     *根据SPUID查询所有SpuSaleAttr包括所有SpuSaleAttrVelue，SKU被选中isisChecked为1，否则为0
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku( String skuId, String spuId);
}
