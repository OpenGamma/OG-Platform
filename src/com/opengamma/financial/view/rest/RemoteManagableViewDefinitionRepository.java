/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManagableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Provides access to a remote {@link ManagableViewDefinitionRepository}.
 */
public class RemoteManagableViewDefinitionRepository implements ManagableViewDefinitionRepository {

  private final FudgeContext _fudgeContext;
  private final RestClient _restClient;
  private final RestTarget _baseTarget;
  
  public RemoteManagableViewDefinitionRepository(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
    _restClient = RestClient.getInstance(fudgeContext, null);
  }
  
  //-------------------------------------------------------------------------
  // ViewDefinitionRepository implementation
  @Override
  public ViewDefinition getDefinition(String definitionName) {
    throw new NotImplementedException();
  }

  @Override
  public Set<String> getDefinitionNames() {
    throw new NotImplementedException();
  }
  
  //-------------------------------------------------------------------------
  // ManagableViewDefinitionRepository implementation
  @Override
  public boolean isModificationSupported() {
    return true;
  }
  
  @Override
  public void addViewDefinition(AddViewDefinitionRequest request) {
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(_fudgeContext);
    MutableFudgeFieldContainer msg = serializationContext.objectToFudgeMsg(request);
    _restClient.post(_baseTarget, new FudgeMsgEnvelope(msg));
  }
  
  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public void removeViewDefinition(String name) {
    throw new NotImplementedException();
  }
  
}
