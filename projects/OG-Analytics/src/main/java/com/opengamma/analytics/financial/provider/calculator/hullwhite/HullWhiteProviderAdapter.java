/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 * 
 * @param <RESULT_TYPE> The result-type for the provider.
 */
public class HullWhiteProviderAdapter<RESULT_TYPE> 
  extends InstrumentDerivativeVisitorSameMethodAdapter<HullWhiteOneFactorProviderInterface, RESULT_TYPE> {
    
  private final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> _visitor;

  public HullWhiteProviderAdapter(final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> visitor) {
    _visitor = visitor;
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative) {
    return derivative.accept(_visitor);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface data) {
    return derivative.accept(_visitor, data.getMulticurveProvider());
  }

}
