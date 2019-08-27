package com.guajue.gmall.mangeservice.mapper;

import com.guajue.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 属性名
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo>{

    /**
     *   根据三级分类id查询属性表
     */
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);

    /**
     *
     * @param ids
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("ids") String ids);
}
