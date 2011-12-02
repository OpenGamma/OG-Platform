/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.UniqueId;

/**
 * Change listener for a single view definition which notifies a computation job.
 */
public class ViewDefinitionChangeListener implements ChangeListener {

  private final ViewComputationJob _computationJob;
  private final UniqueId _viewDefinitionId;

  public ViewDefinitionChangeListener(ViewComputationJob computationJob, UniqueId viewDefinitionId) {
    _computationJob = computationJob;
    _viewDefinitionId = viewDefinitionId;
  }

  @Override
  public void entityChanged(ChangeEvent event) { 
    if (getViewDefinitionId().isVersioned()) {
      // TODO: probably still interested in corrections, but would need to update the computation job with the new ID
      // Locked to a specific version
      return;
    }
    if (event.getBeforeId() == null) {
      // View definition created 
      return;
    }
    if (event.getAfterId() == null) {
      // View definition could have been deleted - do we want to stop the process?
      return;
    }
    if (event.getBeforeId().getObjectId().equals(getViewDefinitionId().getObjectId())) {
      getViewComputationJob().dirtyViewDefinition();
    }
  }

  private UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }

  private ViewComputationJob getViewComputationJob() {
    return _computationJob;
  }

}
