package com.opengamma.masterdb.security.hibernate.future; /**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

public class StockFutureBean extends FutureSecurityBean {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitStockFutureType(this);
  }
}
