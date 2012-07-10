/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import javax.time.calendar.Period;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.time.Tenor;

/**
 *
 */
/* package */ class TenorFormatter implements Formatter<Tenor> {

  @Override
  public Period formatForDisplay(Tenor tenor, ValueSpecification valueSpec) {
    return tenor.getPeriod();
  }

  @Override
  public Period formatForExpandedDisplay(Tenor tenor, ValueSpecification valueSpec) {
    return tenor.getPeriod();
  }

  @Override
  public Object formatForHistory(Tenor history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.TENOR;
  }
}
