/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
@FudgeBuilderFor(FuturePriceCurveSpecification.class)
public class FuturePriceCurveSpecificationFudgeBuilder implements FudgeBuilder<FuturePriceCurveSpecification> {
  /** Field for whether or not to use underlying securities to get expiry */
  private static final String USE_UNDERLYING_SECURITY_FOR_EXPIRY = "useUnderlyingSecurityForExpiry";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FuturePriceCurveSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    serializer.addToMessageWithClassHeaders(message, "curveInstrumentProvider", null, object.getCurveInstrumentProvider());
    message.add(USE_UNDERLYING_SECURITY_FOR_EXPIRY, object.isUseUnderlyingSecurityForExpiry());
    return message;
  }

  @Override
  public FuturePriceCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueIdentifiable target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    final String name = message.getString("name");
    final FudgeField field = message.getByName("curveInstrumentProvider");
    final FuturePriceCurveInstrumentProvider<?> curveInstrumentProvider = (FuturePriceCurveInstrumentProvider<?>) deserializer.fieldValueToObject(field);
    if (message.hasField(USE_UNDERLYING_SECURITY_FOR_EXPIRY)) {
      final boolean useUnderlyingForExpiry = message.getBoolean(USE_UNDERLYING_SECURITY_FOR_EXPIRY);
      return new FuturePriceCurveSpecification(name, target, curveInstrumentProvider, useUnderlyingForExpiry);
    }
    return new FuturePriceCurveSpecification(name, target, curveInstrumentProvider);
  }

}
