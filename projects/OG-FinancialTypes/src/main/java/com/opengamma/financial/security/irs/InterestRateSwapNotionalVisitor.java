/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

import com.opengamma.financial.security.swap.NotionalVisitor;

/**
 * Visitor for the {@code InterestRateSwapNotional} subclasses.
 *
 * Mainly used in the presence of notional schedules.
 *
 * @param <DATA_TYPE> visitor method return type
 * @param <RESULT_TYPE> visitor input data type
 */
public interface InterestRateSwapNotionalVisitor<DATA_TYPE, RESULT_TYPE> extends NotionalVisitor<RESULT_TYPE> {

  RESULT_TYPE visitInterestRateSwapNotional(InterestRateSwapNotional notional, DATA_TYPE data);

  RESULT_TYPE visitInterestRateSwapNotional(InterestRateSwapNotional notional);

}
