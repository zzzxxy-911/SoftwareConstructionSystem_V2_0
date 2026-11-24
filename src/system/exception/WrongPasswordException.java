package system.exception;

/**
 * 密码错误异常
 */
public class WrongPasswordException extends BusinessException {
    public WrongPasswordException(String message) {
        super(message);
    }
}