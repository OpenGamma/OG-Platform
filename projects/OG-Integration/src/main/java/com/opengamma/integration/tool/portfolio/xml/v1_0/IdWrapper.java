package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class IdWrapper {

  @XmlElement(name = "id", required = true)
  private ExtId _externalId;

  public ExtId getExternalId() {
    return _externalId;
  }

  public void setExternalId(ExtId externalId) {
    _externalId = externalId;
  }
}
