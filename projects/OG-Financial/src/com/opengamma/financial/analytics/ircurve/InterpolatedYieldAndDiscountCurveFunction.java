/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.id.ExternalId;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class InterpolatedYieldAndDiscountCurveFunction extends AbstractFunction {

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedYieldAndDiscountCurveFunction.class);
  /** Name of the calculation method */
  public static final String INTERPOLATED_CALCULATION_METHOD = "Interpolated";

  private final YieldCurveFunctionHelper _helper;
  private final Currency _curveCurrency;
  private final String _curveName;
  private final boolean _isYieldCurve;

  private Interpolator1D _interpolator;
  private YieldCurveDefinition _definition;
  private ValueSpecification _result;
  private ValueSpecification _specResult;
  private Set<ValueSpecification> _results;

  public InterpolatedYieldAndDiscountCurveFunction(final String currency, final String name, final String isYieldCurve) {
    this(Currency.of(currency), name, Boolean.parseBoolean(isYieldCurve));
  }

  public InterpolatedYieldAndDiscountCurveFunction(final Currency currency, final String name, final boolean isYieldCurve) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(name, "Name");

    _helper = new YieldCurveFunctionHelper(currency, name);
    _definition = null;
    _curveCurrency = currency;
    _curveName = name;
    _isYieldCurve = isYieldCurve;
    _interpolator = null;
    _result = null;
    _results = null;
  }

  // Temporary for debugging
  public InterpolatedYieldAndDiscountCurveFunction(final String currency, final String name) {
    this(Currency.of(currency), name, true);
  }

  public Currency getCurveCurrency() {
    return _curveCurrency;
  }

  public String getCurveName() {
    return _curveName;
  }

  public boolean isYieldCurve() {
    return _isYieldCurve;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _definition = _helper.init(context, this);

    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(_definition.getCurrency());
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, _curveName).get();
    _interpolator = new CombinedInterpolatorExtrapolator(Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName()), new FlatExtrapolator1D());
    final String curveReqName = _isYieldCurve ? ValueRequirementNames.YIELD_CURVE : ValueRequirementNames.DISCOUNT_CURVE;
    _result = new ValueSpecification(curveReqName, targetSpec, properties);

    _specResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, properties);

    _results = Sets.newHashSet(_result, _specResult);
  }

  @Override
  public String getShortName() {
    return _curveCurrency + "-" + _curveName + (_isYieldCurve ? " Yield Curve" : " Discount Curve");
  }

  protected InterpolatedYieldCurveSpecification createSpecification(final LocalDate curveDate) {
    return _helper.buildCurve(curveDate);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> compile = _helper.compile(context, atInstantProvider);

    final InterpolatedYieldCurveSpecification specification = compile.getThird();

    // ENG-252 see MarkingInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(compile.getFirst(), compile.getSecond()) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return _results;
        }
        return null;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
        result.add(_helper.getMarketDataValueRequirement());
        return result;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return _helper.canApplyTo(target);
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final Map<ExternalId, Double> marketDataMap = _helper.buildMarketDataMap(inputs);

        // Gather market data rates
        // Note that this assumes that all strips are priced in decimal percent. We need to resolve
        // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
        // the crazy IndicativeValue name.
        final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
            OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource(), OpenGammaExecutionContext.getHolidaySource(executionContext));
        final InterpolatedYieldCurveSpecificationWithSecurities specWithSecurities = builder.resolveToSecurity(specification, marketDataMap);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime today = snapshotClock.zonedDateTime(); // TODO: change to times
        final Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
        boolean isFirst = true;
        for (final FixedIncomeStripWithSecurity strip : specWithSecurities.getStrips()) {
          Double price = marketDataMap.get(strip.getSecurityIdentifier());
          if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
            price = 100d - price;
          }
          price /= 100d;
          if (_isYieldCurve) {
            final double years = DateUtils.getDifferenceInYears(today, strip.getMaturity());
            timeInYearsToRates.put(years, price);
          } else {
            if (isFirst) {
              timeInYearsToRates.put(0., 1.);
              isFirst = false;
            }
            final double years = DateUtils.getDifferenceInYears(today, strip.getMaturity());
            timeInYearsToRates.put(years, Math.exp(-price * years));
          }
        }
        final YieldAndDiscountCurve curve = _isYieldCurve ? new YieldCurve(InterpolatedDoublesCurve.from(timeInYearsToRates, _interpolator)) : new DiscountCurve(InterpolatedDoublesCurve.from(
            timeInYearsToRates, _interpolator));
        final ComputedValue resultValue = new ComputedValue(_result, curve);
        final ComputedValue specValue = new ComputedValue(_specResult, specWithSecurities);
        return Sets.newHashSet(resultValue, specValue);
      }

    };
  }
}
