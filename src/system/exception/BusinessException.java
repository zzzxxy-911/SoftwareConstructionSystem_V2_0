package system.exception;

/**
 * 自定义业务异常父类（继承运行时异常）
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}