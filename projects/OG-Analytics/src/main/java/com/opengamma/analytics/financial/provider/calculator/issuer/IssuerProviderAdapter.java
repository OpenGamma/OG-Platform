/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * This class adapts a {@link InstrumentDerivativeVisitor} that is expecting data of type {@link MulticurveProviderInterface}
 * to one that will accept data for type {@link ParameterIssuerProviderInterface}.

 * @param <RESULT_TYPE> The result type for the provider.
 */
public class IssuerProviderAdapter<RESULT_TYPE> extends InstrumentDerivativeVisitorSameMethodAdapter<ParameterIssuerProviderInterface, RESULT_TYPE> {
  /** The underlying visitor */
  private final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> _visitor;

  /**
   * @param visitor The underlying visitor, not null
   */
  public IssuerProviderAdapter(final InstrumentDerivativeVisitor<ParameterProviderInterface, RESULT_TYPE> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    _visitor = visitor;
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative) {
    return derivative.accept(_visitor);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative, final ParameterIssuerProviderInterface data) {
    return derivative.accept(_visitor, data.getMulticurveProvider());
  }

}
