/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;


/**
 * Provides access to a remote {@link AdHocBatchDbManager}. 
 */
public class RemoteAdHocBatchDbManager implements AdHocBatchDbManager {

  private final RestClient _restClient;
  private final RestTarget _targetBase;
  
  public RemoteAdHocBatchDbManager(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }
  
  protected FudgeContext getFudgeContext() {
    return getRestClient().getFudgeContext();
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }
  
  //-------------------------------------------------------------------------
  
  @Override
  public void write(AdHocBatchResult batch) {
    final FudgeSerializationContext sctx = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer defnMsg = sctx.newMessage();
    sctx.objectToFudgeMsgWithClassHeaders(defnMsg, "batch", null, batch, AdHocBatchResult.class);
    final RestTarget target = getTargetBase();
    getRestClient().post(target, defnMsg);
  }
  
}
