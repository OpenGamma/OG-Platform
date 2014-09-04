/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class OpenGammaComponentServerTest extends AbstractFudgeBuilderTestCase {

  @DataProvider(name = "extractName")
  Object[][] data_extractName() {
      return new Object[][] {
        {"classpath:/toolcontext/toolcontext-dev.properties", "toolcontext-dev"},
        {"classpath:/foobar/toolcontext-dev.properties", "foobar-toolcontext-dev"},
        
        {"classpath:/foobar/toolcontext-dev.properties", "foobar-toolcontext-dev"},
        {"classpath:/foobar/toolcontext.properties", "foobar-toolcontext"},
        {"classpath:/foobar/toolcontext-dev-bar-foo.properties", "foobar-toolcontext-dev-bar-foo"},
        
        {"classpath:/toolcontext/toolcontext-dev.ini", "toolcontext-dev"},
        {"classpath:/foobar/toolcontext-dev.ini", "foobar-toolcontext-dev"},
        
        {"classpath:/toolcontext-dev.ini", "toolcontext-dev"},
        
        {"file:toolcontext-dev.ini", "toolcontext-dev"},
      };
  }

  @Test(dataProvider = "extractName")
  public void test_dash_properties(String input, String expected) {
    OpenGammaComponentServer test = new OpenGammaComponentServer();
    assertEquals(expected, test.extractServerName(input));
  }

}
