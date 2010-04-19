/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.Identifier;

/**
 * 
 *
 * @author Andrew Griffin
 */
public class ComputedValueTest {
  
  public static class ComplexValue {
    private final double _i;
    private final double _j;
    public ComplexValue (final double i, final double j) {
      _i = i;
      _j = j;
    }
    public double getI () {
      return _i;
    }
    public double getJ () {
      return _j;
    }
    public static ComplexValue fromFudgeMsg (final FudgeFieldContainer msg) {
      return new ComplexValue (msg.getDouble ("i"), msg.getDouble ("j"));
    }
    public boolean equals (final Object o) {
      if (o == this) return true;
      if (o == null) return false;
      if (!(o instanceof ComplexValue)) return false;
      final ComplexValue other = (ComplexValue)o;
      return (other._i == _i) && (other._j == _j);
    }
  }
  
  private void cycleComputedValue (final ComputedValue value) {
    final FudgeMsgEnvelope fme = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (value);
    assertNotNull (fme);
    final FudgeFieldContainer msg = fme.getMessage ();
    assertNotNull (msg);
    System.out.println (msg);
    final ComputedValue cycledValue = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg (ComputedValue.class, msg);
    assertNotNull (cycledValue);
    assertEquals (value, cycledValue);
  }
  
  private ValueSpecification createValueSpecification () {
    return new ValueSpecification (new ValueRequirement ("test", ComputationTargetType.PRIMITIVE, new Identifier ("foo", "bar")));
  }
  
  @Test
  public void testDouble () {
    cycleComputedValue (new ComputedValue (createValueSpecification (), (Double)3.1412));
  }
  
  @Test
  public void testInteger () {
    cycleComputedValue (new ComputedValue (createValueSpecification (), (Integer)12345678));
  }
  
  @Test
  public void testSubMessage () {
    cycleComputedValue (new ComputedValue (createValueSpecification (), new ComplexValue (1d, 2d)));
  }
  
}