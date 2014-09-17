/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.ircurve.FutureMonthCodeCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalScheme;

/**
 * Builder for converting {@link FutureMonthCodeCurveInstrumentProvider} instances to/from Fudge messages.
 */
@FudgeBuilderFor(FutureMonthCodeCurveInstrumentProvider.class)
public class FutureMonthCodeCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<FutureMonthCodeCurveInstrumentProvider> {
  /** The prefix field */
  private static final String PREFIX_FIELD = "prefix";
  /** The market sector field */
  private static final String MARKET_SECTOR_FIELD = "suffix";
  /** The data field */
  private static final String DATA_FIELD = "dataField";
  /** The type field */
  private static final String TYPE_FIELD = "typeField";
  /** The type field */
  private static final String SCHEME_FIELD = "schemeField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FutureMonthCodeCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    message.add(PREFIX_FIELD, object.getFuturePrefix());
    message.add(MARKET_SECTOR_FIELD, object.getFutureSuffix());
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(TYPE_FIELD, object.getDataFieldType().toString());
    message.add(SCHEME_FIELD, object.getScheme().toString());
    return message;
  }

  @Override
  public FutureMonthCodeCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString(PREFIX_FIELD);
    final String marketSector = message.getString(MARKET_SECTOR_FIELD);
    final ExternalScheme scheme = ExternalScheme.of(message.getString(SCHEME_FIELD));
    if (message.hasField(DATA_FIELD) && message.hasField(TYPE_FIELD)) {
      final String dataField = message.getString(DATA_FIELD);
      final DataFieldType fieldType = DataFieldType.valueOf(message.getString(TYPE_FIELD));
      return new FutureMonthCodeCurveInstrumentProvider(prefix, marketSector, dataField, fieldType, scheme);
    }
    return new FutureMonthCodeCurveInstrumentProvider(prefix, marketSector, scheme);
  }

}
