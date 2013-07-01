/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.Currency;

/**
 * Fudge builder for SyntheticIdentifierCurveInstrumentProvider
 */
@FudgeBuilderFor(SyntheticIdentifierCurveInstrumentProvider.class)
public class SyntheticIdentifierCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<SyntheticIdentifierCurveInstrumentProvider> {
  private static final String CURRENCY_FIELD = "ccy";
  private static final String STRIP_TYPE_FIELD = "stripType";
  private static final String SCHEME_FIELD = "scheme";
  private static final String DATA_FIELD = "dataField";
  private static final String TYPE_FIELD = "typeField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SyntheticIdentifierCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(CURRENCY_FIELD, object.getCurrency().getCode());
    message.add(STRIP_TYPE_FIELD, object.getType().name());
    message.add(SCHEME_FIELD, object.getScheme().getName());
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(TYPE_FIELD, object.getDataFieldType().name());
    return message;
  }

  @Override
  public SyntheticIdentifierCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency ccy = Currency.of(message.getString(CURRENCY_FIELD));
    final StripInstrumentType stripType = StripInstrumentType.valueOf(message.getString(STRIP_TYPE_FIELD));
    final ExternalScheme scheme = ExternalScheme.of(message.getString(SCHEME_FIELD));
    if (message.hasField(DATA_FIELD) && message.hasField(TYPE_FIELD)) {
      final String dataField = message.getString(DATA_FIELD);
      final DataFieldType fieldType = DataFieldType.valueOf(message.getString(TYPE_FIELD));
      return new SyntheticIdentifierCurveInstrumentProvider(ccy, stripType, scheme, dataField, fieldType);
    }
    return new SyntheticIdentifierCurveInstrumentProvider(ccy, stripType, scheme);
  }

}
