package com.yunai.yunai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mybatisflex.core.paginate.Page;
import com.yunai.yunai.annotation.AuthCheck;
import com.yunai.yunai.common.BaseResponse;
import com.yunai.yunai.common.DeleteRequest;
import com.yunai.yunai.common.ResultUtils;
import com.yunai.yunai.constant.UserConstant;
import com.yunai.yunai.exception.ErrorCode;
import com.yunai.yunai.exception.ThrowUtils;
import com.yunai.yunai.model.dto.app.AppAddRequest;
import com.yunai.yunai.model.dto.app.AppAdminUpdateRequest;
import com.yunai.yunai.model.dto.app.AppQueryRequest;
import com.yunai.yunai.model.dto.app.AppUpdateRequest;
import com.yunai.yunai.model.dto.vo.AppVO;
import com.yunai.yunai.model.entity.App;
import com.yunai.yunai.model.entity.User;
import com.yunai.yunai.service.AppService;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    private static final int USER_MAX_PAGE_SIZE = 20;

    @Resource
    private AppService appService;

    /**
     * 用户创建应用
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        appService.validApp(app, true);
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 用户根据 id 修改自己的应用（仅支持修改名称）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        appService.getUserOwnedAppById(appUpdateRequest.getId(), loginUser);
        App app = new App();
        app.setId(appUpdateRequest.getId());
        app.setAppName(appUpdateRequest.getAppName());
        appService.validApp(app, false);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户根据 id 删除自己的应用
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        appService.getUserOwnedAppById(deleteRequest.getId(), loginUser);
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户根据 id 查看自己的应用详情
     */
    @GetMapping("/get")
    public BaseResponse<AppVO> getAppById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        App app = appService.getUserOwnedAppById(id, loginUser);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页查询自己的应用列表
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest appQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > USER_MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "每页最多 20 条");
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getUserAppQueryWrapper(appQueryRequest, loginUser.getId()));
        return ResultUtils.success(buildAppVOPage(appPage, pageNum, pageSize));
    }

    /**
     * 分页查询精选应用列表
     */
    @PostMapping("/good/list/page")
    public BaseResponse<Page<AppVO>> listGoodAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > USER_MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "每页最多 20 条");
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getFeaturedAppQueryWrapper(appQueryRequest));
        return ResultUtils.success(buildAppVOPage(appPage, pageNum, pageSize));
    }

    /**
     * 管理员根据 id 删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        appService.getAppById(deleteRequest.getId());
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员根据 id 更新任意应用
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        appService.getAppById(appAdminUpdateRequest.getId());
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        appService.validApp(app, false);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用列表
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> adminListAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), appService.getQueryWrapper(appQueryRequest));
        return ResultUtils.success(buildAppVOPage(appPage, pageNum, pageSize));
    }

    /**
     * 管理员根据 id 查看应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> adminGetAppById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getAppById(id);
        return ResultUtils.success(appService.getAppVO(app));
    }

    private Page<AppVO> buildAppVOPage(Page<App> appPage, long pageNum, long pageSize) {
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }
}
