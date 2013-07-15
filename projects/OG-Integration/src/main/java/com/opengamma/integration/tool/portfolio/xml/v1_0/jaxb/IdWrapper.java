/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.opengamma.id.ExternalId;

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

  public ExternalId toExternalId() {
    return _externalId.toExternalId();
  }
}
