/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

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
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.tuple.Pair;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.time.Instant;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

public class BatchMasterResourceTest extends AbstractFudgeBuilderTestCase {

  @Mock
  private BatchMasterWriter batchMaster;

  private BatchMasterResource batchMasterResource;

  //private BatchDocument batchDocument = mock(BatchDocument.class);

  private RiskRun riskRun = new RiskRun(       
    new MarketData(UniqueId.of(BatchMaster.BATCH_IDENTIFIER_SCHEME, "market-data")),
    Instant.now(),
    Instant.now(),
    0, 
    newHashSet(new CalculationConfiguration("calc-config")),
    newHashSet(new RiskRunProperty()),
    false,
    VersionCorrection.LATEST, 
    UniqueId.of("Scheme", "view-def")
  );

  @BeforeMethod
  public void setUp() throws Exception {

    Pair<List<RiskRun>, Paging> batchSearchResult = Pair.<List<RiskRun>, Paging>of(newArrayList(riskRun), Paging.ofAll(Collections.emptyList()));

    initMocks(this);
    batchMasterResource = new BatchMasterResource(batchMaster);
    when(batchMaster.searchRiskRun((BatchRunSearchRequest) any())).thenReturn(batchSearchResult);
    when(batchMaster.getRiskRun((ObjectId) any())).thenReturn(riskRun);
  }

  @Test
  public void testSearch() throws Exception {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();

    Pair<List<RiskRun>, Paging> result = (Pair<List<RiskRun>, Paging>) batchMasterResource.searchBatchRuns(batchRunSearchRequest).getEntity();

    assertTrue(result.getFirst().size() > 0);
    RiskRun run = result.getFirst().get(0);
    assertEquals(run, riskRun);
  }

  @Test
  public void testBatchRun() throws Exception {

    String batchUid = "Scheme~MockUniqueId";

    BatchRunResource batchRunResource = batchMasterResource.batchRuns(batchUid);

    batchRunResource.deleteBatchRun();    

    Response response = batchRunResource.get();

    assertEquals(response.getEntity(), riskRun);

  }

  @Test(expectedExceptions = com.opengamma.DataNotFoundException.class)
  public void testBatchRunDataNotFound() throws Exception {
    BatchMasterWriter batchMaster = mock(BatchMasterWriter.class);
    when(batchMaster.getRiskRun((ObjectId) any())).thenThrow(DataNotFoundException.class);
    
    BatchMasterResource batchMasterResource = new BatchMasterResource(batchMaster);    

    String batchUid = "Scheme~MockUniqueId";

    BatchRunResource batchRunResource = batchMasterResource.batchRuns(batchUid);

    Response response = batchRunResource.get();
    
    fail("Execution shouldn't reach here");
  }
  
  @Test
  public void testSnapshots() throws Exception {

    ObjectId snapshotId = riskRun.getMarketData().getObjectId();
    
    when(batchMaster.getMarketDataById((ObjectId) any())).thenReturn(riskRun.getMarketData());

    MarketDataResource marketDataResource = batchMasterResource.getMarketData(snapshotId.toString());
    
    MarketData marketData = (MarketData) marketDataResource.get().getEntity();

    assertEquals(marketData.getObjectId(), snapshotId);   

  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testSearchSnapshots() throws Exception {

    ObjectId snapshotId = riskRun.getMarketData().getObjectId();
    
    PagingRequest pagingRequest = PagingRequest.FIRST_PAGE; 
    
    List<MarketData> marketDataList = newArrayList(riskRun.getMarketData());
    Paging paging = Paging.of(pagingRequest, marketDataList);
      
    when(batchMaster.getMarketData((PagingRequest) any())).thenReturn(Pair.of(marketDataList, paging));
    
    Pair<List<MarketData>, Paging> response = (Pair<List<MarketData>, Paging>) batchMasterResource.searchMarketData(pagingRequest).getEntity();    

    assertEquals(response.getFirst().size(), 1);   
    assertEquals(response.getSecond(), paging); 
  }
  
}
