package com.dtdl.CPNM.service.impl;

import com.dtdl.CPNM.controller.TelekomController;
import com.dtdl.CPNM.dao.TelekomDao;
import com.dtdl.CPNM.kafka.KafkaProducer;
import com.dtdl.CPNM.model.Telekom;
import com.dtdl.CPNM.service.TelekomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("telekomServiceImpl")
@Transactional
public class TelekomServiceImpl implements TelekomService {

    private static final Logger logger = LoggerFactory.getLogger(TelekomController.class);
    @Value("${encryption.sha.iv}")
    String IV;

    @Autowired
    private TelekomDao telekomDao;

    @Autowired
    private AsynchronousService asynchronousService;

    @Autowired
    private KafkaProducer kafkaProducer;

    Map<String,List<String>> telekomIdNumbersLookup=new ConcurrentHashMap<>();

    @Override
    public void disableNumbers(List<String> numbers,Boolean swapNumber) {
        List<String> encryptNumbers=hashWithSHA512(IV,numbers);
        if(swapNumber){
            logger.info("Received a Swapped Number request");
            for (String encryptNumber : encryptNumbers) {
                kafkaProducer.send(encryptNumber);
            }
            logger.info("encrypted numbers sent to kafka queue");
            return;
        }
        //save customer phone number for specific amount of time
        Telekom telekom =new Telekom(LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY:MM:dd::HH:mm:ss")),encryptNumbers);
        logger.info("Received a Blocking Number request");
        telekomDao.save(telekom);
        logger.info("Saved to database");

        //schedule to fix time acc to configuration
        telekomIdNumbersLookup.putIfAbsent(telekom.getId(),encryptNumbers);
        try{
            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    asynchronousService.deleteNumbersById(telekom.getId());
                }
            },35*1000L);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void enableNumbers(List<String> numbers) {
        List<String> encryptNumbers=hashWithSHA512(IV,numbers);
        String telekomId=null;
        for(Map.Entry<String,List<String>> telekomIdNumbersEntry:telekomIdNumbersLookup.entrySet()){
            for(String number:encryptNumbers){
                if (telekomIdNumbersEntry.getValue().contains(number)) {
                    telekomId = telekomIdNumbersEntry.getKey();
                    break;
                }
            }
            if(telekomId!=null)
                break;
        }
        if(telekomId!=null) {
            Telekom telekom = telekomDao.findById(telekomId).orElse(null);
            if (telekom != null) {
                List<String> mobileNumbers = telekom.getMobileNumbers();
                mobileNumbers.removeAll(encryptNumbers);
                telekom.setMobileNumbers(mobileNumbers);
                telekomDao.save(telekom);
                logger.info("Desired number removed from db");
            }
        }

    }

    public static List<String> hashWithSHA512(String iv, List<String> numbers) {
        List<String> encryptNumbers=new ArrayList<>();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            for(String number:numbers) {
                byte[] message = (iv + number).getBytes();
                byte[] hash = md.digest(message);
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }
                encryptNumbers.add(hexString.toString());
            }
            return encryptNumbers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
