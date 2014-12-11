/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ScenarioDataBuilderTest {

  @Test
  public void addSecurityMarketValue() {
    Security security =
        new SimpleSecurity(UniqueId.parse("test~123"), ExternalId.parse("ex~123").toBundle(), "TEST", "test security");

    ScenarioMarketDataEnvironment env =
        new ScenarioDataBuilder()
            .addSecurityMarketValue("1", security, 1d)
            .addSecurityMarketValue("2", security, 2d)
            .addSecurityMarketValue("3", security, 3d)
            .valuationTime("1", ZonedDateTime.now())
            .valuationTime("2", ZonedDateTime.now())
            .valuationTime("3", ZonedDateTime.now())
            .build();

    SingleValueRequirement req = SingleValueRequirement.of(SecurityId.of(security));
    assertEquals(1d, env.getData().get("1").getData().get(req));
    assertEquals(2d, env.getData().get("2").getData().get(req));
    assertEquals(3d, env.getData().get("3").getData().get(req));
  }

  @Test
  public void addSecurityValue() {
    Security security =
        new SimpleSecurity(UniqueId.parse("test~123"), ExternalId.parse("ex~123").toBundle(), "TEST", "test security");

    ScenarioMarketDataEnvironment env =
        new ScenarioDataBuilder()
            .addSecurityValue("1", security, FieldName.of("foo"), 1d)
            .addSecurityValue("2", security, FieldName.of("bar"), 2d)
            .addSecurityValue("3", security, FieldName.of("baz"), LocalDate.of(2011, 3, 8))
            .valuationTime("1", ZonedDateTime.now())
            .valuationTime("2", ZonedDateTime.now())
            .valuationTime("3", ZonedDateTime.now())
            .build();

    SingleValueRequirement req1 = SingleValueRequirement.of(SecurityId.of(security, Double.class, FieldName.of("foo")));
    SingleValueRequirement req2 = SingleValueRequirement.of(SecurityId.of(security, Double.class, FieldName.of("bar")));
    SingleValueRequirement req3 = SingleValueRequirement.of(SecurityId.of(security, LocalDate.class, FieldName.of("baz")));
    assertEquals(1d, env.getData().get("1").getData().get(req1));
    assertEquals(2d, env.getData().get("2").getData().get(req2));
    assertEquals(LocalDate.of(2011, 3, 8), env.getData().get("3").getData().get(req3));
  }
}
