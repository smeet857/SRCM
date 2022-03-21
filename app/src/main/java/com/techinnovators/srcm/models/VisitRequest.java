package com.techinnovators.srcm.models;

public class VisitRequest {
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVisit_checkin() {
        return visit_checkin;
    }

    public void setVisit_checkin(String visit_checkin) {
        this.visit_checkin = visit_checkin;
    }

    String visit_checkin;
}
