package com.sze.findmeamechanic.models;

public class NearbyJob {
    private String name, desc, date, id, type;

    public NearbyJob() {
    }

    public NearbyJob(String name, String desc, String date, String id, String type) {
        this.name = name;
        this.desc = desc;
        this.date = date;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
