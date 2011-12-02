/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;

/**
 * Ranks schemes, allowing the preferred ones to be extracted from identifier bundles.
 */
public final class ExternalSchemeRank {

  /**
   * Default instance with the locally defined preferences (as found in the classpath).
   */
  public static final ExternalSchemeRank DEFAULT = createDefault();

  /**
   * Ranks the schemes - the lower the numerical value, the more preferred the scheme is.
   */
  private final Map<String, Integer> _ranks;

  private ExternalSchemeRank(final Map<String, Integer> ranks) {
    _ranks = ranks;
  }

  public static ExternalSchemeRank ofStrings(final Map<String, Integer> ranks) {
    final Map<String, Integer> copy = new HashMap<String, Integer>(ranks);
    return new ExternalSchemeRank(copy);
  }

  public static ExternalSchemeRank ofSchemes(final Map<ExternalScheme, Integer> ranks) {
    final Map<String, Integer> copy = new HashMap<String, Integer>();
    for (Map.Entry<ExternalScheme, Integer> rank : ranks.entrySet()) {
      copy.put(rank.getKey().getName(), rank.getValue());
    }
    return new ExternalSchemeRank(copy);
  }

  public static ExternalSchemeRank ofStrings(final String[] schemes) {
    final Map<String, Integer> copy = new HashMap<String, Integer>();
    int rank = 1;
    for (String scheme : schemes) {
      copy.put(scheme, rank++);
    }
    return new ExternalSchemeRank(copy);
  }

  public static ExternalSchemeRank ofSchemes(final ExternalScheme[] schemes) {
    final Map<String, Integer> copy = new HashMap<String, Integer>();
    int rank = 1;
    for (ExternalScheme scheme : schemes) {
      copy.put(scheme.getName(), rank++);
    }
    return new ExternalSchemeRank(copy);
  }

  public static ExternalSchemeRank ofStrings(final List<String> schemes) {
    final Map<String, Integer> copy = new HashMap<String, Integer>();
    int rank = 1;
    for (String scheme : schemes) {
      copy.put(scheme, rank++);
    }
    return new ExternalSchemeRank(copy);
  }

  public static ExternalSchemeRank ofSchemes(final List<ExternalScheme> schemes) {
    final Map<String, Integer> copy = new HashMap<String, Integer>();
    int rank = 1;
    for (ExternalScheme scheme : schemes) {
      copy.put(scheme.getName(), rank++);
    }
    return new ExternalSchemeRank(copy);
  }

  private static ExternalSchemeRank createDefault() {
    final ResourceBundle mapping = ResourceBundle.getBundle(ExternalSchemeRank.class.getName());
    final Map<String, Integer> ranks = new HashMap<String, Integer>();
    for (final String key : mapping.keySet()) {
      final String value = mapping.getString(key);
      ranks.put(key, Integer.parseInt(value));
    }
    return new ExternalSchemeRank(ranks);
  }

  /**
   * Ranks a given scheme. The lower the number, the more preferred the scheme is. If the scheme isn't recognized by the
   * ranking, {@code Integer.MAX_VALUE} is returned.
   * 
   * @param scheme the scheme, not null
   * @return the rank
   */
  public int rankScheme(final String scheme) {
    final Integer rank = _ranks.get(scheme);
    if (rank != null) {
      return rank;
    } else {
      return Integer.MAX_VALUE;
    }
  }

  /**
   * Ranks a given scheme. The lower the number, the more preferred the scheme is. If the scheme isn't recognized by the
   * ranking, {@code Integer.MAX_VALUE} is returned.
   * 
   * @param scheme the scheme, not null
   * @return the rank
   */
  public int rankScheme(final ExternalScheme scheme) {
    return rankScheme(scheme.getName());
  }

  /**
   * Returns the preferred identifier from the bundle.
   * 
   * @param bundle identifier bundle to search
   * @return null if the bundle is null or empty
   */
  public ExternalId getPreferredIdentifier(final ExternalIdBundle bundle) {
    if (bundle == null) {
      return null;
    }
    ExternalId preferred = null;
    int rank = Integer.MAX_VALUE;
    for (ExternalId identifier : bundle) {
      if (preferred == null) {
        preferred = identifier;
        rank = rankScheme(identifier.getScheme());
      } else {
        final int thisScheme = rankScheme(identifier.getScheme());
        if (thisScheme < rank) {
          rank = thisScheme;
          preferred = identifier;
        }
      }
    }
    return preferred;
  }

  public String[] asStrings() {
    final String[] strings = new String[_ranks.size()];
    final List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(_ranks.entrySet());
    Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
      @Override
      public int compare(final Entry<String, Integer> o1, final Entry<String, Integer> o2) {
        final int r1 = o1.getValue();
        final int r2 = o2.getValue();
        if (r1 < r2) {
          return -1;
        } else if (r1 > r2) {
          return 1;
        } else {
          return 0;
        }
      }
    });
    int i = 0;
    for (Map.Entry<String, Integer> entry : entries) {
      strings[i++] = entry.getKey();
    }
    return strings;
  }

  @Override
  public String toString() {
    return "ExternalSchemeRank" + _ranks;
  }

  @Override
  public int hashCode() {
    return _ranks.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ExternalSchemeRank)) {
      return false;
    }
    final ExternalSchemeRank other = (ExternalSchemeRank) o;
    return _ranks.equals(other._ranks);
  }

}
