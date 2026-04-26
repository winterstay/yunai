package com.yunai.yunai.service;

import com.mybatisflex.core.service.IService;
import com.yunai.yunai.model.entity.User;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
public interface UserService extends IService<User> {

    //用户注册
    long userRegister(String userAccount, String userPassword, String checkPassword);

    String getEncryptPassword(String userPassword);

}
