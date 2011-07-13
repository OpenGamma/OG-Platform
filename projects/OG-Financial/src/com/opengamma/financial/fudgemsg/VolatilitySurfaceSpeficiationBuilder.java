/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting VolatilitySurfaceSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceSpecification.class)
public class VolatilitySurfaceSpeficiationBuilder implements FudgeBuilder<VolatilitySurfaceSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, VolatilitySurfaceSpecification object) {
    MutableFudgeMsg message = context.newMessage();
    // the following forces it not to use a secondary type if one is available.
    message.add("target", FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    context.addToMessage(message, "surfaceInstrumentProvider", null, object.getSurfaceInstrumentProvider());
    return message; 
  }

  @Override
  public VolatilitySurfaceSpecification buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    UniqueIdentifiable target;
    if (message.hasField("currency")) {
      target = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    } else {
//      try {
        target = context.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
//      } catch (Exception fre) { // arghhhhhh
//        target = Currency.of(message.getString("target"));
//      }
    }
    String name = message.getString("name");
    FudgeField field = message.getByName("surfaceInstrumentProvider");
    SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider = context.fieldValueToObject(SurfaceInstrumentProvider.class, field);
    return new VolatilitySurfaceSpecification(name, target, surfaceInstrumentProvider);
  }

}
