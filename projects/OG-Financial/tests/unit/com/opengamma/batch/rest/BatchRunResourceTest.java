/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.CalculationConfiguration;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.domain.RiskRunProperty;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.time.Instant;
import javax.ws.rs.core.Response;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

/**
 * Tests BatchRunResource.
 */
public class BatchRunResourceTest {
  
  private RiskRun _riskRun;
  private BatchMaster _underlying;
  private BatchRunResource _resource;
  private static final ObjectId _riskRunId = ObjectId.of("Test", "RiskRun");

  @BeforeMethod
  public void setUp() {
    _riskRun = new RiskRun(
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
    
    _underlying = mock(BatchMaster.class);
    _resource = new BatchRunResource(_riskRunId, _underlying);
    when(_underlying.getRiskRun(_riskRunId)).thenReturn(_riskRun);
  }
    
  //-------------------------------------------------------------------------
  @Test
  public void testGet() {
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(_riskRun, test.getEntity());
  }
  
  @Test
  public void testDelete() {
    doNothing().when(_underlying).deleteRiskRun(_riskRunId.getObjectId());
    _resource.deleteBatchRun();  
    
    verify(_underlying).deleteRiskRun(_riskRunId.getObjectId());
  }




}
