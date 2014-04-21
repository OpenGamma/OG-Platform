/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.ScenarioPnLPropertyNamesAndValues;

/**
 * Simple scenario Function returns the difference in PresentValue between defined Scenario and current market conditions.
 * @deprecated The parent class of this function is deprecated
 */
@Deprecated
public class InterestRateFutureOptionBlackScenarioPnLFunction extends InterestRateFutureOptionBlackFunction {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PNL}
   */
  public InterestRateFutureOptionBlackScenarioPnLFunction() {
    super(ValueRequirementNames.PNL, true);
  }

  /** The Black present value calculator */
  private static final PresentValueBlackCalculator s_pvCalculator = PresentValueBlackCalculator.getInstance();

  /** Property to define the price shift */
  private static final String s_priceShift = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT;
  /** Property to define the volatility shift */
  private static final String s_volShift = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT;
  /** Property to define the price shift type */
  private static final String s_priceShiftType = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT_TYPE;
  /** Property to define the volatility shift type */
  private static final String s_volShiftType = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT_TYPE;
  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackScenarioPnLFunction.class);

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle market,
      final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {

    // Compute present value under current market
    final double pvBase = irFutureOption.accept(s_pvCalculator, market);

    // Form market scenario
    final YieldCurveWithBlackCubeBundle marketScen;
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();

    // Apply shift to yield curve(s)
    final YieldCurveBundle curvesScen = new YieldCurveBundle();
    final String priceShiftTypeConstraint = constraints.getValues(s_priceShiftType).iterator().next();
    final String priceConstraint = constraints.getValues(s_priceShift).iterator().next();

    if (priceConstraint.equals("")) {
      // use base market prices
      curvesScen.addAll(market);
    } else {
      final Double shift = Double.valueOf(priceConstraint);
      // As curve may be functional, we can only apply a parallel shift.
      Double parallelShift;
      for (final String crvName : market.getAllNames()) {
        final YieldAndDiscountCurve curve = market.getCurve(crvName);
        if (priceShiftTypeConstraint.equalsIgnoreCase("Additive")) {
          parallelShift = shift;
        } else {
          if (!(priceShiftTypeConstraint.equalsIgnoreCase("Multiplicative"))) {
            s_logger.debug("Valid PriceShiftType's: Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
          }
          // We (arbitrarily) choose to scale by the rate at the short end
          final double shortRate = curve.getInterestRate(0.0);
          parallelShift = shift * shortRate;
        }
        final YieldAndDiscountCurve curveShifted = curve.withParallelShift(parallelShift);
        curvesScen.setCurve(crvName, curveShifted);
      }
    }

    // Apply shift to vol surface
    final String volConstraint = constraints.getValues(s_volShift).iterator().next();
    if (volConstraint.equals("")) {
      // use base market vols
      marketScen = market;
    } else {
      // bump vol surface
      final Double shiftVol = Double.valueOf(volConstraint);
      final String volShiftTypeConstraint = constraints.getValues(s_volShiftType).iterator().next();
      final boolean additiveShift;
      if (volShiftTypeConstraint.equalsIgnoreCase("Additive")) {
        additiveShift = true;
      } else if (volShiftTypeConstraint.equalsIgnoreCase("Multiplicative")) {
        additiveShift = false;
      } else {
        s_logger.debug("In ScenarioPnLFunctions, VolShiftType's are Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
        additiveShift = false;
      }
      final Surface<Double, Double, Double> volSurfaceScen = SurfaceShiftFunctionFactory.getShiftedSurface(market.getBlackParameters(), shiftVol, additiveShift);
      marketScen = new YieldCurveWithBlackCubeBundle(volSurfaceScen, curvesScen);
    }

    // Compute present value under scenario
    final double pvScen = irFutureOption.accept(s_pvCalculator, marketScen);

    // Return with spec
    return Collections.singleton(new ComputedValue(spec, pvScen - pvBase));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> superReqs = super.getRequirements(context, target, desiredValue);
    if (superReqs == null) {
      return null;
    }

    // Test constraints are provided, else set to ""
    final ValueProperties constraints = desiredValue.getConstraints();
    ValueProperties.Builder scenarioDefaults = null;

    final Set<String> priceShiftSet = constraints.getValues(s_priceShift);
    if (priceShiftSet == null || priceShiftSet.isEmpty()) {
      scenarioDefaults = constraints.copy().withoutAny(s_priceShift).with(s_priceShift, "");
    }
    final Set<String> priceShiftTypeSet = constraints.getValues(s_priceShiftType);
    if (priceShiftTypeSet == null || priceShiftTypeSet.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(s_priceShiftType).with(s_priceShiftType, "Multiplicative");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(s_priceShiftType).with(s_priceShiftType, "Multiplicative");
      }
    }
    final Set<String> volShiftSet = constraints.getValues(s_volShift);
    if (volShiftSet == null || volShiftSet.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(s_volShift).with(s_volShift, "");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(s_volShift).with(s_volShift, "");
      }
    }
    final Set<String> volShiftSetType = constraints.getValues(s_volShiftType);
    if (volShiftSetType == null || volShiftSetType.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(s_volShiftType).with(s_volShiftType, "Multiplicative");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(s_volShiftType).with(s_volShiftType, "Multiplicative");
      }
    }

    // If defaults have been added, this adds additional copy of the Function into dep graph with the adjusted constraints
    if (scenarioDefaults != null) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL, target.toSpecification(), scenarioDefaults.get()));
    } // Scenarios are defined, so we're satisfied
    return superReqs;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withAny(s_priceShift)
        .withAny(s_volShift)
        .withAny(s_priceShiftType)
        .withAny(s_volShiftType);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final ValueSpecification input = inputs.keySet().iterator().next();
      if ((ValueRequirementNames.PNL).equals(input.getValueName())) {
        return inputs.keySet();
      }
    }
    return super.getResults(context, target, inputs);
  }
}
