/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose position master.
 * <p>
 * The position master provides a uniform view over a set of portfolios and positions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface PositionMaster {

  /**
   * Searches for portfolio trees matching the specified search criteria.
   * <p>
   * The result will never contain positions, and may contain the node tree depending
   * on the depth parameter in the request.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PortfolioTreeSearchResult searchPortfolioTrees(PortfolioTreeSearchRequest request);

  /**
   * Gets a portfolio tree, excluding positions, by unique identifier.
   * <p>
   * To obtain the positions, use {@link #searchPositions}.
   * <p>
   * A full position master will store detailed historic information on portfolios,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the tree document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no portfolio with that unique identifier
   */
  PortfolioTreeDocument getPortfolioTree(UniqueIdentifier uid);

  /**
   * Adds a portfolio tree, excluding positions, to the data store.
   * <p>
   * The specified document must contain the portfolio tree.
   * 
   * @param document  the document, not null
   * @return the updated tree document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PortfolioTreeDocument addPortfolioTree(PortfolioTreeDocument document);

  /**
   * Updates a portfolio tree, excluding positions, in the data store.
   * <p>
   * This will replace the entire the entire portfolio tree with that specified.
   * Any node in the input document that has a null unique identifier will be added.
   * Any node that already has a unique identifier will be updated.
   * Any positions on nodes that are removed will be removed.
   * <p>
   * The specified document must contain the portfolio tree and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * <p>
   * A full position master will store detailed historic information on portfolios,
   * including a full version history.
   * Older versions can be accessed using a versioned identifier or {@link #historyPortfolioTree}.
   * 
   * @param document  the document, not null
   * @return the updated tree document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no portfolio with that unique identifier
   */
  PortfolioTreeDocument updatePortfolioTree(PortfolioTreeDocument document);

  /**
   * Removes a portfolio from the data store.
   * <p>
   * This will remove the entire portfolio and any associated positions.
   * <p>
   * A full position master will store detailed historic information on portfolios.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uid  the portfolio unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no portfolio with that unique identifier
   */
  void removePortfolioTree(final UniqueIdentifier uid);

  /**
   * Queries the history of a single portfolio tree.
   * <p>
   * The request must contain an object identifier to identify the portfolio tree.
   * 
   * @param request  the history request, not null
   * @return the portfolio tree history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PortfolioTreeHistoryResult historyPortfolioTree(PortfolioTreeHistoryRequest request);

  /**
   * Corrects a portfolio tree in the data store.
   * <p>
   * A full position master will store detailed historic information on portfolios
   * and will support correction of each node.
   * To update the node with a new version, use {@link #updatePortfolioTree}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier or {@link #historyPortfolioTree}.
   * <p>
   * The specified document must contain the portfolio tree and the portfolio unique identifier.
   * The unique identifier must specify the last correction of a specific version of the portfolio.
   * 
   * @param document  the document, not null
   * @return the updated tree document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no portfolio with that unique identifier
   */
  PortfolioTreeDocument correctPortfolioTree(PortfolioTreeDocument document);

  //-------------------------------------------------------------------------
  /**
   * Searches for positions matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PositionSearchResult searchPositions(PositionSearchRequest request);

  /**
   * Gets a position by unique identifier.
   * <p>
   * A full position master will store detailed historic information on positions,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the position document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no position with that unique identifier
   */
  PositionDocument getPosition(UniqueIdentifier uid);

  /**
   * Adds a position to the data store.
   * <p>
   * The specified document must contain the position and parent node.
   * 
   * @param document  the document, not null
   * @return the updated position document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no parent node with the specified identifier
   */
  PositionDocument addPosition(PositionDocument document);

  /**
   * Updates a position in the data store.
   * <p>
   * The specified document must contain the position and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * <p>
   * A full position master will store detailed historic information on positions,
   * including a full version history.
   * Older versions can be accessed using a versioned identifier or {@link #historyPosition}.
   * 
   * @param document  the document, not null
   * @return the updated position document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no position with that unique identifier
   */
  PositionDocument updatePosition(PositionDocument document);

  /**
   * Removes a position from the data store.
   * <p>
   * A full position master will store detailed historic information on positions.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uid  the position unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no position with that unique identifier
   */
  void removePosition(final UniqueIdentifier uid);

  /**
   * Queries the history of a single position.
   * <p>
   * The request must contain an object identifier to identify the position.
   * 
   * @param request  the history request, not null
   * @return the position history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PositionHistoryResult historyPosition(PositionHistoryRequest request);

  /**
   * Corrects a position in the data store.
   * <p>
   * A full position master will store detailed historic information on positions
   * and will support correction of each position.
   * To update the position with a new version, use {@link #updatePosition}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier or {@link #historyPosition}.
   * <p>
   * The specified document must contain the position and the unique identifier.
   * The unique identifier must specify the last correction of a specific version of the position.
   * 
   * @param document  the document, not null
   * @return the updated position document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no position with that unique identifier
   */
  PositionDocument correctPosition(PositionDocument document);

  //-------------------------------------------------------------------------
  /**
   * Gets a single portfolio, including all child nodes and positions.
   * <p>
   * This allows direct access to the entire tree with positions.
   * It is intended for fast access to the whole structure.
   * 
   * @param request  the request, not null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the request is invalid
   */
  Portfolio getFullPortfolio(FullPortfolioGetRequest request);

  /**
   * Gets a single node, including all child nodes and positions.
   * <p>
   * This allows direct access to the entire tree with positions.
   * It is intended for fast access to the whole structure.
   * 
   * @param request  the request, not null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the request is invalid
   */
  PortfolioNode getFullPortfolioNode(FullPortfolioNodeGetRequest request);

  /**
   * Gets a single position with full detail.
   * <p>
   * This allows direct access to the position.
   * It is intended for fast access to the whole structure.
   * 
   * @param request  the request, not null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the request is invalid
   */
  Position getFullPosition(FullPositionGetRequest request);

}
