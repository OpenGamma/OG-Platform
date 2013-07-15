/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlEnumValue;

public enum SettlementType {
  @XmlEnumValue(value = "Physical")
  PHYSICAL,
  @XmlEnumValue(value = "CashSettled")
  CASH_SETTLED
}
