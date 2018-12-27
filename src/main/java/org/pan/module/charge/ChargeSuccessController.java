package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import org.pan.Application;
import org.pan.module.MainStageView;
import org.pan.module.TimeOutViewManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ChargeSuccessController {

    @Autowired
    private TimeOutViewManager timeOutViewManager;

    public void exit(ActionEvent actionEvent) {
        Application.showView(MainStageView.class);
    }

    @PostConstruct
    public void init(){
        timeOutViewManager.register(ChargeSuccessStageView.class, MainStageView.class, 60);
    }
}
