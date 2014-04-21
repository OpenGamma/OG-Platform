/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.Primitive.ExternalBundleIdentifiablePrimitive;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#PRIMITIVE}.
 */
public class PrimitiveResolver extends AbstractIdentifierResolver implements Resolver<Primitive> {

  private static final String SCHEME_PREFIX = "ExternalId-";

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
    boolean backslash = false;
    for (int j = i; j < l; j++) {
      final char c = str.charAt(j);
      if (c == '-') {
        count++;
      } else if (c == '\\') {
        j++;
        backslash = true;
      }
    }
    if ((count == 1) && !backslash) {
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

  public static UniqueId resolveExternalId(final ExternalIdBundle identifiers) {
    final List<ExternalId> ids = new ArrayList<>(identifiers.getExternalIds());
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
  public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
    return resolveExternalId(identifiers);
  }

  @Override
  public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
    return identifier.atLatestVersion();
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    return resolveObjectIdsSingleThread(this, identifiers, versionCorrection);
  }

  // ObjectResolver

  /**
   * Utility function for resolving external ids from unique identifier
   * @param uniqueId unique identifier
   * @param schemePrefix the scheme prefix
   * @return external id bundle
   */
  public static ExternalIdBundle resolveExternalIds(final UniqueId uniqueId, String schemePrefix) {
    final String scheme = uniqueId.getScheme();

    final String[] schemes = unescape(scheme, schemePrefix.length());
    if (schemes != null) {
      final String[] values = unescape(uniqueId.getValue(), 0);
      if (values != null) {
        if (schemes.length == values.length) {
          if (schemes.length == 1) {
            return ExternalIdBundle.of(schemes[0], values[0]);
          } else {
            final ExternalId[] identifiers = new ExternalId[schemes.length];
            for (int i = 0; i < schemes.length; i++) {
              identifiers[i] = ExternalId.of(schemes[i], values[i]);
            }
            return ExternalIdBundle.of(identifiers);
          }
        }
      }
    }
    return null;
  }

  @Override
  public Primitive resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    final String scheme = uniqueId.getScheme();
    if (scheme.startsWith(SCHEME_PREFIX)) {
      ExternalIdBundle externalIdBundle = resolveExternalIds(uniqueId, SCHEME_PREFIX);
      if (externalIdBundle.size() == 1) {
        return new ExternalIdentifiablePrimitive(uniqueId, functional(externalIdBundle.getExternalIds()).first());
      } else {
        return new ExternalBundleIdentifiablePrimitive(uniqueId, externalIdBundle);
      }
    } else {
      return new Primitive(uniqueId);
    }

  }

  @Override
  public DeepResolver deepResolver() {
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
