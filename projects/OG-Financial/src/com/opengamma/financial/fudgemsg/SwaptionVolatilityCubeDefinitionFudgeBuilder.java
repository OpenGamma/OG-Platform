/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeDefinition;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(SwaptionVolatilityCubeDefinition.class)
public class SwaptionVolatilityCubeDefinitionFudgeBuilder implements FudgeBuilder<SwaptionVolatilityCubeDefinition<?, ?, ?>> {
  private static final String NAME_FIELD = "name";
  private static final String TARGET_FIELD = "target";
  private static final String XS_FIELD = "xs";
  private static final String YS_FIELD = "ys";
  private static final String ZS_FIELD = "zs";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwaptionVolatilityCubeDefinition<?, ?, ?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(NAME_FIELD, object.getName());
    message.add(TARGET_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    for (final Object x : object.getXs()) {
      if (x instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, XS_FIELD, null, x);
      } else {
        message.add(XS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    for (final Object y : object.getYs()) {
      if (y instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, YS_FIELD, null, y);
      } else {
        message.add(YS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(y), y.getClass()));
      }
    }
    for (final Object z : object.getXs()) {
      if (z instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, ZS_FIELD, null, z);
      } else {
        message.add(ZS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(z), z.getClass()));
      }
    }
    return message;
  }

  @Override
  public SwaptionVolatilityCubeDefinition<?, ?, ?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final UniqueIdentifiable target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName(TARGET_FIELD));
    final List<FudgeField> xFields = message.getAllByName(XS_FIELD);
    final List<FudgeField> yFields = message.getAllByName(YS_FIELD);
    final List<FudgeField> zFields = message.getAllByName(ZS_FIELD);
    final List<Object> xs = new ArrayList<Object>();
    for (final FudgeField field : xFields) {
      final Object x = deserializer.fieldValueToObject(field);
      xs.add(x);
    }
    final List<Object> ys = new ArrayList<Object>();
    for (final FudgeField field : yFields) {
      final Object y = deserializer.fieldValueToObject(field);
      ys.add(y);
    }
    final List<Object> zs = new ArrayList<Object>();
    for (final FudgeField field : zFields) {
      final Object z = deserializer.fieldValueToObject(field);
      zs.add(z);
    }
    return new SwaptionVolatilityCubeDefinition<Object, Object, Object>(name, target, xs.toArray(), ys.toArray(), zs.toArray());
  }

}
