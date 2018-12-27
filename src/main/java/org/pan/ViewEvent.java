package org.pan;

import de.felixroske.jfxsupport.AbstractFxmlView;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author panmingzhi
 */
@Data
@AllArgsConstructor
public class ViewEvent {
    private ViewEvenType viewEvenType;
    private AbstractFxmlView abstractFxmlView;
    private Object object;

    public boolean isPresent(ViewEvenType viewEvenType, Object object) {
        return this.viewEvenType == viewEvenType && this.object == object;
    }

    public enum ViewEvenType {
        show, hide
    }
}
