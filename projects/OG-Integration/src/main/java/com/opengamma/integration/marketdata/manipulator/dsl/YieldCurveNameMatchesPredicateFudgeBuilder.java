/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.regex.Pattern;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.base.Predicate;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.util.ArgumentChecker;

@FudgeBuilderFor(YieldCurveNameMatchesPredicate.class)
public class YieldCurveNameMatchesPredicateFudgeBuilder implements FudgeBuilder<YieldCurveNameMatchesPredicate> {

  private static final String REGEX = "regex";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, YieldCurveNameMatchesPredicate object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, REGEX, null, object.getPattern().toString());
    return msg;
  }

  @Override
  public YieldCurveNameMatchesPredicate buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    @SuppressWarnings("unchecked")
    String regex = deserializer.fieldValueToObject(String.class, msg.getByName(REGEX));
    return new YieldCurveNameMatchesPredicate(regex);
  }
}

/* package */ class YieldCurveNameMatchesPredicate implements Predicate<YieldCurveKey> {

  private final Pattern _pattern;

  /* package */ YieldCurveNameMatchesPredicate(String regex) {
    ArgumentChecker.notEmpty(regex, "regex");
    _pattern = Pattern.compile(regex);
  }

  @Override
  public boolean apply(YieldCurveKey curveKey) {
    return _pattern.matcher(curveKey.getName()).matches();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return _pattern.equals(((YieldCurveNameMatchesPredicate) o)._pattern);
  }

  @Override
  public int hashCode() {
    return _pattern.hashCode();
  }

  /* package */ Pattern getPattern() {
    return _pattern;
  }
}
