/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;


import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 * @author casey
 *
 */
public class EquityOptionBlackScenarioFunction extends EquityOptionBlackFunction {

  /** The Black present value calculator */
  private static final EquityOptionBlackPresentValueCalculator s_calculator = EquityOptionBlackPresentValueCalculator.getInstance();
  
  /** Default constructor */
  public EquityOptionBlackScenarioFunction() {
    super(ValueRequirementNames.MTM_PNL); // TODO Trying this. Was originally thinking something like SCENARIO_PNL..
  }
  
  @Override
  protected Set<ComputedValue> computeValues(InstrumentDerivative derivative, StaticReplicationDataBundle market, FunctionInputs inputs, Set<ValueRequirement> desiredValues,
      ComputationTargetSpecification targetSpec, ValueProperties resultProperties) {
    
    // Compute present value under current market
    final double pvBase = derivative.accept(s_calculator, market);
    
    // Apply shifts to market
    final double shiftStock = 0.0;
    ForwardCurve fwdCurveScen = market.getForwardCurve().withFractionalShift(shiftStock);
    
    final double shiftVol = 0.0;
    final boolean additiveShift = true;
    final BlackVolatilitySurface<?> volSurfScen = market.getVolatilitySurface().withShift(shiftVol, additiveShift);
    
    final StaticReplicationDataBundle marketScen = new StaticReplicationDataBundle(volSurfScen, market.getDiscountCurve(), fwdCurveScen);
    
    // Compute present value under market scenario
    final double pvScen = derivative.accept(s_calculator, marketScen);
    
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    return Collections.singleton(new ComputedValue(resultSpec, pvScen - pvBase));
  }

}
