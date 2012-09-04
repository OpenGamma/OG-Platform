/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest.json;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

/**
 * REST endpoint for compressing and decompressing the state of the web UI using GZIP and encoding it using base64.
 */
@Path("compressor")
public class Compressor {

  /**
   * Compresses the request body using GZIP, encodes it to base64 and writes it to the response body.
   * @param request The request whose body need to be compressed
   * @param response The response whose body is the GZIPped and base64 encoded version of the request body
   * @throws IOException If compression fails
   */
  @POST
  @Path("compress")
  public void compress(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
    compressStream(request.getInputStream(), response.getOutputStream());
  }

  /**
   * Decodes the request body from base64 and decompresses it using GZIP.
   * @param request A request with a base64 encoded GZIPped body
   * @param response The response whose body unzipped decoded content of the request
   * @throws IOException If decompression fails
   */
  @POST
  @Path("decompress")
  public void decompress(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
    decompressStream(request.getInputStream(), response.getOutputStream());
  }

  /* package */ static void compressStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    InputStream iStream = new BufferedInputStream(inputStream);
    GZIPOutputStream oStream = new GZIPOutputStream(new Base64OutputStream(new BufferedOutputStream(outputStream)), 2048);
    byte[] buffer = new byte[2048];
    int bytesRead;
    while ((bytesRead = iStream.read(buffer)) != -1) {
      oStream.write(buffer, 0, bytesRead);
    }
    oStream.close(); // this is necessary for the gzip and base64 streams
  }

  /* package */ static void decompressStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    InputStream iStream = new GZIPInputStream(new Base64InputStream(new BufferedInputStream(inputStream)));
    OutputStream oStream = new BufferedOutputStream(outputStream);
    byte[] buffer = new byte[2048];
    int bytesRead;
    while ((bytesRead = iStream.read(buffer)) != -1) {
      oStream.write(buffer, 0, bytesRead);
    }
    oStream.flush();
  }
}
