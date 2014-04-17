/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Function to produce {@link InterpolatedYieldCurveSpecificationWithSecurities} values for a named curve/currency pair. An instance must be created and put into the repository for each curve
 * definition to be made available to downstream functions which can reference the required curves using property constraints.
 */
public class ImpliedYieldCurveSpecificationFunction extends AbstractFunction {
  private final Currency _currency;
  private final String _curveName;
  private final ComputationTargetSpecification _targetSpec;
  private ValueSpecification _resultSpec;
  private static final LocalTime CASH_EXPIRY_TIME = LocalTime.of(11, 00);
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;

  private ConfigSourceQuery<YieldCurveDefinition> _yieldCurveDefinition;

  public ImpliedYieldCurveSpecificationFunction(final String currency, final String curveDefinitionName) {
    this(Currency.of(currency), curveDefinitionName);
  }

  public ImpliedYieldCurveSpecificationFunction(final Currency currency, final String curveDefinitionName) {
    _curveName = curveDefinitionName;
    _currency = currency;
    _targetSpec = ComputationTargetSpecification.of(currency);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, _targetSpec, createValueProperties().with(ValuePropertyNames.CURVE, _curveName).get());
    _yieldCurveDefinition = ConfigSourceQuery.init(context, this, YieldCurveDefinition.class);
  }

  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final InterpolatedYieldCurveSpecification _curveSpecification;

    private CompiledImpl(final Instant earliest, final Instant latest, final InterpolatedYieldCurveSpecification curveSpecification) {
      super(earliest, latest);
      _curveSpecification = curveSpecification;
    }

    protected InterpolatedYieldCurveSpecification getCurveSpecification() {
      return _curveSpecification;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CURRENCY;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _currency.equals(target.getValue());
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(_resultSpec);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      return Collections.emptySet();
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
          OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource(), OpenGammaExecutionContext.getHolidaySource(executionContext));
      final SnapshotDataBundle marketData = new SnapshotDataBundle();
      for (final FixedIncomeStripWithIdentifier strip : getCurveSpecification().getStrips()) {
        marketData.setDataPoint(strip.getSecurity(), 0);
      }
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpecificationWithSecurities = resolveToDummySecurity(getCurveSpecification(), marketData, _currency);
      return Collections.singleton(new ComputedValue(_resultSpec, curveSpecificationWithSecurities));
    }

  }

  @SuppressWarnings("synthetic-access")
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atInstantZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final LocalDate curveDate = atInstantZDT.toLocalDate();
    final InterpolatedYieldCurveSpecificationBuilder curveSpecificationBuilder = OpenGammaCompilationContext.getInterpolatedYieldCurveSpecificationBuilder(context);
    final YieldCurveDefinition definition = _yieldCurveDefinition.get(_curveName + "_" + _currency.getCode());
    final InterpolatedYieldCurveSpecification curveSpecification = buildDummyCurve(curveDate, definition);
    return new CompiledImpl(atInstantZDT.with(LocalTime.MIDNIGHT).toInstant(), atInstantZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000).toInstant(), curveSpecification);
  }

  private static InterpolatedYieldCurveSpecification buildDummyCurve(final LocalDate curveDate, final YieldCurveDefinition definition) {
    final Collection<FixedIncomeStripWithIdentifier> ids = new ArrayList<>();
    for (final FixedIncomeStrip strip : definition.getStrips()) {
      if (strip.getInstrumentType() != StripInstrumentType.CASH) {
        throw new OpenGammaRuntimeException("Can only handle CASH strips");
      }
      final ExternalId id = ExternalSchemes.syntheticSecurityId(strip.getInstrumentType() + "_" + strip.getEffectiveTenor().getPeriod());
      ids.add(new FixedIncomeStripWithIdentifier(strip, id));
    }
    final String interpolatorName = definition.getInterpolatorName();
    final String leftExtrapolatorName = definition.getLeftExtrapolatorName();
    final String rightExtrapolatorName = definition.getRightExtrapolatorName();
    final boolean interpolateYield = definition.isInterpolateYields();
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    return new InterpolatedYieldCurveSpecification(curveDate, definition.getName(), definition.getCurrency(), interpolator, interpolateYield, ids, definition.getRegionId());
  }

  private static InterpolatedYieldCurveSpecificationWithSecurities resolveToDummySecurity(final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketData,
      final Currency currency) {
    final Collection<FixedIncomeStripWithSecurity> securityStrips = new TreeSet<>();
    final LocalDate curveDate = curveSpecification.getCurveDate();
    for (final FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      final ZonedDateTime start = curveDate.atTime(CASH_EXPIRY_TIME).atZone(ZoneOffset.UTC);
      final ZonedDateTime maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(ZoneOffset.UTC);
      final Tenor resolvedTenor = Tenor.of(Period.between(curveDate, maturity.toLocalDate()));
      final CashSecurity security = new CashSecurity(currency, curveSpecification.getRegion(), start, maturity, DAY_COUNT, 0, 0);
      securityStrips.add(new FixedIncomeStripWithSecurity(strip.getStrip(), resolvedTenor, maturity, strip.getSecurity(), security));
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, curveSpecification.getName(), curveSpecification.getCurrency(), curveSpecification.getInterpolator(),
        curveSpecification.interpolateYield(), securityStrips);
  }

}
