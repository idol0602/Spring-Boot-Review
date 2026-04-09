package nguyennhatquan.springbootreview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheSyncEvent {
    private String entityName;
    private String action;
    private String entityId;
}
