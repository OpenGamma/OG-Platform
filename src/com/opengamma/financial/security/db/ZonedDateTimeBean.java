/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

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
