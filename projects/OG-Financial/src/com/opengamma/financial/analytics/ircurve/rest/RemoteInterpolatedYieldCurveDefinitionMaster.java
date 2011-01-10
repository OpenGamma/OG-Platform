/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RemoteInterpolatedYieldCurveDefinitionMaster implements InterpolatedYieldCurveDefinitionMaster {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteInterpolatedYieldCurveDefinitionMaster(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  protected FudgeContext getFudgeContext() {
    return getRestClient().getFudgeContext();
  }

  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  protected FudgeDeserializationContext getFudgeDeserializationContext() {
    return new FudgeDeserializationContext(getFudgeContext());
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  private UniqueIdentifier getIdentifier(FudgeFieldContainer msg) {
    final FudgeField uidField = msg.getByName("uniqueId");
    if (uidField == null) {
      return null;
    }
    return UniqueIdentifier.fromFudgeMsg(msg.getFieldValue(FudgeFieldContainer.class, uidField));
  }

  public YieldCurveDefinitionDocument postDefinition(final YieldCurveDefinitionDocument document, final String path) {
    final FudgeSerializationContext sctx = getFudgeSerializationContext();
    final MutableFudgeFieldContainer req = sctx.newMessage();
    sctx.objectToFudgeMsgWithClassHeaders(req, "definition", null, document.getYieldCurveDefinition(), YieldCurveDefinition.class);
    try {
      final FudgeMsgEnvelope respEnv = getRestClient().post(getTargetBase().resolve(path), req);
      if (respEnv == null) {
        throw new IllegalArgumentException("Returned envelope was null");
      }
      UniqueIdentifier uid = getIdentifier(respEnv.getMessage());
      if (uid == null) {
        throw new IllegalArgumentException("No unique identifier returned");
      }
      document.setUniqueId(uid);
      return document;
    } catch (RestRuntimeException ex) {
      throw new IllegalArgumentException("Error adding document", ex);
    }
  }

  @Override
  public YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    return postDefinition(document, "add");
  }

  @Override
  public YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    return postDefinition(document, "addOrUpdate");
  }

  @Override
  public YieldCurveDefinitionDocument correct(final YieldCurveDefinitionDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public YieldCurveDefinitionDocument get(UniqueIdentifier uid) {
    final FudgeFieldContainer msg = getRestClient().getMsg(getTargetBase().resolveBase("curves").resolve(uid.toString()));
    if (msg == null) {
      throw new DataNotFoundException("uid=" + uid);
    }
    uid = getIdentifier(msg);
    if (uid == null) {
      throw new DataNotFoundException("uid=" + uid);
    }
    final FudgeField definitionField = msg.getByName("definition");
    if (definitionField == null) {
      throw new DataNotFoundException("uid=" + uid);
    }
    return new YieldCurveDefinitionDocument(uid, getFudgeDeserializationContext().fieldValueToObject(YieldCurveDefinition.class, definitionField));
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    try {
      getRestClient().delete(getTargetBase().resolveBase("curves").resolve(uid.toString()));
    } catch (RestRuntimeException ex) {
      if (ex.getStatusCode() == 404) {
        throw new DataNotFoundException("uid=" + uid, ex);
      } else {
        throw new IllegalArgumentException("uid=" + uid, ex);
      }
    }
  }

  @Override
  public YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    try {
      final FudgeSerializationContext sctx = getFudgeSerializationContext();
      final MutableFudgeFieldContainer req = sctx.newMessage();
      sctx.objectToFudgeMsgWithClassHeaders(req, "definition", null, document.getYieldCurveDefinition(), YieldCurveDefinition.class);
      getRestClient().put(getTargetBase().resolveBase("curves").resolve(document.getUniqueId().toString()), req);
      return document;
    } catch (RestRuntimeException ex) {
      if (ex.getStatusCode() == 404) {
        throw new DataNotFoundException("uid=" + document.getUniqueId(), ex);
      } else {
        throw new IllegalArgumentException("uid=" + document.getUniqueId(), ex);
      }
    }
  }

}
