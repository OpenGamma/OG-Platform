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
public class HistoricalLoaderSpringContextTest extends AbstractSpringContextValidationTestNG {

  @Test(enabled=false, dataProvider = "runModes", dataProviderClass = AbstractSpringContextValidationTestNG.class)
  public void testHistoricalLoaderBean(final String opengammaPlatformRunmode) {
    loadClassPathResource(opengammaPlatformRunmode, BloombergHistoricalLoader.CONTEXT_CONFIGURATION_PATH);
    assertContextLoaded();
    assertBeanExists(BloombergHistoricalLoader.class, "missingHistoricalDataLoader");
  }
  
  @AfterMethod
  public void runAfter() {
    getSpringContext().close();
  }

}
