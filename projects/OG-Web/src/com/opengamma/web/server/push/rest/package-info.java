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
 * with {@link com.opengamma.web.server.push.rest.Subscribe}.  The annotation must be on a string parameter that can be parsed by
 * {@link com.opengamma.id.UniqueId#parse(String)} and the parameter must also have a {@link javax.ws.rs.PathParam} annotation.  See
 * {@link com.opengamma.web.portfolio.WebPortfoliosResource#findPortfolio(String)} for an example.</p>
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
 * <p>To enable subscriptions for queries the REST method must be annotated with {@link com.opengamma.web.server.push.rest.SubscribeMaster} and
 * the type(s) of master specified as annotation parameters.  See
 * {@link com.opengamma.web.portfolio.WebPortfoliosResource#getJSON(Integer, Integer, Integer, String, String, java.util.List, java.util.List, Boolean)}
 * for an example.</p>
 *
 * <h2>Querying Available View Definitions, Market Data Snapshots and Aggregators</h2>
 * <p>The view definitions, market data snapshots and aggregators available in the system can be queried as follows:</p>
 * <pre>
 *   /jax/viewdefinitions</pre>
 * <p>returns</p>
 * <pre>
 *   [{id: viewDefId1, name: viewDefName1}, {id: viewDefId2, name: viewDefName2}, ...]
 * </pre>
 * <p>and</p>
 * <pre>
 *   /jax/marketdatasnapshots</pre>
 * <p>returns</p>
 * <pre>
 *   [{basisViewName: basisViewName1, snapshots: [{id: snapshot1Id, name: snapshot1Name}, {id: snapshot2Id, name: snapshot2Name}, ...]},
 *    {basisViewName: basisViewName2, snapshots: [{id: snapshot3Id, name: snapshot3Name}, {id: snapshot4Id, name: snapshot4Name}, ...]}, ...]
 * </pre>
 * <p>and</p>
 * <pre>
 *   /jax/aggregators</pre>
 * <p>returns</p>
 * <pre>
 *   [aggregatorName1, aggregatorName2, ...]
 * </pre>
 *
 * <h2>Analytics</h2>
 * <p>The structure of the REST URLs for analytics data reflects the hierarchical structure of views, grids and
 * viewports used to view the data.</p>
 * <pre>
 *   /jax/views                                                                      POST to create view
 *   /jax/views/{viewId}                                                             POST to pause and resume, DELETE to close
 *
 *   /jax/views/{viewId}/portfolio/grid                                              GET column and row structure, notification on structure change
 *   /jax/views/{viewId}/portfolio/columns                                           GET column structure, notification on structure change
 *   /jax/views/{viewId}/portfolio/viewports                                         POST to create viewport
 *   /jax/views/{viewId}/portfolio/viewports/{viewportId}                            POST to update, DELETE to close
 *   /jax/views/{viewId}/portfolio/viewports/{viewportId}/data                       GET data, notification when new data is available
 *
 *   /jax/views/{viewId}/portfolio/depgraphs                                         POST to create dependency graph grid
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}                               DELETE to close
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/grid                          GET column and row structure, notification on structure change
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/columns                       GET column structure, notification on structure change
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports                     POST to create viewport
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports/{viewportId}        POST to update, DELETE to close
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports/{viewportId}/data   GET data, notification when new data is available
 *
 *   /jax/views/{viewId}/primitives/grid                                             GET column and row structure, notification on structure change
 *   /jax/views/{viewId}/primitives/columns                                          GET column structure, notification on structure change
 *   /jax/views/{viewId}/primitives/viewports                                        POST to create viewport
 *   /jax/views/{viewId}/primitives/viewports/{viewportId}                           POST to update, DELETE to close
 *   /jax/views/{viewId}/primitives/viewports/{viewportId}/data                      GET data, notification when new data is available
 *
 *   /jax/views/{viewId}/primitives/depgraphs                                        POST to create dependency graph grid
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}                              DELETE to close
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/grid                         GET column and row structure, notification on structure change
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/columns                      GET column structure, notification on structure change
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports                    POST to create viewport
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports/{viewportId}       POST to update, DELETE to close
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports/{viewportId}/data  GET data, notification when new data is available
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
 *   <li>{@code live}: {@code true} or {@code false} - whether to use live market data or a snapshot.</li>
 *   <li>{@code provider}: name of the market data provider.  Only required for live data. <em>TODO use the value "Live market data (Bloomberg, Activ)" for testing</em>.</li>
 *   <li>{@code snapshotId}: ID of the market data snapshot.  Only required if using a market data snapshot.</li>
 *   <li>{@code versionDateTime}: time of the snapshot.  Only required if using a market data snapshot.</li>
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
 *   /jax/views/{viewId}/portfolio/grid
 *   /jax/views/{viewId}/primitives/grid
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/grid
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/grid
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
 * <p>The request must contain parameters called {@code rows} and {@code columns} with a list of the row and
 * column indices that define the viewport.</p>
 * <p>The response header will contain the location of the new viewport. To close the viewport the client should make
 * a {@code DELETE} request to the view's location.</p>
 *
 * <h3>Viewport Data</h3>
 * <p>To retrieve data for the viewport the client must make a {@code GET} request to:</p>
 * <pre>
 *   /jax/views/{viewId}/portfolio/viewports/{viewportId}/data
 *   /jax/views/{viewId}/primitives/viewports/{viewportId}/data
 *   /jax/views/{viewId}/portfolio/depgraphs/{graphId}/viewports/{viewportId}/data
 *   /jax/views/{viewId}/primitives/depgraphs/{graphId}/viewports/{viewportId}/data
 * </pre>
 * <p>TODO document data JSON</p>
 *
 * <p>TODO async updates</p>
 */
package com.opengamma.web.server.push.rest;
