package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pan.Application;
import org.pan.bean.ConsumptionWallet;
import org.pan.bean.OrderInfo;
import org.pan.bean.PhysicalCard;
import org.pan.module.MainStageView;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ChargeMoneyController implements Initializable {
    public Label userIdentifier;
    public Label userName;
    public Label cardId;
    public Label money;

    @Autowired
    private ShowQrCodeStageView showQrCodeStageView;

    public void exit(ActionEvent actionEvent) {
        Application.showView(MainStageView.class);
    }

    public void money(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        String money = (String) node.getUserData();
        log.debug("选择充值金额:{}", money);


//        Stage stage = new Stage(StageStyle.TRANSPARENT);
//        Scene value = new Scene(showQrCodeStageView.getView());
//        value.setFill(null);
//        stage.setScene(value);
//        stage.initModality(Modality.WINDOW_MODAL);
//        stage.initOwner(GUIState.getStage());
//        stage.show();

        OrderInfo orderInfo = OrderInfo.builder().money(Double.valueOf(money)).build();

        Application.switchView(ChargeMoneyStageView.class,ShowQrCodeStageView.class,orderInfo);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Subscribe
    public void onMessageEvent(PhysicalCard event) throws InterruptedException {
        log.info("卡片信息：{}", event);
        Platform.runLater(() -> {
            userIdentifier.setText(event.getCardUser().getIdentifier());
            userName.setText(event.getCardUser().getName());
            cardId.setText(event.getPhysicalId());

            List<ConsumptionWallet> consumptionWallets = Optional.ofNullable(event.getCardUser()).map(m -> m.getWalletList()).orElse(Arrays.asList());
            Double leftMoney = consumptionWallets.stream().filter(f -> f.getName().equals("充值")).findFirst().map(m -> m.getLeftMoney()).orElse(0D);
            money.setText(String.valueOf(leftMoney));
        });
    }

}
