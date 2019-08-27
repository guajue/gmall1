package com.guajue.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.guajue.gmall.bean.*;
import com.guajue.gmall.service.ListService;
import com.guajue.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class AttrManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;


    @PostMapping("getCatalog1")
    public List<BaseCatalog1> listBaseCatalog1() {
        return manageService.listCatalog1();
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> listCatalog2(@RequestParam String catalog1Id) {
        return manageService.listCatalog2(catalog1Id);
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> listCatalog3(@RequestParam String catalog2Id) {
        return manageService.listCatalog3(catalog2Id);
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> listAttrList(@RequestParam String catalog3Id) {
        return manageService.listAttrList(catalog3Id);
    }

    @PostMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {

        // 保存属性极其属性值
        manageService.saveAttrInfo(baseAttrInfo);
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> listAttrValueList(@RequestParam  String attrId){

        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);

        if(attrInfo == null) {
            return null;
        }

        return attrInfo.getAttrValueList();
    }

    /**
     * 保存SKU到ES中(上架SKU)
     * @param skuId
     */
    @GetMapping(value = "onSale")
    public void onSale(String skuId){

        //根据skuID获取SKU
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        //将SKU保存到ES
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuInfo(skuLsInfo);

    }
}
