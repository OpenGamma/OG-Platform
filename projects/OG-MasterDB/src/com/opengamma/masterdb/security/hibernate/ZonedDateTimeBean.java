/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Date;

/**
 * Hibernate bean for storing a zoned date-time.
 */
public class ZonedDateTimeBean {

  private Date _date;
  private String _zone;

  public Date getDate() {
    return _date;
  }

  public void setDate(final Date date) {
    _date = date;
  }

  public String getZone() {
    return _zone;
  }

  public void setZone(final String zone) {
    _zone = zone;
  }

}
