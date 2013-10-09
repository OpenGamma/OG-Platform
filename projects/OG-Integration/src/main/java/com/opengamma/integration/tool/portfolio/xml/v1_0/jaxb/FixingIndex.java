/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * XML access to the fixing index.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FixingIndex {

  /**
   * The rate type.
   */
  public enum RateType {
    /**
     * OIS.
     */
    OIS,
    /**
     * CMS.
     */
    CMS,
    /**
     * IBOR.
     */
    IBOR,
  }

  @XmlElement(name = "id")
  private ExtId index;  // CSIGNORE

  @XmlElement(name = "rateType")
  private RateType _rateType;

  public ExtId getIndex() {
    return index;
  }

  public void setIndex(ExtId index) {
    this.index = index;
  }

  public RateType getRateType() {
    return _rateType;
  }

  public void setRateType(RateType rateType) {
    _rateType = rateType;
  }
}
