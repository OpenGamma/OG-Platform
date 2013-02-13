/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#PRIMITIVE}.
 */
public class PrimitiveResolver implements Resolver<UniqueIdentifiable> {

  private static final String SCHEME_PREFIX = "ExternalId-";

  private static final class Bundle implements ExternalBundleIdentifiable, UniqueIdentifiable {

    private final UniqueId _uid;
    private final ExternalIdBundle _eids;

    private Bundle(final UniqueId uid, final ExternalIdBundle eids) {
      _uid = uid;
      _eids = eids;
    }

    @Override
    public UniqueId getUniqueId() {
      return _uid;
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return _eids;
    }

  }

  private static final class Single implements ExternalIdentifiable, UniqueIdentifiable {

    private final UniqueId _uid;
    private final ExternalId _eid;

    private Single(final UniqueId uid, final ExternalId eid) {
      _uid = uid;
      _eid = eid;
    }

    @Override
    public UniqueId getUniqueId() {
      return _uid;
    }

    @Override
    public ExternalId getExternalId() {
      return _eid;
    }

  }

  private static void escape(final String str, final StringBuilder into) {
    if ((str.indexOf('-') < 0) && (str.indexOf('\\') < 0)) {
      into.append(str);
    } else {
      final int l = str.length();
      for (int i = 0; i < l; i++) {
        final char c = str.charAt(i);
        if ((c == '-') || (c == '\\')) {
          into.append('\\').append(c);
        } else {
          into.append(c);
        }
      }
    }
  }

  private static String[] unescape(final String str, final int i) {
    final int l = str.length();
    int count = 1;
    for (int j = i; j < l; j++) {
      final char c = str.charAt(j);
      if (c == '-') {
        count++;
      } else if (c == '\\') {
        j++;
      }
    }
    if (count == 1) {
      return new String[] {str.substring(i) };
    }
    final String[] result = new String[count];
    final StringBuilder sb = new StringBuilder();
    count = 0;
    for (int j = i; j < l; j++) {
      final char c = str.charAt(j);
      if (c == '-') {
        result[count++] = sb.toString();
        sb.delete(0, sb.length());
      } else if (c == '\\') {
        j++;
        if (j >= l) {
          return null;
        }
        sb.append(str.charAt(j));
      } else {
        sb.append(c);
      }
    }
    result[count] = sb.toString();
    return result;
  }

  // IdentifierResolver

  @Override
  public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
    final List<ExternalId> ids = new ArrayList<ExternalId>(identifiers.getExternalIds());
    // Natural sorting of external identifiers is by scheme and then by value
    Collections.sort(ids);
    final StringBuilder scheme = new StringBuilder(SCHEME_PREFIX);
    final StringBuilder value = new StringBuilder();
    boolean first = true;
    for (final ExternalId id : ids) {
      if (first) {
        first = false;
      } else {
        scheme.append('-');
        value.append('-');
      }
      escape(id.getScheme().getName(), scheme);
      escape(id.getValue(), value);
    }
    return UniqueId.of(scheme.toString(), value.toString());
  }

  @Override
  public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Set<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ExternalIdBundle bundle : identifiers) {
      result.put(bundle, resolveExternalId(bundle, versionCorrection));
    }
    return result;
  }

  @Override
  public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
    return identifier.atLatestVersion();
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(final Set<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ObjectId identifier : identifiers) {
      result.put(identifier, resolveObjectId(identifier, versionCorrection));
    }
    return result;
  }

  // ObjectResolver

  @Override
  public UniqueIdentifiable resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    final String scheme = uniqueId.getScheme();
    if (scheme.startsWith(SCHEME_PREFIX)) {
      final String[] schemes = unescape(scheme, SCHEME_PREFIX.length());
      if (schemes != null) {
        final String[] values = unescape(uniqueId.getValue(), 0);
        if (values != null) {
          if (schemes.length == values.length) {
            if (schemes.length == 1) {
              return new Single(uniqueId, ExternalId.of(schemes[0], values[0]));
            } else {
              final ExternalId[] identifiers = new ExternalId[schemes.length];
              for (int i = 0; i < schemes.length; i++) {
                identifiers[i] = ExternalId.of(schemes[i], values[i]);
              }
              return new Bundle(uniqueId, ExternalIdBundle.of(identifiers));
            }
          }
        }
      }
    }
    return uniqueId;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
