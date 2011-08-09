/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Client-side access to a remote batch master.
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
    final FudgeSerializer sctx = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg defnMsg = sctx.newMessage();
    sctx.addToMessage(defnMsg, "batch", null, batch);
    final RestTarget target = getTargetBase();
    getRestClient().post(target, defnMsg);
  }

}
