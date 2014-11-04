/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *  @param <RESULT_TYPE> The result-type for the provider.
 */
public class BlackSmileCapInflationZeroCouponProviderAdapter<RESULT_TYPE> extends InstrumentDerivativeVisitorSameMethodAdapter<BlackSmileCapInflationZeroCouponProviderInterface, RESULT_TYPE> {

  private final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> _visitor;

  public BlackSmileCapInflationZeroCouponProviderAdapter(final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> visitor) {
    _visitor = visitor;
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative) {
    return derivative.accept(_visitor);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative, final BlackSmileCapInflationZeroCouponProviderInterface data) {
    return derivative.accept(_visitor, data.getMulticurveProvider());
  }
}
