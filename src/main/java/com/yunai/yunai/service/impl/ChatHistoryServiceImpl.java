package com.yunai.yunai.service.impl;

import java.time.LocalDateTime;
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
import com.yunai.yunai.exception.ThrowUtils;
import com.yunai.yunai.mapper.ChatHistoryMapper;
import com.yunai.yunai.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.yunai.yunai.model.dto.vo.AppVO;
import com.yunai.yunai.model.dto.vo.ChatHistoryVO;
import com.yunai.yunai.model.dto.vo.UserVO;
import com.yunai.yunai.model.entity.App;
import com.yunai.yunai.model.entity.ChatHistory;
import com.yunai.yunai.model.entity.User;
import com.yunai.yunai.model.enums.MessageTypeEnum;
import com.yunai.yunai.model.enums.UserRoleEnum;
import com.yunai.yunai.service.ChatHistoryService;
import com.yunai.yunai.service.AppService;
import com.yunai.yunai.service.UserService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public void validChatHistory(ChatHistory chatHistory, boolean add) {
        if (chatHistory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对话历史数据为空");
        }
        String message = chatHistory.getMessage();
        String messageType = chatHistory.getMessageType();
        Long appId = chatHistory.getAppId();
        Long userId = chatHistory.getUserId();
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
            ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 id 不能为空");
            ThrowUtils.throwIf(MessageTypeEnum.getEnumByValue(messageType) == null, ErrorCode.PARAMS_ERROR,
                    "消息类型非法");
        }
        if (StrUtil.isNotBlank(message) && message.length() > 20000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息过长");
        }
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        Long userId = chatHistory.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            chatHistoryVO.setUser(userVO);
        }
        Long appId = chatHistory.getAppId();
        if (appId != null) {
            App app = appService.getById(appId);
            AppVO appVO = appService.getAppVO(app);
            chatHistoryVO.setApp(appVO);
        }
        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        Set<Long> userIds = chatHistoryList.stream()
                .map(ChatHistory::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        Set<Long> appIds = chatHistoryList.stream()
                .map(ChatHistory::getAppId)
                .collect(Collectors.toSet());
        Map<Long, AppVO> appVOMap = appService.listByIds(appIds).stream()
                .collect(Collectors.toMap(App::getId, appService::getAppVO));
        return chatHistoryList.stream().map(chatHistory -> {
            ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
            BeanUtil.copyProperties(chatHistory, chatHistoryVO);
            chatHistoryVO.setUser(userVOMap.get(chatHistory.getUserId()));
            chatHistoryVO.setApp(appVOMap.get(chatHistory.getAppId()));
            return chatHistoryVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", chatHistoryQueryRequest.getId())
                .eq("appId", chatHistoryQueryRequest.getAppId())
                .eq("userId", chatHistoryQueryRequest.getUserId())
                .eq("messageType", chatHistoryQueryRequest.getMessageType())
                .like("message", chatHistoryQueryRequest.getMessage())
                .orderBy(toSafeSortField(sortField), "ascend".equals(sortOrder));
    }

    @Override
    public void saveUserMessage(Long appId, Long userId, String message) {
        saveChatHistory(appId, userId, message, MessageTypeEnum.USER);
    }

    @Override
    public void saveAiMessage(Long appId, Long userId, String message) {
        saveChatHistory(appId, userId, message, MessageTypeEnum.AI);
    }

    @Override
    public void saveAiErrorMessage(Long appId, Long userId, String errorMessage) {
        String finalMessage = StrUtil.blankToDefault(errorMessage, "AI 回复失败");
        saveChatHistory(appId, userId, finalMessage, MessageTypeEnum.AI);
    }

    @Override
    public void removeByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        List<ChatHistory> chatHistoryList = this.list(QueryWrapper.create().eq("appId", appId));
        if (CollUtil.isEmpty(chatHistoryList)) {
            return;
        }
        List<Long> ids = chatHistoryList.stream().map(ChatHistory::getId).toList();
        this.removeByIds(ids);
    }

    @Override
    public void checkAppReadAuth(App app, User loginUser) {
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        boolean isOwner = loginUser.getId().equals(app.getUserId());
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    private void saveChatHistory(Long appId, Long userId, String message, MessageTypeEnum messageTypeEnum) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(appId);
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType(messageTypeEnum.getValue());
        chatHistory.setCreateTime(LocalDateTime.now());
        chatHistory.setUpdateTime(LocalDateTime.now());
        validChatHistory(chatHistory, true);
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存对话历史失败");
    }

    private String toSafeSortField(String sortField) {
        if (StrUtil.isBlank(sortField)) {
            return "createTime";
        }
        return switch (sortField) {
            case "id", "message", "messageType", "appId", "userId", "createTime", "updateTime" -> sortField;
            default -> "createTime";
        };
    }

}
