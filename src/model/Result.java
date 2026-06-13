package model;

public class Result {
    private final boolean ok;
    private final String message;
    private final Object data;

    private Result(boolean ok, String message, Object data) {
        this.ok = ok;
        this.message = message;
        this.data = data;
    }

    public static Result ok()             { return new Result(true,  null, null); }
    public static Result ok(Object data)  { return new Result(true,  null, data); }
    public static Result fail(String msg) { return new Result(false, msg,  null); }

    public boolean isOk()       { return ok; }
    public String  getMessage() { return message; }
    public Object  getData()    { return data; }
}
