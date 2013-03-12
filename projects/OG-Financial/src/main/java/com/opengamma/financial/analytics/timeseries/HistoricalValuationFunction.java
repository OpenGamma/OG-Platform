/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.financial.view.HistoricalViewEvaluationResult;
import com.opengamma.financial.view.HistoricalViewEvaluationTarget;
import com.opengamma.financial.view.ViewEvaluationTarget;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.timeseries.TimeSeries;

/**
 * Iterates a view client over historical data to produce a historical valuation of a target. The view client iteration is performed by a helper function on a {@link ViewEvaluationTarget} created by
 * this function. The time series appropriate to this function's target are then extracted from the overall evaluation results.
 */
public class HistoricalValuationFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalValuationFunction.class);

  /**
   * Property naming the value produced on the target to generate the time series. For example {@code Historical Series[Value=FairValue]} will produce a time series based on evaluating
   * {@code FairValue[]}.
   */
  public static final String VALUE_PROPERTY = "Value";

  /**
   * Prefix on properties corresponding the the underlying production on the target. For example {@code Historical Series[Value=FairValue, Value_Foo=Bar]} will produce a time series based on
   * evaluating {@code FairValue[Foo=Bar]}.
   */
  public static final String PASSTHROUGH_PREFIX = VALUE_PROPERTY + "_";

  /**
   * Property naming how the target is specified, for example by unique identifier, object identifier or external identifier.
   */
  public static final String TARGET_SPECIFICATION_PROPERTY = "Target";

  /**
   * Value of the {@link #TARGET_SPECIFICATION_PROPERTY} property indicating the target is specified by its unique identifier and is independent of the resolver version/correction time on the spawned
   * view cycles.
   */
  public static final String TARGET_SPECIFICATION_UNIQUE = "Unique";

  /**
   * Value of the {@link #TARGET_SPECIFICATION_PROPERTY} property indicating the target is specified by its object identifier and is dependent of the resolver version/correction time on the spawned
   * view cycles.
   */
  public static final String TARGET_SPECIFICATION_OBJECT = "Object";

  /**
   * Value of the {@link #TARGET_SPECIFICATION_PROPERTY} property indicating the target is specified by its external identifier bundle and is dependent of the resolver version/correction time on the
   * spawned view cycles.
   */
  public static final String TARGET_SPECIFICATION_EXTERNAL = "External";

  protected ValueRequirement getNestedRequirement(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTarget target, final ValueProperties constraints) {
    String valueName = ValueRequirementNames.VALUE;
    ComputationTargetReference requirementTarget = null;
    final ValueProperties.Builder requirementConstraints = ValueProperties.builder();
    if (constraints.getProperties() != null) {
      Set<String> values = constraints.getValues(VALUE_PROPERTY);
      if (values == null || values.isEmpty()) {
        valueName = ValueRequirementNames.VALUE;
      } else if (values.size() > 1) {
        return null;
      } else {
        valueName = Iterables.getOnlyElement(values);
      }
      for (final String constraintName : constraints.getProperties()) {
        final Set<String> constraintValues = constraints.getValues(constraintName);
        if (VALUE_PROPERTY.equals(constraintName)) {
          if (constraintValues.isEmpty()) {
            valueName = ValueRequirementNames.VALUE;
          } else if (constraintValues.size() > 1) {
            return null;
          } else {
            valueName = constraintValues.iterator().next();
          }
        } else if (TARGET_SPECIFICATION_PROPERTY.equals(constraintName)) {
          if (constraintValues.isEmpty() || constraintValues.contains(TARGET_SPECIFICATION_UNIQUE)) {
            requirementTarget = target.toSpecification();
          } else if (constraintValues.contains(TARGET_SPECIFICATION_OBJECT)) {
            final ComputationTargetSpecification targetSpec = target.toSpecification();
            if (targetSpec.getUniqueId() != null) {
              requirementTarget = ComputationTargetResolverUtils.simplifyType(targetSpec.replaceIdentifier(target.getUniqueId().toLatest()), resolver);
            } else {
              // Null - special case
              requirementTarget = targetSpec;
            }
          } else if (constraintValues.contains(TARGET_SPECIFICATION_EXTERNAL)) {
            final ExternalIdBundle identifiers;
            if (target.getValue() instanceof ExternalIdentifiable) {
              final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
              if (identifier == null) {
                identifiers = ExternalIdBundle.EMPTY;
              } else {
                identifiers = identifier.toBundle();
              }
            } else if (target.getValue() instanceof ExternalBundleIdentifiable) {
              identifiers = ((ExternalBundleIdentifiable) target.getValue()).getExternalIdBundle();
            } else if (target.getValue() == null) {
              // Null - special case
              identifiers = ExternalIdBundle.EMPTY;
            } else {
              return null;
            }
            if (target.getContextIdentifiers() == null) {
              requirementTarget = new ComputationTargetRequirement(resolver.simplifyType(target.getType()), identifiers);
            } else {
              requirementTarget = target.getContextSpecification().containing(resolver.simplifyType(target.getLeafSpecification().getType()), identifiers);
            }
          } else if (constraintName.startsWith(PASSTHROUGH_PREFIX)) {
            final String name = constraintName.substring(PASSTHROUGH_PREFIX.length());
            if (constraintValues.isEmpty()) {
              requirementConstraints.withAny(name);
            } else {
              requirementConstraints.with(name, constraintValues);
            }
            if (constraints.isOptional(constraintName)) {
              requirementConstraints.withOptional(name);
            }
          } else {
            return null;
          }
        }
      }
    }
    if (requirementTarget == null) {
      requirementTarget = ComputationTargetResolverUtils.simplifyType(target.toSpecification(), resolver);
    }
    return new ValueRequirement(valueName, requirementTarget, requirementConstraints.get());
  }

  // FunctionDefinition

  @Override
  public void init(final FunctionCompilationContext context) {
    if (OpenGammaCompilationContext.getTempTargets(context) == null) {
      throw new IllegalStateException("Function compilation context does not contain " + OpenGammaCompilationContext.TEMPORARY_TARGETS_NAME);
    }
  }

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.ANYTHING;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return !(target.getValue() instanceof HistoricalViewEvaluationTarget) && context.getViewCalculationConfiguration() != null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties constraints = desiredValue.getConstraints();
    
    String startDateConstraint = null;
    String includeStartConstraint = null;
    String endDateConstraint = null;
    String includeEndConstraint = null;
    String periodConstraint = null; 
    
    if (!constraints.isEmpty()) {
      for (String constraintName : constraints.getProperties()) {
        Set<String> constraintValues = constraints.getValues(constraintName);
        if (constraintName.equals(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)) {
          startDateConstraint = Iterables.getOnlyElement(constraintValues);
        } else if (constraintName.equals(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)) {
          endDateConstraint = Iterables.getOnlyElement(constraintValues);
        } else if (constraintName.equals(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY)) {
          includeStartConstraint = Iterables.getOnlyElement(constraintValues);
        } else if (constraintName.equals(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY)) {
          includeEndConstraint = Iterables.getOnlyElement(constraintValues);
        } else if (constraintName.equals(ValuePropertyNames.SAMPLING_PERIOD)) {
          periodConstraint = Iterables.getOnlyElement(constraintValues);
        } else if (!ValuePropertyNames.FUNCTION.equals(constraintName) && !constraints.isOptional(constraintName)) {
          // getResults uses ValueProperties.all() so have to filter out invalid constraints here
          return null;
        }
      }
    }
    
    LocalDate startDate = startDateConstraint != null ? LocalDate.parse(startDateConstraint) : LocalDate.now();
    boolean includeStart = includeStartConstraint == null || includeStartConstraint.equals(HistoricalTimeSeriesFunctionUtils.YES_VALUE) ? true : false;
    LocalDate endDate = endDateConstraint != null ? LocalDate.parse(endDateConstraint) : LocalDate.now();
    boolean includeEnd = includeEndConstraint == null || includeEndConstraint.equals(HistoricalTimeSeriesFunctionUtils.YES_VALUE) ? true : false;
    Period period;
    if (periodConstraint != null) {
      period = Period.parse(periodConstraint);
      if (startDateConstraint != null) {
        endDate = startDate.plus(period);
      } else {
        // With no other constraints, just specifying the period will cause a run of that length, ending today
        startDate = endDate.minus(period);
      }
    } else {
      period = null;
    }
    
    ViewDefinition viewDefinition = context.getViewCalculationConfiguration().getViewDefinition();
    final HistoricalViewEvaluationTarget tempTarget = new HistoricalViewEvaluationTarget(viewDefinition.getMarketDataUser(), startDate, includeStart, endDate, includeEnd, period);
    final ValueRequirement requirement = getNestedRequirement(context.getComputationTargetResolver(), target, desiredValue.getConstraints());
    if (requirement == null) {
      return null;
    }
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(tempTarget.getViewDefinition(), context.getViewCalculationConfiguration().getName());
    calcConfig.addSpecificRequirement(requirement);
    tempTarget.getViewDefinition().addViewCalculationConfiguration(calcConfig);
    final TempTargetRepository targets = OpenGammaCompilationContext.getTempTargets(context);
    final UniqueId tempTargetId = targets.locateOrStore(tempTarget);
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, new ComputationTargetSpecification(TempTarget.TYPE, tempTargetId), ValueProperties.none()));
  }

  protected ValueProperties.Builder createValueProperties(final HistoricalViewEvaluationTarget target) {
    final ValueProperties.Builder builder = createValueProperties();
    builder.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, target.getStartDate().toString());
    builder.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, target.isIncludeStart() ? HistoricalTimeSeriesFunctionUtils.YES_VALUE
        : HistoricalTimeSeriesFunctionUtils.NO_VALUE);
    builder.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, target.getEndDate().toString());
    builder.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, target.isIncludeEnd() ? HistoricalTimeSeriesFunctionUtils.YES_VALUE
        : HistoricalTimeSeriesFunctionUtils.NO_VALUE);
    builder.with(ValuePropertyNames.SAMPLING_PERIOD, target.getPeriod().toString());
    return builder;
  }

  // TODO: Our declared type of anything means there will never be a parent context, this will probably need fixing

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final TempTarget tempTargetObject = OpenGammaCompilationContext.getTempTargets(context).get(inputs.keySet().iterator().next().getTargetSpecification().getUniqueId());
    if (tempTargetObject instanceof HistoricalViewEvaluationTarget) {
      final HistoricalViewEvaluationTarget historicalTarget = (HistoricalViewEvaluationTarget) tempTargetObject;
      final ViewCalculationConfiguration calcConfig = historicalTarget.getViewDefinition().getCalculationConfiguration(context.getViewCalculationConfiguration().getName());
      final ExternalIdBundle targetEids;
      if (target.getValue() instanceof ExternalIdentifiable) {
        targetEids = ((ExternalIdentifiable) target.getValue()).getExternalId().toBundle();
      } else if (target.getValue() instanceof ExternalBundleIdentifiable) {
        targetEids = ((ExternalBundleIdentifiable) target.getValue()).getExternalIdBundle();
      } else {
        targetEids = null;
      }
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      final ComputationTargetSpecification targetContextSpec = target.getContextSpecification();
      final ComputationTargetReferenceVisitor<Set<String>> getTargetType = new ComputationTargetReferenceVisitor<Set<String>>() {

        @Override
        public Set<String> visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
          if (target.getUniqueId() == null) {
            if (requirement.getIdentifiers().isEmpty()) {
              if (ObjectUtils.equals(requirement.getParent(), targetContextSpec)) {
                // Null target can be referenced by anything
                return ImmutableSet.of(TARGET_SPECIFICATION_OBJECT, TARGET_SPECIFICATION_UNIQUE, TARGET_SPECIFICATION_EXTERNAL);
              }
            }
          } else {
            if ((targetEids != null) && targetEids.equals(requirement.getIdentifiers())) {
              if (ObjectUtils.equals(requirement.getParent(), targetContextSpec)) {
                // Our target
                return Collections.singleton(TARGET_SPECIFICATION_EXTERNAL);
              }
            }
          }
          // Not our target
          return null;
        }

        @Override
        public Set<String> visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
          if (target.getUniqueId() == null) {
            if (specification.getUniqueId() == null) {
              // Null target can be referenced by anything
              return ImmutableSet.of(TARGET_SPECIFICATION_OBJECT, TARGET_SPECIFICATION_UNIQUE, TARGET_SPECIFICATION_EXTERNAL);
            }
          } else if (target.getUniqueId().isLatest()) {
            // The target is a primitive - unique and object are the same
            if (target.getUniqueId().equals(specification.getUniqueId())) {
              if (ObjectUtils.equals(specification.getParent(), targetContextSpec)) {
                // Our target
                return ImmutableSet.of(TARGET_SPECIFICATION_OBJECT, TARGET_SPECIFICATION_UNIQUE);
              }
            }
          } else {
            if (specification.getUniqueId() != null) {
              if (specification.getUniqueId().isLatest()) {
                if (target.getUniqueId().equalObjectId(specification.getUniqueId())) {
                  if (ObjectUtils.equals(specification.getParent(), targetContextSpec)) {
                    // Our target at object specification
                    return Collections.singleton(TARGET_SPECIFICATION_OBJECT);
                  }
                }
              } else {
                if (target.getUniqueId().equals(specification.getUniqueId())) {
                  if (ObjectUtils.equals(specification.getParent(), targetContextSpec)) {
                    // Our target at unique specification
                    return Collections.singleton(TARGET_SPECIFICATION_UNIQUE);
                  }
                }
              }
            }
          }
          // Not our target
          return null;
        }

      };
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      for (final ValueRequirement nestedRequirement : calcConfig.getSpecificRequirements()) {
        final Set<String> targetType = nestedRequirement.getTargetReference().accept(getTargetType);
        if (targetType != null) {
          // The properties on the outputs are based directly on the constraints used to specify the nested view definition. We can't
          // get the strict value specifications because we don't know how those requirements will compile because we don't know the
          // valuation date and the graph building behavior of the functions involved might be valuation date dependent - what if the
          // pricing currency changes over time for example; which do we use for the time series. This isn't always the case, but we'll
          // ignore the forms where we should know the outcomes to avoid complicating matters. A higher priority function should be
          // used to enforce any necessary constraints based on the target's properties.
          final ValueProperties.Builder properties = createValueProperties(historicalTarget);
          properties.with(VALUE_PROPERTY, nestedRequirement.getValueName());
          final ValueProperties nestedConstraints = nestedRequirement.getConstraints();
          if (nestedConstraints.getProperties() != null) {
            for (final String propertyName : nestedConstraints.getProperties()) {
              final Set<String> propertyValues = nestedConstraints.getValues(propertyName);
              final String passthroughName = PASSTHROUGH_PREFIX + propertyName;
              if (propertyValues == null) {
                properties.withAny(passthroughName);
              } else {
                properties.with(passthroughName, propertyValues);
              }
              if (nestedConstraints.isOptional(propertyName)) {
                properties.withOptional(passthroughName);
              }
            }
          }
          results.add(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, targetSpec, properties.get()));
        }
      }
      return results;
    } else {
      return null;
    }
  }

  // FunctionInvoker

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalViewEvaluationResult evaluationResult = (HistoricalViewEvaluationResult) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final ValueRequirement desiredValue : desiredValues) {
      final ValueRequirement requirement = getNestedRequirement(executionContext.getComputationTargetResolver(), target, desiredValue.getConstraints());
      if (requirement != null) {
        @SuppressWarnings("rawtypes")
        final TimeSeries ts = evaluationResult.getTimeSeries(requirement);
        if (ts != null) {
          results.add(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), targetSpec, desiredValue.getConstraints()), ts));
        } else {
          s_logger.warn("Nested requirement {} did not produce a time series for {}", requirement, desiredValue);
        }
      } else {
        s_logger.error("Couldn't produce nested requirement for {}", desiredValue);
      }
    }
    return results;
  }

}
