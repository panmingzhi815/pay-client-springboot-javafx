package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;
import org.pan.Application;
import org.pan.bean.ConsumptionWallet;
import org.pan.bean.OrderInfo;
import org.pan.bean.PhysicalCard;
import org.pan.module.MainStageView;
import org.pan.module.TimeOutViewManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ChargeMoneyController {
    public Label userIdentifier;
    public Label userName;
    public Label cardId;
    public Label money;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Autowired
    private TimeOutViewManager timeOutViewManager;

    public void exit(ActionEvent actionEvent) {
        Application.showView(MainStageView.class);
    }

    public void money(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        String money = (String) node.getUserData();
        log.debug("选择充值金额:{}", money);

        OrderInfo orderInfo = OrderInfo.builder().money(Double.valueOf(money)).build();
        timeOutViewManager.setCurrentLeft(-1);
        Application.showView(ShowQrCodeStageView.class, Modality.WINDOW_MODAL);
        timeOutViewManager.setCurrentLeft(60);
    }

    @PostConstruct
    public void init() {
        timeOutViewManager.register(ChargeMoneyStageView.class, MainStageView.class, 60);
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
