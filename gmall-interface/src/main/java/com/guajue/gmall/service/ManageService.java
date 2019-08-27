package com.guajue.gmall.service;

import com.guajue.gmall.bean.*;

import java.util.List;

public interface ManageService {

    /**
     * 获取所有一级分类
     * @return
     */
     List<BaseCatalog1> listCatalog1();

    /**
     * 根据一级分类ID查询二级分类
     * @param catalog1Id
     * @return
     */
     List<BaseCatalog2> listCatalog2(String catalog1Id);

    /**
     * 根据二级分类ID查询三级分类
     * @param catalog2Id
     * @return
     */
     List<BaseCatalog3> listCatalog3(String catalog2Id);

    /**
     * 根据三级分类查询属性名
     * @param catalog3Id
     * @return
     */
     List<BaseAttrInfo> listAttrList(String catalog3Id);

    /**
     * 新增或修改其属性以及属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 编辑属性，将属性值放入属性对象中
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 根据SpuInfo中的条件查询SpuInfo
     * @param spuInfo
     * @return
     */
    List<SpuInfo> listSpuInfoList(SpuInfo spuInfo);


    /**
     * 查询基本销售属性表
      */
    List<BaseSaleAttr> getBaseSaleAttrList();


    /**
     * 编辑spu信息
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);


    /**
     * 根据spuId获取spuImage中的所有图片列表
     */
    List<SpuImage> getSpuImageList(String spuId);

    /**
     * 根据SpuId获得所有的销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 编辑skuInfo
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据skuId查找Sku
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据SPUID查询所有SpuSaleAttr包括所有SpuSaleAttrVelue，SKU被选中isisChecked为1，否则为0
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 生成json
     * @param spuId
     * @return
     */
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 根据属性值ID获取平台属性值
     * @param attrValueIdList
     * @return
     */
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);

}
