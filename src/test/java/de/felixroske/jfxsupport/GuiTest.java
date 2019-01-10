package de.felixroske.jfxsupport;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pan.Application;
import org.pan.CustomSplash;
import org.pan.bean.OrderInfo;
import org.pan.module.MainStageView;
import org.pan.module.TimeOutViewManager;
import org.pan.module.charge.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuiTest {

    private CloseableHttpClient httpclient = HttpClients.createDefault();

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        new Thread(() -> Application.launch(Application.class, MainStageView.class, new CustomSplash(), new String[0])).start();
        while (GUIState.getStage() == null || !GUIState.getStage().isShowing()) {
            TimeUnit.SECONDS.sleep(3);
        }
    }

    @Test
    public void test() throws InterruptedException {
        MainStageView mainStageView = Application.context.getBean(MainStageView.class);
        ChargeReadCardStageView chargeReadCardStageView = Application.context.getBean(ChargeReadCardStageView.class);
        ChargeMoneyStageView chargeMoneyStageView = Application.context.getBean(ChargeMoneyStageView.class);
        ShowQrCodeStageView showQrCodeStageView = Application.context.getBean(ShowQrCodeStageView.class);
        ChargeSuccessStageView chargeSuccessStageView = Application.context.getBean(ChargeSuccessStageView.class);
        TimeOutViewManager bean = Application.context.getBean(TimeOutViewManager.class);

        while (true) {
            if (GUIState.getScene().getRoot() == mainStageView.getView()) {
                log.info("界面:{}", MainStageView.class);
                TimeUnit.SECONDS.sleep(3);

                Platform.runLater(() -> {
                    log.info("自动选择自助充值");
                    Node lookup = mainStageView.getView().lookup("#btn_charge");
                    MouseEvent event = new MouseEvent(MouseEvent.MOUSE_CLICKED,
                            0d, 0d, 0d, 0d, MouseButton.PRIMARY, 1,
                            true, true, true, true, true, true, true, true, true, true, null);
                    Event.fireEvent(lookup, event);
                });

                TimeUnit.SECONDS.sleep(3);

                continue;
            }


            if (GUIState.getScene().getRoot() == chargeReadCardStageView.getView()) {
                log.info("界面:{}", ChargeReadCardStageView.class);
                TimeUnit.SECONDS.sleep(3);

                Platform.runLater(() -> {
                    log.info("自动刷卡 123");
                    Node lookup = chargeReadCardStageView.getView().lookup("#root");
                    KeyEvent keyEvent1 = new KeyEvent(KeyEvent.KEY_PRESSED, "1", "1", KeyCode.DIGIT1, false, false, false, false);
                    KeyEvent keyEvent2 = new KeyEvent(KeyEvent.KEY_PRESSED, "2", "2", KeyCode.DIGIT2, false, false, false, false);
                    KeyEvent keyEvent3 = new KeyEvent(KeyEvent.KEY_PRESSED, "3", "3", KeyCode.DIGIT3, false, false, false, false);
                    KeyEvent keyEvent4 = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);
                    Event.fireEvent(lookup, keyEvent1);
                    Event.fireEvent(lookup, keyEvent2);
                    Event.fireEvent(lookup, keyEvent3);
                    Event.fireEvent(lookup, keyEvent4);
                });

                TimeUnit.SECONDS.sleep(3);

                continue;
            }

            if (GUIState.getScene().getRoot() == chargeSuccessStageView.getView()) {
                log.info("界面:{}", ChargeSuccessStageView.class);
                bean.setCurrentLeft(5);
                log.info("缴费成功");
                TimeUnit.SECONDS.sleep(6);
                continue;
            }

            if (Optional.ofNullable(showQrCodeStageView).map(m -> m.getView()).map(m -> m.getScene()).map(m -> m.getWindow()).map(m -> m.isShowing()).orElse(false)) {
                log.info("界面:{}", ShowQrCodeStageView.class);
                TimeUnit.SECONDS.sleep(3);

                ShowQrCodeController presenter = (ShowQrCodeController) showQrCodeStageView.getPresenter();
                OrderInfo currentOrder = presenter.getCurrentOrder();

                if (currentOrder == null) {
                    log.info("订单为空，取消缴费");
                    presenter.close(null);
                    TimeUnit.SECONDS.sleep(3);
                    Application.switchView(ShowQrCodeStageView.class, MainStageView.class, null);
                    TimeUnit.SECONDS.sleep(3);
                    continue;
                }

                while (!updateCurrentOrder(currentOrder)) {
                    TimeUnit.SECONDS.sleep(3);
                    continue;
                }

                continue;
            }

            if (GUIState.getScene().getRoot() == chargeMoneyStageView.getView()) {
                log.info("界面:{}", ChargeMoneyStageView.class);
                TimeUnit.SECONDS.sleep(3);

                Platform.runLater(() -> {
                    log.info("触发充值按钮");
                    FlowPane lookup = (FlowPane) chargeMoneyStageView.getView().lookup("#buttons");
                    MouseEvent event = new MouseEvent(MouseEvent.MOUSE_CLICKED,
                            0d, 0d, 0d, 0d, MouseButton.PRIMARY, 1,
                            true, true, true, true, true, true, true, true, true, true, null);
                    Event.fireEvent(lookup.getChildren().get(0), event);
                });

                TimeUnit.SECONDS.sleep(3);

                continue;
            }


        }

    }

    private boolean updateCurrentOrder(OrderInfo currentOrder) {
        String s = Optional.ofNullable(currentOrder).map(m -> m.getOrderId()).orElse("");
        if (s.length() < 10) {
            return false;
        }
        try {
            ShowQrCodeController bean = Application.context.getBean(ShowQrCodeController.class);
            RequestConfig requestConfig = Application.context.getBean(RequestConfig.class);

            HttpPost httpPost = new HttpPost(bean.getServerUrl() + "/order/rechargeMachine_testUpdate.action");
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");
            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("orderId", currentOrder.getOrderId()));
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));

            CloseableHttpResponse response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSON.parseObject(responseContent);
                if (Optional.ofNullable(jsonObject.getString("resultCode")).orElse("").equals("0000")) {
                    log.info("更新测试订单 {} 状态成功", currentOrder);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
        }
        return false;
    }

}