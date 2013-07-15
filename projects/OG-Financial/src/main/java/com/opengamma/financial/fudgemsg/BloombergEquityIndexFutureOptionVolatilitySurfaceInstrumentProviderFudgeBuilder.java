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

import com.opengamma.financial.analytics.volatility.surface.BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 *
 */
@FudgeBuilderFor(BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider> {
  /** The field indicating which value is the cutoff for asking for calls or puts */
  private static final String CALL_FIELD_NAME = "useCallAboveStrikeValue";
  /** The exchange id field name */
  private static final String EXCHANGE_ID_FIELD_NAME = "exchangeId";
  /** The ticker scheme name */
  private static final String TICKER_SCHEME_NAME = "tickerScheme";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getFutureOptionPrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    message.add(CALL_FIELD_NAME, object.useCallAboveStrike());
    message.add(EXCHANGE_ID_FIELD_NAME, object.getExchangeIdName());
    message.add(TICKER_SCHEME_NAME, object.getTickerSchemeName());
    return message;
  }

  @Override
  public BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String futureOptionPrefix = message.getString(PREFIX_FIELD_NAME);
    final String postfix = message.getString(POSTFIX_FIELD_NAME);
    final String dataFieldName = message.getString(DATA_FIELD_NAME);
    final Double useCallAboveValue = message.getDouble(CALL_FIELD_NAME);
    final String exchangeId = message.getString(EXCHANGE_ID_FIELD_NAME);
    final String schemeName = message.getString(TICKER_SCHEME_NAME);
    return new BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider(futureOptionPrefix, postfix, dataFieldName, useCallAboveValue, exchangeId, schemeName);
  }

}
