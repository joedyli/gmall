package com.atguigu.gmall.pms.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrEntity> queryAttrsByCid(Long cid, Integer type, Integer searchType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        // 分类id如果为0，查询所有分类的规格参数
        if (cid != 0) {
            queryWrapper.eq("category_id", cid);
        }
        // 如果参数类型不为空
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        // 如果参数类型不为空
        if (searchType != null) {
            queryWrapper.eq("search_type", searchType);
        }
        return this.list(queryWrapper);
    }

}
