package nguyennhatquan.springbootreview.order.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nguyennhatquan.springbootreview.shared.entity.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}

