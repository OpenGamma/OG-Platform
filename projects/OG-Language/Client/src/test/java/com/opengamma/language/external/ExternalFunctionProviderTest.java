/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.language.function.MetaFunction;

/**
 * Tests the {@link ExternalFunctionProvider} class.
 */
@Test
public class ExternalFunctionProviderTest {

  @ExternalFunction
  public ExternalFunctionProviderTest() {
  }

  public void testGetFunctions() {
    final ExternalFunctionProvider provider = new ExternalFunctionProvider();
    final List<MetaFunction> functions = provider.getFunctions();
    assertNotNull(functions);
    boolean found = false;
    for (MetaFunction function : functions) {
      if ("ExternalFunctionProviderTest".equals(function.getName())) {
        found = true;
      }
    }
    assertTrue(found);
  }

}
