/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractSpringContextValidationTestNG;

/**
 * Test Spring xml file.
 */
public class SecurityLoaderSpringContextTest extends AbstractSpringContextValidationTestNG {

  @Test(enabled=false, dataProvider = "runModes", dataProviderClass = AbstractSpringContextValidationTestNG.class)
  public void testSecurityLoaderBean(final String opengammaPlatformRunmode) {
    loadClassPathResource(opengammaPlatformRunmode, BloombergSecurityFileLoader.CONTEXT_CONFIGURATION_PATH);
    assertContextLoaded();
    assertBeanExists(BloombergSecurityFileLoader.class, "securityLoader");
  }
  
  @AfterMethod
  public void runAfter() {
    getSpringContext().close();
  }

}
