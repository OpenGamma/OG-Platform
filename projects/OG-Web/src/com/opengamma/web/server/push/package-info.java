package com.opengamma.web.server.push;

/**
 * TODO
 * view definition names resource
 * snapshot list resource
 * viewport
 *   create - only live data ATM
 *   grid
 *   data
 * entity subs - annotations
 * manager subs - annotations
 *
 * <p>This package provides push notifications of changes to resources requested through the REST interface.
 * This includes analytics data, entities and searches for entities.</p>
 *
 * <h1>Establishing a Connection</h1>
 * <p>Before a client can receive notifications of updates it must establish a connection and be assigned a
 * client ID.  A connection corresponds to a single view, e.g. a single browser tab.  A user can have multiple
 * connections active at the same time.  A connection is set up with a request to the handshake URL:</p>
 * <pre>
 *   /jax/handshake</pre>
 * <p>The response contains JSON with the client ID, e.g.</p>
 * <pre>
 *   {"clientId": 1234}</pre>
 *
 * <h1>Subscribing for Notifications of Updates</h1>
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
 * with {@link Subscribe}.  The annotation must be on a string parameter that can be parsed by
 * {@link UniqueId#parse(String)} and the parameter must also have a {@link PathParam} annotation.  See
 * {@link WebPortfoliosResource#findPortfolio(String)} for an example.</p>
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
 * <p>To enable subscriptions for queries the REST method must be annotated with {@link SubscribeMaster} and
 * the type(s) of master specified as annotation parameters.  See
 * {@link WebPortfoliosResource#getJSON(Integer, Integer, Integer, String, String, List, List)} for an example.</p>
 *
 * <h2>Views</h2>
 * <p>Before a client can receive data for a view it must create a viewport.  This is done by making a {@code POST}
 * request to:</p>
 * <pre>
 *   /jax/viewports?clientId=...</pre>
 * <p>The response body will contain JSON with the URL for the newly-created viewport, e.g.</p>
 * <pre>{"viewportUrl": "/jax/viewports/567"}</pre>
 *
 * <p>The request body must contain JSON which defines the viewport:</p>
 * <pre>
 *   {"viewDefinitionName": ...
 *    "snapshotId": ...
 *    "portfolioViewport":
 *     {"rows": [[row, timestamp], [row, timestamp], ...],
 *      "dependencyGraphCells": [[row, col], [row, col], ...]},
 *    "primitiveViewport":
 *     {"rows": [[row, timestamp], [row, timestamp], ...],
 *      "dependencyGraphCells": [[row, col], [row, col], ...]}</pre>
 * <ul>
 *   <li>{@code viewDefinitionName}: name of the view definition (see below)</li>
 *   <li>{@code snapshotId}: ID of the market data snapshot (see below).  Omit for live data</li>
 *   <li>{@code portfolioViewport / primitiveViewport}: viewport definition for the separate grids showing portfolio
 *   and primitive data</li>
 *   <li>{@code rows}: The row numbers whose data should be included in the results and the timestamp of the
 *   last time the client received an update for the row</li>
 *   <li>{@code dependencyGraphCells}: array of two-element arrays with the row and column numbers of cells whose
 *   dependency graph should be included in the results.  <em>N.B. This isn't working yet.</em></li>
 * </ul>
 *
 * <h3>Querying Available View Definitions and Market Data Snapshots</h3>
 * <p>The view definitions and market data snapshots available in the system can be queried as follows:</p>
 * <pre>
 *   /jax/viewdefinitionnames</pre>
 * <p>returns</p>
 * <pre>
 *   {"viewDefinitionNames": [viewDef1, viewDef2, ...]}
 * </pre>
 * <pre>
 *   /jax/marketdatasnapshots</pre>
 * <p>returns</p>
 * <pre>
 *   {basisViewName1: {snapshotUniqueId1: snapshotName1, snapshotUniqueId2: snapshotName2, ...},
 *    basisViewName2: {snapshotUniqueId3: snapshotName3, snapshotUniqueId4: snapshotName4, ...}}
 * </pre>
 * TODO subscriptions are cancelled after one update - object is dirty and must be re-requested, no point telling the user it is still dirty
 * TODO mention the (current) lack of a way to unsubscribe
 * TODO analytics data is the only thing which requires a client ID. will this always be true?
 * TODO security - once user logon info is available will be used to cross-check client ID
 * TODO only tested with live data (in the email, not the docs?)
 * TODO dep graph viewing not implemented yet
 *
 * <h1>Receiving Notifications of Updates</h1>
 * A client receives updates by making a request to the updates URL for its client ID.
 * <pre>
 *   /updates/{clientId}</pre>
 * <p>This request blocks until one of the following:</p>
 * <h2>The request times out.</h2>
 * <p>In this case the response body will be empty.</p>
 * <h2>An item for which the client has a subscription is updated.</h2>
 * <p>The response contains JSON with the REST URLs of any updated items:</p>
 * <pre>
 *   {"updates": ["/jax/portfolios", "/jax/positions/DbPrt~2345"]}</pre>
 */

import com.opengamma.id.UniqueId;
import com.opengamma.web.portfolio.WebPortfoliosResource;

import javax.ws.rs.PathParam;
import java.util.List;