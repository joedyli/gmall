package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        // sku相关信息
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setSkuTitle(skuEntity.getTitle());
            itemVo.setSkuSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            return skuEntity;
        }, threadPoolExecutor);

        // 营销信息
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> itemSaleResponseVo = this.smsClient.queryItemSaleBySkuId(skuId);
            List<ItemSaleVo> itemSales = itemSaleResponseVo.getData();
            itemVo.setSales(itemSales);
        }, threadPoolExecutor);

        // 库存信息，是否有货
        CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
        }, threadPoolExecutor);

        // sku图片
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> skuImagesResponseVo = this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = skuImagesResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);

        // 品牌
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        // 分类
        CompletableFuture<Void> categoryFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<CategoryEntity> categoryResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCatagoryId());
            CategoryEntity categoryEntity = categoryResponseVo.getData();
            if (categoryEntity != null) {
                itemVo.setCategoryId(categoryEntity.getId());
                itemVo.setCategoryName(categoryEntity.getName());
            }
        }, threadPoolExecutor);

        // spu信息
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            Long spuId = skuEntity.getSpuId();
            ResponseVo<SpuEntity> spuResponseVo = this.pmsClient.querySpuById(spuId);
            SpuEntity spuEntity = spuResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        // 销售属性
        CompletableFuture<Void> attrFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySkuAttrValueBySpuId(skuEntity.getSpuId());
            List<SkuAttrValueEntity> saleAttrValueEntities = saleAttrResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueEntities);
        }, threadPoolExecutor);

        // 组及组下规格参数（规格参数要带值）
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupResponseVo = this.pmsClient.queryGroupsBySpuIdAndCid(skuEntity.getSpuId(), skuId, skuEntity.getCatagoryId());
            List<ItemGroupVo> itemGroups = groupResponseVo.getData();
            itemVo.setGroups(itemGroups);
        }, threadPoolExecutor);

        // spu描述信息
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuInfoDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuInfoDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                String[] image = StringUtils.split(spuDescEntity.getDecript(), ",");
                itemVo.setDesc(Arrays.asList(image));
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(salesFuture, storeFuture, imageFuture, brandFuture, categoryFuture, spuFuture, attrFuture, groupFuture, descFuture).join();

        return itemVo;
    }
}
