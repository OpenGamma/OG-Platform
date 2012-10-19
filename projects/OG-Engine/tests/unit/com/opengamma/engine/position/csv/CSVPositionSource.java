/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of positions based on CSV-formatted files.
 */
public class CSVPositionSource implements PositionSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CSVPositionSource.class);

  /**
   * The base file directory.
   */
  private final File _baseDirectory;
  /**
   * The portfolio by identifier.
   */
  private final ConcurrentMap<ObjectId, Object> _portfolios = new ConcurrentSkipListMap<ObjectId, Object>();
  /**
   * The nodes by identifier.
   */
  private final Map<UniqueId, PortfolioNode> _nodes = new TreeMap<UniqueId, PortfolioNode>();
  /**
   * The positions by identifier.
   */
  private final Map<ObjectId, Position> _positions = new TreeMap<ObjectId, Position>();
  /**
   * The trades by identifier.
   */
  private final Map<UniqueId, Trade> _trades = new TreeMap<UniqueId, Trade>();

  /**
   * Creates an empty CSV position source.
   */
  public CSVPositionSource() {
    _baseDirectory = null;
  }

  /**
   * Creates a CSV position source using the specified directory.
   * @param baseDirectoryName  the directory name, not null
   */
  public CSVPositionSource(String baseDirectoryName) {
    this(new File(baseDirectoryName));
  }

  /**
   * Creates a CSV position source using the specified directory.
   * @param baseDirectory  the directory, not null
   */
  public CSVPositionSource(File baseDirectory) {
    ArgumentChecker.notNull(baseDirectory, "base directory");
    if (baseDirectory.exists() == false) {
      throw new IllegalArgumentException("Base directory must exist: " + baseDirectory);
    }
    if (baseDirectory.isDirectory() == false) {
      throw new IllegalArgumentException("Base directory must be a directory: " + baseDirectory);
    }
    try {
      _baseDirectory = baseDirectory.getCanonicalFile();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Base directory must resolve to a canonical reference: " + baseDirectory, ex);
    }
    populatePortfolioIds();
  }

  /**
   * Populate the portfolio identifiers from the base directory.
   */
  private void populatePortfolioIds() {
    File[] filesInBaseDirectory = getBaseDirectory().listFiles();
    for (File file : filesInBaseDirectory) {
      if (file.isFile() == false || file.isHidden() || file.canRead() == false) {
        continue;
      }
      String portfolioName = buildPortfolioName(file.getName());
      _portfolios.put(ObjectId.of("CSV-" + file.getName(), portfolioName), file);
    }
  }

  private String buildPortfolioName(String fileName) {
    if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
      return fileName.substring(0, fileName.length() - 4);
    }
    return fileName;
  }

  private Position getPosition(final ObjectId positionId) {
    Position position = _positions.get(positionId);
    if (position == null) {
      throw new DataNotFoundException("Unable to find position: " + positionId);
    }
    return position;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base directory.
   * @return the baseDirectory, may be null
   */
  public File getBaseDirectory() {
    return _baseDirectory;
  }

  //-------------------------------------------------------------------------
  public Set<ObjectId> getPortfolioIds() {
    return Collections.unmodifiableSet(_portfolios.keySet());
  }

  @Override
  public Portfolio getPortfolio(UniqueId portfolioId, final VersionCorrection versionCorrection) {
    // Ignore the version
    return getPortfolio(portfolioId.getObjectId(), VersionCorrection.LATEST);
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    Object portfolio = _portfolios.get(objectId);
    if (portfolio instanceof File) {
      Portfolio created = loadPortfolio(objectId, (File) portfolio);
      _portfolios.replace(objectId, portfolio, created);
      portfolio = _portfolios.get(objectId);
    }
    if (portfolio instanceof Portfolio) {
      return (Portfolio) portfolio;
    }
    throw new DataNotFoundException("Unable to find portfolio: " + objectId);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId nodeId, final VersionCorrection versionCorrection) {
    PortfolioNode node = _nodes.get(nodeId);
    if (node == null) {
      throw new DataNotFoundException("Unable to find node: " + nodeId);
    }
    return node;
  }

  @Override
  public Position getPosition(UniqueId positionId) {
    // Ignore the version
    return getPosition(positionId.getObjectId());
  }
  
  @Override
  public Position getPosition(ObjectId positionId, VersionCorrection versionCorrection) {
    // Ignore the version
    return getPosition(positionId);
  }

  @Override
  public Trade getTrade(UniqueId tradeId) {
    Trade trade = _trades.get(tradeId);
    if (trade == null) {
      throw new DataNotFoundException("Unable to find trade: " + tradeId);
    }
    return trade;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  //-------------------------------------------------------------------------
  private Portfolio loadPortfolio(ObjectId portfolioId, File file) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      return loadPortfolio(portfolioId, fis);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to parse portfolio file: " + file, ex);
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }

  private Portfolio loadPortfolio(ObjectId portfolioId, InputStream inStream) throws IOException {
    SimplePortfolio portfolio = new SimplePortfolio(portfolioId.atVersion("0"), portfolioId.getValue());
    UniqueId rootNodeId = UniqueId.of(portfolioId.getScheme(), "0");
    portfolio.getRootNode().setUniqueId(rootNodeId);
    _nodes.put(rootNodeId, portfolio.getRootNode());
    
    CSVReader csvReader = new CSVReader(new InputStreamReader(inStream));
    String[] tokens = null;
    int curIndex = 1;
    UniqueId positionId = UniqueId.of(portfolioId.getScheme(), Integer.toString(curIndex));
    while ((tokens = csvReader.readNext()) != null) {
      SimplePosition position = parseLine(tokens, positionId);
      if (position != null) {
        ((SimplePortfolioNode) portfolio.getRootNode()).addPosition(position);
        _positions.put(position.getUniqueId().getObjectId(), position);
        positionId = UniqueId.of(portfolioId.getScheme(), Integer.toString(++curIndex));
      }
    }
    s_logger.info("{} parsed stream with {} positions", portfolioId, portfolio.getRootNode().getPositions().size());
    return portfolio;
  }

  /**
   * @param line  the line to parse, not null
   * @param positionId  the portfolio id, not null
   * @return the position
   */
  /* package for testing */ static SimplePosition parseLine(String[] tokens, UniqueId positionId) {
    if (tokens.length < 3) {
      return null;
    }
    // First token is the quantity
    BigDecimal quantity = new BigDecimal(tokens[0].trim());
    
    // Each set of 2 tokens is then security id domain and then id 
    List<ExternalId> securityIdentifiers = new ArrayList<ExternalId>();
    for (int i = 1; i < (tokens.length - 1); i++) {
      String idScheme = tokens[i].trim();
      String idValue = tokens[++i].trim();
      ExternalId id = ExternalId.of(idScheme, idValue);
      securityIdentifiers.add(id);
    }
    ExternalIdBundle securityKey = ExternalIdBundle.of(securityIdentifiers);
    s_logger.debug("Loaded position: {} in {}", quantity, securityKey);
    
    return new SimplePosition(positionId, quantity, securityKey);
  }

}
