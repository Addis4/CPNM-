package com.dtdl.CPNM.controller;

import com.dtdl.CPNM.dto.TelekomRequest;
import com.dtdl.CPNM.service.TelekomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
@RestController
@RequestMapping("/telekom")
public class TelekomController {

    private static final Logger logger = LoggerFactory.getLogger(TelekomController.class);

    @Autowired
    private TelekomService telekomService;

    @PostMapping(value = "/disable/numbers",consumes = {"application/json"})
    public void disableNumbers(@RequestBody @Valid TelekomRequest telekomRequest){
        logger.info("Received Request to Disable number");
        telekomService.disableNumbers(telekomRequest.getNumbers(), telekomRequest.getSwapNumber());
    }

    @PutMapping(value = "/retrieve/numbers")
    public void enableNumbers(@RequestBody @Valid TelekomRequest telekomRequest){
        logger.info("Received Request to Retrieve number");
        telekomService.enableNumbers(telekomRequest.getNumbers());
    }


}
