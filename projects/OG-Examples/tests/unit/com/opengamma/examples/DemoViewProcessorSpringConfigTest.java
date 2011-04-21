/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples;

import org.testng.annotations.Test;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.test.AbstractSpringContextValidationTestNG;

/**
 * Test Spring xml.
 */
public class DemoViewProcessorSpringConfigTest extends AbstractSpringContextValidationTestNG {

  @Test(dataProvider = "runModes", dataProviderClass = AbstractSpringContextValidationTestNG.class)
  public void testViewProcessorLoaderBean(final String opengammaPlatformRunmode) {
    loadFileSystemResource(opengammaPlatformRunmode, "config/demoViewProcessor.xml");
    assertContextLoaded();
    assertBeanExists(ViewProcessor.class, "demoViewProcessor");
  }

}
