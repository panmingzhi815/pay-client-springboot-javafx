package org.pan.restclient;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author panmingzhi
 */

@Component
@ConfigurationProperties(prefix = "dongyun")
@Setter
@Slf4j
public class MqttConnector {

    private String mqttServer;
    private String mqttUserName;
    private String mqttPassword;
    private String deviceName;

    private String version = "1.0";
    private MqttClient mqttClient;

    private MemoryPersistence persistence = new MemoryPersistence();

    @PostConstruct
    public void init() {

        connectMqtt();

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
            String clientId = Optional.ofNullable(mqttClient).map(m -> m.getClientId()).orElse("");
            Boolean isConnected = Optional.ofNullable(mqttClient).map(m -> m.isConnected()).orElse(false);
            log.info("正在检查 mqtt {} 状态 {} ",clientId,isConnected);
            if(!isConnected){
                connectMqtt();
            }
        },10000,30000, TimeUnit.MILLISECONDS);
    }

    private void connectMqtt() {
        try {
            log.info("连接mqtt服务:{}",mqttServer);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
            // 创建客户端
            mqttClient = new MqttClient(mqttServer, "payClient-v" + version + "-" + deviceName + "-" + time, persistence);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    log.warn("mqtt 连接断开");
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    } catch (Exception e) {
                    }
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
            MqttConnectOptions connOpts = getMqttConnectOptions();
            // 建立连接
            mqttClient.connect(connOpts);
            log.info("连接mqtt服务器成功:{}",mqttServer);
        } catch (Exception me) {
            try {
                mqttClient.close();
            } catch (Exception e) {

            }
            log.error("连接mqtt服务器失败:{}",mqttServer);
        }
    }

    private MqttConnectOptions getMqttConnectOptions() {
        // 创建链接参数
        MqttConnectOptions connOpts = new MqttConnectOptions();
        // 在重新启动和重新连接时记住状态
        connOpts.setCleanSession(false);
        connOpts.setConnectionTimeout(10);
        connOpts.setKeepAliveInterval(20);
        // 设置连接的用户名
        connOpts.setUserName(mqttUserName);
        connOpts.setPassword(mqttPassword.toCharArray());
        return connOpts;
    }

    @PreDestroy
    public void destroy() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
                log.error("断开mqtt服务成功");
            } catch (MqttException e) {
                log.error("断开mqtt服务失败");
            }
        }
    }
}
