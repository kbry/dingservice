package com.sangfor.common;

public class ConsoleCrypto {
    private String pubKey;

    private String pubKeyExp;

    private String antiReplayRand;

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String publicKey) {
        this.pubKey = publicKey;
    }

    public String getPubKeyExp() {
        return pubKeyExp;
    }

    public void setPubKeyExp(String pubKeyExp) {
        this.pubKeyExp = pubKeyExp;
    }

    public String getAntiReplayRand() {
        return antiReplayRand;
    }

    public void setAntiReplayRand(String antiReplayRand) {
        this.antiReplayRand = antiReplayRand;
    }
}
