package org.pan.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author panmingzhi
 */
@Entity
@Data
@Table(name = "OrderInfo")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //订单编号
    private String orderId;
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
    //订单成功状态
    private Boolean orderSuccess = false;
}
