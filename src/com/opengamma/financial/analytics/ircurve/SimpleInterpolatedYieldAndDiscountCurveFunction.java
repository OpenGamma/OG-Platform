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

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class SimpleInterpolatedYieldAndDiscountCurveFunction extends AbstractFunction implements FunctionInvoker {
  
  @SuppressWarnings("unchecked")
  private Interpolator1D _interpolator;
  private InterpolatedYieldAndDiscountCurveDefinition _definition;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private Currency _curveCurrency;
  private String _curveName;
  private boolean _isYieldCurve;
  
  public SimpleInterpolatedYieldAndDiscountCurveFunction(Currency currency, String name, boolean isYieldCurve) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(name, "Name");
    _definition = null;
    _curveCurrency = currency;
    _curveName = name;
    _isYieldCurve = isYieldCurve;
    _interpolator = null;
    _requirements = null;
    _result = null;
    _results = null;
  }

  @Override
  public void init(FunctionCompilationContext context) {
    InterpolatedYieldAndDiscountCurveSource curveSource = OpenGammaCompilationContext.getDiscountCurveSource(context);
    _definition = curveSource.getDefinition(_curveCurrency, _curveName);
    _interpolator = Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName());
    _requirements = Collections.unmodifiableSet(buildRequirements(_definition));
    _result = new ValueSpecification(new ValueRequirement(_isYieldCurve ? ValueRequirementNames.YIELD_CURVE
        : ValueRequirementNames.DISCOUNT_CURVE, _definition.getCurrency()));
    _results = Collections.singleton(_result);
  }
  
  public static Set<ValueRequirement> buildRequirements(InterpolatedYieldAndDiscountCurveDefinition definition) {
    Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (FixedIncomeStrip strip : definition.getStrips()) {
      ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      result.add(requirement);
    }
    return result;
  }

  /**
   * @return the definition
   */
  public InterpolatedYieldAndDiscountCurveDefinition getDefinition() {
    return _definition;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(
        target.getUniqueIdentifier(),
        getDefinition().getCurrency().getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
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
  public Set<ComputedValue> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      ComputationTarget target,
      Set<ValueRequirement> desiredValues) {
    // Gather market data rates
    // Note that this assumes that all strips are priced in decimal percent. We need to resolve
    // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
    // the crazy IndicativeValue name.
    Clock snapshotClock = executionContext.getSnapshotClock();
    LocalDate today = snapshotClock.today(); // TODO: change to times
    Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
    boolean isFirst = true;
    for (FixedIncomeStrip strip : getDefinition().getStrips()) {
      ValueRequirement stripRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      FudgeFieldContainer fieldContainer = (FudgeFieldContainer) inputs.getValue(stripRequirement);
      Double price = fieldContainer.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
      if (strip.getInstrumentType() == StripInstrument.FUTURE) {
        price = (100d - price);
      }
      price /= 100d;
      if (_isYieldCurve) {
        if (isFirst) {
          //TODO This is here to avoid problems with instruments with expiry < 1 day 
          // At the moment, interpolators don't extrapolate, and so asking for the rate 
          // if t < 1 throws an exception. It doesn't actually matter in the case of discount curves,
          // because df at t = 0 is 1 by definition, but for yield curves this should change when
          // extrapolation is allowed
          timeInYearsToRates.put(0., 0.);
          isFirst = false;
        }
        double numYears = (strip.getEndDate().toEpochDays() - today.toEpochDays())/DateUtil.DAYS_PER_YEAR;
        timeInYearsToRates.put(numYears, price);
      } else {
        if (isFirst) {
          timeInYearsToRates.put(0., 1.);
          isFirst = false;
        }
        double numYears = (strip.getEndDate().toEpochDays() - today.toEpochDays())/DateUtil.DAYS_PER_YEAR;
        timeInYearsToRates.put(numYears, Math.exp(-price * numYears));
      }
    }
    System.err.println(timeInYearsToRates);
    // Bootstrap the yield curve
    YieldAndDiscountCurve curve = _isYieldCurve ? new InterpolatedYieldCurve(timeInYearsToRates, _interpolator)
        : new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
    ComputedValue resultValue = new ComputedValue(_result, curve);
    return Collections.singleton(resultValue);
  }

}
