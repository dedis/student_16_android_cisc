package com.epfl.dedis.api;

public interface Request {
    // TODO: Replace with better paths (more verbose)
    String CONFIG_UPDATE = "cu";
    String PROPOSE_SEND = "ps";
    String PROPOSE_UPDATE = "pu";
    String PROPOSE_VOTE = "pv";

    void callback(String result);
    void callbackError(int error);
}