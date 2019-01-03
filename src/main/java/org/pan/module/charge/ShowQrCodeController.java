package org.pan.module.charge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Cleanup;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.pan.Application;
import org.pan.bean.OrderInfo;
import org.pan.module.TimeOutViewManager;
import org.pan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage;

/**
 * @author panmingzhi
 */
@FXMLController
@Slf4j
@ConfigurationProperties(prefix = "dongyun")
public class ShowQrCodeController implements Initializable {
    public VBox root;
    public Label title;
    public Label tradeNO;
    public ImageView qrcode;
    public Button close;

    @Setter
    private String buildingId;
    @Setter
    private String serverUrl;
    @Setter
    private String appId;
    @Setter
    private String appSecret;

    public Image loadGifImage = new Image(this.getClass().getResourceAsStream("/image/load.gif"));
    public Image errorImage = new Image(this.getClass().getResourceAsStream("/image/error.png"));
    public Image successImage = new Image(this.getClass().getResourceAsStream("/image/ok.png"));
    private DateTimeFormatter yyyyMMddHHmmssSSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    @Autowired
    private RequestConfig requestConfig;
    @Autowired
    private OrderInfoRepository orderInfoRepository;
    @Autowired
    private CardUserRepository cardUserRepository;
    @Autowired
    private ConsumptionWalletRepository consumptionWalletRepository;
    @Autowired
    private ConsumptionRecordRepository consumptionRecordRepository;
    @Autowired
    private TimeOutViewManager timeOutViewManager;
    @Autowired
    private CommonService commonService;

    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private String orderId = "";
    private ScheduledFuture<?> scheduledFuture;

    @PostConstruct
    public void init() {
        log.info("buildingId:{}", buildingId);
        log.info("serverUrl:{}", serverUrl);
        log.info("appId:{}", appId);
        log.info("appSecret:{}", appSecret);


    }

    public void close(ActionEvent actionEvent) {
        root.getScene().getWindow().hide();

        Lighting effect = (Lighting) GUIState.getScene().getRoot().getEffect();
        effect.setDiffuseConstant(2.0);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(OrderInfo orderInfo) {
        log.info("订单：{}", orderInfo);

        Platform.runLater(()->{
            title.setText("请扫码支付" + orderInfo.getMoney() + "元");
        });
        if (Strings.isEmpty(buildingId) || Strings.isEmpty(serverUrl)) {
            updateState(errorImage, "未配置参数");
            return;
        }
        Platform.runLater(()->{close.setVisible(false);});
        updateState(loadGifImage, "正在生成云平台支付订单，请稍候 . . .");

        scheduledExecutor.submit(()->{
            try {
                orderInfoRepository.save(orderInfo);
            } catch (Exception e) {
                updateState(errorImage, "生成本地订单失败");
                return;
            }

            try {
                HttpPost httpPost = new HttpPost(serverUrl + "/order/rechargeMachine_create.action");
                httpPost.setConfig(requestConfig);
                httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");
                List<NameValuePair> parameters = new ArrayList<>();
                parameters.add(new BasicNameValuePair("bizNO", String.valueOf(orderInfo.getId())));
                Double money = orderInfo.getMoney() * 100;
                parameters.add(new BasicNameValuePair("orderMoney", String.valueOf(money.intValue())));
                parameters.add(new BasicNameValuePair("orderTitle", "自助机充值"));
                parameters.add(new BasicNameValuePair("orderDescribe", "用户编号:" + orderInfo.getUserNO()));
                parameters.add(new BasicNameValuePair("buildingId", buildingId));
                httpPost.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));
                log.info("请求生成云平台支付订单:{}", EntityUtils.toString(httpPost.getEntity()));
                CloseableHttpResponse response = httpclient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                log.info("请求生成云平台支付订单响应码:{}", statusCode);
                if (statusCode == HttpStatus.SC_OK) {
                    String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JSONObject jsonObject = JSON.parseObject(responseContent);
                    if (!Optional.ofNullable(jsonObject.getString("resultCode")).orElse("").equals("0000")) {
                        updateState(errorImage, "生成云平台支付订单失败");
                        return;
                    }
                    Optional<String> orderIdOptional = Optional.ofNullable(jsonObject.getJSONObject("data")).map(m -> m.getString("orderId"));
                    if (!orderIdOptional.isPresent()) {
                        updateState(errorImage, "生成云平台支付订单失败");
                        return;
                    }

                    orderInfo.setOrderId(orderIdOptional.get());
                    orderInfoRepository.save(orderInfo);

                    File file = new File("qrcode.png");
                    creatRrCode(serverUrl + "/pay_platform/order/ScanCode.jsp?orderId=" + orderIdOptional.get(), 500, 500, file);
                    @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
                    Image image = new Image(fileInputStream);
                    updateState(image, "请使用微信或支付宝扫码充值");

                    scheduledFuture = scheduledExecutor.scheduleWithFixedDelay(() -> {
                        checkOrderStatu(orderInfo.getOrderId());
                    }, 5000, 1000, TimeUnit.MILLISECONDS);
                    return;
                }

                throw new IOException("响应异常:" + statusCode);
            } catch (Exception e) {
                log.error("生成云平台订单失败", e);
                updateState(errorImage, "生成云平台支付订单失败");
            }finally {
                Platform.runLater(()->{close.setVisible(true);});
            }
        });

    }

    public void updateState(Image flagImage, String tip) {
        Platform.runLater(() -> {
            tradeNO.setText(tip);
            qrcode.setImage(flagImage);
        });
    }

    private void checkOrderStatu(String orderId) {
        try {
            String url = serverUrl + "/order/rechargeMachine_query.action?orderId=" + orderId;
            log.debug("正在检查订单状态:{}", url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSON.parseObject(responseContent);
                if (!Optional.ofNullable(jsonObject.getString("resultCode")).orElse("").equals("0000")) {
                    return;
                }
                if (!Optional.ofNullable(jsonObject.getJSONObject("data")).map(m -> m.getString("orderState")).orElse("").equals("1")) {
                    return;
                }

                Thread.sleep(10);
                commonService.saveAndUpdate(orderId);

                Platform.runLater(() -> {
                    close(null);
                    Application.switchView(ShowQrCodeStageView.class, ChargeSuccessStageView.class, null);
                });
            }
        } catch (Exception e) {
            log.error("检查订单状态失败", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void creatRrCode(String contents, int width, int height, File file) throws IOException {
        log.info("生成二维内容:{}", contents);
        String binary = null;
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedImage image = toBufferedImage(bitMatrix);
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            throw new IOException("生成二维码失败");
        }
    }

    public void secret(List<NameValuePair> nameValuePairList) {
//        nameValuePairList.sort(new Comparator<NameValuePair>() {
//            @Override
//            public int compare(NameValuePair o1, NameValuePair o2) {
//                return o1.getName().charAt(0) - o2.getName().charAt(0);
//            }
//        });
//        Optional<String> reduce = nameValuePairList.stream().sorted(new Comparator<NameValuePair>() {
//            @Override
//            public int compare(NameValuePair o1, NameValuePair o2) {
//                return o1.getName().compareTo(o2.getName());
//            }
//        }).map(m -> m.getName() + "=" + m.getValue()).reduce(new BinaryOperator<String>() {
//            @Override
//            public String apply(String s, String s2) {
//                return s + s2;
//            }
//        });
//        if(reduce.isPresent()){
//            String bytes = DigestUtils.md5Hex(reduce.get() + appSecret);
//            nameValuePairList.add(new BasicNameValuePair("sign",bytes));
//        }
    }
}
