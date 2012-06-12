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

import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeData;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@FudgeBuilderFor(SwaptionVolatilityCubeData.class)
public class SwaptionVolatilityCubeDataBuilder implements FudgeBuilder<SwaptionVolatilityCubeData> {
  private static final String DATA_FIELD = "data";
  private static final String SWAP_MATURITY_FIELD = "swapMaturity";
  private static final String SWAPTION_EXPIRY_FIELD = "swaptionExpiry";
  private static final String RELATIVE_STRIKE_FIELD = "relativeStrike";
  private static final String VOLATILITY_FIELD = "volatility";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwaptionVolatilityCubeData object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, SwaptionVolatilityCubeData.class);
    for (final Map.Entry<VolatilityPoint, Double> entry : object.getVolatilityPoints().entrySet()) {
      final MutableFudgeMsg data = serializer.newMessage();
      serializer.addToMessage(data, SWAP_MATURITY_FIELD, null, entry.getKey().getSwapTenor());
      serializer.addToMessage(data, SWAPTION_EXPIRY_FIELD, null, entry.getKey().getOptionExpiry());
      serializer.addToMessage(data, RELATIVE_STRIKE_FIELD, null, entry.getKey().getRelativeStrike());
      serializer.addToMessage(data, VOLATILITY_FIELD, null, entry.getValue());
      message.add(DATA_FIELD, data);
    }
    return message;
  }

  @Override
  public SwaptionVolatilityCubeData buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final List<FudgeField> dataFields = message.getAllByName(DATA_FIELD);
    final Map<VolatilityPoint, Double> data = new HashMap<VolatilityPoint, Double>();
    for (final FudgeField dataField : dataFields) {
      final FudgeMsg dataMsg = (FudgeMsg) dataField.getValue();
      final Tenor swapTenor = deserializer.fieldValueToObject(Tenor.class, dataMsg.getByName(SWAP_MATURITY_FIELD));
      final Tenor swaptionExpiry = deserializer.fieldValueToObject(Tenor.class, dataMsg.getByName(SWAPTION_EXPIRY_FIELD));
      final double delta = deserializer.fieldValueToObject(Double.class, dataMsg.getByName(RELATIVE_STRIKE_FIELD));
      final VolatilityPoint coordinate = new VolatilityPoint(swapTenor, swaptionExpiry, delta);
      final Double volatility = deserializer.fieldValueToObject(Double.class, dataMsg.getByName(VOLATILITY_FIELD));
      data.put(coordinate, volatility);
    }
    return new SwaptionVolatilityCubeData(data);
  }

}
