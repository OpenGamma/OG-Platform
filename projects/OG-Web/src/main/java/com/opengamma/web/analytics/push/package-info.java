/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * <p>This package contains push notifications (no longer used).</p>
 * <em>THIS IS OUT OF DATE. SEE {@link com.opengamma.web.analytics.rest}</em>
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
 * with {@link com.opengamma.web.analytics.rest.Subscribe}.  The annotation must be on a string parameter that can be parsed by
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
 * <p>To enable subscriptions for queries the REST method must be annotated with {@link com.opengamma.web.analytics.rest.SubscribeMaster} and
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
 ** <h2>Views</h2>
 * <p>Before a client can receive data for a view it must create a viewport.  This is done by making a {@code POST}
 * request to:</p>
 * <pre>
 *   /jax/viewports?clientId=...</pre>
 * <p>The response will contain JSON with the URL for the newly-created viewport, e.g.</p>
 * <pre>
 *   {"viewportUrl": "/jax/viewports/567"}</pre>
 *
 * <p>The request must contain JSON which defines the viewport:</p>
 * <pre>
 *   {"viewDefinitionName": ...
 *    "marketDataType": ...
 *    "marketDataProvider": ...
 *    "snapshotId": ...
 *    "aggregatorName": ...
 *    "portfolioViewport":
 *     {"rowIds": [rowId1, rowId2, ...],
 *      "lastTimestamps": [timestamp1, timestamp2, ...],
 *      "dependencyGraphCells": [[row, col], [row, col], ...]
 *      "fullConversionModeCells": [[row, col], [row, col], ...]},
 *    "primitiveViewport":
 *     {"rowIds": [rowId1, rowId2, ...],
 *      "lastTimestamps": [timestamp1, timestamp2, ...],
 *      "dependencyGraphCells": [[row, col], [row, col], ...],
 *      "fullConversionModeCells": [[row, col], [row, col], ...]}</pre>
 * <ul>
 *   <li>{@code viewDefinitionName}: name of the view definition (see below)</li>
 *   <li>{@code marketDataType}: {@code "live"} or {@code "snapshot"}</li>
 *   <li>{@code marketDataProvider}: name of the market data provider.  Only relevant for live data.  Omit or {@code "Automatic"} for default provider</li>
 *   <li>{@code snapshotId}: ID of the market data snapshot (see below).  Required if using a market data snapshot.  <em>TODO No testing has
 *   been done using snapshots yet, only live data</em></li>
 *   <li>{@code portfolioViewport / primitiveViewport}: viewport definition for the separate grids showing portfolio
 *   and primitive data</li>
 *   <li>{@code rowIds}: The zero-based row indices whose data should be included in the results.</li>
 *   <li>{@code lastTimestamps}: The timestamp of the last update the client received for each row.  Each item
 *   in {@code lastTimestamps} refers to the row at the same index in {@code rowIds}.  Therefore {@code rowIds} and
 *   {@code lastTimestamps} must be the same length.  If no previous result has been received for the row then
 *   null should be sent.</li>
 *   <li>{@code dependencyGraphCells}: array of two-element arrays with the row and column numbers of cells whose
 *   dependency graph should be included in the results.</li>
 *   <li>{@code fullConversionModeCells}: array of two-elements arrays with the row and column numbers of cells
 *   whose full data should be sent to the client.  This is for cells that contain multi-valued data (e.g.
 *   yield curves) where the user can open a pop-up to view the full data.</li>
 * </ul>
 *
 * <h1>Receiving Notifications of Updates</h1>
 * <p>A client receives updates by making a request to the update URL for its client ID.</p>
 * <pre>
 *   /updates/{clientId}</pre>
 * <p>This request blocks until one of the following:</p>
 *
 * <h2>The request times out.</h2>
 * <p>In this case the response body will be empty.</p>
 *
 * <h2>An item for which the client has a subscription is updated.</h2>
 * <p>The response contains JSON with the REST URLs of any updated items, e.g.</p>
 * <pre>
 *   {"updates": ["/jax/portfolios", "/jax/positions/DbPos~2345"]}</pre>
 * <p>Notifications for a client are queued if the client doesn't have a connection open to the update URL when
 * the notification arrives.  When the client re-establishes the connection the notifications will be delivered
 * immediately.</p>
 *
 * <h2>Notifications Generated by Entities and Queries</h2>
 * <p>The URL used to make the original REST request is published when an entity or query changes (see example
 * above).</p>
 *
 * <h2>Notifications Generated by Viewports</h2>
 * <p>Viewports generate notifications when the grid structure changes and when the data changes.  The URLs are:</p>
 * <pre>
 *   /jax/viewports/{viewportId}/grid
 *   /jax/viewports/{viewportId}/data</pre>
 * <p>The format of the JSON returned from these URLs is unchanged from the Cometd implementation.  <em>TODO is
 * that documented anywhere?</em></p>
 *
 * <h1>Cancelling Subscriptions</h1>
 * <p>There is no way to unsubscribe for notifications for a particular URL.  When any notification is published
 * for a URL all subscriptions for that URL are cancelled.  When and if the client makes another request to that
 * URL a new subscription will be set up.</p>
 * <p><em>Is it feasible to not have an unsubscribe mechanism?  A long running
 * client could create a lot of subscriptions that wouldn't be cleared unless the underlying object were updated.
 * Subscriptions are fairly lightweight so this might not be a massive problem.</em></p>
 *
 * <h1>TODO</h1>
 * <p>The following still needs to be done:</p>
 * <ul>
 *   <li>Creating a viewport without a client ID is supported at the moment but there is no mechanism to
 *   clean up views created in this way.  So they are a massive resource leak.  An explicit closing mechanism
 *   and a timeout mechanism are required.</li>
 *   <li>There is no validation that a client ID belongs to the user who is requesting it.  This should work once
 *   we have user logins.</li>
 *   <li>There is no way to close a client connection apart from waiting for it to time out.  There is support
 *   in the back end but no RESTful interface.</li>
 *   <li>Query subscriptions are created when the user just visits the query page, e.g. /jax/portfolios.  This
 *   probably isn't what we want, a subscription should probably be created if there are some query params
 *   specifying what to search for.  it might even be necessary to allow the relevant query params to be
 *   specified in the annotation</li>
 *   <li>Should subscriptions be created from the POST methods that create entities?  A new filter would be needed
 *   that looked at the redirecting response to find the URL of the new entity.</li>
 * </ul>
 */
package com.opengamma.web.analytics.push;
