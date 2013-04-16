/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class TestStatefulFunction {
  private final boolean _boolean;
  private final int _int;

  public TestStatefulFunction(final boolean aBoolean, final int anInt) {
    _boolean = aBoolean;
    _int = anInt;
  }

  public Function<Double, Double> getSurface(final Double a, final Double b) {
    return new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return _boolean ? _int * a * x[0] : a * x[0] + b * x[1] * x[1];
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(TestStatefulFunction.this, "getSurface", a, b);
      }
    };
  }

  public FudgeMsg toFudgeMsg(final FudgeMsgFactory f) {
    final MutableFudgeMsg msg = f.newMessage();
    msg.add("boolean", _boolean);
    msg.add("int", _int);
    return msg;
  }

  public static TestStatefulFunction fromFudgeMsg(final FudgeMsg msg) {
    return new TestStatefulFunction(msg.getBoolean("boolean"), msg.getInt("int"));
  }

}
