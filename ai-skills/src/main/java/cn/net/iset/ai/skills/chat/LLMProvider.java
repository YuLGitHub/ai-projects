package cn.net.iset.ai.skills.chat;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import cn.net.iset.ai.skills.fc.FunctionCallManager;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletion.Choice;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionTool;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 模型调用
 *
 * @author yule
 * @version 1.0
 * @since 2026/1/9
 */
@Slf4j
@Component
public class LLMProvider {

    private static final String apiKey = "";

    private static final String reqUrl = "";

    private String model = "";

    @Resource
    private FunctionCallManager functionCallManager;

    public String chatCompletionsWithTool(String prompt, String content, String toolName) {

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(300))
                .baseUrl(reqUrl)
                .build();

        // 构建工具
        List<ChatCompletionTool> tools;
        if (StringUtils.isNotEmpty(toolName)) {
            tools = functionCallManager.getTools(toolName);
        } else {
            tools = functionCallManager.getAllTools();
        }
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage(prompt)
                .addUserMessage(content)
                .temperature(0.6)
                .tools(tools)
                .topP(0.95)
//                .maxCompletionTokens(32768) 设置输出最大tokens数，此处会占据输入输出总额度，导致输入额度变小
                .putAdditionalBodyProperty("top_k", JsonValue.from(20))
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        if (CollectionUtils.isNotEmpty(chatCompletion.choices())) {
            Choice choice = chatCompletion.choices().get(0);
            // 构建包含function call结果的对话历史
            List<ChatCompletionMessageParam> messages = new ArrayList<>();
            // 系统消息
            messages.add(ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder()
                            .role(JsonValue.from("system"))
                            .content(prompt)
                            .build()));

            // 用户消息
            messages.add(ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder()
                            .role(JsonValue.from("user"))
                            .content(content)
                            .build()
            ));
            ChatCompletion finalResponse;
            if (choice.message().toolCalls().isPresent()) {
                // 构建包含function call结果的对话历史
                List<ChatCompletionMessageToolCall> toolCalls = choice.message().toolCalls().get();
                // 不为空，则进行方法调用
                if (CollectionUtils.isNotEmpty(toolCalls)) {
                    // 助手消息（包含tool calls）
                    messages.add(ChatCompletionMessageParam.ofAssistant(
                            ChatCompletionAssistantMessageParam.builder()
                                    .role(JsonValue.from("assistant"))
                                    .content(choice.message().content().orElse(""))
                                    .toolCalls(toolCalls)
                                    .build()
                    ));
                    for (ChatCompletionMessageToolCall toolCall : toolCalls) {
                        if (toolCall.isFunction() && toolCall.function().isPresent()) {
                            ChatCompletionMessageFunctionToolCall call = toolCall.function()
                                    .get();
                            String id = call.id();
                            String name = call.function().name();
                            String arguments = call.function().arguments();
                            Map<String, Object> args = JSONUtil.toBean(arguments,
                                    new TypeReference<Map<String, Object>>() {
                                    }, false);
                            String result = functionCallManager.execute(name, args);
                            // 封装回message,最后再调用模型再返回结果
                            // Function call结果消息
                            messages.add(
                                    ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                                            .role(JsonValue.from("tool"))
                                            .toolCallId(id)
                                            .content(result)
                                            .build()));
                        }
                    }
                    // 再次调用模型获取最终响应
                    ChatCompletionCreateParams followUpParams = ChatCompletionCreateParams.builder()
                            .model(model)
                            .messages(messages)
                            .temperature(0.6)
                            .topP(0.95)
                            .putAdditionalBodyProperty("top_k", JsonValue.from(20))
                            .build();

                    finalResponse = client.chat().completions().create(followUpParams);
                    if (CollectionUtils.isNotEmpty(finalResponse.choices())) {
                        return finalResponse.choices().get(0).message().content().orElse("");
                    }
                }
            }
        }
        return chatCompletion.choices().get(0).message().content().orElse("");
    }


}
