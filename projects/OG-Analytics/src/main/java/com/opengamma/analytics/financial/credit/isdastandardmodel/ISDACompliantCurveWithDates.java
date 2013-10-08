/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.threeten.bp.LocalDate;

/**
 * 
 */
public interface ISDACompliantCurveWithDates {

  LocalDate getBaseDate();

  LocalDate getCurveDate(final int index);

  LocalDate[] getCurveDates();

  double getZeroRate(final LocalDate date);

}
