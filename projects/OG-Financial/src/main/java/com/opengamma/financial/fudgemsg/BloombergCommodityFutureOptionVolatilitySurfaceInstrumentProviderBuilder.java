/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.fudgemsg;

import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.DATA_FIELD_NAME;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.POSTFIX_FIELD_NAME;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.PREFIX_FIELD_NAME;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.volatility.surface.BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 *
 */
@FudgeBuilderFor(BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider> {
  private static final String CALL_FIELD_NAME = "useCallAboveStrikeValue";
  private static final String EXCHANGE_ID_FIELD_NAME = "exchangeId";
  private static final String SCHEME_NAME = "schemeName";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getFutureOptionPrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    message.add(CALL_FIELD_NAME, object.useCallAboveStrike());
    message.add(EXCHANGE_ID_FIELD_NAME, object.getExchangeIdName());
    message.add(SCHEME_NAME, object.getSchemeName());
    return message;
  }

  @Override
  public BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    String futureOptionPrefix = message.getString(PREFIX_FIELD_NAME);
    String postfix = message.getString(POSTFIX_FIELD_NAME);
    String dataFieldName = message.getString(DATA_FIELD_NAME);
    String schemeName = message.getString(SCHEME_NAME);
    final Double useCallAboveValue = message.getDouble(CALL_FIELD_NAME);
    final String exchangeId = message.getString(EXCHANGE_ID_FIELD_NAME);
    return new BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(futureOptionPrefix, postfix, dataFieldName, useCallAboveValue, exchangeId, schemeName);
  }

}
