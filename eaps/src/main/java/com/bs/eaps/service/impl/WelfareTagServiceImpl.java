package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.entity.WelfareTag;
import com.bs.eaps.mapper.WelfareTagMapper;
import com.bs.eaps.service.WelfareTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 福利标签服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WelfareTagServiceImpl implements WelfareTagService {

    private final WelfareTagMapper welfareTagMapper;

    @Override
    public List<WelfareTag> getAllTags() {
        LambdaQueryWrapper<WelfareTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WelfareTag::getStatus, "active"); // 只获取激活状态的标签
        queryWrapper.orderByAsc(WelfareTag::getCategory); // 按分类排序
        return welfareTagMapper.selectList(queryWrapper);
    }

    @Override
    public WelfareTag getTagById(Long id) {
        return welfareTagMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean saveOrUpdateTag(WelfareTag tag) {
        if (tag.getId() == null) {
            // 新建标签
            return welfareTagMapper.insert(tag) > 0;
        } else {
            // 更新标签
            return welfareTagMapper.updateById(tag) > 0;
        }
    }

    @Override
    @Transactional
    public boolean removeTagById(Long id) {
        return welfareTagMapper.deleteById(id) > 0;
    }
}