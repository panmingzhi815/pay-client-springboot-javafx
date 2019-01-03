package org.pan.restclient;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @PostConstruct
    public void init() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
            // 创建客户端
            mqttClient = new MqttClient(mqttServer, "payClient-v" + version + "-" + deviceName + "-" + time, persistence);
            // 创建链接参数
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // 在重新启动和重新连接时记住状态
            connOpts.setCleanSession(false);
            // 设置连接的用户名
            connOpts.setUserName(mqttUserName);
            connOpts.setPassword(mqttPassword.toCharArray());
            // 建立连接
            mqttClient.connect(connOpts);
            log.info("连接mqtt服务器成功:{}",mqttServer);
        } catch (Exception me) {
            log.error("连接mqtt服务器失败:{}",mqttServer);
        }
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
