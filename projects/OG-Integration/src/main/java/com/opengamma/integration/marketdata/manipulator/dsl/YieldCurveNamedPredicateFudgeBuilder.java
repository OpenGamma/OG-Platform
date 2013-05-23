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
import com.opengamma.util.ArgumentChecker;

@FudgeBuilderFor(YieldCurveNamedPredicate.class)
public class YieldCurveNamedPredicateFudgeBuilder implements FudgeBuilder<YieldCurveNamedPredicate> {

  private static final String NAMES = "names";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, YieldCurveNamedPredicate object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, NAMES, null, ImmutableList.copyOf(object.getNames()));
    return msg;
  }

  @Override
  public YieldCurveNamedPredicate buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    @SuppressWarnings("unchecked")
    List<String> names = deserializer.fieldValueToObject(List.class, msg.getByName(NAMES));
    return new YieldCurveNamedPredicate(names.toArray(new String[names.size()]));
  }
}

/* package */ class YieldCurveNamedPredicate implements Predicate<YieldCurveKey> {

  private final Set<String> _names;

  /* package */ YieldCurveNamedPredicate(String... names) {
    ArgumentChecker.notEmpty(names, "names");
    _names = ImmutableSet.copyOf(names);
  }

  @Override
  public boolean apply(YieldCurveKey curveKey) {
    return _names.contains(curveKey.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return _names.equals(((YieldCurveNamedPredicate) o)._names);
  }

  @Override
  public int hashCode() {
    return _names.hashCode();
  }

  /* package */ Set<String> getNames() {
    return _names;
  }
}
