/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.net.URI;
import java.util.Comparator;

import com.opengamma.util.ArgumentChecker;

/**
 * Describes a configuration that the OG-Language stack can connect to.
 */
public final class Configuration {

  private final URI _uri;
  private final String _description;

  public Configuration(final URI uri, final String description) {
    ArgumentChecker.notNull(uri, "uri");
    _uri = uri;
    _description = (description != null) ? description : uri.toString();
  }

  public URI getURI() {
    return _uri;
  }

  public String getDescription() {
    return _description;
  }

  public String getHost() {
    return getURI().getHost();
  }

  public Integer getPort() {
    final int port = getURI().getPort();
    return (port <= 0) ? null : port;
  }

  @Override
  public String toString() {
    return getURI().toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Configuration)) {
      return false;
    }
    return getURI().equals(((Configuration) o).getURI());
  }

  @Override
  public int hashCode() {
    return getURI().hashCode();
  }

  private static int compare(final String a, final String b) {
    if (a == null) {
      if (b == null) {
        return 0;
      } else {
        return -1;
      }
    } else {
      if (b == null) {
        return 1;
      } else {
        return a.compareTo(b);
      }
    }
  }

  private static int compare(final Integer a, final Integer b) {
    if (a == null) {
      if (b == null) {
        return 0;
      } else {
        return -1;
      }
    } else {
      if (b == null) {
        return 1;
      } else {
        return a.compareTo(b);
      }
    }
  }

  /**
   * Comparator to sort configurations by host, port and then description.
   */
  public static final Comparator<Configuration> SORT_BY_HOST = new Comparator<Configuration>() {

    @Override
    public int compare(final Configuration o1, final Configuration o2) {
      int c = Configuration.compare(o1.getHost(), o2.getHost());
      if (c != 0) {
        return c;
      }
      c = Configuration.compare(o1.getPort(), o2.getPort());
      if (c != 0) {
        return c;
      }
      return Configuration.compare(o1.getDescription(), o2.getDescription());
    }

  };

  /**
   * Comparator to sort configurations by description, host and then port.
   */
  public static final Comparator<Configuration> SORT_BY_DESCRIPTION = new Comparator<Configuration>() {

    @Override
    public int compare(final Configuration o1, final Configuration o2) {
      int c = Configuration.compare(o1.getDescription(), o2.getDescription());
      if (c != 0) {
        return c;
      }
      c = Configuration.compare(o1.getHost(), o2.getHost());
      if (c != 0) {
        return c;
      }
      return Configuration.compare(o1.getPort(), o2.getPort());
    }

  };

}
