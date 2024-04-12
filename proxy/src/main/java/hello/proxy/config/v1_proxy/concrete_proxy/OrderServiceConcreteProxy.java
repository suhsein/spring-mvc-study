package hello.proxy.config.v1_proxy.concrete_proxy;

import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class OrderServiceConcreteProxy extends OrderServiceV2 {
    private final OrderServiceV2 target;
    private final LogTrace logTrace;

    /**
     * 의존 관계에 따라서 서비스 클래스의 생성자 매개변수로 리포지토리가 들어감
     * concrete 클래스 상속 시 생성자에서 부모 클래스에 대한 생성을 먼저 해야하므로
     * 제일 앞 줄에 super()가 호출되는데,
     * 이 경우에 항상 매개변수로 리포지토리가 필요하므로 기본 생성자를 만들거나 호출할 수 없음
     * 그러므로 그냥 super()의 매개변수로 null 을 넘김
     */
    public OrderServiceConcreteProxy(OrderServiceV2 target, LogTrace logTrace) {
        super(null); // 프록시만 사용하고 부모 객체 기능 사용 x. 그러므로 매개변수로 null 넘겨줌
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public void orderItem(String itemId) {
        TraceStatus status = null;
        try {
            status = logTrace.begin("OrderService.orderItem()");
            target.orderItem(itemId);
            logTrace.end(status);
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
