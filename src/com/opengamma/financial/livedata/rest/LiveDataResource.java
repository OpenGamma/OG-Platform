/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.livedata.user.InMemoryUserSnapshotProvider;

/**
 * RESTful resource for live data.
 */
public class LiveDataResource {

  private final InMemoryUserSnapshotProvider _livedata;

  private final FudgeContext _fudgeContext;

  public LiveDataResource(final InMemoryUserSnapshotProvider livedata) {
    _livedata = livedata;
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  }

  public InMemoryUserSnapshotProvider getLiveData() {
    return _livedata;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Stub class for retrieving/setting/deleting a Live Data value
   */
  public final class Value {

    private final ValueRequirement _valueRequirement;

    private Value(final ValueRequirement valueRequirement) {
      _valueRequirement = valueRequirement;
    }

    @GET
    public FudgeMsgEnvelope get() {
      final Object value = getLiveData().getCurrentValue(_valueRequirement);
      if (value == null) {
        return null;
      }
      final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
      final MutableFudgeFieldContainer msg = context.newMessage();
      context.objectToFudgeMsg(msg, "value", null, value);
      return new FudgeMsgEnvelope(msg);
    }

    @PUT
    public FudgeMsgEnvelope put(final FudgeMsgEnvelope data) {
      final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
      getLiveData().addValue(_valueRequirement, context.fieldValueToObject(data.getMessage().getByName("value")));
      final MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
      msg.add("uniqueIdentifier", _valueRequirement.getTargetSpecification().getUniqueIdentifier().toFudgeMsg(
          getFudgeContext()));
      return new FudgeMsgEnvelope(msg);
    }

    @DELETE
    public void delete() {
      getLiveData().removeValue(_valueRequirement);
    }

  }

  @Path("{name}/PRIMITIVE/{identifier}")
  public Value primitiveValue(@PathParam("name") final String name, @PathParam("identifier") final String identifier) {
    return new Value(getLiveData().makeValueRequirement(name, ComputationTargetType.PRIMITIVE, identifier));
  }

  @Path("{name}/SECURITY/{identifier}")
  public Value securityValue(@PathParam("name") final String name, @PathParam("identifier") final String identifier) {
    return new Value(getLiveData().makeValueRequirement(name, ComputationTargetType.SECURITY, identifier));
  }

  @GET
  public FudgeMsgEnvelope getAllLiveData() {
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer msgOuter = getFudgeContext().newMessage();
    final MutableFudgeFieldContainer msgInner = getFudgeContext().newMessage();
    for (ValueRequirement valueRequirement : getLiveData().getAllValueKeys()) {
      context.objectToFudgeMsg(msgInner, null, null, getLiveData().getCurrentValue(valueRequirement));
    }
    msgOuter.add("livedata", msgInner);
    return new FudgeMsgEnvelope(msgOuter);
  }

}
