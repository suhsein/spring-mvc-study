package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Open Session In View : false
 * OSIV 끄면 영속성 컨텍스트와 DB 커넥션이 유지되지 않음 => Lazy Loading 한 트랜잭션 내에서만 가능
 *
 * 컨트롤러에서 처리하던 쿼리를 쿼리 서비스로 분리하여, 트랜잭션 내에서 쿼리 할 수 있도록 함
 *
 * OSIV 는 실시간 트래픽이 큰 애플리케이션에서는 끄는 것을 추천. DB 커넥션이 모자랄 수 있기 때문에
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(o->new OrderDto(o))
                .collect(toList());
    }
}
