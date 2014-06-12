/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.portfolio.PortfolioCopier;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.reader.MasterPositionReader;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePositionReader;
import com.opengamma.integration.copier.portfolio.writer.MasterPositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetSimplePositionWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.test.TestGroup;

import au.com.bytecode.opencsv.CSVReader;

@Test(groups = TestGroup.UNIT)
public class PortfolioWithTradesCopierTest {

// TODO Improve portfolio copier test coverage:
// MasterPositionReader, SingleSheetSimplePositionReader, ZippedPositionReader
// MasterPositionWriter, SingleSheetSimplePositionWriter, SingleSheetMultiParserPositionWriter, ZippedPositionWriter
// SimplePortfolioCopier, ResolvingPortfolioCopier
  
  private static final String PORTFOLIO_NAME = "test";
  private static final String PORTFOLIO_FILE = "src/test/java/com/opengamma/integration/copier/TestPortfolioWithTrades.csv";
  private static final String SECURITY_TYPE = "StandardVanillaCDS";
  
  @Test
  public void testCsvToMastersToCsv() throws Exception {
    
    PortfolioCopier portfolioCopier = new SimplePortfolioCopier();

    PositionMaster positionMaster = new InMemoryPositionMaster();
    SecurityMaster securityMaster = new InMemorySecurityMaster();
    SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    
    // Set up mock portfolio master
    PortfolioMaster portfolioMaster = mock(PortfolioMaster.class);
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(PORTFOLIO_NAME);
    PortfolioSearchResult portSearchResult = new PortfolioSearchResult();
    when(portfolioMaster.search(portSearchRequest)).thenReturn(portSearchResult);
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(PORTFOLIO_NAME);
    rootNode.setUniqueId(UniqueId.of("abc", "123"));
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME, rootNode);
    PortfolioDocument portfolioDocument = new PortfolioDocument();
    portfolioDocument.setPortfolio(portfolio);
    when(portfolioMaster.add(any(PortfolioDocument.class))).thenReturn(portfolioDocument);
    
    // file to masters
    PositionReader positionReader =
        new SingleSheetSimplePositionReader(PORTFOLIO_FILE, SECURITY_TYPE);
    PositionWriter positionWriter =
        new MasterPositionWriter(PORTFOLIO_NAME, portfolioMaster, positionMaster, securityMaster, false, false, false);
    portfolioCopier.copy(positionReader, positionWriter);
    positionReader.close();
    positionWriter.close();

    portSearchResult.setDocuments(Collections.singletonList(portfolioDocument));
    
    // Masters to file
    positionReader = new MasterPositionReader(PORTFOLIO_NAME, portfolioMaster, positionMaster, securitySource);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    positionWriter = new SingleSheetSimplePositionWriter(SheetFormat.CSV, outputStream, SECURITY_TYPE);
    portfolioCopier.copy(positionReader, positionWriter);
    positionReader.close();
    positionWriter.close();

    // Compare source and destination
    try (CSVReader sourceReader = new CSVReader(new InputStreamReader(new FileInputStream(PORTFOLIO_FILE)))) {
      try (CSVReader destReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())))) {
        int j = 1;
        do {
          String[] sourceRow;
          String[] destRow;
          try {
            sourceRow = sourceReader.readNext();
            destRow = destReader.readNext();
          } catch (Throwable e) {
            fail("Error reading the next rows: " + e);
            return;
          }
          if (sourceRow == null && destRow == null) {
            break;
          }
          assert(sourceRow != null && destRow != null);
          assertEquals(sourceRow.length, destRow.length, 
                  "Row lengths do not match (source has " + 
                  sourceRow.length + " columns while destination has " + 
                  destRow.length + " columns)");
          for (int i = 0; i < sourceRow.length; i++) {
            assertEquals(sourceRow[i], destRow[i], "Differing contents in line " + j + ", column " + i);
          }
          j++;
        } while (true);
      }
    }
  }
  
}
