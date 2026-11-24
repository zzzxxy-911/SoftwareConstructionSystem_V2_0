package system.exception;

/**
 * 用户不存在异常
 */
public class UserNotExistsException extends BusinessException {
    public UserNotExistsException(String message) {
        super(message);
    }
}