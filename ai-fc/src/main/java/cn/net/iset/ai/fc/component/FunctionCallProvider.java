package cn.net.iset.ai.fc.component;

import com.openai.models.chat.completions.ChatCompletionTool;
import java.util.Map;

/**
 * function call 提供者
 *
 * @author yule
 * @version 1.0
 * @since 2026/1/9
 */
public interface FunctionCallProvider {

    /**
     * 功能名称
     *
     * @return
     */
    String getFunctionName();

    /**
     * 构建工具
     *
     * @return
     */
    ChatCompletionTool buildTool();

    /**
     * 执行功能
     *
     * @param params 参数
     * @return
     */
    String execute(Map<String, Object> params);
}
