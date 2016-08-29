package org.bksport.qa;

public class DistanceWeight {
  private String str = "";
  private String uri = "";
  private int    dis = 0;

  public DistanceWeight() {
    str = "";
    dis = 0;
  }

  public void setStr(String s) {
    this.str = s;
  }

  public String getStr() {
    return str;
  }

  public void setUri(String s) {
    this.uri = s;
  }

  public String getUri() {
    return uri;
  }

  public void setDis(int dis) {
    this.dis = dis;
  }

  public int getDis() {
    return dis;
  }
}
