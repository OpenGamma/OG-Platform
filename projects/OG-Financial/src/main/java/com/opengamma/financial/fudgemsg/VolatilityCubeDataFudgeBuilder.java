/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
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
import com.opengamma.util.tuple.Triple;

/**
 * Builder for converting {@link VolatilityCubeData} instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilityCubeData.class)
public class VolatilityCubeDataFudgeBuilder implements FudgeBuilder<VolatilityCubeData<?, ?, ?>> {
  /** The definition field */
  private static final String DEFINITION_FIELD = "definitionName";
  /** The specification field */
  private static final String SPECIFICATION_FIELD = "specificationName";
  /** The xs field */
  private static final String XS_FIELD = "xs";
  /** The x sub-message field */
  private static final String XS_SUBMESSAGE_FIELD = "xsSubMessage";
  /** The ys field */
  private static final String YS_FIELD = "ys";
  /** The y sub-message field */
  private static final String YS_SUBMESSAGE_FIELD = "ysSubMessage";
  /** The zs field */
  private static final String ZS_FIELD = "zs";
  /** The z sub-message field */
  private static final String ZS_SUBMESSAGE_FIELD = "zsSubMessage";
  /** The x field */
  private static final String X_FIELD = "x";
  /** The y field */
  private static final String Y_FIELD = "y";
  /** The z field */
  private static final String Z_FIELD = "z";
  /** The value field */
  private static final String VALUE_FIELD = "value";
  /** The values field */
  private static final String VALUES_FIELD = "values";
  /** The x labels field */
  private static final String X_LABEL_FIELD = "xLabel";
  /** The y labels field */
  private static final String Y_LABEL_FIELD = "yLabel";
  /** The z labels field */
  private static final String Z_LABEL_FIELD = "zLabel";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilityCubeData<?, ?, ?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(DEFINITION_FIELD, object.getDefinitionName());
    message.add(SPECIFICATION_FIELD, object.getSpecificationName());
    final MutableFudgeMsg xsSubMsg = message.addSubMessage(XS_SUBMESSAGE_FIELD, null);
    FudgeSerializer.addClassHeader(xsSubMsg, object.getXs().getClass().getComponentType());
    for (final Object x : object.getXs()) {
      if (x != null) {
        xsSubMsg.add(XS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    final MutableFudgeMsg ysSubMsg = message.addSubMessage(YS_SUBMESSAGE_FIELD, null);
    FudgeSerializer.addClassHeader(ysSubMsg, object.getYs().getClass().getComponentType());
    for (final Object y : object.getYs()) {
      if (y != null) {
        ysSubMsg.add(YS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(y), y.getClass()));
      }
    }
    final MutableFudgeMsg zsSubMsg = message.addSubMessage(ZS_SUBMESSAGE_FIELD, null);
    FudgeSerializer.addClassHeader(zsSubMsg, object.getZs().getClass().getComponentType());
    for (final Object z : object.getZs()) {
      if (z != null) {
        zsSubMsg.add(ZS_FIELD, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(z), z.getClass()));
      }
    }
    for (final Entry<?, Double> entry : object.asMap().entrySet()) {
      final Triple<Object, Object, Object> triple = (Triple<Object, Object, Object>) entry.getKey();
      final MutableFudgeMsg subMessage = serializer.newMessage();
      if (triple.getFirst() != null && triple.getSecond() != null) {
        serializer.addToMessageWithClassHeaders(subMessage, X_FIELD, null, triple.getFirst());
        serializer.addToMessageWithClassHeaders(subMessage, Y_FIELD, null, triple.getSecond());
        serializer.addToMessageWithClassHeaders(subMessage, Z_FIELD, null, triple.getThird());
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
    final String definitionName = message.getString(DEFINITION_FIELD);
    final String specificationName = message.getString(SPECIFICATION_FIELD);
    Object[] xsArray;
    Object[] ysArray;
    Object[] zsArray;
    try {
      final FudgeMsg xsSubMsg = message.getMessage(XS_SUBMESSAGE_FIELD);
      final String xClassName = xsSubMsg.getString(0);
      final Class<?> xClass = xClassName != null ? Class.forName(xClassName) : Object.class;
      final List<FudgeField> xsFields = xsSubMsg.getAllByName(XS_FIELD);
      xsArray = (Object[]) Array.newInstance(xClass, xsFields.size());
      int i = 0;
      for (final FudgeField xField : xsFields) {
        final Object x = deserializer.fieldValueToObject(xField);
        xsArray[i] = x;
        i++;
      }
      final FudgeMsg ysSubMsg = message.getMessage(YS_SUBMESSAGE_FIELD);
      final String yClassName = ysSubMsg.getString(0);
      final Class<?> yClass = yClassName != null ? Class.forName(yClassName) : Object.class;
      final List<FudgeField> ysFields = ysSubMsg.getAllByName(YS_FIELD);
      ysArray = (Object[]) Array.newInstance(yClass, ysFields.size());
      int j = 0;
      for (final FudgeField yField : ysFields) {
        final Object y = deserializer.fieldValueToObject(yField);
        ysArray[j] = y;
        j++;
      }
      final FudgeMsg zsSubMsg = message.getMessage(ZS_SUBMESSAGE_FIELD);
      final String zClassName = zsSubMsg.getString(0);
      final Class<?> zClass = zClassName != null ? Class.forName(zClassName) : Object.class;
      final List<FudgeField> zsFields = zsSubMsg.getAllByName(ZS_FIELD);
      zsArray = (Object[]) Array.newInstance(zClass, zsFields.size());
      int k = 0;
      for (final FudgeField zField : zsFields) {
        final Object z = deserializer.fieldValueToObject(zField);
        zsArray[k] = z;
        k++;
      }
    } catch (final ClassNotFoundException e) {
      throw new OpenGammaRuntimeException(e.getMessage());
    }
    final String xLabel = message.getString(X_LABEL_FIELD);
    final String yLabel = message.getString(Y_LABEL_FIELD);
    final String zLabel = message.getString(Z_LABEL_FIELD);
    if (xsArray.length > 0 && ysArray.length > 0 && zsArray.length > 0) {
      final Class<?> xClazz = xsArray[0].getClass();
      final Class<?> yClazz = ysArray[0].getClass();
      final Class<?> zClazz = zsArray[0].getClass();
      final Map<Triple<Object, Object, Object>, Double> values = new HashMap<>();
      final List<FudgeField> valuesFields = message.getAllByName(VALUES_FIELD);
      for (final FudgeField valueField : valuesFields) {
        final FudgeMsg subMessage = (FudgeMsg) valueField.getValue();
        final Object x = deserializer.fieldValueToObject(xClazz, subMessage.getByName(X_FIELD));
        final Object y = deserializer.fieldValueToObject(yClazz, subMessage.getByName(Y_FIELD));
        final Object z = deserializer.fieldValueToObject(zClazz, subMessage.getByName(Z_FIELD));
        final Double value = subMessage.getDouble(VALUE_FIELD);
        values.put(Triple.of(x, y, z), value);
      }
      return new VolatilityCubeData<>(definitionName, specificationName, xLabel, yLabel, zLabel, values);
    }
    return new VolatilityCubeData<>(definitionName, specificationName, xLabel, yLabel, zLabel, Collections.<Triple<Object, Object, Object>, Double>emptyMap());
  }

}
