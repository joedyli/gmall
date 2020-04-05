package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

//@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<SpuAttrValueVo> baseAttrs;

    // sku信息
    private List<SkuVo> skus;

    public List<String> getSpuImages() {
        return spuImages;
    }

    public void setSpuImages(List<String> spuImages) {
        this.spuImages = spuImages;
    }

    public List<SpuAttrValueVo> getBaseAttrs() {
        return baseAttrs;
    }

    public void setBaseAttrs(List<SpuAttrValueVo> baseAttrs) {
        this.baseAttrs = baseAttrs;
    }

    public List<SkuVo> getSkus() {
        return skus;
    }

    public void setSkus(List<SkuVo> skus) {
        this.skus = skus;
    }
}
