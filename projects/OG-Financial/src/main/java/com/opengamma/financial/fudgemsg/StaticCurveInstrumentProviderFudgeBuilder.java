/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;

/**
 * Builder for converting {@link StaticCurveInstrumentProvider} instances to/from Fudge messages.
 */
@FudgeBuilderFor(StaticCurveInstrumentProvider.class)
public class StaticCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<StaticCurveInstrumentProvider> {
  /** The instrument field */
  private static final String INSTRUMENT_FIELD = "instrument";
  /** The data field */
  private static final String DATA_FIELD = "dataField";
  /** The type field */
  private static final String TYPE_FIELD = "typeField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final StaticCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    serializer.addToMessage(message, INSTRUMENT_FIELD, null, object.getInstrument(null, null));
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(TYPE_FIELD, object.getDataFieldType().toString());
    return message;
  }

  @Override
  public StaticCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FudgeField instrumentIdentifier = message.getByName(INSTRUMENT_FIELD);
    final ExternalId identifier = deserializer.fieldValueToObject(ExternalId.class, instrumentIdentifier);
    if (message.hasField(DATA_FIELD) && message.hasField(TYPE_FIELD)) {
      final String dataField = message.getString(DATA_FIELD);
      final DataFieldType fieldType = DataFieldType.valueOf(message.getString(TYPE_FIELD));
      return new StaticCurveInstrumentProvider(identifier, dataField, fieldType);
    }
    return new StaticCurveInstrumentProvider(identifier);
  }

}
