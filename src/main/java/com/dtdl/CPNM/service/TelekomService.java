package com.dtdl.CPNM.service;

import java.util.List;

public interface TelekomService {

    public void disableNumbers(List<String> numbers,Boolean swapNumber);

    public void enableNumbers(List<String> numbers);
}
