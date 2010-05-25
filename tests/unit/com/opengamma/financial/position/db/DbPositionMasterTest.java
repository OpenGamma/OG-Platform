/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.DBTest;

/**
 * Tests the basic behavior of the public HibernatePositionMaster methods.
 */
public class DbPositionMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMasterTest.class);

  private DbPositionMaster _posMaster;

  public DbPositionMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    String contextLocation =  "config/test-position-master-context.xml";
    ApplicationContext context = new FileSystemXmlApplicationContext(contextLocation);
    _posMaster = (DbPositionMaster) context.getBean(getDatabaseType()+"DbPositionMaster");
  }

  @After
  public void tearDown() throws Exception {
    _posMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_posMaster);
    assertEquals(true, _posMaster.getIdentifierScheme().matches("Db[.][A-Za-z]+[.]PosMaster"));
    assertNotNull(_posMaster.getTemplate());
    assertNotNull(_posMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPortfolio_noMatch() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier portfolioUid = _posMaster.putPortfolio(base);
    UniqueIdentifier uid = UniqueIdentifier.of(portfolioUid.getScheme(), "123456789", "1");
    
    final Portfolio test = _posMaster.getPortfolio(uid);
    assertEquals(null, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPortfolioNode_noMatch() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier portfolioUid = _posMaster.putPortfolio(base);
    UniqueIdentifier uid = UniqueIdentifier.of(portfolioUid.getScheme(), "123456789-123456789", "1");
    
    final PortfolioNode test = _posMaster.getPortfolioNode(uid);
    assertEquals(null, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPortfolio() {
    final PortfolioImpl base = buildPortfolio();
    final List<Position> expectedPositions = buildExpectedPositions(base);
    UniqueIdentifier uid = _posMaster.putPortfolio(base);
    
    final Portfolio test = _posMaster.getPortfolio(uid);
    assertNotNull(test);
    assertEquals("Test Equity Option Portfolio", test.getName());
    assertEquals("Test Equity Option Portfolio Node", test.getRootNode().getName());
    assertEquals(2, test.getRootNode().getChildNodes().size());
    assertEquals(5, test.getRootNode().getChildNodes().get(0).getPositions().size());
    assertEquals(5, test.getRootNode().getChildNodes().get(1).getPositions().size());
    assertEquals(true, test.getRootNode().getChildNodes().get(0).getName().equals("Options on AAPL US Equity") ||
        test.getRootNode().getChildNodes().get(0).getName().equals("Options on T US Equity"));
    assertEquals(true, test.getRootNode().getChildNodes().get(1).getName().equals("Options on AAPL US Equity") ||
        test.getRootNode().getChildNodes().get(1).getName().equals("Options on T US Equity"));
    for (PortfolioNode loopNode : test.getRootNode().getChildNodes()) {
      for (Position loopPos : loopNode.getPositions()) {
        assertEquals(true, expectedPositions.remove(loopPos));
      }
    }
    
    final Portfolio older = _posMaster.getPortfolio(uid, Instant.now(_posMaster.getTimeSource()).minusSeconds(600));
    assertEquals(null, older);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPortfolioNode_tree() {
    final PortfolioImpl base = buildPortfolio();
    final List<Position> expectedPositions = buildExpectedPositions(base);
    _posMaster.putPortfolio(base);
    UniqueIdentifier uid = base.getRootNode().getUniqueIdentifier();
    
    final PortfolioNode test = _posMaster.getPortfolioNode(uid);
    assertNotNull(test);
    assertEquals("Test Equity Option Portfolio Node", test.getName());
    assertEquals(2, test.getChildNodes().size());
    assertEquals(5, test.getChildNodes().get(0).getPositions().size());
    assertEquals(5, test.getChildNodes().get(1).getPositions().size());
    assertEquals(true, test.getChildNodes().get(0).getName().equals("Options on AAPL US Equity") ||
        test.getChildNodes().get(0).getName().equals("Options on T US Equity"));
    assertEquals(true, test.getChildNodes().get(1).getName().equals("Options on AAPL US Equity") ||
        test.getChildNodes().get(1).getName().equals("Options on T US Equity"));
    for (PortfolioNode loopNode : test.getChildNodes()) {
      for (Position loopPos : loopNode.getPositions()) {
        assertEquals(true, expectedPositions.remove(loopPos));
      }
    }
    
    final PortfolioNode older = _posMaster.getPortfolioNode(uid, Instant.now(_posMaster.getTimeSource()).minusSeconds(600));
    assertEquals(null, older);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPortfolioNode_partTree() {
    final PortfolioImpl base = buildPortfolio();
    final List<Position> expectedPositions = buildExpectedPositions(base);
    _posMaster.putPortfolio(base);
    UniqueIdentifier uid = base.getRootNode().getChildNodes().get(0).getUniqueIdentifier();
    
    final PortfolioNode test = _posMaster.getPortfolioNode(uid);
    assertNotNull(test);
    assertEquals(0, test.getChildNodes().size());
    assertEquals(5, test.getPositions().size());
    assertEquals(true, test.getName().equals(base.getRootNode().getChildNodes().get(0).getName()));
    for (PortfolioNode loopNode : test.getChildNodes()) {
      for (Position loopPos : loopNode.getPositions()) {
        assertEquals(true, expectedPositions.remove(loopPos));
      }
    }
    
    final Position older = _posMaster.getPosition(uid, Instant.now(_posMaster.getTimeSource()).minusSeconds(600));
    assertEquals(null, older);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPosition() {
    final PortfolioImpl base = buildPortfolio();
    final List<Position> expectedPositions = buildExpectedPositions(base);
    _posMaster.putPortfolio(base);
    UniqueIdentifier uid = expectedPositions.get(0).getUniqueIdentifier();
    
    final Position test = _posMaster.getPosition(uid);
    assertNotNull(test);
    assertEquals(expectedPositions.get(0), test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_getPortfolioIds() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier uid = _posMaster.putPortfolio(base);
    
    final Set<UniqueIdentifier> test1 = _posMaster.getPortfolioIds();
    assertNotNull(test1);
    assertEquals(1, test1.size());
    assertEquals(true, test1.contains(uid));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_updatePortfolioOnly() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier uid1 = _posMaster.putPortfolio(base);
    
    base.setName("New name");
    UniqueIdentifier uid2 = _posMaster.updatePortfolioOnly(base);
    
    final Portfolio test1 = _posMaster.getPortfolio(uid1);
    assertNotNull(test1);
    assertEquals("Test Equity Option Portfolio", test1.getName());
    
    final Portfolio test2 = _posMaster.getPortfolio(uid2);
    assertNotNull(test2);
    assertEquals("New name", test2.getName());
  }

  @Test(expected=DataNotFoundException.class)
  public void test_putPortfolio_updatePortfolioOnly_notFound() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier uid = _posMaster.putPortfolio(base);
    
    final PortfolioImpl updated = new PortfolioImpl("New name");
    updated.setUniqueIdentifier(UniqueIdentifier.of(uid.getScheme(), "123456", "1"));  // invalid id
    _posMaster.updatePortfolioOnly(updated);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_putPortfolio_removePortfolio() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier uid = _posMaster.putPortfolio(base);
    
    _posMaster.removePortfolio(uid);
    
    // not returned in list of ids
    final Set<UniqueIdentifier> test2 = _posMaster.getPortfolioIds();
    assertNotNull(test2);
    assertEquals(0, test2.size());
    
    // still able to retrieve original
    final Portfolio test3 = _posMaster.getPortfolio(uid);
    assertNotNull(test3);
    
    // retrieving latest doesn't find it
    final Portfolio test4 = _posMaster.getPortfolio(uid.toLatest());
    assertEquals(null, test4);
  }

  @Test(expected=DataNotFoundException.class)
  public void test_putPortfolio_removePortfolio_notFound() {
    final PortfolioImpl base = buildPortfolio();
    UniqueIdentifier uid = _posMaster.putPortfolio(base);
    
    _posMaster.removePortfolio(UniqueIdentifier.of(uid.getScheme(), "123456", "1"));  // invalid id
  }

  //-------------------------------------------------------------------------
  private PortfolioImpl buildPortfolio() {
    final PortfolioImpl base = new PortfolioImpl("Test Equity Option Portfolio");
    base.getRootNode().setName("Test Equity Option Portfolio Node");
    PortfolioNodeImpl node = new PortfolioNodeImpl();
    node.setName("Options on AAPL US Equity");
    base.getRootNode().addChildNode(node);
    node.addPosition(new PositionImpl(new BigDecimal(10), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C100 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(12), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C105 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(14), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C110 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(16), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C115 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(18), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C120 Equity")));
    node = new PortfolioNodeImpl();
    node.setName("Options on T US Equity");
    base.getRootNode().addChildNode(node);
    node.addPosition(new PositionImpl(new BigDecimal(10), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C21 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(12), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C22 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(14), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C23 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(16), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C24 Equity")));
    node.addPosition(new PositionImpl(new BigDecimal(18), Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C25 Equity")));
    return base;
  }

  private List<Position> buildExpectedPositions(final PortfolioImpl base) {
    final List<Position> expectedPositions = new ArrayList<Position>();
    expectedPositions.addAll(base.getRootNode().getChildNodes().get(0).getPositions());
    expectedPositions.addAll(base.getRootNode().getChildNodes().get(1).getPositions());
    return expectedPositions;
  }

}
