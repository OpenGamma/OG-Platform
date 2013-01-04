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
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.Currency;

/**
 * Fudge builder for SyntheticIdentifierCurveInstrumentProvider
 */
@FudgeBuilderFor(SyntheticIdentifierCurveInstrumentProvider.class)
public class SyntheticIdentifierCurveInstrumentProviderFudgeBuilder implements FudgeBuilder<SyntheticIdentifierCurveInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SyntheticIdentifierCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("ccy", object.getCurrency().getCode());
    message.add("stripType", object.getType().name());
    message.add("scheme", object.getScheme().getName());
    return message;
  }

  @Override
  public SyntheticIdentifierCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency ccy = Currency.of(message.getString("ccy"));
    final StripInstrumentType stripType = StripInstrumentType.valueOf(message.getString("stripType"));
    final ExternalScheme scheme = ExternalScheme.of(message.getString("scheme"));
    return new SyntheticIdentifierCurveInstrumentProvider(ccy, stripType, scheme);
  }

}
