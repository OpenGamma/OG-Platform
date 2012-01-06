/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.rest;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.batch.*;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import org.fudgemsg.FudgeMsgEnvelope;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.time.Instant;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

public class BatchMasterResourceTest extends AbstractFudgeBuilderTestCase {

  @Mock
  private BatchMaster batchMaster;

  private BatchSearchResult batchSearchResult;

  private BatchMasterResource batchMasterResource;

  //private BatchDocument batchDocument = mock(BatchDocument.class);

  //private BatchDocument batchDocument = new BatchDocument(null, null, null, null, null, null, null, null, null, 0);

  private BatchDocument batchDocument = new BatchDocument(
    UniqueId.of("Scheme", "uid"),
    UniqueId.of("Scheme", "view"),
    UniqueId.of("Scheme", "market"),
    Instant.now(),
    VersionCorrection.of(Instant.now(), Instant.now()),
    BatchStatus.COMPLETE,
    Instant.now(),
    Instant.now(),
    Instant.now(),
    0
  );

  @BeforeMethod
  public void setUp() throws Exception {

    batchSearchResult = new BatchSearchResult();

    initMocks(this);
    batchMasterResource = new BatchMasterResource(getFudgeContext(), batchMaster);
    when(batchMaster.search((BatchSearchRequest) any())).thenReturn(batchSearchResult);
    when(batchMaster.get((UniqueId) any())).thenReturn(batchDocument);
  }

  @Test
  public void testSearch() throws Exception {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();

    FudgeMsgEnvelope fudgeMsgEnvelope = getFudgeContext().toFudgeMsg(batchSearchRequest);
    FudgeMsgEnvelope batchSearchResultEnvelope = batchMasterResource.search(fudgeMsgEnvelope);

    assertEquals(getFudgeContext().fromFudgeMsg(BatchSearchResult.class, batchSearchResultEnvelope.getMessage()), batchSearchResult);
  }

  @Test
  public void testBatchRun() throws Exception {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();

    FudgeMsgEnvelope fudgeMsgEnvelope = getFudgeContext().toFudgeMsg(batchSearchRequest);

    String batchUid = "Scheme~MockUniqueId";

    BatchMasterResource.BatchRunResource batchRunResource = batchMasterResource.batchRun(batchUid);


    FudgeMsgEnvelope envelope = batchRunResource.delete();

    assertNull(envelope);

    FudgeMsgEnvelope batchDocumentEnvelope = batchRunResource.get();

    assertEquals(getFudgeContext().fromFudgeMsg(BatchDocument.class, batchDocumentEnvelope.getMessage()), batchDocument);

  }

  @Test(expectedExceptions =  javax.ws.rs.WebApplicationException.class)
  public void testBatchRunDataNotFound() throws Exception {
    BatchMaster batchMaster = mock(BatchMaster.class);
    when(batchMaster.get((UniqueId) any())).thenThrow(DataNotFoundException.class);
    
    BatchMasterResource batchMasterResource = new BatchMasterResource(getFudgeContext(), batchMaster);    

    String batchUid = "Scheme~MockUniqueId";

    BatchMasterResource.BatchRunResource batchRunResource = batchMasterResource.batchRun(batchUid);

    FudgeMsgEnvelope batchDocumentEnvelope = batchRunResource.get();
    
    fail("Execution shouldn't reach here");
  }
}
