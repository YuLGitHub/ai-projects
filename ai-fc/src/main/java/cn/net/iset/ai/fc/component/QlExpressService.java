package cn.net.iset.ai.fc.component;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表达式引擎
 *
 */
@Slf4j
@Component
public class QlExpressService {

    private final ExpressRunner runner;

    public QlExpressService() {
        // 开启高精度计算
        runner = new ExpressRunner(true, false);
        // 是否忽略charset类型的数据，而识别为string，比如'a' -> "a"
        runner.setIgnoreConstChar(true);
        try {
            // 绝对值
            runner.addFunctionOfClassMethod("abs", Math.class, "abs",
                    new Class[]{double.class}, null);
            // 乘方
            runner.addFunctionOfClassMethod("pow", Math.class, "pow",
                    new Class[]{double.class, double.class}, null);
            // 开方
            runner.addFunctionOfClassMethod("sqrt", Math.class, "sqrt",
                    new Class[]{double.class}, null);
        } catch (Exception e) {
            log.error("表达式引擎加载失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 表达式计算
     *
     * @param express  表达式
     * @param variable 变量
     * @return
     * @throws Exception
     */
    public BigDecimal execute(String express, Map<String, Object> variable) throws Exception {
        DefaultContext<String, Object> expressContext = new DefaultContext<>();
        if (Objects.nonNull(variable)) {
            expressContext.putAll(variable);
        }
        Object ret = runner.execute(express, expressContext, null, false, true);
        return Optional.ofNullable(ret)
                .map(r -> new BigDecimal(r.toString()))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 数字表达式计算
     *
     * @param express  数字表达式
     * @return
     * @throws Exception
     */
    public BigDecimal execute(String express) throws Exception {
        Object ret = runner.execute(express, null, null, false, true);
        return Optional.ofNullable(ret)
                .map(r -> new BigDecimal(r.toString()))
                .orElse(BigDecimal.ZERO);
    }

}
