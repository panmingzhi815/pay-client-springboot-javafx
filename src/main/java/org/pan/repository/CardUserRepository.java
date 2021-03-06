package org.pan.repository;

import org.pan.bean.CardUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author panmingzhi
 */
@Repository
public interface CardUserRepository extends CrudRepository<CardUser, Long> {

    public CardUser findByIdentifier(String identifier);
}
