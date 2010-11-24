/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.PositionSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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
   * The portfolio files by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, Object> _portfolios = new ConcurrentSkipListMap<UniqueIdentifier, Object>();
  /**
   * The nodes by identifier.
   */
  private final Map<UniqueIdentifier, PortfolioNode> _nodes = new TreeMap<UniqueIdentifier, PortfolioNode>();
  /**
   * The positions by identifier.
   */
  private final Map<UniqueIdentifier, Position> _positions = new TreeMap<UniqueIdentifier, Position>();
  /**
   * The trades by identifier.
   */
  private final Map<UniqueIdentifier, Trade> _trades = new TreeMap<UniqueIdentifier, Trade>();
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
      _portfolios.put(UniqueIdentifier.of("CSV-" + file.getName(), portfolioName), file);
    }
  }

  private String buildPortfolioName(String fileName) {
    if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
      return fileName.substring(0, fileName.length() - 4);
    }
    return fileName;
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
  public Set<UniqueIdentifier> getPortfolioIds() {
    return Collections.unmodifiableSet(_portfolios.keySet());
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier portfolioId) {
    Object portfolio = _portfolios.get(portfolioId);
    if (portfolio instanceof File) {
      Portfolio created = loadPortfolio(portfolioId, (File) portfolio);
      _portfolios.replace(portfolioId, portfolio, created);
      portfolio = _portfolios.get(portfolioId);
    }
    if (portfolio instanceof Portfolio) {
      return (Portfolio) portfolio;
    }
    return null;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier identifier) {
    return _nodes.get(identifier);
  }

  @Override
  public Position getPosition(UniqueIdentifier identifier) {
    return _positions.get(identifier);
  }
  
  @Override
  public Trade getTrade(UniqueIdentifier identifier) {
    return _trades.get(identifier);
  }

  //-------------------------------------------------------------------------
  private Portfolio loadPortfolio(UniqueIdentifier portfolioId, File file) {
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

  private Portfolio loadPortfolio(UniqueIdentifier portfolioId, InputStream inStream) throws IOException {
    PortfolioImpl portfolio = new PortfolioImpl(portfolioId, portfolioId.getValue());
    UniqueIdentifier rootNodeId = UniqueIdentifier.of(portfolioId.getScheme(), "0");
    portfolio.getRootNode().setUniqueIdentifier(rootNodeId);
    _nodes.put(rootNodeId, portfolio.getRootNode());
    
    CSVReader csvReader = new CSVReader(new InputStreamReader(inStream));
    String[] tokens = null;
    int curIndex = 1;
    UniqueIdentifier positionId = UniqueIdentifier.of(portfolioId.getScheme(), Integer.toString(curIndex));
    while ((tokens = csvReader.readNext()) != null) {
      PositionImpl position = parseLine(tokens, positionId);
      if (position != null) {
        position.setPortfolioNode(rootNodeId);
        ((PortfolioNodeImpl) portfolio.getRootNode()).addPosition(position);
        _positions.put(position.getUniqueIdentifier(), position);
        positionId = UniqueIdentifier.of(portfolioId.getScheme(), Integer.toString(++curIndex));
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
  /* package for testing */ static PositionImpl parseLine(String[] tokens, UniqueIdentifier positionId) {
    if (tokens.length < 3) {
      return null;
    }
    // First token is the quantity
    BigDecimal quantity = new BigDecimal(tokens[0].trim());
    
    // Each set of 2 tokens is then security id domain and then id 
    List<Identifier> securityIdentifiers = new ArrayList<Identifier>();
    for (int i = 1; i < (tokens.length - 1); i++) {
      String idScheme = tokens[i].trim();
      String idValue = tokens[++i].trim();
      Identifier id = Identifier.of(idScheme, idValue);
      securityIdentifiers.add(id);
    }
    IdentifierBundle securityKey = IdentifierBundle.of(securityIdentifiers);
    s_logger.debug("Loaded position: {} in {}", quantity, securityKey);
    
    return new PositionImpl(positionId, quantity, securityKey);
  }
}
