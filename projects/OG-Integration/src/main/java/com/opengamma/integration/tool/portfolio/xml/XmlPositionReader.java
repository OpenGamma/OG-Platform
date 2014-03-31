/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.util.Iterator;

import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Portfolio reader for a single portfolio from the xml file format. Note that
 * as the class implements PositionReader, it is stateful and not thread safe.
 */
public class XmlPositionReader implements PositionReader {

  /**
   * The name of the portfolio being processed. May be null if no name is
   * supplied in the file.
   */
  private final String _portfolioName;

  /**
   * Iterator to handle the ongoing reading of portfolio data from the file, not null.
   */
  private final Iterator<PortfolioPosition> _positionIterator;

  /**
   * The path of the current portfolio within the root portfolio.
   */
  private String[] _currentPath = new String[0];

  /**
   * Create a portfolio reader for the portfolio handler. The portfolio handler acts as
   * a buffer between the version specific xml parsing code and the rest of the system.
   *
   * @param vph the portfolio handler to create a reader for.
   */
  public XmlPositionReader(VersionedPortfolioHandler vph) {
    _portfolioName = vph.getPortfolioName();
    _positionIterator = vph.getPositions().iterator();
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
    return _positionIterator.hasNext() ? processPosition(_positionIterator.next()) : null;
  }

  private ObjectsPair<ManageablePosition, ManageableSecurity[]> processPosition(PortfolioPosition position) {
    // Handle a portfolio level change
    _currentPath = position.getPortfolioPath();
    return ObjectsPair.of(position.getPosition(), position.getSecurities());
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }

  @Override
  public void close() {
    // Nothing to do as the file is handled outside of this class
  }

  @Override
  public String getPortfolioName() {
    return _portfolioName;
  }
}
