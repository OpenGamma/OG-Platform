package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Calendar {

  public enum Type {Bank}

  @XmlAttribute
  private Type type;

  @XmlElement(name = "id")
  private ExtId _externalId;

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public ExtId getId() {
    return _externalId;
  }

  public void setId(ExtId externalId) {
    _externalId = externalId;
  }
}
