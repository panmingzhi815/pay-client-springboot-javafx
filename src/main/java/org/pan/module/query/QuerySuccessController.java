package org.pan.module.query;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;
import org.pan.Application;
import org.pan.ViewEvent;
import org.pan.bean.ConsumptionRecord;
import org.pan.bean.ConsumptionWallet;
import org.pan.bean.PhysicalCard;
import org.pan.module.MainStageView;
import org.pan.module.TimeOutViewManager;
import org.pan.module.charge.ChargeReadCardStageView;
import org.pan.repository.ConsumptionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class QuerySuccessController {

    public VBox recordBox;
    public Label money;
    public Label userIdentifier;
    public Label userName;
    public Label cardId;
    @Autowired
    private ConsumptionRecordRepository consumptionRecordRepository;

    @Autowired
    private TimeOutViewManager timeOutViewManager;

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

            Page<ConsumptionRecord> recordPage = consumptionRecordRepository.findConsumptionRecordsByCardUserIdentifier(event.getCardUser().getIdentifier(), PageRequest.of(0, 10, Sort.by(Sort.Order.desc("databaseTime"))));
            recordBox.getChildren().clear();
            for (ConsumptionRecord consumptionRecord : recordPage) {
                FlowPane flowPane = getFlowPane(consumptionRecord);
                recordBox.getChildren().add(flowPane);
            }
        });
    }

    @PostConstruct
    public void init(){
        timeOutViewManager.register(QuerySuccessStageView.class, MainStageView.class, 60);
    }

    private FlowPane getFlowPane(ConsumptionRecord consumptionRecord) {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyleClass().add("h3");
        flowPane.setStyle("-fx-background-color: #ffffff;");
        flowPane.setPadding(new Insets(5.0, 0, 5.0, 50.0));
        flowPane.setHgap(80.0);

        Label label = new Label(consumptionRecord.getConsumptionRecordEnum());
        ObservableList<String> styleClass = label.getStyleClass();
        styleClass.add("alert-success");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Label label2 = new Label(format.format(consumptionRecord.getDatabaseTime()));

        Label label3 = new Label(String.valueOf(consumptionRecord.getOperatorMoney()));
        label3.setAlignment(Pos.CENTER_RIGHT);
        label3.setPrefWidth(100.0);
        flowPane.getChildren().addAll(label, label2, label3);
        return flowPane;
    }

    public void exit(ActionEvent actionEvent) {
        Application.switchView(QuerySuccessStageView.class, MainStageView.class, null);
    }
}
