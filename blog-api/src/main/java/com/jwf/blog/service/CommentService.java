package com.jwf.blog.service;

import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.params.CommentParam;

public interface CommentService {

    Result commentsByArticleId(Long articleId);

    Result comment(CommentParam commentParam);
}
