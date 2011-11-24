/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.view.calc.ViewResultListenerFactory;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.masterdb.batch.BatchRunMaster;

public class BatchDbViewResultListenerFactory implements ViewResultListenerFactory {

  private BatchRunMaster _batchRunMaster;

  @Override
  public ViewResultListener createViewResultListener() {
    return new BatchDbViewResultListener(_batchRunMaster);
  }

  public BatchRunMaster getBatchRunMaster() {
    return _batchRunMaster;
  }

  public void setBatchRunMaster(BatchRunMaster batchRunMaster) {
    this._batchRunMaster = batchRunMaster;
  }
}
