/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface DerivativeVisitor<S, T> {

  T visit(Derivative derivative, S data);

  T visit(Derivative derivative);

  T visitEquityFuture(EquityFuture equityFuture, S data);

  T visitEquityFuture(EquityFuture equityFuture);

  T visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture, S data);

  T visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture);

  T visitVarianceSwap(VarianceSwap derivative, S data);

  T visitVarianceSwap(VarianceSwap derivative);

  T visitEquityIndexOption(EquityIndexOption equityIndexOption, S data);

  T visitEquityIndexOption(EquityIndexOption equityIndexOption);
}
