/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;

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

  RESULT_TYPE visitInterestRateNotional(InterestRateNotional notional, DATA_TYPE data);

  RESULT_TYPE visitSecurityNotional(SecurityNotional notional, DATA_TYPE period);

  RESULT_TYPE visitVarianceSwapNotional(VarianceSwapNotional notional, DATA_TYPE period);

  RESULT_TYPE visitCommodityNotional(CommodityNotional notional, DATA_TYPE period);

}
