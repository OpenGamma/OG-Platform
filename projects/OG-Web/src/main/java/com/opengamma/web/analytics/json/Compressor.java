/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;

/**
 * REST endpoint for compressing and decompressing the state of the web UI using GZIP and encoding it using base64.
 */
@Path("compressor")
public class Compressor {

  /**
   * Compresses the "content" parameter using GZIP, encodes it to base64 and returns it wrapped in a JSON object
   * with the key "data".
   * @param content The content that needs to be compressed
   * @return Response containing JSON with the compressed content keyed with "data"
   * @throws IOException If compression fails
   */
  @POST
  @Path("compress")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response compress(@FormParam("content") String content) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    compressStream(new ByteArrayInputStream(content.getBytes()), outputStream);
    ImmutableMap<String, String> data = ImmutableMap.of("data", outputStream.toString());
    return Response.status(Response.Status.OK).entity(new JSONObject(data).toString()).build();
  }

  /**
   * Decodes the "content" parameter from base64, decompresses it using GZIP and returns it wrapped in a JSON object
   * with the key "data".
   * @param content The content that needs to be decompressed
   * @return Response containing JSON with the decompressed content keyed with "data"
   * @throws IOException If decompression fails
   */
  @POST
  @Path("decompress")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response decompress(@FormParam("content") String content) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    decompressStream(new ByteArrayInputStream(content.getBytes()), outputStream);
    String data = "{\"data\":" + outputStream.toString() + "}";
    return Response.status(Response.Status.OK).entity(data).build();
  }

  /* package */ static void compressStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    InputStream iStream = new BufferedInputStream(inputStream);
    GZIPOutputStream oStream =
        new GZIPOutputStream(new Base64OutputStream(new BufferedOutputStream(outputStream), true, -1, null), 2048);
    byte[] buffer = new byte[2048];
    int bytesRead;
    while ((bytesRead = iStream.read(buffer)) != -1) {
      oStream.write(buffer, 0, bytesRead);
    }
    oStream.close(); // this is necessary for the gzip and base64 streams
  }

  /* package */ static void decompressStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    @SuppressWarnings("resource")
    InputStream iStream = new GZIPInputStream(new Base64InputStream(new BufferedInputStream(inputStream), false, -1, null));
    OutputStream oStream = new BufferedOutputStream(outputStream);
    byte[] buffer = new byte[2048];
    int bytesRead;
    while ((bytesRead = iStream.read(buffer)) != -1) {
      oStream.write(buffer, 0, bytesRead);
    }
    oStream.flush();
  }

}
