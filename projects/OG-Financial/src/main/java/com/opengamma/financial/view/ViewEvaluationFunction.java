/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.ViewCycleMetadata;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.async.ResultCallback;
import com.opengamma.util.time.DateUtils;

/**
 * Iterates a view client over historical data to produce historical valuations of one or more targets. The targets required and the parameters for the valuation are encoded into a
 * {@link ViewEvaluationTarget}. The results of the valuation are written to the value cache as a {@link ViewEvaluationResult} for each configuration and dependent nodes in the graph will extract time
 * series appropriate to their specific targets.
 */
public class ViewEvaluationFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Name of the property used to distinguish the outputs when the view definition has multiple calculation configurations.
   */
  public static final String PROPERTY_CALC_CONFIG = "config";

  private static final Logger s_logger = LoggerFactory.getLogger(ViewEvaluationFunction.class);

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.of(ViewEvaluationTarget.class);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ViewEvaluationTarget viewEvaluation = (ViewEvaluationTarget) target.getValue();
    final Collection<String> calcConfigs = viewEvaluation.getViewDefinition().getAllCalculationConfigurationNames();
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(calcConfigs.size());
    final ValueProperties.Builder properties = createValueProperties();
    for (final String calcConfig : calcConfigs) {
      properties.withoutAny(PROPERTY_CALC_CONFIG).with(PROPERTY_CALC_CONFIG, calcConfig);
      results.add(new ValueSpecification(ValueRequirementNames.VALUE, target.toSpecification(), properties.get()));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  protected UniqueId storeViewDefinition(final FunctionExecutionContext executionContext, final UniqueId targetId, final ViewDefinition viewDefinition) {
    final String name = targetId.toString(); // Use the tempTarget identifier as a name to reuse an existing config item
    final ConfigMaster master = OpenGammaExecutionContext.getConfigMaster(executionContext);
    if (master == null) {
      throw new IllegalStateException("Execution context does not contain a " + OpenGammaExecutionContext.CONFIG_MASTER_NAME);
    }
    final ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
    request.setName(name);
    final ConfigSearchResult<ViewDefinition> result = master.search(request);
    if (result.getDocuments() != null) {
      for (final ConfigDocument document : result.getDocuments()) {
        if (viewDefinition.equals(document.getValue().getValue())) {
          // Found a matching one
          s_logger.debug("Using previous view definition {}", document.getUniqueId());
          return document.getUniqueId();
        } else {
          // Found a dead one; either our temp target unique identifiers are not unique (different repositories MUST have different schemes) or the identifier
          // sequence has been restarted/repeated and is colliding with old or dead configuration documents.
          s_logger.info("Deleting expired view definition {}", document.getUniqueId());
          master.removeVersion(document.getUniqueId());
        }
      }
    }
    final ConfigItem<ViewDefinition> item = ConfigItem.of(viewDefinition);
    item.setName(name);
    final UniqueId uid = master.add(new ConfigDocument(item)).getUniqueId();
    s_logger.info("Created new view definition {} for {}", uid, name);
    return uid;
  }

  protected Collection<ViewCycleExecutionOptions> getExecutionCycleOptions(final FunctionExecutionContext executionContext, final ViewEvaluationTarget target) {
    LocalDate startDate = DateConstraint.getLocalDate(executionContext, target.getFirstValuationDate());
    if (startDate == null) {
      startDate = LocalDate.now(executionContext.getValuationClock());
    }
    if (!target.isIncludeFirstValuationDate()) {
      startDate = startDate.plusDays(1);
    }
    LocalDate finishDate = DateConstraint.getLocalDate(executionContext, target.getLastValuationDate());
    if (finishDate == null) {
      finishDate = LocalDate.now(executionContext.getValuationClock());
    }
    if (!target.isIncludeLastValuationDate()) {
      finishDate = finishDate.minusDays(1);
    }
    if (startDate.isAfter(finishDate)) {
      throw new IllegalArgumentException("First valuation date " + startDate + " is after last valuation date " + finishDate);
    }
    LocalDateTime dateTime = LocalDateTime.of(startDate, target.getValuationTime());
    final Collection<ViewCycleExecutionOptions> cycles = new ArrayList<ViewCycleExecutionOptions>(DateUtils.getDaysBetween(startDate, true, finishDate, true));
    do {
      final ZonedDateTime valuation = ZonedDateTime.of(dateTime, target.getTimeZone());
      cycles.add(ViewCycleExecutionOptions.builder().setValuationTime(valuation.toInstant()).setMarketDataSpecification(MarketData.historical(dateTime.getDate(), null))
          .setResolverVersionCorrection(VersionCorrection.of(valuation.toInstant(), target.getCorrection())).create());
      if (dateTime.getDate().equals(finishDate)) {
        return cycles;
      }
      dateTime = dateTime.plusDays(1);
    } while (true);
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ViewEvaluationTarget viewEvaluation = (ViewEvaluationTarget) target.getValue();
    final UniqueId viewId = storeViewDefinition(executionContext, target.getUniqueId(), viewEvaluation.getViewDefinition());
    final ViewProcessor viewProcessor = OpenGammaExecutionContext.getViewProcessor(executionContext);
    if (viewProcessor == null) {
      throw new IllegalStateException("Execution context does not contain a " + OpenGammaExecutionContext.VIEW_PROCESSOR_NAME);
    }
    final ViewClient viewClient = viewProcessor.createViewClient(viewEvaluation.getViewDefinition().getMarketDataUser());
    final UniqueId viewClientId = viewClient.getUniqueId();
    s_logger.info("Created view client {}, connecting to {}", viewClientId, viewId);
    final Collection<ViewCycleExecutionOptions> cycles = getExecutionCycleOptions(executionContext, viewEvaluation);
    viewClient.attachToViewProcess(viewId, ExecutionOptions.of(new ArbitraryViewCycleExecutionSequence(cycles), EnumSet.of(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER)), true);
    final ViewEvaluationResultBuilder resultBuilder = new ViewEvaluationResultBuilder(viewEvaluation.getTimeZone(), cycles.size(), viewEvaluation.getViewDefinition());
    final AsynchronousOperation<Set<ComputedValue>> async = AsynchronousOperation.createSet();
    final AtomicReference<ResultCallback<Set<ComputedValue>>> asyncResult = new AtomicReference<ResultCallback<Set<ComputedValue>>>(async.getCallback());
    viewClient.setResultListener(new ViewResultListener() {

      private void reportException(final RuntimeException e) {
        final ResultCallback<?> callback = asyncResult.getAndSet(null);
        if (callback != null) {
          try {
            callback.setException(e);
          } finally {
            s_logger.info("Shutting down view client {}", viewClientId);
            viewClient.shutdown();
          }
        } else {
          s_logger.warn("Callback already made before exception for {}", viewClientId);
        }
      }

      private void reportResult(final Set<ComputedValue> values) {
        final ResultCallback<Set<ComputedValue>> callback = asyncResult.getAndSet(null);
        if (callback != null) {
          try {
            callback.setResult(values);
          } finally {
            s_logger.info("Shutting down view client {}", viewClientId);
            viewClient.shutdown();
          }
        } else {
          s_logger.warn("Callback already made before results for {}", viewClientId);
        }
      }

      @Override
      public UserPrincipal getUser() {
        return viewEvaluation.getViewDefinition().getMarketDataUser();
      }

      @Override
      public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
        s_logger.debug("View definition compiled for {}", viewClientId);
        resultBuilder.store(compiledViewDefinition);
      }

      @Override
      public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
        s_logger.error("View compilation failure for {} - {}", viewClientId, exception);
        reportException(new OpenGammaRuntimeException("View definition compilation failed for " + valuationTime, exception));
      }

      @Override
      public void cycleStarted(final ViewCycleMetadata cycleMetadata) {
        // This is good. Don't need to do anything.
        s_logger.debug("Cycle started for {}", viewClientId);
      }

      @Override
      public void cycleFragmentCompleted(final ViewComputationResultModel fullFragment, final ViewDeltaResultModel deltaFragment) {
        // This shouldn't happen. We've asked for full results only
        s_logger.error("Cycle fragment completed for {}", viewClientId);
        reportException(new OpenGammaRuntimeException("Assertion error"));
        assert false;
      }

      @Override
      public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
        s_logger.debug("Cycle completed for {}", viewClientId);
        try {
          viewClient.triggerCycle();
          resultBuilder.store(fullResult);
        } catch (final RuntimeException e) {
          s_logger.error("Caught exception during cycle completed callback", e);
          reportException(e);
        }
      }

      @Override
      public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
        s_logger.error("Cycle execution failed for {}", viewClientId);
        reportException(new OpenGammaRuntimeException("View cycle execution failed for " + executionOptions, exception));
      }

      @Override
      public void processCompleted() {
        s_logger.info("View process completed for {}", viewClientId);
        try {
          final Map<String, ViewEvaluationResult> viewResults = resultBuilder.getResults();
          final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(viewResults.size());
          final ValueProperties.Builder properties = createValueProperties();
          final ComputationTargetSpecification targetSpec = target.toSpecification();
          for (final Map.Entry<String, ViewEvaluationResult> viewResult : viewResults.entrySet()) {
            properties.withoutAny(PROPERTY_CALC_CONFIG).with(PROPERTY_CALC_CONFIG, viewResult.getKey());
            results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.VALUE, targetSpec, properties.get()), viewResult.getValue()));
          }
          reportResult(results);
        } catch (final RuntimeException e) {
          s_logger.error("Caught exception during process completed callback", e);
          reportException(e);
        }
      }

      @Override
      public void processTerminated(final boolean executionInterrupted) {
        // Normally we would have expected one of the other notifications, so if the callback exists we report an error
        final ResultCallback<?> callback = asyncResult.getAndSet(null);
        if (callback != null) {
          s_logger.error("View process terminated for {}", viewClientId);
          reportException(new OpenGammaRuntimeException(executionInterrupted ? "Execution interrupted" : "View process terminated"));
        } else {
          s_logger.debug("View process terminated for {}", viewClientId);
        }
      }

      @Override
      public void clientShutdown(final Exception e) {
        // Normally we would have expected one of the other notifications or this in response to us calling "shutdown", so if the callback exists we report an error
        final ResultCallback<?> callback = asyncResult.getAndSet(null);
        if (callback != null) {
          s_logger.error("View client shutdown for {}", viewClientId);
          reportException(new OpenGammaRuntimeException("View client shutdown", e));
        } else {
          s_logger.debug("View client shutdown for {}", viewClientId);
        }
      }

    });
    viewClient.triggerCycle();
    return async.getResult();
  }

}
