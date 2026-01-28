package com.medical.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.dto.LoginDTO;
import com.medical.entity.User;

/**
 * 用户Service接口
 *
 * @author AI Medical Team
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return token
     */
    String login(LoginDTO loginDTO);

    /**
     * 用户退出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    User getCurrentUser();

    /**
     * 分页查询用户列表
     *
     * @param page 分页参数
     * @param username 用户名(可选)
     * @param realName 真实姓名(可选)
     * @param role 角色(可选)
     * @return 用户列表
     */
    IPage<User> getUserList(Page<User> page, String username, String realName, Integer role);

}
