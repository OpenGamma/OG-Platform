/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Objects;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A MarketDataSelector which specifies a yield curve to be shifted. Note that this
 * class is not responsible for specifying the actual manipulation to be done.
 */
public final class YieldCurveSelector implements DistinctMarketDataSelector {

  private static final String KEY = "structureId";

  private final YieldCurveKey _key;

  private YieldCurveSelector(YieldCurveKey key) {
    _key = ArgumentChecker.notNull(key, "key");
  }

  /**
   * Construct a specification for the supplied yield curve key.
   *
   * @param yieldCurveKey the key of the yield curve to be shifted, not null
   * @return a new MarketDataSelector for the yield curve
   */
  public static DistinctMarketDataSelector of(YieldCurveKey yieldCurveKey) {
    return new YieldCurveSelector(yieldCurveKey);
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(ValueSpecification valueSpecification,
                                                         String calculationConfigurationName,
                                                         SelectorResolver resolver) {
    Currency currency = Currency.of(valueSpecification.getTargetSpecification().getUniqueId().getValue());
    String curve = valueSpecification.getProperties().getStrictValue(ValuePropertyNames.CURVE);
    if (_key.getName().equals(curve) && _key.getCurrency().equals(currency)) {
      return this;
    } else {
      return null;
    }
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, KEY, null, _key);
    return msg;
  }

  @SuppressWarnings("unchecked")
  public static MarketDataSelector fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    YieldCurveKey key = deserializer.fieldValueToObject(YieldCurveKey.class, msg.getByName(KEY));
    return new YieldCurveSelector(key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_key);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveSelector other = (YieldCurveSelector) obj;
    return Objects.equals(this._key, other._key);
  }
}
