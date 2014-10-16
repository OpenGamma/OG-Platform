/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *  @param <RESULT_TYPE> The result-type for the provider.
 */
public class BlackSmileCapInflationYearOnYearProviderAdapter<RESULT_TYPE> extends InstrumentDerivativeVisitorSameMethodAdapter<BlackSmileCapInflationYearOnYearProviderInterface, RESULT_TYPE> {

  private final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> _visitor;

  public BlackSmileCapInflationYearOnYearProviderAdapter(final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> visitor) {
    _visitor = visitor;
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative) {
    return derivative.accept(_visitor);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative, final BlackSmileCapInflationYearOnYearProviderInterface data) {
    return derivative.accept(_visitor, data.getMulticurveProvider());
  }

}
