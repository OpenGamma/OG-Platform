/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;

import javax.time.TimeSource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.HibernateTest;

/**
 * Tests the basic behavior of the public HibernatePositionMaster methods.
 */
public class HibernatePositionMasterTest extends HibernateTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernatePositionMasterTest.class);
  
  private HibernatePositionMaster _posMaster;

  public HibernatePositionMasterTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }
  
  /* package */ static Class<?>[] getHibernateMappingClassesImpl () {
    return new Class<?>[] {
        IdentifierAssociationBean.class,
        PortfolioBean.class,
        PortfolioNodeBean.class,
        PositionBean.class
    };
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return getHibernateMappingClassesImpl ();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _posMaster = new HibernatePositionMaster();
    _posMaster.setSessionFactory(getSessionFactory());
    _posMaster.setIdentifierScheme ("Test");
    s_logger.debug ("PosMaster initialization complete {}", _posMaster);
  }
  
  private void createTestEquityOptionPortfolio () {
    _posMaster.getHibernateTemplate ().execute (new HibernateCallback () {
      
      private int _positionIdentifier = 1;

      private PositionBean createTestPosition (final PositionMasterSession posSession, final PortfolioNodeBean node) {
        final PositionBean position = new PositionBean ();
        position.setIdentifier (Integer.toString (_positionIdentifier++));
        position.setQuantity (new BigDecimal (10));
        posSession.addPositionToPortfolioNode (position, node);
        return position;
      }
      
      private void addDomainSpecificIdentifierToPosition (final PositionMasterSession posSession, final String identifier, final PositionBean position) {
        IdentifierAssociationBean bean = new IdentifierAssociationBean ();
        bean.setScheme ("Test 1");
        bean.setIdentifier (identifier);
        bean.setPosition (position);
        posSession.saveIdentifierAssociationBean (bean);
        bean = new IdentifierAssociationBean ();
        bean.setScheme ("Test 2");
        bean.setIdentifier ("ID " + position.getIdentifier ());
        bean.setPosition (position);
        posSession.saveIdentifierAssociationBean (bean);
      }

      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession posSession = new PositionMasterSession (session);
        final PortfolioNodeBean root = new PortfolioNodeBean ();
        posSession.savePortfolioNodeBean (root);
        PortfolioNodeBean node = new PortfolioNodeBean ();
        node.setIdentifier ("node 1");
        node.setName ("Options on AAPL US Equity");
        node.setAncestor (root);
        posSession.savePortfolioNodeBean (node);
        addDomainSpecificIdentifierToPosition (posSession, "AJL US 04/17/10 C210 Equity", createTestPosition (posSession, node)); // 1
        addDomainSpecificIdentifierToPosition (posSession, "AJL US 04/17/10 C220 Equity", createTestPosition (posSession, node)); // 2
        addDomainSpecificIdentifierToPosition (posSession, "AJL US 04/17/10 C230 Equity", createTestPosition (posSession, node)); // 3
        addDomainSpecificIdentifierToPosition (posSession, "AJL US 04/17/10 C240 Equity", createTestPosition (posSession, node)); // 4
        addDomainSpecificIdentifierToPosition (posSession, "AJL US 04/17/10 C250 Equity", createTestPosition (posSession, node)); // 5
        node = new PortfolioNodeBean ();
        node.setIdentifier ("node 2");
        node.setName ("Options on T US Equity");
        node.setAncestor (root);
        posSession.savePortfolioNodeBean (node);
        addDomainSpecificIdentifierToPosition (posSession, "T US 04/17/10 C15 Equity", createTestPosition (posSession, node)); // 6
        addDomainSpecificIdentifierToPosition (posSession, "T US 04/17/10 C17.5 Equity", createTestPosition (posSession, node)); // 7
        addDomainSpecificIdentifierToPosition (posSession, "T US 04/17/10 C20 Equity", createTestPosition (posSession, node)); // 8
        addDomainSpecificIdentifierToPosition (posSession, "T US 04/17/10 C21 Equity", createTestPosition (posSession, node)); // 9
        addDomainSpecificIdentifierToPosition (posSession, "T US 04/17/10 C22 Equity", createTestPosition (posSession, node)); // 10
        final PortfolioBean portfolio = new PortfolioBean ();
        portfolio.setName ("Test Equity Option Portfolio");
        portfolio.setIdentifier ("test equity option");
        portfolio.setRoot (root);
        posSession.savePortfolioBean (portfolio);
        return null;
      }
      
    });
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPortfolioIds() {
    createTestEquityOptionPortfolio();
    Collection<UniqueIdentifier> ids = _posMaster.getPortfolioIds();
    assertNotNull(ids);
    assertEquals(1, ids.size());
    assertTrue(ids.contains(UniqueIdentifier.of("Test", "test equity option")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPortfolio() {
    createTestEquityOptionPortfolio();
    Portfolio portfolio = _posMaster.getPortfolio(UniqueIdentifier.of("Test", "doesn't exist"));
    assertNull(portfolio);
    portfolio = _posMaster.getPortfolio(UniqueIdentifier.of("Test", "test equity option"));
    assertNotNull(portfolio);
    assertEquals("Test Equity Option Portfolio", portfolio.getName());
    Collection<Position> positions = portfolio.getRootNode().getPositions();
    assertNotNull(positions);
    assertEquals(0, positions.size());
    Collection<PortfolioNode> nodes = portfolio.getRootNode().getChildNodes();
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (PortfolioNode node : nodes) {
      assertEquals(0, node.getChildNodes().size());
      assertEquals(5, node.getPositions().size());
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPortfolioNode() {
    createTestEquityOptionPortfolio();
    PortfolioNode portfolioNode = _posMaster.getPortfolioNode(UniqueIdentifier.of("Test", "doesn't exist"));
    assertNull(portfolioNode);
    portfolioNode = _posMaster.getPortfolioNode(UniqueIdentifier.of("Test", "node 1"));
    assertNotNull(portfolioNode);
    assertEquals("Options on AAPL US Equity", portfolioNode.getName());
    assertEquals(5, portfolioNode.getPositions().size());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_getPortfolioNode_invalidScheme() {
    createTestEquityOptionPortfolio();
    _posMaster.getPortfolioNode(UniqueIdentifier.of ("BAD SCHEME", "node 1"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPosition() {
    createTestEquityOptionPortfolio();
    Position position = _posMaster.getPosition(UniqueIdentifier.of("Test", "doesn't exist"));
    assertNull(position);
    position = _posMaster.getPosition(UniqueIdentifier.of("Test", "10"));
    assertNotNull(position);
    assertEquals("10", position.getUniqueIdentifier().getValue());
    assertEquals(new BigDecimal(10), position.getQuantity());
    final IdentifierBundle dsids = position.getSecurityKey();
    assertNotNull(dsids);
    assertEquals("T US 04/17/10 C22 Equity", dsids.getIdentifier(new IdentificationScheme("Test 1")));
    assertEquals("ID 10", dsids.getIdentifier(new IdentificationScheme("Test 2")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_getPosition_invalidScheme() {
    createTestEquityOptionPortfolio();
    _posMaster.getPosition(UniqueIdentifier.of ("BAD SCHEME", "10"));
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void testPutPortfolio () {
    final PortfolioImpl portfolio = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Test Equity Option Portfolio");
    int count = 1;
    PortfolioNodeImpl node = new PortfolioNodeImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), "Options on AAPL US Equity");
    portfolio.getRootNode ().addChildNode (node);
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C100 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C105 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C110 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C115 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "AAPL US 01/21/12 C120 Equity")));
    node = new PortfolioNodeImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), "Options on T US Equity");
    portfolio.getRootNode ().addChildNode (node);
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C21 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C22 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C23 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C24 Equity")));
    node.addPosition (new PositionImpl (UniqueIdentifier.of ("Test", Integer.toString (count++)), new BigDecimal (10), new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "T US 05/22/10 C25 Equity")));
    _posMaster.putPortfolio (TimeSource.system().instant(), portfolio);
    final Portfolio getPortfolio = _posMaster.getPortfolio (UniqueIdentifier.of ("Test", "1"));
    assertNotNull (getPortfolio);
    assertEquals ("Test Equity Option Portfolio", getPortfolio.getName ());
    assertEquals (2, getPortfolio.getRootNode ().getChildNodes ().size ());
    assertEquals (5, getPortfolio.getRootNode ().getChildNodes().get (0).getPositions ().size ());
    assertEquals (5, getPortfolio.getRootNode ().getChildNodes().get (1).getPositions ().size ());
    assertEquals ("AAPL US 01/21/12 C100 Equity", getPortfolio.getRootNode ().getChildNodes().get (0).getPositions ().get (0).getSecurityKey ().getIdentifier(IdentificationScheme.BLOOMBERG_TICKER));
  }

  // TODO test the PositionMaster with requests at different points in time
  
}