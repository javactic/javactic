package com.github.javactic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

public class ErrorMessage {
    
    private static final String REPLACE = Pattern.quote("{}");

    private final String shortMsg;
    private final String longMsg;
    
    private ErrorMessage(String shortMsg, String longMsg) {
        this.shortMsg = shortMsg;
        this.longMsg = longMsg;
    }

    public static ErrorMessage of(String shortMsg, String longMsg) {
        return new ErrorMessage(shortMsg, longMsg);
    }
    
    public static ErrorMessage with(String msg, Object... args) {
        String replaced = parse(msg, args);
        return ErrorMessage.of(replaced, replaced);
    }
    
    public static ErrorMessage fromThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return ErrorMessage.of(t.getMessage(), sw.toString());
    }

    public String getShortMsg() {
        return shortMsg;
    }
    
    public String getLongMsg() {
        return longMsg;
    }
    
    static String parse(String msg, Object... args) {
        for (Object arg : args) {
            msg = msg.replaceFirst(REPLACE, arg.toString());
        }
        return msg;
    }

}
