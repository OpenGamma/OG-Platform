/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecuritySourceServiceNames.DEFAULT_SECURITYSOURCE_NAME;
import static com.opengamma.financial.security.rest.SecuritySourceServiceNames.SECURITYSOURCE_SECURITY;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Arrays;
import java.util.Collection;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgFormatter;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.core.security.Security;
import com.opengamma.engine.test.MockSecurity;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.MockFinancialSecuritySource;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.rest.SecuritySourceResource;
import com.opengamma.financial.security.rest.SecuritySourceService;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.ObjectsPairFudgeBuilder;

/**
 * Test RESTful security source.
 */
public class RESTMethodTest {

  private final SecuritySourceService _securitySourceService = new SecuritySourceService(OpenGammaFudgeContext.getInstance());
  private UniqueId _uid1;
  private UniqueId _uid2;

  protected SecuritySourceService getSecuritySourceService() {
    return _securitySourceService;
  }

  protected SecuritySourceResource getSecuritySourceResource() {
    return getSecuritySourceService().findResource(DEFAULT_SECURITYSOURCE_NAME);
  }

  @BeforeMethod
  public void configureService() {
    MockFinancialSecuritySource securitySource = new MockFinancialSecuritySource();
    ExternalId secId1 = ExternalId.of(ExternalScheme.of("d1"), "v1");
    ExternalId secId2 = ExternalId.of(ExternalScheme.of("d2"), "v2");
    
    MockSecurity sec1 = new MockSecurity("t1");
    sec1.setExternalIdBundle(ExternalIdBundle.of(secId1));
    sec1.setSecurityType("BOND");
    securitySource.addSecurity(sec1);
    
    MockSecurity sec2 = new MockSecurity("t2");
    sec2.setExternalIdBundle(ExternalIdBundle.of(secId2));
    securitySource.addSecurity(sec2);
    
    BondSecurity bondSec = new GovernmentBondSecurity("US TREASURY N/B", "Government", "US", "Treasury", Currency.USD,
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC)), "", 200,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), DayCountFactory.INSTANCE.getDayCount("Actual/Actual"),
        ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC), ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC),
        ZonedDateTime.of(2011, 2, 1, 12, 0, 0, 0, TimeZone.UTC), 100, 100000000, 5000, 1000, 100, 100);
    bondSec.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B")));
    securitySource.addSecurity(bondSec);
    
    getSecuritySourceService().setUnderlying(securitySource);
    _uid1 = sec1.getUniqueId();
    _uid2 = sec2.getUniqueId();
  }

  @Test
  public void testFindSecuritySource() {
    assertNull(getSecuritySourceService().findResource("woot"));
    assertNotNull(getSecuritySourceResource());
  }

  @Test
  public void testGetSecurityByIdentifier() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurity(_uid1.toString());
    checkSecurityMessage(fme);
  }
  
  @Test
  public void testGetBulkSecuritiesByUniqueId() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurities(Lists.newArrayList(_uid1.toString(), _uid2.toString()));
    assertNotNull(fme);
    final FudgeMsg msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final Collection<FudgeField> securities = msg.getAllByName(SECURITYSOURCE_SECURITY);
    assertNotNull(securities);
    assertEquals(2, securities.size());
    FudgeDeserializer deserializer = new FudgeDeserializer(getSecuritySourceResource().getFudgeContext());
    for (FudgeField fudgeField : securities) {
      assertNotNull(fudgeField);
      ObjectsPair<UniqueId, Security> objectsPair = ObjectsPairFudgeBuilder.buildObject(deserializer, (FudgeMsg) fudgeField.getValue(), UniqueId.class, Security.class);
      assertNotNull(objectsPair);
      UniqueId uniqueId = objectsPair.getKey();
      assertNotNull(uniqueId);
      Security security = objectsPair.getValue();
      assertNotNull(security);
    }
  }

  @Test
  public void testGetSecurityByBundle() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurity(Arrays.asList("d1~v1"), null, null);
    checkSecurityMessage(fme);
  }
  
  @Test
  public void testGetSecurityByBundleVersionCorrection() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurity(Arrays.asList("d1~v1"), Instant.now().toString(), Instant.now().toString());
    checkSecurityMessage(fme);
  }
  
  private void checkSecurityMessage(final FudgeMsgEnvelope fme) {
    assertNotNull(fme);
    final FudgeMsg msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final FudgeMsg security = msg.getFieldValue(FudgeMsg.class, msg.getByName(SECURITYSOURCE_SECURITY));
    assertNotNull(security);
  }

  @Test
  public void testGetSecurities() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurities(Arrays.asList("d1~v1", "d2~v2"), null, null);
    checkSecuritiesMessage(fme);
  }
  
  @Test
  public void testGetSecuritiesVersionCorrection() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getSecurities(Arrays.asList("d1~v1", "d2~v2"), Instant.now().toString(), Instant.now().toString());
    checkSecuritiesMessage(fme);
  }
  
  private void checkSecuritiesMessage(final FudgeMsgEnvelope fme) {
    assertNotNull(fme);
    final FudgeMsg msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final Collection<FudgeField> securities = msg.getAllByName(SECURITYSOURCE_SECURITY);
    assertNotNull(securities);
    assertEquals(2, securities.size());
  }

  @Test
  public void testGetAllBondsOfIssuerType() {
    final FudgeMsgEnvelope fme = getSecuritySourceResource().getBondsWithIssuerName("US TREASURY N/B");
    assertNotNull(fme);
    final FudgeMsg msg = fme.getMessage();
    assertNotNull(msg);
    FudgeMsgFormatter.outputToSystemOut(msg);
    final Collection<FudgeField> securities = msg.getAllByName(SECURITYSOURCE_SECURITY);
    assertNotNull(securities);
    assertEquals(1, securities.size());
  }

}
