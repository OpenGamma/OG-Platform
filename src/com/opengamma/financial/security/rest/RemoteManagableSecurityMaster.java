/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import java.util.Collection;
import java.util.Set;

import javax.time.InstantProvider;

import org.apache.commons.lang.NotImplementedException;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.AddSecurityRequest;
import com.opengamma.financial.security.ManageableSecurityMaster;
import com.opengamma.financial.security.UpdateSecurityRequest;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Provides access to a remote {@link ManageableSecurityMaster}.
 */
public class RemoteManagableSecurityMaster implements ManageableSecurityMaster {

  private final FudgeContext _fudgeContext;
  private final RestTarget _baseTarget;
  private final RestClient _restClient;
  
  public RemoteManagableSecurityMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
    _restClient = RestClient.getInstance(fudgeContext, null);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isManagerFor(UniqueIdentifier uid) {
    return false;
  }

  @Override
  public boolean isModificationSupported() {
    return false;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    FudgeMsgEnvelope response = _restClient.getMsgEnvelope(_baseTarget.resolve(uid.toString()));
    
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    return deserializationContext.fudgeMsgToObject(Security.class, response.getMessage());
  }

  @Override
  public Security getSecurity(UniqueIdentifier uid, InstantProvider asAt, InstantProvider asViewedAt) {
    throw new NotImplementedException();
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    throw new NotImplementedException();
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    throw new NotImplementedException();
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    throw new NotImplementedException();
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier addSecurity(AddSecurityRequest request) {
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(_fudgeContext);
    MutableFudgeFieldContainer msg = serializationContext.objectToFudgeMsg(request);
    FudgeMsgEnvelope response = _restClient.post(_baseTarget, new FudgeMsgEnvelope(msg));
    
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    UniqueIdentifier uid = deserializationContext.fudgeMsgToObject(UniqueIdentifier.class, response.getMessage());
    return uid;
  }

  @Override
  public UniqueIdentifier updateSecurity(UpdateSecurityRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public UniqueIdentifier removeSecurity(UniqueIdentifier uid) {
    throw new NotImplementedException();
  }

}
