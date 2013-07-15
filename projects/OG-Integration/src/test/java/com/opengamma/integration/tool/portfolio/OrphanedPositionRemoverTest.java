/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.math.BigDecimal;

import org.joda.beans.JodaBeanUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link OrphanedPositionRemover}
 */
@Test(groups = TestGroup.UNIT)
public class OrphanedPositionRemoverTest {

  private static final ExternalId SEC1 = ExternalId.of ("Test", "sec1");
  private static final ExternalId SEC2 = ExternalId.of ("Test", "sec2");
  private static final ExternalId SEC3 = ExternalId.of ("Test", "sec3");
  private static final ExternalId SEC4 = ExternalId.of ("Test", "sec4");
  private static final ExternalId COUNTER_PARTY = ExternalId.of ("Test", "counterParty");
  
  private static final ManageableTrade TRADE1 = new ManageableTrade(BigDecimal.ONE, SEC1, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY);
  private static final ManageableTrade TRADE2 = new ManageableTrade(BigDecimal.ONE, SEC2, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY);
  private static final ManageableTrade TRADE3 = new ManageableTrade(BigDecimal.ONE, SEC3, LocalDate.now(), OffsetTime.now(), COUNTER_PARTY);
  
  private InMemoryPositionMaster _positionMaster;
  private InMemoryPortfolioMaster _portfolioMaster;
  private PositionDocument _pos1;
  private PositionDocument _pos2;
  private PositionDocument _pos3;
  private PositionDocument _pos4;
  
  @BeforeMethod
  public void setUp() {
    
    _positionMaster = new InMemoryPositionMaster();
    _pos1 = new PositionDocument(new ManageablePosition(BigDecimal.ONE, SEC1));
    _pos1.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos1 = _positionMaster.add(_pos1);
    
    _pos2 = new PositionDocument(new ManageablePosition(BigDecimal.valueOf(2), SEC2));
    _pos2.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos2.getPosition().addTrade(JodaBeanUtils.clone(TRADE2));
    _pos2 = _positionMaster.add(_pos2);
    
    _pos3 = new PositionDocument(new ManageablePosition(BigDecimal.valueOf(3), SEC3));
    _pos3.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos3.getPosition().addTrade(JodaBeanUtils.clone(TRADE2));
    _pos3.getPosition().addTrade(JodaBeanUtils.clone(TRADE3));
    _pos3 = _positionMaster.add(_pos3);
    
    _pos4 = new PositionDocument(new ManageablePosition(BigDecimal.valueOf(3), SEC4));
    _pos4.getPosition().addTrade(JodaBeanUtils.clone(TRADE1));
    _pos4.getPosition().addTrade(JodaBeanUtils.clone(TRADE2));
    _pos4.getPosition().addTrade(JodaBeanUtils.clone(TRADE3));
    _pos4 = _positionMaster.add(_pos4);
    
    _portfolioMaster = new InMemoryPortfolioMaster();
    
    _portfolioMaster.add(new PortfolioDocument(new ManageablePortfolio("Port1", generatePortfolio())));
    
  }

  private ManageablePortfolioNode generatePortfolio() {
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Port1");
    rootNode.addPosition(_pos1.getUniqueId());
    
    ManageablePortfolioNode pn2 = new ManageablePortfolioNode("pn2");
    pn2.addPosition(_pos2.getUniqueId());
    
    ManageablePortfolioNode pn3 = new ManageablePortfolioNode("pn3");
    pn3.addPosition(_pos3.getUniqueId());
    
    rootNode.addChildNode(pn2);
    rootNode.addChildNode(pn3);
    return rootNode;
  }

  @Test
  public void removeOrphanedPosition() {
    
    PositionDocument positionDocument = _positionMaster.get(_pos1.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    positionDocument = _positionMaster.get(_pos2.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    positionDocument = _positionMaster.get(_pos3.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    positionDocument = _positionMaster.get(_pos4.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    
    OrphanedPositionRemover positionRemover = new OrphanedPositionRemover(_portfolioMaster, _positionMaster);
    positionRemover.run();
    
    positionDocument = _positionMaster.get(_pos1.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    positionDocument = _positionMaster.get(_pos2.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    positionDocument = _positionMaster.get(_pos3.getUniqueId());
    AssertJUnit.assertNotNull(positionDocument.getPosition());
    try {
    _positionMaster.get(_pos4.getUniqueId());
    AssertJUnit.fail("position 4 should have been removed");
    } catch (DataNotFoundException ex) {
      //do nothing
    }
    
    
  }

}
