/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(FuturePriceCurveDefinition.class)
public class FuturePriceCurveDefinitionFudgeBuilder implements FudgeBuilder<FuturePriceCurveDefinition<?>> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FuturePriceCurveDefinition<?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    for (final Object x : object.getXs()) {
      if (x instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, "xs", null, x);
      } else {
        message.add("xs", null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    return message;
  }

  @Override
  public FuturePriceCurveDefinition<?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueIdentifiable target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    final String name = message.getString("name");
    final List<FudgeField> xsFields = message.getAllByName("xs");
    final List<Object> xs = new ArrayList<Object>();
    for (final FudgeField xField : xsFields) {
      final Object x = deserializer.fieldValueToObject(xField);
      xs.add(x);
    }
    return FuturePriceCurveDefinition.of(name, target, xs);
  }

}
