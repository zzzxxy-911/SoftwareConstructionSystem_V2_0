package system.exception;

/**
 * 用户已存在异常
 */
public class UserExistsException extends BusinessException {
    public UserExistsException(String message) {
        super(message);
    }
}