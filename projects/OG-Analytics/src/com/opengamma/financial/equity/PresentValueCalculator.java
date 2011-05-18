/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import com.opengamma.financial.equity.future.EquityIndexDividendFuture;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import org.apache.commons.lang.Validate;

/**
 * TODO: Change YieldCurveBundle to something further fit to Equity
 */
public final class PresentValueCalculator extends AbstractEquityDerivativeVisitor<YieldCurveBundle, Double>  {
  
  private static final PresentValueCalculator s_instance = new PresentValueCalculator();

  public static PresentValueCalculator getInstance() {
    return s_instance;
  }

  private PresentValueCalculator() {
  }

  @Override
  public Double visit(final EquityDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final YieldCurveBundle curves) {
    Validate.notNull(curves); // shall we put the current market price in curves?
    Validate.notNull(future);
    //final double ta = future.getFixingDate();
    //final double tb = future.getDeliveryDate();
    
    final double current = 100.0; // !!! this is garbage? how does this get passed in ? 
    return (current - future.getStrike()) * future.getPointValue();
    
    // !!! or... SpotIndex / Z(0,T) - pV(EquityIndexFuture)
  }

  @Override
  public Double visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture) {
    return null;
  }

}
