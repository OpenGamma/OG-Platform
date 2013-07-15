/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import org.threeten.bp.YearMonth;

import com.opengamma.util.jaxb.AbstractDateAdapter;

/**
 * Converts between a String in an XML file and a YearMonth.
 */
public class DerivativeExpiryDateAdapter extends AbstractDateAdapter<YearMonth> {

  /**
   * Creates an instance.
   */
  public DerivativeExpiryDateAdapter() {
    super("MMMyy");
  }

  @Override
  public YearMonth unmarshal(String v) throws Exception {
    return getFormatter().parse(v, YearMonth.class);
  }

}
