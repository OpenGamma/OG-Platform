/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.view.ConfigDocumentWatchSetProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Pulls a {@link CurveConstructionConfiguration} from the config database.
 */
public class CurveConstructionConfigurationFunction extends AbstractFunction {
  /** The configuration name */
  private final String _configurationName;

  /**
   * @param configurationName The configuration name, not null
   */
  public CurveConstructionConfigurationFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, this, CurveConstructionConfiguration.class);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final CurveConstructionConfigurationSource curveConfigurationSource = new ConfigDBCurveConstructionConfigurationSource(configSource);
    final Instant versionTime = atZDT.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toInstant();
    final CurveConstructionConfiguration curveConstructionConfiguration = curveConfigurationSource.getCurveConstructionConfiguration(_configurationName,
        VersionCorrection.of(versionTime, versionTime));
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    //TODO also return any exogenous configs
    final ValueProperties properties = createValueProperties()
        .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_CONSTRUCTION_CONFIG, ComputationTargetSpecification.NULL, properties);
    final Set<ComputedValue> result = Collections.singleton(new ComputedValue(spec, curveConstructionConfiguration));
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        return result;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

    };
  }
}
