package com.guajue.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.guajue.gmall.bean.SkuInfo;
import com.guajue.gmall.bean.SkuSaleAttrValue;
import com.guajue.gmall.bean.SpuSaleAttr;
import com.guajue.gmall.service.ListService;
import com.guajue.gmall.service.ManageService;
import com.guajue.gmall.webutil.util.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("{skuId}")
    @LoginRequire
    public String skuInfoPage(@PathVariable(value = "skuId") String skuId,
                              Model model){

        //记录热度
        listService.incrHotScore(skuId);


        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);

        //显示属性和属性值
        List<SpuSaleAttr> saleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        model.addAttribute("saleAttrList",saleAttrList);

        //根据属性跳转
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        String key="";
        Map<String, String> stringStringHashMap = new HashMap<>();

        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {

            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);

            if(key.length() != 0){
                key= key+"|";
            }
            key = key+skuSaleAttrValue.getSaleAttrValueId();


            if((i+1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                stringStringHashMap.put(key,skuSaleAttrValue.getSkuId());
                key="";
            }
        }

        String jsonString = JSON.toJSONString(stringStringHashMap);
        model.addAttribute("valuesSkuJson",jsonString);



        return "item";
    }


}
