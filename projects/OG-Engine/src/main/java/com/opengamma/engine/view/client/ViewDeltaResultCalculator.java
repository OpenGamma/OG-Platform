/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.DeltaDefinition;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.util.tuple.Pair;

/**
 * Produces {@link ViewDeltaResultModel} instances by comparing two {@link ViewComputationResultModel}.
 */
public class ViewDeltaResultCalculator {

  /**
   * Computes the delta between and old and new results.
   * 
   * @param viewDefinition the view definition to which the results apply
   * @param previousResult the previous result
   * @param result the new result
   * @return the delta between the two results, not null
   */
  public static ViewDeltaResultModel computeDeltaModel(ViewDefinition viewDefinition, ViewResultModel previousResult, ViewResultModel result) {
    InMemoryViewDeltaResultModel deltaModel = new InMemoryViewDeltaResultModel();
    deltaModel.setViewCycleExecutionOptions(result.getViewCycleExecutionOptions());
    deltaModel.setCalculationTime(result.getCalculationTime());
    deltaModel.setCalculationDuration(result.getCalculationDuration());
    deltaModel.setVersionCorrection(result.getVersionCorrection());
    deltaModel.setViewCycleId(result.getViewCycleId());
    deltaModel.setViewProcessId(result.getViewProcessId());
    if (previousResult != null) {
      deltaModel.setPreviousCalculationTime(previousResult.getCalculationTime());
    }
    for (String calcConfigName : result.getCalculationConfigurationNames()) {
      final DeltaDefinition deltaDefinition = viewDefinition.getCalculationConfiguration(calcConfigName).getDeltaDefinition();
      final ViewCalculationResultModel resultCalcModel = result.getCalculationResult(calcConfigName);
      final ViewCalculationResultModel previousCalcModel = previousResult != null ? previousResult.getCalculationResult(calcConfigName) : null;
      for (ComputationTargetSpecification targetSpec : resultCalcModel.getAllTargets()) {
        computeDeltaModel(deltaDefinition, deltaModel, targetSpec, calcConfigName, previousCalcModel, resultCalcModel);
      }
    }
    return deltaModel;
  }

  private static void computeDeltaModel(DeltaDefinition deltaDefinition, InMemoryViewDeltaResultModel deltaModel, ComputationTargetSpecification targetSpec,
      String calcConfigName, ViewCalculationResultModel previousCalcModel, ViewCalculationResultModel resultCalcModel) {
    final Map<Pair<String, ValueProperties>, ComputedValueResult> resultValues = resultCalcModel.getValues(targetSpec);
    if (resultValues != null) {
      if (previousCalcModel == null) {
        // Everything is new/delta because this is a new calculation context.
        for (Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> resultEntry : resultValues.entrySet()) {
          deltaModel.addValue(calcConfigName, resultEntry.getValue());
        }
      } else {
        final Map<Pair<String, ValueProperties>, ComputedValueResult> previousValues = previousCalcModel.getValues(targetSpec);
        if (previousValues == null) {
          // Everything is new/delta because this is a new target.
          for (ComputedValueResult result : resultValues.values()) {
            deltaModel.addValue(calcConfigName, result);
          }
        } else {
          // Have to individual delta.
          for (Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> resultEntry : resultValues.entrySet()) {
            ComputedValueResult resultValue = resultEntry.getValue();
            ComputedValueResult previousValue = previousValues.get(resultEntry.getKey());
            // REVIEW jonathan 2010-05-07 -- The previous value that we're comparing with is the value from the last
            // computation cycle, not the value that we last emitted as a delta. It is therefore important that the
            // DeltaComparers take this into account in their implementation of isDelta. E.g. they should compare the
            // values after truncation to the required decimal place, rather than testing whether the difference of the
            // full values is greater than some threshold; this way, there will always be a point beyond which a change
            // is detected, even in the event of gradual creep.
            if (deltaDefinition.isDelta(previousValue, resultValue) || !ObjectUtils.equals(previousValue.getAggregatedExecutionLog(), resultValue.getAggregatedExecutionLog())) {
              deltaModel.addValue(calcConfigName, resultEntry.getValue());
            }
          }
        }
      }
    }
  }

}
