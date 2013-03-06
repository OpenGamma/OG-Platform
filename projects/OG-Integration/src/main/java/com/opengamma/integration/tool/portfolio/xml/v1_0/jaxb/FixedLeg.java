/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class FixedLeg extends SwapLeg {

  @XmlElement(name = "rate")
  private BigDecimal _rate;

  public BigDecimal getRate() {
    return _rate;
  }

  public void setRate(BigDecimal rate) {
    _rate = rate;
  }
}
