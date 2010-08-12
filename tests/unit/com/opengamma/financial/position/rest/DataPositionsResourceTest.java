/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.financial.position.rest.DataPositionResource;
import com.opengamma.financial.position.rest.DataPositionsResource;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests DataPositionsResource.
 */
public class DataPositionsResourceTest {

  private PositionMaster _underlying;
  private DataPositionsResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataPositionsResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testSearchPositions() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 20));
    request.setMinQuantity(BigDecimal.TEN);
    
    final PositionSearchResult result = new PositionSearchResult();
    result.setPaging(new Paging(1, 20, 0));
    when(_underlying.searchPositions(same(request))).thenReturn(result);
    
    PositionSearchResult test = _resource.get(request);
    assertSame(result, test);
  }

  @Test
  public void testAddPosition() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    final PositionDocument request = new PositionDocument(position);
    
    final PositionDocument result = new PositionDocument(position);
    result.setPositionId(UniqueIdentifier.of("Test", "PosA"));
    when(_underlying.addPosition(same(request))).thenReturn(result);
    
    PositionDocument test = _resource.post(request);
    assertSame(result, test);
  }

  @Test
  public void testFindPosition() {
    DataPositionResource test = _resource.findPosition("Test::PosA");
    assertSame(_resource, test.getPositionsResource());
    assertEquals(UniqueIdentifier.of("Test", "PosA"), test.getUrlPositionId());
  }

}
