/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Calendar {

  public enum CalendarType {
    @XmlEnumValue(value = "bank")
    BANK,
    @XmlEnumValue(value = "currency")
    CURRENCY
  }

  @XmlAttribute(name = "calendarType")
  private CalendarType _calendarType;

  @XmlElement(name = "id")
  private ExtId _externalId;

  public CalendarType getCalendarType() {
    return _calendarType;
  }

  public void setCalendarType(CalendarType type) {
    this._calendarType = type;
  }

  public ExtId getId() {
    return _externalId;
  }

  public void setId(ExtId externalId) {
    _externalId = externalId;
  }
}
