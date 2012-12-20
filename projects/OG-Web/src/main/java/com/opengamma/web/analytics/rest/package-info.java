/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * <p>This package provides push notifications when changes occur to resources requested through the REST interface.
 * This includes analytics data, entities and queries for entities.</p>
 *
 * <h1>Establishing a Connection</h1>
 * <p>Before a client can receive notifications of updates it must establish a connection and be assigned a
 * client ID.  A connection corresponds to a single view, e.g. a single browser tab.  A user can have multiple
 * connections active at the same time.  A connection is set up with a request to the handshake URL:</p>
 * <pre>
 *   /handshake</pre>
 * <p>The response contains JSON with the client ID, e.g.</p>
 * <pre>
 *   {"clientId": "1234"}</pre>
 *
 * <h1>Subscribing to Notifications of Updates</h1>
 * <p>There is no way to explicitly subscribe to notifications.  If a client ID is included in a request
 * for a REST resource a subscription will be created. e.g.</p>
 * <pre>
 *   /jax/portfolios/DbPrt~234</pre>
 * <p>will return the portfolio details and</p>
 * <pre>
 *   /jax/portfolios/DbPrt~234?clientId=1234</pre>
 * <p>will return the portfolio details and set up a subscription.  The client with ID {@code 1234} will
 * receive a notification if the portfolio is updated.</p>
 * <p>There are three different types of object that can produce updates:</p>
 *
 * <h2>Entities</h2>
 * <p>If a client requests an entity (for example a portfolio as shown above) it will receive a notification
 * if the entity is updated.  To enable this the REST method that returns the entity must have a parameter annotated
 * with {@link com.opengamma.web.analytics.rest.Subscribe}.  The annotation must be on a string parameter that can be
 * parsed by {@link com.opengamma.id.UniqueId#parse UniqueId.parse()} and the parameter must also have a
 * {@link javax.ws.rs.PathParam PathParam} annotation.
 * See {@link com.opengamma.web.portfolio.WebPortfoliosResource#findPortfolio WebPortfoliosResource.findPortfolio()}
 * for an example.</p>
 *
 * <h2>Queries</h2>
 * <p>If a client performs a query to search for multiple entities it will receive a notification if something
 * is updated that <em>might</em> change the result of the query.  For example if a client searches for positions:</p>
 * <pre>
 *   /jax/positions?minquantity=5&maxquantity=10&identifier=</pre>
 * <p>an update would be sent if <em>any</em> position were updated, not only those with a size in the range 5-10.
 * This is a very conservative approach which will often lead to a client re-running a query and receiving the same
 * results. A complete solution to this problem would be much more complex and this behaviour is not a problem if the
 * queries are cheap.</p>
 * <p>To enable subscriptions for queries the REST method must be annotated with
 * {@link com.opengamma.web.analytics.rest.SubscribeMaster} and the type(s) of master specified as annotation
 * parameters.  See
 * {@link com.opengamma.web.portfolio.WebPortfoliosResource#getJSON WebPortfoliosResource.getJSON()}
 * for an example.</p>
 *
 * <h2>Querying the data needed for setting up views</h2>
 * <h3>Live Data Sources</h3>
 * <pre>
 *   /jax/livedatasources</pre>
 * <p>returns the names of the available sources of live data</p>
 * <pre>
 *   [dataSourceName1, dataSourceName2, ...]
 * </pre>
 * <h3>View Definitions</h3>
 * <pre>
 *   /jax/viewdefinitions</pre>
 * <p>returns a list of available view definitions</p>
 * <pre>
 *   [{id: viewDefId1, name: viewDefName1}, {id: viewDefId2, name: viewDefName2}, ...]
 * </pre>
 * <h3>Market Data Snapshots</h3>
 * <pre>
 *   /jax/marketdatasnapshots</pre>
 * <p>returns a list of available snapshots</p>
 * <pre>
 *   [{basisViewName: basisViewName1, snapshots: [{id: snapshot1Id, name: snapshot1Name}, {id: snapshot2Id, name: snapshot2Name}, ...]},
 *    {basisViewName: basisViewName2, snapshots: [{id: snapshot3Id, name: snapshot3Name}, {id: snapshot4Id, name: snapshot4Name}, ...]}, ...]
 * </pre>
 * <pre>
 *   /jax/marketdatasnapshots/{snapshotObjectId}</pre>
 * <p>returns the version history for a snapshot</p>
 * <pre>
 *   [{uniqueId: snapshot1Id,
 *     correctionFrom: snapshot1CorrectionFromTime,
 *     correctionTo: snapshot1CorrectionToTime,
 *     versionFrom: snapshot1VersionFromTime,
 *     versionTo: snapshot1VersionToTime}, ...]
 * </pre>
 * <h3>Aggregators</h3>
 * <pre>
 *   /jax/aggregators</pre>
 * <p>returns the available portfolio aggregators</p>
 * <pre>
 *   [aggregatorName1, aggregatorName2, ...]
 * </pre>
 * <h3>Time Series Resolver Keys</h3>
 * <pre>
 *   /jax/timeseriesresolverkeys</pre>
 * <p>returns a list of time series resolver keys (which are the names of configuration entries for
 * {@link com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating HistoricalTimeSeriesRating}s)</p>
 * <pre>
 *   [keyName1, keyName2, ...]
 * </pre>
 *
 * <h2>Analytics</h2>
 * <p>The structure of the REST URLs for analytics data reflects the hierarchical structure of views, grids and
 * viewports used to view the data.</p>
 * <pre>
 *   /jax/views                                                                      POST to create view
 *   /jax/views/{viewId}                                                             POST to pause and resume, DELETE to close
 *
 *   /jax/views/{viewId}/portfolio                                                   GET column and row structure, notification on structure change
 *   /jax/views/{viewId}/portfolio/viewports                                         POST to create viewport
 *   /jax/views/{viewId}/portfolio/viewports/{viewportId}                            GET data, PUT to update, DELETE to close, notification when new data is available
 *
 *   /jax/views/{viewId}/portfolio/depgraphs                                         POST to create dependency graph grid
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}                               GET column and row structure, DELETE to close, notification on structure change
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports                     POST to create viewport
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports/{viewportId}        GET data, PUT to update, DELETE to close, notification when new data is available
 *
 *   /jax/views/{viewId}/primitives                                                  GET column and row structure, notification on structure change
 *   /jax/views/{viewId}/primitives/viewports                                        POST to create viewport
 *   /jax/views/{viewId}/primitives/viewports/{viewportId}                           GET data, PUT to update, DELETE to close, notification when new data is available
 *
 *   /jax/views/{viewId}/primitives/depgraphs                                        POST to create dependency graph grid
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}                              GET column and row structure, DELETE to close, notification on structure change
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports                    POST to create viewport
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports/{viewportId}       GET data, PUT to update, DELETE to close, notification when new data is available
 * </pre>
 *
 * <h3>Views</h3>
 * <p>Before a client can receive data for a view it must create a view. This is done by making a {@code POST}
 * request to:</p>
 * <pre>
 *   /jax/views?clientId=...</pre>
 * <p>The request must contain the follow parameters to define the view:</p>
 * <ul>
 *   <li>{@code viewDefinitionId}: Unique ID of the view definition.</li>
 *   <li>{@code aggregators}: names of the aggregators used to aggregate the portfolio, omit for no aggregation.</li>
 *   <li>{@code valuationTime}: valuation time used by the calculation engine.</li>
 *   <li>{@code portfolioVersionTime}: the time of the portfolio version used in the calculations.</li>
 *   <li>{@code portfolioCorrectionTime}: the correction time of the portfolio version used in the calculations.</li>
 *   <li>{@code marketDataProviders}: JSON array spcifying the market data providers.
 *   See {@link com.opengamma.web.analytics.MarketDataSpecificationJsonReader MarketDataSpecificationJsonReader}
 *   for details.</li>
 * </ul>
 * <p>The response header will contain the location of the new view. To close a view the client should make a
 * {@code DELETE} request to the view's location.</p>
 *
 * <h3>Grids</h3>
 * <p>A view always has at least two grids, the portfolio and primitives grids. TODO depgraphs</p>
 *
 * <h3>Grid Structure</h3>
 * <p>To retrieve the row and column structure for each grid the client should make a {@code GET} request to:</p>
 * <pre>
 *   /jax/views/{viewId}/portfolio
 *   /jax/views/{viewId}/primitives
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}
 * </pre>
 * <p>TODO document the grid structure JSON</p>
 *
 * <h3>Viewports</h3>
 * <p>After creating a view the client must create a viewport in order to receive data. This is done by making a
 * {@code POST} request to:</p>
 * <pre>
 *   /jax/views/{viewId}/portfolio/viewports
 *   /jax/views/{viewId}/primitives/viewports
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports
 * </pre>
 * <p>The request must contain parameters specifying which cells the viewport should contain. These can be either:
 * <ul>
 *   <li>{@code rows} and {@code columns} containing the row and column indices in the viewport. This
 *   creates a rectangular viewport.</li>
 *   <li>{@code cells} containing the row and column index for each cell in the viewport as a comma-separated pair,
 *   e.g. '10,12'.</li>
 * </ul>
 * <p>The response header will contain the location of the new viewport. To close the viewport the client should make
 * a {@code DELETE} request to the viewport's location.</p>
 *
 * <h3>Viewport Data</h3>
 * <p>To retrieve data for the viewport the client must make a {@code GET} request to:</p>
 * <pre>
 *   /jax/views/{viewId}/portfolio/viewports/{viewportId}
 *   /jax/views/{viewId}/primitives/viewports/{viewportId}
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports/{viewportId}
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports/{viewportId}
 * </pre>
 * <p>The data is returned as an array of objects for each cell by row and then by column. The order of cell or
 * indices in the parameters used to create the viewport is ignored.</p>
 * <p>TODO document data JSON</p>
 *
 * <p>TODO async updates</p>
 */
package com.opengamma.web.analytics.rest;
