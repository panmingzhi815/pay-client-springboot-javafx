package org.pan.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
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
import org.pan.bean.CardUser;
import org.pan.bean.ConsumptionRecord;
import org.pan.bean.ConsumptionWallet;
import org.pan.bean.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author panmingzhi
 */
@Service
@Slf4j
@ConfigurationProperties(prefix = "dongyun")
public class CommonService {

    @Autowired
    private OrderInfoRepository orderInfoRepository;
    @Autowired
    private CardUserRepository cardUserRepository;
    @Autowired
    private ConsumptionWalletRepository consumptionWalletRepository;
    @Autowired
    private ConsumptionRecordRepository consumptionRecordRepository;
    @Autowired
    private RequestConfig requestConfig;
    @Setter
    private String buildingId;
    @Setter
    private String serverUrl;
    @Setter
    private String appId;
    @Setter
    private String appSecret;
    @Setter
    private String deviceName;

    private CloseableHttpClient httpclient = HttpClients.createDefault();

    @Transactional(rollbackOn = Exception.class)
    public void saveAndUpdate(String orderId) throws IOException {
        OrderInfo byOrOrderId = orderInfoRepository.findByOrOrderId(orderId);
        byOrOrderId.setOrderSuccess(true);
        orderInfoRepository.save(byOrOrderId);

        CardUser byIdentifier = cardUserRepository.findByIdentifier(byOrOrderId.getUserNO());
        List<ConsumptionWallet> consumptionWallets = Optional.ofNullable(byIdentifier).map(m -> m.getWalletList()).orElse(Arrays.asList());
        Optional<ConsumptionWallet> first = consumptionWallets.stream().filter(f -> f.getName().equals("充值")).findFirst();
        if(!first.isPresent()){
            throw new IOException("未找到用户的充值钱包");
        }

        ConsumptionWallet consumptionWallet = first.get();
        consumptionWallet.setLeftMoney(add(consumptionWallet.getLeftMoney(),byOrOrderId.getMoney()));
        consumptionWalletRepository.save(consumptionWallet);

        ConsumptionRecord consumptionRecord = ConsumptionRecord.builder()
                .cardUserIdentifier(byOrOrderId.getUserNO())
                .consumptionRecordEnum("充值")
                .operatorMoney(byOrOrderId.getMoney())
                .databaseTime(new Date())
                .cardUserGroup(byIdentifier.getGroupCodeNameJoinStr())
                .deviceName(deviceName)
                .leftMoney(consumptionWallet.getLeftMoney())
                .build();
        consumptionRecordRepository.save(consumptionRecord);

        HttpPost httpPost = new HttpPost(serverUrl + "/order/rechargeMachine_update.action");
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("orderId",orderId));
        parameters.add(new BasicNameValuePair("bizState","1"));
        httpPost.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));
        log.info("查询订单支付状态:{}", EntityUtils.toString(httpPost.getEntity()));
        CloseableHttpResponse response = httpclient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("查询订单支付状态响应码:{}", statusCode);
        if(statusCode == HttpStatus.SC_OK){
            String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(responseContent);
            if(!Optional.ofNullable(jsonObject.getString("resultCode")).orElse("").equals("0000")){
                throw new IOException("更新云平台订单状态失败");
            }
        }
    }

    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

}
