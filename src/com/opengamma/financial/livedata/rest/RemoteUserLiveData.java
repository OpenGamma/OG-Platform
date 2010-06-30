/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Remote interface to the user Live Data.
 */
public class RemoteUserLiveData {

  private final FudgeContext _fudgeContext;
  private final RestTarget _restTarget;
  private final RestClient _restClient;

  public RemoteUserLiveData(final FudgeContext fudgeContext, final RestTarget restTarget) {
    _fudgeContext = fudgeContext;
    _restTarget = restTarget;
    _restClient = RestClient.getInstance(getFudgeContext(), null);
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected RestTarget getRestTarget() {
    return _restTarget;
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  protected FudgeDeserializationContext getFudgeDeserializationContext() {
    return new FudgeDeserializationContext(getFudgeContext());
  }

  public Collection<ComputedValue> getAllLiveData() {
    final FudgeDeserializationContext context = getFudgeDeserializationContext();
    final FudgeFieldContainer response = getRestClient().getMsg(getRestTarget());
    final FudgeFieldContainer valueMsg = response.getFieldValue(FudgeFieldContainer.class, response
        .getByName("livedata"));
    final List<ComputedValue> valueList = new ArrayList<ComputedValue>(valueMsg.getNumFields());
    for (FudgeField field : valueMsg) {
      valueList.add(context.fieldValueToObject(ComputedValue.class, field));
    }
    return valueList;
  }

  private RestTarget getValueTarget(final String name, final ComputationTargetType type, final String identifier) {
    return getRestTarget().resolveBase(name).resolveBase(type.name()).resolve(identifier);
  }

  public UniqueIdentifier putValue(final String name, final ComputationTargetType type, final String identifier,
      final Object value) {
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, "value", null, value);
    final FudgeMsgEnvelope fme = getRestClient().put(getValueTarget(name, type, identifier), msg);
    final FudgeField field = fme.getMessage().getByName("uniqueIdentifier");
    return UniqueIdentifier.fromFudgeMsg(fme.getMessage().getFieldValue(FudgeFieldContainer.class, field));
  }

  public UniqueIdentifier[] putValues(final String name, final ComputationTargetType type, final String[] identifier,
      final Object[] value) {
    assert (identifier.length == value.length);
    // TODO: this is very inefficient; there should be a PUT request
    final UniqueIdentifier[] result = new UniqueIdentifier[identifier.length];
    for (int i = 0; i < identifier.length; i++) {
      result[i] = putValue(name, type, identifier[i], value[i]);
    }
    return result;
  }

  public Object getValue(final String name, final ComputationTargetType type, final String identifier) {
    return getRestClient().getSingleValue(Object.class, getValueTarget(name, type, identifier), "value");
  }

  public Object[] getValues(final String name, final ComputationTargetType type, final String[] identifiers) {
    // TODO: this is very inefficient; there should be a single GET request
    final Object[] result = new Object[identifiers.length];
    for (int i = 0; i < identifiers.length; i++) {
      result[i] = getValue(name, type, identifiers[i]);
    }
    return result;
  }

  public void deleteValue(final String name, final ComputationTargetType type, final String identifier) {
    getRestClient().delete(getValueTarget(name, type, identifier));
  }

  public void deleteValues(final String name, final ComputationTargetType type, final String[] identifiers) {
    // TODO: this is very inefficient; there should be a single DELETE request
    for (int i = 0; i < identifiers.length; i++) {
      deleteValue(name, type, identifiers[i]);
    }
  }

}
