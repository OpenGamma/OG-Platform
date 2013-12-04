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
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;

/**
 * Builder for converting VolatilitySurfaceData instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceData.class)
public class VolatilitySurfaceDataFudgeBuilder implements FudgeBuilder<VolatilitySurfaceData<?, ?>> {
  private static final String TARGET_FIELD = "target";
  private static final String DEFINITION_FIELD = "definitionName";
  private static final String SPECIFICATION_FIELD = "specificationName";
  private static final String XS_FIELD = "xs";
  private static final String XS_SUBMESSAGE_FIELD = "xsSubMessage";
  private static final String YS_FIELD = "ys";
  private static final String YS_SUBMESSAGE_FIELD = "ysSubMessage";
  private static final String X_FIELD = "x";
  private static final String Y_FIELD = "y";
  private static final String VALUE_FIELD = "value";
  private static final String VALUES_FIELD = "values";
  private static final String X_LABEL_FIELD = "xLabel";
  private static final String Y_LABEL_FIELD = "yLabel";
  
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilitySurfaceData<?, ?> object) {
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
    for (final Entry<?, Double> entry : object.asMap().entrySet()) {
      final Pair<Object, Object> pair = (Pair<Object, Object>) entry.getKey();
      final MutableFudgeMsg subMessage = serializer.newMessage();
      if (pair.getFirst() != null && pair.getSecond() != null) {
        subMessage.add(X_FIELD, null, serializer.objectToFudgeMsg(pair.getFirst()));
        subMessage.add(Y_FIELD, null, serializer.objectToFudgeMsg(pair.getSecond()));
        subMessage.add(VALUE_FIELD, null, entry.getValue());
        message.add(VALUES_FIELD, null, subMessage);
      }
    }
    message.add(X_LABEL_FIELD, object.getXLabel());
    message.add(Y_LABEL_FIELD, object.getYLabel());
    return message;
  }

  @Override
  public VolatilitySurfaceData<?, ?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    UniqueIdentifiable target;
    target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName(TARGET_FIELD));
    final String definitionName = message.getString(DEFINITION_FIELD);
    final String specificationName = message.getString(SPECIFICATION_FIELD);
    Object[] xsArray;
    Object[] ysArray;
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
    }
    String xLabel;
    String yLabel;
    if (message.hasField(X_LABEL_FIELD)) {
      xLabel = message.getString(X_LABEL_FIELD);
    } else {
      xLabel = VolatilitySurfaceData.DEFAULT_X_LABEL;     // for backwards compatibility - should be removed at some point
    }
    if (message.hasField(Y_LABEL_FIELD)) {
      yLabel = message.getString(Y_LABEL_FIELD);
    } else {
      yLabel = VolatilitySurfaceData.DEFAULT_Y_LABEL;     // for backwards compatibility - should be removed at some point
    }
    if (xsArray.length > 0 && ysArray.length > 0) {
      final Class<?> xClazz = xsArray[0].getClass();
      final Class<?> yClazz = ysArray[0].getClass();
      final Map<Pair<Object, Object>, Double> values = new HashMap<Pair<Object, Object>, Double>();
      final List<FudgeField> valuesFields = message.getAllByName(VALUES_FIELD);
      for (final FudgeField valueField : valuesFields) {
        final FudgeMsg subMessage = (FudgeMsg) valueField.getValue();
        final Object x = deserializer.fieldValueToObject(xClazz, subMessage.getByName(X_FIELD));
        final Object y = deserializer.fieldValueToObject(yClazz, subMessage.getByName(Y_FIELD));
        final Double value = subMessage.getDouble(VALUE_FIELD);
        values.put(Pair.of(x, y), value);
      }
      return new VolatilitySurfaceData<Object, Object>(definitionName, specificationName, target, xsArray, xLabel, ysArray, yLabel, values);
    }
    return new VolatilitySurfaceData<Object, Object>(definitionName, specificationName, target, xsArray, xLabel, ysArray, yLabel, Collections.<Pair<Object, Object>, Double>emptyMap());
  }

}
