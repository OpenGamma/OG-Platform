/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test CSVPositionSource.
 */
public class CSVPositionSourceTest {

  private static UniqueIdentifier UID = UniqueIdentifier.of("A", "B");

  @Test
  public void parseLineEmpty() {
    assertNull(CSVPositionSource.parseLine(new String[] {""}, UID));
  }

  @Test
  public void parseLineTooShort() {
    assertNull(CSVPositionSource.parseLine(new String[] {"foo", "bar"}, UID));
  }

  @Test
  public void parseLineOneIdentifier() {
    Position position = CSVPositionSource.parseLine(new String[] {"98.4", "KIRK", "MY-ID"}, UID);
    assertNotNull(position);
    
    assertEquals(UID, position.getUniqueIdentifier());
    
    assertNotNull(position.getQuantity());
    assertEquals(0, new BigDecimal(984).scaleByPowerOfTen(-1).compareTo(position.getQuantity()));
    
    assertNotNull(position.getSecurityKey());
    assertEquals(1, position.getSecurityKey().getIdentifiers().size());
    Identifier id = position.getSecurityKey().getIdentifiers().iterator().next();
    assertNotNull(id);
    assertNotNull(id.getScheme());
    assertEquals("KIRK", id.getScheme().getName());
    assertNotNull(id.getValue());
    assertEquals("MY-ID", id.getValue());
  }

  @Test
  public void parseLineThreeIdentifiers() {
    Position position = CSVPositionSource.parseLine(new String[] {"98.4", "Domain1", "Value1", "Domain2", "Value2", "Domain3", "Value3"}, UID);
    assertNotNull(position);
    
    assertNotNull(position.getQuantity());
    assertEquals(0, new BigDecimal(984).scaleByPowerOfTen(-1).compareTo(position.getQuantity()));
    
    assertNotNull(position.getSecurityKey());
    assertEquals(3, position.getSecurityKey().getIdentifiers().size());
    
    for (Identifier id : position.getSecurityKey().getIdentifiers()) {
      assertNotNull(id);
      assertNotNull(id.getScheme());
      assertNotNull(id.getValue());
      assertEquals(id.getScheme().getName().charAt(6), id.getValue().charAt(5));
    }
  }

  private String createTempTestPortfolioDirectory() throws IOException {
    String tempDirName = getTempPortfolioDirectory();
    File tempDir = new File(tempDirName);
    if (!tempDir.mkdir()) {
      throw new IOException("Could not create temporary directory for portfolio files at '" + tempDirName + "'");
    }
    
    Collection<String> testPortfolio1 = new ArrayList<String>();
    testPortfolio1.add("1000,BbgId,APVJS.X Equity");
    testPortfolio1.add("1000,BbgId,APVJS.X Equity");
    testPortfolio1.add("2000,BbgId,APVJN.X Equity");
    testPortfolio1.add("5000,BbgId,AJLJV.X Equity");
    testPortfolio1.add("3500,BbgId,IBMJE.X Equity");
    testPortfolio1.add("450,BbgId,IBMJF.X Equity");
    FileUtils.writeLines(new File(tempDir, "testPortfolio1.csv"), testPortfolio1);
    
    Collection<String> testPortfolio2 = new ArrayList<String>();
    testPortfolio2.add("120,Domain1,Id1");
    testPortfolio2.add("400,Domain1,Id2");
    testPortfolio2.add("750,Domain2,Id1");
    testPortfolio2.add("1000,Domain3,Id1");
    FileUtils.writeLines(new File(tempDir, "testPortfolio2.port"), testPortfolio2);
    
    return tempDirName;
  }

  private String getTempPortfolioDirectory() {
    return System.getProperty("java.io.tmpdir") + "/csvPositionSourceTest-" + System.currentTimeMillis();
  }

  private void cleanUpTestPortfolios(String portfolioDirName) throws IOException {
    FileUtils.deleteDirectory(new File(portfolioDirName));
  }

  @Test
  public void testLoadPortfolios() throws IOException {
    String portfolioDirName = createTempTestPortfolioDirectory();
    CSVPositionSource pm = new CSVPositionSource(portfolioDirName);
    
    assertEquals(2, pm.getPortfolioIds().size());
    
    // Loaded correctly
    UniqueIdentifier[] portIds = pm.getPortfolioIds().toArray(new UniqueIdentifier[0]);
    Portfolio port1 = pm.getPortfolio(portIds[0]);
    assertEquals(6, port1.getRootNode().getPositions().size());   
    Portfolio port2 = pm.getPortfolio(portIds[1]);
    assertEquals(4, port2.getRootNode().getPositions().size());
    
    // Unknown portfolio should return null
    Portfolio unknownPort = pm.getPortfolio(UniqueIdentifier.of("Wrong scheme", "Irrelevant value"));
    assertNull(unknownPort);
    
    // Retrieval by root node
    PortfolioNode rootNode1 = pm.getPortfolioNode(port1.getRootNode().getUniqueIdentifier());
    assertEquals(6, rootNode1.getPositions().size());
    
    // Retrieval by position
    Position port1Pos0 = rootNode1.getPositions().get(0);
    assertEquals(port1Pos0, pm.getPosition(port1Pos0.getUniqueIdentifier()));
    
    cleanUpTestPortfolios(portfolioDirName);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonexistentPortfolioDirectory() {
    new CSVPositionSource(new File(getTempPortfolioDirectory()));
  }

}
