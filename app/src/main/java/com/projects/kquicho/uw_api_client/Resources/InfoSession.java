package com.projects.kquicho.uw_api_client.Resources;
public class InfoSession {

    private int id;
    private String employer = null;
    private String date = null;
    private String start_time = null;
    private String end_time = null;
    private String building_code = null;
    private String building_room = null;
    private String building_map_url = null;
    private String website = null;
    private String audience = null;
    private String programs = null;
    private String description = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getBuildingCode() {
        return building_code;
    }

    public void setBuildingRoom(String building_room) {
        this.building_room = building_room;
    }

    public String getBuildingRoom() {
        return building_room;
    }

    public void setBuildingMapUrl(String building_map_url) {
        this.building_map_url = building_map_url;
    }

    public String getBuildingMapUrl() {
        return building_map_url;
    }

    public void setBuildingCode(String building_code) {
        this.building_code = building_code;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getPrograms() {
        return programs;
    }

    public void setPrograms(String programs) {
        this.programs = programs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
