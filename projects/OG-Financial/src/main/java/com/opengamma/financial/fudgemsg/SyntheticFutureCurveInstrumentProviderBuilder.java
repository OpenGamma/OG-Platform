/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.ircurve.SyntheticFutureCurveInstrumentProvider;

/**
 * Fudge builder for {@link SyntheticFutureCurveInstrumentProvider}
 */
@FudgeBuilderFor(SyntheticFutureCurveInstrumentProvider.class)
public class SyntheticFutureCurveInstrumentProviderBuilder implements FudgeBuilder<SyntheticFutureCurveInstrumentProvider> {
  /** The future prefix field */
  private static final String PREFIX_FIELD = "prefix";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SyntheticFutureCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(PREFIX_FIELD, object.getFuturePrefix());
    return message;
  }

  @Override
  public SyntheticFutureCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String futurePrefix = message.getString(PREFIX_FIELD);
    return new SyntheticFutureCurveInstrumentProvider(futurePrefix);
  }

}
