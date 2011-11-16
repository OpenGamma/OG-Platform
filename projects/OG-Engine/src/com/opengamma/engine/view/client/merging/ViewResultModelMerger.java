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
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * Provides the ability to merge {@link ViewResultModel} instances.
 */
public class ViewResultModelMerger {

  private InMemoryViewResultModel _currentMergedResult;
  
  /**
   * Adds a new result.
   * 
   * @param newResult  the new result to merge
   */
  public void merge(ViewResultModel newResult) {
    if (_currentMergedResult == null) {
      // Start of a new result
      _currentMergedResult = new InMemoryViewDeltaResultModel();
      _currentMergedResult.setCalculationConfigurationNames(newResult.getCalculationConfigurationNames());
    }
    merge(newResult, _currentMergedResult);
  }

  /**
   * Retrieves the latest merged result.
   * 
   * @return  the latest merged result
   */
  public ViewResultModel getLatestResult() {
    return _currentMergedResult;
  }

  /*package*/ static void merge(ViewResultModel source, InMemoryViewResultModel destination) {
    destination.setValuationTime(source.getValuationTime());
    destination.setCalculationTime(source.getCalculationTime());
    destination.setCalculationDuration(source.getCalculationDuration());
    destination.setVersionCorrection(source.getVersionCorrection());
    destination.setViewCycleId(source.getViewCycleId());
    destination.setViewProcessId(source.getViewProcessId());
    destination.ensureCalculationConfigurationNames(source.getCalculationConfigurationNames());
    
    for (ComputationTargetSpecification targetSpec : source.getAllTargets()) {
      for (String calcConfigName : source.getCalculationConfigurationNames()) {
        ViewCalculationResultModel resultCalcModel = source.getCalculationResult(calcConfigName);
        Collection<ComputedValue> resultValues = resultCalcModel.getAllValues(targetSpec);
        if (resultValues == null) {
          continue;
        }
        for (ComputedValue result : resultValues) {
          destination.addValue(calcConfigName, result);
        }
      }
    }
  }
  
}
