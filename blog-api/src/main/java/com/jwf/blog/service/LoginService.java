package com.jwf.blog.service;

import com.jwf.blog.dao.pojo.SysUser;
import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.params.LoginParam;

public interface LoginService {
    /**
     * 登录
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    SysUser checkToken(String token);

    /**
     * 退出登录
     * @param token
     * @return
     */
    Result logout(String token);

    Result register(LoginParam loginParam);
}
