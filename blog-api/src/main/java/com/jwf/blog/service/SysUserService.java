package com.jwf.blog.service;

import com.jwf.blog.dao.pojo.SysUser;
import com.jwf.blog.vo.Result;
import com.jwf.blog.vo.UserVo;

public interface SysUserService {

    SysUser findSysUserById(Long id);

    SysUser findUser(String account, String pwd);

    Result getUserInfoByToken(String token);

    SysUser findUserByAccount(String account);

    void save(SysUser sysUser);

    UserVo findUserVoById(Long authorId);
}
