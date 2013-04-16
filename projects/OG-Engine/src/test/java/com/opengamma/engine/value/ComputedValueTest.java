/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test ComputedValue.
 */
@Test(groups = TestGroup.UNIT)
public class ComputedValueTest {
  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test_constructor_Object_Portfolio() {
    ValueSpecification vspec = ValueSpecification.of("DATA", ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar"), ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionid").get());
    ComputedValue test = new ComputedValue(vspec, "HELLO");
    assertEquals("HELLO", test.getValue());
    assertEquals(vspec, test.getSpecification());
  }

  public static class ComplexValue {
    private final double _i;
    private final double _j;

    public ComplexValue(final double i, final double j) {
      _i = i;
      _j = j;
    }

    public double getI() {
      return _i;
    }

    public double getJ() {
      return _j;
    }

    public static ComplexValue fromFudgeMsg(final FudgeMsg msg) {
      return new ComplexValue(msg.getDouble("i"), msg.getDouble("j"));
    }

    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof ComplexValue) {
        final ComplexValue other = (ComplexValue) obj;
        return (other._i == _i) && (other._j == _j);
      }
      return false;
    }
  }

  private void cycleComputedValue(final ComputedValue value) {
    final FudgeMsgEnvelope fme = s_fudgeContext.toFudgeMsg(value);
    assertNotNull(fme);
    final FudgeMsg msg = fme.getMessage();
    assertNotNull(msg);
    final ComputedValue cycledValue = s_fudgeContext.fromFudgeMsg(ComputedValue.class, msg);
    assertNotNull(cycledValue);
    assertEquals(value, cycledValue);
  }

  private ValueSpecification createValueSpecification() {
    return new ValueSpecification("test", ComputationTargetSpecification.of(UniqueId.of("foo", "bar")),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
  }

  public void testDouble() {
    cycleComputedValue(new ComputedValue(createValueSpecification(), 3.1412d));
  }

  public void testInteger() {
    cycleComputedValue(new ComputedValue(createValueSpecification(), 12345678));
  }

  @Test
  public void testSubMessage() {
    cycleComputedValue(new ComputedValue(createValueSpecification(), new ComplexValue(1d, 2d)));
  }

}
