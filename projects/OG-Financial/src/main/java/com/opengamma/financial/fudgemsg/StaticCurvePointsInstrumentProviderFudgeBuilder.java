/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;

/**
 * Builder for converting {@link StaticCurvePointsInstrumentProvider} instances to/from Fudge messages.
 */
@FudgeBuilderFor(StaticCurvePointsInstrumentProvider.class)
public class StaticCurvePointsInstrumentProviderFudgeBuilder implements FudgeBuilder<StaticCurvePointsInstrumentProvider> {
  /** The instrument field */
  private static final String INSTRUMENT_FIELD = "instrument";
  /** The data field */
  private static final String DATA_FIELD = "dataField";
  /** The type field */
  private static final String TYPE_FIELD = "typeField";
  /** The underlying instrument field */
  private static final String UNDERLYING_INSTRUMENT_FIELD = "underlyingInstrument";
  /** The underlying instrument data field */
  private static final String UNDERLYING_DATA_FIELD = "underlyingDataField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final StaticCurvePointsInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    serializer.addToMessage(message, INSTRUMENT_FIELD, null, object.getInstrument(null, null));
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(TYPE_FIELD, object.getDataFieldType().toString());
    serializer.addToMessage(message, UNDERLYING_INSTRUMENT_FIELD, null, object.getUnderlyingInstrument());
    message.add(UNDERLYING_DATA_FIELD, object.getUnderlyingMarketDataField());
    return message;
  }

  @Override
  public StaticCurvePointsInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FudgeField instrumentIdentifier = message.getByName(INSTRUMENT_FIELD);
    final ExternalId identifier = deserializer.fieldValueToObject(ExternalId.class, instrumentIdentifier);
    final String dataField = message.getString(DATA_FIELD);
    final DataFieldType fieldType = DataFieldType.valueOf(message.getString(TYPE_FIELD));
    final FudgeField underlyingInstrumentIdentifier = message.getByName(UNDERLYING_INSTRUMENT_FIELD);
    final ExternalId underlyingIdentifier = deserializer.fieldValueToObject(ExternalId.class, underlyingInstrumentIdentifier);
    final String underlyingDataField = message.getString(UNDERLYING_DATA_FIELD);
    return new StaticCurvePointsInstrumentProvider(identifier, dataField, fieldType, underlyingIdentifier, underlyingDataField);
  }


}
