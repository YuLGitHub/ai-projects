package cn.net.iset.ai.skills;

import cn.net.iset.ai.skills.chat.LLMProvider;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiSkillsApplicationTests {

    @Resource
    private LLMProvider llmProvider;

    @Test
    void contextLoads() {
    }

    @Test
    void testCalcLLM() {
        String prompt = "你是一个专业的财务分析师，请根据提供的财务报表，确定财务报表中的[营业利润]科目的勾稽关系(包含本身的子项合计也包含这些科目之间的勾稽关系)是否准确。\n"
                + "特别注意：\n"
                + "结果以JSON的格式输出：\n"
                + "{\n"
                + "\"indicator\":\"配平的指标，取值范围：营业利润\",\n"
                + "\"formulas\":\"配平公式\",\n"
                + "\"expression\":\"配平公式带入值计算，例如xx+xx-xx\"\n"
                + "\"articulationSuccess\":\"true/false\",\n"
                + "\"originalValue\":\"原始值\",\n"
                + "\"articulationValue\": \"配平计算得到的值,调用计算工具进行计算\"\n"
                + "}\n"
                + "|项目|金额|\n"
                + "|一、营业收入|285,288,591.93|\n"
                + "|减:营业成本|179,241,108.68|\n"
                + "|税金及附加|876,265.06|\n"
                + "|销售费用|6,358,583.25|\n"
                + "|管理费用|7,625,280.14|\n"
                + "|研发费用|23,160,413.47|\n"
                + "|财务费用|-1,082,501.72|\n"
                + "|其中:利息费用|117,348.17|\n"
                + "|利息收入|1,441,272.00|\n"
                + "|加;其他收益|1,624,198.75|\n"
                + "|投资收益|907,566.29|\n"
                + "|公允价值变动收益|6,757.45|\n"
                + "|信用减值(损失)利得|306,290.13|\n"
                + "|资产减值损失|-471,372.60|\n"
                + "|二、营业利润|71,482,883.07|\n"
                + "|加:营业外收入|11,326,500.00|\n"
                + "|减:营业外支出|16,599.82|\n"
                + "|三、利润总额|82,792,783.25|\n"
                + "|减:所得税费用|10,001,317.64|\n"
                + "|四、净利润|72,791,465.61|\n"
                + "|(一)持续经营净利润|72,791,465.61|\n"
                + "|五、综合收益总额|72,791,465.61|";

        String result = llmProvider.chatCompletionsWithTool(prompt, "", null);
        System.out.printf("result:" + result);
    }
}
