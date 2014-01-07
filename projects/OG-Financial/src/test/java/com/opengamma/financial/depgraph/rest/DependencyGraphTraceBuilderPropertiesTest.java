/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphTraceBuilderPropertiesTest {

  @BeforeMethod
  public void beforeMethod() {
  }

  private DependencyGraphTraceBuilderProperties createBuilder() {
    return new DependencyGraphTraceBuilderProperties();
  }

  @Test
  public void valuationTime() {
    final DependencyGraphTraceBuilderProperties builder = createBuilder();
    final Instant i1 = builder.getValuationTime();
    Instant instant = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]").toInstant();
    final DependencyGraphTraceBuilderProperties prime = builder.valuationTime(instant);
    final Instant i2 = prime.getValuationTime();
    assertEquals(i1, builder.getValuationTime()); // original unchanged
    assertFalse(Objects.equals(i1, i2));
  }

  @Test
  public void calculationConfigurationName() {
    final DependencyGraphTraceBuilderProperties builder = createBuilder();
    final String c1 = builder.getCalculationConfigurationName();
    final DependencyGraphTraceBuilderProperties prime = builder.calculationConfigurationName("Foo");
    final String c2 = prime.getCalculationConfigurationName();
    assertEquals(c1, builder.getCalculationConfigurationName()); // original unchanged
    assertFalse(c1.equals(c2));
  }

  @Test
  public void defaultProperties() {
    ValueProperties valueProperties = ValueProperties.parse("A=[foo,bar],B=*");
    final DependencyGraphTraceBuilderProperties builder = createBuilder();
    final ValueProperties p1 = builder.getDefaultProperties();
    final DependencyGraphTraceBuilderProperties prime = builder.defaultProperties(valueProperties);
    final ValueProperties p2 = prime.getDefaultProperties();
    assertEquals(p1, builder.getDefaultProperties()); // original unchanged
    assertFalse(p1.equals(p2));
  }

  @Test
  public void addRequirement() {

    final ComputationTargetSpecification target = ComputationTargetSpecification.of(UniqueId.of("Scheme", "PrimitiveValue"));
    final ValueRequirement vr1 = new ValueRequirement("Value1", target);
    final ValueRequirement vr2 = new ValueRequirement("Value2", target);

    final DependencyGraphTraceBuilderProperties builder = createBuilder();
    final Collection<ValueRequirement> r1 = builder.getRequirements();
    final DependencyGraphTraceBuilderProperties prime = builder.addRequirement(vr1);
    final Collection<ValueRequirement> r2 = prime.getRequirements();
    final DependencyGraphTraceBuilderProperties prime2 = prime.addRequirement(vr2);
    final Collection<ValueRequirement> r3 = prime2.getRequirements();
    assertEquals(r1, builder.getRequirements()); // original unchanged
    assertEquals(r2, prime.getRequirements()); // unchanged
    assertEquals(r1.size(), 0);
    assertEquals(r2.size(), 1);
    assertEquals(r3.size(), 2);
  }

  @Test
  public void marketData() {
    String snapshotId = "Foo~1";
    List<MarketDataSpecification> marketData = Lists.<MarketDataSpecification>newArrayList(MarketData.user(UniqueId.parse(snapshotId)));

    final DependencyGraphTraceBuilderProperties builder1 = createBuilder();
    List<MarketDataSpecification> defaultMD = builder1.getMarketData();
    final DependencyGraphTraceBuilderProperties builder2 = builder1.marketData(marketData);

    assertEquals(defaultMD, builder1.getMarketData());
    assertEquals(marketData, builder2.getMarketData());

  }

  @Test
  public void addMarketData() {
    String snapshotId = "Foo~1";
    UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));

    final DependencyGraphTraceBuilderProperties builder1 = createBuilder();
    List<MarketDataSpecification> defaultMD = builder1.getMarketData();
    final DependencyGraphTraceBuilderProperties builder2 = builder1.addMarketData(marketData);

    assertEquals(defaultMD, builder1.getMarketData());
    assertEquals(Lists.newArrayList(marketData), builder2.getMarketData());

  }

  @Test
  public void requirements() {
    Set<ValueRequirement> requirements = Collections.singleton(new ValueRequirement("testValue", new ComputationTargetRequirement(ComputationTargetType.POSITION, ExternalId.of("GOLDMAN", "FOO1"))));

    final DependencyGraphTraceBuilderProperties builder1 = createBuilder();
    Collection<ValueRequirement> defaultRequirements = builder1.getRequirements();
    final DependencyGraphTraceBuilderProperties builder2 = builder1.requirements(requirements);

    assertEquals(defaultRequirements, builder1.getRequirements());
    assertEquals(requirements, builder2.getRequirements());
  }

  @Test
  public void resolutionTime() {
    String rtStr = "V1970-01-01T00:00:01Z.CLATEST";
    VersionCorrection rt = VersionCorrection.parse(rtStr);

    final DependencyGraphTraceBuilderProperties builder1 = createBuilder();
    VersionCorrection defaultVC = builder1.getResolutionTime();
    final DependencyGraphTraceBuilderProperties builder2 = builder1.resolutionTime(rt);

    assertEquals(defaultVC, builder1.getResolutionTime());
    assertEquals(rt, builder2.getResolutionTime());
  }
}
