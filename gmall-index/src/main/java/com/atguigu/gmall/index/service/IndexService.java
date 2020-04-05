package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    public List<CategoryEntity> queryLevel1Category() {
        ResponseVo<List<CategoryEntity>> categoryResp = this.gmallPmsFeign.queryCategory(0l);
        return categoryResp.getData();
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String KEY_PREFIX = "index:category:";

    @GmallCache(prefix = KEY_PREFIX, timeout = 14400, random = 3600, lock = "lock")
    public List<CategoryEntity> querySubCategories(Long pid) {

        ResponseVo<List<CategoryEntity>> listResp = this.gmallPmsFeign.querySubCategory(pid);
        List<CategoryEntity> categoryVOS = listResp.getData();

        return categoryVOS;
    }
//    public List<CategoryEntity> querySubCategories(Long pid) {
//
//        // 从缓存中获取
//        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (StringUtils.isNotBlank(cacheCategories)){
//            // 如果缓存中有，直接返回
//            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
//            return categoryEntities;
//        }
//
//        ResponseVo<List<CategoryEntity>> subCategoryResp = this.gmallPmsFeign.querySubCategory(pid);
//
//        // 把查询结果放入缓存
//        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(subCategoryResp), 30, TimeUnit.DAYS);
//
//        return subCategoryResp.getData();
//    }

    @Autowired
    private RedissonClient redissonClient;

    public void testLock() {

        RLock lock = this.redissonClient.getLock("lock"); // 只要锁的名称相同就是同一把锁
        lock.lock(10, TimeUnit.SECONDS); // 加锁

        // 查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isBlank(value)) {
            return;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把redis中的num值+1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

//        lock.unlock(); // 解锁
    }

    public String readLock() {
        // 初始化读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readWriteLock.readLock(); // 获取读锁

        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁

        String msg = this.redisTemplate.opsForValue().get("msg");

        //rLock.unlock(); // 解锁
        return msg;
    }

    public String writeLock() {
        // 初始化读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readWriteLock.writeLock(); // 获取写锁

        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁

        this.redisTemplate.opsForValue().set("msg", UUID.randomUUID().toString());

        //rLock.unlock(); // 解锁
        return "成功写入了内容。。。。。。";
    }

    public String latch() {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");
        try {
            countDownLatch.trySetCount(6);
            countDownLatch.await();

            return "关门了。。。。。";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String countDown() {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");

        countDownLatch.countDown();
        return "出来了一个人。。。";
    }

//    public void testLock() {
//        // 1. 从redis中获取锁,setnx
//        String uuid = UUID.randomUUID().toString();
//        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
//        if (lock) {
//            // 查询redis中的num值
//            String value = this.redisTemplate.opsForValue().get("num");
//            // 没有该值return
//            if (StringUtils.isBlank(value)){
//                return ;
//            }
//            // 有值就转成成int
//            int num = Integer.parseInt(value);
//            // 把redis中的num值+1
//            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
//
//            // 2. 释放锁 del
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), uuid);
////            if (StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("num"))) {
////                this.redisTemplate.delete("lock");
////            }
//        } else {
//            // 3. 每隔1秒钟回调一次，再次尝试获取锁
//            try {
//                Thread.sleep(1000);
//                testLock();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
