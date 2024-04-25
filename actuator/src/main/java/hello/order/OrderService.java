package hello.order;

import java.util.concurrent.atomic.AtomicInteger;

public interface OrderService {
    void order();

    void cancel();

    AtomicInteger getStock(); // Atomic Integer -> 멀티 스레드 상황에서 안전하게 값 증가, 감소할 수 있음
}
