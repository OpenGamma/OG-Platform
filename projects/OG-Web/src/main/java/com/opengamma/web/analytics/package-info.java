/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * <p>The classes in this package implement the back end for the analytics user interface. This displays analytics data
 * calculated for a view definition and portfolio. There UI for a view always has two main grids. The portfolio
 * grid shows analytics data calculated for positions and nodes in a portfolio. The primitives grid shows other
 * calculated data that isn't tied to the portfolio structure, e.g. curves, surfaces etc. These grids can each
 * have an arbitrary number of dependency graph grids. These show the hierarchy of calculations that produce the
 * value in a cell. Each grid (top level and dependency graph) can have an arbitrary number of viewports which
 * represent the part of the grid that's currently visible.</p>
 * <h2>Notes on concurrency</h2>
 * <p>The server-side state for each view is a complex hierarchy of grids, dependency graphs, viewports and data. This
 * state can be mutated by the client (opening dependency graphs, updating viewports) and the server
 * (changing the grid structure and calculating results). It would be a non-trivial task to make
 * this structure safe for concurrent access by multiple threads. It's also unclear whether this would be necessary
 * or the benefits would be worth it. At the very least it would be a premature optimiztion.</p>
 * <p>Therefore the threading model is very simple. The shared classes at the top level are thread safe
 * ({@link AnalyticsViewManager}, {@link AnalyticsViewClientConnection}). But {@link SimpleAnalyticsView} and
 * everything below it in the hierarchy are not. Threads that mutate the state must have exclusive access,
 * multiple reader threads can access the structure concurrently. {@link LockingAnalyticsView} is an
 * {@link AnalyticsView} implementation that decorates {@link SimpleAnalyticsView} and enforces this model.</p>
 */
package com.opengamma.web.analytics;
