package com.yunai.yunai.service;

import java.util.List;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yunai.yunai.model.dto.app.AppQueryRequest;
import com.yunai.yunai.model.dto.vo.AppVO;
import com.yunai.yunai.model.entity.App;
import com.yunai.yunai.model.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
public interface AppService extends IService<App> {

    void validApp(App app, boolean add);

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    QueryWrapper getUserAppQueryWrapper(AppQueryRequest appQueryRequest, Long userId);

    QueryWrapper getFeaturedAppQueryWrapper(AppQueryRequest appQueryRequest);

    App getAppById(Long id);

    App getUserOwnedAppById(Long id, User loginUser);

    User getLoginUser(HttpServletRequest request);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);
}
