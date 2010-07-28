/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Bean representation of Expiry
 */
public class ExpiryBean {

  // No id as it's part of the other securities
  private Date _date;
  private ExpiryAccuracy _accuracy;

  public Date getDate() {
    return _date;
  }

  public void setDate(final Date date) {
    _date = date;
  }

  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }

  public void setAccuracy(final ExpiryAccuracy accuracy) {
    _accuracy = accuracy;
  }

}
