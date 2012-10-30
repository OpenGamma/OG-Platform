/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibarnate bean for storing the market.
 */
public class MarketBean extends EnumBean {

  protected MarketBean() {
  }

  public MarketBean(String market) {
    super(market);
  }

}
