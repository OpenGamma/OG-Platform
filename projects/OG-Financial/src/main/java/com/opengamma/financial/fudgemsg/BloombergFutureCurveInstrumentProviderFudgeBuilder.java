/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;

/**
 * Builder for converting {@link BloombergFutureCurveInstrumentProvider} instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergFutureCurveInstrumentProvider.class)
public class BloombergFutureCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFutureCurveInstrumentProvider> {
  /** The prefix field */
  private static final String PREFIX_FIELD = "prefix";
  /** The market sector field */
  private static final String MARKET_SECTOR_FIELD = "marketSector";
  /** The data field */
  private static final String DATA_FIELD = "dataField";
  /** The type field */
  private static final String TYPE_FIELD = "typeField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFutureCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    message.add(PREFIX_FIELD, object.getFuturePrefix());
    message.add(MARKET_SECTOR_FIELD, object.getMarketSector());
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(TYPE_FIELD, object.getDataFieldType().toString());
    return message;
  }

  @Override
  public BloombergFutureCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString(PREFIX_FIELD);
    final String marketSector = message.getString(MARKET_SECTOR_FIELD);
    if (message.hasField(DATA_FIELD) && message.hasField(TYPE_FIELD)) {
      final String dataField = message.getString(DATA_FIELD);
      final DataFieldType fieldType = DataFieldType.valueOf(message.getString(TYPE_FIELD));
      return new BloombergFutureCurveInstrumentProvider(prefix, marketSector, dataField, fieldType);
    }
    return new BloombergFutureCurveInstrumentProvider(prefix, marketSector);
  }

}
