/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Notification that a market data provider has become available.
 */
public class MarketDataAvailabilityNotification {

  /** Field name for Fudge message. */
  private static final String SCHEMES = "schemes";

  /** Schemes handled by the provider. */
  private final Set<ExternalScheme> _schemes;

  /**
   * @param schemes Schemes handled by the provider.
   */
  public MarketDataAvailabilityNotification(Set<ExternalScheme> schemes) {
    ArgumentChecker.notEmpty(schemes, "schemes");
    _schemes = ImmutableSet.copyOf(schemes);
  }

  /**
   * @return The schemes handled by the market data provider that has become available.
   */
  public Set<ExternalScheme> getSchemes() {
    return _schemes;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    MutableFudgeMsg schemesMsg = serializer.newMessage();
    for (ExternalScheme scheme : _schemes) {
      serializer.addToMessage(schemesMsg, null, null, scheme.getName());
    }
    serializer.addToMessage(msg, SCHEMES, null, schemesMsg);
    return msg;
  }

  public static MarketDataAvailabilityNotification fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    FudgeMsg schemesMsg = msg.getMessage(SCHEMES);
    Set<ExternalScheme> schemes = Sets.newHashSet();
    for (FudgeField field : schemesMsg) {
      String schemeName = deserializer.fieldValueToObject(String.class, field);
      schemes.add(ExternalScheme.of(schemeName));
    }
    return new MarketDataAvailabilityNotification(schemes);
  }

  @Override
  public String toString() {
    return "MarketDataAvailabilityNotification [_schemes=" + _schemes + "]";
  }
}
