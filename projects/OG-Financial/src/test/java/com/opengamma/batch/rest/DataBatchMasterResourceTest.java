/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.BatchMasterWriter;
import com.opengamma.batch.domain.CalculationConfiguration;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.domain.RiskRunProperty;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.FudgeResponse;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataBatchMasterResourceTest extends AbstractFudgeBuilderTestCase {

  @Mock
  private BatchMasterWriter batchMaster;

  private DataBatchMasterResource batchMasterResource;

  private RiskRun riskRun = new RiskRun(       
    new MarketData(UniqueId.of(BatchMaster.BATCH_IDENTIFIER_SCHEME, "market-data")),
    Instant.now(),
    Instant.now(),
    0, 
    newHashSet(new CalculationConfiguration("calc-config")),
    newHashSet(new RiskRunProperty()),
    false,
    VersionCorrection.LATEST, 
    UniqueId.of("Scheme", "view-def"),
    "cyclename"
  );

  @BeforeMethod
  public void setUp() throws Exception {
    List<RiskRun> list = newArrayList(riskRun);
    Pair<List<RiskRun>, Paging> batchSearchResult = Pairs.of(list, Paging.ofAll(Collections.emptyList()));
    
    initMocks(this);
    batchMasterResource = new DataBatchMasterResource(batchMaster);
    when(batchMaster.searchRiskRun((BatchRunSearchRequest) any())).thenReturn(batchSearchResult);
    when(batchMaster.getRiskRun((ObjectId) any())).thenReturn(riskRun);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSearch() throws Exception {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    
    Object entity = batchMasterResource.searchBatchRuns(batchRunSearchRequest).getEntity();
    entity = FudgeResponse.unwrap(entity);
    Pair<List<RiskRun>, Paging> result = (Pair<List<RiskRun>, Paging>) entity;
    
    assertTrue(result.getFirst().size() > 0);
    RiskRun run = result.getFirst().get(0);
    assertEquals(run, riskRun);
  }

  @Test
  public void testBatchRun() throws Exception {
    String batchUid = "Scheme~MockUniqueId";
    DataBatchRunResource batchRunResource = batchMasterResource.batchRuns(batchUid);
    
    batchRunResource.deleteBatchRun();    
    
    Response response = batchRunResource.get();
    assertEquals(response.getEntity(), riskRun);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = com.opengamma.DataNotFoundException.class)
  public void testBatchRunDataNotFound() throws Exception {
    BatchMasterWriter batchMaster = mock(BatchMasterWriter.class);
    when(batchMaster.getRiskRun((ObjectId) any())).thenThrow(DataNotFoundException.class);
    
    DataBatchMasterResource batchMasterResource = new DataBatchMasterResource(batchMaster);    
    
    String batchUid = "Scheme~MockUniqueId";
    DataBatchRunResource batchRunResource = batchMasterResource.batchRuns(batchUid);
    
    batchRunResource.get();
  }

  @Test
  public void testSnapshots() throws Exception {
    ObjectId snapshotId = riskRun.getMarketData().getObjectId();
    
    when(batchMaster.getMarketDataById((ObjectId) any())).thenReturn(riskRun.getMarketData());
    
    DataMarketDataResource marketDataResource = batchMasterResource.getMarketData(snapshotId.toString());
    
    MarketData marketData = (MarketData) marketDataResource.get().getEntity();
    assertEquals(marketData.getObjectId(), snapshotId);   
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testSearchSnapshots() throws Exception {
    PagingRequest pagingRequest = PagingRequest.FIRST_PAGE;
    
    List<MarketData> marketDataList = newArrayList(riskRun.getMarketData());
    Paging paging = Paging.of(pagingRequest, marketDataList);
      
    when(batchMaster.getMarketData((PagingRequest) any())).thenReturn(Pairs.of(marketDataList, paging));
    
    Object entity = batchMasterResource.searchMarketData(pagingRequest).getEntity();
    entity = FudgeResponse.unwrap(entity);
    Pair<List<MarketData>, Paging> response = (Pair<List<MarketData>, Paging>) entity;

    assertEquals(response.getFirst().size(), 1);
    assertEquals(response.getSecond(), paging);
  }

}
