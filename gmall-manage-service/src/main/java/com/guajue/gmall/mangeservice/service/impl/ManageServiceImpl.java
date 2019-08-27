package com.guajue.gmall.mangeservice.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.guajue.gmall.bean.*;
import com.guajue.gmall.mangeservice.constant.ManageConst;
import com.guajue.gmall.mangeservice.mapper.*;
import com.guajue.gmall.service.ManageService;
import com.guajue.gmall.serviceutil.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;


    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> listCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }


    @Override
    public List<BaseCatalog2> listCatalog2(String catalog1Id) {

        //创建BaseCatalog2对象
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> listCatalog3(String catalog2Id) {

        //创建BaseCatalog2对象
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> listAttrList(String catalog3Id) {

        //创建BaseAttrInfo对象
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);

        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
    }
    
    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //有ID修改，没有就添加
        String id = baseAttrInfo.getId();

        if(StringUtils.isEmpty(id)||id.length()<1) {//没有ID,添加属性

            //保存属性
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

        }else {//有ID，修改属性

            //修改属性
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);

            //清空该属性原有属性值
            Example example = new Example(BaseAttrValue.class);
            example.createCriteria().andEqualTo("attrId", id);
            baseAttrValueMapper.deleteByExample(example);

        }

        //添加该属性的属性值
        if(baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {

            for (BaseAttrValue baseAttrValue:
                    baseAttrInfo.getAttrValueList()) {

                //防止ID是一个空字符串
                //baseAttrValue.setId(null);
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {

        //根据属性ID查询得到属性对象
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        if(baseAttrInfo == null){
            return null;
        }

        //查询该属性ID下的所有属性值
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        //将属性值添加入该属性中
        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> listSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        //根据有没有ID判断是新增还是修改
        if(spuInfo.getId() == null || spuInfo.getId().length() < 1){//没有ID，新增SPU
            spuInfoMapper.insertSelective(spuInfo);
        }else{//有ID，根据ID修改SPU
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }

        //清空该spuInfo中的图片和属性
        //清空图片
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        //清空属性极其属性值
        //清空属性
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        //清空属性值
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        //将更新的图片加入数据库
        if(spuInfo.getSpuImageList() != null && spuInfo.getSpuImageList().size() > 0) {
            for (SpuImage s:
                    spuInfo.getSpuImageList()) {

                s.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(s);
            }
        }

        //将更新的属性和属性值加入数据库
        if(spuInfo.getSpuSaleAttrList() != null && spuInfo.getSpuSaleAttrList().size() > 0) {
            for (SpuSaleAttr s:
                    spuInfo.getSpuSaleAttrList() ) {

                //将属性加入数据库
                s.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(s);

                //将每个属性的属性值加入数据库
                for (SpuSaleAttrValue sv:
                     s.getSpuSaleAttrValueList()) {

                    sv.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(sv);
                }

            }
        }

    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {

        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);

        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        //根据skuInfo是否有ID来判断新增或者修改
        if(skuInfo.getId() != null && skuInfo.getId().length() > 0) {//有ID，为修改
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }else{//没有ID，新增SKU
            skuInfoMapper.insertSelective(skuInfo);
        }

        //清空该SKU的skuImageList，skuAttrValueList，skuSaleAttrValueList
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        //添加新的skuImageList，skuAttrValueList，skuSaleAttrValueList
        if(skuInfo.getSkuImageList() != null && skuInfo.getSkuImageList().size() > 0) {
            for (SkuImage si:
                    skuInfo.getSkuImageList()) {
                si.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(si);
            }
        }

        if(skuInfo.getSkuAttrValueList() != null && skuInfo.getSkuAttrValueList().size() > 0) {
            for (SkuAttrValue sa:
                    skuInfo.getSkuAttrValueList()) {
                sa.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(sa);
            }
        }

        if(skuInfo.getSkuSaleAttrValueList() != null && skuInfo.getSkuSaleAttrValueList().size() > 0) {
            for (SkuSaleAttrValue sv:
                    skuInfo.getSkuSaleAttrValueList()) {
                sv.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(sv);
            }
        }

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo = null;

        try{

            Jedis jedis = redisUtil.getJedis();
            String skuInfoKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_PREFIX;

            String skuJson = jedis.get(skuInfoKey);
            if(skuJson == null || skuJson.length() == 0){//没能命中缓存

                //获取分布式锁的key
                String skuLockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey, "OK", "nx", "px", ManageConst.SKULOCK_EXPIRE_PX);

                if("OK".equals(lockKey)){//获取锁

                    //从数据库中获取数据
                    skuInfo = getSkuInfoDB(skuId);

                    //重新放入缓存中
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));

                    //放回连接
                    jedis.close();

                    return skuInfo;
                }else{

                    Thread.sleep(ManageConst.SKULOCK_EXPIRE_PX);

                    return getSkuInfo(skuId);
                }
            }else{//命中缓存

                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return getSkuInfoDB(skuId);
    }


    public SkuInfo getSkuInfoDB(String skuId) {

        //根据ID查SkuInfo
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo == null) {
            return null;
        }
        //根据skuInfo的ID查图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        
        //保存skuAttrValueList
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }



    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        if(skuInfo == null){
            return null;
        }

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {

        String[] strArray = new String[attrValueIdList.size()];
        for (int i = 0; i <strArray.length ; i++) {
            strArray[i] = attrValueIdList.get(i);
        }
        String ids = StringUtils.join(strArray, ",");
        System.out.println(ids);
        return baseAttrInfoMapper.selectAttrInfoListByIds(ids);

    }
}
