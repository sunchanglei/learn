package learn.netty;

public interface RpcFutureListener {

    public void onResult(Object result);
    public void onException(Throwable throwable);

}
