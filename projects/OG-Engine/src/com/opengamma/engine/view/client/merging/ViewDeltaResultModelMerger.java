/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.InMemoryViewDeltaResultModel;

/**
 * Provides the ability to merge {@link ViewDeltaResultModel} instances.
 */
public class ViewDeltaResultModelMerger {

  private InMemoryViewDeltaResultModel _currentMergedResult;
  
  /**
   * Adds a new result.
   * 
   * @param newResult  the new result to merge
   */
  public void merge(ViewDeltaResultModel newResult) {
    if (_currentMergedResult == null) {
      // Start of a new result
      _currentMergedResult = new InMemoryViewDeltaResultModel();
      _currentMergedResult.setPreviousResultTimestamp(newResult.getPreviousResultTimestamp());
      _currentMergedResult.setCalculationConfigurationNames(newResult.getCalculationConfigurationNames());
    }
    _currentMergedResult.setValuationTime(newResult.getValuationTime());
    _currentMergedResult.setResultTimestamp(newResult.getResultTimestamp());
    _currentMergedResult.ensureCalculationConfigurationNames(newResult.getCalculationConfigurationNames());
    
    for (ComputationTargetSpecification targetSpec : newResult.getAllTargets()) {
      for (String calcConfigName : newResult.getCalculationConfigurationNames()) {
        ViewCalculationResultModel resultCalcModel = newResult.getCalculationResult(calcConfigName);
        Map<String, ComputedValue> resultValues = resultCalcModel.getValues(targetSpec);
        if (resultValues == null) {
          continue;
        }
        for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
          _currentMergedResult.addValue(calcConfigName, resultEntry.getValue());
        }
      }
    }
  }

  /**
   * Retrieves the latest merged result.
   * 
   * @return  the latest merged result, or {@link null} to indicate no change
   */
  public ViewDeltaResultModel getLatestResult() {
    if (_currentMergedResult == null) {
      return null;
    }
    if (_currentMergedResult.getAllTargets().size() == 0) {
      // No changes
      return null;
    }
    
    return _currentMergedResult;
  }

}
