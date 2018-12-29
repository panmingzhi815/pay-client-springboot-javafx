package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;
import org.pan.bean.OrderInfo;
import org.pan.module.TimeOutViewManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ShowQrCodeController implements Initializable {
    public VBox root;
    public Label title;
    @Autowired
    private TimeOutViewManager timeOutViewManager;

    public void close(ActionEvent actionEvent) {
        root.getScene().getWindow().hide();

        Lighting effect = (Lighting) GUIState.getScene().getRoot().getEffect();
        effect.setDiffuseConstant(2.0);
    }

    @Subscribe
    public void onMessageEvent(OrderInfo orderInfo) throws InterruptedException {
        log.info("订单：{}", orderInfo);

        Platform.runLater(() -> {
            title.setText("请扫码支付" + orderInfo.getMoney() + "元");
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
