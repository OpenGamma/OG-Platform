/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Determines which output value specifications have changed as a result of different execution option function parameters.
 */
public final class FunctionParametersDelta {

  /**
   * The "empty" delta instance, corresponding to no changes in function parameters.
   */
  public static final FunctionParametersDelta EMPTY = new FunctionParametersDelta(null);

  private final Collection<DistinctMarketDataSelector> _selectors;

  private FunctionParametersDelta(final Collection<DistinctMarketDataSelector> selectors) {
    _selectors = selectors;
  }

  /* package */Collection<DistinctMarketDataSelector> getSelectors() {
    return _selectors;
  }

  /**
   * Creates a delta of function parameterization between two cycle execution options.
   * 
   * @param firstCycleOptions the options for the first cycle, not null
   * @param secondCycleOptions the options for the second cycle, not null
   * @return the delta, not null
   */
  public static FunctionParametersDelta of(final ViewCycleExecutionOptions firstCycleOptions, final ViewCycleExecutionOptions secondCycleOptions) {
    return of(firstCycleOptions.getFunctionParameters(), secondCycleOptions.getFunctionParameters());
  }

  /**
   * Creates a delta of function parameterizations.
   * 
   * @param firstCycleParameters the parameterization for the first cycle, not null
   * @param secondCycleParameters the parameterization for the second cycle, not null
   * @return the delta, not null
   */
  public static FunctionParametersDelta of(final Map<DistinctMarketDataSelector, FunctionParameters> firstCycleParameters,
      final Map<DistinctMarketDataSelector, FunctionParameters> secondCycleParameters) {
    if (firstCycleParameters.isEmpty()) {
      if (secondCycleParameters.isEmpty()) {
        // No delta - no parameters
        return EMPTY;
      } else {
        // Delta is anything defined in the second parameter set
        return FunctionParametersDelta.of(secondCycleParameters.keySet());
      }
    } else {
      if (secondCycleParameters.isEmpty()) {
        // Delta is anything defined in the first parameter set
        return FunctionParametersDelta.of(firstCycleParameters.keySet());
      } else {
        // Delta is the negative intersection of market data selectors, plus anything  in the union with different parameters
        final ArrayList<DistinctMarketDataSelector> delta = new ArrayList<DistinctMarketDataSelector>(firstCycleParameters.size() + secondCycleParameters.size());
        int union = 0;
        for (Map.Entry<DistinctMarketDataSelector, FunctionParameters> first : firstCycleParameters.entrySet()) {
          final FunctionParameters secondValue = secondCycleParameters.get(first.getKey());
          if (secondValue != null) {
            // Intersection - include if parameters are different
            if (first.getValue().equals(secondValue)) {
              union++;
            } else {
              delta.add(first.getKey());
            }
          } else {
            // Left negative intersection - include
            delta.add(first.getKey());
          }
        }
        if (union != secondCycleParameters.size()) {
          // Include the right negative intersection
          for (DistinctMarketDataSelector secondKey : secondCycleParameters.keySet()) {
            if (!firstCycleParameters.containsKey(secondKey)) {
              delta.add(secondKey);
            }
          }
        }
        if (delta.isEmpty()) {
          return EMPTY;
        } else {
          return new FunctionParametersDelta(delta);
        }
      }
    }
  }

  /* package */static FunctionParametersDelta of(final Collection<DistinctMarketDataSelector> selectors) {
    return new FunctionParametersDelta(new ArrayList<DistinctMarketDataSelector>(selectors));
  }

  /**
   * Returns the value specifications that are directly dirtied by this delta. The dependency graph must be used to determine any derived values that are also invalidated as a result of these.
   * 
   * @param calcConfig the calculation configuration, not null
   * @param firstCycle the compiled view definition from the first cycle, not null
   * @param secondCycle the compiled view definition from the second cycle, not null
   * @return the dirty specifications, not null
   */
  public Set<ValueSpecification> getValueSpecifications(final String calcConfig, final CompiledViewDefinition firstCycle, final CompiledViewDefinition secondCycle) {
    if (getSelectors() == null) {
      // Nothing in the delta
      return Collections.emptySet();
    }
    final CompiledViewCalculationConfiguration firstCycleConfig = firstCycle.getCompiledCalculationConfiguration(calcConfig);
    if (firstCycleConfig == null) {
      return Collections.emptySet();
    }
    final CompiledViewCalculationConfiguration secondCycleConfig = secondCycle.getCompiledCalculationConfiguration(calcConfig);
    if (secondCycleConfig == null) {
      return Collections.emptySet();
    }
    return getValueSpecifications(firstCycleConfig.getMarketDataSelections(), secondCycleConfig.getMarketDataSelections());
  }

  private Set<ValueSpecification> getValueSpecifications(final Map<DistinctMarketDataSelector, Set<ValueSpecification>> firstCycle,
      final Map<DistinctMarketDataSelector, Set<ValueSpecification>> secondCycle) {
    final Set<ValueSpecification> delta = Sets.newHashSetWithExpectedSize(getSelectors().size());
    for (DistinctMarketDataSelector selector : getSelectors()) {
      Set<ValueSpecification> specifications = firstCycle.get(selector);
      if (specifications != null) {
        // Specifications set on the first cycle might not be set on the second - mark dirty
        delta.addAll(specifications);
      }
      specifications = secondCycle.get(selector);
      if (specifications != null) {
        // Specifications set on the second cycle might not have been set on the first - mark dirty
        delta.addAll(specifications);
      }
    }
    return delta;
  }

}
