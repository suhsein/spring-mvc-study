package hello.order.v4;

import hello.order.OrderService;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Timed("my.order")
@Slf4j
public class OrderServiceV4 implements OrderService {
    private AtomicInteger stock = new AtomicInteger(100);
    @Override
    public void order() {
        log.info("주문");
        stock.decrementAndGet();
    }
    @Override
    public void cancel() {
        log.info("취소");
        stock.incrementAndGet();
    }

    @Override
    public AtomicInteger getStock() {
        return stock;
    }
}
