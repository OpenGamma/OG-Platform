/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeData;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
@FudgeBuilderFor(SwaptionVolatilityCubeData.class)
public class SwaptionVolatilityCubeDataBuilder implements FudgeBuilder<SwaptionVolatilityCubeData<Object, Object, Object>> {
  private static final String DATA_FIELD = "data";
  private static final String SWAP_MATURITY_FIELD = "swapMaturity";
  private static final String SWAPTION_EXPIRY_FIELD = "swaptionExpiry";
  private static final String RELATIVE_STRIKE_FIELD = "relativeStrike";
  private static final String VOLATILITY_FIELD = "volatility";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwaptionVolatilityCubeData<Object, Object, Object> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, SwaptionVolatilityCubeData.class);
    for (final Map.Entry<Triple<Object, Object, Object>, Double> entry : object.getVolatilityPoints().entrySet()) {
      final MutableFudgeMsg data = serializer.newMessage();
      serializer.addToMessage(data, SWAP_MATURITY_FIELD, null, entry.getKey().getFirst());
      serializer.addToMessage(data, SWAPTION_EXPIRY_FIELD, null, entry.getKey().getSecond());
      serializer.addToMessage(data, RELATIVE_STRIKE_FIELD, null, entry.getKey().getThird());
      serializer.addToMessage(data, VOLATILITY_FIELD, null, entry.getValue());
      message.add(DATA_FIELD, data);
    }
    return message;
  }

  @Override
  public SwaptionVolatilityCubeData<Object, Object, Object> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final List<FudgeField> dataFields = message.getAllByName(DATA_FIELD);
    final Map<Triple<Object, Object, Object>, Double> data = new HashMap<Triple<Object, Object, Object>, Double>();
    for (final FudgeField dataField : dataFields) {
      final FudgeMsg dataMsg = (FudgeMsg) dataField.getValue();
      final Object swapTenor = deserializer.fieldValueToObject(Tenor.class, dataMsg.getByName(SWAP_MATURITY_FIELD));
      final Object swaptionExpiry = deserializer.fieldValueToObject(Tenor.class, dataMsg.getByName(SWAPTION_EXPIRY_FIELD));
      final Object delta = deserializer.fieldValueToObject(Double.class, dataMsg.getByName(RELATIVE_STRIKE_FIELD));
      final Triple<Object, Object, Object> coordinate = Triple.of(swapTenor, swaptionExpiry, delta);
      final Double volatility = deserializer.fieldValueToObject(Double.class, dataMsg.getByName(VOLATILITY_FIELD));
      data.put(coordinate, volatility);
    }
    return new SwaptionVolatilityCubeData<Object, Object, Object>(data);
  }

}
