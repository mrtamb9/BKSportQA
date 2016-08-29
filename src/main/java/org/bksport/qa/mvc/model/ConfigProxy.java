package org.bksport.qa.mvc.model;

import org.puremvc.java.patterns.proxy.Proxy;

public class ConfigProxy extends Proxy {

  private String agHost     = "172.245.22.156";
  private int    agPort     = 10035;
  private String agUsername = "congnh";
  private String agPassword = "a@2a@2";
  private String kimHost    = "localhost";
  private int    kimPort    = 1099;

  public ConfigProxy(String name) {
    super(name);
  }

  public String getAGHost() {
    return agHost;
  }

  public int getAGPort() {
    return agPort;
  }

  public String getAGUsername() {
    return agUsername;
  }

  public String getAGPassword() {
    return agPassword;
  }

  public String getKIMHost() {
    return kimHost;
  }

  public int getKIMPort() {
    return kimPort;
  }

  public void setAGHost(String agHost) {
    this.agHost = agHost;
  }

  public void setAGPort(int agPort) {
    this.agPort = agPort;
  }

  public void setAGUsername(String agUsername) {
    this.agUsername = agUsername;
  }

  public void setAGPassword(String agPassword) {
    this.agPassword = agPassword;
  }

  public void setKIMHost(String kimHost) {
    this.kimHost = kimHost;
  }

  public void setKIMPort(int kimPort) {
    this.kimPort = kimPort;
  }
}
