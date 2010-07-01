/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.livedata.rest;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.livedata.user.OverrideLiveData;
import com.opengamma.id.UniqueIdentifier;

/**
 * REST interface to the OverrideLiveData class.
 */
@Path("/livedata")
public class OverrideLiveDataResource {

  private final OverrideLiveData _liveData;
  private final FudgeContext _fudgeContext;

  public OverrideLiveDataResource(final OverrideLiveData liveData) {
    _liveData = liveData;
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  }

  protected OverrideLiveData getLiveData() {
    return _liveData;
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
      final Object value = getLiveData().getValue(_valueRequirement);
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
      getLiveData().putValue(_valueRequirement, context.fieldValueToObject(data.getMessage().getByName("value")));
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

  private ValueRequirement makeValueRequirement(final String valueName, final ComputationTargetType type,
      final String identifier) {
    return new ValueRequirement(valueName, type, UniqueIdentifier.parse(identifier));
  }

  @Path("{name}/PRIMITIVE/{identifier}")
  public Value primitiveValue(@PathParam("name") final String name, @PathParam("identifier") final String identifier) {
    return new Value(makeValueRequirement(name, ComputationTargetType.PRIMITIVE, identifier));
  }

  @Path("{name}/SECURITY/{identifier}")
  public Value securityValue(@PathParam("name") final String name, @PathParam("identifier") final String identifier) {
    return new Value(makeValueRequirement(name, ComputationTargetType.SECURITY, identifier));
  }

  @GET
  public FudgeMsgEnvelope getAllValues() {
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer message = context.newMessage();
    final MutableFudgeFieldContainer livedata = context.newMessage();
    for (Map.Entry<ValueRequirement, Object> value : getLiveData().getAllValues().entrySet()) {
      context.objectToFudgeMsg(livedata, null, null, new ComputedValue(new ValueSpecification(value.getKey()), value
          .getValue()));
    }
    message.add("livedata", livedata);
    return new FudgeMsgEnvelope(message);
  }

  // Even bigger hack to allow values to be deleted

  @POST
  @Path("removeAll")
  public void removeAllValues(final FudgeMsgEnvelope data) {
    getLiveData().removeAllValues();
  }

}
