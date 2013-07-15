/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsImpl;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link AvailableOutputsFudgeBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class AvailableOutputsBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testCycleEmptyObject() {
    final AvailableOutputsImpl outputs = new AvailableOutputsImpl();
    final AvailableOutputs cycled = cycleObject(AvailableOutputs.class, outputs);
    assertTrue(cycled.getOutputs().isEmpty());
    assertTrue(cycled.getPortfolioNodeOutputs().isEmpty());
    assertTrue(cycled.getPositionOutputs().isEmpty());
    assertTrue(cycled.getSecurityTypes().isEmpty());
  }

  public void testCyclePopulatedObject() {
    final AvailableOutputsImpl outputs = new AvailableOutputsImpl();
    outputs.portfolioNodeOutput("V", ValueProperties.with("P", "A", "B").get());
    outputs.positionOutput("V", "Swap", ValueProperties.with("P", "A", "C").get());
    outputs.positionOutput("V", "Option", ValueProperties.with("P", "A", "D").get());
    final AvailableOutputs cycled = cycleObject(AvailableOutputs.class, outputs);
    assertEquals(cycled.getOutputs().size(), 1);
    AvailableOutput output = cycled.getOutputs().iterator().next();
    assertEquals(output.getValueName(), "V");
    assertEquals(output.getProperties(), ValueProperties.with("P", "A", "B", "C", "D").get());
    assertEquals(output.getSecurityTypes(), new HashSet<String>(Arrays.asList("Swap", "Option")));
    assertEquals(output.getPositionProperties("Swap"), ValueProperties.with("P", "A", "C").get());
    assertEquals(output.getPositionProperties("Option"), ValueProperties.with("P", "A", "D").get());
    assertEquals(output.getPortfolioNodeProperties(), ValueProperties.with("P", "A", "B").get());
    assertEquals(cycled.getPortfolioNodeOutputs().size(), 1);
    output = cycled.getPortfolioNodeOutputs().iterator().next();
    assertEquals(output.getValueName(), "V");
    assertEquals(output.getProperties(), ValueProperties.with("P", "A", "B").get());
    assertEquals(output.getPortfolioNodeProperties(), ValueProperties.with("P", "A", "B").get());
    assertEquals(cycled.getPositionOutputs().size(), 1);
    output = cycled.getPositionOutputs().iterator().next();
    assertEquals(output.getValueName(), "V");
    assertEquals(output.getProperties(), ValueProperties.with("P", "A", "C", "D").get());
    assertEquals(output.getSecurityTypes(), new HashSet<String>(Arrays.asList("Swap", "Option")));
    assertEquals(output.getPositionProperties("Swap"), ValueProperties.with("P", "A", "C").get());
    assertEquals(output.getPositionProperties("Option"), ValueProperties.with("P", "A", "D").get());
    assertEquals(cycled.getPositionOutputs("Swap").size(), 1);
    output = cycled.getPositionOutputs("Swap").iterator().next();
    assertEquals(output.getValueName(), "V");
    assertEquals(output.getProperties(), ValueProperties.with("P", "A", "C").get());
    assertEquals(output.getSecurityTypes(), new HashSet<String>(Arrays.asList("Swap")));
    assertEquals(cycled.getPositionOutputs("Option").size(), 1);
    output = cycled.getPositionOutputs("Option").iterator().next();
    assertEquals(output.getValueName(), "V");
    assertEquals(output.getProperties(), ValueProperties.with("P", "A", "D").get());
    assertEquals(output.getSecurityTypes(), new HashSet<String>(Arrays.asList("Option")));
  }

}
