/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A server-side subscription result.
 */
public class SubscriptionResult {
  
  private final LiveDataSpecification _specFromClient;
  private final DistributionSpecification _distributionSpecification;
  private final LiveDataSubscriptionResult _result;
  private final RuntimeException _exception;
  
  public SubscriptionResult(LiveDataSpecification specFromClient,
                            DistributionSpecification distributionSpecification,
                            LiveDataSubscriptionResult result,
                            RuntimeException exception) {
    ArgumentChecker.notNull(specFromClient, "What data the client requested");
    ArgumentChecker.notNull(result, "Result");
    if (result != LiveDataSubscriptionResult.SUCCESS) {
      ArgumentChecker.notNull(exception, "Exception");
    } else {
      ArgumentChecker.notNull(distributionSpecification, "How the data will be distributed");
    }
    
    _specFromClient = specFromClient;
    _distributionSpecification = distributionSpecification;
    _result = result;
    _exception = exception;
  }
  
  public LiveDataSubscriptionResponse toResponse() {
    String msg = null;
    if (_exception != null) {
      msg = _exception.getMessage();
    }
    LiveDataSpecification fullyQualifiedSpec = null;
    String jmsTopic = null;
    if (_distributionSpecification != null) {
      fullyQualifiedSpec = _distributionSpecification.getFullyQualifiedLiveDataSpecification();
      jmsTopic = _distributionSpecification.getJmsTopic();
    }
    return new LiveDataSubscriptionResponse(
        _specFromClient,
        _result,
        msg,
        fullyQualifiedSpec,
        jmsTopic,
        null);
  }

  public LiveDataSpecification getSpecFromClient() {
    return _specFromClient;
  }

  public DistributionSpecification getDistributionSpecification() {
    return _distributionSpecification;
  }

  public LiveDataSubscriptionResult getResult() {
    return _result;
  }

  public RuntimeException getException() {
    return _exception;
  }
  
}
