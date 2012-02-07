/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.id.UniqueId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

/**
 * Tests BatchRunResource.
 */
public class MarketDataResourceTest {

  private UniqueId _baseMarketDataUid = UniqueId.of("Test", "BaseMarketData");
  private MarketData _marketData;
  private BatchMaster _batchMaster;
  private MarketDataResource _resource;

  @BeforeMethod
  public void setUp() {
    _marketData = new MarketData(_baseMarketDataUid);

    _batchMaster = mock(BatchMaster.class);
    _resource = new MarketDataResource(_marketData.getObjectId(), _batchMaster);
    when(_batchMaster.getMarketDataById(_marketData.getObjectId())).thenReturn(_marketData);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGet() {
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(_marketData, test.getEntity());
  }
  
  @Test
  public void testDelete() {
    doNothing().when(_batchMaster).deleteMarketData(_marketData.getObjectId());
    _resource.delete();
    verify(_batchMaster).deleteMarketData(_marketData.getObjectId());
  }

  @Test
  public void testAddValuesToMarketData() {
    Set<MarketDataValue> marketDataValues = newHashSet(new MarketDataValue());
    _resource.addDataValues(marketDataValues);
    verify(_batchMaster).addValuesToMarketData(_marketData.getObjectId(), marketDataValues);
  }

  @Test
  public void testgetDataValues() {
    MarketDataValue mdv = new MarketDataValue();
    List<MarketDataValue> marketDataValues = newArrayList(mdv);
    PagingRequest pagingRequest = PagingRequest.ofPage(2, 30);
    when(_batchMaster.getMarketDataValues(_marketData.getObjectId(), pagingRequest)).thenReturn(Pair.of(marketDataValues, Paging.ofAll(marketDataValues)));
    
    Response response = _resource.getDataValues(pagingRequest);

    verify(_batchMaster).getMarketDataValues(_marketData.getObjectId(), pagingRequest);
  }
}
