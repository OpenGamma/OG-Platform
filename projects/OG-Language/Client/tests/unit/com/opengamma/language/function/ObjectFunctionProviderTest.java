/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.testng.annotations.Test;

/**
 * Tests the {@link ObjectFunctionProvider} class to check it can load its definitions.
 */
public class ObjectFunctionProviderTest {

  @Test
  public void testDefinitionsLoad() {
    final ObjectFunctionProvider provider = new ObjectFunctionProvider();
    final Set<MetaFunction> functions = provider.getDefinitions();
    assertNotNull(functions);
    assertFalse(functions.isEmpty());
    /*
    for (MetaFunction function : functions) {
      System.out.println(function.getName() + "\t" + function.getDescription());
      for (Parameter parameter : function.getParameter()) {
        System.out.println("\t" + parameter.getName() + "\t" + parameter.getDescription());
      }
    }
    */
  }

}
