/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.testng.annotations.Test;

/**
 * Test the {@link ValueRequirementNamesFunction} class.
 */
public class ValueRequirementNamesFunctionTest {

  @Test
  public void testGetValueRequirementNames() {
    final Set<String> valueNames = ValueRequirementNamesFunction.getValueRequirementNames();
    assertNotNull(valueNames);
    assertFalse(valueNames.isEmpty());
    /*
    for (String valueName : valueNames) {
      System.out.println(valueName);
    }
    */
  }

}
