package cn.net.iset.ai.skills.fc;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionTool;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 算术运算
 *
 * @author yule
 * @version 1.0
 * @since 2026/1/12
 */
@Slf4j
@Component
public class SimpleCalcFunctionCall implements FunctionCallProvider {


    @Resource
    private QlExpressService qlExpressService;

    /**
     * 功能调用枚举
     */
    private final FunctionCallEnum functionCallEnum = FunctionCallEnum.ARITHMETIC_OPERATIONS;

    @Override
    public String getFunctionName() {
        return functionCallEnum.getName();
    }

    @Override
    public ChatCompletionTool buildTool() {
        Map<String, JsonValue> parameters = new HashMap<>();
        // 类型
        parameters.put("type", JsonValue.from("object"));
        // 属性
        Map<String, JsonValue> properties = new HashMap<>();
        Map<String, JsonValue> expressionProp = new HashMap<>();
        expressionProp.put("type", JsonValue.from("string"));
        expressionProp.put("description", JsonValue.from("算术表达式"));
        properties.put("expression", JsonValue.from(expressionProp));
        parameters.put("properties", JsonValue.from(properties));
        parameters.put("required", JsonValue.from(Collections.singletonList("expression")));
        // 构建对象
        ChatCompletionFunctionTool functionTool = ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name(getFunctionName())
                        .description(functionCallEnum.getDesc())
                        .parameters(FunctionParameters.builder()
                                .additionalProperties(parameters)
                                .build())
                        .build())
                .build();
        return ChatCompletionTool.ofFunction(functionTool);
    }

    @Override
    public String execute(Map<String, Object> params) {
        String expression = (String) params.get("expression");
        return calc(expression).toString();
    }


    /**
     * 计算指标值
     *
     * @param formulas
     * @return
     */
    public static String getCalcFormulas(String formulas) {
        if (formulas.contains("=")) {
            String[] split = formulas.split("=");
            // 如果大于两个
            if (split.length > 1) {
                formulas = split[0].length() > split[1].length() ? split[0] : split[1];
            } else if (split.length > 0) {
                formulas = split[0];
            }
        }
        return formulas;

    }

    /**
     * 根据计算公式，进行计算
     *
     * @param formulas 带数字的计算推理
     * @return
     */
    public BigDecimal calc(String formulas) {
        // 先看有没有等号，有的话，用等号分出推理的公式
        try {
            // 对formulas进行计算
            formulas = getCalcFormulas(formulas);
            formulas = formatFormulas(formulas);
            return qlExpressService.execute(formulas);
        } catch (Exception e) {
            log.error("run qlExpress error, reason:{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建可执行的表达式
     *
     * @param formulas
     * @return
     */
    private static String formatFormulas(String formulas) {
        if (StringUtils.isEmpty(formulas)) {
            return formulas;
        }
        // 去掉空格和千分位
        return formulas.replace(",", "").replace(" ", "");
    }
}
