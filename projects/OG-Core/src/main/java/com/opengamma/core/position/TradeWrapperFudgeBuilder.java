/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.joda.beans.JodaBeanUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.impl.TradeWrapper;
import com.opengamma.util.fudgemsg.DirectBeanFudgeBuilder;

/**
 * Fudge message builder for {@code TradeWrapper}.
 */
@GenericFudgeBuilderFor(TradeWrapper.class)
public class TradeWrapperFudgeBuilder implements FudgeBuilder<TradeWrapper> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, TradeWrapper trade) {
    DirectBeanFudgeBuilder<TradeWrapper> builder = new DirectBeanFudgeBuilder<>(JodaBeanUtils.metaBean(trade.getClass()));
    MutableFudgeMsg msg = builder.buildMessage(serializer, trade);
    FudgeSerializer.addClassHeader(msg, trade.getClass());
    return msg;
  }

  @Override
  public TradeWrapper buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeContext fudgeContext = deserializer.getFudgeContext();
    Class<?> possibleClazz = null;
    List<FudgeField> types = message.getAllByOrdinal(FudgeSerializer.TYPES_HEADER_ORDINAL);
    if (types.size() != 0 && types.get(0).getValue() instanceof String) {
      String clazzString = (String) types.get(0).getValue();
      try {
        possibleClazz = fudgeContext.getTypeDictionary().loadClass(clazzString);
        DirectBeanFudgeBuilder<TradeWrapper> builder = new DirectBeanFudgeBuilder<>(JodaBeanUtils.metaBean(possibleClazz));
        return builder.buildObject(deserializer, message);
      } catch (ClassNotFoundException e) {
        throw new OpenGammaRuntimeException("TradeWrapper deserialization error for " + possibleClazz, e);
      }
    }
    throw new OpenGammaRuntimeException("TradeWrapper deserialization error, either no type information or type is not a String");
  }

}
