/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose position master.
 * <p>
 * The position master provides a uniform view over a set of portfolios and positions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface PositionMaster {

  /**
   * Searches for portfolios matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  PortfolioSearchResult searchPortfolios(PortfolioSearchRequest request);

  /**
   * Gets a portfolio by unique identifier.
   * <p>
   * A full portfolio master will store detailed historic information on portfolios,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the portfolio document, null if not found
   * @throws IllegalArgumentException if the identifier is not from this portfolio master
   */
  PortfolioDocument getPortfolio(UniqueIdentifier uid);

//  /**
//   * Adds a portfolio to the data store.
//   * <p>
//   * The specified document must contain the portfolio.
//   * It must not contain the unique identifier.
//   * 
//   * @param document  the document, not null
//   * @return the updated portfolio document, not null
//   * @throws IllegalArgumentException if the request is invalid
//   */
//  PortfolioDocument addPortfolio(PortfolioDocument document);
//
//  /**
//   * Updates a portfolio in the data store.
//   * <p>
//   * The specified document must contain the portfolio and the unique identifier.
//   * If the identifier has a version it must be the latest version.
//   * <p>
//   * A full portfolio master will store detailed historic information on portfolios,
//   * including a full version history.
//   * Older versions can be accessed using a versioned identifier or {@link #searchHistoric}.
//   * 
//   * @param document  the document, not null
//   * @return the updated portfolio document, not null
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the portfolio is not found
//   */
//  PortfolioDocument updatePortfolio(PortfolioDocument document);
//
//  /**
//   * Removes a portfolio from the data store.
//   * <p>
//   * A full portfolio master will store detailed historic information on portfolios.
//   * Thus, a removal does not prevent retrieval or correction of an earlier version.
//   * <p>
//   * If the identifier has a version it must be the latest version.
//   * 
//   * @param uid  the portfolio unique identifier to remove, not null
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the portfolio is not found
//   */
//  void removePortfolio(final UniqueIdentifier uid);
//
  /**
   * Searches for portfolios matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  PortfolioSearchHistoricResult searchPortfolioHistoric(PortfolioSearchHistoricRequest request);
//
//  /**
//   * Corrects a portfolio in the data store.
//   * <p>
//   * A full portfolio master will store detailed historic information on portfolios
//   * and will support correction of each portfolio.
//   * To update the portfolio with a new version, use {@link #update}.
//   * To correct a previously stored version, use this method.
//   * Older versions and corrections can be accessed using a versioned identifier or {@link #searchHistoric}.
//   * <p>
//   * The specified document must contain the portfolio and the unique identifier.
//   * The unique identifier must specify the last correction of a specific version of the portfolio.
//   * 
//   * @param document  the document, not null
//   * @return the updated portfolio document, not null
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the portfolio is not found
//   */
//  PortfolioDocument correctPortfolio(PortfolioDocument document);
//
//  //-------------------------------------------------------------------------
//  /**
//  * Searches for portfolio trees matching the specified search criteria.
//  * 
//  * @param request  the search request, not null
//  * @return the search result, not null
//  */
//  PortfolioTreeSearchResult searchPortfolioTrees(PortfolioTreeSearchRequest request);

  /**
  * Gets a portfolio tree by unique identifier.
  * <p>
  * A full position master will store detailed historic information on portfolios,
  * including a full version history.
  * The version in the identifier allows access to these historic versions.
  * 
  * @param uid  the unique identifier, not null
  * @return the tree document, null if not found
  * @throws IllegalArgumentException if the identifier is not from this position master
  */
  PortfolioTreeDocument getPortfolioTree(UniqueIdentifier uid);

  /**
  * Adds a portfolio tree to the data store.
  * <p>
  * The specified document must contain the portfolio tree.
  * It must not contain the unique identifier.
  * 
  * @param document  the document, not null
  * @return the updated tree document, not null
  * @throws IllegalArgumentException if the request is invalid
  */
  PortfolioTreeDocument addPortfolioTree(PortfolioTreeDocument document);

  /**
  * Updates a portfolio tree in the data store.
  * <p>
  * The specified document must contain the portfolio tree and the unique identifier.
  * If the identifier has a version it must be the latest version.
  * <p>
  * A full position master will store detailed historic information on portfolios,
  * including a full version history.
  * Older versions can be accessed using a versioned identifier or {@link #searchHistoric}.
  * 
  * @param document  the document, not null
  * @return the updated tree document, not null
  * @throws IllegalArgumentException if the request is invalid
  * @throws DataNotFoundException if the node is not found
  */
  PortfolioTreeDocument updatePortfolioTree(PortfolioTreeDocument document);

  /**
  * Removes a portfolio tree from the data store.
  * <p>
  * A full position master will store detailed historic information on portfolios.
  * Thus, a removal does not prevent retrieval or correction of an earlier version.
  * <p>
  * If the identifier has a version it must be the latest version.
  * 
  * @param uid  the portfolio unique identifier to remove, not null
  * @throws IllegalArgumentException if the request is invalid
  * @throws DataNotFoundException if the node is not found
  */
  void removePortfolioTree(final UniqueIdentifier uid);

//  /**
//  * Searches for portfolio trees matching the specified search criteria.
//  * <p>
//  * The request must contain an object identifier that must not have a version.
//  * 
//  * @param request  the search request, not null
//  * @return the search result, not null
//  */
//  PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(PortfolioTreeSearchHistoricRequest request);

  /**
  * Corrects a portfolio tree in the data store.
  * <p>
  * A full position master will store detailed historic information on portfolios
  * and will support correction of each node.
  * To update the node with a new version, use {@link #update}.
  * To correct a previously stored version, use this method.
  * Older versions and corrections can be accessed using a versioned identifier or {@link #searchHistoric}.
  * <p>
  * The specified document must contain the portfolio tree and the portfolio unique identifier.
  * The unique identifier must specify the last correction of a specific version of the portfolio.
  * 
  * @param document  the document, not null
  * @return the updated tree document, not null
  * @throws IllegalArgumentException if the request is invalid
  * @throws DataNotFoundException if the node is not found
  */
  PortfolioTreeDocument correctPortfolioTree(PortfolioTreeDocument document);

//  //-------------------------------------------------------------------------
//  /**
//   * Searches for nodes matching the specified search criteria.
//   * 
//   * @param request  the search request, not null
//   * @return the search result, not null
//   */
//  PortfolioNodeSearchResult searchPortfolioNodes(PortfolioNodeSearchRequest request);

  /**
   * Gets a node by unique identifier.
   * <p>
   * A full position master will store detailed historic information on nodes,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the node document, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  PortfolioNodeDocument getPortfolioNode(UniqueIdentifier uid);

//  /**
//   * Adds a node to the data store.
//   * <p>
//   * The specified document must contain the node.
//   * It must not contain the unique identifier.
//   * 
//   * @param document  the document, not null
//   * @return the updated node document, not null
//   * @throws IllegalArgumentException if the request is invalid
//   */
//  PortfolioNodeDocument addPortfolioNode(PortfolioNodeDocument document);
//
//  /**
//   * Updates a node in the data store.
//   * <p>
//   * The specified document must contain the node and the unique identifier.
//   * If the identifier has a version it must be the latest version.
//   * <p>
//   * A full position master will store detailed historic information on nodes,
//   * including a full version history.
//   * Older versions can be accessed using a versioned identifier or {@link #searchHistoric}.
//   * 
//   * @param document  the document, not null
//   * @return the updated node document, not null
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the node is not found
//   */
//  PortfolioNodeDocument updatePortfolioNode(PortfolioNodeDocument document);
//
//  /**
//   * Removes a node from the data store.
//   * <p>
//   * A full position master will store detailed historic information on nodes.
//   * Thus, a removal does not prevent retrieval or correction of an earlier version.
//   * <p>
//   * If the identifier has a version it must be the latest version.
//   * 
//   * @param uid  the node unique identifier to remove, not null
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the node is not found
//   */
//  void removePortfolioNode(final UniqueIdentifier uid);
//
//  /**
//   * Searches for nodes matching the specified search criteria.
//   * <p>
//   * The request must contain an object identifier that must not have a version.
//   * 
//   * @param request  the search request, not null
//   * @return the search result, not null
//   */
//  PortfolioNodeSearchHistoricResult searchPortfolioNodeHistoric(PortfolioNodeSearchHistoricRequest request);
//
//  /**
//   * Corrects a node in the data store.
//   * <p>
//   * A full position master will store detailed historic information on nodes
//   * and will support correction of each node.
//   * To update the node with a new version, use {@link #update}.
//   * To correct a previously stored version, use this method.
//   * Older versions and corrections can be accessed using a versioned identifier or {@link #searchHistoric}.
//   * <p>
//   * The specified document must contain the node and the unique identifier.
//   * The unique identifier must specify the last correction of a specific version of the node.
//   * 
//   * @param document  the document, not null
//   * @return the updated node document, not null
//   * @throws IllegalArgumentException if the request is invalid
//   * @throws DataNotFoundException if the node is not found
//   */
//  PortfolioNodeDocument correctPortfolioNode(PortfolioNodeDocument document);

  //-------------------------------------------------------------------------
  /**
   * Searches for positions matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
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
   * @return the position document, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  PositionDocument getPosition(UniqueIdentifier uid);

  /**
   * Adds a position to the data store.
   * <p>
   * The specified document must contain the position.
   * It must not contain the unique identifier.
   * 
   * @param document  the document, not null
   * @return the updated position document, not null
   * @throws IllegalArgumentException if the request is invalid
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
   * Older versions can be accessed using a versioned identifier or {@link #searchHistoric}.
   * 
   * @param document  the document, not null
   * @return the updated position document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the position is not found
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
   * @throws DataNotFoundException if the position is not found
   */
  void removePosition(final UniqueIdentifier uid);

  /**
   * Searches for positions matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  PositionSearchHistoricResult searchPositionHistoric(PositionSearchHistoricRequest request);

  /**
   * Corrects a position in the data store.
   * <p>
   * A full position master will store detailed historic information on positions
   * and will support correction of each position.
   * To update the position with a new version, use {@link #update}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier or {@link #searchHistoric}.
   * <p>
   * The specified document must contain the position and the unique identifier.
   * The unique identifier must specify the last correction of a specific version of the position.
   * 
   * @param document  the document, not null
   * @return the updated position document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the position is not found
   */
  PositionDocument correctPosition(PositionDocument document);

}
