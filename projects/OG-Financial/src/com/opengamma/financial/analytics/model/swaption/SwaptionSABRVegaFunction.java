/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionSABRVegaFunction extends SwaptionSABRFunction {
  private static final DoublesPairComparator COMPARATOR = new DoublesPairComparator();
  private VolatilityCubeDefinition _definition;

  public SwaptionSABRVegaFunction(final Currency currency, final String cubeName, final boolean useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, cubeName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
  }

  public SwaptionSABRVegaFunction(final String currency, final String cubeName, final String useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, cubeName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
  }

  @Override 
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    _definition = getHelper().init(context, this);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube definition for swaption cube called " + getHelper().getDefinitionName());
    }
  }
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final SwaptionSecurity swaptionSecurity = (SwaptionSecurity) target.getSecurity();
    final FixedIncomeInstrumentDefinition<?> swaptionDefinition = swaptionSecurity.accept(getConverter());
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(target, inputs));
    final InterestRateDerivative swaption = swaptionDefinition.toDerivative(now, getFundingCurveName(), getForwardCurveName());
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    final Object alphaSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    if (alphaSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get alpha sensitivity");
    }
    final Object nuSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    if (nuSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get nu sensitivity");
    }
    final Object rhoSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    if (rhoSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get rho sensitivity");
    }
    final ValueRequirement cubeRequirement = getCubeRequirement(target);
    final Object sabrSurfacesObject = inputs.getValue(cubeRequirement);
    if (sabrSurfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR fitted surfaces");
    }
    final SABRFittedSurfaces sabrFittedSurfaces = (SABRFittedSurfaces) sabrSurfacesObject;
    final Map<Double, List<Pair<Double, DoubleMatrix2D>>> inverseJacobians = getMaturityExpiryValueMap(sabrFittedSurfaces.getInverseJacobians());
    return Collections.singleton(new ComputedValue(getResultSpec(target), new DoubleLabelledMatrix2D(new Double[]{1., 2.}, new Object[]{1., 2.}, 
        new Double[]{3., 4.}, new Object[]{3., 4.}, new double[][]{new double[]{0.1, 0.2}, new double[]{0.3, 0.4}})));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>(super.getRequirements(context, target, desiredValue));
    final String ccyCode = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(ccyCode);
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    return requirements;
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }

  private ValueProperties getModelSensitivityProperties(String ccyCode) {
    return ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, ccyCode)
      .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
      .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
      .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get();    
  }
  
  private ValueSpecification getResultSpec(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
            .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get());
  }
  
  private <T> Map<Double, List<Pair<Double, T>>> getMaturityExpiryValueMap(final Map<DoublesPair, T> data) {
    final TreeMap<DoublesPair, T> sorted = new TreeMap<DoublesPair, T>(COMPARATOR);
    sorted.putAll(data);
    final Map<Double, List<Pair<Double, T>>> result = new TreeMap<Double, List<Pair<Double, T>>>();
    for (final Map.Entry<DoublesPair, T> entry : sorted.entrySet()) {
      final double maturity = entry.getKey().second;
      if (!result.containsKey(maturity)) {
        final List<Pair<Double, T>> expiryValue = new ArrayList<Pair<Double, T>>();
        expiryValue.add(Pair.of(entry.getKey().first, entry.getValue()));
        result.put(maturity, expiryValue);
      } else {
        final List<Pair<Double, T>> expiryValue = result.get(maturity);
        expiryValue.add(Pair.of(entry.getKey().first, entry.getValue()));
      }
    }
    return result;
  }
  
  private static final class DoublesPairComparator implements Comparator<DoublesPair> {
    public DoublesPairComparator() {
    }

    @Override
    public int compare(final DoublesPair p1, final DoublesPair p2) {
      if (Double.compare(p1.second, p2.second) == 0) {
        return Double.compare(p1.first, p2.first);
      }
      return Double.compare(p1.second, p2.second);
    }
  }
}
