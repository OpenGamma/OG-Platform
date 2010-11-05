/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.time.Instant;

import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.world.exchange.Exchange;
import com.opengamma.financial.world.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Test MasterExchangeSource.
 */
public class MasterExchangeSourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final Identifier ID = Identifier.of("C", "D");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID);

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterExchangeSource(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterExchangeSource(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor3arg_nullMaster() throws Exception {
    new MasterExchangeSource(null, null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getExchange_found() throws Exception {
    Instant now = Instant.nowSystemClock();
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterExchangeSource test = new MasterExchangeSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(example(), testResult);
  }

  @Test
  public void test_getExchange_notFound() throws Exception {
    Instant now = Instant.nowSystemClock();
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    MasterExchangeSource test = new MasterExchangeSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSingleExchange_Identifier_found() throws Exception {
    Instant now = Instant.nowSystemClock();
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    request.setFullDetail(true);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Exchange testResult = test.getSingleExchange(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  @Test
  public void test_getSingleExchange_Identifier_noFound() throws Exception {
    Instant now = Instant.nowSystemClock();
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    request.setFullDetail(true);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Exchange testResult = test.getSingleExchange(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSingleExchange_IdentifierBundle_found() throws Exception {
    Instant now = Instant.nowSystemClock();
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    request.setFullDetail(true);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Exchange testResult = test.getSingleExchange(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  //-------------------------------------------------------------------------
  protected ManageableExchange example() {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setUniqueIdentifier(UID);
    exchange.setName("NYSE");
    exchange.setRegionId(IdentifierBundle.of(RegionUtils.countryRegionId("US")));
    return exchange;
  }

}
