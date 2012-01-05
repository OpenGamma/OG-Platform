/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.net.URI;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.historicaltimeseries.rest.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.financial.marketdatasnapshot.rest.RemoteMarketDataSnapshotMaster;
import com.opengamma.financial.security.rest.RemoteSecurityMaster;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.rest.RemoteManageableViewDefinitionRepository;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.RemotePortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;

/**
 * Provides access to a remote representation of a "client". A client is defined as a set of coherent
 * and related masters. These might be the set of masters corresponding to the "shared" data used by
 * all users of an OpenGamma instance, or the masters containing data specific to a single user or
 * perhaps a group of users.
 */
public class RemoteClient {

  /**
   * Source of targets for the entities this client will connect to.
   */
  public abstract static class TargetProvider {

    protected <T> T notImplemented(final String what) {
      throw new UnsupportedOperationException("The '" + what + "' is not available for this client");
    }

    public URI getPortfolioMaster() {
      return notImplemented("portfolioMaster");
    }

    public URI getPositionMaster() {
      return notImplemented("positionMaster");
    }

    public RestTarget getSecurityMaster() {
      return notImplemented("securityMaster");
    }

    public URI getViewDefinitionRepository() {
      return notImplemented("viewDefinitionRepository");
    }

    public RestTarget getInterpolatedYieldCurveDefinitionMaster() {
      return notImplemented("interpolatedYieldCurveDefinitionMaster");
    }

    public RestTarget getHeartbeat() {
      return notImplemented("heartbeat");
    }

    public RestTarget getMarketDataSnapshotMaster() {
      return notImplemented("marketDataSnapshotMaster");
    }

    public RestTarget getHistoricalTimeSeriesMaster() {
      return notImplemented("historicalTimeSeriesMaster");
    }

  }

  /**
   * Source of targets externally specified.
   */
  public static final class ExternalTargetProvider extends TargetProvider {

    private URI _portfolioMaster;
    private URI _positionMaster;
    private RestTarget _securityMaster;
    private URI _viewDefinitionRepository;
    private RestTarget _interpolatedYieldCurveDefinitionMaster;
    private RestTarget _heartbeat;
    private RestTarget _marketDataSnapshotMaster;
    private RestTarget _historicalTimeSeriesMaster;

    public void setPortfolioMaster(final URI portfolioMaster) {
      // The remote portfolio master is broken and assumes a "portfolio" prefix on its URLs 
      _portfolioMaster = (portfolioMaster != null) ? portfolioMaster.resolve(".") : null;
    }

    @Override
    public URI getPortfolioMaster() {
      return (_portfolioMaster != null) ? _portfolioMaster : super.getPortfolioMaster();
    }

    public void setPositionMaster(final URI positionMaster) {
      // The remote position master is broken and assumes a "position" prefix on its URLs
      _positionMaster = (positionMaster != null) ? positionMaster.resolve(".") : null;
    }

    @Override
    public URI getPositionMaster() {
      return (_positionMaster != null) ? _positionMaster : super.getPositionMaster();
    }

    public void setSecurityMaster(final RestTarget securityMaster) {
      _securityMaster = securityMaster;
    }

    @Override
    public RestTarget getSecurityMaster() {
      return (_securityMaster != null) ? _securityMaster : super.getSecurityMaster();
    }

    public void setViewDefinitionRepositoryUri(final URI viewDefinitionRepository) {
      _viewDefinitionRepository = viewDefinitionRepository;
    }

    @Override
    public URI getViewDefinitionRepository() {
      return (_viewDefinitionRepository != null) ? _viewDefinitionRepository : super.getViewDefinitionRepository();
    }

    public void setInterpolatedYieldCurveDefinitionMaster(final RestTarget interpolatedYieldCurveDefinitionMaster) {
      _interpolatedYieldCurveDefinitionMaster = interpolatedYieldCurveDefinitionMaster;
    }

    @Override
    public RestTarget getInterpolatedYieldCurveDefinitionMaster() {
      return (_interpolatedYieldCurveDefinitionMaster != null) ? _interpolatedYieldCurveDefinitionMaster : super.getInterpolatedYieldCurveDefinitionMaster();
    }

    public void setHeartbeat(final RestTarget heartbeat) {
      _heartbeat = heartbeat;
    }

    @Override
    public RestTarget getHeartbeat() {
      return (_heartbeat != null) ? _heartbeat : super.getHeartbeat();
    }

    public void setMarketDataSnapshotMaster(final RestTarget marketDataSnapshotMaster) {
      _marketDataSnapshotMaster = marketDataSnapshotMaster;
    }

    @Override
    public RestTarget getMarketDataSnapshotMaster() {
      return (_marketDataSnapshotMaster != null) ? _marketDataSnapshotMaster : super.getMarketDataSnapshotMaster();
    }

    public void setHistoricalTimeSeriesMaster(final RestTarget historicalTimeSeriesMaster) {
      _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
    }

    @Override
    public RestTarget getHistoricalTimeSeriesMaster() {
      return (_historicalTimeSeriesMaster != null) ? _historicalTimeSeriesMaster : super.getHistoricalTimeSeriesMaster();
    }

  }

  /**
   * Source of targets built from a single "base" URI.
   */
  public static final class BaseUriTargetProvider extends TargetProvider {

    private final RestTarget _baseTarget;

    public BaseUriTargetProvider(final RestTarget baseTarget) {
      ArgumentChecker.notNull(baseTarget, "baseTarget");
      _baseTarget = baseTarget;
    }

    @Override
    public URI getPortfolioMaster() {
      // The remote portfolio master is broken and assumes a "prtMaster/portfolio" prefix on its URLs 
      return _baseTarget.getURI();
    }

    @Override
    public URI getPositionMaster() {
      // The remote position master is broken and assumes a "posMaster/position" prefix on its URLs
      return _baseTarget.getURI();
    }

    @Override
    public RestTarget getSecurityMaster() {
      return _baseTarget.resolveBase(ClientResource.SECURITIES_PATH);
    }

    @Override
    public URI getViewDefinitionRepository() {
      return _baseTarget.resolveBase(ClientResource.VIEW_DEFINITIONS_PATH).getURI();
    }

    @Override
    public RestTarget getInterpolatedYieldCurveDefinitionMaster() {
      return _baseTarget.resolveBase(ClientResource.INTERPOLATED_YIELD_CURVE_DEFINITIONS_PATH);
    }

    @Override
    public RestTarget getHeartbeat() {
      return _baseTarget.resolve(ClientResource.HEARTBEAT_PATH);
    }

    @Override
    public RestTarget getMarketDataSnapshotMaster() {
      return _baseTarget.resolveBase(ClientResource.MARKET_DATA_SNAPSHOTS_PATH);
    }

    // TODO: user timeseries?

  }

  private final String _clientId;
  private final FudgeContext _fudgeContext;
  private final TargetProvider _targetProvider;
  private volatile PortfolioMaster _portfolioMaster;
  private volatile PositionMaster _positionMaster;
  private volatile SecurityMaster _securityMaster;
  private volatile ManageableViewDefinitionRepository _viewDefinitionRepository;
  private volatile InterpolatedYieldCurveDefinitionMaster _interpolatedYieldCurveDefinitionMaster;
  private volatile MarketDataSnapshotMaster _marketDataSnapshotMaster;
  private volatile HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  // TODO [PLAT-637] We're using the in memory job as a hack
  private static ConventionBundleMaster s_conventionBundleMaster;

  public RemoteClient(String clientId, FudgeContext fudgeContext, TargetProvider uriProvider) {
    _clientId = clientId;
    _fudgeContext = fudgeContext;
    _targetProvider = uriProvider;
  }

  public String getClientId() {
    return _clientId;
  }

  public PortfolioMaster getPortfolioMaster() {
    if (_portfolioMaster == null) {
      _portfolioMaster = new RemotePortfolioMaster(_targetProvider.getPortfolioMaster());
    }
    return _portfolioMaster;
  }

  public PositionMaster getPositionMaster() {
    if (_positionMaster == null) {
      _positionMaster = new RemotePositionMaster(_targetProvider.getPositionMaster());
    }
    return _positionMaster;
  }

  public SecurityMaster getSecurityMaster() {
    if (_securityMaster == null) {
      _securityMaster = new RemoteSecurityMaster(_fudgeContext, _targetProvider.getSecurityMaster());
    }
    return _securityMaster;
  }

  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    if (_viewDefinitionRepository == null) {
      _viewDefinitionRepository = new RemoteManageableViewDefinitionRepository(_targetProvider.getViewDefinitionRepository());
    }
    return _viewDefinitionRepository;
  }

  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    if (_interpolatedYieldCurveDefinitionMaster == null) {
      _interpolatedYieldCurveDefinitionMaster = new RemoteInterpolatedYieldCurveDefinitionMaster(_fudgeContext, _targetProvider.getInterpolatedYieldCurveDefinitionMaster());
    }
    return _interpolatedYieldCurveDefinitionMaster;
  }

  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    if (_marketDataSnapshotMaster == null) {
      _marketDataSnapshotMaster = new RemoteMarketDataSnapshotMaster(_fudgeContext, _targetProvider.getMarketDataSnapshotMaster());
    }
    return _marketDataSnapshotMaster;
  }

  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    if (_historicalTimeSeriesMaster == null) {
      _historicalTimeSeriesMaster = new RemoteHistoricalTimeSeriesMaster(_fudgeContext, _targetProvider.getHistoricalTimeSeriesMaster());
    }
    return _historicalTimeSeriesMaster;
  }

  public ConventionBundleMaster getConventionBundleMaster() {
    // TODO [PLAT-637] We're using the in memory job as a hack
    synchronized (RemoteClient.class) {
      if (s_conventionBundleMaster == null) {
        s_conventionBundleMaster = new InMemoryConventionBundleMaster();
      }
      return s_conventionBundleMaster;
    }
  }

  /**
   * Creates a heartbeat sender. If nothing has happened for a timeout duration, that would result in messages being sent to the server,
   * the heartbeat signal should be sent as a keep-alive.
   * 
   * @return a runnable sender. Each invocation of {@link Runnable#run} will send a heartbeat signal
   */
  public Runnable createHeartbeatSender() {
    final RestClient client = RestClient.getInstance(_fudgeContext, null);
    final RestTarget target = _targetProvider.getHeartbeat();
    return new Runnable() {
      @Override
      public void run() {
        client.post(target);
      }
    };
  }

  /**
   * A hack to allow the Excel side to get hold of a RemoteClient without it having to be aware of the URI. Eventually
   * we will need a UserMaster to host users and their clients, and the entry point for Excel will be a
   * RemoteUserMaster.
   *
   * @param fudgeContext  the Fudge context
   * @param usersUri  uri as far as /users
   * @param username  the username
   * @return  a {@link RemoteClient} instance for the new client
   */
  public static RemoteClient forNewClient(FudgeContext fudgeContext, RestTarget usersUri, String username) {
    return forClient(fudgeContext, usersUri, username, GUIDGenerator.generate().toString());
  }

  public static RemoteClient forClient(FudgeContext fudgeContext, RestTarget usersUri, String username, String clientId) {
    RestTarget uri = usersUri.resolveBase(username).resolveBase("clients").resolveBase(clientId);
    return new RemoteClient(clientId, fudgeContext, new BaseUriTargetProvider(uri));
  }

}
