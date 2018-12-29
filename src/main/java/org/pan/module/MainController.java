package org.pan.module;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.pan.Application;
import org.pan.module.charge.ChargeReadCardStageView;
import org.pan.module.query.QueryReadCardStageView;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author panmingzhi
 */

@FXMLController
@Slf4j
public class MainController implements Initializable {

    @FXML
    public Label time;
    @FXML
    public Label date;
    @FXML
    public HBox btn_charge;
    @FXML
    public HBox btn_search;

    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        log.info("启动主页");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_charge.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Application.switchView(MainStageView.class, ChargeReadCardStageView.class, null);
            }
        });

        btn_search.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Application.switchView(MainStageView.class, QueryReadCardStageView.class, null);
            }
        });

        scheduledExecutor.scheduleWithFixedDelay(() -> {
            Platform.runLater(() -> {
                LocalDateTime now = LocalDateTime.now();
                time.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                date.setText(now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEE")));
            });
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
