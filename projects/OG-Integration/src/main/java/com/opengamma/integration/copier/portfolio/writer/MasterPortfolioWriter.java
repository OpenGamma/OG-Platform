/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.beancompare.BeanCompare;
import com.opengamma.util.beancompare.BeanDifference;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A class that writes securities and portfolio positions and trades to the OG masters
 */
public class MasterPortfolioWriter implements PortfolioWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(MasterPortfolioWriter.class);

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;
  
  private PortfolioDocument _portfolioDocument;
  private ManageablePortfolioNode _currentNode;
  private ManageablePortfolioNode _originalNode;
  private ManageablePortfolioNode _originalRoot;

  private String[] _currentPath;

  private BeanCompare _beanCompare;

  private boolean _overwrite;

  private boolean _mergePositions;
  private Map<ObjectId, ManageablePosition> _securityIdToPosition;

  private boolean _keepCurrentPositions;


  public MasterPortfolioWriter(String portfolioName, PortfolioMaster portfolioMaster,
      PositionMaster positionMaster, SecurityMaster securityMaster, boolean overwrite,
      boolean mergePositions, boolean keepCurrentPositions)  {

    ArgumentChecker.notEmpty(portfolioName, "portfolioName");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");

    _overwrite = overwrite;
    _mergePositions = mergePositions;
    _keepCurrentPositions = keepCurrentPositions;

    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;

    _currentPath = new String[0];

    _beanCompare = new BeanCompare();

    _securityIdToPosition = new HashMap<ObjectId, ManageablePosition>();

    createPortfolio(portfolioName);
  }

  /**
   * WritePosition checks if the position exists in the previous version of the portfolio.
   * If so, the existing position is reused.
   * @param position    the position to be written
   * @param securities  the security(ies) related to the above position, also to be written; index 1 onwards are underlyings
   * @return            the positions/securities in the masters after writing
   */
  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> writePosition(ManageablePosition position, ManageableSecurity[] securities) {
    
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(securities, "securities");

    // Write securities
    List<ManageableSecurity> writtenSecurities = new ArrayList<ManageableSecurity>();
    for (ManageableSecurity security : securities) {
      ManageableSecurity writtenSecurity = writeSecurity(security);
      if (writtenSecurity != null) {
        writtenSecurities.add(writtenSecurity);
      }
    }

    // If merging positions, check if any of the positions in the current node reference the same security id
    // and if so, just update the existing position
    if (_mergePositions && _securityIdToPosition.containsKey(writtenSecurities.get(0).getUniqueId().getObjectId())) {

      // Just add new quantity to existing position
      ManageablePosition existingPosition = _securityIdToPosition.get(writtenSecurities.get(0).getUniqueId().getObjectId());
      existingPosition.setQuantity(existingPosition.getQuantity().add(position.getQuantity()));

      // Save the updated position to the position master
      PositionDocument addedDoc = _positionMaster.update(new PositionDocument(existingPosition));

      // update position map (huh?)
      _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), addedDoc.getPosition());

      // Return the updated position
      return new ObjectsPair<ManageablePosition, ManageableSecurity[]>(addedDoc.getPosition(),
          securities);
    }

    // Write position
    if (_overwrite) {
      // Add the new position to the position master
      PositionDocument addedDoc = _positionMaster.add(new PositionDocument(position));

      // Add the new position to the portfolio
      _currentNode.addPosition(addedDoc.getUniqueId());

      // Update position map
      _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), addedDoc.getPosition());

      // Return the new position
      return new ObjectsPair<ManageablePosition, ManageableSecurity[]>(addedDoc.getPosition(),
          writtenSecurities.toArray(new ManageableSecurity[writtenSecurities.size()]));

    } else {

      if (!(_originalNode == null) && !_originalNode.getPositionIds().isEmpty()) {
        PositionSearchRequest searchReq = new PositionSearchRequest();

        // Filter positions in current node of original portfolio
        searchReq.setPositionObjectIds(_originalNode.getPositionIds());

        // Filter positions with same external ids
        ExternalIdSearch externalIdSearch = new ExternalIdSearch();
        externalIdSearch.addExternalIds(position.getSecurityLink().getExternalIds());
        externalIdSearch.setSearchType(ExternalIdSearchType.ALL);
        searchReq.setSecurityIdSearch(externalIdSearch);

        // Filter positions with the same quantity
        searchReq.setMinQuantity(position.getQuantity());
        searchReq.setMaxQuantity(position.getQuantity());

        // Search
        PositionSearchResult searchResult = _positionMaster.search(searchReq);

        PositionDocument firstDocument = searchResult.getFirstDocument();
        if (firstDocument != null) {
          ManageablePosition existingPosition = firstDocument.getPosition();
          // Add the existing position to the portfolio
          _currentNode.addPosition(existingPosition.getUniqueId());

          // Update position map
          _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), existingPosition);

          // Return the existing position
          return new ObjectsPair<ManageablePosition, ManageableSecurity[]>(existingPosition,
              writtenSecurities.toArray(new ManageableSecurity[writtenSecurities.size()]));
        }

        // TODO also confirm that all the associated trades are identical
      }

      // Add the new position to the position master
      PositionDocument addedDoc = _positionMaster.add(new PositionDocument(position));

      // Add the new position to the portfolio
      _currentNode.addPosition(addedDoc.getUniqueId());

      // Update position map
      _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), addedDoc.getPosition());

      // Return the new position
      return new ObjectsPair<ManageablePosition, ManageableSecurity[]>(addedDoc.getPosition(),
          writtenSecurities.toArray(new ManageableSecurity[writtenSecurities.size()]));
    }
  }


  /*
   * writeSecurity searches for an existing security that matches an external id search, and attempts to
   * reuse/update it wherever possible, instead of creating a new one.
   */
  private ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    ArgumentChecker.notNull(security, "security");
    
    SecuritySearchRequest searchReq = new SecuritySearchRequest();
    ExternalIdSearch idSearch = new ExternalIdSearch(security.getExternalIdBundle());  // match any one of the IDs
    searchReq.setVersionCorrection(VersionCorrection.ofVersionAsOf(ZonedDateTime.now())); // valid now
    searchReq.setExternalIdSearch(idSearch);
    searchReq.setFullDetail(true);
    searchReq.setSortOrder(SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    SecuritySearchResult searchResult = _securityMaster.search(searchReq);
    if (_overwrite) {
      for (ManageableSecurity foundSecurity : searchResult.getSecurities()) {
        _securityMaster.remove(foundSecurity.getUniqueId());
      }
    } else {
      for (ManageableSecurity foundSecurity : searchResult.getSecurities()) {
        List<BeanDifference<?>> differences = null;
        if (foundSecurity.getClass().equals(security.getClass())) {
          try {
            differences = _beanCompare.compare(foundSecurity, security);
          } catch (Exception e) {
            s_logger.error("Error comparing securities with ID bundle " + security.getExternalIdBundle(), e);
            return null;
          }
        }
        if (differences != null && differences.size() == 1 && differences.get(0).getProperty().propertyType() == UniqueId.class) {
          // It's already there, don't update or add it
          return foundSecurity;
        } else {
          SecurityDocument updateDoc = new SecurityDocument(security);
          updateDoc.setUniqueId(foundSecurity.getUniqueId());
          try {
            return _securityMaster.update(updateDoc).getSecurity();
          } catch (Throwable t) {
            s_logger.error("Unable to update security " + security.getUniqueId() + ": " + t.getMessage());
            return null;
          }
        }
      }
    }

    // Not found, so add it
    SecurityDocument addDoc = new SecurityDocument(security);
    SecurityDocument result = _securityMaster.add(addDoc);
    return result.getSecurity();
  }
  
  @Override
  public String[] getCurrentPath() {
    Stack<ManageablePortfolioNode> stack = 
        _portfolioDocument.getPortfolio().getRootNode().findNodeStackByObjectId(_currentNode.getUniqueId());
    String[] result = new String[stack.size()];
    int i = stack.size();
    while (!stack.isEmpty()) {
      result[--i] = stack.pop().getName();
    }
    return result;
  }

  @Override
  public void setPath(String[] newPath) {
    
    ArgumentChecker.notNull(newPath, "newPath");

    if (!Arrays.equals(newPath, _currentPath)) {

      if (newPath.length == 0) {
        _currentNode = _portfolioDocument.getPortfolio().getRootNode();
        _originalNode = _originalRoot;
      } else {
        if (_originalRoot != null) {
          _originalNode = findNode(newPath, _originalRoot);
        }
        _currentNode = createNode(newPath, _portfolioDocument.getPortfolio().getRootNode());
      }

      // Reset position map
      _securityIdToPosition = new HashMap<ObjectId, ManageablePosition>();

      _currentPath = newPath;
    }
  }

  @Override
  public void flush() {
    _portfolioDocument = _portfolioMaster.update(_portfolioDocument);
  }
  
  @Override
  public void close() {
    flush();
  }
  
  private ManageablePortfolioNode findNode(String[] path, ManageablePortfolioNode startNode) {
    
    // Degenerate case
    if (path.length == 1) {
      if (startNode.getName().equals(path[0])) {
        return startNode;
      } else {
        return null;
      }
    
    // Recursive case, traverse all child nodes
    } else {
      for (ManageablePortfolioNode childNode : startNode.getChildNodes()) {
        String[] newPath = (String[]) ArrayUtils.subarray(path, 1, path.length - 1);
        ManageablePortfolioNode result = findNode(newPath, childNode);
        if (result != null) {
          return result;
        }
      }
      return null;
    }
  }
  
  private ManageablePortfolioNode createNode(String[] path, ManageablePortfolioNode startNode) {
    ManageablePortfolioNode node = startNode;
    for (String p : path) {
      ManageablePortfolioNode foundNode = null;
      for (ManageablePortfolioNode n : node.getChildNodes()) {
        if (n.getName().equals(p)) {
          foundNode = n;
          break;
        }
      }
      if (foundNode == null) {
        ManageablePortfolioNode newNode = new ManageablePortfolioNode(p);
        node.addChildNode(newNode);
        node = newNode;
      } else {
        node = foundNode;
      }
    }
    return node;
  }
  
  protected void createPortfolio(String portfolioName) {


    // Check to see whether the portfolio already exists
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    PortfolioSearchResult portSearchResult = _portfolioMaster.search(portSearchRequest);

    if (_overwrite) {
      for (PortfolioDocument doc : portSearchResult.getDocuments()) {
        _portfolioMaster.remove(doc.getUniqueId());
      }
      // Create a new root node
      ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);

      ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
      _portfolioDocument = new PortfolioDocument();
      _portfolioDocument.setPortfolio(portfolio);
      _portfolioDocument = _portfolioMaster.add(_portfolioDocument);
      _originalRoot = null;
      _originalNode = null;

    } else {
      _portfolioDocument = portSearchResult.getFirstDocument();

      // If it doesn't, create it (add) 
      if (_portfolioDocument == null) {
        // Create a new root node
        ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);

        ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
        _portfolioDocument = new PortfolioDocument();
        _portfolioDocument.setPortfolio(portfolio);
        _portfolioDocument = _portfolioMaster.add(_portfolioDocument);
        _originalRoot = null;
        _originalNode = null;
        
      // If it does, create a new version of the existing portfolio (update)
      } else {
        ManageablePortfolio portfolio = _portfolioDocument.getPortfolio();
        _originalRoot = portfolio.getRootNode();
        _originalNode = _originalRoot;

        ManageablePortfolioNode rootNode;
        if (_keepCurrentPositions) {
          // Use the original root node
          rootNode = _originalRoot;
        } else {
          // Create a new root node
          rootNode = new ManageablePortfolioNode(portfolioName);
        }

        portfolio.setRootNode(rootNode);
        _portfolioDocument.setPortfolio(portfolio);
        _portfolioDocument = _portfolioMaster.update(_portfolioDocument);
      }      
    }
    // Set current node to the root node
    _currentNode = _portfolioDocument.getPortfolio().getRootNode();
  }

  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }
}
