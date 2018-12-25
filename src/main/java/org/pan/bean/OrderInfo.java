package org.pan.bean;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author panmingzhi
 */
@Entity
@Data
@Table(name = "OrderInfo")
@Builder(toBuilder = true)
public class OrderInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //业务编号
    private String bizNO;
    //交易编号
    private String tradeNO;
    //用户编号
    private String userNO;
    //用户姓名
    private String userName;
    //用户IC卡
    private String userCard;
    //订单金额
    private Double money;
    //创建时间
    private Date createTime;
    //终端编号
    private String deviceNO;
}
