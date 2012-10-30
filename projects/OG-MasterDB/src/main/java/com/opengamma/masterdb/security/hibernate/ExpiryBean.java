/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Hibernate bean for storing an Expiry.
 */
public class ExpiryBean {

  // No id as it's part of the other securities
  private ZonedDateTimeBean _expiry;
  private ExpiryAccuracy _accuracy;

  public ZonedDateTimeBean getExpiry() {
    return _expiry;
  }

  public void setExpiry(final ZonedDateTimeBean expiry) {
    _expiry = expiry;
  }

  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }

  public void setAccuracy(final ExpiryAccuracy accuracy) {
    _accuracy = accuracy;
  }

}
