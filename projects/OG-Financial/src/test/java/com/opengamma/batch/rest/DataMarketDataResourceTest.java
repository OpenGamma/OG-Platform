/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.batch.BatchMasterWriter;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.id.UniqueId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests BatchRunResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataMarketDataResourceTest {

  private UniqueId _baseMarketDataUid = UniqueId.of("Test", "BaseMarketData");
  private MarketData _marketData;
  private BatchMasterWriter _batchMaster;
  private DataMarketDataResource _resource;

  @BeforeMethod
  public void setUp() {
    _marketData = new MarketData(_baseMarketDataUid);

    _batchMaster = mock(BatchMasterWriter.class);
    _resource = new DataMarketDataResource(_marketData.getObjectId(), _batchMaster);
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
    when(_batchMaster.getMarketDataValues(_marketData.getObjectId(), pagingRequest)).thenReturn(Pairs.of(marketDataValues, Paging.ofAll(marketDataValues)));
    
    _resource.getDataValues(pagingRequest);
    
    verify(_batchMaster).getMarketDataValues(_marketData.getObjectId(), pagingRequest);
  }

}
