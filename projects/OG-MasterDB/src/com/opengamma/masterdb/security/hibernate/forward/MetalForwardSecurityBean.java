package com.opengamma.masterdb.security.hibernate.forward;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

public class MetalForwardSecurityBean extends CommodityForwardSecurityBean {
  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitMetalForwardType(this);
  }
}
