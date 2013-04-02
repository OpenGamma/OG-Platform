/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfigurationImpl;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link CompiledViewCalculationConfigurationFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class CompiledViewCalculationConfigurationFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmpty() {
    final CompiledViewCalculationConfiguration in = new CompiledViewCalculationConfigurationImpl("1", Collections.<ComputationTargetSpecification>emptySet(),
        Collections.<ValueSpecification, Set<ValueRequirement>>emptyMap(), Collections.<ValueSpecification, Collection<ValueSpecification>>emptyMap());
    final CompiledViewCalculationConfiguration out = cycleObject(CompiledViewCalculationConfiguration.class, in);
    assertEquals(out.getName(), in.getName());
    assertEquals(out.getComputationTargets(), in.getComputationTargets());
    assertEquals(out.getMarketDataRequirements(), in.getMarketDataRequirements());
    assertEquals(out.getTerminalOutputSpecifications(), in.getTerminalOutputSpecifications());
  }

  public void testBasic() {
    final ComputationTargetRequirement targetReq = ComputationTargetRequirement.of(ExternalId.of("Foo", "Bar"));
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("Sec", "123"));
    final ValueSpecification valueSpecification = new ValueSpecification("Value", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo").get());
    final ValueRequirement valueRequirement = new ValueRequirement("Value", targetReq);
    final ValueSpecification dataSpecification1 = new ValueSpecification("Data1", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    final ValueSpecification dataSpecification2a = new ValueSpecification("Data2a", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    final ValueSpecification dataSpecification2b = new ValueSpecification("Data2b", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    final ValueSpecification dataSpecification3a = new ValueSpecification("Data3a", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    final ValueSpecification dataSpecification3b = new ValueSpecification("Data3b", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    final ValueSpecification dataSpecification3c = new ValueSpecification("Data3c", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    final CompiledViewCalculationConfiguration in = new CompiledViewCalculationConfigurationImpl("2", ImmutableSet.of(ComputationTargetSpecification.NULL, targetSpec),
        ImmutableMap.<ValueSpecification, Set<ValueRequirement>>of(valueSpecification, ImmutableSet.of(valueRequirement)), ImmutableMap.<ValueSpecification, Collection<ValueSpecification>>of(
            dataSpecification1, Collections.singleton(dataSpecification1),
            dataSpecification2a, Collections.singleton(dataSpecification2b),
            dataSpecification3a, ImmutableSet.of(dataSpecification3a, dataSpecification3b, dataSpecification3c)));
    final CompiledViewCalculationConfiguration out = cycleObject(CompiledViewCalculationConfiguration.class, in);
    assertEquals(out.getName(), in.getName());
    assertEquals(out.getComputationTargets(), in.getComputationTargets());
    assertEquals(out.getTerminalOutputSpecifications(), in.getTerminalOutputSpecifications());
    assertEquals(out.getMarketDataRequirements(), in.getMarketDataRequirements());
  }

}
