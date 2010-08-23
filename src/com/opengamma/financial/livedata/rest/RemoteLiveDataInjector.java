/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link LiveDataInjector}.
 */
public class RemoteLiveDataInjector implements LiveDataInjector {

  private final FudgeContext _fudgeContext;
  private final RestTarget _baseTarget;
  private final RestClient _restClient;
  
  public RemoteLiveDataInjector(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
    _restClient = RestClient.getInstance(fudgeContext, null);
  }
  
  @Override
  public void addValue(ValueRequirement valueRequirement, Object value) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    ArgumentChecker.notNull(value, "value");
    _restClient.put(getValueRequirementTarget(valueRequirement), _fudgeContext.toFudgeMsg(value));
  }

  @Override
  public void removeValue(ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    _restClient.delete(getValueRequirementTarget(valueRequirement));
  }
  
  private RestTarget getValueRequirementTarget(ValueRequirement valueRequirement) {
    return _baseTarget
      .resolveBase(valueRequirement.getValueName())
      .resolveBase(valueRequirement.getTargetSpecification().getType().name())
      .resolveBase(valueRequirement.getTargetSpecification().getUniqueIdentifier().toString());
  }

}
