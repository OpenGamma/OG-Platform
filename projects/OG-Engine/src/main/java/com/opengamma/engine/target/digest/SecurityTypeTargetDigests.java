/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.digest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;

/**
 * Basic implementation that returns the security type as the digest.
 * <p>
 * This is supplied mainly for use as an example implementation. The quality of these digests will depend on the function repository and security modeling being used. An implementation that has more
 * detailed knowledge of the analytic functions or targets in use might be necessary to benefit from the target digest algorithm.
 */
public class SecurityTypeTargetDigests extends AbstractTargetDigests {

  private static final class Digest {

    private final Object _label;
    private final Object _user;

    public Digest(final Object label, final Object user) {
      _label = label;
      _user = user;
    }

    @Override
    public String toString() {
      return _label + "(" + _user + ")";
    }

  }

  /**
   * Base class for a map that creates values on the fly based on the keys.
   */
  protected abstract static class MapImpl<K, V> {

    private volatile Map<K, V> _data = Collections.emptyMap();

    protected abstract V createValue(K key);

    public V get(final K key) {
      if (key == null) {
        return null;
      }
      Map<K, V> data = _data;
      V value = data.get(key);
      if (value != null) {
        return value;
      }
      value = createValue(key);
      synchronized (this) {
        data = _data;
        final V existing = data.get(key);
        if (existing != null) {
          return existing;
        }
        final Map<K, V> newData = new HashMap<K, V>(data);
        newData.put(key, value);
        _data = newData;
      }
      return value;
    }

  }

  /**
   * Normalization cache of digests. This is to avoid excessive object creation, and cheapens the comparison operations as any digest may be compared by identity only.
   */
  protected static final class Digests extends MapImpl<Object, Digest> {

    private final Object _label;

    public Digests(final Object label) {
      _label = label;
    }

    @Override
    protected Digest createValue(final Object user) {
      return new Digest(_label, user);
    }

  }

  private final Digests _positions = new Digests("POSITION");
  private final Digests _trades = new Digests("TRADE");

  public SecurityTypeTargetDigests() {
    addHandler(ComputationTargetType.POSITION, new TargetDigests() {
      @Override
      public Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification targetSpec) {
        final ComputationTarget target = context.getComputationTargetResolver().resolve(targetSpec);
        if (target != null) {
          return getPositionDigest(target.getPosition());
        } else {
          return null;
        }
      }
    });
    addHandler(ComputationTargetType.TRADE, new TargetDigests() {
      @Override
      public Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification targetSpec) {
        final ComputationTarget target = context.getComputationTargetResolver().resolve(targetSpec);
        if (target != null) {
          return getTradeDigest(target.getTrade());
        } else {
          return null;
        }
      }
    });
    addHandler(ComputationTargetType.SECURITY, new TargetDigests() {
      @Override
      public Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification targetSpec) {
        final ComputationTarget target = context.getComputationTargetResolver().resolve(targetSpec);
        if (target != null) {
          return getSecurityDigest(target.getSecurity());
        } else {
          return null;
        }
      }
    });
  }

  protected Object getPositionDigest(Position position) {
    return _positions.get(getSecurityDigest(position.getSecurity()));
  }

  protected Object getTradeDigest(Trade trade) {
    return _trades.get(getSecurityDigest(trade.getSecurity()));
  }

  protected Object getSecurityDigest(Security security) {
    return security.getSecurityType();
  }

}
