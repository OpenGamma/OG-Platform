package com.opengamma.integration.copier;

import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import org.testng.annotations.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.portfolio.PortfolioCopier;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.reader.MasterPortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetSimplePortfolioWriter;
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

public class PortfolioCopierTest {

// TODO Improve portfolio copier test coverage:
// MasterPortfolioReader, SingleSheetSimplePortfolioReader, ZippedPortfolioReader
// MasterPortfolioWriter, SingleSheetSimplePortfolioWriter, SingleSheetMultiParserPortfolioWriter, ZippedPortfolioWriter
// SimplePortfolioCopier, ResolvingPortfolioCopier
  
  private static final String PORTFOLIO_NAME = "test";
  private static final String PORTFOLIO_FILE = "tests/unit/com/opengamma/integration/copier/TestPortfolio.csv";
  private static final String SECURITY_TYPE = "Equity";
  
  @Test
  public void testCsvToMastersToCsv() {
    
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
    PortfolioReader portfolioReader = 
        new SingleSheetSimplePortfolioReader(PORTFOLIO_FILE, SECURITY_TYPE);
    PortfolioWriter portfolioWriter = new MasterPortfolioWriter(PORTFOLIO_NAME, portfolioMaster, positionMaster, securityMaster, false);
    portfolioCopier.copy(portfolioReader, portfolioWriter);
    portfolioReader.close();
    portfolioWriter.close();

    portSearchResult.setDocuments(Collections.singletonList(portfolioDocument));
    
    // Masters to file
    portfolioReader = new MasterPortfolioReader(PORTFOLIO_NAME, portfolioMaster, positionMaster, securitySource);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    portfolioWriter = new SingleSheetSimplePortfolioWriter(SheetFormat.CSV, outputStream, SECURITY_TYPE);
    portfolioCopier.copy(portfolioReader, portfolioWriter);
    portfolioReader.close();
    portfolioWriter.close();

    // Compare source and destination
    CSVReader sourceReader;
    CSVReader destReader;
    try {
      sourceReader = new CSVReader(new InputStreamReader(new FileInputStream(PORTFOLIO_FILE)));
      destReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
    } catch (Throwable e) {
      fail("Could not open files for comparing: " + e);
      return;
    }
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

    try {
      sourceReader.close();
      destReader.close();
    } catch (IOException ex) {
      fail("Could not close files after comparing: " + ex);
    }
    
  }
  
}
