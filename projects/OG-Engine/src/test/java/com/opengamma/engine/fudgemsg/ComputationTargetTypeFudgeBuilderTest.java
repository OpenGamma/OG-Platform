/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ComputationTargetTypeFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetTypeFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  private static final class Foo implements UniqueIdentifiable {

    @Override
    public UniqueId getUniqueId() {
      return null;
    }

  }

  public void testNull() {
    assertEncodeDecodeCycle(ComputationTargetType.class, ComputationTargetType.NULL);
  }

  public void testCombinations() {
    final ComputationTargetType[] types = new ComputationTargetType[99];
    int i = 0, j = 0;
    types[i++] = ComputationTargetType.PRIMITIVE;
    types[i++] = ComputationTargetType.POSITION;
    types[i++] = ComputationTargetType.of(Foo.class);
    while (i < types.length) {
      final ComputationTargetType type = types[j++];
      types[i++] = type.containing(ComputationTargetType.PRIMITIVE);
      types[i++] = ComputationTargetType.PORTFOLIO_NODE.containing(type);
      types[i++] = type.or(types[j]);
    }
    for (ComputationTargetType type : types) {
      assertEncodeDecodeCycle(ComputationTargetType.class, type);
    }
  }

}
