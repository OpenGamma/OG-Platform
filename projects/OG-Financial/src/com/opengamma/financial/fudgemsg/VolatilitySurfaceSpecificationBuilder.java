/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting VolatilitySurfaceSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceSpecification.class)
public class VolatilitySurfaceSpecificationBuilder implements FudgeBuilder<VolatilitySurfaceSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, VolatilitySurfaceSpecification object) {
    MutableFudgeMsg message = serializer.newMessage();
    // the following forces it not to use a secondary type if one is available.
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    // for compatibility with old code, remove.
    if (object.getTarget() instanceof Currency) {
      message.add("currency", object.getTarget());
    } else {
      // just for now...
      message.add("currency", Currency.USD);
    }
    message.add("name", object.getName());
    serializer.addToMessageWithClassHeaders(message, "surfaceInstrumentProvider", null, object.getSurfaceInstrumentProvider());
    return message; 
  }

  @Override
  public VolatilitySurfaceSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    UniqueIdentifiable target;
    if (!message.hasField("target")) {
      target = deserializer.fieldValueToObject(Currency.class, message.getByName("currency"));
    } else {
//      try {
      target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
//      } catch (Exception fre) { // arghhhhhh
//        target = Currency.of(message.getString("target"));
//      }
    }
    String name = message.getString("name");
    FudgeField field = message.getByName("surfaceInstrumentProvider");
    SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider = (SurfaceInstrumentProvider<?, ?>) deserializer.fieldValueToObject(field);
    return new VolatilitySurfaceSpecification(name, target, surfaceInstrumentProvider);
  }

}
