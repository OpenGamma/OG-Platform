/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;

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
    _currentMergedResult.setViewCycleId(newResult.getViewCycleId());
    _currentMergedResult.setViewProcessId(newResult.getViewProcessId());
    _currentMergedResult.ensureCalculationConfigurationNames(newResult.getCalculationConfigurationNames());
    
    for (ComputationTargetSpecification targetSpec : newResult.getAllTargets()) {
      for (String calcConfigName : newResult.getCalculationConfigurationNames()) {
        ViewCalculationResultModel resultCalcModel = newResult.getCalculationResult(calcConfigName);
        Collection<ComputedValue> resultValues = resultCalcModel.getAllValues(targetSpec);
        if (resultValues == null) {
          continue;
        }
        for (ComputedValue result : resultValues) {
          _currentMergedResult.addValue(calcConfigName, result);
        }
      }
    }
  }

  /**
   * Retrieves the latest merged result.
   * 
   * @return  the latest merged result
   */
  public ViewDeltaResultModel getLatestResult() {
    return _currentMergedResult;
  }

}
