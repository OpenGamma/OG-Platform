/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.memory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.InstantProvider;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.financial.position.AddPortfolioNodeRequest;
import com.opengamma.financial.position.AddPortfolioRequest;
import com.opengamma.financial.position.AddPositionRequest;
import com.opengamma.financial.position.ManageablePositionMaster;
import com.opengamma.financial.position.ManagedPortfolio;
import com.opengamma.financial.position.ManagedPortfolioNode;
import com.opengamma.financial.position.ManagedPosition;
import com.opengamma.financial.position.PortfolioNodeSummary;
import com.opengamma.financial.position.PositionSummary;
import com.opengamma.financial.position.SearchPortfoliosRequest;
import com.opengamma.financial.position.SearchPortfoliosResult;
import com.opengamma.financial.position.SearchPositionsRequest;
import com.opengamma.financial.position.SearchPositionsResult;
import com.opengamma.financial.position.UpdatePortfolioNodeRequest;
import com.opengamma.financial.position.UpdatePortfolioRequest;
import com.opengamma.financial.position.UpdatePositionRequest;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierTemplate;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, in-memory implementation of {@code ManagablePositionMaster}. This implementation does not support
 * versioning or resurrection of portfolios.
 */
public class InMemoryPositionMaster implements ManageablePositionMaster {

  /**
   * The default scheme used for any {@link UniqueIdentifier}s created by this {@link PositionSource}.
   */
  public static final String DEFAULT_UID_SCHEME = "Memory";
  /**
   * The portfolios.
   */
  private final Map<UniqueIdentifier, PortfolioImpl> _portfolios = new ConcurrentHashMap<UniqueIdentifier, PortfolioImpl>();
  /**
   * A cache of nodes by identifier.
   */
  private final Map<UniqueIdentifier, PortfolioNodeImpl> _nodes = new ConcurrentHashMap<UniqueIdentifier, PortfolioNodeImpl>();
  /**
   * A cache of positions by identifier.
   */
  private final Map<UniqueIdentifier, PositionImpl> _positions = new ConcurrentHashMap<UniqueIdentifier, PositionImpl>();
  /**
   * The next index for the identifier.
   */
  private final AtomicLong _nextIdentityKey = new AtomicLong();
  /**
   * The template to use for {@link UniqueIdentifier} generation and parsing.
   */
  private final UniqueIdentifierTemplate _uidTemplate;

  /**
   * Creates an empty position master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryPositionMaster() {
    this(new UniqueIdentifierTemplate(DEFAULT_UID_SCHEME));
  }
  
  /**
   * Creates an empty position master using the specified template for any {@link UniqueIdentifier}s created.
   * 
   * @param uidTemplate  the template to use for any {@link UniqueIdentifier}s created, not null
   */
  public InMemoryPositionMaster(UniqueIdentifierTemplate uidTemplate) {
    ArgumentChecker.notNull(uidTemplate, "uidTemplate");
    _uidTemplate = uidTemplate;
  }

  //-------------------------------------------------------------------------
  // PositionMaster implementation
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return _portfolios.keySet();
  }

  @Override
  public PortfolioImpl getPortfolio(UniqueIdentifier identifier) {
    return identifier == null ? null : _portfolios.get(identifier);
  }

  @Override
  public PortfolioNodeImpl getPortfolioNode(UniqueIdentifier identifier) {
    return identifier == null ? null : _nodes.get(identifier);
  }

  @Override
  public PositionImpl getPosition(UniqueIdentifier identifier) {
    return identifier == null ? null : _positions.get(identifier);
  }

  //-------------------------------------------------------------------------
  // ManagablePositionMaster implementation
  @Override
  public boolean isModificationSupported() {
    return true;
  }
  
  @Override
  public UniqueIdentifier addPortfolio(AddPortfolioRequest request) {
    Validate.notNull(request, "AddPortfolioRequest must not be null");
    request.checkValid();
    
    final UniqueIdentifier portfolioUid = getNextPortfolioUid();
    final PortfolioImpl portfolio = getNewPortfolio(portfolioUid, request.getName());
    if (request.getRootNode() != null) {
      final PortfolioNodeImpl rootNode = buildNodeFromRequest(portfolioUid, request.getRootNode());
      portfolio.setRootNode(rootNode);
    }
    return portfolioUid;
  }
  
  private PortfolioNodeImpl buildNodeFromRequest(UniqueIdentifier portfolioUid, PortfolioNode nodeRequest) {
    final UniqueIdentifier nodeUid = getNextInternalUid(portfolioUid);
    final PortfolioNodeImpl node = getNewPortfolioNode(nodeUid, nodeRequest.getName());
    
    for (PortfolioNode childRequest : nodeRequest.getChildNodes()) {
      node.addChildNode(buildNodeFromRequest(portfolioUid, childRequest));
    }
    for (Position positionRequest : nodeRequest.getPositions()) {
      final UniqueIdentifier positionUid = getNextInternalUid(portfolioUid);
      final PositionImpl position = getNewPosition(positionUid, positionRequest.getQuantity(), positionRequest.getSecurityKey());
      node.addPosition(position);
    }
    return node;
  }

  @Override
  public UniqueIdentifier addPortfolioNode(AddPortfolioNodeRequest request) {
    Validate.notNull(request, "AddPortfolioNodeRequest must not be null");
    request.checkValid();
    
    final UniqueIdentifier parentNodeUid = request.getParentNode();
    PortfolioNodeImpl parentNode = getPortfolioNode(parentNodeUid);
    if (parentNode == null) {
      throw new DataNotFoundException("The specified parent node could not be found");
    }
    final UniqueIdentifier portfolioUid = extractPortfolioUid(parentNodeUid);
    final UniqueIdentifier newPortfolioNodeUid = getNextInternalUid(portfolioUid);
    final PortfolioNodeImpl newPortfolioNode = getNewPortfolioNode(newPortfolioNodeUid, request.getName());
    parentNode.addChildNode(newPortfolioNode);
    return newPortfolioNodeUid;
  }

  @Override
  public UniqueIdentifier addPosition(AddPositionRequest request) {
    Validate.notNull(request, "AddPositionRequest must not be null");
    request.checkValid();
    
    final UniqueIdentifier parentNodeUid = request.getParentNode();
    PortfolioNodeImpl parentNode = getPortfolioNode(parentNodeUid);
    if (parentNode == null) {
      throw new DataNotFoundException("The specified parent node could not be found");
    }
    final UniqueIdentifier portfolioUid = extractPortfolioUid(parentNodeUid);
    final UniqueIdentifier newPositionUid = getNextInternalUid(portfolioUid);
    final PositionImpl newPosition = getNewPosition(newPositionUid, request.getQuantity(), request.getSecurityKey());
    parentNode.addPosition(newPosition);
    return newPositionUid;
  }
  
  @Override
  public UniqueIdentifier updatePortfolio(UpdatePortfolioRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public UniqueIdentifier updatePortfolioNode(UpdatePortfolioNodeRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public UniqueIdentifier updatePosition(UpdatePositionRequest request) {
    throw new NotImplementedException();
  }
  
  @Override
  public UniqueIdentifier removePortfolio(UniqueIdentifier portfolioUid) {
    throw new NotImplementedException();
  }

  @Override
  public UniqueIdentifier removePortfolioNode(UniqueIdentifier nodeUid) {
    throw new NotImplementedException();
  }

  @Override
  public UniqueIdentifier removePosition(UniqueIdentifier positionUid) {
    throw new NotImplementedException();
  }

  @Override
  public ManagedPortfolio getManagedPortfolio(UniqueIdentifier portfolioUid) {
    Portfolio portfolio = getPortfolio(portfolioUid);
    if (portfolio == null) {
      throw new DataNotFoundException("The specified portfolio could not be found");
    }
    return getManagedPortfolio(portfolio); 
  }
  
  private ManagedPortfolio getManagedPortfolio(Portfolio portfolio) {
    ManagedPortfolio managedPortfolio = new ManagedPortfolio();
    managedPortfolio.setUniqueIdentifier(portfolio.getUniqueIdentifier());
    managedPortfolio.setName(portfolio.getName());
    ManagedPortfolioNode managedRootNode = getManagedPortfolioNode(portfolio.getRootNode());
    managedPortfolio.setRootNode(managedRootNode);
    return managedPortfolio;
  }

  @Override
  public ManagedPortfolioNode getManagedPortfolioNode(UniqueIdentifier nodeUid) {
    PortfolioNode portfolioNode = getPortfolioNode(nodeUid);
    if (portfolioNode == null) {
      throw new DataNotFoundException("The specified portfolio node could not be found");
    }
    return getManagedPortfolioNode(portfolioNode);
  }
  
  private ManagedPortfolioNode getManagedPortfolioNode(PortfolioNode portfolioNode) {
    ManagedPortfolioNode managedNode = new ManagedPortfolioNode();
    managedNode.setUniqueIdentifier(portfolioNode.getUniqueIdentifier());
    managedNode.setName(portfolioNode.getName());
    managedNode.setPortfolioUid(extractPortfolioUid(portfolioNode.getUniqueIdentifier()));
    // TODO: set parent node UID
    
    for (PortfolioNode childNode : portfolioNode.getChildNodes()) {
      PortfolioNodeSummary childNodeSummary = new PortfolioNodeSummary();
      childNodeSummary.setUniqueIdentifier(childNode.getUniqueIdentifier());
      childNodeSummary.setName(childNode.getName());
      childNodeSummary.setTotalPositions(countPositions(childNode));
      managedNode.getChildNodes().add(childNodeSummary);
    }
    
    for (Position position : portfolioNode.getPositions()) {
      PositionSummary positionSummary = new PositionSummary();
      positionSummary.setUniqueIdentifier(position.getUniqueIdentifier());
      positionSummary.setQuantity(position.getQuantity());
    }
    
    return managedNode;
  }

  @Override
  public ManagedPosition getManagedPosition(UniqueIdentifier positionUid) {
    Position position = getPosition(positionUid);
    if (position == null) {
      throw new DataNotFoundException("The specified position could not be found");
    }
    
    ManagedPosition managedPosition = new ManagedPosition();
    managedPosition.setUniqueIdentifier(positionUid);
    managedPosition.setPortfolioUid(extractPortfolioUid(positionUid));
    managedPosition.setQuantity(position.getQuantity());
    managedPosition.setSecurityKey(position.getSecurityKey());
    // TODO: set parent node UID
    return managedPosition;
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid, InstantProvider instantProvider) {
    return getPortfolio(uid);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid, InstantProvider instantProvider) {
    return getPortfolioNode(uid);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid, InstantProvider instantProvider) {
    return getPosition(uid);
  }

  @Override
  public boolean isManagerFor(UniqueIdentifier uid) {
    throw new NotImplementedException();
  }

  @Override
  public SearchPortfoliosResult searchPortfolios(SearchPortfoliosRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public SearchPositionsResult searchPositions(SearchPositionsRequest request) {
    throw new NotImplementedException();
  }
  
  @Override
  public UniqueIdentifier reinstatePortfolio(UniqueIdentifier portfolioUid) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UniqueIdentifier reinstatePortfolioNode(UniqueIdentifier nodeUid) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UniqueIdentifier reinstatePosition(UniqueIdentifier positionUid) {
    throw new UnsupportedOperationException();
  }
  
  //-------------------------------------------------------------------------
  private long getNextId() {
    return _nextIdentityKey.incrementAndGet();
  }
  
  private UniqueIdentifier getNextPortfolioUid() {
    return _uidTemplate.uid(Long.toString(getNextId()));
  }
  
  private UniqueIdentifier getNextInternalUid(UniqueIdentifier portfolioUid) {
    String valueContent = portfolioUid.getValue() + "-" + getNextId();
    return _uidTemplate.uid(valueContent);
  }
  
  private UniqueIdentifier extractPortfolioUid(UniqueIdentifier nodeUid) {
    String valueContent = _uidTemplate.extractValueContent(nodeUid);
    int pos = valueContent.indexOf("-");
    if (pos < 0) {
      throw new IllegalArgumentException("The specified UniqueIdentifier is not in the expected format");
    }
    String portfolioValue = valueContent.substring(0, pos);
    return _uidTemplate.uid(portfolioValue);
  }
  
  private PortfolioImpl getNewPortfolio(UniqueIdentifier portfolioUid, String portfolioName) {
    final PortfolioImpl portfolio = new PortfolioImpl(portfolioUid, portfolioName);
    _portfolios.put(portfolioUid, portfolio);
    return portfolio;
  }

  private PortfolioNodeImpl getNewPortfolioNode(UniqueIdentifier nodeUid, String nodeName) {
    final PortfolioNodeImpl node = new PortfolioNodeImpl(nodeUid, nodeName);
    _nodes.put(nodeUid, node);
    return node;
  }
  
  private PositionImpl getNewPosition(UniqueIdentifier positionUid, BigDecimal quantity, IdentifierBundle securityKey) {
    final PositionImpl position = new PositionImpl(positionUid, quantity, securityKey);
    _positions.put(positionUid, position);
    return position;
  }
  
  private int countPositions(PortfolioNode node) {
    int total = node.getPositions().size();
    for (PortfolioNode childNode : node.getChildNodes()) {
      total += countPositions(childNode);
    }
    return total;
  }
}
