/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

public class GroovyDslRunnerTest {

  @Test
  public void runScript() {
    Simulation simulation = GroovyDslRunner.runScript("src/test/resources/scenarios/ScenariosTest.groovy");
    assertNotNull(simulation);
    //System.out.println(simulation);
  }
}
