package com.oumuanode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.dto.TagListDto;
import com.oumuanode.domain.entity.Tag;
import com.oumuanode.domain.vo.PageVo;

public interface TagService extends IService<Tag> {

    ResponseResult<PageVo> pageTagList(Integer pageNum, Integer pageSize, TagListDto tagListDto);
}