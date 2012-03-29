/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple portfolio copier that copies positions from readers to the specified writer.
 */
public class SimplePortfolioCopier implements PortfolioCopier {

  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter) {
    copy(portfolioReader, portfolioWriter, null);
  }

  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter, PortfolioCopierVisitor visitor) {

    ArgumentChecker.notNull(portfolioWriter, "portfolioWriter");
    ArgumentChecker.notNull(portfolioReader, "portfolioReader");
    
    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    // Read in next row, checking for EOF
    while ((next = portfolioReader.readNext()) != null) {
      
      ManageablePosition writtenPosition;
      List<ManageableSecurity> writtenSecurities = new LinkedList<ManageableSecurity>();
      
      // Is position and security data is available for the current row?
      if (next.getFirst() != null && next.getSecond() != null) {
        
        // Set current path
        String[] path = portfolioReader.getCurrentPath();
        portfolioWriter.setPath(path);
        
        // Write position and security data
        for (ManageableSecurity security : next.getSecond()) {
          writtenSecurities.add(portfolioWriter.writeSecurity(security));
        }
        writtenPosition = portfolioWriter.writePosition(next.getFirst());
        
        if (visitor != null) {
          visitor.info(StringUtils.arrayToDelimitedString(path, "/"), writtenPosition, writtenSecurities);
        }
      } else {
        if (visitor != null) {
          visitor.error("Could not load" + (next.getFirst() == null ? " position" : "") + (next.getSecond() == null ? " security" : ""));
        }
      }
      
    }

    // Flush changes to portfolio master
    portfolioWriter.flush();
  }
}
