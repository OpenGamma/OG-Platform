/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.opengamma.util.PlatformConfigUtils;

/**
 * Extend from this to verify that a Spring configuration is valid in each of the
 * platform configurations. This is to spot changes made to the code that prevent
 * the beans from being instantiated properly. 
 */
@RunWith(Parameterized.class)
public abstract class AbstractPlatformSpringContextValidationTest extends AbstractSpringContextValidationTest {

  protected AbstractPlatformSpringContextValidationTest(final String opengammaPlatformRunmode) {
    PlatformConfigUtils.configureSystemProperties(opengammaPlatformRunmode);
  }

  @Parameters
  public static Collection<Object[]> getParameters() {
    final Collection<Object[]> params = new ArrayList<Object[]>(2);
    params.add(new Object[] {"shareddev"});
    params.add(new Object[] {"standalone"});
    return params;
  }

}
