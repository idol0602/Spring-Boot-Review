package nguyennhatquan.springbootreview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nguyennhatquan.springbootreview.entity.OrderStatus;

@Data
public class UpdateOrderRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}

