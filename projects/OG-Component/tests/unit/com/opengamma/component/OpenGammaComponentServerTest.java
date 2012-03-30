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

/**
 * Test.
 */
@Test
public class OpenGammaComponentServerTest extends AbstractFudgeBuilderTestCase {

  @DataProvider(name = "extractName")
  public Object[][] data_extractName() {
      return new Object[][] {
        {"classpath:toolcontext/toolcontext-dev.properties", "toolcontext"},
        {"classpath:foobar/toolcontext-dev.properties", "foobar-toolcontext"},
        
        {"classpath:foobar/toolcontext-dev.properties", "foobar-toolcontext"},
        {"classpath:foobar/toolcontext.properties", "foobar-toolcontext"},
        {"classpath:foobar/toolcontext-dev-bar-foo.properties", "foobar-toolcontext"},
        
        {"classpath:toolcontext/toolcontext-dev.ini", "toolcontext"},
        {"classpath:foobar/toolcontext-dev.ini", "foobar-toolcontext"},
        
        {"classpath:toolcontext-dev.ini", "toolcontext"},
        
        {"file:toolcontext-dev.ini", "toolcontext"},
      };
  }

  @Test(dataProvider = "extractName")
  public void test_dash_properties(String input, String expected) {
    OpenGammaComponentServer test = new OpenGammaComponentServer();
    assertEquals(expected, test.extractServerName(input));
  }

}
