/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * 
 */
public class ViewResultModelMergeUtils {

  /**
   * Restricted constructor
   */
  protected ViewResultModelMergeUtils() {
  }
  
  //-------------------------------------------------------------------------
  public static void merge(ViewResultModel source, InMemoryViewResultModel destination) {
    destination.setValuationTime(source.getValuationTime());
    destination.setCalculationTime(source.getCalculationTime());
    destination.setCalculationDuration(source.getCalculationDuration());
    destination.setVersionCorrection(source.getVersionCorrection());
    destination.setViewCycleId(source.getViewCycleId());
    destination.setViewProcessId(source.getViewProcessId());
    
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
