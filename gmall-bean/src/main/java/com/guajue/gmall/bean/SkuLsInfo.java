package com.guajue.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuLsInfo implements Serializable {

    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    String skuDesc;

    Long hotScore=0L;

    List<SkuLsAttrValue> skuAttrValueList;
}