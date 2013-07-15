/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jaxb;

import org.threeten.bp.LocalDate;

/**
 * XML adapter for LocalDate.
 */
public class LocalDateAdapter extends AbstractDateAdapter<LocalDate> {

  /**
   * Creates an instance.
   */
  public LocalDateAdapter() {
    super("yyyy-MM-dd");
  }

  @Override
  public LocalDate unmarshal(String v) throws Exception {
    return getFormatter().parse(v, LocalDate.class);
  }

}
