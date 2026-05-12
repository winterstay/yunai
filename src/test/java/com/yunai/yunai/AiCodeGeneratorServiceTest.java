package com.yunai.yunai;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.yunai.yunai.ai.AiCodeGeneratorService;
import com.yunai.yunai.ai.model.HtmlCodeResult;
import com.yunai.yunai.ai.model.MultiFileCodeResult;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个YunAi的工作记录小工具");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个YunAi的留言板");
        Assertions.assertNotNull(multiFileCode);
    }

}
