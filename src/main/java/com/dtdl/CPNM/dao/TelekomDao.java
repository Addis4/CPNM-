package com.dtdl.CPNM.dao;

import com.dtdl.CPNM.model.Telekom;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TelekomDao extends MongoRepository<Telekom,String> {

}
