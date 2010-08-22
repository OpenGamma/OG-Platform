/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.world.RegionSource;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.DateUtil;


/**
 * 
 */
public class SimpleInterpolatedYieldAndDiscountCurveFunction extends AbstractFunction implements FunctionInvoker {

  @SuppressWarnings("unchecked")
  private Interpolator1D _interpolator;
  private YieldCurveDefinition _definition;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private final Currency _curveCurrency;
  private final String _curveName;
  private final boolean _isYieldCurve;
  private LocalDate _curveDate;
  private InterpolatedYieldCurveSpecification _specification;

  public SimpleInterpolatedYieldAndDiscountCurveFunction(final LocalDate curveDate, final Currency currency, final String name, final boolean isYieldCurve) {
    Validate.notNull(curveDate, "Curve Date");
    Validate.notNull(currency, "Currency");
    Validate.notNull(name, "Name");
    _definition = null;
    _curveDate = curveDate;
    _curveCurrency = currency;
    _curveName = name;
    _isYieldCurve = isYieldCurve;
    _interpolator = null;
    _requirements = null;
    _result = null;
    _results = null;
  }
  
  private ConfigSearchRequest buildConfigSearchRequest(String name) {
    ConfigSearchRequest yieldCurveDefinitionRequest = new ConfigSearchRequest();
    yieldCurveDefinitionRequest.setName(name);
    return yieldCurveDefinitionRequest;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    SecuritySource secSource = context.getSecuritySource();
    ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(configSource);
    _definition = curveDefinitionSource.getDefinition(_curveCurrency, _curveName);
    ConfigDBInterpolatedYieldCurveSpecificationBuilder curveSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(regionSource, configSource, secSource);
    _specification = curveSpecBuilder.buildCurve(_curveDate, _definition);
    _interpolator = Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName());
    _requirements = Collections.unmodifiableSet(buildRequirements(_specification));
    _result = new ValueSpecification(new ValueRequirement(_isYieldCurve ? ValueRequirementNames.YIELD_CURVE : ValueRequirementNames.DISCOUNT_CURVE, _definition.getCurrency()));
    _results = Collections.singleton(_result);
  }

  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final ResolvedFixedIncomeStrip strip : specification.getStrips()) {
      final ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getSecurity().getIdentifiers());
      result.add(requirement);
    }
    return result;
  }

  /**
   * @return the specification
   */
  public InterpolatedYieldCurveSpecification getSpecification() {
    return _specification;
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    // REVIEW: jim 23-July-2010 is this enough?  Probably not, but I'm not entirely sure what the deal with the Ids is...
    return ObjectUtils.equals(target.getUniqueIdentifier(), getSpecification().getCurrency().getUniqueIdentifier()); 
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _results;
    }
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public String getShortName() {
    return _curveCurrency + "-" + _curveName + (_isYieldCurve ? " Yield Curve" : " Discount Curve");
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    // Gather market data rates
    // Note that this assumes that all strips are priced in decimal percent. We need to resolve
    // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
    // the crazy IndicativeValue name.
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime today = snapshotClock.zonedDateTime(); // TODO: change to times
    final Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
    boolean isFirst = true;

    for (final ResolvedFixedIncomeStrip strip : getSpecification().getStrips()) {
      final ValueRequirement stripRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getSecurity().getIdentifiers());
      Double price = (Double) inputs.getValue(stripRequirement);
      if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
        price = (100d - price);
      }
      price /= 100d;
      if (_isYieldCurve) {
        if (isFirst) {
          // TODO This is here to avoid problems with instruments with expiry < 1 day
          // At the moment, interpolators don't extrapolate, and so asking for the rate
          // if t < 1 throws an exception. It doesn't actually matter in the case of discount curves,
          // because df at t = 0 is 1 by definition, but for yield curves this should change when
          // extrapolation is allowed
          timeInYearsToRates.put(0., 0.);
          isFirst = false;
        }
        final double years = DateUtil.getDifferenceInYears(today, strip.getMaturity()); 
        timeInYearsToRates.put(years, price);
      } else {
        if (isFirst) {
          timeInYearsToRates.put(0., 1.);
          isFirst = false;
        }
        final double years = DateUtil.getDifferenceInYears(today, strip.getMaturity());
        timeInYearsToRates.put(years, Math.exp(-price * years));
      }
    }
    // System.err.println("Time in years to rates: " + timeInYearsToRates);
    // Bootstrap the yield curve
    final YieldAndDiscountCurve curve = _isYieldCurve ? new InterpolatedYieldCurve(timeInYearsToRates, _interpolator) : new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
    final ComputedValue resultValue = new ComputedValue(_result, curve);
    return Collections.singleton(resultValue);
  }

}
