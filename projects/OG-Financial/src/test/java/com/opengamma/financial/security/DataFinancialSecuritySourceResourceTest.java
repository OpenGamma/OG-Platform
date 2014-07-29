/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataFinancialSecuritySourceResourceTest {

  private FinancialSecuritySource _underlying;
  private UriInfo _uriInfo;
  private DataFinancialSecuritySourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(FinancialSecuritySource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataFinancialSecuritySourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void testSearchBonds() {
    BondSecurity target = new GovernmentBondSecurity("US TREASURY N/B", "Government", "US", "Treasury", Currency.USD,
        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(zdt(2011, 2, 1, 12, 0, 0, 0, ZoneOffset.UTC)), "", 200,
        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), DayCounts.ACT_ACT_ISDA,
        zdt(2011, 2, 1, 12, 0, 0, 0, ZoneOffset.UTC), zdt(2011, 2, 1, 12, 0, 0, 0, ZoneOffset.UTC),
        zdt(2011, 2, 1, 12, 0, 0, 0, ZoneOffset.UTC), 100d, 100000000, 5000, 1000, 100, 100);
    target.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B")));
    Collection targetColl = ImmutableList.<Security>of(target);
    
    when(_underlying.getBondsWithIssuerName(eq("US TREASURY N/B"))).thenReturn(targetColl);
    
    Response test = _resource.searchBonds("US TREASURY N/B");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(FudgeListWrapper.of(targetColl), test.getEntity());
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
