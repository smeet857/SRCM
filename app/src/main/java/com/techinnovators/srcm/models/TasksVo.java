package com.techinnovators.srcm.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TasksVo {
    @SerializedName("data")
    @Expose
    private List<Data> data = null;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public static class Data {
        @SerializedName("visit_state")
        @Expose
        private String visitState;
        @SerializedName("contact_person_mobile_no")
        @Expose
        private Object contactPersonMobileNo;
        @SerializedName("project_name")
        @Expose
        private String projectName;
        @SerializedName("visit_district")
        @Expose
        private String visitDistrict;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("visit_place")
        @Expose
        private String visitPlace;
        @SerializedName("visit_location")
        @Expose
        private String visitLocation;
        @SerializedName("visit_type")
        @Expose
        private String visitType;
        @SerializedName("visit_mode")
        @Expose
        private String visitMode;
        @SerializedName("visit_date")
        @Expose
        private String visitDate;
        @SerializedName("visit_checkin")
        @Expose
        private String visitCheckin;
        @SerializedName("project_type")
        @Expose
        private String projectType;
        @SerializedName("visit_checkout")
        @Expose
        private Object visitCheckout;
        @SerializedName("visit_taluka")
        @Expose
        private String visitTaluka;
        @SerializedName("visit_expiry_date")
        @Expose
        private String visitExpiryDate;
        @SerializedName("contact_person_name")
        @Expose
        private Object contactPersonName;

        public String getVisitState() {
            return visitState;
        }

        public void setVisitState(String visitState) {
            this.visitState = visitState;
        }

        public Object getContactPersonMobileNo() {
            return contactPersonMobileNo;
        }

        public void setContactPersonMobileNo(Object contactPersonMobileNo) {
            this.contactPersonMobileNo = contactPersonMobileNo;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getVisitDistrict() {
            return visitDistrict;
        }

        public void setVisitDistrict(String visitDistrict) {
            this.visitDistrict = visitDistrict;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVisitPlace() {
            return visitPlace;
        }

        public void setVisitPlace(String visitPlace) {
            this.visitPlace = visitPlace;
        }

        public String getVisitLocation() {
            return visitLocation;
        }

        public void setVisitLocation(String visitLocation) {
            this.visitLocation = visitLocation;
        }

        public String getVisitType() {
            return visitType;
        }

        public void setVisitType(String visitType) {
            this.visitType = visitType;
        }

        public String getVisitMode() {
            return visitMode;
        }

        public void setVisitMode(String visitMode) {
            this.visitMode = visitMode;
        }

        public String getVisitDate() {
            return visitDate;
        }

        public void setVisitDate(String visitDate) {
            this.visitDate = visitDate;
        }

        public String getVisitCheckin() {
            return visitCheckin;
        }

        public void setVisitCheckin(String visitCheckin) {
            this.visitCheckin = visitCheckin;
        }

        public String getProjectType() {
            return projectType;
        }

        public void setProjectType(String projectType) {
            this.projectType = projectType;
        }

        public Object getVisitCheckout() {
            return visitCheckout;
        }

        public void setVisitCheckout(Object visitCheckout) {
            this.visitCheckout = visitCheckout;
        }

        public String getVisitTaluka() {
            return visitTaluka;
        }

        public void setVisitTaluka(String visitTaluka) {
            this.visitTaluka = visitTaluka;
        }

        public String getVisitExpiryDate() {
            return visitExpiryDate;
        }

        public void setVisitExpiryDate(String visitExpiryDate) {
            this.visitExpiryDate = visitExpiryDate;
        }

        public Object getContactPersonName() {
            return contactPersonName;
        }

        public void setContactPersonName(Object contactPersonName) {
            this.contactPersonName = contactPersonName;
        }

    }
}
