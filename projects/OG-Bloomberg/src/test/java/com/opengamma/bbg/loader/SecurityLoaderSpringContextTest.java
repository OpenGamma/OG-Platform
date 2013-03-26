/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractSpringContextValidationTestNG;
import com.opengamma.util.test.TestGroup;

/**
 * Test Spring.
 */
@Test(groups = TestGroup.INTEGRATION)
public class SecurityLoaderSpringContextTest extends AbstractSpringContextValidationTestNG {

  @Test(enabled=false)
  public void testSecurityLoaderBean(final String opengammaPlatformRunmode) {
    loadClassPathResource(BloombergSecurityFileLoader.CONTEXT_CONFIGURATION_PATH);
    assertContextLoaded();
    assertBeanExists(BloombergSecurityFileLoader.class, "securityLoader");
  }

  @AfterMethod
  public void runAfter() {
    getSpringContext().close();
  }

}
