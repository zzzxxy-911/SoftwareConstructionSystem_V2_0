package system.exception;

/**
 * 参数为空异常
 */
public class ParamEmptyException extends BusinessException {
    public ParamEmptyException(String message) {
        super(message);
    }
}