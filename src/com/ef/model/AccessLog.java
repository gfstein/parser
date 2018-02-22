package com.ef.model;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "ACCESS_LOG")
public class AccessLog implements EntityParser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_date")
    private Date accessDate;
    private String ip;
    private String request;
    private Integer status;

    @Column(name = "user_agent")
    private String userAgent;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccessLog && ((AccessLog) obj).getIp().equals(this.getIp());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getAccessDate() {
        return accessDate;
    }

    public void setAccessDate(Date accessDate) {
        this.accessDate = accessDate;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String method) {
        this.request = method;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String browser) {
        this.userAgent = browser;
    }
}
