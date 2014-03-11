/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.surface.SurfaceDefinition;

/**
 * Builder for converting SurfaceDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(SurfaceDefinition.class)
public class SurfaceDefinitionFudgeBuilder implements FudgeBuilder<SurfaceDefinition<?, ?>> {
  /** The definition name field */
  private static final String NAME_FIELD = "name";
  /** The xs field */
  private static final String XS_FIELD = "xs";
  /** The ys field */
  private static final String YS_FIELD = "ys";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SurfaceDefinition<?, ?> object) {
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
    return message;
  }

  @Override
  public SurfaceDefinition<?, ?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
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
    return new SurfaceDefinition<>(name, xs, ys);
  }

}
