/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple portfolio copier that copies positions from readers to the specified writer.
 */
public class ProfilingPortfolioCopier implements PortfolioCopier {

  private static final Logger s_logger = LoggerFactory.getLogger(ProfilingPortfolioCopier.class);
  private String[] _structure;

  private int _readTime;
  private int _writeTime;

  public ProfilingPortfolioCopier() {
    _structure = null;
  }


  public ProfilingPortfolioCopier(String[] structure) {
    _structure = structure;
  }

  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter) {
    copy(portfolioReader, portfolioWriter, null);
  }

  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter, PortfolioCopierVisitor visitor) {

    ArgumentChecker.notNull(portfolioWriter, "portfolioWriter");
    ArgumentChecker.notNull(portfolioReader, "portfolioReader");
    
    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    while (true) {

      Instant time = Instant.now();

      // Read in next row, checking for errors and EOF
      try {
        next = portfolioReader.readNext();
      } catch (Exception e) {
        // skip to next row on uncaught exception while parsing row
        s_logger.error("Unable to parse row", e);
        continue;
      }
      if (next == null) {
        // stop loading on EOF
        break;
      }

      _readTime += time.periodUntil(Instant.now(), ChronoUnit.MILLIS);
      time = Instant.now();

      // Is position and security data is available for the current row?
          ManageablePosition position = next.getFirst();
      ManageableSecurity[] securities = next.getSecond();

      // Is position and security data available for the current row?
      if (position != null && securities != null) {

        // Set current path
        String[] path;
        if (_structure == null) {
          path = portfolioReader.getCurrentPath();
        } else {
          path = new String[_structure.length];
          for (int i = 0; i < _structure.length; i++) {
            path[i] = position.getAttributes().get(_structure[i]);
          }
        }
        portfolioWriter.setPath(path);

        // Write position and security data
        ObjectsPair<ManageablePosition, ManageableSecurity[]> written =
            portfolioWriter.writePosition(position, securities);
        
        if (visitor != null && written != null) {
          visitor.info(StringUtils.arrayToDelimitedString(path, "/"), written.getFirst(), written.getSecond());
        }
      } else {
        if (visitor != null) {
          if (position == null) {
            visitor.error("Could not load position");
          }
          if (securities == null) {
            visitor.error("Could not load security(ies)");
          }
        }
      }

      _writeTime += time.periodUntil(Instant.now(), ChronoUnit.MILLIS);
    }

    System.out.println("Read time: " + _readTime / 1000.0);
    System.out.println("Write time: " + _writeTime / 1000.0);
  }
}
