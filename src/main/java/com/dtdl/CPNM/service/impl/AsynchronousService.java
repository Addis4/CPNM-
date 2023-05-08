package com.dtdl.CPNM.service.impl;


import com.dtdl.CPNM.controller.TelekomController;
import com.dtdl.CPNM.kafka.KafkaProducer;
import com.dtdl.CPNM.dao.TelekomDao;
import com.dtdl.CPNM.model.Telekom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("asynchronousService")
@Async
public class AsynchronousService {

    private static final Logger logger = LoggerFactory.getLogger(TelekomController.class);

    @Autowired
    private TelekomDao telekomDao;
    @Autowired
    private KafkaProducer kafkaProducer;

    public void deleteNumbersById(String id){
        logger.info("Async call is made to disable services");
        Telekom telekom = telekomDao.findById(id).orElse(null);
        if(telekom !=null && telekom.getMobileNumbers()!=null && !telekom.getMobileNumbers().isEmpty()) {

            for(int i=0; i<telekom.getMobileNumbers().size(); i++) {
                kafkaProducer.send(telekom.getMobileNumbers().get(i));
            }
            telekomDao.deleteById(id);
            logger.info("Emptied database");

        }
    }
}
