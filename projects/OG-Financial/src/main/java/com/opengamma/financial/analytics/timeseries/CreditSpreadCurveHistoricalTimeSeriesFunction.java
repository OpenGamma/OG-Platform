/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * 
 */
public class CreditSpreadCurveHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(CreditSpreadCurveHistoricalTimeSeriesFunction.class);

  private CurveDefinitionSource _curveDefinitionSource;
  private CurveSpecificationBuilder _curveSpecificationBuilder;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
    _curveSpecificationBuilder = ConfigDBCurveSpecificationBuilder.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final String idName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveName = idName;
    final CurveSpecification curveSpecification = CurveUtils.getCurveSpecification(now.toInstant(), _curveDefinitionSource, _curveSpecificationBuilder, now.toLocalDate(), curveName);
    final String dataField = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    final String resolutionKey;
    final Set<String> resolutionKeyConstraint = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    if (resolutionKeyConstraint == null || resolutionKeyConstraint.size() != 1) {
      resolutionKey = "Null";
    } else {
      resolutionKey = Iterables.getOnlyElement(resolutionKeyConstraint);
    }
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final Set<String> endDateConstraint = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    final String endDateString;
    if (endDateConstraint == null || endDateConstraint.size() != 1) {
      endDateString = "Now";
    } else {
      endDateString = Iterables.getOnlyElement(endDateConstraint);
    }
    final LocalDate endDate = DateConstraint.evaluate(executionContext, endDateString);
    final boolean includeEnd = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    final Set<CurveNodeWithIdentifier> nodes = curveSpecification.getNodes();
    for (final CurveNodeWithIdentifier node : nodes) {
      final ExternalIdBundle identifier = ExternalIdBundle.of(node.getIdentifier());
      final HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, identifier, resolutionKey, startDate, includeStart, endDate, includeEnd);
      if (timeSeries != null) {
        bundle.add(dataField, identifier, timeSeries);
      } else {
        s_logger.warn("Could not get time series for {}", identifier);
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.CREDIT_SPREAD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(), desiredValue
        .getConstraints()), bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.STANDARD_VANILLA_CDS_SECURITY.or(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY).or(FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_OPTION_SECURITY)
        .or(FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_INDEX_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(CURVE)
        //        .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, "PX_LAST")
        .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, MarketDataRequirementNames.MARKET_VALUE).withAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CREDIT_SPREAD_CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder constraints = null;
    final ValueProperties desiredValueConstraints = desiredValue.getConstraints();
    final Set<String> curveNames = desiredValueConstraints.getValues(CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    Set<String> values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      constraints = desiredValueConstraints.copy().with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, MarketDataRequirementNames.MARKET_VALUE);
    } else if (values.size() > 1) {
      constraints = desiredValueConstraints.copy().withoutAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, values.iterator().next());
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, "");
    } else if (values.size() > 1) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY).with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, values.iterator().next());
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, "");
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, "");
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    return Collections.emptySet();
  }

}
