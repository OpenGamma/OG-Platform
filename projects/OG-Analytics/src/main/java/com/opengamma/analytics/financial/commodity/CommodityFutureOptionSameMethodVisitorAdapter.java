/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.CommodityFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;

/**
 * Visitor that delegates to the same method for all commodity future options.
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The type of the result
 */
public abstract class CommodityFutureOptionSameMethodVisitorAdapter<DATA_TYPE, RESULT_TYPE> extends InstrumentDerivativeVisitorAdapter<DATA_TYPE, RESULT_TYPE> {

  /**
   * @param option The commodity future option
   * @param data The data
   * @return A result of type RESULT_TYPE
   */
  public abstract RESULT_TYPE visit(CommodityFutureOption<?> option, DATA_TYPE data);

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption option, final DATA_TYPE data) {
    return visit(option, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption option, final DATA_TYPE data) {
    return visit(option, data);
  }
}
