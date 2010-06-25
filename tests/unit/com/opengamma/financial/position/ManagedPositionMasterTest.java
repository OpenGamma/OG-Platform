/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.Paging;

/**
 * Test the Fudge messages.
 */
public class ManagedPositionMasterTest {

  private ManagedPortfolio managedPortfolio;
  private ManagedPortfolioNode managedRoot;
  private ManagedPortfolioNode managedNode1;
  private ManagedPosition managedPosition1;
  private PortfolioSummary portfolioSummary;
  private PortfolioNodeSummary portfolioNodeSummary1;
  private PortfolioNodeSummary portfolioNodeSummary2;
  private PositionSummary positionSummary1;
  private PositionSummary positionSummary2;
  private AddPortfolioRequest addPortfolioRequest;
  private AddPortfolioNodeRequest addPortfolioNodeRequest;
  private AddPositionRequest addPositionRequest;
  private UpdatePortfolioRequest updatePortfolioRequest;
  private UpdatePortfolioNodeRequest updatePortfolioNodeRequest;
  private UpdatePositionRequest updatePositionRequest;
  private SearchPortfoliosResult searchPortfoliosResult;
  private SearchPositionsResult searchPositionsResult;

  @Before
  public void setUp() {
    portfolioNodeSummary1 = new PortfolioNodeSummary();
    portfolioNodeSummary1.setName("MPN1");
    portfolioNodeSummary1.setUniqueIdentifier(UniqueIdentifier.of("MPN", "1"));
    portfolioNodeSummary1.setTotalPositions(2);
    
    portfolioNodeSummary2 = new PortfolioNodeSummary();
    portfolioNodeSummary2.setName("MPN2");
    portfolioNodeSummary2.setUniqueIdentifier(UniqueIdentifier.of("MPN", "2"));
    portfolioNodeSummary2.setTotalPositions(0);
    
    positionSummary1 = new PositionSummary();
    positionSummary1.setUniqueIdentifier(UniqueIdentifier.of("MP", "1"));
    positionSummary1.setQuantity(BigDecimal.TEN);
    
    positionSummary2 = new PositionSummary();
    positionSummary2.setUniqueIdentifier(UniqueIdentifier.of("MP", "2"));
    positionSummary2.setQuantity(BigDecimal.TEN);
    
    portfolioSummary = new PortfolioSummary();
    portfolioSummary.setName("MF");
    portfolioSummary.setUniqueIdentifier(UniqueIdentifier.of("MF", "1"));
    portfolioSummary.setStartInstant(Instant.ofEpochSeconds(123));
    portfolioSummary.setEndInstant(Instant.ofEpochSeconds(1234));
    portfolioSummary.setTotalPositions(6);
    portfolioSummary.setActive(false);
    
    managedNode1 = new ManagedPortfolioNode();
    managedNode1.setName("MPN1");
    managedNode1.setUniqueIdentifier(UniqueIdentifier.of("MPN", "1"));
    managedNode1.setPortfolioUid(UniqueIdentifier.of("MF", "1"));
    managedNode1.setParentNodeUid(UniqueIdentifier.of("MPN", "R"));
    managedNode1.getChildNodes().add(portfolioNodeSummary1);
    managedNode1.getChildNodes().add(portfolioNodeSummary2);
    managedNode1.getPositions().add(positionSummary1);
    managedNode1.getPositions().add(positionSummary2);
    
    managedPosition1 = new ManagedPosition();
    managedPosition1.setUniqueIdentifier(UniqueIdentifier.of("MPN", "1"));
    managedPosition1.setPortfolioUid(UniqueIdentifier.of("MF", "1"));
    managedPosition1.setParentNodeUid(UniqueIdentifier.of("MPN", "R"));
    managedPosition1.setQuantity(BigDecimal.TEN);
    managedPosition1.setSecurityKey(new IdentifierBundle(Identifier.of("A", "B")));
    
    managedRoot = new ManagedPortfolioNode();
    managedRoot.setName("MPNR");
    managedRoot.setUniqueIdentifier(UniqueIdentifier.of("MPN", "R"));
    managedRoot.setPortfolioUid(UniqueIdentifier.of("MF", "1"));
    managedRoot.setParentNodeUid(null);
    managedRoot.getChildNodes().add(portfolioNodeSummary1);
    managedRoot.getChildNodes().add(portfolioNodeSummary2);
    managedRoot.getPositions().add(positionSummary1);
    managedRoot.getPositions().add(positionSummary2);
    
    managedPortfolio = new ManagedPortfolio();
    managedPortfolio.setName("MF");
    managedPortfolio.setUniqueIdentifier(UniqueIdentifier.of("MF", "1"));
    managedPortfolio.setRootNode(managedRoot);
    
    addPortfolioRequest = new AddPortfolioRequest();
    addPortfolioRequest.setName("ADDED");
    addPortfolioRequest.setRootNode(new PortfolioNodeImpl("foo"));
    
    addPortfolioNodeRequest = new AddPortfolioNodeRequest();
    addPortfolioNodeRequest.setParentNode(UniqueIdentifier.of("PARENT", "1"));
    addPortfolioNodeRequest.setName("ADDED");
    
    addPositionRequest = new AddPositionRequest();
    addPositionRequest.setParentNode(UniqueIdentifier.of("PARENT", "1"));
    addPositionRequest.setQuantity(BigDecimal.TEN);
    addPositionRequest.setSecurityKey(new IdentifierBundle(Identifier.of("A", "B")));
    
    updatePortfolioRequest = new UpdatePortfolioRequest();
    updatePortfolioRequest.setUniqueIdentifier(UniqueIdentifier.of("A", "1"));
    updatePortfolioRequest.setName("ADDED");
    
    updatePortfolioNodeRequest = new UpdatePortfolioNodeRequest();
    updatePortfolioNodeRequest.setUniqueIdentifier(UniqueIdentifier.of("A", "1"));
    updatePortfolioNodeRequest.setName("ADDED");
    
    updatePositionRequest = new UpdatePositionRequest();
    updatePositionRequest.setUniqueIdentifier(UniqueIdentifier.of("A", "1"));
    updatePositionRequest.setQuantity(BigDecimal.TEN);
    updatePositionRequest.setSecurityKey(new IdentifierBundle(Identifier.of("A", "B")));
    
    Paging paging = new Paging(1, 20, 25);
    List<PortfolioSummary> portfolios = new ArrayList<PortfolioSummary>();
    portfolios.add(portfolioSummary);
    searchPortfoliosResult = new SearchPortfoliosResult(paging, portfolios);
    
    List<PositionSummary> positions = new ArrayList<PositionSummary>();
    positions.add(positionSummary1);
    positions.add(positionSummary2);
    searchPositionsResult = new SearchPositionsResult(paging, positions);
  }

  @After
  public void tearDown() throws Exception {
    managedPortfolio = null;
    managedRoot = null;
    managedNode1 = null;
    managedPosition1 = null;
    portfolioSummary = null;
    portfolioNodeSummary1 = null;
    portfolioNodeSummary2 = null;
    positionSummary1 = null;
    positionSummary2 = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_ManagedPortfolio() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(managedPortfolio);
    ManagedPortfolio obj = fudge.fromFudgeMsg(ManagedPortfolio.class, env.getMessage());
    assertEquals(managedPortfolio.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_ManagedPortfolioNode() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(managedNode1);
    ManagedPortfolioNode obj = fudge.fromFudgeMsg(ManagedPortfolioNode.class, env.getMessage());
    assertEquals(managedNode1.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_ManagedPosition() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(managedPosition1);
    ManagedPosition obj = fudge.fromFudgeMsg(ManagedPosition.class, env.getMessage());
    assertEquals(managedPosition1.toString(), obj.toString());  // no equals method
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_PortfolioSummary() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(portfolioSummary);
    PortfolioSummary obj = fudge.fromFudgeMsg(PortfolioSummary.class, env.getMessage());
    assertEquals(portfolioSummary.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_PortfolioNodeSummary() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(portfolioNodeSummary1);
    PortfolioNodeSummary obj = fudge.fromFudgeMsg(PortfolioNodeSummary.class, env.getMessage());
    assertEquals(portfolioNodeSummary1.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_PositionSummary() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(positionSummary1);
    PositionSummary obj = fudge.fromFudgeMsg(PositionSummary.class, env.getMessage());
    assertEquals(positionSummary1.toString(), obj.toString());  // no equals method
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_AddPortfolioRequest() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(addPortfolioRequest);
    AddPortfolioRequest obj = fudge.fromFudgeMsg(AddPortfolioRequest.class, env.getMessage());
    assertEquals(addPortfolioRequest.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_AddPortfolioNodeRequest() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(addPortfolioNodeRequest);
    AddPortfolioNodeRequest obj = fudge.fromFudgeMsg(AddPortfolioNodeRequest.class, env.getMessage());
    assertEquals(addPortfolioNodeRequest.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_AddPositionRequest() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(addPositionRequest);
    AddPositionRequest obj = fudge.fromFudgeMsg(AddPositionRequest.class, env.getMessage());
    assertEquals(addPositionRequest.toString(), obj.toString());  // no equals method
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_UpdatePortfolioRequest() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(updatePortfolioRequest);
    UpdatePortfolioRequest obj = fudge.fromFudgeMsg(UpdatePortfolioRequest.class, env.getMessage());
    assertEquals(updatePortfolioRequest.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_UpdatePortfolioNodeRequest() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(updatePortfolioNodeRequest);
    UpdatePortfolioNodeRequest obj = fudge.fromFudgeMsg(UpdatePortfolioNodeRequest.class, env.getMessage());
    assertEquals(updatePortfolioNodeRequest.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_UpdatePositionRequest() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(updatePositionRequest);
    UpdatePositionRequest obj = fudge.fromFudgeMsg(UpdatePositionRequest.class, env.getMessage());
    assertEquals(updatePositionRequest.toString(), obj.toString());  // no equals method
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_SearchPortfoliosResult() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(searchPortfoliosResult);
    SearchPortfoliosResult obj = fudge.fromFudgeMsg(SearchPortfoliosResult.class, env.getMessage());
    assertEquals(searchPortfoliosResult.toString(), obj.toString());  // no equals method
  }

  @Test
  public void test_SearchPositionsResult() throws Exception {
    FudgeContext fudge = FudgeContext.GLOBAL_DEFAULT;
    FudgeMsgEnvelope env = fudge.toFudgeMsg(searchPositionsResult);
    SearchPositionsResult obj = fudge.fromFudgeMsg(SearchPositionsResult.class, env.getMessage());
    assertEquals(searchPositionsResult.toString(), obj.toString());  // no equals method
  }

}
