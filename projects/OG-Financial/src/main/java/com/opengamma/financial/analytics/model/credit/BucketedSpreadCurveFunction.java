/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class BucketedSpreadCurveFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get spread curve bucket data");
        }
        final YieldCurveData data = (YieldCurveData) dataObject;
        final ArrayList<Tenor> times = new ArrayList<>();
        final ArrayList<Double> rates = new ArrayList<>();
        for (final Map.Entry<ExternalIdBundle, Double> dataEntry : data.getDataPoints().entrySet()) {
          // TODO: The original code here was based on there just being one external ID per point and that having a value which is a period. It would
          // be better to use an id-scheme to tag such values just in case there are any other arbitrary tickers thrown into the bundle. The safest
          // interim approach is to use the first parseable one 
          Period period = null;
          for (final ExternalId id : dataEntry.getKey()) {
            try {
              period = Period.parse(id.getValue());
              break;
            } catch (final DateTimeParseException e) {
              // ignore
            }
          }
          if (period == null) {
            throw new IllegalArgumentException(dataEntry.toString());
          }
          times.add(Tenor.of(period));
          rates.add(dataEntry.getValue());
        }
        final NodalTenorDoubleCurve curve = new NodalTenorDoubleCurve(times.toArray(new Tenor[times.size()]), rates.toArray(new Double[rates.size()]), false);

        final ValueProperties properties = createValueProperties().get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_SPREADS, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, curve));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return target.getUniqueId().getScheme().equals(CreditCurveIdentifier.OBJECT_SCHEME);
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties().get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.BUCKETED_SPREADS, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final CreditCurveIdentifier curveId = CreditCurveIdentifier.of(target.toSpecification().getUniqueId());
        final Currency ccy = curveId.getCurrency();
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveId.toString()).get();
        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(3);
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, ComputationTargetSpecification.of(ccy), properties));
        return requirements;
      }

    };
  }

}
