package com.yunai.yunai.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yunai.yunai.exception.BusinessException;
import com.yunai.yunai.exception.ErrorCode;
import com.yunai.yunai.mapper.AppMapper;
import com.yunai.yunai.model.dto.app.AppQueryRequest;
import com.yunai.yunai.model.dto.vo.AppVO;
import com.yunai.yunai.model.dto.vo.UserVO;
import com.yunai.yunai.model.entity.App;
import com.yunai.yunai.model.entity.User;
import com.yunai.yunai.service.AppService;
import com.yunai.yunai.service.UserService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private static final int FEATURED_PRIORITY_THRESHOLD = 0;

    @Resource
    private UserService userService;

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用数据为空");
        }
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        Integer priority = app.getPriority();
        if (add && StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        if (StrUtil.isNotBlank(appName) && appName.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (StrUtil.isNotBlank(app.getCover()) && app.getCover().length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用封面过长");
        }
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 20000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 过长");
        }
        if (priority != null && priority < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优先级不能小于 0");
        }
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public QueryWrapper getUserAppQueryWrapper(AppQueryRequest appQueryRequest, Long userId) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        return QueryWrapper.create()
                .eq("userId", userId)
                .like("appName", appQueryRequest.getAppName())
                .orderBy(toSafeSortField(appQueryRequest.getSortField()),
                        "ascend".equals(appQueryRequest.getSortOrder()));
    }

    @Override
    public QueryWrapper getFeaturedAppQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        return QueryWrapper.create()
                .like("appName", appQueryRequest.getAppName())
                .gt("priority", FEATURED_PRIORITY_THRESHOLD)
                .orderBy("priority", false)
                .orderBy("createTime", false);
    }

    @Override
    public App getAppById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return app;
    }

    @Override
    public App getUserOwnedAppById(Long id, User loginUser) {
        App app = getAppById(id);
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return app;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    private String toSafeSortField(String sortField) {
        if (StrUtil.isBlank(sortField)) {
            return "createTime";
        }
        return switch (sortField) {
            case "id", "appName", "cover", "initPrompt", "codeGenType", "deployKey", "deployedTime", "priority",
                    "userId", "editTime", "createTime", "updateTime" ->
                sortField;
            default -> "createTime";
        };
    }

}
