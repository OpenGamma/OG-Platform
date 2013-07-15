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

import com.opengamma.financial.analytics.volatility.surface.BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 * SurfaceProvider provides ticker codes for creation of surfaces. These are serialized along with VolatilitySurfaceSpecification
 */
@FudgeBuilderFor(BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergEquityFutureOptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider> {
  /** The field indicating which value is the cutoff for asking for calls or puts */
  private static final String CALL_FIELD_NAME = "useCallAboveStrikeValue";
  /** The exchange id field name */
  private static final String EXCHANGE_ID_FIELD_NAME = "exchangeId";
  /** The ticker scheme name */
  private static final String TICKER_SCHEME_NAME = "tickerScheme";
  // backwards compatibility
  /** The default value of the exchange id */
  private static final String DEFAULT_EXCHANGE_ID = "CBT";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getFutureOptionPrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    message.add(CALL_FIELD_NAME, object.useCallAboveStrike());
    message.add(EXCHANGE_ID_FIELD_NAME, object.getExchangeIdName());
    message.add(TICKER_SCHEME_NAME, object.getSchemeName());
    return message;
  }

  @Override
  public BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    String futureOptionPrefix = message.getString(PREFIX_FIELD_NAME);
    //backward compatibility
    if (futureOptionPrefix == null) {
      futureOptionPrefix = message.getString("futureOptionPrefix");
    }
    String postfix = message.getString(POSTFIX_FIELD_NAME);
    //backward compatibility
    if (postfix == null) {
      postfix = message.getString("postfix");
    }
    String dataFieldName = message.getString(DATA_FIELD_NAME);
    //backward compatibility
    if (dataFieldName == null) {
      dataFieldName = message.getString("dataFieldName");
    }
    final Double useCallAboveValue = message.getDouble(CALL_FIELD_NAME);
    if (message.hasField(EXCHANGE_ID_FIELD_NAME)) {
      final String exchangeId = message.getString(EXCHANGE_ID_FIELD_NAME);
      if (message.hasField(TICKER_SCHEME_NAME)) {
        final String tickerScheme = message.getString(TICKER_SCHEME_NAME);
        return new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(futureOptionPrefix, postfix, dataFieldName, useCallAboveValue, exchangeId, tickerScheme);
      }
      return new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(futureOptionPrefix, postfix, dataFieldName, useCallAboveValue, exchangeId);
    }
    if (message.hasField(TICKER_SCHEME_NAME)) { //this will never be hit, but better to be safe
      final String tickerScheme = message.getString(TICKER_SCHEME_NAME);
      return new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(futureOptionPrefix, postfix, dataFieldName, useCallAboveValue, DEFAULT_EXCHANGE_ID, tickerScheme);
    }
    return new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(futureOptionPrefix, postfix, dataFieldName, useCallAboveValue, DEFAULT_EXCHANGE_ID);
  }

}
