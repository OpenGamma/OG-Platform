/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlEnumValue;

public enum BuySell {
  @XmlEnumValue(value = "Buy")
  BUY,
  @XmlEnumValue(value = "Sell")
  SELL
}
