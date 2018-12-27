package org.pan.module.charge;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.pan.Application;
import org.pan.ViewEvent;
import org.pan.bean.PhysicalCard;
import org.pan.module.MainStageView;
import org.pan.module.TimeOutViewManager;
import org.pan.repository.PhysicalCardRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
public class ChargeReadCardController implements Initializable {

    @Autowired
    public PhysicalCardRepository physicalCardRepository;
    @Autowired
    private TimeOutViewManager timeOutViewManager;

    public StackPane pleaseCard;
    public VBox root;
    public Label tip;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private StringBuilder cardBuilder = new StringBuilder(32);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executorService.scheduleWithFixedDelay(() -> {
            Platform.runLater(() -> {
                pleaseCard.setVisible(!pleaseCard.isVisible());
            });
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);

        root.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER && cardBuilder.length() > 0) {
                    String card = StringUtils.leftPad(cardBuilder.toString().toUpperCase(), 16, "0");
                    log.info("卡片内码:{}", card);
                    cardBuilder.delete(0, cardBuilder.length());
                    validCard(card);
                }
                log.debug("按键:{}", event.getText());
                if (event.getCode().isDigitKey() || event.getCode().isLetterKey()) {
                    cardBuilder.append(event.getText());
                }
            }
        });
    }

    @PostConstruct
    public void init(){
        timeOutViewManager.register(ChargeReadCardStageView.class, MainStageView.class, 60);
    }

    @Subscribe
    public void showEvent(ViewEvent viewEvent) {
        if (viewEvent.isPresent(ViewEvent.ViewEvenType.show, this)) {
            Platform.runLater(() -> tip.setText(""));
        }
    }

    private void validCard(String card) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                PhysicalCard physicalCard = physicalCardRepository.findPhysicalCardByPhysicalId(card);
                if (physicalCard == null) {
                    Platform.runLater(() -> tip.setText("无效卡"));
                    return;
                }

                Platform.runLater(() -> {
                    tip.setText("");
                    Application.switchView(ChargeReadCardStageView.class, ChargeMoneyStageView.class, physicalCard);
                });
            }
        });
    }

    @PreDestroy
    public void destroy(){
        executorService.shutdown();
    }

    public void exit(ActionEvent actionEvent) {
        Application.switchView(ChargeReadCardStageView.class, MainStageView.class, null);
    }
}
