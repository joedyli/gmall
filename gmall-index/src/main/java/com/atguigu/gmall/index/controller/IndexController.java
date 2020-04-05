package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("cates")
    public ResponseVo<List<CategoryEntity>> queryLevel1Category() {

        List<CategoryEntity> categoryEntities = this.indexService.queryLevel1Category();

        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("cates/{pid}")
    public ResponseVo<List<CategoryEntity>> querySubCategories(@PathVariable("pid")Long pid){

        List<CategoryEntity> categoryEntities = this.indexService.querySubCategories(pid);

        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("testlock")
    public ResponseVo<Object> testLock(){
        indexService.testLock();

        return ResponseVo.ok(null);
    }

    @GetMapping("read")
    public ResponseVo<String> read(){
        String msg = indexService.readLock();

        return ResponseVo.ok(msg);
    }

    @GetMapping("write")
    public ResponseVo<String> write(){
        String msg = indexService.writeLock();

        return ResponseVo.ok(msg);
    }

    /**
     * 等待
     * @return
     */
    @GetMapping("latch")
    public ResponseVo<Object> countDownLatch(){

        String msg = indexService.latch();

        return ResponseVo.ok(msg);
    }

    /**
     * 计数
     * @return
     */
    @GetMapping("out")
    public ResponseVo<Object> out(){

        String msg = indexService.countDown();

        return ResponseVo.ok(msg);
    }

}
