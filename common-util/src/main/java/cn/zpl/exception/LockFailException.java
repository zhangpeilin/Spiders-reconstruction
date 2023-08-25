package cn.zpl.exception;

public class LockFailException extends RuntimeException {

    public LockFailException(String message) {
        super(message);
    }
}
