package org.pan.repository;

import org.pan.bean.CardUser;
import org.pan.bean.ConsumptionWallet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author panmingzhi
 */
@Repository
public interface ConsumptionWalletRepository extends CrudRepository<ConsumptionWallet, Long> {

}
