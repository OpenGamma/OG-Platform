/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.DateUtil;


/**
 * 
 */
public class SimpleInterpolatedYieldAndDiscountCurveFunction extends AbstractFunction {

  @SuppressWarnings("unchecked")
  private Interpolator1D _interpolator;
  private YieldCurveDefinition _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private final Currency _curveCurrency;
  private final String _curveName;
  private final boolean _isYieldCurve;
  private InterpolatedYieldCurveSpecificationBuilder _curveSpecificationBuilder;

  public SimpleInterpolatedYieldAndDiscountCurveFunction(final Currency currency, final String name, final boolean isYieldCurve) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(name, "Name");
    _definition = null;
    _curveCurrency = currency;
    _curveName = name;
    _isYieldCurve = isYieldCurve;
    _interpolator = null;
    _result = null;
    _results = null;
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

  @SuppressWarnings("unchecked")
  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(configSource);
    _definition = curveDefinitionSource.getDefinition(_curveCurrency, _curveName);
    _curveSpecificationBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
    _interpolator = new CombinedInterpolatorExtrapolator(Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName()), new FlatExtrapolator1D());
    _result = new ValueSpecification(new ValueRequirement(_isYieldCurve ? ValueRequirementNames.YIELD_CURVE : ValueRequirementNames.DISCOUNT_CURVE, _definition.getCurrency()), getUniqueIdentifier());
    _results = Collections.singleton(_result);
  }

  @Override
  public String getShortName() {
    return _curveCurrency + "-" + _curveName + (_isYieldCurve ? " Yield Curve" : " Discount Curve");
  }

  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification, final FunctionCompilationContext context) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      final ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurity());
      result.add(requirement);
    }
    ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    // get the swap convention so we can find out the initial rate
    ConventionBundle conventionBundle = conventionBundleSource
        .getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, specification.getCurrency().getISOCode() + "_SWAP"));
    ConventionBundle referenceRateConvention = conventionBundleSource.getConventionBundle(IdentifierBundle.of(conventionBundle.getSwapFloatingLegInitialRate()));
    Identifier initialRefRateId = Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, referenceRateConvention.getIdentifiers().getIdentifier(IdentificationScheme.BLOOMBERG_TICKER));
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, initialRefRateId));
    return result;
  }

  private Map<Identifier, Double> buildMarketDataMap(FunctionInputs inputs) {
    Map<Identifier, Double> marketDataMap = new HashMap<Identifier, Double>();
    for (ComputedValue value : inputs.getAllValues()) {
      ComputationTargetSpecification targetSpecification = value.getSpecification().getRequirementSpecification().getTargetSpecification();
      if (value.getValue() instanceof Double) {
        marketDataMap.put(targetSpecification.getIdentifier(), (Double) value.getValue());
      }
    }
    return marketDataMap;
  }

  protected InterpolatedYieldCurveSpecification createSpecification(final LocalDate curveDate) {
    return _curveSpecificationBuilder.buildCurve(curveDate, _definition);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final InterpolatedYieldCurveSpecification specification = createSpecification(atInstant.toLocalDate());
    final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(specification, context));
    // ENG-252 see MarkingInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return _results;
        }
        return null;
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return requirements;
        }
        return null;
      }

      @Override
      public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        // REVIEW: jim 23-July-2010 is this enough? Probably not, but I'm not entirely sure what the deal with the Ids is...
        return ObjectUtils.equals(target.getUniqueIdentifier(), specification.getCurrency().getUniqueIdentifier());
      }

      @SuppressWarnings("unchecked")
      @Override
      public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
        // Gather market data rates
        // Note that this assumes that all strips are priced in decimal percent. We need to resolve
        // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
        // the crazy IndicativeValue name.
        FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext), OpenGammaExecutionContext
            .getConventionBundleSource(executionContext), executionContext.getSecuritySource());
        InterpolatedYieldCurveSpecificationWithSecurities specWithSecurities = builder.resolveToSecurity(specification, buildMarketDataMap(inputs));
        final Clock snapshotClock = executionContext.getSnapshotClock();
        final ZonedDateTime today = snapshotClock.zonedDateTime(); // TODO: change to times
        final Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
        boolean isFirst = true;
        for (final FixedIncomeStripWithSecurity strip : specWithSecurities.getStrips()) {
          final ValueRequirement stripRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurityIdentifier());
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
        // Bootstrap the yield curve
        final YieldAndDiscountCurve curve = _isYieldCurve ? new InterpolatedYieldCurve(timeInYearsToRates, _interpolator) : new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
        final ComputedValue resultValue = new ComputedValue(_result, curve);
        return Collections.singleton(resultValue);
      }

    };
  }

}
