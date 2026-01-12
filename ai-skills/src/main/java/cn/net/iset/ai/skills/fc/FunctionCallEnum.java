package cn.net.iset.ai.skills.fc;

/**
 * fc枚举
 *
 * @author yule
 * @version 1.0
 * @since 2026/1/9
 */
public enum FunctionCallEnum {

    ARITHMETIC_OPERATIONS(1, "arithmeticOperations", "用于执行加减乘除等基本算术运算")

    ;

    /**
     * code
     */
    private int code;

    /**
     * 名称
     */
    private String name;

    /**
     * 方法描述
     */
    private String desc;


    FunctionCallEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
