/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Records the content of a network stream to a file.
 */
public class RecordNetworkStream {

  public static void main(final String[] args) throws IOException { // CSIGNORE
    final String host = args[0];
    final Integer port = Integer.parseInt(args[1]);
    final String file = args[2];
    try (Socket socket = new Socket(host, port)) {
      try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
        final BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        try {
          final byte[] buffer = new byte[4096];
          final long start = System.nanoTime();
          while (System.nanoTime() - start < 300000000000L) {
            final int bytes = input.read(buffer);
            if (bytes < 0) {
              return;
            }
            output.write(buffer, 0, bytes);
          }
        } finally {
          input.close();
        }
      }
    }
  }

}
