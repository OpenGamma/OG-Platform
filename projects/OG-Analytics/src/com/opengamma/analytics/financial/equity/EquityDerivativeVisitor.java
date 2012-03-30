/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface EquityDerivativeVisitor<S, T> {

  T visit(EquityDerivative derivative, S data);

  T visit(EquityDerivative derivative);

  T visitEquityFuture(EquityFuture equityFuture, S data);

  T visitEquityFuture(EquityFuture equityFuture);

  T visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture, S data);

  T visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture);

  T visitVarianceSwap(VarianceSwap derivative, S data);

  T visitVarianceSwap(VarianceSwap derivative);

  /*
   * TODO:  
   *  a) Include here the initial list of equity derivatives that we wish to handle
   *  b) Build the functionality for them
   *  T visitEquitySingleStockDividendFuture(EquitySingleStockDividendFuture equitySingleStockDividendFuture, S data);
   *  (DONE) T visitVarianceSwap(VarianceSwap derivative, S data);
   */

}
