package com.guajue.gmall.gmalllistweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.guajue.gmall.bean.BaseAttrInfo;
import com.guajue.gmall.bean.BaseAttrValue;
import com.guajue.gmall.bean.SkuLsParams;
import com.guajue.gmall.bean.SkuLsResult;
import com.guajue.gmall.service.ListService;
import com.guajue.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
@CrossOrigin
public class ListController {


    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams,
                          Model model){

        //设置分页
        skuLsParams.setPageSize(2);

        //获取SKU属性值列表
        SkuLsResult search = listService.search(skuLsParams);

        //获取平台属性值
        List<String> attrValueIdList = search.getAttrValueIdList();
        List<BaseAttrInfo> attrList =  manageService.getAttrList(attrValueIdList);
        String urlParam = makeUrlParam(skuLsParams);
        List<BaseAttrValue> baseAttrValues = new ArrayList<>();

        //清除已选中的属性和属性值
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo =  iterator.next();
                for (BaseAttrValue b:
                        baseAttrInfo.getAttrValueList()) {
                    for (String valueId:
                            skuLsParams.getValueId()) {
                        if(valueId.equals(b.getId())){
                            iterator.remove();

                            //保存面包屑
                            BaseAttrValue baseAttrValue = new BaseAttrValue();
                            baseAttrValue.setValueName(baseAttrInfo.getAttrName()+":"+b.getValueName());
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValue.setUrlParam(makeUrlParam);
                            baseAttrValues.add(baseAttrValue);

                        }
                    }
                }
            }
        }



        model.addAttribute("totalPages", search.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        model.addAttribute("baseAttrValuesList",baseAttrValues);
        model.addAttribute("keyword",   skuLsParams.getKeyword());
        model.addAttribute("urlParam",urlParam);
        model.addAttribute("attrList",attrList);
        model.addAttribute("skuLsInfoList",search.getSkuLsInfoList());

        return "list";
    }

    public String makeUrlParam(SkuLsParams skuLsParam,String... excludeValueIds){

        String urlParam = "";

        //拼接路径
        if(skuLsParam.getKeyword() != null && skuLsParam.getKeyword().length() > 0){
            urlParam += "keyword="+skuLsParam.getKeyword();
        }

        if(skuLsParam.getCatalog3Id() != null && skuLsParam.getCatalog3Id().length() > 0){
            if(urlParam.length() > 0){
                urlParam += "&";
            }
            urlParam += "catalog3Id="+skuLsParam.getCatalog3Id();
        }

        if(skuLsParam.getValueId() != null && skuLsParam.getValueId().length > 0){

            String[] valueId = skuLsParam.getValueId();
            for (int i = 0; i <valueId.length ; i++) {

                if(excludeValueIds != null &&excludeValueIds.length > 0 ){
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId[i])){
                        continue;
                    }
                }

                if(urlParam.length() > 0){
                    urlParam += "&";
                }

                urlParam += "valueId=" + valueId[i];
            }

        }

        return urlParam;

    }

}
