/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.threeten.bp.LocalDate;

/**
 * Represents a group of elements to indicate an fx-related expiry.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FxExpiry {

  @XmlElement(name = "expiryDate", required = true)
  private LocalDate _expiryDate;

  @XmlElement(name = "expiryCutoff", required = true)
  private String _expiryCutoff;

  @XmlElementWrapper(name = "expiryCalendars", required = true)
  @XmlElement(name = "calendar")
  private Set<Calendar> _expiryCalendars;

  public LocalDate getExpiryDate() {
    return _expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    _expiryDate = expiryDate;
  }

  public String getExpiryCutoff() {
    return _expiryCutoff;
  }

  public void setExpiryCutoff(String expiryCutoff) {
    _expiryCutoff = expiryCutoff;
  }

  public Set<Calendar> getExpiryCalendars() {
    return _expiryCalendars;
  }

  public void setExpiryCalendars(Set<Calendar> expiryCalendars) {
    _expiryCalendars = expiryCalendars;
  }
}
