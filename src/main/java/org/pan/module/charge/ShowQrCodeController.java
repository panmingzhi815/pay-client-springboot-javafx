package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;
import org.pan.Application;
import org.pan.ViewEvent;
import org.pan.bean.ConsumptionWallet;
import org.pan.bean.OrderInfo;
import org.pan.bean.PhysicalCard;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ShowQrCodeController {
    public VBox root;
    public Label title;

    public void close(ActionEvent actionEvent) {

        Application.switchView(ShowQrCodeStageView.class,ChargeMoneyStageView.class,null);
    }

    @Subscribe
    public void onMessageEvent(OrderInfo orderInfo) throws InterruptedException {
        log.info("订单：{}", orderInfo);

        Platform.runLater(()->{
            title.setText("请扫码支付" + orderInfo.getMoney() + "元");
        });
    }
}
