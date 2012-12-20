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
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.RemoteMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.RemotePortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.RemoteSecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.rest.FudgeRestClient;

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

    public URI getSecurityMaster() {
      return notImplemented("securityMaster");
    }

    public URI getConfigMaster() {
      return notImplemented("configMaster");
    }

    public URI getInterpolatedYieldCurveDefinitionMaster() {
      return notImplemented("interpolatedYieldCurveDefinitionMaster");
    }

    public URI getHeartbeat() {
      return notImplemented("heartbeat");
    }

    public URI getMarketDataSnapshotMaster() {
      return notImplemented("marketDataSnapshotMaster");
    }

    public URI getHistoricalTimeSeriesMaster() {
      return notImplemented("historicalTimeSeriesMaster");
    }

  }

  /**
   * Source of targets externally specified.
   */
  public static final class ExternalTargetProvider extends TargetProvider {

    private URI _portfolioMaster;
    private URI _positionMaster;
    private URI _securityMaster;
    private URI _configMaster;
    private URI _interpolatedYieldCurveDefinitionMaster;
    private URI _heartbeat;
    private URI _marketDataSnapshotMaster;
    private URI _historicalTimeSeriesMaster;

    public void setPortfolioMaster(final URI portfolioMaster) {
      _portfolioMaster = portfolioMaster;
    }

    @Override
    public URI getPortfolioMaster() {
      return (_portfolioMaster != null) ? _portfolioMaster : super.getPortfolioMaster();
    }

    public void setPositionMaster(final URI positionMaster) {
      _positionMaster = positionMaster;
    }

    @Override
    public URI getPositionMaster() {
      return (_positionMaster != null) ? _positionMaster : super.getPositionMaster();
    }

    public void setSecurityMaster(final URI securityMaster) {
      _securityMaster = securityMaster;
    }

    @Override
    public URI getSecurityMaster() {
      return (_securityMaster != null) ? _securityMaster : super.getSecurityMaster();
    }

    public void setConfigMaster(final URI configMaster) {
      _configMaster = configMaster;
    }

    @Override
    public URI getConfigMaster() {
      return (_configMaster != null) ? _configMaster : super.getConfigMaster();
    }

    public void setInterpolatedYieldCurveDefinitionMaster(final URI interpolatedYieldCurveDefinitionMaster) {
      _interpolatedYieldCurveDefinitionMaster = interpolatedYieldCurveDefinitionMaster;
    }

    @Override
    public URI getInterpolatedYieldCurveDefinitionMaster() {
      return (_interpolatedYieldCurveDefinitionMaster != null) ? _interpolatedYieldCurveDefinitionMaster : super.getInterpolatedYieldCurveDefinitionMaster();
    }

    public void setHeartbeat(final URI heartbeat) {
      _heartbeat = heartbeat;
    }

    @Override
    public URI getHeartbeat() {
      return (_heartbeat != null) ? _heartbeat : super.getHeartbeat();
    }

    public void setMarketDataSnapshotMaster(final URI marketDataSnapshotMaster) {
      _marketDataSnapshotMaster = marketDataSnapshotMaster;
    }

    @Override
    public URI getMarketDataSnapshotMaster() {
      return (_marketDataSnapshotMaster != null) ? _marketDataSnapshotMaster : super.getMarketDataSnapshotMaster();
    }

    public void setHistoricalTimeSeriesMaster(final URI historicalTimeSeriesMaster) {
      _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
    }

    @Override
    public URI getHistoricalTimeSeriesMaster() {
      return (_historicalTimeSeriesMaster != null) ? _historicalTimeSeriesMaster : super.getHistoricalTimeSeriesMaster();
    }
  }

  /**
   * Source of targets built from a single "base" URI.
   */
  public static final class BaseUriTargetProvider extends TargetProvider {
    private final URI _baseUri;
    private final String _userName;
    private final String _clientName;

    public BaseUriTargetProvider(final URI baseUri, final String userName, final String clientName) {
      ArgumentChecker.notNull(baseUri, "baseUri");
      _baseUri = baseUri;
      _userName = userName;
      _clientName = clientName;
    }

    @Override
    public URI getSecurityMaster() {
      return DataFinancialClientResource.uriSecurityMaster(_baseUri, _userName, _clientName);
    }

    @Override
    public URI getPositionMaster() {
      return DataFinancialClientResource.uriPositionMaster(_baseUri, _userName, _clientName);
    }

    @Override
    public URI getPortfolioMaster() {
      return DataFinancialClientResource.uriPortfolioMaster(_baseUri, _userName, _clientName);
    }

    @Override
    public URI getConfigMaster() {
      return DataFinancialClientResource.uriConfigMaster(_baseUri, _userName, _clientName);
    }

    @Override
    public URI getInterpolatedYieldCurveDefinitionMaster() {
      return DataFinancialClientResource.uriInterpolatedYieldCurveDefinitionMaster(_baseUri, _userName, _clientName);
    }

    @Override
    public URI getMarketDataSnapshotMaster() {
      return DataFinancialClientResource.uriSnapshotMaster(_baseUri, _userName, _clientName);
    }

    @Override
    public URI getHeartbeat() {
      return DataFinancialClientResource.uriHeartbeat(_baseUri, _userName, _clientName);
    }
  }

  private final String _clientId;
  private final TargetProvider _targetProvider;
  private volatile PortfolioMaster _portfolioMaster;
  private volatile PositionMaster _positionMaster;
  private volatile SecurityMaster _securityMaster;
  private volatile ConfigMaster _configMaster;
  private volatile InterpolatedYieldCurveDefinitionMaster _interpolatedYieldCurveDefinitionMaster;
  private volatile MarketDataSnapshotMaster _marketDataSnapshotMaster;
  private volatile HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  // TODO [PLAT-637] We're using the in memory job as a hack
  private static ConventionBundleMaster s_conventionBundleMaster;

  public RemoteClient(final String clientId, final FudgeContext fudgeContext, final TargetProvider uriProvider) {
    _clientId = clientId;
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
      _securityMaster = new RemoteSecurityMaster(_targetProvider.getSecurityMaster());
    }
    return _securityMaster;
  }

  public ConfigMaster getConfigMaster() {
    if (_configMaster == null) {
      _configMaster = new RemoteConfigMaster(_targetProvider.getConfigMaster());
    }
    return _configMaster;
  }

  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    if (_interpolatedYieldCurveDefinitionMaster == null) {
      _interpolatedYieldCurveDefinitionMaster = new RemoteInterpolatedYieldCurveDefinitionMaster(_targetProvider.getInterpolatedYieldCurveDefinitionMaster());
    }
    return _interpolatedYieldCurveDefinitionMaster;
  }

  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    if (_marketDataSnapshotMaster == null) {
      _marketDataSnapshotMaster = new RemoteMarketDataSnapshotMaster(_targetProvider.getMarketDataSnapshotMaster());
    }
    return _marketDataSnapshotMaster;
  }

  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    if (_historicalTimeSeriesMaster == null) {
      _historicalTimeSeriesMaster = new RemoteHistoricalTimeSeriesMaster(_targetProvider.getHistoricalTimeSeriesMaster());
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
    final FudgeRestClient client = FudgeRestClient.create();
    final URI uri = _targetProvider.getHeartbeat();
    return new Runnable() {
      @Override
      public void run() {
        client.accessFudge(uri).post();
      }
    };
  }

  //-------------------------------------------------------------------------
  /**
   * A hack to allow the Excel side to get hold of a RemoteClient without it having to be aware of the URI. Eventually
   * we will need a UserMaster to host users and their clients, and the entry point for Excel will be a
   * RemoteUserMaster.
   *
   * @param fudgeContext  the Fudge context, not null
   * @param baseUserManagerUri  base URI for the user manager, does not include "users/{user}", not null
   * @param userName  the username
   * @return  a {@link RemoteClient} instance for the new client
   */
  public static RemoteClient forNewClient(final FudgeContext fudgeContext, final URI baseUserManagerUri, final String userName) {
    final String clientName = GUIDGenerator.generate().toString();
    return forClient(fudgeContext, baseUserManagerUri, userName, clientName);
  }

  public static RemoteClient forClient(final FudgeContext fudgeContext, final URI baseUserManagerUri, final String userName, final String clientName) {
    return new RemoteClient(clientName, fudgeContext, new BaseUriTargetProvider(baseUserManagerUri, userName, clientName));
  }

}
