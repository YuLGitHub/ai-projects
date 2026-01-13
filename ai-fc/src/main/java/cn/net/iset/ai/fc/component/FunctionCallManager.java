package cn.net.iset.ai.fc.component;

import com.openai.models.chat.completions.ChatCompletionTool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工具类管理器
 *
 * @author yule
 * @version 1.0
 * @since 2026/1/9
 */
@Component
public class FunctionCallManager {


    /**
     * 方法提供
     */
    private final List<FunctionCallProvider> providers;


    @Autowired
    public FunctionCallManager(List<FunctionCallProvider> providers) {
        this.providers = providers;
    }


    /**
     * 通过名称获取工具
     *
     * @param funName
     * @return
     */
    public List<ChatCompletionTool> getTools(String funName) {
        List<ChatCompletionTool> tools = new ArrayList<>();
        for (FunctionCallProvider provider : providers) {
            if (provider.getFunctionName().equals(funName)) {
                tools.add(provider.buildTool());
            }
        }
        return tools;
    }

    /**
     * 获取所有工具方法
     *
     * @return
     */
    public List<ChatCompletionTool> getAllTools() {
        List<ChatCompletionTool> tools = new ArrayList<>();
        for (FunctionCallProvider provider : providers) {
            tools.add(provider.buildTool());
        }
        return tools;
    }

    /**
     * 根据方法名称执行结果
     *
     * @param functionName
     * @return
     */
    public String execute(String functionName, Map<String, Object> params) {
        for (FunctionCallProvider provider : providers) {
            if (provider.getFunctionName().equals(functionName)) {
                return provider.execute(params);
            }
        }
        return null;
    }
}
