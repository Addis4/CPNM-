package com.dtdl.CPNM.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class TelekomRequest {

    @NotEmpty
    private List<String> numbers;

    private Boolean swapNumber;

    public List<String> getNumbers() {
        return numbers;
    }

    public Boolean getSwapNumber() {
        if(swapNumber==null)
            return false;
        return swapNumber;
    }
}
