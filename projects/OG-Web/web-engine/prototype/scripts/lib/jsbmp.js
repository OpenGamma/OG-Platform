/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 
 *
 * This library is a modified version of the MIT-licensed BMP and Base64 libraries written
 * by Sam Angove. His original license comments and copyright statements have been left intact
 * but some of the code has been reformatted and refactored.
 *
 */
(function (pub, namespace) {
    /* jsbmp

    Create bitmap files using JavaScript. The use-case this was written
    for is to create simple images which can be provided in `data` URIs.

    Copyright (c) 2009 Sam Angove <sam [a inna circle] rephrase [period] net>

    License: MIT

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
    */

    /*
    Create the binary contents of a bitmap file.

    This is not a public interface and is subject to change.

    Arguments:

        width -- width of the bitmap
        height -- height of the bitmap
        palette -- array of 'rrggbb' strings (if appropriate)
        imgdata -- pixel data in faux-binary escaped text
        bpp -- bits per pixel; use in conjunction with compression
        compression -- compression mode (e.g. uncompressed, 8-bit RLE, 4-bit RLE)
    */
    var bmp = function (width, height, palette, imgdata, bpp, compression) {
        var imgdatasize = imgdata.length,
            palettelength = palette.length,
            palettesize = palettelength * 4,            // 4 bytes per colour
            filesize = 64 + palettesize + imgdatasize,  // size of file
            pixeloffset = 54 + palettesize;             // pixel data offset
        var data = [
            'BM',                                       // magic number
            pack(width),                                // size of file
            '\x00\x00\x00\x00',                         // unused
            pack(pixeloffset),                          // number of bytes until pixel data
            '\x28\x00\x00\x00',                         // number of bytes left in the header
            pack(width),                                // width of pixmap
            pack(height),                               // height of pixmap
            '\x01\x00',                                 // number of colour planes, must be 1
            pack(bpp, 2),                               // bits per pixel
            pack(compression),                          // compression mode
            pack(imgdatasize),                          // size of raw BMP data (after the header)
            '\x13\x0B\x00\x00',                         // # pixels per metre horizontal res.
            '\x13\x0B\x00\x00',                         // # pixels per metre vertical res
            pack(palettelength),                        // num colours in palette
            '\x00\x00\x00\x00'                          // all colours are important
         ];
        for (var i = 0; i < palettelength; ++i) data.push(pack(parseInt(palette[i], 16)));
        data.push(imgdata);
        return data.join('');
    };
    /*
    Pack JS integer (signed big-endian?) `num` into a little-endian binary string
    of length `len`.
    */
    var pack = function (num, length) {
        var output = [], i, length = typeof length === 'undefined' ? 4 : length;
        for (i = 0; i < length; ++i) output.push(String.fromCharCode((num >> (i * 8)) & 0xff));
        return output.join('');
    };


    /*
    Create an uncompressed Windows bitmap (BI_RGB) given width, height and an
    array of pixels.

    Pixels should be in BMP order, i.e. starting at the bottom left, going up
    one row at a time.

    Example:

        var onebluepixel = bmp(1, 1, ['0000ff']);
    */
    var bmp_rgb = function (width, height, pixarray) {
        var rowsize = (width * 3), rowpadding = (rowsize % 4);
        if (rowpadding) rowpadding = Math.abs(4 - rowpadding);
        var imgdatasize = (rowsize + rowpadding) * height, i, j, k = 0, pix, pixcache = {}, pixels = []
        for (i = 0; i < height; ++i) {
            for (j = 0; j < width; ++j) {
                if (!pixcache[pix = pixarray[k++]]) pixcache[pix] = pack(parseInt(pix, 16), 3);
                pixels.push(pixcache[pix]);
            }
            for (j = 0; j < rowpadding; ++j) pixels.push('\x00');
        }
        return bmp(width, height, [], pixels.join(''), 24, 0);
    };

    /*
    Create a Windows bitmap encoded with 8-bit run-length encoding (BI_RLE8)
    given width, height and an array of [colour, runlength] pairs.

    Pixels should be in BMP order, i.e. starting at the bottom left, going up
    one row at a time.

    Example:

        var twothousandbluepixels = bmp(2000, 1, ['0000ff', 2000]);
    */
    var bmp_rle8 = function (width, height, pixarray) {
        var pixcache = {}, palette = [], pixels = [], linelen, run, colour, runlength, i, j = 0, overflow;
        for (i = 0; i < height; ++i) {
            linelen = 0;
            while (linelen < width) {
                if (overflow) (run = overflow), (overflow = null); else run = pixarray[j++];
                colour = run[0];
                runlength = run[1];
                // Length has to fit in one byte, so split into multiple blocks
                // if the run is too long.
                if (runlength > 255) {
                    overflow = [colour, runlength - 255];
                    runlength = 255;
                }
                if (!pixcache[colour]) {
                    pixcache[colour] = pack(palette.length, 1);
                    palette.push(colour);
                }
                pixels.push(pack(runlength, 1));
                pixels.push(pixcache[colour]);
                linelen += runlength;
            }
            // end of line marker
            pixels.push('\x00\x00');
        }
        pixels.push('\x00\x01');
        return bmp(width, height, palette, pixels.join(''), 8, 1);
    };

    /* datauri

    Create base64encoded `data` URIs from binary data.

    Copyright (c) 2009 Sam Angove <sam [a inna circle] rephrase [period] net>

    License: MIT

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
    */

    /*
    Base64 encode binary data for ASCII transport

    See: http://en.wikipedia.org/wiki/Base64
    */
    var base64encode = (function (){
        // This is a non-standard extension available in Mozilla and possibly other browsers.
        if (pub.btoa) return pub.btoa;
        /* JS fallback based on public domain code from Tyler Akins:
            http://rumkin.com/tools/compression/base64.php */
        var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
        return function (data) {
            var chr1, chr2, chr3, i = 0, j = 0, length = data.length,
                output = new Array(Math.ceil(data.length / 3) * 4);
            while (i < length) {                                            // convert 3 bytes into 4 6-bit chunks
                chr1 = data.charCodeAt(i++);
                chr2 = data.charCodeAt(i++);
                chr3 = data.charCodeAt(i++);
                output[j++] = chars[chr1 >> 2];                             // reduce byte to 6 bits
                output[j++] = chars[((chr1 & 3) << 4) | (chr2 >> 4)];       // last 2 bits of chr1 + first 4 of chr2
                if (isNaN(chr2)) {                                          // pad with zeroes
                    output[j++] = '=';
                    output[j++] = '=';
                } else {
                    output[j++] = chars[((chr2 & 15) << 2) | (chr3 >> 6)];  // last 4 bits of chr2 + first 2 of chr3
                    output[j++] = isNaN(chr3) ? '=' : chars[chr3 & 63];     // last 6 bits
                }
            }
            return output.join('');
        };
    })();

    /*
    Convert binary data to a `data` URI.

        See: http://en.wikipedia.org/wiki/Data_URI_scheme
    */
    var datauri = function (type, data) {return 'data:' + type + ';base64,' + encodeURIComponent(base64encode(data));};

    pub[namespace] = {
        rgb: function (width, height, pixarray) {return datauri('image/bmp', bmp_rgb(width, height, pixarray));},
        rle8: function (width, height, pixarray) {return datauri('image/bmp', bmp_rle8(width, height, pixarray));}
    };
})(typeof exports === 'undefined' ? window : exports, 'BMP');