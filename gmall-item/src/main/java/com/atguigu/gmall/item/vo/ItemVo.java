package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVo {

    // 分类
    private Long categoryId;
    private String categoryName;

    // 品牌
    private Long brandId;
    private String brandName;

    // spu信息
    private Long spuId;
    private String spuName;

    // sku相关信息
    private Long skuId;
    private String skuTitle;
    private String skuSubTitle;
    private BigDecimal price;
    private Integer weight;

    private List<ItemSaleVo> sales;  // 营销信息

    private Boolean store = false; // 是否有货

    // 需要sku所属spu下的所有sku的营销信息
    private List<SkuAttrValueEntity> saleAttrs;

    private List<SkuImagesEntity> images; // sku的图片信息

    private List<String> desc; // 海报信息

    private List<ItemGroupVo> groups; // 分组及组下的规格参数

}
