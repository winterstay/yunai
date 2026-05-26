package com.yunai.yunai.service;

import java.util.List;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yunai.yunai.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.yunai.yunai.model.dto.vo.ChatHistoryVO;
import com.yunai.yunai.model.entity.App;
import com.yunai.yunai.model.entity.ChatHistory;
import com.yunai.yunai.model.entity.User;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/winterstay">winterStay</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    void validChatHistory(ChatHistory chatHistory, boolean add);

    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    void saveUserMessage(Long appId, Long userId, String message);

    void saveAiMessage(Long appId, Long userId, String message);

    void saveAiErrorMessage(Long appId, Long userId, String errorMessage);

    void removeByAppId(Long appId);

    void checkAppReadAuth(App app, User loginUser);
}
