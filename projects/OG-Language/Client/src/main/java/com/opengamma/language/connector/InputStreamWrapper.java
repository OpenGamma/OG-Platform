/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an input stream, hiding the {@link InputStream#available} method because it throws exceptions on pipes but gets called when the stream is wrapped in a buffered input stream.
 */
/* package */class InputStreamWrapper extends InputStream {

  private final InputStream _i;

  public InputStreamWrapper(final InputStream i) {
    _i = i;
  }

  @Override
  public int read() throws IOException {
    return _i.read();
  }

  @Override
  public int read(final byte[] b) throws IOException {
    return _i.read(b);
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    return _i.read(b, off, len);
  }

  @Override
  public long skip(final long n) throws IOException {
    return _i.skip(n);
  }

  @Override
  public int available() throws IOException {
    return 0;
  }

  @Override
  public void close() throws IOException {
    _i.close();
  }

  @Override
  public void mark(final int readlimit) {
  }

  @Override
  public void reset() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean markSupported() {
    return false;
  }

}
