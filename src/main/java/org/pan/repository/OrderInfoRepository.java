package org.pan.repository;

import org.pan.bean.OrderInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author panmingzhi
 */
@Repository
public interface OrderInfoRepository extends CrudRepository<OrderInfo, Long> {
}
