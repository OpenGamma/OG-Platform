/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link QuerySplittingPositionMaster} class.
 */
@Test(groups = TestGroup.UNIT)
public class QuerySplittingPositionMasterTest {

  public void testSearch_disabled() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final PositionSearchRequest request = new PositionSearchRequest();
    final PositionSearchResult result = new PositionSearchResult();
    Mockito.when(mock.search(request)).thenReturn(result);
    assertEquals(instance.getMaxSearchRequest(), 0);
    assertSame(instance.search(request), result);
  }

  public void testSearch_no_positions() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final PositionSearchRequest request = new PositionSearchRequest();
    final PositionSearchResult result = new PositionSearchResult();
    Mockito.when(mock.search(request)).thenReturn(result);
    instance.setMaxSearchRequest(Integer.MAX_VALUE);
    assertSame(instance.search(request), result);
  }

  public void testSearch_small() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("Foo", "Bar"));
    final PositionSearchResult result = new PositionSearchResult();
    Mockito.when(mock.search(request)).thenReturn(result);
    instance.setMaxSearchRequest(1);
    assertSame(instance.search(request), result);
  }

  public void testSearch_invalid_paging() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("Test", "1"));
    request.addPositionObjectId(ObjectId.of("Test", "2"));
    request.setPagingRequest(PagingRequest.FIRST_PAGE);
    final PositionSearchResult result = new PositionSearchResult();
    Mockito.when(mock.search(request)).thenReturn(result);
    instance.setMaxSearchRequest(1);
    assertSame(instance.search(request), result);
    request.setPagingRequest(PagingRequest.ONE);
    assertSame(instance.search(request), result);
  }

  public void testSearch_large_page_all() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final VersionCorrection now = VersionCorrection.of(Instant.now(), Instant.now());
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("Test", "1"));
    request.addPositionObjectId(ObjectId.of("Test", "2"));
    request.addPositionObjectId(ObjectId.of("Test", "3"));
    request.addPositionObjectId(ObjectId.of("Test", "4"));
    request.addPositionObjectId(ObjectId.of("Test", "5"));
    final PositionSearchRequest request1 = new PositionSearchRequest();
    request1.addPositionObjectId(ObjectId.of("Test", "1"));
    final PositionSearchResult result1 = new PositionSearchResult();
    result1.setPaging(Paging.of(PagingRequest.ALL, 1));
    result1.setVersionCorrection(now);
    final PositionDocument doc1 = Mockito.mock(PositionDocument.class);
    result1.getDocuments().add(doc1);
    Mockito.when(mock.search(request1)).thenReturn(result1);
    final PositionSearchRequest request2 = new PositionSearchRequest();
    request2.addPositionObjectId(ObjectId.of("Test", "2"));
    request2.addPositionObjectId(ObjectId.of("Test", "3"));
    final PositionSearchResult result2 = new PositionSearchResult();
    result2.setPaging(Paging.of(PagingRequest.ALL, 2));
    result2.setVersionCorrection(now);
    final PositionDocument doc2 = Mockito.mock(PositionDocument.class);
    result2.getDocuments().add(doc2);
    final PositionDocument doc3 = Mockito.mock(PositionDocument.class);
    result2.getDocuments().add(doc3);
    Mockito.when(mock.search(request2)).thenReturn(result2);
    final PositionSearchRequest request3 = new PositionSearchRequest();
    request3.addPositionObjectId(ObjectId.of("Test", "4"));
    request3.addPositionObjectId(ObjectId.of("Test", "5"));
    final PositionSearchResult result3 = new PositionSearchResult();
    result3.setPaging(Paging.of(PagingRequest.ALL, 2));
    result3.setVersionCorrection(now);
    final PositionDocument doc4 = Mockito.mock(PositionDocument.class);
    result3.getDocuments().add(doc4);
    final PositionDocument doc5 = Mockito.mock(PositionDocument.class);
    result3.getDocuments().add(doc5);
    Mockito.when(mock.search(request3)).thenReturn(result3);
    instance.setMaxSearchRequest(2);
    final PositionSearchResult result = new PositionSearchResult();
    result.setPaging(Paging.of(PagingRequest.ALL, 5));
    result.setVersionCorrection(now);
    result.getDocuments().add(doc1);
    result.getDocuments().add(doc2);
    result.getDocuments().add(doc3);
    result.getDocuments().add(doc4);
    result.getDocuments().add(doc5);
    assertEquals(instance.search(request), result);
  }

  public void testHistory() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final PositionHistoryRequest request = new PositionHistoryRequest();
    final PositionHistoryResult result = new PositionHistoryResult();
    Mockito.when(mock.history(request)).thenReturn(result);
    assertSame(instance.history(request), result);
  }

  public void testGetTrade() {
    final PositionMaster mock = Mockito.mock(PositionMaster.class);
    final QuerySplittingPositionMaster instance = new QuerySplittingPositionMaster(mock);
    final ManageableTrade trade = Mockito.mock(ManageableTrade.class);
    Mockito.when(mock.getTrade(UniqueId.of("Foo", "Bar"))).thenReturn(trade);
    assertSame(instance.getTrade(UniqueId.of("Foo", "Bar")), trade);
  }

}
