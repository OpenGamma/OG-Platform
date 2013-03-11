/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.batch.BatchRunWriter;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListenerFactory;

/**
 * 
 */
public class BatchDbViewResultListenerFactory implements ViewResultListenerFactory {

  private BatchRunWriter _batchRunWriter;

  @Override
  public ViewResultListener createViewResultListener() {
    return new BatchDbViewResultListener(_batchRunWriter);
  }

  public BatchRunWriter getBatchRunMaster() {
    return _batchRunWriter;
  }

  public void setBatchRunMaster(BatchRunWriter batchRunWriter) {
    this._batchRunWriter = batchRunWriter;
  }
}
