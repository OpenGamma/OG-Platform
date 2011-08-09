/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;

/**
 * Change listener for a single view definition which notifies a computation job.
 */
public class ViewDefinitionChangeListener implements ChangeListener {

  private final ViewComputationJob _computationJob;
  private final String _viewDefinitionName;
  
  public ViewDefinitionChangeListener(ViewComputationJob computationJob, String viewDefinitionName) {
    _computationJob = computationJob;
    _viewDefinitionName = viewDefinitionName;
  }
  
  @Override
  public void entityChanged(ChangeEvent event) {
    if (event.getBeforeId() == null) {
      // View definition created 
      return;
    }
    if (event.getAfterId() == null) {
      // View definition could have been deleted - do we want to stop the process?
      return;
    }
    if (event.getBeforeId().getValue().equals(getViewDefinitionName())) {
      getViewComputationJob().dirtyViewDefinition();
    }
  }
  
  private String getViewDefinitionName() {
    return _viewDefinitionName;
  }
  
  private ViewComputationJob getViewComputationJob() {
    return _computationJob;
  }
  
}
