/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import org.testng.annotations.Test;

import com.opengamma.component.ComponentInfo;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ComponentInfoFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    ComponentInfo object = new ComponentInfo(String.class, "shared");
    object.addAttribute("A", "B");
    assertEncodeDecodeCycle(ComponentInfo.class, object);
  }

}
