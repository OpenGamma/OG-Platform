/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.i18n.Country;

/**
 * Test MasterExchangeSource.
 */
@Test
public class MasterExchangeSourceTest {

  private static final UniqueId UID = UniqueId.of("A", "B");
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterExchangeSource(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterExchangeSource(null, null);
  }

  //-------------------------------------------------------------------------
  public void test_getExchange_noOverride_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterExchangeSource test = new MasterExchangeSource(mock);
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(example(), testResult);
  }

  public void test_getExchange_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(UID, VC)).thenReturn(doc);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(example(), testResult);
  }

  public void test_getExchange_notFound() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    when(mock.get(UID, VC)).thenThrow(new DataNotFoundException(""));
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSingleExchange_ExternalId_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getSingleExchange(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  public void test_getSingleExchange_ExternalId_notFound() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getSingleExchange(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSingleExchange_ExternalIdBundle_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getSingleExchange(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  //-------------------------------------------------------------------------
  protected ManageableExchange example() {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setUniqueId(UID);
    exchange.setName("NYSE");
    exchange.setRegionIdBundle(ExternalIdBundle.of(RegionUtils.countryRegionId(Country.US)));
    return exchange;
  }

}
