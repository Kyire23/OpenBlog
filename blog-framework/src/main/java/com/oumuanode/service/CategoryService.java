package com.oumuanode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.Category;
import com.oumuanode.domain.vo.CategoryVo;
import com.oumuanode.domain.vo.PageVo;

import java.util.List;

public interface CategoryService extends IService<Category> {

    ResponseResult getCategoryList();

    List<CategoryVo> listAllCategory();

    PageVo selectCategoryPage(Category category, Integer pageNum, Integer pageSize);
}