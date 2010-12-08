/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.masterdb.security.hibernate.EnumBean;

public class CommodityFutureTypeBean extends EnumBean {
  
  protected CommodityFutureTypeBean() {
  }

  public CommodityFutureTypeBean(String commodityType) {
    super(commodityType);
  }
  
}
