package org.pan;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author panmingzhi
 */
@Data
@AllArgsConstructor
public class ViewEvent {
    private ViewEvenType viewEvenType;
    private Object object;

    public boolean isPresent(ViewEvenType viewEvenType, Object object) {
        return this.viewEvenType == viewEvenType && this.object == object;
    }

    public enum ViewEvenType {
        show, hide
    }
}
