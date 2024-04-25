package hello.controller;

import hello.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 예제의 단순성을 위해서
 * Get Mapping 을 사용함
 */
@Slf4j
@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/order")
    public String order() {
        log.info("order");
        orderService.order();
        return "Order";
    }

    @GetMapping("/cancel")
    public String cancel() {
        log.info("cancel");
        orderService.cancel();
        return "Cancel";
    }

    @GetMapping("/stock")
    public int stock(){
        log.info("stock");
        return orderService.getStock().get();
    }
}
