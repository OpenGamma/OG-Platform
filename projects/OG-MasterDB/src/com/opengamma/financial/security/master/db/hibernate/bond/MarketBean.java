/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.bond;

import com.opengamma.financial.security.master.db.hibernate.EnumBean;

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
