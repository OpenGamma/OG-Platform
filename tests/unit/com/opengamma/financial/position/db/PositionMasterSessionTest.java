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
import java.util.Date;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.test.HibernateTest;

/**
 * Tests the basic behavior of the PositionMasterSession methods.
 * 
 * @author Andrew Griffin
 */
public class PositionMasterSessionTest extends HibernateTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionMasterSessionTest.class);
  
  private final Date _dateNow = new Date ();
  private final InstantProvider _instantNow = Instant.millis (_dateNow.getTime ());
  private final Date _dateBefore = new Date (_dateNow.getTime () - 7l * 24l * 3600l * 1000l);
  private final InstantProvider _instantBefore = Instant.millis (_dateBefore.getTime ());
  private HibernateTemplate _template;
  
  public PositionMasterSessionTest(String databaseType) {
    super(databaseType);
  }
  
  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return HibernatePositionMasterTest.getHibernateMappingClassesImpl ();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _template = new HibernateTemplate (getSessionFactory ());
  }
  
  protected HibernateTemplate getTemplate () {
    return _template;
  }
  
  private <T extends DateIdentifiableBean> T createBeanBefore (final T bean, final String identifier) {
    bean.setIdentifier (identifier);
    bean.setStartDate (null);
    bean.setEndDate (_dateNow);
    return bean;
  }
  
  private <T extends DateIdentifiableBean> T createBeanNow (final T bean, final String identifier) {
    bean.setIdentifier (identifier);
    bean.setStartDate (_dateNow);
    bean.setEndDate (null);
    return bean;
  }
  
  private <T extends DateIdentifiableBean> T createBeanAny (final T bean, final String identifier) {
    bean.setIdentifier (identifier);
    bean.setStartDate (_dateBefore);
    bean.setEndDate (null);
    return bean;
  }
  
  @Test
  public void testDomainSpecificIdentifierAssociationBean () {
    s_logger.info ("beginning testDomainSpecificIdentifierAssociationBean");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        // create beans
        final PositionBean position0 = createBeanAny (new PositionBean (), "position 0");
        position0.setQuantity (new BigDecimal (10));
        final PositionBean position1 = createBeanAny (new PositionBean (), "position 1");
        position1.setQuantity (new BigDecimal (20));
        final DomainSpecificIdentifier identifier0 = new DomainSpecificIdentifier ("domain 0", "identifier 0");
        final DomainSpecificIdentifier identifier1 = new DomainSpecificIdentifier ("domain 1", "identifier 1");
        final DomainSpecificIdentifierAssociationBean association0before = createBeanBefore (new DomainSpecificIdentifierAssociationBean (), null);
        association0before.setDomainSpecificIdentifier (identifier0);
        association0before.setPosition (position0);
        final DomainSpecificIdentifierAssociationBean association0now = createBeanNow (new DomainSpecificIdentifierAssociationBean (), null);
        association0now.setDomainSpecificIdentifier (identifier0);
        association0now.setPosition (position1);
        final DomainSpecificIdentifierAssociationBean association1a = createBeanAny (new DomainSpecificIdentifierAssociationBean (), null);
        association1a.setDomainSpecificIdentifier (identifier1);
        association1a.setPosition (position0);
        final DomainSpecificIdentifierAssociationBean association1b = createBeanAny (new DomainSpecificIdentifierAssociationBean (), null);
        association1b.setDomainSpecificIdentifier (identifier1);
        association1b.setPosition (position1);
        // save
        positionMasterSession.saveDomainSpecificIdentifierAssociationBean (association0before);
        positionMasterSession.saveDomainSpecificIdentifierAssociationBean (association0now);
        positionMasterSession.saveDomainSpecificIdentifierAssociationBean (association1a);
        positionMasterSession.saveDomainSpecificIdentifierAssociationBean (association1b);
        // get by domain identifier
        Collection<DomainSpecificIdentifierAssociationBean> beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (_instantBefore, identifier0);
        assertNotNull (beans);
        assertEquals (1, beans.size ());
        assertTrue (beans.contains (association0before));
        for (DomainSpecificIdentifierAssociationBean bean : beans) {
          assertEquals (identifier0, bean.getDomainSpecificIdentifier()); 
        }
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (_instantNow, identifier0);
        assertNotNull (beans);
        assertEquals (1, beans.size ());
        assertTrue (beans.contains (association0now));
        for (DomainSpecificIdentifierAssociationBean bean : beans) {
          assertEquals (identifier0, bean.getDomainSpecificIdentifier()); 
        }
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (_instantNow, identifier1);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (association1a));
        assertTrue (beans.contains (association1b));
        for (DomainSpecificIdentifierAssociationBean bean : beans) {
          assertEquals (identifier1, bean.getDomainSpecificIdentifier()); 
        }
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (_instantNow, "domain 0", "identifier 2");
        assertNotNull (beans);
        assertEquals (0, beans.size ());
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByDomainIdentifier (_instantNow, "domain 2", "identifier 0");
        assertNotNull (beans);
        assertEquals (0, beans.size ());
        // get by position
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByPosition (_instantBefore, position0);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (association0before));
        assertTrue (beans.contains (association1a));
        for (DomainSpecificIdentifierAssociationBean bean : beans) {
          assertEquals (position0, bean.getPosition ()); 
        }
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByPosition (_instantBefore, position1);
        assertNotNull (beans);
        assertEquals (1, beans.size ());
        assertTrue (beans.contains (association1b));
        for (DomainSpecificIdentifierAssociationBean bean : beans) {
          assertEquals (position1, bean.getPosition ()); 
        }
        beans = positionMasterSession.getDomainSpecificIdentifierAssociationBeanByPosition (_instantNow, position1);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (association0now));
        assertTrue (beans.contains (association1b));
        for (DomainSpecificIdentifierAssociationBean bean : beans) {
          assertEquals (position1, bean.getPosition ()); 
        }
        return null;
      }
    });
  }
  
  @Test(expected=NullPointerException.class)
  public void testPortfolioBeanNoName () {
    s_logger.info ("beginning testPortfolioBeanNoName");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PortfolioNodeBean portfolioNode = createBeanAny (new PortfolioNodeBean (), "0");
        final PortfolioBean portfolio = new PortfolioBean ();
        portfolio.setIdentifier ("identifier");
        portfolio.setRoot (portfolioNode);
        // name is null
        positionMasterSession.savePortfolioBean (portfolio);
        return null;
      }
    });
  }
  
  @Test(expected=NullPointerException.class)
  public void testPortfolioBeanNoRoot () {
    s_logger.info ("beginning testPortfolioBeanNoRoot");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PortfolioBean portfolio = new PortfolioBean ();
        portfolio.setIdentifier ("identifier");
        portfolio.setName ("name");
        // root is null
        positionMasterSession.savePortfolioBean (portfolio);
        return null;
      }
    });
  }
  
  @Test
  public void testPortfolioBeanNoIdentifier () {
    s_logger.info ("beginning testPortfolioBeanNoRoot");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PortfolioNodeBean portfolioNode = createBeanAny (new PortfolioNodeBean (), "0");
        final PortfolioBean portfolio = new PortfolioBean ();
        // identifier is null
        portfolio.setName ("name");
        portfolio.setRoot (portfolioNode);
        positionMasterSession.savePortfolioBean (portfolio);
        assertNotNull (portfolio.getIdentifier ());
        s_logger.debug ("allocated portfolio identifier = {}", portfolio.getIdentifier ()); 
        return null;
      }
    });
  }
  
  @Test
  public void testPortfolioBean () {
    s_logger.info ("beginning testPortfolioBean");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        // create test beans
        final PortfolioNodeBean portfolioNode0 = createBeanAny (new PortfolioNodeBean (), "0");
        final PortfolioNodeBean portfolioNode1 = createBeanAny (new PortfolioNodeBean (), "1");
        final PortfolioBean portfolio0before = createBeanBefore (new PortfolioBean (), "test id");
        portfolio0before.setName ("test name");
        portfolio0before.setRoot (portfolioNode0);
        final PortfolioBean portfolio0now = createBeanNow (new PortfolioBean (), "test id");
        portfolio0now.setName ("test name");
        portfolio0now.setRoot (portfolioNode1);
        final PortfolioBean portfolio1 = createBeanAny (new PortfolioBean (), "test id#2");
        portfolio1.setName ("another test");
        portfolio1.setRoot (portfolioNode0);
        // save beans
        positionMasterSession.savePortfolioBean (portfolio0before);
        positionMasterSession.savePortfolioBean (portfolio0now);
        positionMasterSession.savePortfolioBean (portfolio1);
        // get all
        Collection<PortfolioBean> beans = positionMasterSession.getAllPortfolioBeans (_instantBefore);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (portfolio0before));
        assertTrue (beans.contains (portfolio1));
        beans = positionMasterSession.getAllPortfolioBeans (_instantNow);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (portfolio0now));
        assertTrue (beans.contains (portfolio1));
        // get by name
        beans = positionMasterSession.getPortfolioBeanByName (_instantBefore, "test name");
        assertNotNull (beans);
        assertEquals (1, beans.size ());
        assertTrue (beans.contains (portfolio0before));
        beans = positionMasterSession.getPortfolioBeanByName (_instantNow, "test name");
        assertNotNull (beans);
        assertEquals (1, beans.size ());
        assertTrue (beans.contains (portfolio0now));
        beans = positionMasterSession.getPortfolioBeanByName (_instantNow, "another test");
        assertNotNull (beans);
        assertEquals (1, beans.size ());
        assertTrue (beans.contains (portfolio1));
        beans = positionMasterSession.getPortfolioBeanByName (_instantNow, "invalid");
        assertNotNull (beans);
        assertEquals (0, beans.size ());
        // get by identifier
        PortfolioBean bean = positionMasterSession.getPortfolioBeanByIdentifier (_instantBefore, "test id");
        assertNotNull (bean);
        assertEquals (portfolio0before, bean);
        bean = positionMasterSession.getPortfolioBeanByIdentifier (_instantNow, "test id");
        assertNotNull (bean);
        assertEquals (portfolio0now, bean);
        bean = positionMasterSession.getPortfolioBeanByIdentifier (_instantNow, "test id#2");
        assertNotNull (bean);
        assertEquals (portfolio1, bean);
        bean = positionMasterSession.getPortfolioBeanByIdentifier (_instantNow, "invalid id");
        assertNull (bean);
        return null;
      }
    });
  }
  
  @Test
  public void testPortfolioNodeBeanNoIdentifier () {
    s_logger.info ("beginning testPortfolioNodeBeanNoIdentifier");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PortfolioNodeBean portfolioNode = new PortfolioNodeBean ();
        portfolioNode.setName ("test");
        // identifier is null
        positionMasterSession.savePortfolioNodeBean (portfolioNode);
        assertNotNull (portfolioNode.getIdentifier ());
        s_logger.debug ("allocated portfolioNode identifier = {}", portfolioNode.getIdentifier ());
        return null;
      }
    });
  }

  @Test
  public void testPortfolioNodeBean () {
    s_logger.info ("beginning testPortfolioNodeBean");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        // create test beans
        final PortfolioNodeBean portfolioNode0 = createBeanAny (new PortfolioNodeBean (), "0");
        portfolioNode0.setName ("root");
        portfolioNode0.setAncestor (null);
        final PortfolioNodeBean portfolioNode0_1 = createBeanAny (new PortfolioNodeBean (), "0_1");
        portfolioNode0_1.setAncestor (portfolioNode0);
        final PortfolioNodeBean portfolioNode0_2before = createBeanBefore (new PortfolioNodeBean (), "0_2");
        portfolioNode0_2before.setAncestor (portfolioNode0);
        final PortfolioNodeBean portfolioNode0_2now = createBeanNow (new PortfolioNodeBean (), "0_2");
        portfolioNode0_2now.setAncestor (portfolioNode0);
        final PortfolioNodeBean portfolioNode0_2before_1 = createBeanAny (new PortfolioNodeBean (), "0_2_1");
        portfolioNode0_2before_1.setAncestor (portfolioNode0_2before);
        // save beans
        positionMasterSession.savePortfolioNodeBean (portfolioNode0);
        positionMasterSession.savePortfolioNodeBean (portfolioNode0_1);
        positionMasterSession.savePortfolioNodeBean (portfolioNode0_2before);
        positionMasterSession.savePortfolioNodeBean (portfolioNode0_2now);
        positionMasterSession.savePortfolioNodeBean (portfolioNode0_2before_1);
        // get by identifier
        PortfolioNodeBean bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (_instantNow, "0");
        assertNotNull (bean);
        assertEquals (portfolioNode0, bean);
        bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (_instantNow, "0_1");
        assertNotNull (bean);
        assertEquals (portfolioNode0_1, bean);
        bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (_instantBefore, "0_2");
        assertNotNull (bean);
        assertEquals (portfolioNode0_2before, bean);
        bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (_instantNow, "0_2");
        assertNotNull (bean);
        assertEquals (portfolioNode0_2now, bean);
        bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (_instantNow, "0_2_1");
        assertNull (bean);
        bean = positionMasterSession.getPortfolioNodeBeanByIdentifier (_instantBefore, "0_2_1");
        assertNotNull (bean);
        assertEquals (portfolioNode0_2before_1, bean);
        // get by immediate ancestor
        Collection<PortfolioNodeBean> beans = positionMasterSession.getPortfolioNodeBeanByImmediateAncestor (_instantBefore, portfolioNode0);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (portfolioNode0_1));
        assertTrue (beans.contains (portfolioNode0_2before));
        beans = positionMasterSession.getPortfolioNodeBeanByImmediateAncestor (_instantNow, portfolioNode0);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (portfolioNode0_1));
        assertTrue (beans.contains (portfolioNode0_2now));
        // get by ancestor
        beans = positionMasterSession.getPortfolioNodeBeanByAncestor (_instantBefore, portfolioNode0);
        assertNotNull (beans);
        assertEquals (3, beans.size ());
        assertTrue (beans.contains (portfolioNode0_1));
        assertTrue (beans.contains (portfolioNode0_2before));
        assertTrue (beans.contains (portfolioNode0_2before_1));
        beans = positionMasterSession.getPortfolioNodeBeanByAncestor (_instantNow, portfolioNode0);
        assertNotNull (beans);
        assertEquals (2, beans.size ());
        assertTrue (beans.contains (portfolioNode0_1));
        assertTrue (beans.contains (portfolioNode0_2now));
        return null;
      }
    });
  }
  
  @Test(expected=NullPointerException.class)
  public void testPositionBeanNoQuantity () {
    s_logger.info ("beginning testPositionBeanNoQuantity");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PositionBean position = new PositionBean ();
        position.setIdentifier ("identifier");
        position.setCounterparty ("counterparty");
        position.setTrader ("trader");
        // quantity is null
        positionMasterSession.savePositionBean (position);
        return null;
      }
    });
  }
  
  @Test
  public void testPositionBeanNoIdentifier () {
    s_logger.info ("beginning testPositionBeanNoIdentifier");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        final PositionBean position = new PositionBean ();
        position.setQuantity (new BigDecimal (10));
        position.setCounterparty ("counterparty");
        position.setTrader ("trader");
        // identifier is null
        positionMasterSession.savePositionBean (position);
        assertNotNull (position.getIdentifier ());
        s_logger.debug ("allocated position identifier = {}", position.getIdentifier ());
        return null;
      }
    });
  }
  
  @Test
  public void testPositionBean () {
    s_logger.info ("beginning testPositionBean");
    getTemplate ().execute (new HibernateCallback () {
      @Override
      public Object doInHibernate (final Session session) throws HibernateException, SQLException {
        final PositionMasterSession positionMasterSession = new PositionMasterSession (session);
        // create test beans
        final PositionBean position1before = createBeanBefore (new PositionBean (), "position 1");
        position1before.setQuantity (new BigDecimal (10));
        position1before.setCounterparty ("counterparty 1a");
        position1before.setTrader ("trader 1a");
        final PositionBean position1now = createBeanNow (new PositionBean (), "position 1");
        position1now.setQuantity (new BigDecimal (20));
        position1now.setCounterparty ("counterparty 1b");
        position1now.setTrader ("trader 1b");
        final PositionBean position2 = createBeanAny (new PositionBean (), "position 2");
        position2.setQuantity (new BigDecimal (30));
        position2.setCounterparty ("counterparty 2");
        position2.setTrader ("trader 2");
        // save beans
        positionMasterSession.savePositionBean (position1before);
        positionMasterSession.savePositionBean (position1now);
        positionMasterSession.savePositionBean (position2);
        // get by identifier
        PositionBean bean = positionMasterSession.getPositionBeanByIdentifier (_instantNow, "position 1");
        assertNotNull (bean);
        assertEquals (position1now, bean);
        bean = positionMasterSession.getPositionBeanByIdentifier (_instantBefore, "position 1");
        assertNotNull (bean);
        assertEquals (position1before, bean);
        // build a crude portfolio node tree
        final PortfolioNodeBean root = createBeanNow (new PortfolioNodeBean (), "root");
        final PortfolioNodeBean child1 = createBeanNow (new PortfolioNodeBean (), "left");
        child1.setAncestor (root);
        final PortfolioNodeBean child2 = createBeanNow (new PortfolioNodeBean (), "right");
        child2.setAncestor (root);
        positionMasterSession.savePortfolioNodeBean (root);
        positionMasterSession.savePortfolioNodeBean (child1);
        positionMasterSession.savePortfolioNodeBean (child2);
        positionMasterSession.addPositionToPortfolioNode (position1now, child1);
        positionMasterSession.addPositionToPortfolioNode (position2, child2);
        // get by immediate portfolio node
        Collection<PositionBean> beans = positionMasterSession.getPositionBeanByImmediatePortfolioNode (_instantNow, child1);
        assertNotNull (bean);
        assertEquals (1, beans.size ());
        beans = positionMasterSession.getPositionBeanByImmediatePortfolioNode (_instantNow, child2);
        assertNotNull (bean);
        assertEquals (1, beans.size ());
        beans = positionMasterSession.getPositionBeanByImmediatePortfolioNode (_instantNow, root);
        assertNotNull (bean);
        assertEquals (0, beans.size ());
        // get by portfolio node
        beans = positionMasterSession.getPositionBeanByPortfolioNode (_instantNow, root);
        assertNotNull (bean);
        assertEquals (2, beans.size ());
        return null;
      }
    });
  }

}