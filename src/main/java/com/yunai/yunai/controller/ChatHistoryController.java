package com.yunai.yunai.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mybatisflex.core.paginate.Page;
import com.yunai.yunai.annotation.AuthCheck;
import com.yunai.yunai.common.BaseResponse;
import com.yunai.yunai.common.ResultUtils;
import com.yunai.yunai.constant.UserConstant;
import com.yunai.yunai.exception.ErrorCode;
import com.yunai.yunai.exception.ThrowUtils;
import com.yunai.yunai.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.yunai.yunai.model.dto.vo.ChatHistoryVO;
import com.yunai.yunai.model.entity.App;
import com.yunai.yunai.model.entity.ChatHistory;
import com.yunai.yunai.model.entity.User;
import com.yunai.yunai.service.AppService;
import com.yunai.yunai.service.ChatHistoryService;
import com.yunai.yunai.service.UserService;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    private static final int USER_DEFAULT_PAGE_SIZE = 10;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 分页查看某个应用的对话历史（仅应用创建者和管理员可见）
     */
    @PostMapping("/app/list/page/vo")
    public BaseResponse<Page<ChatHistoryVO>> listAppChatHistoryVOByPage(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = chatHistoryQueryRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        User loginUser = userService.getLoginUser(request);
        App app = appService.getAppById(appId);
        chatHistoryService.checkAppReadAuth(app, loginUser);
        long pageNum = chatHistoryQueryRequest.getPageNum() <= 0 ? 1 : chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize() <= 0 ? USER_DEFAULT_PAGE_SIZE
                : chatHistoryQueryRequest.getPageSize();
        Page<ChatHistory> historyPage = chatHistoryService.page(Page.of(pageNum, pageSize),
                chatHistoryService.getQueryWrapper(chatHistoryQueryRequest));
        List<ChatHistory> records = historyPage.getRecords();
        Collections.reverse(records);
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, historyPage.getTotalRow());
        chatHistoryVOPage.setRecords(chatHistoryService.getChatHistoryVOList(records));
        return ResultUtils.success(chatHistoryVOPage);
    }

    /**
     * 获取某个应用最新 10 条对话历史
     */
    @GetMapping("/app/recent/vo")
    public BaseResponse<List<ChatHistoryVO>> listRecentChatHistoryVOByAppId(@RequestParam Long appId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getAppById(appId);
        chatHistoryService.checkAppReadAuth(app, loginUser);
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(USER_DEFAULT_PAGE_SIZE);
        Page<ChatHistory> historyPage = chatHistoryService.page(Page.of(1, USER_DEFAULT_PAGE_SIZE),
                chatHistoryService.getQueryWrapper(queryRequest));
        List<ChatHistory> records = historyPage.getRecords();
        Collections.reverse(records);
        return ResultUtils.success(chatHistoryService.getChatHistoryVOList(records));
    }

    /**
     * 管理员分页查看所有应用的对话历史（按时间倒序）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> adminListChatHistoryVOByPage(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        Page<ChatHistory> historyPage = chatHistoryService.page(Page.of(pageNum, pageSize),
                chatHistoryService.getQueryWrapper(chatHistoryQueryRequest));
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, historyPage.getTotalRow());
        chatHistoryVOPage.setRecords(chatHistoryService.getChatHistoryVOList(historyPage.getRecords()));
        return ResultUtils.success(chatHistoryVOPage);
    }

    /**
     * 管理员根据 id 查看对话历史详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ChatHistoryVO> getChatHistoryVOByIdByAdmin(@RequestParam String id) {
        ThrowUtils.throwIf(StrUtil.isBlank(id), ErrorCode.PARAMS_ERROR);
        ChatHistory chatHistory = chatHistoryService.getById(Long.parseLong(id));
        ThrowUtils.throwIf(chatHistory == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(chatHistoryService.getChatHistoryVO(chatHistory));
    }
}
