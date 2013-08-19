/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.snapshot.ManageableSnapshot;
import com.opengamma.master.snapshot.ManageableSnapshotNode;
import com.opengamma.master.snapshot.SnapshotDocument;
import com.opengamma.master.snapshot.SnapshotMaster;
import com.opengamma.master.snapshot.SnapshotSearchRequest;
import com.opengamma.master.snapshot.SnapshotSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
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
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.beancompare.BeanCompare;
import com.opengamma.util.beancompare.BeanDifference;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A class that writes securities and snapshot positions and trades to the OG masters
 */
public class MasterSnapshotWriter implements SnapshotWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(MasterSnapshotWriter.class);

  private static final int NUMBER_OF_THREADS = 30;

  private final SnapshotMaster _snapshotMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;
  private final SecuritySource _securitySource;

  private SnapshotDocument _snapshotDocument;
  private ManageableSnapshotNode _currentNode;
  private ManageableSnapshotNode _originalNode;
  private ManageableSnapshotNode _originalRoot;

  private String[] _currentPath;

  private BeanCompare _beanCompare;

  private boolean _mergePositions;
  private Map<ObjectId, ManageablePosition> _securityIdToPosition;

  private boolean _keepCurrentPositions;

  private boolean _discardIncompleteOptions;

  private boolean _multithread;
  private ExecutorService _executorService;


  /**
   * Create a master snapshot writer
   * @param snapshotName             The name of the snapshot to create/write to
   * @param snapshotMaster           The snapshot master to which to write the snapshot
   * @param positionMaster            The position master to which to write positions
   * @param securityMaster            The security master to which to write securities
   * @param mergePositions            If true, attempt to roll multiple positions in the same security into one position,
   *                                  for all positions in the same snapshot node;
   *                                  if false, each position is loaded separately
   * @param keepCurrentPositions      If true, keep the existing snapshot node tree and add new entries;
   *                                  if false, delete the entire existing snapshot node tree before loading the new
   *                                  snapshot
   * @param discardIncompleteOptions  If true, when an underlying cannot be loaded, the position/trade will be discarded;
   *                                  if false, the option will be created with a dangling reference to the underlying
   */

  public MasterSnapshotWriter(String snapshotName,
                               SnapshotMaster snapshotMaster,
                               PositionMaster positionMaster,
                               SecurityMaster securityMaster,
                               boolean mergePositions,
                               boolean keepCurrentPositions,
                               boolean discardIncompleteOptions) {
    this(snapshotName, snapshotMaster, positionMaster, securityMaster, mergePositions,
         keepCurrentPositions, discardIncompleteOptions, false);
  }

  public MasterSnapshotWriter(String snapshotName,
                               SnapshotMaster snapshotMaster,
                               PositionMaster positionMaster,
                               SecurityMaster securityMaster,
                               boolean mergePositions,
                               boolean keepCurrentPositions,
                               boolean discardIncompleteOptions,
                               boolean multithread) {

    ArgumentChecker.notEmpty(snapshotName, "snapshotName");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");

    _mergePositions = mergePositions;
    _keepCurrentPositions = keepCurrentPositions;
    _discardIncompleteOptions = discardIncompleteOptions;

    _snapshotMaster = snapshotMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;

    _securitySource = new MasterSecuritySource(_securityMaster);

    _beanCompare = new BeanCompare();

    //_currentPath = new String[0];
    //_securityIdToPosition = new HashMap<ObjectId, ManageablePosition>();

    _multithread = multithread;
    if (_multithread) {
      _executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    createSnapshot(snapshotName);

    _securityIdToPosition = new HashMap<>();
    setPath(new String[0]);
  }

  @Override
  public void addAttribute(String key, String value) {
    _snapshotDocument.getSnapshot().addAttribute(key, value);
  }

  /**
   * Returns the sum of the quantities for the specified positions. This is separated out into a method to allow
   * custom behaviour for different clients. For instance, in one case the sums of the quantities of all the trades
   * of both positions might be required, whereas in another case the preference might be to sum the quantities of
   * the positions themselves without regard to the quantities specified in their trades (this is the default behaviour).
   * This is not featured in the SnapshotWriter interface, and as such is a hack.
   * @param position1 the first position
   * @param position2 the second position
   * @return the sum of the positions' quantities
   */
  protected BigDecimal sumPositionQuantities(final ManageablePosition position1, final ManageablePosition position2) {
    return position1.getQuantity().add(position2.getQuantity());
  }

  /**
   * WritePosition checks if the position exists in the previous version of the snapshot.
   * If so, the existing position is reused.
   * @param position    the position to be written
   * @param securities  the security(ies) related to the above position, also to be written; index 1 onwards are underlyings
   * @return            the positions/securities in the masters after writing, null on failure
   */
  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> writePosition(final ManageablePosition position, final ManageableSecurity[] securities) {
    
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(securities, "securities");

    // Write securities
    final List<ManageableSecurity> writtenSecurities = new ArrayList<>();
    for (ManageableSecurity security : securities) {
      if (security != null || !_discardIncompleteOptions) { // latter term preserves old behaviour
        ManageableSecurity writtenSecurity = writeSecurity(security);
        if (writtenSecurity != null) {
          writtenSecurities.add(writtenSecurity);
        }
      }
    }

    // If no securities were actually written successfully, just skip writing this position entirely
    if (writtenSecurities.size() != securities.length && _discardIncompleteOptions) {
      // this does persist the securities that it is given so that we don't keep hitting Bloomberg when there are missing underlyings.
      return null;
    } else if (writtenSecurities.isEmpty()) { // preserve old behaviour if _discardIncompleteOptions is false
      return null;
    }

    // If merging positions, check if any of the positions in the current node reference the same security id
    // and if so, just update the existing position and return
    if (_mergePositions && _securityIdToPosition.containsKey(writtenSecurities.get(0).getUniqueId().getObjectId())) {

      // Add new quantity to existing position's quantity
      final ManageablePosition existingPosition = _securityIdToPosition.get(writtenSecurities.get(0).getUniqueId().getObjectId());
      existingPosition.setQuantity(sumPositionQuantities(existingPosition, position));

      // Add new trades to existing position's trades
      for (ManageableTrade trade : position.getTrades()) {
        existingPosition.addTrade(trade);
      }

      if (!_multithread) {
        // Save the updated existing position to the position master
        PositionDocument addedDoc = _positionMaster.update(new PositionDocument(existingPosition));
        s_logger.debug("Updated position {}, delta position {}", addedDoc.getPosition(), position);

        // update position map (huh?)
        _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), addedDoc.getPosition());

        // Return the updated position
        return new ObjectsPair<>(addedDoc.getPosition(),
            securities);
      } else {
        // update position map
        _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), existingPosition);

         // Return the updated position
        return new ObjectsPair<>(existingPosition, securities);
      }
    }
    // Attempt to reuse an existing position from the previous version of the snapshot, and return if an exact match is found
    if (!(_originalNode == null) && !_originalNode.getPositionIds().isEmpty()) {
      ManageablePosition existingPosition = matchExistingPosition(position, writtenSecurities);
      if (existingPosition != null) {
        return new ObjectsPair<>(existingPosition,
            writtenSecurities.toArray(new ManageableSecurity[writtenSecurities.size()]));
      }
    }

    // No existing position could be reused/updated: just Add the new position to the position master as a new document
    // (can't launch a thread since we need the position id immediately, to be stored with the pos document in the map)
    PositionDocument addedDoc;
    try {
      addedDoc = _positionMaster.add(new PositionDocument(position));
      s_logger.debug("Added position {}", position);
    } catch (Exception e) {
      s_logger.error("Unable to add position " + position.getUniqueId() + ": " + e.getMessage());
      return null;
    }
    // Add the new position to the snapshot
    _currentNode.addPosition(addedDoc.getUniqueId());

    // Update position map
    _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), addedDoc.getPosition());

    // Return the new position
    return new ObjectsPair<>(addedDoc.getPosition(),
        writtenSecurities.toArray(new ManageableSecurity[writtenSecurities.size()]));
  }

  private ManageablePosition matchExistingPosition(final ManageablePosition position, final List<ManageableSecurity> writtenSecurities) {
    PositionSearchRequest searchReq = new PositionSearchRequest();

    // Filter positions in current node of original snapshot
    searchReq.setPositionObjectIds(_originalNode.getPositionIds());

    // Filter positions with the same quantity
    searchReq.setMinQuantity(position.getQuantity());
    searchReq.setMaxQuantity(position.getQuantity());

    // TODO Compare position attributes

    PositionSearchResult searchResult = _positionMaster.search(searchReq);
    for (ManageablePosition existingPosition : searchResult.getPositions()) {
      ManageablePosition chosenPosition = null;
      if (writtenSecurities.get(0).getUniqueId().getObjectId().equals(existingPosition.getSecurityLink().getObjectId())) {
        chosenPosition = existingPosition;
      } else {
        for (ExternalId id : existingPosition.getSecurityLink().getExternalIds()) {
          if (writtenSecurities.get(0).getExternalIdBundle().contains(id) && existingPosition.getQuantity().equals(position.getQuantity())) {
            chosenPosition = existingPosition;
            break;
          }
        }
      }
      // Check for trade equality
      if (chosenPosition != null && (chosenPosition.getTrades().size() == position.getTrades().size())) {

        for (ManageableTrade trade : chosenPosition.getTrades()) {

          ManageableTrade comparableTrade = JodaBeanUtils.clone(trade);
          comparableTrade.setUniqueId(null);
          if (!(position.getTrades().contains(comparableTrade))) {
            chosenPosition = null;
            break;
          }
        }

        // If identical, reuse the chosen position
        if (chosenPosition != null) {
          // Add the existing position to the snapshot
          _currentNode.addPosition(chosenPosition.getUniqueId());

          // Update position map
          _securityIdToPosition.put(writtenSecurities.get(0).getUniqueId().getObjectId(), chosenPosition);

          // return existing position
          return chosenPosition;
        }
      }
    }

    return null;
  }

  /*
   * writeSecurity searches for an existing security that matches an external id search, and attempts to
   * reuse/update it wherever possible, instead of creating a new one.
   */
  private ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    ArgumentChecker.notNull(security, "security");
    
    SecuritySearchRequest searchReq = new SecuritySearchRequest();
    ExternalIdSearch idSearch = new ExternalIdSearch(security.getExternalIdBundle());  // match any one of the IDs
    searchReq.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now())); // valid now
    searchReq.setExternalIdSearch(idSearch);
    searchReq.setFullDetail(true);
    searchReq.setSortOrder(SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    SecuritySearchResult searchResult = _securityMaster.search(searchReq);

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
      if (differences != null && (differences.isEmpty() || (differences.size() == 1 && differences.get(0).getProperty().propertyType() == UniqueId.class))) {
        // It's already there, don't update or add it
        return foundSecurity;
      } else {
        s_logger.debug("Updating security " + foundSecurity + " due to differences: " + differences);
        SecurityDocument updateDoc = new SecurityDocument(security);
        updateDoc.setVersionFromInstant(Instant.now());
        try {
          //updateDoc.setUniqueId(foundSecurity.getUniqueId());
          //return _securityMaster.update(updateDoc).getSecurity();
          UniqueId newId = _securityMaster.addVersion(foundSecurity.getUniqueId().getObjectId(), updateDoc);
          security.setUniqueId(newId);
          return security;
        } catch (Throwable t) {
          s_logger.error("Unable to update security " + security.getUniqueId() + ": " + t.getMessage());
          return null;
        }
      }
    }

    // Not found, so add it
    SecurityDocument addDoc = new SecurityDocument(security);
    try {
      SecurityDocument result = _securityMaster.add(addDoc);
      return result.getSecurity();
    } catch (Exception e) {
      s_logger.error("Failed to write security " + security + " to the security master", e);
      return null;
    }
  }

  private void testQuantities(ManageablePosition position) {
    int tradeQty = 0;
    for (ManageableTrade trade : position.getTrades()) {
      tradeQty += trade.getQuantity().intValue();
    }
    if (tradeQty != position.getQuantity().intValue()) {
      s_logger.warn("Position quantity and total trade quantities do not match for " + position);
    }
  }

  @Override
  public String[] getCurrentPath() {
    Stack<ManageableSnapshotNode> stack =
        _snapshotDocument.getSnapshot().getRootNode().findNodeStackByObjectId(_currentNode.getUniqueId());
    String[] result = new String[stack.size()];
    int i = stack.size();
    while (!stack.isEmpty()) {
      result[--i] = stack.pop().getName();
    }
    return result;
  }

  @Override
  public void setPath(String[] newPath) {
    ArgumentChecker.noNulls(newPath, "newPath");

    if (!Arrays.equals(newPath, _currentPath)) {

      // Update positions in position map, concurrently, and wait for their completion
      if (_mergePositions && _multithread) {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (final ManageablePosition position : _securityIdToPosition.values()) {
          testQuantities(position);
          tasks.add(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
              try {
                // Update the position in the position master
                PositionDocument addedDoc = _positionMaster.update(new PositionDocument(position));
                s_logger.debug("Updated position {}", position);
                 // Add the new position to the snapshot node
                _currentNode.addPosition(addedDoc.getUniqueId());
              } catch (Exception e) {
                s_logger.error("Unable to update position " + position.getUniqueId() + ": " + e.getMessage());
              }
              return 0;
            }
          });
        }
        try {
          List<Future<Integer>> futures = _executorService.invokeAll(tasks);
        } catch (Exception e) {
          s_logger.warn("ExecutorService invokeAll failed: " + e.getMessage());
        }
      }

      // Reset position map
      _securityIdToPosition = new HashMap<>();

      if (_originalRoot != null) {
        _originalNode = findNode(newPath, _originalRoot);
        _currentNode = getOrCreateNode(newPath, _snapshotDocument.getSnapshot().getRootNode());
      } else {
        _currentNode = getOrCreateNode(newPath, _snapshotDocument.getSnapshot().getRootNode());
      }

      // If keeping original snapshot nodes and merging positions, populate position map with existing positions in node
      if (_keepCurrentPositions && _mergePositions && _originalNode != null) {
        s_logger.debug("Storing security associations for positions " + _originalNode.getPositionIds() + " at path " + StringUtils.join(newPath, '/'));
        for (ObjectId positionId : _originalNode.getPositionIds()) {
          ManageablePosition position = null;
          try {
            position = _positionMaster.get(positionId, VersionCorrection.LATEST).getPosition();
          } catch (Exception e) {
            // no action
            s_logger.error("Exception retrieving position " + positionId, e);
          }
          if (position != null) {
            position.getSecurityLink().resolve(_securitySource);
            if (position.getSecurity() != null) {
              if (_securityIdToPosition.containsKey(position.getSecurity())) {
                ManageablePosition existing = _securityIdToPosition.get(position.getSecurity());
                s_logger.warn("Merging positions but found existing duplicates under path " + StringUtils.join(newPath, '/') + ": " + position + " and " + existing
                    + ".  New trades for security " + position.getSecurity().getUniqueId().getObjectId() + " will be added to position " + position.getUniqueId());
              
              } else {
                _securityIdToPosition.put(position.getSecurity().getUniqueId().getObjectId(), position);
              }
            }
          }
        }
        if (s_logger.isDebugEnabled()) {
          StringBuilder sb = new StringBuilder("Cached security to position mappings at path ").append(StringUtils.join(newPath, '/')).append(":");
          for (Map.Entry<ObjectId, ManageablePosition> entry : _securityIdToPosition.entrySet()) {
            sb.append(System.lineSeparator()).append("  ").append(entry.getKey()).append(" = ").append(entry.getValue().getUniqueId());
          }
          s_logger.debug(sb.toString());
        }
      }

      _currentPath = newPath;
    }
  }

  @Override
  public void flush() {
    _snapshotDocument = _snapshotMaster.update(_snapshotDocument);
  }
  
  @Override
  public void close() {
    // Execute remaining position writing threads, which will update the snapshot nodes with any written positions'
    // object IDs
    if (_executorService != null) {
      _executorService.shutdown();
    }

    // Write the snapshot (include the node tree) to the snapshot master
    flush();
  }
  
  private ManageableSnapshotNode findNode(String[] path, ManageableSnapshotNode startNode) {

    // Degenerate case
    if (path.length == 0) {
      return startNode;
    }

    for (ManageableSnapshotNode childNode : startNode.getChildNodes()) {
      if (path[0].equals(childNode.getName())) {
        ManageableSnapshotNode result = findNode((String[]) ArrayUtils.subarray(path, 1, path.length), childNode);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }
  
  private ManageableSnapshotNode getOrCreateNode(String[] path, ManageableSnapshotNode startNode) {
    ManageableSnapshotNode node = startNode;
    for (String p : path) {
      ManageableSnapshotNode foundNode = null;
      for (ManageableSnapshotNode n : node.getChildNodes()) {
        if (n.getName().equals(p)) {
          foundNode = n;
          break;
        }
      }
      if (foundNode == null) {
        ManageableSnapshotNode newNode = new ManageableSnapshotNode(p);
        node.addChildNode(newNode);
        node = newNode;
      } else {
        node = foundNode;
      }
    }
    return node;
  }

  protected void createSnapshot(String snapshotName) {

    // Check to see whether the snapshot already exists
    SnapshotSearchRequest portSearchRequest = new SnapshotSearchRequest();
    portSearchRequest.setName(snapshotName);
    SnapshotSearchResult portSearchResult = _snapshotMaster.search(portSearchRequest);

    _snapshotDocument = portSearchResult.getFirstDocument();

    // If it doesn't, create it (add)
    if (_snapshotDocument == null) {
      // Create a new root node
      ManageableSnapshotNode rootNode = new ManageableSnapshotNode(snapshotName);

      ManageableSnapshot snapshot = new ManageableSnapshot(snapshotName, rootNode);
      _snapshotDocument = new SnapshotDocument();
      _snapshotDocument.setSnapshot(snapshot);
      _snapshotDocument = _snapshotMaster.add(_snapshotDocument);
      _originalRoot = null;
      _originalNode = null;

      // Set current node to the root node
      _currentNode = rootNode;

      // If it does, create a new version of the existing snapshot (update)
    } else {
      ManageableSnapshot snapshot = _snapshotDocument.getSnapshot();
      _originalRoot = snapshot.getRootNode();
      _originalNode = _originalRoot;

      if (_keepCurrentPositions) {
        // Use the original root node
        snapshot.setRootNode(cloneTree(_originalRoot));
        _snapshotDocument.setSnapshot(snapshot);

        // Set current node to the root node
        _currentNode = snapshot.getRootNode();
      } else {
        // Create a new root node
        ManageableSnapshotNode rootNode;
        rootNode = JodaBeanUtils.clone(_originalRoot);
        rootNode.setChildNodes(new ArrayList<ManageableSnapshotNode>());
        rootNode.setPositionIds(new ArrayList<ObjectId>());
        snapshot.setRootNode(rootNode);
        _snapshotDocument.setSnapshot(snapshot);

        // Set current node to the root node
        _currentNode = rootNode;
      }
    }
  }

  private static ManageableSnapshotNode cloneTree(final ManageableSnapshotNode originalRoot) {
    ManageableSnapshotNode newRoot = JodaBeanUtils.clone(originalRoot);
    newRoot.setChildNodes(new ArrayList<ManageableSnapshotNode>());
    for (ManageableSnapshotNode child : originalRoot.getChildNodes()) {
      newRoot.addChildNode(cloneTree(child));
    }
    return newRoot;
  }

  // TODO are these methods necessary? they're not used
  public SnapshotMaster getSnapshotMaster() {
    return _snapshotMaster;
  }

  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

}
