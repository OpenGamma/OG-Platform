/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static com.google.common.collect.Maps.newHashMap;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Triple;

/**
 * Builder for converting VolatilityCubeData instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilityCubeData.class)
public class VolatilityCubeDataFudgeBuilder implements FudgeBuilder<VolatilityCubeData<?, ?, ?>> {
  private static final String TARGET_FIELD = "target";
  private static final String DEFINITION_FIELD = "definitionName";
  private static final String SPECIFICATION_FIELD = "specificationName";
  private static final String XS_FIELD = "xs";
  private static final String XS_SUBMESSAGE_FIELD = "xsSubMessage";
  private static final String YS_FIELD = "ys";
  private static final String YS_SUBMESSAGE_FIELD = "ysSubMessage";
  private static final String ZS_FIELD = "zs";
  private static final String ZS_SUBMESSAGE_FIELD = "zsSubMessage";
  private static final String X_FIELD = "x";
  private static final String Y_FIELD = "y";
  private static final String Z_FIELD = "z";
  private static final String VALUE_FIELD = "value";
  private static final String VALUES_FIELD = "values";
  private static final String X_LABEL_FIELD = "xLabel";
  private static final String Y_LABEL_FIELD = "yLabel";
  private static final String Z_LABEL_FIELD = "zLabel";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilityCubeData<?, ?, ?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    // the following forces it not to use a secondary type if one is available.
    message.add(TARGET_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    serializer.addToMessage(message, TARGET_FIELD, null, object.getTarget());
    message.add(DEFINITION_FIELD, object.getDefinitionName());
    message.add(SPECIFICATION_FIELD, object.getSpecificationName());
    MutableFudgeMsg xsSubMsg = message.addSubMessage(XS_SUBMESSAGE_FIELD, null);
    FudgeSerializer.addClassHeader(xsSubMsg, object.getXs().getClass().getComponentType());
    for (final Object x : object.getXs()) {
      if (x != null) {
        xsSubMsg.add(XS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    MutableFudgeMsg ysSubMsg = message.addSubMessage(YS_SUBMESSAGE_FIELD, null);
    FudgeSerializer.addClassHeader(ysSubMsg, object.getYs().getClass().getComponentType());
    for (final Object y : object.getYs()) {
      if (y != null) {
        ysSubMsg.add(YS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(y), y.getClass()));
      }
    }
    MutableFudgeMsg zsSubMsg = message.addSubMessage(ZS_SUBMESSAGE_FIELD, null);
    FudgeSerializer.addClassHeader(zsSubMsg, object.getZs().getClass().getComponentType());
    for (final Object z : object.getZs()) {
      if (z != null) {
        zsSubMsg.add(ZS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(z), z.getClass()));
      }
    }
    for (final Entry<?, Double> entry : object.asMap().entrySet()) {
      @SuppressWarnings("unchecked")
      final Triple<Object, Object, Object> triple = (Triple<Object, Object, Object>) entry.getKey();
      final MutableFudgeMsg subMessage = serializer.newMessage();
      if (triple.getFirst() != null && triple.getSecond() != null) {
        subMessage.add(X_FIELD, null, serializer.objectToFudgeMsg(triple.getFirst()));
        subMessage.add(Y_FIELD, null, serializer.objectToFudgeMsg(triple.getSecond()));
        subMessage.add(Y_FIELD, null, serializer.objectToFudgeMsg(triple.getThird()));
        subMessage.add(VALUE_FIELD, null, entry.getValue());
        message.add(VALUES_FIELD, null, subMessage);
      }
    }
    message.add(X_LABEL_FIELD, object.getXLabel());
    message.add(Y_LABEL_FIELD, object.getYLabel());
    message.add(Z_LABEL_FIELD, object.getZLabel());
    return message;
  }

  @Override
  public VolatilityCubeData<?, ?, ?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    UniqueIdentifiable target;
    target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName(TARGET_FIELD));
    final String definitionName = message.getString(DEFINITION_FIELD);
    final String specificationName = message.getString(SPECIFICATION_FIELD);
    Object[] xsArray;
    Object[] ysArray;
    Object[] zsArray;
    if (message.hasField(XS_SUBMESSAGE_FIELD)) {
      try {
        FudgeMsg xsSubMsg = message.getMessage(XS_SUBMESSAGE_FIELD);
        String xClassName = xsSubMsg.getString(0);
        Class<?> xClass = xClassName != null ? Class.forName(xClassName) : Object.class;
        final List<FudgeField> xsFields = xsSubMsg.getAllByName(XS_FIELD);
        xsArray = (Object[]) Array.newInstance(xClass, xsFields.size());
        int i = 0;
        for (final FudgeField xField : xsFields) {
          final Object x = deserializer.fieldValueToObject(xField);
          xsArray[i] = x;
          i++;
        }
        FudgeMsg ysSubMsg = message.getMessage(YS_SUBMESSAGE_FIELD);
        String yClassName = ysSubMsg.getString(0);
        Class<?> yClass = yClassName != null ? Class.forName(yClassName) : Object.class;
        final List<FudgeField> ysFields = ysSubMsg.getAllByName(YS_FIELD);
        ysArray = (Object[]) Array.newInstance(yClass, ysFields.size());

        int j = 0;
        for (final FudgeField yField : ysFields) {
          final Object y = deserializer.fieldValueToObject(yField);
          ysArray[j] = y;
          j++;
        }

        FudgeMsg zsSubMsg = message.getMessage(ZS_SUBMESSAGE_FIELD);
        String zClassName = zsSubMsg.getString(0);
        Class<?> zClass = zClassName != null ? Class.forName(zClassName) : Object.class;
        final List<FudgeField> zsFields = zsSubMsg.getAllByName(ZS_FIELD);
        zsArray = (Object[]) Array.newInstance(zClass, zsFields.size());

        int k = 0;
        for (final FudgeField zField : zsFields) {
          final Object z = deserializer.fieldValueToObject(zField);
          zsArray[k] = z;
          k++;
        }
      } catch (ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Cannot find class, probably refactoring", ex);
      }
    } else { // old format, should still support
      final List<FudgeField> xsFields = message.getAllByName(XS_FIELD);
      if (xsFields.size() > 0) {
        final Object firstX = deserializer.fieldValueToObject(xsFields.get(0));
        xsArray = (Object[]) Array.newInstance(firstX.getClass(), xsFields.size());
      } else {
        xsArray = new Object[0];
      }
      int i = 0;
      for (final FudgeField xField : xsFields) {
        final Object x = deserializer.fieldValueToObject(xField);
        xsArray[i] = x;
        i++;
      }
      final List<FudgeField> ysFields = message.getAllByName(YS_FIELD);
      if (ysFields.size() > 0) {
        final Object firstY = deserializer.fieldValueToObject(ysFields.get(0));
        ysArray = (Object[]) Array.newInstance(firstY.getClass(), ysFields.size());
      } else {
        ysArray = new Object[0];
      }
      int j = 0;
      for (final FudgeField yField : ysFields) {
        final Object y = deserializer.fieldValueToObject(yField);
        ysArray[j] = y;
        j++;
      }
      final List<FudgeField> zsFields = message.getAllByName(ZS_FIELD);
      if (zsFields.size() > 0) {
        final Object firstZ = deserializer.fieldValueToObject(zsFields.get(0));
        zsArray = (Object[]) Array.newInstance(firstZ.getClass(), zsFields.size());
      } else {
        zsArray = new Object[0];
      }
      int k = 0;
      for (final FudgeField zField : zsFields) {
        final Object z = deserializer.fieldValueToObject(zField);
        zsArray[k] = z;
        k++;
      }
    }
    String xLabel;
    String yLabel;
    String zLabel;
    if (message.hasField(X_LABEL_FIELD)) {
      xLabel = message.getString(X_LABEL_FIELD);
    } else {
      xLabel = VolatilityCubeData.DEFAULT_X_LABEL;     // for backwards compatibility - should be removed at some point
    }
    if (message.hasField(Y_LABEL_FIELD)) {
      yLabel = message.getString(Y_LABEL_FIELD);
    } else {
      yLabel = VolatilityCubeData.DEFAULT_Y_LABEL;     // for backwards compatibility - should be removed at some point
    }
    if (message.hasField(Z_LABEL_FIELD)) {
      zLabel = message.getString(Z_LABEL_FIELD);
    } else {
      zLabel = VolatilityCubeData.DEFAULT_Z_LABEL;     // for backwards compatibility - should be removed at some point
    }
    if (xsArray.length > 0 && ysArray.length > 0) {
      final Class<?> xClazz = xsArray[0].getClass();
      final Class<?> yClazz = ysArray[0].getClass();
      final Class<?> zClazz = zsArray[0].getClass();
      final Map<Triple<Object, Object, Object>, Double> values = newHashMap();
      final List<FudgeField> valuesFields = message.getAllByName(VALUES_FIELD);
      for (final FudgeField valueField : valuesFields) {
        final FudgeMsg subMessage = (FudgeMsg) valueField.getValue();
        final Object x = deserializer.fieldValueToObject(xClazz, subMessage.getByName(X_FIELD));
        final Object y = deserializer.fieldValueToObject(yClazz, subMessage.getByName(Y_FIELD));
        final Object z = deserializer.fieldValueToObject(zClazz, subMessage.getByName(Z_FIELD));
        final Double value = subMessage.getDouble(VALUE_FIELD);
        values.put(Triple.of(x, y, z), value);
      }
      return new VolatilityCubeData<>(definitionName, specificationName, target, xLabel, yLabel, zLabel, values);
    }
    return new VolatilityCubeData<>(definitionName, specificationName, target, xLabel, yLabel, zLabel, Collections.<Triple<Object, Object, Object>, Double>emptyMap());
  }

}
