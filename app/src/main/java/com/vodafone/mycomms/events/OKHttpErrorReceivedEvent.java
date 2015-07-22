package com.vodafone.mycomms.events;

public class OKHttpErrorReceivedEvent {
    private String errorMessage;

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

