package com.oumuanode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oumuanode.constants.SystemConstants;
import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.Link;
import com.oumuanode.domain.vo.LinkVo;
import com.oumuanode.domain.vo.PageVo;
import com.oumuanode.mapper.LinkMapper;
import com.oumuanode.service.LinkService;
import com.oumuanode.utils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service("linkService")
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link> implements LinkService {


    @Override
    public ResponseResult getAllLink() {
        LambdaQueryWrapper<Link> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Link :: getStatus, SystemConstants.LINK_STATUS_NORMAL);

        List<Link> links = list(lambdaQueryWrapper);

        List<LinkVo> linkVos = BeanCopyUtils.copyBeanList(links, LinkVo.class);

        return ResponseResult.okResult(linkVos);


    }

    @Override
    public PageVo selectLinkPage(Link link, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.hasText(link.getName()),Link ::getName,link.getName());
        queryWrapper.eq(Objects.nonNull(link.getStatus()),Link::getStatus, link.getStatus());

        Page<Link> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page(page,queryWrapper);

        //转换成VO
        List<Link> categories = page.getRecords();

        PageVo pageVo = new PageVo();
        pageVo.setTotal(page.getTotal());
        pageVo.setRows(categories);
        return pageVo;

    }
}