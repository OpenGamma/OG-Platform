/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import org.threeten.bp.YearMonth;

import com.opengamma.util.jaxb.AbstractDateAdapter;

public class DerivativeExpiryDateAdapter extends AbstractDateAdapter<YearMonth> {

  @Override
  protected String getDatePattern() {
    return "MMMyy";
  }

  @Override
  public YearMonth unmarshal(String v) throws Exception {
    return _formatter.parse(v, YearMonth.class);
  }
}
