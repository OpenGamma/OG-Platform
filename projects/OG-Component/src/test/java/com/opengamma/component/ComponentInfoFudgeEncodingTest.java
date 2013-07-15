/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentInfoFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    ComponentInfo object = new ComponentInfo(String.class, "shared");
    object.addAttribute("A", "B");
    assertEncodeDecodeCycle(ComponentInfo.class, object);
  }

}
