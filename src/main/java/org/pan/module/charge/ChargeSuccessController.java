package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.pan.Application;
import org.pan.module.MainStageView;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ChargeSuccessController {
    public void exit(ActionEvent actionEvent) {
        Application.showView(MainStageView.class);
    }
}
