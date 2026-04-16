package nguyennhatquan.springbootreview.shared.repository;

import nguyennhatquan.springbootreview.shared.entity.Order;
import nguyennhatquan.springbootreview.shared.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndStatusAndIsDeletedFalse(Long userId, OrderStatus status, Pageable pageable);
    Optional<Order> findByIdAndIsDeletedFalse(Long id);
}
