/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.lang.reflect.Array;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;

/**
 * Builder for converting VolatilityCubeDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilityCubeDefinition.class)
public class VolatilityCubeDefinitionFudgeBuilder implements FudgeBuilder<VolatilityCubeDefinition<Object, Object, Object>> {
  /** The definition name field */
  private static final String NAME_FIELD = "name";
  /** The xs field */
  private static final String XS_FIELD = "xs";
  /** The ys field */
  private static final String YS_FIELD = "ys";
  /** The zs field */
  private static final String ZS_FIELD = "zs";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilityCubeDefinition<Object, Object, Object> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(NAME_FIELD, object.getName());
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
    for (final Object z : object.getZs()) {
      if (z instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, ZS_FIELD, null, z);
      } else {
        message.add(ZS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(z), z.getClass()));
      }
    }
    return message;
  }

  @Override
  public VolatilityCubeDefinition<Object, Object, Object> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final List<FudgeField> xsFields = message.getAllByName(XS_FIELD);
    final Object firstX = deserializer.fieldValueToObject(xsFields.get(0));
    final Object[] xs = (Object[]) Array.newInstance(firstX.getClass(), xsFields.size());
    int i = 0;
    for (final FudgeField xField : xsFields) {
      final Object x = deserializer.fieldValueToObject(xField);
      xs[i] = x;
      i++;
    }
    final List<FudgeField> ysFields = message.getAllByName(YS_FIELD);
    final Object firstY = deserializer.fieldValueToObject(ysFields.get(0));
    final Object[] ys = (Object[]) Array.newInstance(firstY.getClass(), ysFields.size());
    int j = 0;
    for (final FudgeField yField : ysFields) {
      final Object y = deserializer.fieldValueToObject(yField);
      ys[j] = y;
      j++;
    }
    final List<FudgeField> zsFields = message.getAllByName(ZS_FIELD);
    final Object firstZ = deserializer.fieldValueToObject(zsFields.get(0));
    final Object[] zs = (Object[]) Array.newInstance(firstZ.getClass(), zsFields.size());
    int k = 0;
    for (final FudgeField zField : zsFields) {
      final Object z = deserializer.fieldValueToObject(zField);
      zs[k] = z;
      k++;
    }
    return new VolatilityCubeDefinition<>(name, xs, ys, zs);
  }

}
