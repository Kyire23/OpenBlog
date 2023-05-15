package com.oumuanode.controller;

import com.oumuanode.constants.SystemConstants;
import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.dto.AddCommentDto;
import com.oumuanode.domain.entity.Comment;
import com.oumuanode.service.CommentService;
import com.oumuanode.utils.BeanCopyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/comment")
@Api(tags = "评论",description = "评论相关接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/commentList")
    @ApiOperation(value = "评论列表",notes = "查询评论")
    public ResponseResult commentList(Long articleId,Integer pageNum,Integer pageSize){
        return commentService.commentList(SystemConstants.ARTICLE_COMMENT,articleId,pageNum,pageSize);
    }


    @PostMapping
    @ApiOperation(value = "评论列表",notes = "添加评论")
    public ResponseResult addComment(@RequestBody AddCommentDto addCommentDto){
        Comment comment = BeanCopyUtils.copyBean(addCommentDto, Comment.class);
        return commentService.addComment(comment);
    }

    //TODO: 删除评论
//    @PostMapping
//    public ResponseResult removeComment(@RequestBody RemoveCommentDto removeCommentDto){
//        return commentService.removeComment(removeCommentDto);
//    }


    @GetMapping("/linkCommentList")
    @ApiOperation(value = "友链评论列表",notes = "获取一页友链评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum",value = "页号"),
            @ApiImplicitParam(name = "pageSize",value = "每页大小"),
    }
    )
    public ResponseResult linkCommentList(Integer pageNum,Integer pageSize){
        return commentService.commentList(SystemConstants.LINK_COMMENT,null,pageNum,pageSize);
    }
}