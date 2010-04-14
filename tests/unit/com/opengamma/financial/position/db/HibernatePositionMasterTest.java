/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.test.HibernateTest;

/**
 * Tests the basic behavior of the HibernatePositionMaster methods.
 * 
 * @author Andrew Griffin
 */
public class HibernatePositionMasterTest extends HibernateTest {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernatePositionMasterTest.class);
  
  private HibernatePositionMaster _posMaster;
  
  public HibernatePositionMasterTest(String databaseType) {
    super(databaseType);
  }
  
  /* package */ static Class<?>[] getHibernateMappingClassesImpl () {
    return new Class<?>[] {
        DomainSpecificIdentifierAssociationBean.class,
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
    s_logger.debug ("PosMaster initialization complete {}", _posMaster);
  }
  
  private void storeTestPositions () {
    fail ("TODO: store a standard set of positions");
  }
  
  private void storeTestPositionsWithTime () {
    fail ("TODO: store a standard set of positions with the same DSIDs at different times");
  }
  
  private void storeTestPortfolioNodes () {
    storeTestPositionsWithTime ();
    fail ("TODO: store a standard set of portfolio nodes linked to the standard positions");
  }
  
  private void storeTestRootPortfolios () {
    storeTestPositionsWithTime ();
    fail ("TODO: store a standard set of root portfolios linked to the standard nodes");
  }
  
  @Test
  public void testPortfolioNodes () {
    storeTestPortfolioNodes ();
    fail ("TODO: retrieve the portfolio nodes");
  }
  
  @Test
  public void testPositions () {
    storeTestPositions ();
    fail ("TODO: retrieve the position nodes");
  }
  
  @Test
  public void testPositionsWithTime () {
    storeTestPositionsWithTime ();
    fail ("TODO: retrieve the position nodes");
  }
  
  @Test
  public void testRootPortfolios () {
    storeTestRootPortfolios ();
    fail ("TODO: retrieve the root portfolios");
  }
  
}