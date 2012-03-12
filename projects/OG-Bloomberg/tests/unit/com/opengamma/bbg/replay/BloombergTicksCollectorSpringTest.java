/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractSpringContextValidationTestNG;

@Test(enabled = false, description = "Properties file refers to absolute paths which won't work")
public class BloombergTicksCollectorSpringTest extends AbstractSpringContextValidationTestNG {

  @Test(enabled = false, dataProvider = "runModes", dataProviderClass = AbstractSpringContextValidationTestNG.class)
  public void testSpringContext(final String opengammaPlatformRunmode) {
    loadClassPathResource(opengammaPlatformRunmode, BloombergTicksCollectorLauncher.CONFIG_XML_CLASSPATH);
    assertContextLoaded();
  }
  
  @AfterMethod
  public void runAfter() {
    getSpringContext().close();
  }

}
