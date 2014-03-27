/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.impl.NonVersionedRedisSecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

/*
 * REDIS DATA STRUCTURES:
 * Portfolio Names:
 *     Key["PORTFOLIOS"] -> Hash
 *        Hash[Name] -> UniqueId for the portfolio
 * Portfolio Unique ID Lookups:
 *     Key["NAME-"Name] -> Hash
 *        Hash[UNIQUE_ID] -> UniqueId for the portfolio
 * Portfolio objects themselves:
 *     Key["PRT-"UniqueId] -> Hash
 *        Hash[NAME] -> Name
 *        HASH["ATT-"AttributeName] -> Attribute Value
 * Portfolio contents:
 *     Key["PRTPOS-"UniqueId] -> Set
 *        Each item in the list is a UniqueId for a position
 * Positions:
 *     Key["POS-"UniqueId] -> Hash
 *        Hash[QTY] -> Quantity
 *        Hash[SEC] -> ExternalId for the security
 *        Hash["ATT-"AttributeName] -> Attribute Value
 * Position contents:
 *     Key["POSTRADES-"UniqueId] -> Set
 *        Each item in the list is a UniqueId for a trade
 * Trades:
 *        Key["TRADE-"UniqueId] -> Hash
 *        Hash[QTY] -> Quantity
 *        Hash[SEC] -> ExternalId for the security
 *        Hash["ATT-"AttributeName] -> Attribute Value
 */

/**
 * A lightweight {@link PositionSource} that cannot handle any versioning, and
 * which stores all positions and portfolios as Redis-native data structures
 * (rather than Fudge encoding).
 */
public class NonVersionedRedisPositionSource implements PositionSource {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisSecuritySource.class);
  
  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "RedisPos";
  /**
   * The default scheme for trade unique identifiers.
   */
  public static final String TRADE_IDENTIFIER_SCHEME_DEFAULT = "RedisTrade";

  private static final String PORTFOLIOS_HASH_KEY_NAME = "PORTFOLIOS";
  
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private final String _portfoliosHashKeyName;
  private Timer _getPortfolioTimer = new Timer();
  private Timer _getPositionTimer = new Timer();
  private Timer _portfolioStoreTimer = new Timer();
  private Timer _positionStoreTimer = new Timer();
  private Timer _positionSetTimer = new Timer();
  private Timer _positionAddTimer = new Timer();
  
  public NonVersionedRedisPositionSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public NonVersionedRedisPositionSource(JedisPool jedisPool, String redisPrefix) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix.intern();
    _portfoliosHashKeyName = constructallPortfoliosRedisKey();
    registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "NonVersionedRedisPositionSource");
  }
  
  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  public JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  public String getRedisPrefix() {
    return _redisPrefix;
  }

  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getPortfolioTimer = summaryRegistry.timer(namePrefix + ".getPortfolio");
    _getPositionTimer = summaryRegistry.timer(namePrefix + ".getPosition");
    _portfolioStoreTimer = summaryRegistry.timer(namePrefix + ".portfolioStore");
    _positionStoreTimer = summaryRegistry.timer(namePrefix + ".positionStore");
    _positionSetTimer = summaryRegistry.timer(namePrefix + ".positionSet");
    _positionAddTimer = summaryRegistry.timer(namePrefix + ".positionAdd");
  }
  
  protected static UniqueId generateUniqueId() {
    return UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
  }

  protected static UniqueId generateTradeUniqueId() {
    return UniqueId.of(TRADE_IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
  }

  // ---------------------------------------------------------------------------------------
  // REDIS KEY MANAGEMENT
  // ---------------------------------------------------------------------------------------
  
  protected final String toRedisKey(String id, String intermediate) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append(intermediate);
    sb.append(id);
    String keyText = sb.toString();
    return keyText;
  }
  
  protected final String toRedisKey(UniqueId uniqueId, String intermediate) {
    return toRedisKey(uniqueId.toString(), intermediate);
  }
  
  protected final String toPortfolioRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, "PRT-");
  }
  
  protected final String toPortfolioPositionsRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, "PRTPOS-");
  }
  
  protected final String toPositionRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, "POS-");
  }

  protected final String toTradeRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, "TRADE-");
  }

  protected final String toPositionTradesRedisKey(UniqueId uniqueId) {
    return toRedisKey(uniqueId, "POSTRADE-");
  }

  protected final String constructallPortfoliosRedisKey() {
    return toRedisKey(PORTFOLIOS_HASH_KEY_NAME, "");
  }
  
  protected final String toPortfolioNameRedisKey(String portfolioName) {
    return toRedisKey(portfolioName, "NAME-");
  }
  
  // ---------------------------------------------------------------------------------------
  // DATA MANIPULATION
  // ---------------------------------------------------------------------------------------
  
  /**
   * Deep store an entire portfolio, including all positions.
   * The portfolio itself is not modified, including setting the unique ID.
   * 
   * @param portfolio The portfolio to store.
   * @return the UniqueId of the portfolio.
   */
  public UniqueId storePortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    UniqueId uniqueId = null;
    
    try (Timer.Context context = _portfolioStoreTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        uniqueId = storePortfolio(jedis, portfolio);
        storePortfolioNodes(jedis, toPortfolioPositionsRedisKey(uniqueId), portfolio.getRootNode());
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store portfolio " + portfolio, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store portfolio " + portfolio, e);
      }
      
    }
    
    return uniqueId;
  }
  
  public UniqueId storePosition(Position position) {
    ArgumentChecker.notNull(position, "position");
    UniqueId uniqueId = null;
    
    try (Timer.Context context = _positionStoreTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        uniqueId = storePosition(jedis, position);
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }
      
    }
    
    return uniqueId;
  }
  
  /**
   * A special fast-pass method to just update a position quantity, without
   * updating any of the other fields. Results in a single Redis write.
   * 
   * @param position The position, which must already be in the source.
   */
  public void updatePositionQuantity(Position position) {
    ArgumentChecker.notNull(position, "position");
    
    try (Timer.Context context = _positionSetTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        String redisKey = toPositionRedisKey(position.getUniqueId());
        jedis.hset(redisKey, "QTY", position.getQuantity().toPlainString());
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }
      
    }
  }

  /**
   * Store a new position and attach it to the specified portfolio.
   * @param portfolio the existing portfolio. Must already be in this source.
   * @param position the new position to store and attach.
   * @return map of id to position, not-null.
   */
  public Map<String, Position> addPositionToPortfolio(Portfolio portfolio, Position position) {
    return addPositionsToPortfolio(portfolio, Collections.singleton(position));
  }
  
  /**
   * Store a new set of positions and attach it to the specified portfolio.
   * @param portfolio the existing portfolio. Must already be in this source.
   * @param positions the new positions to store and attach.
   * @return map of id to position, not-null.
   */
  public Map<String, Position> addPositionsToPortfolio(Portfolio portfolio, Collection<Position> positions) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(portfolio.getUniqueId(), "portfolio UniqueId");
    ArgumentChecker.notNull(positions, "position");
    
    Map<String, Position> id2position = Maps.newLinkedHashMap();
    try (Timer.Context context = _positionAddTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        String[] uniqueIdStrings = new String[positions.size()];
        int i = 0;
        for (Position position : positions) {
          String uniqueId = storePosition(jedis, position).toString();
          uniqueIdStrings[i] = uniqueId;
          i++;
          id2position.put(uniqueId, position);
        }
        UniqueId portfolioUniqueId = portfolio.getUniqueId();
        String portfolioPositionsKey = toPortfolioPositionsRedisKey(portfolioUniqueId);
        // NOTE kirk 2013-06-18 -- The following call is a known performance bottleneck.
        // I spent a full day attempting almost every single way I could imagine to
        // figure out what was going on, before I gave up for the time being.
        // When we're running in a far more realistic way we need to second guess
        // it, but it is a known performance issue on large portfolio loading.
        jedis.sadd(portfolioPositionsKey, uniqueIdStrings);
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store positions " + positions, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store positions " + positions, e);
      }
    }
    return id2position;
    
  }
  
  protected UniqueId storePortfolio(Jedis jedis, Portfolio portfolio) {
    UniqueId uniqueId = portfolio.getUniqueId();
    if (uniqueId == null) {
      uniqueId = generateUniqueId();
    }
    
    String uniqueIdKey = toPortfolioRedisKey(uniqueId);
    String portfolioNameKey = toPortfolioNameRedisKey(portfolio.getName());
    jedis.hset(portfolioNameKey, "UNIQUE_ID", uniqueId.toString());
    
    jedis.hset(_portfoliosHashKeyName, portfolio.getName(), uniqueId.toString());
    
    jedis.hset(uniqueIdKey, "NAME", portfolio.getName());
    
    for (Map.Entry<String, String> attribute : portfolio.getAttributes().entrySet()) {
      jedis.hset(uniqueIdKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }
    
    return uniqueId;
  }
  
  protected void storePortfolioNodes(Jedis jedis, String redisKey, PortfolioNode node) {
    Set<String> positionUniqueIds = new HashSet<String>();
    for (Position position : node.getPositions()) {
      UniqueId uniqueId = storePosition(jedis, position);
      positionUniqueIds.add(uniqueId.toString());
    }
    if (!positionUniqueIds.isEmpty()) {
      jedis.sadd(redisKey, positionUniqueIds.toArray(new String[0]));
    }
    
    if (!node.getChildNodes().isEmpty()) {
      s_logger.warn("Possible misuse. Portfolio has a deep structure, but this source flattens. Positions being stored flat.");
    }
    for (PortfolioNode childNode : node.getChildNodes()) {
      storePortfolioNodes(jedis, redisKey, childNode);
    }
  }
  
  protected UniqueId storePosition(Jedis jedis, Position position) {
    UniqueId uniqueId = position.getUniqueId();
    if (uniqueId == null) {
      uniqueId = generateUniqueId();
    }
    
    String redisKey = toPositionRedisKey(uniqueId);
    jedis.hset(redisKey, "QTY", position.getQuantity().toPlainString());
    ExternalIdBundle securityBundle = position.getSecurityLink().getExternalId();
    if (securityBundle == null) {
      throw new OpenGammaRuntimeException("Can only store positions with a link to an ExternalId");
    }
    if (securityBundle.size() != 1) {
      s_logger.warn("Bundle {} not exactly one. Possible misuse of this source.", securityBundle);
    }
    ExternalId securityId = securityBundle.iterator().next();
    jedis.hset(redisKey, "SEC", securityId.toString());
    
    for (Map.Entry<String, String> attribute : position.getAttributes().entrySet()) {
      jedis.hset(redisKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }

    if (position.getTrades() != null) {
      Set<String> tradeUniqueIds = new HashSet<>();
      for (Trade trade : position.getTrades()) {
        UniqueId tradeId = storeTrade(jedis, trade);
        tradeUniqueIds.add(tradeId.toString());
      }
      jedis.sadd(toPositionTradesRedisKey(uniqueId), tradeUniqueIds.toArray(new String[tradeUniqueIds.size()]));
    }

    return uniqueId;
  }

  protected UniqueId storeTrade(Jedis jedis, Trade trade) {
    UniqueId uniqueId = trade.getUniqueId();
    if (uniqueId == null) {
      uniqueId = generateTradeUniqueId();
    }

    String redisKey = toTradeRedisKey(uniqueId);
    jedis.hset(redisKey, "QTY", trade.getQuantity().toPlainString());
    ExternalIdBundle securityBundle = trade.getSecurityLink().getExternalId();
    if (securityBundle == null) {
      throw new OpenGammaRuntimeException("Can only store positions with a link to an ExternalId");
    }
    if (securityBundle.size() != 1) {
      s_logger.warn("Bundle {} not exactly one. Possible misuse of this source.", securityBundle);
    }
    ExternalId securityId = securityBundle.iterator().next();
    jedis.hset(redisKey, "SEC", securityId.toString());

    for (Map.Entry<String, String> attribute : trade.getAttributes().entrySet()) {
      jedis.hset(redisKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }

    return uniqueId;
  }
  
  // ---------------------------------------------------------------------------------------
  // QUERIES OUTSIDE OF POSITION SOURCE INTERFACE
  // ---------------------------------------------------------------------------------------
  
  public Portfolio getByName(String portfolioName) {
    ArgumentChecker.notNull(portfolioName, "portfolioName");
    
    Portfolio portfolio = null;
    
    try (Timer.Context context = _getPortfolioTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        String nameKey = toPortfolioNameRedisKey(portfolioName);
        String uniqueIdString = jedis.hget(nameKey, "UNIQUE_ID");
        
        if (uniqueIdString != null) {
          UniqueId uniqueId = UniqueId.parse(uniqueIdString);
          portfolio = getPortfolioWithJedis(jedis, uniqueId);
        }

        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to get portfolio by name " + portfolioName, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get portfolio by name " + portfolioName, e);
      }
      
    }
    return portfolio;
  }
  
  public Map<String, UniqueId> getAllPortfolioNames() {
    Map<String, UniqueId> result = new TreeMap<String, UniqueId>();
    Jedis jedis = getJedisPool().getResource();
    try {
      Map<String, String> portfolioNames = jedis.hgetAll(_portfoliosHashKeyName);
      for (Map.Entry<String, String> entry : portfolioNames.entrySet()) {
        result.put(entry.getKey(), UniqueId.parse(entry.getValue()));
      }

      getJedisPool().returnResource(jedis);
    } catch (Exception e) {
      s_logger.error("Unable to get portfolio names", e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to get portfolio names", e);
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------
  // IMPLEMENTATION OF POSITION SOURCE
  // ---------------------------------------------------------------------------------------
  
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public Portfolio getPortfolio(UniqueId uniqueId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    SimplePortfolio portfolio = null;
    
    try (Timer.Context context = _getPortfolioTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        portfolio = getPortfolioWithJedis(jedis, uniqueId);
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to get portfolio " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get portfolio " + uniqueId, e);
      }
      
    }
    
    if (portfolio == null) {
      throw new DataNotFoundException("Unable to locate portfolio with UniqueId " + uniqueId);
    }
    
    return portfolio;
  }
  
  protected SimplePortfolio getPortfolioWithJedis(Jedis jedis, UniqueId uniqueId) {
    SimplePortfolio portfolio = null;
    String redisKey = toPortfolioRedisKey(uniqueId);
    if (jedis.exists(redisKey)) {
      Map<String, String> hashFields = jedis.hgetAll(redisKey);
      
      portfolio = new SimplePortfolio(hashFields.get("NAME"));
      portfolio.setUniqueId(uniqueId);

      for (Map.Entry<String, String> field : hashFields.entrySet()) {
        if (!field.getKey().startsWith("ATT-")) {
          continue;
        }
        String attributeName = field.getKey().substring(4);
        portfolio.addAttribute(attributeName, field.getValue());
      }
      
      SimplePortfolioNode portfolioNode = new SimplePortfolioNode();
      portfolioNode.setName(portfolio.getName());

      String portfolioPositionsKey = toPortfolioPositionsRedisKey(portfolio.getUniqueId());
      Set<String> positionUniqueIds = jedis.smembers(portfolioPositionsKey);
      for (String positionUniqueId : positionUniqueIds) {
        Position position = getPosition(jedis, UniqueId.parse(positionUniqueId));
        if (position != null) {
          portfolioNode.addPosition(position);
        }
      }
      portfolio.setRootNode(portfolioNode);
    }
    return portfolio;
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    return getPortfolio(UniqueId.of(objectId, null), null);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId uniqueId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Trades not supported.");
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    SimplePosition position = null;
    
    try (Timer.Context context = _getPositionTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        position = getPosition(jedis, uniqueId);
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to get position " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get position " + uniqueId, e);
      }
      
    }
    
    if (position == null) {
      throw new DataNotFoundException("Unable to find position with UniqueId " + uniqueId);
    }
    
    return position;
  }

  protected SimplePosition getPosition(Jedis jedis, UniqueId uniqueId) {
    String redisKey = toPositionRedisKey(uniqueId);
    if (!jedis.exists(redisKey)) {
      return null;
    }
    SimplePosition position = new SimplePosition();
    position.setUniqueId(uniqueId);
    Map<String, String> hashFields = jedis.hgetAll(redisKey);
    position.setQuantity(new BigDecimal(hashFields.get("QTY")));
    ExternalId secId = ExternalId.parse(hashFields.get("SEC"));
    SimpleSecurityLink secLink = new SimpleSecurityLink();
    secLink.addExternalId(secId);
    position.setSecurityLink(secLink);

    for (Map.Entry<String, String> field : hashFields.entrySet()) {
      if (!field.getKey().startsWith("ATT-")) {
        continue;
      }
      String attributeName = field.getKey().substring(4);
      position.addAttribute(attributeName, field.getValue());
    }

    // trades
    String tradesKey = toPositionTradesRedisKey(position.getUniqueId());
    Set<String> tradesUniqueIds = jedis.smembers(tradesKey);
    for (String tradesUniqueId : tradesUniqueIds) {
      Trade trade = getTrade(jedis, UniqueId.parse(tradesUniqueId));
      if (trade != null) {
        position.addTrade(trade);
      }
    }

    return position;
  }

  protected SimpleTrade getTrade(Jedis jedis, UniqueId uniqueId) {
    String redisKey = toTradeRedisKey(uniqueId);
    if (!jedis.exists(redisKey)) {
      return null;
    }
    SimpleTrade trade = new SimpleTrade();
    trade.setUniqueId(uniqueId);
    Map<String, String> hashFields = jedis.hgetAll(redisKey);
    trade.setQuantity(new BigDecimal(hashFields.get("QTY")));
    ExternalId secId = ExternalId.parse(hashFields.get("SEC"));
    SimpleSecurityLink secLink = new SimpleSecurityLink();
    secLink.addExternalId(secId);
    trade.setSecurityLink(secLink);

    for (Map.Entry<String, String> field : hashFields.entrySet()) {
      if (!field.getKey().startsWith("ATT-")) {
        continue;
      }
      String attributeName = field.getKey().substring(4);
      trade.addAttribute(attributeName, field.getValue());
    }

    return trade;
  }

  @Override
  public Position getPosition(ObjectId objectId, VersionCorrection versionCorrection) {
    return getPosition(UniqueId.of(objectId, null));
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    SimpleTrade trade = null;

    try (Timer.Context context = _getPositionTimer.time()) {

      Jedis jedis = getJedisPool().getResource();
      try {

        trade = getTrade(jedis, uniqueId);

        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to get position " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get trade " + uniqueId, e);
      }

    }

    if (trade == null) {
      throw new DataNotFoundException("Unable to find position with UniqueId " + uniqueId);
    }

    return trade;
  }

}
