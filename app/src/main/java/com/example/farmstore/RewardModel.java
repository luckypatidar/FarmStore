package com.example.farmstore;

import com.google.firebase.Timestamp;

import java.util.Date;

public class RewardModel {
    private String type,upperLimit,lowerLimit,discORamt,coupenBody,coupenId;
    private Date timestamp;
    private Boolean alreadyUsed;

    public RewardModel(String coupenId,String type, String upperLimit, String lowerLimit, String discORamt, String coupenBody, Date timestamp,boolean alreadyUsed) {
        this.coupenId=coupenId;
        this.type = type;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.discORamt = discORamt;
        this.coupenBody = coupenBody;
        this.timestamp = timestamp;
        this.alreadyUsed =alreadyUsed;
    }

    public Boolean getAlreadyUsed() {
        return alreadyUsed;
    }

    public void setAlreadyUsed(Boolean alreadyUsed) {
        this.alreadyUsed = alreadyUsed;
    }

    public String getCoupenId() {
        return coupenId;
    }

    public void setCoupenId(String coupenId) {
        this.coupenId = coupenId;
    }


    public String getDiscORamt() {
        return discORamt;
    }

    public void setDiscORamt(String discORamt) {
        this.discORamt = discORamt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(String upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(String lowerLimit) {
        this.lowerLimit = lowerLimit;
    }


    public String getCoupenBody() {
        return coupenBody;
    }

    public void setCoupenBody(String coupenBody) {
        this.coupenBody = coupenBody;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}