/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.language.definition.Parameter;
import com.opengamma.language.object.ObjectFunctionProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ObjectFunctionProvider} class to check it can load its definitions.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectFunctionProviderTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ObjectFunctionProviderTest.class);

  @Test
  public void testDefinitionsLoad() {
    final ObjectFunctionProvider provider = new ObjectFunctionProvider();
    final Set<MetaFunction> functions = provider.getDefinitions();
    assertNotNull(functions);
    assertFalse(functions.isEmpty());
    for (MetaFunction function : functions) {
      s_logger.info("{} {}", function.getName(), function.getDescription());
      for (Parameter parameter : function.getParameter()) {
        s_logger.info("\t{}\t{}", parameter.getName(), parameter.getDescription());
      }
    }
  }

}
