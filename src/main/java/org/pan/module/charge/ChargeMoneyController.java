package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pan.Application;
import org.pan.bean.ConsumptionWallet;
import org.pan.bean.OrderInfo;
import org.pan.bean.PhysicalCard;
import org.pan.module.MainStageView;
import org.pan.module.TimeOutViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
@ConfigurationProperties(prefix = "dongyun")
public class ChargeMoneyController implements Initializable {
    public Label userIdentifier;
    public Label userName;
    public Label cardId;
    public Label money;
    public FlowPane buttons;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Autowired
    private TimeOutViewManager timeOutViewManager;

    @Setter
    private List<Float> moneys;

    public void exit(ActionEvent actionEvent) {
        Application.showView(MainStageView.class);
    }

    public void money(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        String money = (String) node.getUserData();
        log.debug("选择充值金额:{}", money);

        OrderInfo orderInfo = OrderInfo.builder()
                .money(Double.valueOf(money))
                .userNO(userIdentifier.getText().trim())
                .userCard(cardId.getText().trim())
                .userName(userName.getText().trim())
                .build();

        timeOutViewManager.setCurrentLeft(-1);
        Application.showView(ShowQrCodeStageView.class, Modality.WINDOW_MODAL,orderInfo);
        timeOutViewManager.setCurrentLeft(60);
    }

    @PostConstruct
    public void init() {
        timeOutViewManager.register(ChargeMoneyStageView.class, MainStageView.class, 60);
        log.info("moneys:{}", moneys);
    }

    @Subscribe
    public void onMessageEvent(PhysicalCard event) throws InterruptedException {
        log.info("卡片信息：{}", event);
        executorService.submit(()->{
            List<ConsumptionWallet> consumptionWallets = Optional.ofNullable(event.getCardUser()).map(m -> m.getWalletList()).orElse(Arrays.asList());
            Double leftMoney = consumptionWallets.stream().filter(f -> f.getName().equals("充值")).findFirst().map(m -> m.getLeftMoney()).orElse(0D);
            Platform.runLater(() -> {
                userIdentifier.setText(event.getCardUser().getIdentifier());
                userName.setText(event.getCardUser().getName());
                cardId.setText(event.getPhysicalId());
                money.setText(String.valueOf(leftMoney));
            });
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(moneys == null) {
            moneys = Arrays.asList(50F,100F,200F,300F,400F,500F);
        }
        for (int i = 0; i < moneys.size() && i < 6; i++) {
            Float aFloat = moneys.get(i);
            Button button = new Button(aFloat.floatValue() + "元");
            button.setUserData(String.valueOf(aFloat));
            button.getStyleClass().add("btn");
            button.getStyleClass().add("btn-lg");
            button.getStyleClass().add("font-20");

            button.setEffect(new DropShadow());

            button.setPrefHeight(80);
            button.setPrefWidth(150);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    money(event);
                }
            });

            buttons.getChildren().add(button);
        }
    }
}
