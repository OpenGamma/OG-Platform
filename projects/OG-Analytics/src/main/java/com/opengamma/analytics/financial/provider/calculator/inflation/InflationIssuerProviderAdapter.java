/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <RESULT_TYPE> The result-type for the provider.
 */
public class InflationIssuerProviderAdapter<RESULT_TYPE> 
  extends InstrumentDerivativeVisitorSameMethodAdapter<ParameterInflationIssuerProviderInterface, RESULT_TYPE> {
  
  private final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, RESULT_TYPE> _visitor;

  public InflationIssuerProviderAdapter(final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, RESULT_TYPE> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    _visitor = visitor;
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative) {
    return derivative.accept(_visitor);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative, final ParameterInflationIssuerProviderInterface data) {
    return derivative.accept(_visitor, data.getInflationProvider());
  }
}
