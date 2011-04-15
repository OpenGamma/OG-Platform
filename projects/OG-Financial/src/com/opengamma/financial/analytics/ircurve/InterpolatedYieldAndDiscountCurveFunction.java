/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class InterpolatedYieldAndDiscountCurveFunction extends AbstractFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedYieldAndDiscountCurveFunction.class);

  /**
   * Resultant value specification property for the curve result. Note these should be moved into either the ValuePropertyNames class
   * if there are generic terms, or an OpenGammaValuePropertyNames if they are more specific to our financial integration.
   */
  public static final String PROPERTY_CURVE_DEFINITION_NAME = "NAME";

  private Interpolator1D<? extends Interpolator1DDataBundle> _interpolator;
  private YieldCurveDefinition _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private final Currency _curveCurrency;
  private final String _curveName;
  private final boolean _isYieldCurve;
  private InterpolatedYieldCurveSpecificationBuilder _curveSpecificationBuilder;

  public InterpolatedYieldAndDiscountCurveFunction(final Currency currency, final String name, final boolean isYieldCurve) {
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

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public void init(final FunctionCompilationContext context) {
    final InterpolatedYieldCurveDefinitionSource curveDefinitionSource = OpenGammaCompilationContext.getInterpolatedYieldCurveDefinitionSource(context);
    _definition = curveDefinitionSource.getDefinition(_curveCurrency, _curveName);
    _curveSpecificationBuilder = OpenGammaCompilationContext.getInterpolatedYieldCurveSpecificationBuilder(context);
    _interpolator = new CombinedInterpolatorExtrapolator(Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName()), new FlatExtrapolator1D());
    _result = new ValueSpecification(_isYieldCurve ? ValueRequirementNames.YIELD_CURVE : ValueRequirementNames.DISCOUNT_CURVE, new ComputationTargetSpecification(_definition.getCurrency()),
        createValueProperties().with(PROPERTY_CURVE_DEFINITION_NAME, _curveName).get());
    _results = Collections.singleton(_result);
    if (_definition.getUniqueId() != null) {
      context.getFunctionReinitializer().reinitializeFunction(this, _definition.getUniqueId());
    } else {
      s_logger.warn("Curve {} on {} has no identifier - cannot subscribe to updates", _curveName, _curveCurrency);
    }
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
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    // get the swap convention so we can find out the initial rate
    final ConventionBundle conventionBundle = conventionBundleSource
        .getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, specification.getCurrency().getCode() + "_SWAP"));
    final ConventionBundle referenceRateConvention = conventionBundleSource.getConventionBundle(IdentifierBundle.of(conventionBundle.getSwapFloatingLegInitialRate()));
    final Identifier initialRefRateId = SecurityUtils.bloombergTickerSecurityId(referenceRateConvention.getIdentifiers().getIdentifier(SecurityUtils.BLOOMBERG_TICKER));
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, initialRefRateId));
    return result;
  }

  private Map<Identifier, Double> buildMarketDataMap(final FunctionInputs inputs) {
    final Map<Identifier, Double> marketDataMap = new HashMap<Identifier, Double>();
    for (final ComputedValue value : inputs.getAllValues()) {
      final ComputationTargetSpecification targetSpecification = value.getSpecification().getTargetSpecification();
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
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return _results;
        }
        return null;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        if (canApplyTo(context, target)) {
          return requirements;
        }
        return null;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        // REVIEW: jim 23-July-2010 is this enough? Probably not, but I'm not entirely sure what the deal with the Ids is...
        return ObjectUtils.equals(target.getUniqueId(), specification.getCurrency().getUniqueId());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        // Gather market data rates
        // Note that this assumes that all strips are priced in decimal percent. We need to resolve
        // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
        // the crazy IndicativeValue name.
        final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
            OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource());
        final InterpolatedYieldCurveSpecificationWithSecurities specWithSecurities = builder.resolveToSecurity(specification, buildMarketDataMap(inputs));
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
        final YieldAndDiscountCurve curve = _isYieldCurve ? new YieldCurve(InterpolatedDoublesCurve.from(timeInYearsToRates, _interpolator)) : new DiscountCurve(
            InterpolatedDoublesCurve.from(timeInYearsToRates, _interpolator));
        final ComputedValue resultValue = new ComputedValue(_result, curve);
        return Collections.singleton(resultValue);
      }

    };
  }
}
