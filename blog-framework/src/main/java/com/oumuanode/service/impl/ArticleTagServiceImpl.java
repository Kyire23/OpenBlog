package com.oumuanode.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oumuanode.domain.entity.ArticleTag;
import com.oumuanode.mapper.ArticleTagMapper;
import com.oumuanode.service.ArticleTagService;
import org.springframework.stereotype.Service;


@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {
}
