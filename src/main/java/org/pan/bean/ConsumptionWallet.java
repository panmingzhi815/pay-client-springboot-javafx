package org.pan.bean;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "ConsumptionWallet")
@ToString(exclude = {"cardUser"})
public class ConsumptionWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //剩余金额
    private Double leftMoney;

    //钱包名称
    private String name;

    //用户
    @ManyToOne
    private CardUser cardUser;
}
