package com.yunai.yunai.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yunai.yunai.mapper.UserMapper;
import com.yunai.yunai.model.entity.User;
import com.yunai.yunai.service.UserService;

import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
