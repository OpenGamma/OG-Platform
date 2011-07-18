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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(FuturePriceCurveSpecification.class)
public class FuturePriceCurveSpecificationBuilder implements FudgeBuilder<FuturePriceCurveSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final FuturePriceCurveSpecification object) {
    final MutableFudgeMsg message = context.newMessage();
    message.add("target", FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    context.addToMessageWithClassHeaders(message, "curveInstrumentProvider", null, object.getCurveInstrumentProvider());
    return message;
  }

  @Override
  public FuturePriceCurveSpecification buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final UniqueIdentifiable target = context.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    final String name = message.getString("name");
    final FudgeField field = message.getByName("curveInstrumentProvider");
    final FuturePriceCurveInstrumentProvider<?> curveInstrumentProvider = (FuturePriceCurveInstrumentProvider<?>) context.fieldValueToObject(field);
    return new FuturePriceCurveSpecification(name, target, curveInstrumentProvider);
  }

}
