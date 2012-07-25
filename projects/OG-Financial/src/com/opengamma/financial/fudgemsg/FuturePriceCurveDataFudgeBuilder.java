/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveData;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(FuturePriceCurveData.class)
public class FuturePriceCurveDataFudgeBuilder implements FudgeBuilder<FuturePriceCurveData<?>> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FuturePriceCurveData<?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    serializer.addToMessage(message, "target", null, object.getTarget());
    message.add("definitionName", object.getDefinitionName());
    message.add("specificationName", object.getSpecificationName());
    for (final Object x : object.getXs()) {
      if (x != null) {
        message.add("xs", null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    for (final Entry<?, Double> entry : object.asMap().entrySet()) {
      final Object x = entry.getKey();
      final MutableFudgeMsg subMessage = serializer.newMessage();
      if (x != null) {
        subMessage.add("x", null, serializer.objectToFudgeMsg(x));
        subMessage.add("value", null, entry.getValue());
        message.add("values", null, subMessage);
      }
    }
    return message;
  }

  @Override
  public FuturePriceCurveData<?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueIdentifiable target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    final String definitionName = message.getString("definitionName");
    final String specificationName = message.getString("specificationName");
    final List<FudgeField> xsFields = message.getAllByName("xs");
    final List<Object> xs = new ArrayList<Object>();
    Object[] xsArray = null;
    for (final FudgeField xField : xsFields) {
      final Object x = deserializer.fieldValueToObject(xField);
      xs.add(x);
      if (xsArray == null) {
        xsArray = (Object[]) Array.newInstance(x.getClass(), 0);
      }
    }
    if (xs.size() > 0) {
      final Class<?> xClazz = xs.get(0).getClass();
      final Map<Object, Double> values = new HashMap<Object, Double>();
      final List<FudgeField> valuesFields = message.getAllByName("values");
      for (final FudgeField valueField : valuesFields) {
        final FudgeMsg subMessage = (FudgeMsg) valueField.getValue();
        final Object x = deserializer.fieldValueToObject(xClazz, subMessage.getByName("x"));
        final Double value = subMessage.getDouble("value");
        values.put(x, value);
      }
      return new FuturePriceCurveData<Object>(definitionName, specificationName, target, xs.toArray(xsArray), values);
    }
    return new FuturePriceCurveData<Object>(definitionName, specificationName, target, xs.toArray(), Collections.<Object, Double>emptyMap());
  }

}
