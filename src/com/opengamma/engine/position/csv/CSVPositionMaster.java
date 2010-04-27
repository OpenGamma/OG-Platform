/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import java.io.BufferedReader;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioId;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@code PositionMaster} based on CSV-formatted files.
 */
public class CSVPositionMaster implements PositionMaster {

  /**
   * The logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(CSVPositionMaster.class);

  /**
   * The logger.
   */
  private final File _baseDirectory;
  /**
   * The map of portfolio files by identifier.
   */
  private final Map<PortfolioId, File> _portfolioFiles = new TreeMap<PortfolioId, File>();
  /**
   * The nodes by identity key.
   */
  private final Map<Identifier, PortfolioNode> _nodesByIdentityKey = new TreeMap<Identifier, PortfolioNode>();
  /**
   * The positions by identity key.
   */
  private final Map<Identifier, Position> _positionsByIdentityKey = new TreeMap<Identifier, Position>();

  /**
   * Creates an empty CSV position master.
   */
  public CSVPositionMaster() {
    _baseDirectory = null;
  }

  /**
   * Creates a CSV position master using the specified directory.
   * @param baseDirectoryName  the directory name, not null
   */
  public CSVPositionMaster(String baseDirectoryName) {
    this(new File(baseDirectoryName));
  }

  /**
   * Creates a CSV position master using the specified directory.
   * @param baseDirectory  the directory, not null
   */
  public CSVPositionMaster(File baseDirectory) {
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
      _portfolioFiles.put(PortfolioId.of(portfolioName), file);
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
  @Override
  public Set<PortfolioId> getPortfolioIds() {
    return Collections.unmodifiableSet(_portfolioFiles.keySet());
  }

  @Override
  public Portfolio getPortfolio(PortfolioId portfolioId) {
    if (portfolioId == null || _portfolioFiles.containsKey(portfolioId.getId())) {
      return null;
    }
    return loadPortfolio(portfolioId, _portfolioFiles.get(portfolioId));
  }

  @Override
  public PortfolioNode getPortfolioNode(Identifier identityKey) {
    return _nodesByIdentityKey.get(identityKey);
  }

  @Override
  public Position getPosition(Identifier identityKey) {
    return _positionsByIdentityKey.get(identityKey);
  }

  //-------------------------------------------------------------------------
  private Portfolio loadPortfolio(PortfolioId portfolioId, File file) {
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

  private Portfolio loadPortfolio(PortfolioId portfolioId, InputStream inStream) throws IOException {
    int currPosition = 0;
    PortfolioImpl portfolio = new PortfolioImpl(portfolioId, portfolioId.getId());
    _nodesByIdentityKey.put(portfolio.getRootNode().getIdentityKey(), portfolio.getRootNode());
    
    BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
    String line = null;
    while ((line = in.readLine()) != null) {
      currPosition++;
      PositionBean position = parseLine(line);
      if (position != null) {
        String identityKey = portfolioId.getId() + "-" + currPosition;
        position.setIdentityKey(identityKey);
        portfolio.getRootNode().addPosition(position);
        _positionsByIdentityKey.put(position.getIdentityKey(), position);
      }
    }
    s_logger.info("{} parsed stream with {} positions", portfolioId, portfolio.getRootNode().getPositions().size());
    return portfolio;
  }

  /**
   * @param line  the line to parse, not null
   * @return
   */
  /* package for testing */ static PositionBean parseLine(String line) {
    String[] tokens = StringUtils.split(line, ',');
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
      Identifier id = new Identifier(new IdentificationScheme(idScheme), idValue);
      securityIdentifiers.add(id);
    }
    IdentifierBundle securityKey = new IdentifierBundle(securityIdentifiers);
    s_logger.debug("Loaded position: {} in {}", quantity, securityKey);
    
    return new PositionBean(quantity, securityKey);
  }

}
