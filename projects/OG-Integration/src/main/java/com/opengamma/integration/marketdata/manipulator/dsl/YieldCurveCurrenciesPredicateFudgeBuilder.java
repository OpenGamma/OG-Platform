/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;

@FudgeBuilderFor(YieldCurveCurrenciesPredicate.class)
public class YieldCurveCurrenciesPredicateFudgeBuilder implements FudgeBuilder<YieldCurveCurrenciesPredicate> {

  private static final String CURRENCIES = "currencies";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, YieldCurveCurrenciesPredicate object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CURRENCIES, null, ImmutableList.copyOf(object.getCodes()));
    return msg;
  }

  @Override
  public YieldCurveCurrenciesPredicate buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    @SuppressWarnings("unchecked")
    List<String> codes = deserializer.fieldValueToObject(List.class, msg.getByName(CURRENCIES));
    return new YieldCurveCurrenciesPredicate(codes.toArray(new String[codes.size()]));
  }
}

/* package */ class YieldCurveCurrenciesPredicate implements Predicate<YieldCurveKey> {

  private final Set<String> _codes;

  /* package */ YieldCurveCurrenciesPredicate(String... codes) {
    _codes = ImmutableSet.copyOf(codes);
  }

  @Override
  public boolean apply(YieldCurveKey curve) {
    return _codes.contains(curve.getCurrency().getCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return _codes.equals(((YieldCurveCurrenciesPredicate) o)._codes);
  }

  @Override
  public int hashCode() {
    return _codes.hashCode();
  }

  /* package */ Set<String> getCodes() {
    return _codes;
  }
}
