/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import org.threeten.bp.LocalDate;

public class LocalDateAdapter extends AbstractDateAdapter<LocalDate> {

  @Override
  protected String getDatePattern() {
    return "yyyy-MM-dd";
  }

  @Override
  public LocalDate unmarshal(String v) throws Exception {
    return _formatter.parse(v, LocalDate.class);
  }
}
