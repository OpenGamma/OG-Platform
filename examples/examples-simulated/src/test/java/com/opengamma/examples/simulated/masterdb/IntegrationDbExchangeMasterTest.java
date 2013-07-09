/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.masterdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.threeten.bp.ZoneId;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.integration.masterdb.AbstractIntegrationDbExchangeMasterTest;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbExchangeMaster.
 */
@Test(groups = TestGroup.INTEGRATION)
public class IntegrationDbExchangeMasterTest extends AbstractIntegrationDbExchangeMasterTest {

  @Test
  public void test_querySampleEntry() throws Exception {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("London Stock Exchange");
    final ExchangeSearchResult result = getExchangeMaster().search(request);
    assertEquals(1, result.getDocuments().size());
    final ExchangeDocument doc = result.getFirstDocument();
    assertNotNull(doc.getVersionFromInstant());
    assertNull(doc.getVersionToInstant());
    assertNotNull(doc.getCorrectionFromInstant());
    assertNull(doc.getCorrectionToInstant());
    assertEquals("London Stock Exchange", doc.getExchange().getName());
    assertEquals(ZoneId.of("Europe/London"), doc.getExchange().getTimeZone());
    assertEquals(true, doc.getExchange().getRegionIdBundle().contains(ExternalSchemes.countryRegionId(Country.GB)));
  }

}
