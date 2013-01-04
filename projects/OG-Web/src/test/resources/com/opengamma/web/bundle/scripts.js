/**
 * @SEE https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/map
 */

if (!Array.prototype.map)
{
  Array.prototype.map = function(fun /*, thisp */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (typeof fun !== "function")
      throw new TypeError();

    var res = new Array(len);
    var thisp = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in t)
        res[i] = fun.call(thisp, t[i], i, t);
    }

    return res;
  };
}
/**
 * @SEE https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/reduce
 */
 
if (!Array.prototype.reduce)
{
  Array.prototype.reduce = function(fun /*, initialValue */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (typeof fun !== "function")
      throw new TypeError();

    // no value to return if no initial value and an empty array
    if (len == 0 && arguments.length == 1)
      throw new TypeError();

    var k = 0;
    var accumulator;
    if (arguments.length >= 2)
    {
      accumulator = arguments[1];
    }
    else
    {
      do
      {
        if (k in t)
        {
          accumulator = t[k++];
          break;
        }

        // if array contains no values, no initial value to return
        if (++k >= len)
          throw new TypeError();
      }
      while (true);
    }

    while (k < len)
    {
      if (k in t)
        accumulator = fun.call(undefined, accumulator, t[k], k, t);
      k++;
    }

    return accumulator;
  };
}
/**
 * @SEE https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/indexOf
 */

if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(searchElement /*, fromIndex */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (len === 0)
      return -1;

    var n = 0;
    if (arguments.length > 0)
    {
      n = Number(arguments[1]);
      if (n !== n)
        n = 0;
      else if (n !== 0 && n !== (1 / 0) && n !== -(1 / 0))
        n = (n > 0 || -1) * Math.floor(Math.abs(n));
    }

    if (n >= len)
      return -1;

    var k = n >= 0
          ? n
          : Math.max(len - Math.abs(n), 0);

    for (; k < len; k++)
    {
      if (k in t && t[k] === searchElement)
        return k;
    }
    return -1;
  };
}


/**
 * @SEE https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/filter
 */

if (!Array.prototype.filter)
{
  Array.prototype.filter = function(fun /*, thisp */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (typeof fun !== "function")
      throw new TypeError();

    var res = [];
    var thisp = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in t)
      {
        var val = t[i]; // in case fun mutates this
        if (fun.call(thisp, val, i, t))
          res.push(val);
      }
    }

    return res;
  };
}
Function.prototype.partial = function () {
    var method = this, orig = arguments, orig_len = orig.length, i, j, args, arguments_len;
    return function () {
        if (!(arguments_len = arguments.length)) return method.apply(this, orig);
        for (args = [], i = 0, j = 0; i < orig_len; i += 1) args.push(orig[i] === undefined ? arguments[j++] : orig[i]);
        return method.apply(this, args.concat(Array.prototype.slice.call(arguments, j, arguments_len)));
    };
};
/*
    http://www.JSON.org/json2.js
    2010-08-25

    Public Domain.

    NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.

    See http://www.JSON.org/js.html


    This code should be minified before deployment.
    See http://javascript.crockford.com/jsmin.html

    USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO
    NOT CONTROL.


    This file creates a global JSON object containing two methods: stringify
    and parse.

        JSON.stringify(value, replacer, space)
            value       any JavaScript value, usually an object or array.

            replacer    an optional parameter that determines how object
                        values are stringified for objects. It can be a
                        function or an array of strings.

            space       an optional parameter that specifies the indentation
                        of nested structures. If it is omitted, the text will
                        be packed without extra whitespace. If it is a number,
                        it will specify the number of spaces to indent at each
                        level. If it is a string (such as '\t' or '&nbsp;'),
                        it contains the characters used to indent at each level.

            This method produces a JSON text from a JavaScript value.

            When an object value is found, if the object contains a toJSON
            method, its toJSON method will be called and the result will be
            stringified. A toJSON method does not serialize: it returns the
            value represented by the name/value pair that should be serialized,
            or undefined if nothing should be serialized. The toJSON method
            will be passed the key associated with the value, and this will be
            bound to the value

            For example, this would serialize Dates as ISO strings.

                Date.prototype.toJSON = function (key) {
                    function f(n) {
                        // Format integers to have at least two digits.
                        return n < 10 ? '0' + n : n;
                    }

                    return this.getUTCFullYear()   + '-' +
                         f(this.getUTCMonth() + 1) + '-' +
                         f(this.getUTCDate())      + 'T' +
                         f(this.getUTCHours())     + ':' +
                         f(this.getUTCMinutes())   + ':' +
                         f(this.getUTCSeconds())   + 'Z';
                };

            You can provide an optional replacer method. It will be passed the
            key and value of each member, with this bound to the containing
            object. The value that is returned from your method will be
            serialized. If your method returns undefined, then the member will
            be excluded from the serialization.

            If the replacer parameter is an array of strings, then it will be
            used to select the members to be serialized. It filters the results
            such that only members with keys listed in the replacer array are
            stringified.

            Values that do not have JSON representations, such as undefined or
            functions, will not be serialized. Such values in objects will be
            dropped; in arrays they will be replaced with null. You can use
            a replacer function to replace those with JSON values.
            JSON.stringify(undefined) returns undefined.

            The optional space parameter produces a stringification of the
            value that is filled with line breaks and indentation to make it
            easier to read.

            If the space parameter is a non-empty string, then that string will
            be used for indentation. If the space parameter is a number, then
            the indentation will be that many spaces.

            Example:

            text = JSON.stringify(['e', {pluribus: 'unum'}]);
            // text is '["e",{"pluribus":"unum"}]'


            text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\t');
            // text is '[\n\t"e",\n\t{\n\t\t"pluribus": "unum"\n\t}\n]'

            text = JSON.stringify([new Date()], function (key, value) {
                return this[key] instanceof Date ?
                    'Date(' + this[key] + ')' : value;
            });
            // text is '["Date(---current time---)"]'


        JSON.parse(text, reviver)
            This method parses a JSON text to produce an object or array.
            It can throw a SyntaxError exception.

            The optional reviver parameter is a function that can filter and
            transform the results. It receives each of the keys and values,
            and its return value is used instead of the original value.
            If it returns what it received, then the structure is not modified.
            If it returns undefined then the member is deleted.

            Example:

            // Parse the text. Values that look like ISO date strings will
            // be converted to Date objects.

            myData = JSON.parse(text, function (key, value) {
                var a;
                if (typeof value === 'string') {
                    a =
/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(value);
                    if (a) {
                        return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4],
                            +a[5], +a[6]));
                    }
                }
                return value;
            });

            myData = JSON.parse('["Date(09/09/2001)"]', function (key, value) {
                var d;
                if (typeof value === 'string' &&
                        value.slice(0, 5) === 'Date(' &&
                        value.slice(-1) === ')') {
                    d = new Date(value.slice(5, -1));
                    if (d) {
                        return d;
                    }
                }
                return value;
            });


    This is a reference implementation. You are free to copy, modify, or
    redistribute.
*/

/*jslint evil: true, strict: false */

/*members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply,
    call, charCodeAt, getUTCDate, getUTCFullYear, getUTCHours,
    getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join,
    lastIndex, length, parse, prototype, push, replace, slice, stringify,
    test, toJSON, toString, valueOf
*/


// Create a JSON object only if one does not already exist. We create the
// methods in a closure to avoid creating global variables.

if (!this.JSON) {
    this.JSON = {};
}

(function () {

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    if (typeof Date.prototype.toJSON !== 'function') {

        Date.prototype.toJSON = function (key) {

            return isFinite(this.valueOf()) ?
                   this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z' : null;
        };

        String.prototype.toJSON =
        Number.prototype.toJSON =
        Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };
    }

    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap,
        indent,
        meta = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        },
        rep;


    function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

        escapable.lastIndex = 0;
        return escapable.test(string) ?
            '"' + string.replace(escapable, function (a) {
                var c = meta[a];
                return typeof c === 'string' ? c :
                    '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
            }) + '"' :
            '"' + string + '"';
    }


    function str(key, holder) {

// Produce a string from holder[key].

        var i,          // The loop counter.
            k,          // The member key.
            v,          // The member value.
            length,
            mind = gap,
            partial,
            value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

        if (value && typeof value === 'object' &&
                typeof value.toJSON === 'function') {
            value = value.toJSON(key);
        }

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

// What happens next depends on the value's type.

        switch (typeof value) {
        case 'string':
            return quote(value);

        case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

            return isFinite(value) ? String(value) : 'null';

        case 'boolean':
        case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

            return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

        case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

            if (!value) {
                return 'null';
            }

// Make an array to hold the partial results of stringifying this object value.

            gap += indent;
            partial = [];

// Is the value an array?

            if (Object.prototype.toString.apply(value) === '[object Array]') {

// The value is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                length = value.length;
                for (i = 0; i < length; i += 1) {
                    partial[i] = str(i, value) || 'null';
                }

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

                v = partial.length === 0 ? '[]' :
                    gap ? '[\n' + gap +
                            partial.join(',\n' + gap) + '\n' +
                                mind + ']' :
                          '[' + partial.join(',') + ']';
                gap = mind;
                return v;
            }

// If the replacer is an array, use it to select the members to be stringified.

            if (rep && typeof rep === 'object') {
                length = rep.length;
                for (i = 0; i < length; i += 1) {
                    k = rep[i];
                    if (typeof k === 'string') {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            } else {

// Otherwise, iterate through all of the keys in the object.

                for (k in value) {
                    if (Object.hasOwnProperty.call(value, k)) {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            }

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

            v = partial.length === 0 ? '{}' :
                gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' +
                        mind + '}' : '{' + partial.join(',') + '}';
            gap = mind;
            return v;
        }
    }

// If the JSON object does not yet have a stringify method, give it one.

    if (typeof JSON.stringify !== 'function') {
        JSON.stringify = function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

            var i;
            gap = '';
            indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }

// If the space parameter is a string, it will be used as the indent string.

            } else if (typeof space === 'string') {
                indent = space;
            }

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

            rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                    (typeof replacer !== 'object' ||
                     typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

            return str('', {'': value});
        };
    }


// If the JSON object does not yet have a parse method, give it one.

    if (typeof JSON.parse !== 'function') {
        JSON.parse = function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

            var j;

            function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }


// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

            if (/^[\],:{}\s]*$/
.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
.replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
.replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

                return typeof reviver === 'function' ?
                    walk({'': j}, '') : j;
            }

// If the text is not JSON parseable, then a SyntaxError is thrown.

            throw new SyntaxError('JSON.parse');
        };
    }
}());

/**
 * Dual licensed under the Apache License 2.0 and the MIT license.
 * $Revision: 1536 $ $Date: 2010-11-09 19:30:34 +0100 (Tue, 09 Nov 2010) $
 */

if (typeof dojo !== 'undefined')
{
    dojo.provide('org.cometd');
}
else
{
    // Namespaces for the cometd implementation
    this.org = this.org || {};
    org.cometd = {};
}

org.cometd.JSON = {};
org.cometd.JSON.toJSON = org.cometd.JSON.fromJSON = function(object)
{
    throw 'Abstract';
};

org.cometd.Utils = {};

org.cometd.Utils.isString = function(value)
{
    if (value === undefined || value === null)
    {
        return false;
    }
    return typeof value === 'string' ||  value instanceof String;
};

org.cometd.Utils.isArray = function(value)
{
    if (value === undefined || value === null)
    {
        return false;
    }
    return value instanceof Array;
};

/**
 * Returns whether the given element is contained into the given array.
 * @param element the element to check presence for
 * @param array the array to check for the element presence
 * @return the index of the element, if present, or a negative index if the element is not present
 */
org.cometd.Utils.inArray = function(element, array)
{
    for (var i = 0; i < array.length; ++i)
    {
        if (element == array[i])
        {
            return i;
        }
    }
    return -1;
};

org.cometd.Utils.setTimeout = function(cometd, funktion, delay)
{
    return setTimeout(function()
    {
        try
        {
            funktion();
        }
        catch (x)
        {
            cometd._debug('Exception invoking timed function', funktion, x);
        }
    }, delay);
};

/**
 * A registry for transports used by the Cometd object.
 */
org.cometd.TransportRegistry = function()
{
    var _types = [];
    var _transports = {};

    this.getTransportTypes = function()
    {
        return _types.slice(0);
    };

    this.findTransportTypes = function(version, crossDomain, url)
    {
        var result = [];
        for (var i = 0; i < _types.length; ++i)
        {
            var type = _types[i];
            if (_transports[type].accept(version, crossDomain, url) === true)
            {
                result.push(type);
            }
        }
        return result;
    };

    this.negotiateTransport = function(types, version, crossDomain, url)
    {
        for (var i = 0; i < _types.length; ++i)
        {
            var type = _types[i];
            for (var j = 0; j < types.length; ++j)
            {
                if (type == types[j])
                {
                    var transport = _transports[type];
                    if (transport.accept(version, crossDomain, url) === true)
                    {
                        return transport;
                    }
                }
            }
        }
        return null;
    };

    this.add = function(type, transport, index)
    {
        var existing = false;
        for (var i = 0; i < _types.length; ++i)
        {
            if (_types[i] == type)
            {
                existing = true;
                break;
            }
        }

        if (!existing)
        {
            if (typeof index !== 'number')
            {
                _types.push(type);
            }
            else
            {
                _types.splice(index, 0, type);
            }
            _transports[type] = transport;
        }

        return !existing;
    };

    this.find = function(name)
    {
        for (var i = 0; i < _types.length; ++i)
        {
            if (_types[i] == type)
            {
                return _transports[type];
            }
        }
        return null;
    };

    this.remove = function(type)
    {
        for (var i = 0; i < _types.length; ++i)
        {
            if (_types[i] == type)
            {
                _types.splice(i, 1);
                var transport = _transports[type];
                delete _transports[type];
                return transport;
            }
        }
        return null;
    };

    this.reset = function()
    {
        for (var i = 0; i < _types.length; ++i)
        {
            _transports[_types[i]].reset();
        }
    };
};

/**
 * Base object with the common functionality for transports.
 */
org.cometd.Transport = function()
{
    var _type;
    var _cometd;

    /**
     * Function invoked just after a transport has been successfully registered.
     * @param type the type of transport (for example 'long-polling')
     * @param cometd the cometd object this transport has been registered to
     * @see #unregistered()
     */
    this.registered = function(type, cometd)
    {
        _type = type;
        _cometd = cometd;
    };

    /**
     * Function invoked just after a transport has been successfully unregistered.
     * @see #registered(type, cometd)
     */
    this.unregistered = function()
    {
        _type = null;
        _cometd = null;
    };

    this._debug = function()
    {
        _cometd._debug.apply(_cometd, arguments);
    };

    this.getConfiguration = function()
    {
        return _cometd.getConfiguration();
    };

    this.getAdvice = function()
    {
        return _cometd.getAdvice();
    };

    this.setTimeout = function(funktion, delay)
    {
        return org.cometd.Utils.setTimeout(_cometd, funktion, delay);
    };

    /**
     * Converts the given response into an array of bayeux messages
     * @param response the response to convert
     * @return an array of bayeux messages obtained by converting the response
     */
    this.convertToMessages = function (response)
    {
        if (org.cometd.Utils.isString(response))
        {
            try
            {
                return org.cometd.JSON.fromJSON(response);
            }
            catch(x)
            {
                this._debug('Could not convert to JSON the following string', '"' + response + '"');
                throw x;
            }
        }
        if (org.cometd.Utils.isArray(response))
        {
            return response;
        }
        if (response === undefined || response === null)
        {
            return [];
        }
        if (response instanceof Object)
        {
            return [response];
        }
        throw 'Conversion Error ' + response + ', typeof ' + (typeof response);
    };

    /**
     * Returns whether this transport can work for the given version and cross domain communication case.
     * @param version a string indicating the transport version
     * @param crossDomain a boolean indicating whether the communication is cross domain
     * @return true if this transport can work for the given version and cross domain communication case,
     * false otherwise
     */
    this.accept = function(version, crossDomain, url)
    {
        throw 'Abstract';
    };

    /**
     * Returns the type of this transport.
     * @see #registered(type, cometd)
     */
    this.getType = function()
    {
        return _type;
    };

    this.send = function(envelope, metaConnect)
    {
        throw 'Abstract';
    };

    this.reset = function()
    {
        this._debug('Transport', _type, 'reset');
    };

    this.abort = function()
    {
        this._debug('Transport', _type, 'aborted');
    };

    this.toString = function()
    {
        return this.getType();
    };
};

org.cometd.Transport.derive = function(baseObject)
{
    function F() {}
    F.prototype = baseObject;
    return new F();
};

/**
 * Base object with the common functionality for transports based on requests.
 * The key responsibility is to allow at most 2 outstanding requests to the server,
 * to avoid that requests are sent behind a long poll.
 * To achieve this, we have one reserved request for the long poll, and all other
 * requests are serialized one after the other.
 */
org.cometd.RequestTransport = function()
{
    var _super = new org.cometd.Transport();
    var _self = org.cometd.Transport.derive(_super);
    var _requestIds = 0;
    var _metaConnectRequest = null;
    var _requests = [];
    var _envelopes = [];

    function _coalesceEnvelopes(envelope)
    {
        while (_envelopes.length > 0)
        {
            var envelopeAndRequest = _envelopes[0];
            var newEnvelope = envelopeAndRequest[0];
            var newRequest = envelopeAndRequest[1];
            if (newEnvelope.url === envelope.url &&
                    newEnvelope.sync === envelope.sync)
            {
                _envelopes.shift();
                envelope.messages = envelope.messages.concat(newEnvelope.messages);
                this._debug('Coalesced', newEnvelope.messages.length, 'messages from request', newRequest.id);
                continue;
            }
            break;
        }
    }

    function _transportSend(envelope, request)
    {
        this.transportSend(envelope, request);
        request.expired = false;

        if (!envelope.sync)
        {
            var maxDelay = this.getConfiguration().maxNetworkDelay;
            var delay = maxDelay;
            if (request.metaConnect === true)
            {
                delay += this.getAdvice().timeout;
            }

            this._debug('Transport', this, 'waiting at most', delay, 'ms for the response, maxNetworkDelay =', maxDelay);

            var self = this;
            request.timeout = this.setTimeout(function()
            {
                request.expired = true;
                if (request.xhr)
                {
                    request.xhr.abort();
                }
                var errorMessage = 'Request ' + request.id + ' of transport ' + self.getType() + ' exceeded ' + delay + ' ms max network delay';
                self._debug(errorMessage);
                self.complete(request, false, request.metaConnect);
                envelope.onFailure(request.xhr, 'timeout', errorMessage);
            }, delay);
        }
    }

    function _queueSend(envelope)
    {
        var requestId = ++_requestIds;
        var request = {
            id: requestId,
            metaConnect: false
        };

        // Consider the metaConnect requests which should always be present
        if (_requests.length < this.getConfiguration().maxConnections - 1)
        {
            this._debug('Transport', this, 'sending request', requestId, envelope);
            _transportSend.call(this, envelope, request);
            _requests.push(request);
        }
        else
        {
            this._debug('Transport queueing request', requestId, envelope);
            _envelopes.push([envelope, request]);
        }
    }

    function _metaConnectComplete(request)
    {
        var requestId = request.id;
        this._debug('Transport', this, 'metaConnect complete', requestId);
        if (_metaConnectRequest !== null && _metaConnectRequest.id !== requestId)
        {
            throw 'Longpoll request mismatch, completing request ' + requestId;
        }

        // Reset metaConnect request
        _metaConnectRequest = null;
    }

    function _complete(request, success)
    {
        var index = org.cometd.Utils.inArray(request, _requests);
        // The index can be negative the request has been aborted
        if (index >= 0)
        {
            _requests.splice(index, 1);
        }

        if (_envelopes.length > 0)
        {
            var envelopeAndRequest = _envelopes.shift();
            var nextEnvelope = envelopeAndRequest[0];
            var nextRequest = envelopeAndRequest[1];
            this._debug('Transport dequeued request', nextRequest.id);
            if (success)
            {
                if (this.getConfiguration().autoBatch)
                {
                    _coalesceEnvelopes.call(this, nextEnvelope);
                }
                _queueSend.call(this, nextEnvelope);
                this._debug('Transport completed request', request.id, nextEnvelope);
            }
            else
            {
                // Keep the semantic of calling response callbacks asynchronously after the request
                var self = this;
                this.setTimeout(function()
                {
                    self.complete(nextRequest, false, nextRequest.metaConnect);
                    nextEnvelope.onFailure(nextRequest.xhr, 'error', 'Previous request failed');
                }, 0);
            }
        }
    }

    _self.complete = function(request, success, metaConnect)
    {
        if (metaConnect)
        {
            _metaConnectComplete.call(this, request);
        }
        else
        {
            _complete.call(this, request, success);
        }
    };

    /**
     * Performs the actual send depending on the transport type details.
     * @param envelope the envelope to send
     * @param request the request information
     */
    _self.transportSend = function(envelope, request)
    {
        throw 'Abstract';
    };

    _self.transportSuccess = function(envelope, request, responses)
    {
        if (!request.expired)
        {
            clearTimeout(request.timeout);
            this.complete(request, true, request.metaConnect);
            if (responses && responses.length > 0)
            {
                envelope.onSuccess(responses);
            }
            else
            {
                envelope.onFailure(request, 'Empty HTTP response');
            }
        }
    };

    _self.transportFailure = function(envelope, request, reason, exception)
    {
        if (!request.expired)
        {
            clearTimeout(request.timeout);
            this.complete(request, false, request.metaConnect);
            envelope.onFailure(request.xhr, reason, exception);
        }
    };

    function _metaConnectSend(envelope)
    {
        if (_metaConnectRequest !== null)
        {
            throw 'Concurrent metaConnect requests not allowed, request id=' + _metaConnectRequest.id + ' not yet completed';
        }

        var requestId = ++_requestIds;
        this._debug('Transport', this, 'metaConnect send', requestId, envelope);
        var request = {
            id: requestId,
            metaConnect: true
        };
        _transportSend.call(this, envelope, request);
        _metaConnectRequest = request;
    }

    _self.send = function(envelope, metaConnect)
    {
        if (metaConnect)
        {
            _metaConnectSend.call(this, envelope);
        }
        else
        {
            _queueSend.call(this, envelope);
        }
    };

    _self.abort = function()
    {
        _super.abort();
        for (var i = 0; i < _requests.length; ++i)
        {
            var request = _requests[i];
            this._debug('Aborting request', request);
            if (request.xhr)
            {
                request.xhr.abort();
            }
        }
        if (_metaConnectRequest)
        {
            this._debug('Aborting metaConnect request', _metaConnectRequest);
            if (_metaConnectRequest.xhr)
            {
                _metaConnectRequest.xhr.abort();
            }
        }
        this.reset();
    };

    _self.reset = function()
    {
        _super.reset();
        _metaConnectRequest = null;
        _requests = [];
        _envelopes = [];
    };

    return _self;
};

org.cometd.LongPollingTransport = function()
{
    var _super = new org.cometd.RequestTransport();
    var _self = org.cometd.Transport.derive(_super);
    // By default, support cross domain
    var _supportsCrossDomain = true;

    _self.accept = function(version, crossDomain, url)
    {
        return _supportsCrossDomain || !crossDomain;
    };

    _self.xhrSend = function(packet)
    {
        throw 'Abstract';
    };

    _self.transportSend = function(envelope, request)
    {
        var self = this;
        try
        {
            var sameStack = true;
            request.xhr = this.xhrSend({
                transport: this,
                url: envelope.url,
                sync: envelope.sync,
                headers: this.getConfiguration().requestHeaders,
                body: org.cometd.JSON.toJSON(envelope.messages),
                onSuccess: function(response)
                {
                    self._debug('Transport', self, 'received response', response);
                    var success = false;
                    try
                    {
                        var received = self.convertToMessages(response);
                        if (received.length === 0)
                        {
                            _supportsCrossDomain = false;
                            self.transportFailure(envelope, request, 'no response', null);
                        }
                        else
                        {
                            success = true;
                            self.transportSuccess(envelope, request, received);
                        }
                    }
                    catch(x)
                    {
                        self._debug(x);
                        if (!success)
                        {
                            _supportsCrossDomain = false;
                            self.transportFailure(envelope, request, 'bad response', x);
                        }
                    }
                },
                onError: function(reason, exception)
                {
                    _supportsCrossDomain = false;
                    if (sameStack)
                    {
                        // Keep the semantic of calling response callbacks asynchronously after the request
                        self.setTimeout(function()
                        {
                            self.transportFailure(envelope, request, reason, exception);
                        }, 0);
                    }
                    else
                    {
                        self.transportFailure(envelope, request, reason, exception);
                    }
                }
            });
            sameStack = false;
        }
        catch (x)
        {
            _supportsCrossDomain = false;
            // Keep the semantic of calling response callbacks asynchronously after the request
            this.setTimeout(function()
            {
                self.transportFailure(envelope, request, 'error', x);
            }, 0);
        }
    };

    _self.reset = function()
    {
        _super.reset();
        _supportsCrossDomain = true;
    };

    return _self;
};

org.cometd.CallbackPollingTransport = function()
{
    var _super = new org.cometd.RequestTransport();
    var _self = org.cometd.Transport.derive(_super);
    var _maxLength = 2000;

    _self.accept = function(version, crossDomain, url)
    {
        return true;
    };

    _self.jsonpSend = function(packet)
    {
        throw 'Abstract';
    };

    _self.transportSend = function(envelope, request)
    {
        // Microsoft Internet Explorer has a 2083 URL max length
        // We must ensure that we stay within that length
        var messages = org.cometd.JSON.toJSON(envelope.messages);
        // Encode the messages because all brackets, quotes, commas, colons, etc
        // present in the JSON will be URL encoded, taking many more characters
        var urlLength = envelope.url.length + encodeURI(messages).length;

        var self = this;

        // Let's stay on the safe side and use 2000 instead of 2083
        // also because we did not count few characters among which
        // the parameter name 'message' and the parameter 'jsonp',
        // which sum up to about 50 chars
        if (urlLength > _maxLength)
        {
            var x = envelope.messages.length > 1 ?
                    'Too many bayeux messages in the same batch resulting in message too big ' +
                    '(' + urlLength + ' bytes, max is ' + _maxLength + ') for transport ' + this.getType() :
                    'Bayeux message too big (' + urlLength + ' bytes, max is ' + _maxLength + ') ' +
                    'for transport ' + this.getType();
            // Keep the semantic of calling response callbacks asynchronously after the request
            this.setTimeout(function()
            {
                self.transportFailure(envelope, request, 'error', x);
            }, 0);
        }
        else
        {
            try
            {
                var sameStack = true;
                this.jsonpSend({
                    transport: this,
                    url: envelope.url,
                    sync: envelope.sync,
                    headers: this.getConfiguration().requestHeaders,
                    body: messages,
                    onSuccess: function(responses)
                    {
                        var success = false;
                        try
                        {
                            var received = self.convertToMessages(responses);
                            if (received.length === 0)
                            {
                                self.transportFailure(envelope, request, 'no response', null);
                            }
                            else
                            {
                                success=true;
                                self.transportSuccess(envelope, request, received);
                            }
                        }
                        catch (x)
                        {
                            self._debug(x);
                            if (!success)
                            {
                                self.transportFailure(envelope, request, 'bad response', x);
                            }
                        }
                    },
                    onError: function(reason, exception)
                    {
                        if (sameStack)
                        {
                            // Keep the semantic of calling response callbacks asynchronously after the request
                            self.setTimeout(function()
                            {
                                self.transportFailure(envelope, request, reason, exception);
                            }, 0);
                        }
                        else
                        {
                            self.transportFailure(envelope, request, reason, exception);
                        }
                    }
                });
                sameStack = false;
            }
            catch (xx)
            {
                // Keep the semantic of calling response callbacks asynchronously after the request
                this.setTimeout(function()
                {
                    self.transportFailure(envelope, request, 'error', xx);
                }, 0);
            }
        }
    };

    return _self;
};

org.cometd.WebSocketTransport = function()
{
    var OPENED = 1;
    var CLOSED = 2;

    var _super = new org.cometd.Transport();
    var _self = org.cometd.Transport.derive(_super);
    var _cometd;
    // By default, support WebSocket
    var _supportsWebSocket = true;
    var _webSocketSupported = false;
    var _state = CLOSED;
    var _timeouts = {};
    var _envelopes = {};
    var _webSocket;
    var _successCallback;

    _self.registered = function(type, cometd)
    {
        _super.registered(type, cometd);
        _cometd = cometd;
    };

    _self.accept = function(version, crossDomain, url)
    {
        // Using !! to return a boolean (and not the WebSocket object)
        return _supportsWebSocket && !!window.WebSocket && _cometd.websocketEnabled === true;
    };

    function _websocketSend(envelope, metaConnect)
    {
        try
        {
            var json = org.cometd.JSON.toJSON(envelope.messages);
            _webSocket.send(json);
            this._debug('Transport', this, 'sent', envelope, 'metaConnect =', metaConnect);

            // Manage the timeout waiting for the response
            var maxDelay = this.getConfiguration().maxNetworkDelay;
            var delay = maxDelay;
            if (metaConnect)
            {
                delay += this.getAdvice().timeout;
            }

            var messageIds = [];
            for (var i = 0; i < envelope.messages.length; ++i)
            {
                var message = envelope.messages[i];
                if (message.id)
                {
                    messageIds.push(message.id);
                    var self = this;
                    _timeouts[message.id] = this.setTimeout(function()
                    {
                        var errorMessage = 'Message ' + message.id + ' of transport ' + self.getType() + ' exceeded ' + delay + ' ms max network delay';
                        self._debug(errorMessage);

                        delete _timeouts[message.id];

                        for (var ids in _envelopes)
                        {
                            if (_envelopes[ids] === envelope)
                            {
                                delete _envelopes[ids];
                                break;
                            }
                        }
                        envelope.onFailure(_webSocket, 'timeout', errorMessage);
                    }, delay);
                }
            }

            this._debug('Transport', this, 'waiting at most', delay, ' ms for messages', messageIds, 'maxNetworkDelay =', maxDelay, ', timeouts:', org.cometd.JSON.toJSON(_timeouts));
        }
        catch (x)
        {
            // Keep the semantic of calling response callbacks asynchronously after the request
            this.setTimeout(function()
            {
                envelope.onFailure(_webSocket, 'error', x);
            }, 0);
        }
    }

    _self.onMessage = function(wsMessage)
    {
        this._debug('Transport', this, 'received websocket message', wsMessage);

        if (_state === OPENED)
        {
            var messages = this.convertToMessages(wsMessage.data);
            var messageIds = [];
            for (var i = 0; i < messages.length; ++i)
            {
                var message = messages[i];

                // Detect if the message is a response to a request we made.
                // If it's a meta message, for sure it's a response;
                // otherwise it's a publish message and publish responses lack the data field
                if (/^\/meta\//.test(message.channel) || message.data === undefined)
                {
                    if (message.id)
                    {
                        messageIds.push(message.id);

                        var timeout = _timeouts[message.id];
                        if (timeout)
                        {
                            clearTimeout(timeout);
                            delete _timeouts[message.id];
                            this._debug('Transport', this, 'removed timeout for message', message.id, ', timeouts:', org.cometd.JSON.toJSON(_timeouts));
                        }
                    }
                }

                if ('/meta/disconnect' === message.channel && message.successful)
                {
                    _webSocket.close();
                }
            }

            // Remove the envelope corresponding to the messages
            var removed = false;
            for (var j = 0; j < messageIds.length; ++j)
            {
                var id = messageIds[j];
                for (var key in _envelopes)
                {
                    var ids = key.split(',');
                    var index = org.cometd.Utils.inArray(id, ids);
                    if (index >= 0)
                    {
                        removed = true;
                        ids.splice(index, 1);
                        var envelope = _envelopes[key];
                        delete _envelopes[key];
                        if (ids.length > 0)
                        {
                            _envelopes[ids.join(',')] = envelope;
                        }
                        break;
                    }
                }
            }
            if (removed)
            {
                this._debug('Transport', this, 'removed envelope, envelopes:', org.cometd.JSON.toJSON(_envelopes));
            }

            _successCallback.call(this, messages);
        }
    };

    _self.onClose = function()
    {
        this._debug('Transport', this, 'closed', _webSocket);

        // Remember if we were able to connect
        // This close event could be due to server shutdown, and if it restarts we want to try websocket again
        _supportsWebSocket = _webSocketSupported;

        for (var id in _timeouts)
        {
            clearTimeout(_timeouts[id]);
            delete _timeouts[id];
        }

        for (var ids in _envelopes)
        {
            _envelopes[ids].onFailure(_webSocket, 'closed');
            delete _envelopes[ids];
        }

        _state = CLOSED;
    };

    _self.send = function(envelope, metaConnect)
    {
        this._debug('Transport', this, 'sending', envelope, 'metaConnect =', metaConnect);

        // Store the envelope in any case; if the websocket cannot be opened, we fail it in close()
        var messageIds = [];
        for (var i = 0; i < envelope.messages.length; ++i)
        {
            var message = envelope.messages[i];
            if (message.id)
            {
                messageIds.push(message.id);
            }
        }
        _envelopes[messageIds.join(',')] = envelope;
        this._debug('Transport', this, 'stored envelope, envelopes:', org.cometd.JSON.toJSON(_envelopes));

        if (_state === OPENED)
        {
            _websocketSend.call(this, envelope, metaConnect);
        }
        else
        {
            // Mangle the URL, changing the scheme from 'http' to 'ws'
            var url = envelope.url.replace(/^http/, 'ws');
            this._debug('Transport', this, 'connecting to URL', url);

            _webSocket = new window.WebSocket(url);
            var self = this;
            _webSocket.onopen = function()
            {
                self._debug('WebSocket opened', _webSocket);
                _webSocketSupported = true;
                _state = OPENED;
                // Store the success callback, which is independent from the envelope,
                // so that it can be used to notify arrival of messages.
                _successCallback = envelope.onSuccess;
                _websocketSend.call(self, envelope, metaConnect);
            };
            _webSocket.onclose = function()
            {
                self.onClose();
            };
            _webSocket.onmessage = function(message)
            {
                self.onMessage(message);
            };
        }
    };

    _self.reset = function()
    {
        _super.reset();
        if (_webSocket)
        {
            _webSocket.close();
        }
        _supportsWebSocket = true;
        _webSocketSupported = false;
        _state = CLOSED;
        _timeouts = {};
        _envelopes = {};
        _webSocket = null;
        _successCallback = null;
    };

    return _self;
};

/**
 * The constructor for a Cometd object, identified by an optional name.
 * The default name is the string 'default'.
 * In the rare case a page needs more than one Bayeux conversation,
 * a new instance can be created via:
 * <pre>
 * var bayeuxUrl2 = ...;
 * var cometd2 = new $.Cometd();
 * cometd2.init({url: bayeuxUrl2});
 * </pre>
 * @param name the optional name of this cometd object
 */
// IMPLEMENTATION NOTES:
// Be very careful in not changing the function order and pass this file every time through JSLint (http://jslint.com)
// The only implied globals must be "dojo", "org" and "window", and check that there are no "unused" warnings
// Failing to pass JSLint may result in shrinkers/minifiers to create an unusable file.
org.cometd.Cometd = function(name)
{
    var _cometd = this;
    var _name = name || 'default';
    var _crossDomain = false;
    var _transports = new org.cometd.TransportRegistry();
    var _transport;
    var _status = 'disconnected';
    var _messageId = 0;
    var _clientId = null;
    var _batch = 0;
    var _messageQueue = [];
    var _internalBatch = false;
    var _listeners = {};
    var _backoff = 0;
    var _scheduledSend = null;
    var _extensions = [];
    var _advice = {};
    var _handshakeProps;
    var _reestablish = false;
    var _connected = false;
    var _config = {
        maxConnections: 2,
        backoffIncrement: 1000,
        maxBackoff: 60000,
        logLevel: 'info',
        reverseIncomingExtensions: true,
        maxNetworkDelay: 10000,
        requestHeaders: {},
        appendMessageTypeToURL: true,
        autoBatch: false,
        advice: {
            timeout: 60000,
            interval: 0,
            reconnect: 'retry'
        }
    };

    /**
     * Mixes in the given objects into the target object by copying the properties.
     * @param deep if the copy must be deep
     * @param target the target object
     * @param objects the objects whose properties are copied into the target
     */
    function _mixin(deep, target, objects)
    {
        var result = target || {};

        // Skip first 2 parameters (deep and target), and loop over the others
        for (var i = 2; i < arguments.length; ++i)
        {
            var object = arguments[i];

            if (object === undefined || object === null)
            {
                continue;
            }

            for (var propName in object)
            {
                var prop = object[propName];

                // Avoid infinite loops
                if (prop === target)
                {
                    continue;
                }
                // Do not mixin undefined values
                if (prop === undefined)
                {
                    continue;
                }

                if (deep && typeof prop === 'object' && prop !== null)
                {
                    if (prop instanceof Array)
                    {
                        result[propName] = _mixin(deep, [], prop);
                    }
                    else
                    {
                        result[propName] = _mixin(deep, {}, prop);
                    }
                }
                else
                {
                    result[propName] = prop;
                }
            }
        }

        return result;
    }

    /**
     * This method is exposed as facility for extensions that may need to clone messages.
     */
    this._mixin = _mixin;

    function _isString(value)
    {
        return org.cometd.Utils.isString(value);
    }

    function _isFunction(value)
    {
        if (value === undefined || value === null)
        {
            return false;
        }
        return typeof value === 'function';
    }

    function _log(level, args)
    {
        if (window.console)
        {
            var logger = window.console[level];
            if (_isFunction(logger))
            {
                logger.apply(window.console, args);
            }
        }
    }

    function _warn()
    {
        _log('warn', arguments);
    }
    this._warn = _warn;

    function _info()
    {
        if (_config.logLevel != 'warn')
        {
            _log('info', arguments);
        }
    }
    this._info = _info;

    function _debug()
    {
        if (_config.logLevel == 'debug')
        {
            _log('debug', arguments);
        }
    }
    this._debug = _debug;

    function _configure(configuration)
    {
        _debug('Configuring cometd object with', configuration);
        // Support old style param, where only the Bayeux server URL was passed
        if (_isString(configuration))
        {
            configuration = { url: configuration };
        }
        if (!configuration)
        {
            configuration = {};
        }

        _config = _mixin(false, _config, configuration);

        if (!_config.url)
        {
            throw 'Missing required configuration parameter \'url\' specifying the Bayeux server URL';
        }

        // Check if we're cross domain
        // [1] = protocol:, [2] = //host:port, [3] = host:port, [4] = host, [5] = :port, [6] = port, [7] = uri, [8] = rest
        var urlParts = /(^https?:)?(\/\/(([^:\/\?#]+)(:(\d+))?))?([^\?#]*)(.*)?/.exec(_config.url);
        _crossDomain = urlParts[3] && urlParts[3] != window.location.host;

        // Check if appending extra path is supported
        if (_config.appendMessageTypeToURL)
        {
            if (urlParts[8] !== undefined && urlParts[8].length > 0)
            {
                _info('Appending message type to URI ' + urlParts[7] + urlParts[8] + ' is not supported, disabling \'appendMessageTypeToURL\' configuration');
                _config.appendMessageTypeToURL = false;
            }
            else
            {
                var uriSegments = urlParts[7].split('/');
                var lastSegmentIndex = uriSegments.length - 1;
                if (urlParts[7].match(/\/$/))
                {
                    lastSegmentIndex -= 1;
                }
                if (uriSegments[lastSegmentIndex].indexOf('.') >= 0)
                {
                    // Very likely the CometD servlet's URL pattern is mapped to an extension, such as *.cometd
                    // It will be difficult to add the extra path in this case
                    _info('Appending message type to URI ' + urlParts[7] + ' is not supported, disabling \'appendMessageTypeToURL\' configuration');
                    _config.appendMessageTypeToURL = false;
                }
            }
        }
    }

    function _clearSubscriptions()
    {
        for (var channel in _listeners)
        {
            var subscriptions = _listeners[channel];
            for (var i = 0; i < subscriptions.length; ++i)
            {
                var subscription = subscriptions[i];
                if (subscription && !subscription.listener)
                {
                    delete subscriptions[i];
                    _debug('Removed subscription', subscription, 'for channel', channel);
                }
            }
        }
    }

    function _setStatus(newStatus)
    {
        if (_status != newStatus)
        {
            _debug('Status', _status, '->', newStatus);
            _status = newStatus;
        }
    }

    function _isDisconnected()
    {
        return _status == 'disconnecting' || _status == 'disconnected';
    }

    function _nextMessageId()
    {
        return ++_messageId;
    }

    function _applyExtension(scope, callback, name, message, outgoing)
    {
        try
        {
            return callback.call(scope, message);
        }
        catch (x)
        {
            _debug('Exception during execution of extension', name, x);
            var exceptionCallback = _cometd.onExtensionException;
            if (_isFunction(exceptionCallback))
            {
                _debug('Invoking extension exception callback', name, x);
                try
                {
                    exceptionCallback.call(_cometd, x, name, outgoing, message);
                }
                catch(xx)
                {
                    _info('Exception during execution of exception callback in extension', name, xx);
                }
            }
            return message;
        }
    }

    function _applyIncomingExtensions(message)
    {
        for (var i = 0; i < _extensions.length; ++i)
        {
            if (message === undefined || message === null)
            {
                break;
            }

            var index = _config.reverseIncomingExtensions ? _extensions.length - 1 - i : i;
            var extension = _extensions[index];
            var callback = extension.extension.incoming;
            if (_isFunction(callback))
            {
                var result = _applyExtension(extension.extension, callback, extension.name, message, false);
                message = result === undefined ? message : result;
            }
        }
        return message;
    }

    function _applyOutgoingExtensions(message)
    {
        for (var i = 0; i < _extensions.length; ++i)
        {
            if (message === undefined || message === null)
            {
                break;
            }

            var extension = _extensions[i];
            var callback = extension.extension.outgoing;
            if (_isFunction(callback))
            {
                var result = _applyExtension(extension.extension, callback, extension.name, message, true);
                message = result === undefined ? message : result;
            }
        }
        return message;
    }

    function _notify(channel, message)
    {
        var subscriptions = _listeners[channel];
        if (subscriptions && subscriptions.length > 0)
        {
            for (var i = 0; i < subscriptions.length; ++i)
            {
                var subscription = subscriptions[i];
                // Subscriptions may come and go, so the array may have 'holes'
                if (subscription)
                {
                    try
                    {
                        subscription.callback.call(subscription.scope, message);
                    }
                    catch (x)
                    {
                        _debug('Exception during notification', subscription, message, x);
                        var listenerCallback = _cometd.onListenerException;
                        if (_isFunction(listenerCallback))
                        {
                            _debug('Invoking listener exception callback', subscription, x);
                            try
                            {
                                listenerCallback.call(_cometd, x, subscription.handle, subscription.listener, message);
                            }
                            catch (xx)
                            {
                                _info('Exception during execution of listener callback', subscription, xx);
                            }
                        }
                    }
                }
            }
        }
    }

    function _notifyListeners(channel, message)
    {
        // Notify direct listeners
        _notify(channel, message);

        // Notify the globbing listeners
        var channelParts = channel.split('/');
        var last = channelParts.length - 1;
        for (var i = last; i > 0; --i)
        {
            var channelPart = channelParts.slice(0, i).join('/') + '/*';
            // We don't want to notify /foo/* if the channel is /foo/bar/baz,
            // so we stop at the first non recursive globbing
            if (i == last)
            {
                _notify(channelPart, message);
            }
            // Add the recursive globber and notify
            channelPart += '*';
            _notify(channelPart, message);
        }
    }

    function _cancelDelayedSend()
    {
        if (_scheduledSend !== null)
        {
            clearTimeout(_scheduledSend);
        }
        _scheduledSend = null;
    }

    function _delayedSend(operation)
    {
        _cancelDelayedSend();
        var delay = _advice.interval + _backoff;
        _debug('Function scheduled in', delay, 'ms, interval =', _advice.interval, 'backoff =', _backoff, operation);
        _scheduledSend = org.cometd.Utils.setTimeout(_cometd, operation, delay);
    }

    // Needed to break cyclic dependencies between function definitions
    var _handleMessages;
    var _handleFailure;

    /**
     * Delivers the messages to the CometD server
     * @param messages the array of messages to send
     * @param longpoll true if this send is a long poll
     */
    function _send(sync, messages, longpoll, extraPath)
    {
        // We must be sure that the messages have a clientId.
        // This is not guaranteed since the handshake may take time to return
        // (and hence the clientId is not known yet) and the application
        // may create other messages.
        for (var i = 0; i < messages.length; ++i)
        {
            var message = messages[i];
            message.id = '' + _nextMessageId();
            if (_clientId)
            {
                message.clientId = _clientId;
            }
            message = _applyOutgoingExtensions(message);
            if (message !== undefined && message !== null)
            {
                messages[i] = message;
            }
            else
            {
                messages.splice(i--, 1);
            }
        }
        if (messages.length === 0)
        {
            return;
        }

        var url = _config.url;
        if (_config.appendMessageTypeToURL)
        {
            // If url does not end with '/', then append it
            if (!url.match(/\/$/))
            {
                url = url + '/';
            }
            if (extraPath)
            {
                url = url + extraPath;
            }
        }

        var envelope = {
            url: url,
            sync: sync,
            messages: messages,
            onSuccess: function(rcvdMessages)
            {
                try
                {
                    _handleMessages.call(_cometd, rcvdMessages);
                }
                catch (x)
                {
                    _debug('Exception during handling of messages', x);
                }
            },
            onFailure: function(conduit, reason, exception)
            {
                try
                {
                    _handleFailure.call(_cometd, conduit, messages, reason, exception);
                }
                catch (x)
                {
                    _debug('Exception during handling of failure', x);
                }
            }
        };
        _debug('Send, sync =', sync, envelope);
        _transport.send(envelope, longpoll);
    }

    function _queueSend(message)
    {
        if (_batch > 0 || _internalBatch === true)
        {
            _messageQueue.push(message);
        }
        else
        {
            _send(false, [message], false);
        }
    }

    /**
     * Sends a complete bayeux message.
     * This method is exposed as a public so that extensions may use it
     * to send bayeux message directly, for example in case of re-sending
     * messages that have already been sent but that for some reason must
     * be resent.
     */
    this.send = _queueSend;

    function _resetBackoff()
    {
        _backoff = 0;
    }

    function _increaseBackoff()
    {
        if (_backoff < _config.maxBackoff)
        {
            _backoff += _config.backoffIncrement;
        }
    }

    /**
     * Starts a the batch of messages to be sent in a single request.
     * @see #_endBatch(sendMessages)
     */
    function _startBatch()
    {
        ++_batch;
    }

    function _flushBatch()
    {
        var messages = _messageQueue;
        _messageQueue = [];
        if (messages.length > 0)
        {
            _send(false, messages, false);
        }
    }

    /**
     * Ends the batch of messages to be sent in a single request,
     * optionally sending messages present in the message queue depending
     * on the given argument.
     * @see #_startBatch()
     */
    function _endBatch()
    {
        --_batch;
        if (_batch < 0)
        {
            throw 'Calls to startBatch() and endBatch() are not paired';
        }

        if (_batch === 0 && !_isDisconnected() && !_internalBatch)
        {
            _flushBatch();
        }
    }

    /**
     * Sends the connect message
     */
    function _connect()
    {
        if (!_isDisconnected())
        {
            var message = {
                channel: '/meta/connect',
                connectionType: _transport.getType()
            };

            // In case of reload or temporary loss of connection
            // we want the next successful connect to return immediately
            // instead of being held by the server, so that connect listeners
            // can be notified that the connection has been re-established
            if (!_connected)
            {
                message.advice = { timeout: 0 };
            }

            _setStatus('connecting');
            _debug('Connect sent', message, org.cometd.JSON.toJSON(message));
            _send(false, [message], true, 'connect');
            _setStatus('connected');
        }
    }

    function _delayedConnect()
    {
        _setStatus('connecting');
        _delayedSend(function()
        {
            _connect();
        });
    }

    function _updateAdvice(newAdvice)
    {
        if (newAdvice)
        {
            _advice = _mixin(false, {}, _config.advice, newAdvice);
            _debug('New advice', _advice, org.cometd.JSON.toJSON(_advice));
        }
    }

    /**
     * Sends the initial handshake message
     */
    function _handshake(handshakeProps)
    {
        _clientId = null;

        _clearSubscriptions();

        // Reset the transports if we're not retrying the handshake
        if (_isDisconnected())
        {
            _transports.reset();
        }

        if (_isDisconnected())
        {
            _updateAdvice(_config.advice);
        }
        else
        {
            // We are retrying the handshake, either because another handshake failed
            // and we're backing off, or because the server timed us out and asks us to
            // re-handshake: in both cases, make sure that if the handshake succeeds
            // the next action is a connect.
            _updateAdvice(_mixin(false, _advice, {reconnect: 'retry'}));
        }

        _batch = 0;

        // Mark the start of an internal batch.
        // This is needed because handshake and connect are async.
        // It may happen that the application calls init() then subscribe()
        // and the subscribe message is sent before the connect message, if
        // the subscribe message is not held until the connect message is sent.
        // So here we start a batch to hold temporarily any message until
        // the connection is fully established.
        _internalBatch = true;

        // Save the properties provided by the user, so that
        // we can reuse them during automatic re-handshake
        _handshakeProps = handshakeProps;

        var version = '1.0';

        // Figure out the transports to send to the server
        var transportTypes = _transports.findTransportTypes(version, _crossDomain, _config.url);

        var bayeuxMessage = {
            version: version,
            minimumVersion: '0.9',
            channel: '/meta/handshake',
            supportedConnectionTypes: transportTypes,
            advice: {
                timeout: _advice.timeout,
                interval: _advice.interval
            }
        };
        // Do not allow the user to mess with the required properties,
        // so merge first the user properties and *then* the bayeux message
        var message = _mixin(false, {}, _handshakeProps, bayeuxMessage);

        // Pick up the first available transport as initial transport
        // since we don't know if the server supports it
        _transport = _transports.negotiateTransport(transportTypes, version, _crossDomain, _config.url);
        _debug('Initial transport is', _transport.getType(), _transport);

        // We started a batch to hold the application messages,
        // so here we must bypass it and send immediately.
        _setStatus('handshaking');
        _debug('Handshake sent', message);
        _send(false, [message], false, 'handshake');
    }

    function _delayedHandshake()
    {
        _setStatus('handshaking');

        // We will call _handshake() which will reset _clientId, but we want to avoid
        // that between the end of this method and the call to _handshake() someone may
        // call publish() (or other methods that call _queueSend()).
        _internalBatch = true;

        _delayedSend(function()
        {
            _handshake(_handshakeProps);
        });
    }

    function _failHandshake(message)
    {
        _notifyListeners('/meta/handshake', message);
        _notifyListeners('/meta/unsuccessful', message);

        // Only try again if we haven't been disconnected and
        // the advice permits us to retry the handshake
        var retry = !_isDisconnected() && _advice.reconnect != 'none';
        if (retry)
        {
            _increaseBackoff();
            _delayedHandshake();
        }
        else
        {
            _resetBackoff();
            _setStatus('disconnected');
        }
    }

    function _handshakeResponse(message)
    {
        if (message.successful)
        {
            // Save clientId, figure out transport, then follow the advice to connect
            _clientId = message.clientId;

            var newTransport = _transports.negotiateTransport(message.supportedConnectionTypes, message.version, _crossDomain, _config.url);
            if (newTransport === null)
            {
                throw 'Could not negotiate transport with server; client ' +
                      _transports.findTransportTypes(message.version, _crossDomain, _config.url) +
                      ', server ' + message.supportedConnectionTypes;
            }
            else if (_transport != newTransport)
            {
                _debug('Transport', _transport, '->', newTransport);
                _transport = newTransport;
            }

            // End the internal batch and allow held messages from the application
            // to go to the server (see _handshake() where we start the internal batch).
            _internalBatch = false;
            _flushBatch();

            // Here the new transport is in place, as well as the clientId, so
            // the listeners can perform a publish() if they want.
            // Notify the listeners before the connect below.
            message.reestablish = _reestablish;
            _reestablish = true;
            _notifyListeners('/meta/handshake', message);

            var action = _isDisconnected() ? 'none' : _advice.reconnect;
            switch (action)
            {
                case 'retry':
                    _resetBackoff();
                    _delayedConnect();
                    break;
                case 'none':
                    _resetBackoff();
                    _setStatus('disconnected');
                    break;
                default:
                    throw 'Unrecognized advice action ' + action;
            }
        }
        else
        {
            _failHandshake(message);
        }
    }

    function _handshakeFailure(xhr, message)
    {
        _failHandshake({
            successful: false,
            failure: true,
            channel: '/meta/handshake',
            request: message,
            xhr: xhr,
            advice: {
                reconnect: 'retry',
                interval: _backoff
            }
        });
    }

    function _failConnect(message)
    {
        // Notify the listeners after the status change but before the next action
        _notifyListeners('/meta/connect', message);
        _notifyListeners('/meta/unsuccessful', message);

        // This may happen when the server crashed, the current clientId
        // will be invalid, and the server will ask to handshake again
        // Listeners can call disconnect(), so check the state after they run
        var action = _isDisconnected() ? 'none' : _advice.reconnect;
        switch (action)
        {
            case 'retry':
                _increaseBackoff();
                _delayedConnect();
                break;
            case 'handshake':
                _resetBackoff();
                _delayedHandshake();
                break;
            case 'none':
                _resetBackoff();
                _setStatus('disconnected');
                break;
            default:
                throw 'Unrecognized advice action' + action;
        }
    }

    function _connectResponse(message)
    {
        _connected = message.successful;

        if (_connected)
        {
            _notifyListeners('/meta/connect', message);

            // Normally, the advice will say "reconnect: 'retry', interval: 0"
            // and the server will hold the request, so when a response returns
            // we immediately call the server again (long polling)
            // Listeners can call disconnect(), so check the state after they run
            var action1 = _isDisconnected() ? 'none' : _advice.reconnect;
            switch (action1)
            {
                case 'retry':
                    _resetBackoff();
                    _delayedConnect();
                    break;
                case 'none':
                    _resetBackoff();
                    _setStatus('disconnected');
                    break;
                default:
                    throw 'Unrecognized advice action ' + action1;
            }
        }
        else
        {
            _failConnect(message);
        }
    }

    function _connectFailure(xhr, message)
    {
        _connected = false;
        _failConnect({
            successful: false,
            failure: true,
            channel: '/meta/connect',
            request: message,
            xhr: xhr,
            advice: {
                reconnect: 'retry',
                interval: _backoff
            }
        });
    }

    function _disconnect(abort)
    {
        _cancelDelayedSend();
        if (abort)
        {
            _transport.abort();
        }
        _clientId = null;
        _setStatus('disconnected');
        _batch = 0;
        _messageQueue = [];
        _resetBackoff();
    }

    function _failDisconnect(message)
    {
        _disconnect(true);
        _notifyListeners('/meta/disconnect', message);
        _notifyListeners('/meta/unsuccessful', message);
    }

    function _disconnectResponse(message)
    {
        if (message.successful)
        {
            _disconnect(false);
            _notifyListeners('/meta/disconnect', message);
        }
        else
        {
            _failDisconnect(message);
        }
    }

    function _disconnectFailure(xhr, message)
    {
        _failDisconnect({
            successful: false,
            failure: true,
            channel: '/meta/disconnect',
            request: message,
            xhr: xhr,
            advice: {
                reconnect: 'none',
                interval: 0
            }
        });
    }

    function _failSubscribe(message)
    {
        _notifyListeners('/meta/subscribe', message);
        _notifyListeners('/meta/unsuccessful', message);
    }

    function _subscribeResponse(message)
    {
        if (message.successful)
        {
            _notifyListeners('/meta/subscribe', message);
        }
        else
        {
            _failSubscribe(message);
        }
    }

    function _subscribeFailure(xhr, message)
    {
        _failSubscribe({
            successful: false,
            failure: true,
            channel: '/meta/subscribe',
            request: message,
            xhr: xhr,
            advice: {
                reconnect: 'none',
                interval: 0
            }
        });
    }

    function _failUnsubscribe(message)
    {
        _notifyListeners('/meta/unsubscribe', message);
        _notifyListeners('/meta/unsuccessful', message);
    }

    function _unsubscribeResponse(message)
    {
        if (message.successful)
        {
            _notifyListeners('/meta/unsubscribe', message);
        }
        else
        {
            _failUnsubscribe(message);
        }
    }

    function _unsubscribeFailure(xhr, message)
    {
        _failUnsubscribe({
            successful: false,
            failure: true,
            channel: '/meta/unsubscribe',
            request: message,
            xhr: xhr,
            advice: {
                reconnect: 'none',
                interval: 0
            }
        });
    }

    function _failMessage(message)
    {
        _notifyListeners('/meta/publish', message);
        _notifyListeners('/meta/unsuccessful', message);
    }

    function _messageResponse(message)
    {
        if (message.successful === undefined)
        {
            if (message.data)
            {
                // It is a plain message, and not a bayeux meta message
                _notifyListeners(message.channel, message);
            }
            else
            {
                _debug('Unknown message', message);
            }
        }
        else
        {
            if (message.successful)
            {
                _notifyListeners('/meta/publish', message);
            }
            else
            {
                _failMessage(message);
            }
        }
    }

    function _messageFailure(xhr, message)
    {
        _failMessage({
            successful: false,
            failure: true,
            channel: message.channel,
            request: message,
            xhr: xhr,
            advice: {
                reconnect: 'none',
                interval: 0
            }
        });
    }

    function _receive(message)
    {
        message = _applyIncomingExtensions(message);
        if (message === undefined || message === null)
        {
            return;
        }

        _updateAdvice(message.advice);

        var channel = message.channel;
        switch (channel)
        {
            case '/meta/handshake':
                _handshakeResponse(message);
                break;
            case '/meta/connect':
                _connectResponse(message);
                break;
            case '/meta/disconnect':
                _disconnectResponse(message);
                break;
            case '/meta/subscribe':
                _subscribeResponse(message);
                break;
            case '/meta/unsubscribe':
                _unsubscribeResponse(message);
                break;
            default:
                _messageResponse(message);
                break;
        }
    }

    /**
     * Receives a message.
     * This method is exposed as a public so that extensions may inject
     * messages simulating that they had been received.
     */
    this.receive = _receive;

    _handleMessages = function _handleMessages(rcvdMessages)
    {
        _debug('Received', rcvdMessages, org.cometd.JSON.toJSON(rcvdMessages));

        for (var i = 0; i < rcvdMessages.length; ++i)
        {
            var message = rcvdMessages[i];
            _receive(message);
        }
    };

    _handleFailure = function _handleFailure(conduit, messages, reason, exception)
    {
        _debug('handleFailure', conduit, messages, reason, exception);

        for (var i = 0; i < messages.length; ++i)
        {
            var message = messages[i];
            var channel = message.channel;
            switch (channel)
            {
                case '/meta/handshake':
                    _handshakeFailure(conduit, message);
                    break;
                case '/meta/connect':
                    _connectFailure(conduit, message);
                    break;
                case '/meta/disconnect':
                    _disconnectFailure(conduit, message);
                    break;
                case '/meta/subscribe':
                    _subscribeFailure(conduit, message);
                    break;
                case '/meta/unsubscribe':
                    _unsubscribeFailure(conduit, message);
                    break;
                default:
                    _messageFailure(conduit, message);
                    break;
            }
        }
    };

    function _hasSubscriptions(channel)
    {
        var subscriptions = _listeners[channel];
        if (subscriptions)
        {
            for (var i = 0; i < subscriptions.length; ++i)
            {
                if (subscriptions[i])
                {
                    return true;
                }
            }
        }
        return false;
    }

    function _resolveScopedCallback(scope, callback)
    {
        var delegate = {
            scope: scope,
            method: callback
        };
        if (_isFunction(scope))
        {
            delegate.scope = undefined;
            delegate.method = scope;
        }
        else
        {
            if (_isString(callback))
            {
                if (!scope)
                {
                    throw 'Invalid scope ' + scope;
                }
                delegate.method = scope[callback];
                if (!_isFunction(delegate.method))
                {
                    throw 'Invalid callback ' + callback + ' for scope ' + scope;
                }
            }
            else if (!_isFunction(callback))
            {
                throw 'Invalid callback ' + callback;
            }
        }
        return delegate;
    }

    function _addListener(channel, scope, callback, isListener)
    {
        // The data structure is a map<channel, subscription[]>, where each subscription
        // holds the callback to be called and its scope.

        var delegate = _resolveScopedCallback(scope, callback);
        _debug('Adding listener on', channel, 'with scope', delegate.scope, 'and callback', delegate.method);

        var subscription = {
            channel: channel,
            scope: delegate.scope,
            callback: delegate.method,
            listener: isListener
        };

        var subscriptions = _listeners[channel];
        if (!subscriptions)
        {
            subscriptions = [];
            _listeners[channel] = subscriptions;
        }

        // Pushing onto an array appends at the end and returns the id associated with the element increased by 1.
        // Note that if:
        // a.push('a'); var hb=a.push('b'); delete a[hb-1]; var hc=a.push('c');
        // then:
        // hc==3, a.join()=='a',,'c', a.length==3
        var subscriptionID = subscriptions.push(subscription) - 1;
        subscription.id = subscriptionID;
        subscription.handle = [channel, subscriptionID];

        _debug('Added listener', subscription, 'for channel', channel, 'having id =', subscriptionID);

        // The subscription to allow removal of the listener is made of the channel and the index
        return subscription.handle;
    }

    function _removeListener(subscription)
    {
        var subscriptions = _listeners[subscription[0]];
        if (subscriptions)
        {
            delete subscriptions[subscription[1]];
            _debug('Removed listener', subscription);
        }
    }

    //
    // PUBLIC API
    //

    /**
     * Registers the given transport under the given transport type.
     * The optional index parameter specifies the "priority" at which the
     * transport is registered (where 0 is the max priority).
     * If a transport with the same type is already registered, this function
     * does nothing and returns false.
     * @param type the transport type
     * @param transport the transport object
     * @param index the index at which this transport is to be registered
     * @return true if the transport has been registered, false otherwise
     * @see #unregisterTransport(type)
     */
    this.registerTransport = function(type, transport, index)
    {
        var result = _transports.add(type, transport, index);
        if (result)
        {
            _debug('Registered transport', type);

            if (_isFunction(transport.registered))
            {
                transport.registered(type, this);
            }
        }
        return result;
    };

    /**
     * @return an array of all registered transport types
     */
    this.getTransportTypes = function()
    {
        return _transports.getTransportTypes();
    };

    /**
     * Unregisters the transport with the given transport type.
     * @param type the transport type to unregister
     * @return the transport that has been unregistered,
     * or null if no transport was previously registered under the given transport type
     */
    this.unregisterTransport = function(type)
    {
        var transport = _transports.remove(type);
        if (transport !== null)
        {
            _debug('Unregistered transport', type);

            if (_isFunction(transport.unregistered))
            {
                transport.unregistered();
            }
        }
        return transport;
    };

    this.findTransport = function(name)
    {
        return _transports.find(name);
    };

    /**
     * Configures the initial Bayeux communication with the Bayeux server.
     * Configuration is passed via an object that must contain a mandatory field <code>url</code>
     * of type string containing the URL of the Bayeux server.
     * @param configuration the configuration object
     */
    this.configure = function(configuration)
    {
        _configure.call(this, configuration);
    };

    /**
     * Configures and establishes the Bayeux communication with the Bayeux server
     * via a handshake and a subsequent connect.
     * @param configuration the configuration object
     * @param handshakeProps an object to be merged with the handshake message
     * @see #configure(configuration)
     * @see #handshake(handshakeProps)
     */
    this.init = function(configuration, handshakeProps)
    {
        this.configure(configuration);
        this.handshake(handshakeProps);
    };

    /**
     * Establishes the Bayeux communication with the Bayeux server
     * via a handshake and a subsequent connect.
     * @param handshakeProps an object to be merged with the handshake message
     */
    this.handshake = function(handshakeProps)
    {
        _setStatus('disconnected');
        _reestablish = false;
        _handshake(handshakeProps);
    };

    /**
     * Disconnects from the Bayeux server.
     * It is possible to suggest to attempt a synchronous disconnect, but this feature
     * may only be available in certain transports (for example, long-polling may support
     * it, callback-polling certainly does not).
     * @param sync whether attempt to perform a synchronous disconnect
     * @param disconnectProps an object to be merged with the disconnect message
     */
    this.disconnect = function(sync, disconnectProps)
    {
        if (_isDisconnected())
        {
            return;
        }

        if (disconnectProps === undefined)
        {
            if (typeof sync !== 'boolean')
            {
                disconnectProps = sync;
                sync = false;
            }
        }

        var bayeuxMessage = {
            channel: '/meta/disconnect'
        };
        var message = _mixin(false, {}, disconnectProps, bayeuxMessage);
        _setStatus('disconnecting');
        _send(sync === true, [message], false, 'disconnect');
    };

    /**
     * Marks the start of a batch of application messages to be sent to the server
     * in a single request, obtaining a single response containing (possibly) many
     * application reply messages.
     * Messages are held in a queue and not sent until {@link #endBatch()} is called.
     * If startBatch() is called multiple times, then an equal number of endBatch()
     * calls must be made to close and send the batch of messages.
     * @see #endBatch()
     */
    this.startBatch = function()
    {
        _startBatch();
    };

    /**
     * Marks the end of a batch of application messages to be sent to the server
     * in a single request.
     * @see #startBatch()
     */
    this.endBatch = function()
    {
        _endBatch();
    };

    /**
     * Executes the given callback in the given scope, surrounded by a {@link #startBatch()}
     * and {@link #endBatch()} calls.
     * @param scope the scope of the callback, may be omitted
     * @param callback the callback to be executed within {@link #startBatch()} and {@link #endBatch()} calls
     */
    this.batch = function(scope, callback)
    {
        var delegate = _resolveScopedCallback(scope, callback);
        this.startBatch();
        try
        {
            delegate.method.call(delegate.scope);
            this.endBatch();
        }
        catch (x)
        {
            _debug('Exception during execution of batch', x);
            this.endBatch();
            throw x;
        }
    };

    /**
     * Adds a listener for bayeux messages, performing the given callback in the given scope
     * when a message for the given channel arrives.
     * @param channel the channel the listener is interested to
     * @param scope the scope of the callback, may be omitted
     * @param callback the callback to call when a message is sent to the channel
     * @returns the subscription handle to be passed to {@link #removeListener(object)}
     * @see #removeListener(subscription)
     */
    this.addListener = function(channel, scope, callback)
    {
        if (arguments.length < 2)
        {
            throw 'Illegal arguments number: required 2, got ' + arguments.length;
        }
        if (!_isString(channel))
        {
            throw 'Illegal argument type: channel must be a string';
        }

        return _addListener(channel, scope, callback, true);
    };

    /**
     * Removes the subscription obtained with a call to {@link #addListener(string, object, function)}.
     * @param subscription the subscription to unsubscribe.
     * @see #addListener(channel, scope, callback)
     */
    this.removeListener = function(subscription)
    {
        if (!org.cometd.Utils.isArray(subscription))
        {
            throw 'Invalid argument: expected subscription, not ' + subscription;
        }

        _removeListener(subscription);
    };

    /**
     * Removes all listeners registered with {@link #addListener(channel, scope, callback)} or
     * {@link #subscribe(channel, scope, callback)}.
     */
    this.clearListeners = function()
    {
        _listeners = {};
    };

    /**
     * Subscribes to the given channel, performing the given callback in the given scope
     * when a message for the channel arrives.
     * @param channel the channel to subscribe to
     * @param scope the scope of the callback, may be omitted
     * @param callback the callback to call when a message is sent to the channel
     * @param subscribeProps an object to be merged with the subscribe message
     * @return the subscription handle to be passed to {@link #unsubscribe(object)}
     */
    this.subscribe = function(channel, scope, callback, subscribeProps)
    {
        if (arguments.length < 2)
        {
            throw 'Illegal arguments number: required 2, got ' + arguments.length;
        }
        if (!_isString(channel))
        {
            throw 'Illegal argument type: channel must be a string';
        }
        if (_isDisconnected())
        {
            throw 'Illegal state: already disconnected';
        }

        // Normalize arguments
        if (_isFunction(scope))
        {
            subscribeProps = callback;
            callback = scope;
            scope = undefined;
        }

        // Only send the message to the server if this clientId has not yet subscribed to the channel
        var send = !_hasSubscriptions(channel);

        var subscription = _addListener(channel, scope, callback, false);

        if (send)
        {
            // Send the subscription message after the subscription registration to avoid
            // races where the server would send a message to the subscribers, but here
            // on the client the subscription has not been added yet to the data structures
            var bayeuxMessage = {
                channel: '/meta/subscribe',
                subscription: channel
            };
            var message = _mixin(false, {}, subscribeProps, bayeuxMessage);
            _queueSend(message);
        }

        return subscription;
    };

    /**
     * Unsubscribes the subscription obtained with a call to {@link #subscribe(string, object, function)}.
     * @param subscription the subscription to unsubscribe.
     */
    this.unsubscribe = function(subscription, unsubscribeProps)
    {
        if (arguments.length < 1)
        {
            throw 'Illegal arguments number: required 1, got ' + arguments.length;
        }
        if (_isDisconnected())
        {
            throw 'Illegal state: already disconnected';
        }

        // Remove the local listener before sending the message
        // This ensures that if the server fails, this client does not get notifications
        this.removeListener(subscription);

        var channel = subscription[0];
        // Only send the message to the server if this clientId unsubscribes the last subscription
        if (!_hasSubscriptions(channel))
        {
            var bayeuxMessage = {
                channel: '/meta/unsubscribe',
                subscription: channel
            };
            var message = _mixin(false, {}, unsubscribeProps, bayeuxMessage);
            _queueSend(message);
        }
    };

    /**
     * Removes all subscriptions added via {@link #subscribe(channel, scope, callback, subscribeProps)},
     * but does not remove the listeners added via {@link addListener(channel, scope, callback)}.
     */
    this.clearSubscriptions = function()
    {
        _clearSubscriptions();
    };

    /**
     * Publishes a message on the given channel, containing the given content.
     * @param channel the channel to publish the message to
     * @param content the content of the message
     * @param publishProps an object to be merged with the publish message
     */
    this.publish = function(channel, content, publishProps)
    {
        if (arguments.length < 1)
        {
            throw 'Illegal arguments number: required 1, got ' + arguments.length;
        }
        if (!_isString(channel))
        {
            throw 'Illegal argument type: channel must be a string';
        }
        if (_isDisconnected())
        {
            throw 'Illegal state: already disconnected';
        }

        var bayeuxMessage = {
            channel: channel,
            data: content
        };
        var message = _mixin(false, {}, publishProps, bayeuxMessage);
        _queueSend(message);
    };

    /**
     * Returns a string representing the status of the bayeux communication with the Bayeux server.
     */
    this.getStatus = function()
    {
        return _status;
    };

    /**
     * Returns whether this instance has been disconnected.
     */
    this.isDisconnected = _isDisconnected;

    /**
     * Sets the backoff period used to increase the backoff time when retrying an unsuccessful or failed message.
     * Default value is 1 second, which means if there is a persistent failure the retries will happen
     * after 1 second, then after 2 seconds, then after 3 seconds, etc. So for example with 15 seconds of
     * elapsed time, there will be 5 retries (at 1, 3, 6, 10 and 15 seconds elapsed).
     * @param period the backoff period to set
     * @see #getBackoffIncrement()
     */
    this.setBackoffIncrement = function(period)
    {
        _config.backoffIncrement = period;
    };

    /**
     * Returns the backoff period used to increase the backoff time when retrying an unsuccessful or failed message.
     * @see #setBackoffIncrement(period)
     */
    this.getBackoffIncrement = function()
    {
        return _config.backoffIncrement;
    };

    /**
     * Returns the backoff period to wait before retrying an unsuccessful or failed message.
     */
    this.getBackoffPeriod = function()
    {
        return _backoff;
    };

    /**
     * Sets the log level for console logging.
     * Valid values are the strings 'error', 'warn', 'info' and 'debug', from
     * less verbose to more verbose.
     * @param level the log level string
     */
    this.setLogLevel = function(level)
    {
        _config.logLevel = level;
    };

    /**
     * Registers an extension whose callbacks are called for every incoming message
     * (that comes from the server to this client implementation) and for every
     * outgoing message (that originates from this client implementation for the
     * server).
     * The format of the extension object is the following:
     * <pre>
     * {
     *     incoming: function(message) { ... },
     *     outgoing: function(message) { ... }
     * }
     * </pre>
     * Both properties are optional, but if they are present they will be called
     * respectively for each incoming message and for each outgoing message.
     * @param name the name of the extension
     * @param extension the extension to register
     * @return true if the extension was registered, false otherwise
     * @see #unregisterExtension(name)
     */
    this.registerExtension = function(name, extension)
    {
        if (arguments.length < 2)
        {
            throw 'Illegal arguments number: required 2, got ' + arguments.length;
        }
        if (!_isString(name))
        {
            throw 'Illegal argument type: extension name must be a string';
        }

        var existing = false;
        for (var i = 0; i < _extensions.length; ++i)
        {
            var existingExtension = _extensions[i];
            if (existingExtension.name == name)
            {
                existing = true;
                break;
            }
        }
        if (!existing)
        {
            _extensions.push({
                name: name,
                extension: extension
            });
            _debug('Registered extension', name);

            // Callback for extensions
            if (_isFunction(extension.registered))
            {
                extension.registered(name, this);
            }

            return true;
        }
        else
        {
            _info('Could not register extension with name', name, 'since another extension with the same name already exists');
            return false;
        }
    };

    /**
     * Unregister an extension previously registered with
     * {@link #registerExtension(name, extension)}.
     * @param name the name of the extension to unregister.
     * @return true if the extension was unregistered, false otherwise
     */
    this.unregisterExtension = function(name)
    {
        if (!_isString(name))
        {
            throw 'Illegal argument type: extension name must be a string';
        }

        var unregistered = false;
        for (var i = 0; i < _extensions.length; ++i)
        {
            var extension = _extensions[i];
            if (extension.name == name)
            {
                _extensions.splice(i, 1);
                unregistered = true;
                _debug('Unregistered extension', name);

                // Callback for extensions
                var ext = extension.extension;
                if (_isFunction(ext.unregistered))
                {
                    ext.unregistered();
                }

                break;
            }
        }
        return unregistered;
    };

    /**
     * Find the extension registered with the given name.
     * @param name the name of the extension to find
     * @return the extension found or null if no extension with the given name has been registered
     */
    this.getExtension = function(name)
    {
        for (var i = 0; i < _extensions.length; ++i)
        {
            var extension = _extensions[i];
            if (extension.name == name)
            {
                return extension.extension;
            }
        }
        return null;
    };

    /**
     * Returns the name assigned to this Cometd object, or the string 'default'
     * if no name has been explicitly passed as parameter to the constructor.
     */
    this.getName = function()
    {
        return _name;
    };

    /**
     * Returns the clientId assigned by the Bayeux server during handshake.
     */
    this.getClientId = function()
    {
        return _clientId;
    };

    /**
     * Returns the URL of the Bayeux server.
     */
    this.getURL = function()
    {
        return _config.url;
    };

    this.getTransport = function()
    {
        return _transport;
    };

    this.getConfiguration = function()
    {
        return _mixin(true, {}, _config);
    };

    this.getAdvice = function()
    {
        return _mixin(true, {}, _advice);
    };
};


/*!
 * jQuery JavaScript Library v1.5.1
 * http://jquery.com/
 *
 * Copyright 2011, John Resig
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * Includes Sizzle.js
 * http://sizzlejs.com/
 * Copyright 2011, The Dojo Foundation
 * Released under the MIT, BSD, and GPL Licenses.
 *
 * Date: Wed Feb 23 13:55:29 2011 -0500
 */
(function( window, undefined ) {

// Use the correct document accordingly with window argument (sandbox)
var document = window.document;
var jQuery = (function() {

// Define a local copy of jQuery
var jQuery = function( selector, context ) {
		// The jQuery object is actually just the init constructor 'enhanced'
		return new jQuery.fn.init( selector, context, rootjQuery );
	},

	// Map over jQuery in case of overwrite
	_jQuery = window.jQuery,

	// Map over the $ in case of overwrite
	_$ = window.$,

	// A central reference to the root jQuery(document)
	rootjQuery,

	// A simple way to check for HTML strings or ID strings
	// (both of which we optimize for)
	quickExpr = /^(?:[^<]*(<[\w\W]+>)[^>]*$|#([\w\-]+)$)/,

	// Check if a string has a non-whitespace character in it
	rnotwhite = /\S/,

	// Used for trimming whitespace
	trimLeft = /^\s+/,
	trimRight = /\s+$/,

	// Check for digits
	rdigit = /\d/,

	// Match a standalone tag
	rsingleTag = /^<(\w+)\s*\/?>(?:<\/\1>)?$/,

	// JSON RegExp
	rvalidchars = /^[\],:{}\s]*$/,
	rvalidescape = /\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,
	rvalidtokens = /"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,
	rvalidbraces = /(?:^|:|,)(?:\s*\[)+/g,

	// Useragent RegExp
	rwebkit = /(webkit)[ \/]([\w.]+)/,
	ropera = /(opera)(?:.*version)?[ \/]([\w.]+)/,
	rmsie = /(msie) ([\w.]+)/,
	rmozilla = /(mozilla)(?:.*? rv:([\w.]+))?/,

	// Keep a UserAgent string for use with jQuery.browser
	userAgent = navigator.userAgent,

	// For matching the engine and version of the browser
	browserMatch,

	// Has the ready events already been bound?
	readyBound = false,

	// The deferred used on DOM ready
	readyList,

	// Promise methods
	promiseMethods = "then done fail isResolved isRejected promise".split( " " ),

	// The ready event handler
	DOMContentLoaded,

	// Save a reference to some core methods
	toString = Object.prototype.toString,
	hasOwn = Object.prototype.hasOwnProperty,
	push = Array.prototype.push,
	slice = Array.prototype.slice,
	trim = String.prototype.trim,
	indexOf = Array.prototype.indexOf,

	// [[Class]] -> type pairs
	class2type = {};

jQuery.fn = jQuery.prototype = {
	constructor: jQuery,
	init: function( selector, context, rootjQuery ) {
		var match, elem, ret, doc;

		// Handle $(""), $(null), or $(undefined)
		if ( !selector ) {
			return this;
		}

		// Handle $(DOMElement)
		if ( selector.nodeType ) {
			this.context = this[0] = selector;
			this.length = 1;
			return this;
		}

		// The body element only exists once, optimize finding it
		if ( selector === "body" && !context && document.body ) {
			this.context = document;
			this[0] = document.body;
			this.selector = "body";
			this.length = 1;
			return this;
		}

		// Handle HTML strings
		if ( typeof selector === "string" ) {
			// Are we dealing with HTML string or an ID?
			match = quickExpr.exec( selector );

			// Verify a match, and that no context was specified for #id
			if ( match && (match[1] || !context) ) {

				// HANDLE: $(html) -> $(array)
				if ( match[1] ) {
					context = context instanceof jQuery ? context[0] : context;
					doc = (context ? context.ownerDocument || context : document);

					// If a single string is passed in and it's a single tag
					// just do a createElement and skip the rest
					ret = rsingleTag.exec( selector );

					if ( ret ) {
						if ( jQuery.isPlainObject( context ) ) {
							selector = [ document.createElement( ret[1] ) ];
							jQuery.fn.attr.call( selector, context, true );

						} else {
							selector = [ doc.createElement( ret[1] ) ];
						}

					} else {
						ret = jQuery.buildFragment( [ match[1] ], [ doc ] );
						selector = (ret.cacheable ? jQuery.clone(ret.fragment) : ret.fragment).childNodes;
					}

					return jQuery.merge( this, selector );

				// HANDLE: $("#id")
				} else {
					elem = document.getElementById( match[2] );

					// Check parentNode to catch when Blackberry 4.6 returns
					// nodes that are no longer in the document #6963
					if ( elem && elem.parentNode ) {
						// Handle the case where IE and Opera return items
						// by name instead of ID
						if ( elem.id !== match[2] ) {
							return rootjQuery.find( selector );
						}

						// Otherwise, we inject the element directly into the jQuery object
						this.length = 1;
						this[0] = elem;
					}

					this.context = document;
					this.selector = selector;
					return this;
				}

			// HANDLE: $(expr, $(...))
			} else if ( !context || context.jquery ) {
				return (context || rootjQuery).find( selector );

			// HANDLE: $(expr, context)
			// (which is just equivalent to: $(context).find(expr)
			} else {
				return this.constructor( context ).find( selector );
			}

		// HANDLE: $(function)
		// Shortcut for document ready
		} else if ( jQuery.isFunction( selector ) ) {
			return rootjQuery.ready( selector );
		}

		if (selector.selector !== undefined) {
			this.selector = selector.selector;
			this.context = selector.context;
		}

		return jQuery.makeArray( selector, this );
	},

	// Start with an empty selector
	selector: "",

	// The current version of jQuery being used
	jquery: "1.5.1",

	// The default length of a jQuery object is 0
	length: 0,

	// The number of elements contained in the matched element set
	size: function() {
		return this.length;
	},

	toArray: function() {
		return slice.call( this, 0 );
	},

	// Get the Nth element in the matched element set OR
	// Get the whole matched element set as a clean array
	get: function( num ) {
		return num == null ?

			// Return a 'clean' array
			this.toArray() :

			// Return just the object
			( num < 0 ? this[ this.length + num ] : this[ num ] );
	},

	// Take an array of elements and push it onto the stack
	// (returning the new matched element set)
	pushStack: function( elems, name, selector ) {
		// Build a new jQuery matched element set
		var ret = this.constructor();

		if ( jQuery.isArray( elems ) ) {
			push.apply( ret, elems );

		} else {
			jQuery.merge( ret, elems );
		}

		// Add the old object onto the stack (as a reference)
		ret.prevObject = this;

		ret.context = this.context;

		if ( name === "find" ) {
			ret.selector = this.selector + (this.selector ? " " : "") + selector;
		} else if ( name ) {
			ret.selector = this.selector + "." + name + "(" + selector + ")";
		}

		// Return the newly-formed element set
		return ret;
	},

	// Execute a callback for every element in the matched set.
	// (You can seed the arguments with an array of args, but this is
	// only used internally.)
	each: function( callback, args ) {
		return jQuery.each( this, callback, args );
	},

	ready: function( fn ) {
		// Attach the listeners
		jQuery.bindReady();

		// Add the callback
		readyList.done( fn );

		return this;
	},

	eq: function( i ) {
		return i === -1 ?
			this.slice( i ) :
			this.slice( i, +i + 1 );
	},

	first: function() {
		return this.eq( 0 );
	},

	last: function() {
		return this.eq( -1 );
	},

	slice: function() {
		return this.pushStack( slice.apply( this, arguments ),
			"slice", slice.call(arguments).join(",") );
	},

	map: function( callback ) {
		return this.pushStack( jQuery.map(this, function( elem, i ) {
			return callback.call( elem, i, elem );
		}));
	},

	end: function() {
		return this.prevObject || this.constructor(null);
	},

	// For internal use only.
	// Behaves like an Array's method, not like a jQuery method.
	push: push,
	sort: [].sort,
	splice: [].splice
};

// Give the init function the jQuery prototype for later instantiation
jQuery.fn.init.prototype = jQuery.fn;

jQuery.extend = jQuery.fn.extend = function() {
	var options, name, src, copy, copyIsArray, clone,
		target = arguments[0] || {},
		i = 1,
		length = arguments.length,
		deep = false;

	// Handle a deep copy situation
	if ( typeof target === "boolean" ) {
		deep = target;
		target = arguments[1] || {};
		// skip the boolean and the target
		i = 2;
	}

	// Handle case when target is a string or something (possible in deep copy)
	if ( typeof target !== "object" && !jQuery.isFunction(target) ) {
		target = {};
	}

	// extend jQuery itself if only one argument is passed
	if ( length === i ) {
		target = this;
		--i;
	}

	for ( ; i < length; i++ ) {
		// Only deal with non-null/undefined values
		if ( (options = arguments[ i ]) != null ) {
			// Extend the base object
			for ( name in options ) {
				src = target[ name ];
				copy = options[ name ];

				// Prevent never-ending loop
				if ( target === copy ) {
					continue;
				}

				// Recurse if we're merging plain objects or arrays
				if ( deep && copy && ( jQuery.isPlainObject(copy) || (copyIsArray = jQuery.isArray(copy)) ) ) {
					if ( copyIsArray ) {
						copyIsArray = false;
						clone = src && jQuery.isArray(src) ? src : [];

					} else {
						clone = src && jQuery.isPlainObject(src) ? src : {};
					}

					// Never move original objects, clone them
					target[ name ] = jQuery.extend( deep, clone, copy );

				// Don't bring in undefined values
				} else if ( copy !== undefined ) {
					target[ name ] = copy;
				}
			}
		}
	}

	// Return the modified object
	return target;
};

jQuery.extend({
	noConflict: function( deep ) {
		window.$ = _$;

		if ( deep ) {
			window.jQuery = _jQuery;
		}

		return jQuery;
	},

	// Is the DOM ready to be used? Set to true once it occurs.
	isReady: false,

	// A counter to track how many items to wait for before
	// the ready event fires. See #6781
	readyWait: 1,

	// Handle when the DOM is ready
	ready: function( wait ) {
		// A third-party is pushing the ready event forwards
		if ( wait === true ) {
			jQuery.readyWait--;
		}

		// Make sure that the DOM is not already loaded
		if ( !jQuery.readyWait || (wait !== true && !jQuery.isReady) ) {
			// Make sure body exists, at least, in case IE gets a little overzealous (ticket #5443).
			if ( !document.body ) {
				return setTimeout( jQuery.ready, 1 );
			}

			// Remember that the DOM is ready
			jQuery.isReady = true;

			// If a normal DOM Ready event fired, decrement, and wait if need be
			if ( wait !== true && --jQuery.readyWait > 0 ) {
				return;
			}

			// If there are functions bound, to execute
			readyList.resolveWith( document, [ jQuery ] );

			// Trigger any bound ready events
			if ( jQuery.fn.trigger ) {
				jQuery( document ).trigger( "ready" ).unbind( "ready" );
			}
		}
	},

	bindReady: function() {
		if ( readyBound ) {
			return;
		}

		readyBound = true;

		// Catch cases where $(document).ready() is called after the
		// browser event has already occurred.
		if ( document.readyState === "complete" ) {
			// Handle it asynchronously to allow scripts the opportunity to delay ready
			return setTimeout( jQuery.ready, 1 );
		}

		// Mozilla, Opera and webkit nightlies currently support this event
		if ( document.addEventListener ) {
			// Use the handy event callback
			document.addEventListener( "DOMContentLoaded", DOMContentLoaded, false );

			// A fallback to window.onload, that will always work
			window.addEventListener( "load", jQuery.ready, false );

		// If IE event model is used
		} else if ( document.attachEvent ) {
			// ensure firing before onload,
			// maybe late but safe also for iframes
			document.attachEvent("onreadystatechange", DOMContentLoaded);

			// A fallback to window.onload, that will always work
			window.attachEvent( "onload", jQuery.ready );

			// If IE and not a frame
			// continually check to see if the document is ready
			var toplevel = false;

			try {
				toplevel = window.frameElement == null;
			} catch(e) {}

			if ( document.documentElement.doScroll && toplevel ) {
				doScrollCheck();
			}
		}
	},

	// See test/unit/core.js for details concerning isFunction.
	// Since version 1.3, DOM methods and functions like alert
	// aren't supported. They return false on IE (#2968).
	isFunction: function( obj ) {
		return jQuery.type(obj) === "function";
	},

	isArray: Array.isArray || function( obj ) {
		return jQuery.type(obj) === "array";
	},

	// A crude way of determining if an object is a window
	isWindow: function( obj ) {
		return obj && typeof obj === "object" && "setInterval" in obj;
	},

	isNaN: function( obj ) {
		return obj == null || !rdigit.test( obj ) || isNaN( obj );
	},

	type: function( obj ) {
		return obj == null ?
			String( obj ) :
			class2type[ toString.call(obj) ] || "object";
	},

	isPlainObject: function( obj ) {
		// Must be an Object.
		// Because of IE, we also have to check the presence of the constructor property.
		// Make sure that DOM nodes and window objects don't pass through, as well
		if ( !obj || jQuery.type(obj) !== "object" || obj.nodeType || jQuery.isWindow( obj ) ) {
			return false;
		}

		// Not own constructor property must be Object
		if ( obj.constructor &&
			!hasOwn.call(obj, "constructor") &&
			!hasOwn.call(obj.constructor.prototype, "isPrototypeOf") ) {
			return false;
		}

		// Own properties are enumerated firstly, so to speed up,
		// if last one is own, then all properties are own.

		var key;
		for ( key in obj ) {}

		return key === undefined || hasOwn.call( obj, key );
	},

	isEmptyObject: function( obj ) {
		for ( var name in obj ) {
			return false;
		}
		return true;
	},

	error: function( msg ) {
		throw msg;
	},

	parseJSON: function( data ) {
		if ( typeof data !== "string" || !data ) {
			return null;
		}

		// Make sure leading/trailing whitespace is removed (IE can't handle it)
		data = jQuery.trim( data );

		// Make sure the incoming data is actual JSON
		// Logic borrowed from http://json.org/json2.js
		if ( rvalidchars.test(data.replace(rvalidescape, "@")
			.replace(rvalidtokens, "]")
			.replace(rvalidbraces, "")) ) {

			// Try to use the native JSON parser first
			return window.JSON && window.JSON.parse ?
				window.JSON.parse( data ) :
				(new Function("return " + data))();

		} else {
			jQuery.error( "Invalid JSON: " + data );
		}
	},

	// Cross-browser xml parsing
	// (xml & tmp used internally)
	parseXML: function( data , xml , tmp ) {

		if ( window.DOMParser ) { // Standard
			tmp = new DOMParser();
			xml = tmp.parseFromString( data , "text/xml" );
		} else { // IE
			xml = new ActiveXObject( "Microsoft.XMLDOM" );
			xml.async = "false";
			xml.loadXML( data );
		}

		tmp = xml.documentElement;

		if ( ! tmp || ! tmp.nodeName || tmp.nodeName === "parsererror" ) {
			jQuery.error( "Invalid XML: " + data );
		}

		return xml;
	},

	noop: function() {},

	// Evalulates a script in a global context
	globalEval: function( data ) {
		if ( data && rnotwhite.test(data) ) {
			// Inspired by code by Andrea Giammarchi
			// http://webreflection.blogspot.com/2007/08/global-scope-evaluation-and-dom.html
			var head = document.head || document.getElementsByTagName( "head" )[0] || document.documentElement,
				script = document.createElement( "script" );

			if ( jQuery.support.scriptEval() ) {
				script.appendChild( document.createTextNode( data ) );
			} else {
				script.text = data;
			}

			// Use insertBefore instead of appendChild to circumvent an IE6 bug.
			// This arises when a base node is used (#2709).
			head.insertBefore( script, head.firstChild );
			head.removeChild( script );
		}
	},

	nodeName: function( elem, name ) {
		return elem.nodeName && elem.nodeName.toUpperCase() === name.toUpperCase();
	},

	// args is for internal usage only
	each: function( object, callback, args ) {
		var name, i = 0,
			length = object.length,
			isObj = length === undefined || jQuery.isFunction(object);

		if ( args ) {
			if ( isObj ) {
				for ( name in object ) {
					if ( callback.apply( object[ name ], args ) === false ) {
						break;
					}
				}
			} else {
				for ( ; i < length; ) {
					if ( callback.apply( object[ i++ ], args ) === false ) {
						break;
					}
				}
			}

		// A special, fast, case for the most common use of each
		} else {
			if ( isObj ) {
				for ( name in object ) {
					if ( callback.call( object[ name ], name, object[ name ] ) === false ) {
						break;
					}
				}
			} else {
				for ( var value = object[0];
					i < length && callback.call( value, i, value ) !== false; value = object[++i] ) {}
			}
		}

		return object;
	},

	// Use native String.trim function wherever possible
	trim: trim ?
		function( text ) {
			return text == null ?
				"" :
				trim.call( text );
		} :

		// Otherwise use our own trimming functionality
		function( text ) {
			return text == null ?
				"" :
				text.toString().replace( trimLeft, "" ).replace( trimRight, "" );
		},

	// results is for internal usage only
	makeArray: function( array, results ) {
		var ret = results || [];

		if ( array != null ) {
			// The window, strings (and functions) also have 'length'
			// The extra typeof function check is to prevent crashes
			// in Safari 2 (See: #3039)
			// Tweaked logic slightly to handle Blackberry 4.7 RegExp issues #6930
			var type = jQuery.type(array);

			if ( array.length == null || type === "string" || type === "function" || type === "regexp" || jQuery.isWindow( array ) ) {
				push.call( ret, array );
			} else {
				jQuery.merge( ret, array );
			}
		}

		return ret;
	},

	inArray: function( elem, array ) {
		if ( array.indexOf ) {
			return array.indexOf( elem );
		}

		for ( var i = 0, length = array.length; i < length; i++ ) {
			if ( array[ i ] === elem ) {
				return i;
			}
		}

		return -1;
	},

	merge: function( first, second ) {
		var i = first.length,
			j = 0;

		if ( typeof second.length === "number" ) {
			for ( var l = second.length; j < l; j++ ) {
				first[ i++ ] = second[ j ];
			}

		} else {
			while ( second[j] !== undefined ) {
				first[ i++ ] = second[ j++ ];
			}
		}

		first.length = i;

		return first;
	},

	grep: function( elems, callback, inv ) {
		var ret = [], retVal;
		inv = !!inv;

		// Go through the array, only saving the items
		// that pass the validator function
		for ( var i = 0, length = elems.length; i < length; i++ ) {
			retVal = !!callback( elems[ i ], i );
			if ( inv !== retVal ) {
				ret.push( elems[ i ] );
			}
		}

		return ret;
	},

	// arg is for internal usage only
	map: function( elems, callback, arg ) {
		var ret = [], value;

		// Go through the array, translating each of the items to their
		// new value (or values).
		for ( var i = 0, length = elems.length; i < length; i++ ) {
			value = callback( elems[ i ], i, arg );

			if ( value != null ) {
				ret[ ret.length ] = value;
			}
		}

		// Flatten any nested arrays
		return ret.concat.apply( [], ret );
	},

	// A global GUID counter for objects
	guid: 1,

	proxy: function( fn, proxy, thisObject ) {
		if ( arguments.length === 2 ) {
			if ( typeof proxy === "string" ) {
				thisObject = fn;
				fn = thisObject[ proxy ];
				proxy = undefined;

			} else if ( proxy && !jQuery.isFunction( proxy ) ) {
				thisObject = proxy;
				proxy = undefined;
			}
		}

		if ( !proxy && fn ) {
			proxy = function() {
				return fn.apply( thisObject || this, arguments );
			};
		}

		// Set the guid of unique handler to the same of original handler, so it can be removed
		if ( fn ) {
			proxy.guid = fn.guid = fn.guid || proxy.guid || jQuery.guid++;
		}

		// So proxy can be declared as an argument
		return proxy;
	},

	// Mutifunctional method to get and set values to a collection
	// The value/s can be optionally by executed if its a function
	access: function( elems, key, value, exec, fn, pass ) {
		var length = elems.length;

		// Setting many attributes
		if ( typeof key === "object" ) {
			for ( var k in key ) {
				jQuery.access( elems, k, key[k], exec, fn, value );
			}
			return elems;
		}

		// Setting one attribute
		if ( value !== undefined ) {
			// Optionally, function values get executed if exec is true
			exec = !pass && exec && jQuery.isFunction(value);

			for ( var i = 0; i < length; i++ ) {
				fn( elems[i], key, exec ? value.call( elems[i], i, fn( elems[i], key ) ) : value, pass );
			}

			return elems;
		}

		// Getting an attribute
		return length ? fn( elems[0], key ) : undefined;
	},

	now: function() {
		return (new Date()).getTime();
	},

	// Create a simple deferred (one callbacks list)
	_Deferred: function() {
		var // callbacks list
			callbacks = [],
			// stored [ context , args ]
			fired,
			// to avoid firing when already doing so
			firing,
			// flag to know if the deferred has been cancelled
			cancelled,
			// the deferred itself
			deferred  = {

				// done( f1, f2, ...)
				done: function() {
					if ( !cancelled ) {
						var args = arguments,
							i,
							length,
							elem,
							type,
							_fired;
						if ( fired ) {
							_fired = fired;
							fired = 0;
						}
						for ( i = 0, length = args.length; i < length; i++ ) {
							elem = args[ i ];
							type = jQuery.type( elem );
							if ( type === "array" ) {
								deferred.done.apply( deferred, elem );
							} else if ( type === "function" ) {
								callbacks.push( elem );
							}
						}
						if ( _fired ) {
							deferred.resolveWith( _fired[ 0 ], _fired[ 1 ] );
						}
					}
					return this;
				},

				// resolve with given context and args
				resolveWith: function( context, args ) {
					if ( !cancelled && !fired && !firing ) {
						firing = 1;
						try {
							while( callbacks[ 0 ] ) {
								callbacks.shift().apply( context, args );
							}
						}
						// We have to add a catch block for
						// IE prior to 8 or else the finally
						// block will never get executed
						catch (e) {
							throw e;
						}
						finally {
							fired = [ context, args ];
							firing = 0;
						}
					}
					return this;
				},

				// resolve with this as context and given arguments
				resolve: function() {
					deferred.resolveWith( jQuery.isFunction( this.promise ) ? this.promise() : this, arguments );
					return this;
				},

				// Has this deferred been resolved?
				isResolved: function() {
					return !!( firing || fired );
				},

				// Cancel
				cancel: function() {
					cancelled = 1;
					callbacks = [];
					return this;
				}
			};

		return deferred;
	},

	// Full fledged deferred (two callbacks list)
	Deferred: function( func ) {
		var deferred = jQuery._Deferred(),
			failDeferred = jQuery._Deferred(),
			promise;
		// Add errorDeferred methods, then and promise
		jQuery.extend( deferred, {
			then: function( doneCallbacks, failCallbacks ) {
				deferred.done( doneCallbacks ).fail( failCallbacks );
				return this;
			},
			fail: failDeferred.done,
			rejectWith: failDeferred.resolveWith,
			reject: failDeferred.resolve,
			isRejected: failDeferred.isResolved,
			// Get a promise for this deferred
			// If obj is provided, the promise aspect is added to the object
			promise: function( obj ) {
				if ( obj == null ) {
					if ( promise ) {
						return promise;
					}
					promise = obj = {};
				}
				var i = promiseMethods.length;
				while( i-- ) {
					obj[ promiseMethods[i] ] = deferred[ promiseMethods[i] ];
				}
				return obj;
			}
		} );
		// Make sure only one callback list will be used
		deferred.done( failDeferred.cancel ).fail( deferred.cancel );
		// Unexpose cancel
		delete deferred.cancel;
		// Call given func if any
		if ( func ) {
			func.call( deferred, deferred );
		}
		return deferred;
	},

	// Deferred helper
	when: function( object ) {
		var lastIndex = arguments.length,
			deferred = lastIndex <= 1 && object && jQuery.isFunction( object.promise ) ?
				object :
				jQuery.Deferred(),
			promise = deferred.promise();

		if ( lastIndex > 1 ) {
			var array = slice.call( arguments, 0 ),
				count = lastIndex,
				iCallback = function( index ) {
					return function( value ) {
						array[ index ] = arguments.length > 1 ? slice.call( arguments, 0 ) : value;
						if ( !( --count ) ) {
							deferred.resolveWith( promise, array );
						}
					};
				};
			while( ( lastIndex-- ) ) {
				object = array[ lastIndex ];
				if ( object && jQuery.isFunction( object.promise ) ) {
					object.promise().then( iCallback(lastIndex), deferred.reject );
				} else {
					--count;
				}
			}
			if ( !count ) {
				deferred.resolveWith( promise, array );
			}
		} else if ( deferred !== object ) {
			deferred.resolve( object );
		}
		return promise;
	},

	// Use of jQuery.browser is frowned upon.
	// More details: http://docs.jquery.com/Utilities/jQuery.browser
	uaMatch: function( ua ) {
		ua = ua.toLowerCase();

		var match = rwebkit.exec( ua ) ||
			ropera.exec( ua ) ||
			rmsie.exec( ua ) ||
			ua.indexOf("compatible") < 0 && rmozilla.exec( ua ) ||
			[];

		return { browser: match[1] || "", version: match[2] || "0" };
	},

	sub: function() {
		function jQuerySubclass( selector, context ) {
			return new jQuerySubclass.fn.init( selector, context );
		}
		jQuery.extend( true, jQuerySubclass, this );
		jQuerySubclass.superclass = this;
		jQuerySubclass.fn = jQuerySubclass.prototype = this();
		jQuerySubclass.fn.constructor = jQuerySubclass;
		jQuerySubclass.subclass = this.subclass;
		jQuerySubclass.fn.init = function init( selector, context ) {
			if ( context && context instanceof jQuery && !(context instanceof jQuerySubclass) ) {
				context = jQuerySubclass(context);
			}

			return jQuery.fn.init.call( this, selector, context, rootjQuerySubclass );
		};
		jQuerySubclass.fn.init.prototype = jQuerySubclass.fn;
		var rootjQuerySubclass = jQuerySubclass(document);
		return jQuerySubclass;
	},

	browser: {}
});

// Create readyList deferred
readyList = jQuery._Deferred();

// Populate the class2type map
jQuery.each("Boolean Number String Function Array Date RegExp Object".split(" "), function(i, name) {
	class2type[ "[object " + name + "]" ] = name.toLowerCase();
});

browserMatch = jQuery.uaMatch( userAgent );
if ( browserMatch.browser ) {
	jQuery.browser[ browserMatch.browser ] = true;
	jQuery.browser.version = browserMatch.version;
}

// Deprecated, use jQuery.browser.webkit instead
if ( jQuery.browser.webkit ) {
	jQuery.browser.safari = true;
}

if ( indexOf ) {
	jQuery.inArray = function( elem, array ) {
		return indexOf.call( array, elem );
	};
}

// IE doesn't match non-breaking spaces with \s
if ( rnotwhite.test( "\xA0" ) ) {
	trimLeft = /^[\s\xA0]+/;
	trimRight = /[\s\xA0]+$/;
}

// All jQuery objects should point back to these
rootjQuery = jQuery(document);

// Cleanup functions for the document ready method
if ( document.addEventListener ) {
	DOMContentLoaded = function() {
		document.removeEventListener( "DOMContentLoaded", DOMContentLoaded, false );
		jQuery.ready();
	};

} else if ( document.attachEvent ) {
	DOMContentLoaded = function() {
		// Make sure body exists, at least, in case IE gets a little overzealous (ticket #5443).
		if ( document.readyState === "complete" ) {
			document.detachEvent( "onreadystatechange", DOMContentLoaded );
			jQuery.ready();
		}
	};
}

// The DOM ready check for Internet Explorer
function doScrollCheck() {
	if ( jQuery.isReady ) {
		return;
	}

	try {
		// If IE is used, use the trick by Diego Perini
		// http://javascript.nwbox.com/IEContentLoaded/
		document.documentElement.doScroll("left");
	} catch(e) {
		setTimeout( doScrollCheck, 1 );
		return;
	}

	// and execute any waiting functions
	jQuery.ready();
}

// Expose jQuery to the global object
return jQuery;

})();


(function() {

	jQuery.support = {};

	var div = document.createElement("div");

	div.style.display = "none";
	div.innerHTML = "   <link/><table></table><a href='/a' style='color:red;float:left;opacity:.55;'>a</a><input type='checkbox'/>";

	var all = div.getElementsByTagName("*"),
		a = div.getElementsByTagName("a")[0],
		select = document.createElement("select"),
		opt = select.appendChild( document.createElement("option") ),
		input = div.getElementsByTagName("input")[0];

	// Can't get basic test support
	if ( !all || !all.length || !a ) {
		return;
	}

	jQuery.support = {
		// IE strips leading whitespace when .innerHTML is used
		leadingWhitespace: div.firstChild.nodeType === 3,

		// Make sure that tbody elements aren't automatically inserted
		// IE will insert them into empty tables
		tbody: !div.getElementsByTagName("tbody").length,

		// Make sure that link elements get serialized correctly by innerHTML
		// This requires a wrapper element in IE
		htmlSerialize: !!div.getElementsByTagName("link").length,

		// Get the style information from getAttribute
		// (IE uses .cssText insted)
		style: /red/.test( a.getAttribute("style") ),

		// Make sure that URLs aren't manipulated
		// (IE normalizes it by default)
		hrefNormalized: a.getAttribute("href") === "/a",

		// Make sure that element opacity exists
		// (IE uses filter instead)
		// Use a regex to work around a WebKit issue. See #5145
		opacity: /^0.55$/.test( a.style.opacity ),

		// Verify style float existence
		// (IE uses styleFloat instead of cssFloat)
		cssFloat: !!a.style.cssFloat,

		// Make sure that if no value is specified for a checkbox
		// that it defaults to "on".
		// (WebKit defaults to "" instead)
		checkOn: input.value === "on",

		// Make sure that a selected-by-default option has a working selected property.
		// (WebKit defaults to false instead of true, IE too, if it's in an optgroup)
		optSelected: opt.selected,

		// Will be defined later
		deleteExpando: true,
		optDisabled: false,
		checkClone: false,
		noCloneEvent: true,
		noCloneChecked: true,
		boxModel: null,
		inlineBlockNeedsLayout: false,
		shrinkWrapBlocks: false,
		reliableHiddenOffsets: true
	};

	input.checked = true;
	jQuery.support.noCloneChecked = input.cloneNode( true ).checked;

	// Make sure that the options inside disabled selects aren't marked as disabled
	// (WebKit marks them as diabled)
	select.disabled = true;
	jQuery.support.optDisabled = !opt.disabled;

	var _scriptEval = null;
	jQuery.support.scriptEval = function() {
		if ( _scriptEval === null ) {
			var root = document.documentElement,
				script = document.createElement("script"),
				id = "script" + jQuery.now();

			try {
				script.appendChild( document.createTextNode( "window." + id + "=1;" ) );
			} catch(e) {}

			root.insertBefore( script, root.firstChild );

			// Make sure that the execution of code works by injecting a script
			// tag with appendChild/createTextNode
			// (IE doesn't support this, fails, and uses .text instead)
			if ( window[ id ] ) {
				_scriptEval = true;
				delete window[ id ];
			} else {
				_scriptEval = false;
			}

			root.removeChild( script );
			// release memory in IE
			root = script = id  = null;
		}

		return _scriptEval;
	};

	// Test to see if it's possible to delete an expando from an element
	// Fails in Internet Explorer
	try {
		delete div.test;

	} catch(e) {
		jQuery.support.deleteExpando = false;
	}

	if ( !div.addEventListener && div.attachEvent && div.fireEvent ) {
		div.attachEvent("onclick", function click() {
			// Cloning a node shouldn't copy over any
			// bound event handlers (IE does this)
			jQuery.support.noCloneEvent = false;
			div.detachEvent("onclick", click);
		});
		div.cloneNode(true).fireEvent("onclick");
	}

	div = document.createElement("div");
	div.innerHTML = "<input type='radio' name='radiotest' checked='checked'/>";

	var fragment = document.createDocumentFragment();
	fragment.appendChild( div.firstChild );

	// WebKit doesn't clone checked state correctly in fragments
	jQuery.support.checkClone = fragment.cloneNode(true).cloneNode(true).lastChild.checked;

	// Figure out if the W3C box model works as expected
	// document.body must exist before we can do this
	jQuery(function() {
		var div = document.createElement("div"),
			body = document.getElementsByTagName("body")[0];

		// Frameset documents with no body should not run this code
		if ( !body ) {
			return;
		}

		div.style.width = div.style.paddingLeft = "1px";
		body.appendChild( div );
		jQuery.boxModel = jQuery.support.boxModel = div.offsetWidth === 2;

		if ( "zoom" in div.style ) {
			// Check if natively block-level elements act like inline-block
			// elements when setting their display to 'inline' and giving
			// them layout
			// (IE < 8 does this)
			div.style.display = "inline";
			div.style.zoom = 1;
			jQuery.support.inlineBlockNeedsLayout = div.offsetWidth === 2;

			// Check if elements with layout shrink-wrap their children
			// (IE 6 does this)
			div.style.display = "";
			div.innerHTML = "<div style='width:4px;'></div>";
			jQuery.support.shrinkWrapBlocks = div.offsetWidth !== 2;
		}

		div.innerHTML = "<table><tr><td style='padding:0;border:0;display:none'></td><td>t</td></tr></table>";
		var tds = div.getElementsByTagName("td");

		// Check if table cells still have offsetWidth/Height when they are set
		// to display:none and there are still other visible table cells in a
		// table row; if so, offsetWidth/Height are not reliable for use when
		// determining if an element has been hidden directly using
		// display:none (it is still safe to use offsets if a parent element is
		// hidden; don safety goggles and see bug #4512 for more information).
		// (only IE 8 fails this test)
		jQuery.support.reliableHiddenOffsets = tds[0].offsetHeight === 0;

		tds[0].style.display = "";
		tds[1].style.display = "none";

		// Check if empty table cells still have offsetWidth/Height
		// (IE < 8 fail this test)
		jQuery.support.reliableHiddenOffsets = jQuery.support.reliableHiddenOffsets && tds[0].offsetHeight === 0;
		div.innerHTML = "";

		body.removeChild( div ).style.display = "none";
		div = tds = null;
	});

	// Technique from Juriy Zaytsev
	// http://thinkweb2.com/projects/prototype/detecting-event-support-without-browser-sniffing/
	var eventSupported = function( eventName ) {
		var el = document.createElement("div");
		eventName = "on" + eventName;

		// We only care about the case where non-standard event systems
		// are used, namely in IE. Short-circuiting here helps us to
		// avoid an eval call (in setAttribute) which can cause CSP
		// to go haywire. See: https://developer.mozilla.org/en/Security/CSP
		if ( !el.attachEvent ) {
			return true;
		}

		var isSupported = (eventName in el);
		if ( !isSupported ) {
			el.setAttribute(eventName, "return;");
			isSupported = typeof el[eventName] === "function";
		}
		el = null;

		return isSupported;
	};

	jQuery.support.submitBubbles = eventSupported("submit");
	jQuery.support.changeBubbles = eventSupported("change");

	// release memory in IE
	div = all = a = null;
})();



var rbrace = /^(?:\{.*\}|\[.*\])$/;

jQuery.extend({
	cache: {},

	// Please use with caution
	uuid: 0,

	// Unique for each copy of jQuery on the page
	// Non-digits removed to match rinlinejQuery
	expando: "jQuery" + ( jQuery.fn.jquery + Math.random() ).replace( /\D/g, "" ),

	// The following elements throw uncatchable exceptions if you
	// attempt to add expando properties to them.
	noData: {
		"embed": true,
		// Ban all objects except for Flash (which handle expandos)
		"object": "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
		"applet": true
	},

	hasData: function( elem ) {
		elem = elem.nodeType ? jQuery.cache[ elem[jQuery.expando] ] : elem[ jQuery.expando ];

		return !!elem && !isEmptyDataObject( elem );
	},

	data: function( elem, name, data, pvt /* Internal Use Only */ ) {
		if ( !jQuery.acceptData( elem ) ) {
			return;
		}

		var internalKey = jQuery.expando, getByName = typeof name === "string", thisCache,

			// We have to handle DOM nodes and JS objects differently because IE6-7
			// can't GC object references properly across the DOM-JS boundary
			isNode = elem.nodeType,

			// Only DOM nodes need the global jQuery cache; JS object data is
			// attached directly to the object so GC can occur automatically
			cache = isNode ? jQuery.cache : elem,

			// Only defining an ID for JS objects if its cache already exists allows
			// the code to shortcut on the same path as a DOM node with no cache
			id = isNode ? elem[ jQuery.expando ] : elem[ jQuery.expando ] && jQuery.expando;

		// Avoid doing any more work than we need to when trying to get data on an
		// object that has no data at all
		if ( (!id || (pvt && id && !cache[ id ][ internalKey ])) && getByName && data === undefined ) {
			return;
		}

		if ( !id ) {
			// Only DOM nodes need a new unique ID for each element since their data
			// ends up in the global cache
			if ( isNode ) {
				elem[ jQuery.expando ] = id = ++jQuery.uuid;
			} else {
				id = jQuery.expando;
			}
		}

		if ( !cache[ id ] ) {
			cache[ id ] = {};

			// TODO: This is a hack for 1.5 ONLY. Avoids exposing jQuery
			// metadata on plain JS objects when the object is serialized using
			// JSON.stringify
			if ( !isNode ) {
				cache[ id ].toJSON = jQuery.noop;
			}
		}

		// An object can be passed to jQuery.data instead of a key/value pair; this gets
		// shallow copied over onto the existing cache
		if ( typeof name === "object" || typeof name === "function" ) {
			if ( pvt ) {
				cache[ id ][ internalKey ] = jQuery.extend(cache[ id ][ internalKey ], name);
			} else {
				cache[ id ] = jQuery.extend(cache[ id ], name);
			}
		}

		thisCache = cache[ id ];

		// Internal jQuery data is stored in a separate object inside the object's data
		// cache in order to avoid key collisions between internal data and user-defined
		// data
		if ( pvt ) {
			if ( !thisCache[ internalKey ] ) {
				thisCache[ internalKey ] = {};
			}

			thisCache = thisCache[ internalKey ];
		}

		if ( data !== undefined ) {
			thisCache[ name ] = data;
		}

		// TODO: This is a hack for 1.5 ONLY. It will be removed in 1.6. Users should
		// not attempt to inspect the internal events object using jQuery.data, as this
		// internal data object is undocumented and subject to change.
		if ( name === "events" && !thisCache[name] ) {
			return thisCache[ internalKey ] && thisCache[ internalKey ].events;
		}

		return getByName ? thisCache[ name ] : thisCache;
	},

	removeData: function( elem, name, pvt /* Internal Use Only */ ) {
		if ( !jQuery.acceptData( elem ) ) {
			return;
		}

		var internalKey = jQuery.expando, isNode = elem.nodeType,

			// See jQuery.data for more information
			cache = isNode ? jQuery.cache : elem,

			// See jQuery.data for more information
			id = isNode ? elem[ jQuery.expando ] : jQuery.expando;

		// If there is already no cache entry for this object, there is no
		// purpose in continuing
		if ( !cache[ id ] ) {
			return;
		}

		if ( name ) {
			var thisCache = pvt ? cache[ id ][ internalKey ] : cache[ id ];

			if ( thisCache ) {
				delete thisCache[ name ];

				// If there is no data left in the cache, we want to continue
				// and let the cache object itself get destroyed
				if ( !isEmptyDataObject(thisCache) ) {
					return;
				}
			}
		}

		// See jQuery.data for more information
		if ( pvt ) {
			delete cache[ id ][ internalKey ];

			// Don't destroy the parent cache unless the internal data object
			// had been the only thing left in it
			if ( !isEmptyDataObject(cache[ id ]) ) {
				return;
			}
		}

		var internalCache = cache[ id ][ internalKey ];

		// Browsers that fail expando deletion also refuse to delete expandos on
		// the window, but it will allow it on all other JS objects; other browsers
		// don't care
		if ( jQuery.support.deleteExpando || cache != window ) {
			delete cache[ id ];
		} else {
			cache[ id ] = null;
		}

		// We destroyed the entire user cache at once because it's faster than
		// iterating through each key, but we need to continue to persist internal
		// data if it existed
		if ( internalCache ) {
			cache[ id ] = {};
			// TODO: This is a hack for 1.5 ONLY. Avoids exposing jQuery
			// metadata on plain JS objects when the object is serialized using
			// JSON.stringify
			if ( !isNode ) {
				cache[ id ].toJSON = jQuery.noop;
			}

			cache[ id ][ internalKey ] = internalCache;

		// Otherwise, we need to eliminate the expando on the node to avoid
		// false lookups in the cache for entries that no longer exist
		} else if ( isNode ) {
			// IE does not allow us to delete expando properties from nodes,
			// nor does it have a removeAttribute function on Document nodes;
			// we must handle all of these cases
			if ( jQuery.support.deleteExpando ) {
				delete elem[ jQuery.expando ];
			} else if ( elem.removeAttribute ) {
				elem.removeAttribute( jQuery.expando );
			} else {
				elem[ jQuery.expando ] = null;
			}
		}
	},

	// For internal use only.
	_data: function( elem, name, data ) {
		return jQuery.data( elem, name, data, true );
	},

	// A method for determining if a DOM node can handle the data expando
	acceptData: function( elem ) {
		if ( elem.nodeName ) {
			var match = jQuery.noData[ elem.nodeName.toLowerCase() ];

			if ( match ) {
				return !(match === true || elem.getAttribute("classid") !== match);
			}
		}

		return true;
	}
});

jQuery.fn.extend({
	data: function( key, value ) {
		var data = null;

		if ( typeof key === "undefined" ) {
			if ( this.length ) {
				data = jQuery.data( this[0] );

				if ( this[0].nodeType === 1 ) {
					var attr = this[0].attributes, name;
					for ( var i = 0, l = attr.length; i < l; i++ ) {
						name = attr[i].name;

						if ( name.indexOf( "data-" ) === 0 ) {
							name = name.substr( 5 );
							dataAttr( this[0], name, data[ name ] );
						}
					}
				}
			}

			return data;

		} else if ( typeof key === "object" ) {
			return this.each(function() {
				jQuery.data( this, key );
			});
		}

		var parts = key.split(".");
		parts[1] = parts[1] ? "." + parts[1] : "";

		if ( value === undefined ) {
			data = this.triggerHandler("getData" + parts[1] + "!", [parts[0]]);

			// Try to fetch any internally stored data first
			if ( data === undefined && this.length ) {
				data = jQuery.data( this[0], key );
				data = dataAttr( this[0], key, data );
			}

			return data === undefined && parts[1] ?
				this.data( parts[0] ) :
				data;

		} else {
			return this.each(function() {
				var $this = jQuery( this ),
					args = [ parts[0], value ];

				$this.triggerHandler( "setData" + parts[1] + "!", args );
				jQuery.data( this, key, value );
				$this.triggerHandler( "changeData" + parts[1] + "!", args );
			});
		}
	},

	removeData: function( key ) {
		return this.each(function() {
			jQuery.removeData( this, key );
		});
	}
});

function dataAttr( elem, key, data ) {
	// If nothing was found internally, try to fetch any
	// data from the HTML5 data-* attribute
	if ( data === undefined && elem.nodeType === 1 ) {
		data = elem.getAttribute( "data-" + key );

		if ( typeof data === "string" ) {
			try {
				data = data === "true" ? true :
				data === "false" ? false :
				data === "null" ? null :
				!jQuery.isNaN( data ) ? parseFloat( data ) :
					rbrace.test( data ) ? jQuery.parseJSON( data ) :
					data;
			} catch( e ) {}

			// Make sure we set the data so it isn't changed later
			jQuery.data( elem, key, data );

		} else {
			data = undefined;
		}
	}

	return data;
}

// TODO: This is a hack for 1.5 ONLY to allow objects with a single toJSON
// property to be considered empty objects; this property always exists in
// order to make sure JSON.stringify does not expose internal metadata
function isEmptyDataObject( obj ) {
	for ( var name in obj ) {
		if ( name !== "toJSON" ) {
			return false;
		}
	}

	return true;
}




jQuery.extend({
	queue: function( elem, type, data ) {
		if ( !elem ) {
			return;
		}

		type = (type || "fx") + "queue";
		var q = jQuery._data( elem, type );

		// Speed up dequeue by getting out quickly if this is just a lookup
		if ( !data ) {
			return q || [];
		}

		if ( !q || jQuery.isArray(data) ) {
			q = jQuery._data( elem, type, jQuery.makeArray(data) );

		} else {
			q.push( data );
		}

		return q;
	},

	dequeue: function( elem, type ) {
		type = type || "fx";

		var queue = jQuery.queue( elem, type ),
			fn = queue.shift();

		// If the fx queue is dequeued, always remove the progress sentinel
		if ( fn === "inprogress" ) {
			fn = queue.shift();
		}

		if ( fn ) {
			// Add a progress sentinel to prevent the fx queue from being
			// automatically dequeued
			if ( type === "fx" ) {
				queue.unshift("inprogress");
			}

			fn.call(elem, function() {
				jQuery.dequeue(elem, type);
			});
		}

		if ( !queue.length ) {
			jQuery.removeData( elem, type + "queue", true );
		}
	}
});

jQuery.fn.extend({
	queue: function( type, data ) {
		if ( typeof type !== "string" ) {
			data = type;
			type = "fx";
		}

		if ( data === undefined ) {
			return jQuery.queue( this[0], type );
		}
		return this.each(function( i ) {
			var queue = jQuery.queue( this, type, data );

			if ( type === "fx" && queue[0] !== "inprogress" ) {
				jQuery.dequeue( this, type );
			}
		});
	},
	dequeue: function( type ) {
		return this.each(function() {
			jQuery.dequeue( this, type );
		});
	},

	// Based off of the plugin by Clint Helfers, with permission.
	// http://blindsignals.com/index.php/2009/07/jquery-delay/
	delay: function( time, type ) {
		time = jQuery.fx ? jQuery.fx.speeds[time] || time : time;
		type = type || "fx";

		return this.queue( type, function() {
			var elem = this;
			setTimeout(function() {
				jQuery.dequeue( elem, type );
			}, time );
		});
	},

	clearQueue: function( type ) {
		return this.queue( type || "fx", [] );
	}
});




var rclass = /[\n\t\r]/g,
	rspaces = /\s+/,
	rreturn = /\r/g,
	rspecialurl = /^(?:href|src|style)$/,
	rtype = /^(?:button|input)$/i,
	rfocusable = /^(?:button|input|object|select|textarea)$/i,
	rclickable = /^a(?:rea)?$/i,
	rradiocheck = /^(?:radio|checkbox)$/i;

jQuery.props = {
	"for": "htmlFor",
	"class": "className",
	readonly: "readOnly",
	maxlength: "maxLength",
	cellspacing: "cellSpacing",
	rowspan: "rowSpan",
	colspan: "colSpan",
	tabindex: "tabIndex",
	usemap: "useMap",
	frameborder: "frameBorder"
};

jQuery.fn.extend({
	attr: function( name, value ) {
		return jQuery.access( this, name, value, true, jQuery.attr );
	},

	removeAttr: function( name, fn ) {
		return this.each(function(){
			jQuery.attr( this, name, "" );
			if ( this.nodeType === 1 ) {
				this.removeAttribute( name );
			}
		});
	},

	addClass: function( value ) {
		if ( jQuery.isFunction(value) ) {
			return this.each(function(i) {
				var self = jQuery(this);
				self.addClass( value.call(this, i, self.attr("class")) );
			});
		}

		if ( value && typeof value === "string" ) {
			var classNames = (value || "").split( rspaces );

			for ( var i = 0, l = this.length; i < l; i++ ) {
				var elem = this[i];

				if ( elem.nodeType === 1 ) {
					if ( !elem.className ) {
						elem.className = value;

					} else {
						var className = " " + elem.className + " ",
							setClass = elem.className;

						for ( var c = 0, cl = classNames.length; c < cl; c++ ) {
							if ( className.indexOf( " " + classNames[c] + " " ) < 0 ) {
								setClass += " " + classNames[c];
							}
						}
						elem.className = jQuery.trim( setClass );
					}
				}
			}
		}

		return this;
	},

	removeClass: function( value ) {
		if ( jQuery.isFunction(value) ) {
			return this.each(function(i) {
				var self = jQuery(this);
				self.removeClass( value.call(this, i, self.attr("class")) );
			});
		}

		if ( (value && typeof value === "string") || value === undefined ) {
			var classNames = (value || "").split( rspaces );

			for ( var i = 0, l = this.length; i < l; i++ ) {
				var elem = this[i];

				if ( elem.nodeType === 1 && elem.className ) {
					if ( value ) {
						var className = (" " + elem.className + " ").replace(rclass, " ");
						for ( var c = 0, cl = classNames.length; c < cl; c++ ) {
							className = className.replace(" " + classNames[c] + " ", " ");
						}
						elem.className = jQuery.trim( className );

					} else {
						elem.className = "";
					}
				}
			}
		}

		return this;
	},

	toggleClass: function( value, stateVal ) {
		var type = typeof value,
			isBool = typeof stateVal === "boolean";

		if ( jQuery.isFunction( value ) ) {
			return this.each(function(i) {
				var self = jQuery(this);
				self.toggleClass( value.call(this, i, self.attr("class"), stateVal), stateVal );
			});
		}

		return this.each(function() {
			if ( type === "string" ) {
				// toggle individual class names
				var className,
					i = 0,
					self = jQuery( this ),
					state = stateVal,
					classNames = value.split( rspaces );

				while ( (className = classNames[ i++ ]) ) {
					// check each className given, space seperated list
					state = isBool ? state : !self.hasClass( className );
					self[ state ? "addClass" : "removeClass" ]( className );
				}

			} else if ( type === "undefined" || type === "boolean" ) {
				if ( this.className ) {
					// store className if set
					jQuery._data( this, "__className__", this.className );
				}

				// toggle whole className
				this.className = this.className || value === false ? "" : jQuery._data( this, "__className__" ) || "";
			}
		});
	},

	hasClass: function( selector ) {
		var className = " " + selector + " ";
		for ( var i = 0, l = this.length; i < l; i++ ) {
			if ( (" " + this[i].className + " ").replace(rclass, " ").indexOf( className ) > -1 ) {
				return true;
			}
		}

		return false;
	},

	val: function( value ) {
		if ( !arguments.length ) {
			var elem = this[0];

			if ( elem ) {
				if ( jQuery.nodeName( elem, "option" ) ) {
					// attributes.value is undefined in Blackberry 4.7 but
					// uses .value. See #6932
					var val = elem.attributes.value;
					return !val || val.specified ? elem.value : elem.text;
				}

				// We need to handle select boxes special
				if ( jQuery.nodeName( elem, "select" ) ) {
					var index = elem.selectedIndex,
						values = [],
						options = elem.options,
						one = elem.type === "select-one";

					// Nothing was selected
					if ( index < 0 ) {
						return null;
					}

					// Loop through all the selected options
					for ( var i = one ? index : 0, max = one ? index + 1 : options.length; i < max; i++ ) {
						var option = options[ i ];

						// Don't return options that are disabled or in a disabled optgroup
						if ( option.selected && (jQuery.support.optDisabled ? !option.disabled : option.getAttribute("disabled") === null) &&
								(!option.parentNode.disabled || !jQuery.nodeName( option.parentNode, "optgroup" )) ) {

							// Get the specific value for the option
							value = jQuery(option).val();

							// We don't need an array for one selects
							if ( one ) {
								return value;
							}

							// Multi-Selects return an array
							values.push( value );
						}
					}

					// Fixes Bug #2551 -- select.val() broken in IE after form.reset()
					if ( one && !values.length && options.length ) {
						return jQuery( options[ index ] ).val();
					}

					return values;
				}

				// Handle the case where in Webkit "" is returned instead of "on" if a value isn't specified
				if ( rradiocheck.test( elem.type ) && !jQuery.support.checkOn ) {
					return elem.getAttribute("value") === null ? "on" : elem.value;
				}

				// Everything else, we just grab the value
				return (elem.value || "").replace(rreturn, "");

			}

			return undefined;
		}

		var isFunction = jQuery.isFunction(value);

		return this.each(function(i) {
			var self = jQuery(this), val = value;

			if ( this.nodeType !== 1 ) {
				return;
			}

			if ( isFunction ) {
				val = value.call(this, i, self.val());
			}

			// Treat null/undefined as ""; convert numbers to string
			if ( val == null ) {
				val = "";
			} else if ( typeof val === "number" ) {
				val += "";
			} else if ( jQuery.isArray(val) ) {
				val = jQuery.map(val, function (value) {
					return value == null ? "" : value + "";
				});
			}

			if ( jQuery.isArray(val) && rradiocheck.test( this.type ) ) {
				this.checked = jQuery.inArray( self.val(), val ) >= 0;

			} else if ( jQuery.nodeName( this, "select" ) ) {
				var values = jQuery.makeArray(val);

				jQuery( "option", this ).each(function() {
					this.selected = jQuery.inArray( jQuery(this).val(), values ) >= 0;
				});

				if ( !values.length ) {
					this.selectedIndex = -1;
				}

			} else {
				this.value = val;
			}
		});
	}
});

jQuery.extend({
	attrFn: {
		val: true,
		css: true,
		html: true,
		text: true,
		data: true,
		width: true,
		height: true,
		offset: true
	},

	attr: function( elem, name, value, pass ) {
		// don't get/set attributes on text, comment and attribute nodes
		if ( !elem || elem.nodeType === 3 || elem.nodeType === 8 || elem.nodeType === 2 ) {
			return undefined;
		}

		if ( pass && name in jQuery.attrFn ) {
			return jQuery(elem)[name](value);
		}

		var notxml = elem.nodeType !== 1 || !jQuery.isXMLDoc( elem ),
			// Whether we are setting (or getting)
			set = value !== undefined;

		// Try to normalize/fix the name
		name = notxml && jQuery.props[ name ] || name;

		// Only do all the following if this is a node (faster for style)
		if ( elem.nodeType === 1 ) {
			// These attributes require special treatment
			var special = rspecialurl.test( name );

			// Safari mis-reports the default selected property of an option
			// Accessing the parent's selectedIndex property fixes it
			if ( name === "selected" && !jQuery.support.optSelected ) {
				var parent = elem.parentNode;
				if ( parent ) {
					parent.selectedIndex;

					// Make sure that it also works with optgroups, see #5701
					if ( parent.parentNode ) {
						parent.parentNode.selectedIndex;
					}
				}
			}

			// If applicable, access the attribute via the DOM 0 way
			// 'in' checks fail in Blackberry 4.7 #6931
			if ( (name in elem || elem[ name ] !== undefined) && notxml && !special ) {
				if ( set ) {
					// We can't allow the type property to be changed (since it causes problems in IE)
					if ( name === "type" && rtype.test( elem.nodeName ) && elem.parentNode ) {
						jQuery.error( "type property can't be changed" );
					}

					if ( value === null ) {
						if ( elem.nodeType === 1 ) {
							elem.removeAttribute( name );
						}

					} else {
						elem[ name ] = value;
					}
				}

				// browsers index elements by id/name on forms, give priority to attributes.
				if ( jQuery.nodeName( elem, "form" ) && elem.getAttributeNode(name) ) {
					return elem.getAttributeNode( name ).nodeValue;
				}

				// elem.tabIndex doesn't always return the correct value when it hasn't been explicitly set
				// http://fluidproject.org/blog/2008/01/09/getting-setting-and-removing-tabindex-values-with-javascript/
				if ( name === "tabIndex" ) {
					var attributeNode = elem.getAttributeNode( "tabIndex" );

					return attributeNode && attributeNode.specified ?
						attributeNode.value :
						rfocusable.test( elem.nodeName ) || rclickable.test( elem.nodeName ) && elem.href ?
							0 :
							undefined;
				}

				return elem[ name ];
			}

			if ( !jQuery.support.style && notxml && name === "style" ) {
				if ( set ) {
					elem.style.cssText = "" + value;
				}

				return elem.style.cssText;
			}

			if ( set ) {
				// convert the value to a string (all browsers do this but IE) see #1070
				elem.setAttribute( name, "" + value );
			}

			// Ensure that missing attributes return undefined
			// Blackberry 4.7 returns "" from getAttribute #6938
			if ( !elem.attributes[ name ] && (elem.hasAttribute && !elem.hasAttribute( name )) ) {
				return undefined;
			}

			var attr = !jQuery.support.hrefNormalized && notxml && special ?
					// Some attributes require a special call on IE
					elem.getAttribute( name, 2 ) :
					elem.getAttribute( name );

			// Non-existent attributes return null, we normalize to undefined
			return attr === null ? undefined : attr;
		}
		// Handle everything which isn't a DOM element node
		if ( set ) {
			elem[ name ] = value;
		}
		return elem[ name ];
	}
});




var rnamespaces = /\.(.*)$/,
	rformElems = /^(?:textarea|input|select)$/i,
	rperiod = /\./g,
	rspace = / /g,
	rescape = /[^\w\s.|`]/g,
	fcleanup = function( nm ) {
		return nm.replace(rescape, "\\$&");
	};

/*
 * A number of helper functions used for managing events.
 * Many of the ideas behind this code originated from
 * Dean Edwards' addEvent library.
 */
jQuery.event = {

	// Bind an event to an element
	// Original by Dean Edwards
	add: function( elem, types, handler, data ) {
		if ( elem.nodeType === 3 || elem.nodeType === 8 ) {
			return;
		}

		// TODO :: Use a try/catch until it's safe to pull this out (likely 1.6)
		// Minor release fix for bug #8018
		try {
			// For whatever reason, IE has trouble passing the window object
			// around, causing it to be cloned in the process
			if ( jQuery.isWindow( elem ) && ( elem !== window && !elem.frameElement ) ) {
				elem = window;
			}
		}
		catch ( e ) {}

		if ( handler === false ) {
			handler = returnFalse;
		} else if ( !handler ) {
			// Fixes bug #7229. Fix recommended by jdalton
			return;
		}

		var handleObjIn, handleObj;

		if ( handler.handler ) {
			handleObjIn = handler;
			handler = handleObjIn.handler;
		}

		// Make sure that the function being executed has a unique ID
		if ( !handler.guid ) {
			handler.guid = jQuery.guid++;
		}

		// Init the element's event structure
		var elemData = jQuery._data( elem );

		// If no elemData is found then we must be trying to bind to one of the
		// banned noData elements
		if ( !elemData ) {
			return;
		}

		var events = elemData.events,
			eventHandle = elemData.handle;

		if ( !events ) {
			elemData.events = events = {};
		}

		if ( !eventHandle ) {
			elemData.handle = eventHandle = function() {
				// Handle the second event of a trigger and when
				// an event is called after a page has unloaded
				return typeof jQuery !== "undefined" && !jQuery.event.triggered ?
					jQuery.event.handle.apply( eventHandle.elem, arguments ) :
					undefined;
			};
		}

		// Add elem as a property of the handle function
		// This is to prevent a memory leak with non-native events in IE.
		eventHandle.elem = elem;

		// Handle multiple events separated by a space
		// jQuery(...).bind("mouseover mouseout", fn);
		types = types.split(" ");

		var type, i = 0, namespaces;

		while ( (type = types[ i++ ]) ) {
			handleObj = handleObjIn ?
				jQuery.extend({}, handleObjIn) :
				{ handler: handler, data: data };

			// Namespaced event handlers
			if ( type.indexOf(".") > -1 ) {
				namespaces = type.split(".");
				type = namespaces.shift();
				handleObj.namespace = namespaces.slice(0).sort().join(".");

			} else {
				namespaces = [];
				handleObj.namespace = "";
			}

			handleObj.type = type;
			if ( !handleObj.guid ) {
				handleObj.guid = handler.guid;
			}

			// Get the current list of functions bound to this event
			var handlers = events[ type ],
				special = jQuery.event.special[ type ] || {};

			// Init the event handler queue
			if ( !handlers ) {
				handlers = events[ type ] = [];

				// Check for a special event handler
				// Only use addEventListener/attachEvent if the special
				// events handler returns false
				if ( !special.setup || special.setup.call( elem, data, namespaces, eventHandle ) === false ) {
					// Bind the global event handler to the element
					if ( elem.addEventListener ) {
						elem.addEventListener( type, eventHandle, false );

					} else if ( elem.attachEvent ) {
						elem.attachEvent( "on" + type, eventHandle );
					}
				}
			}

			if ( special.add ) {
				special.add.call( elem, handleObj );

				if ( !handleObj.handler.guid ) {
					handleObj.handler.guid = handler.guid;
				}
			}

			// Add the function to the element's handler list
			handlers.push( handleObj );

			// Keep track of which events have been used, for global triggering
			jQuery.event.global[ type ] = true;
		}

		// Nullify elem to prevent memory leaks in IE
		elem = null;
	},

	global: {},

	// Detach an event or set of events from an element
	remove: function( elem, types, handler, pos ) {
		// don't do events on text and comment nodes
		if ( elem.nodeType === 3 || elem.nodeType === 8 ) {
			return;
		}

		if ( handler === false ) {
			handler = returnFalse;
		}

		var ret, type, fn, j, i = 0, all, namespaces, namespace, special, eventType, handleObj, origType,
			elemData = jQuery.hasData( elem ) && jQuery._data( elem ),
			events = elemData && elemData.events;

		if ( !elemData || !events ) {
			return;
		}

		// types is actually an event object here
		if ( types && types.type ) {
			handler = types.handler;
			types = types.type;
		}

		// Unbind all events for the element
		if ( !types || typeof types === "string" && types.charAt(0) === "." ) {
			types = types || "";

			for ( type in events ) {
				jQuery.event.remove( elem, type + types );
			}

			return;
		}

		// Handle multiple events separated by a space
		// jQuery(...).unbind("mouseover mouseout", fn);
		types = types.split(" ");

		while ( (type = types[ i++ ]) ) {
			origType = type;
			handleObj = null;
			all = type.indexOf(".") < 0;
			namespaces = [];

			if ( !all ) {
				// Namespaced event handlers
				namespaces = type.split(".");
				type = namespaces.shift();

				namespace = new RegExp("(^|\\.)" +
					jQuery.map( namespaces.slice(0).sort(), fcleanup ).join("\\.(?:.*\\.)?") + "(\\.|$)");
			}

			eventType = events[ type ];

			if ( !eventType ) {
				continue;
			}

			if ( !handler ) {
				for ( j = 0; j < eventType.length; j++ ) {
					handleObj = eventType[ j ];

					if ( all || namespace.test( handleObj.namespace ) ) {
						jQuery.event.remove( elem, origType, handleObj.handler, j );
						eventType.splice( j--, 1 );
					}
				}

				continue;
			}

			special = jQuery.event.special[ type ] || {};

			for ( j = pos || 0; j < eventType.length; j++ ) {
				handleObj = eventType[ j ];

				if ( handler.guid === handleObj.guid ) {
					// remove the given handler for the given type
					if ( all || namespace.test( handleObj.namespace ) ) {
						if ( pos == null ) {
							eventType.splice( j--, 1 );
						}

						if ( special.remove ) {
							special.remove.call( elem, handleObj );
						}
					}

					if ( pos != null ) {
						break;
					}
				}
			}

			// remove generic event handler if no more handlers exist
			if ( eventType.length === 0 || pos != null && eventType.length === 1 ) {
				if ( !special.teardown || special.teardown.call( elem, namespaces ) === false ) {
					jQuery.removeEvent( elem, type, elemData.handle );
				}

				ret = null;
				delete events[ type ];
			}
		}

		// Remove the expando if it's no longer used
		if ( jQuery.isEmptyObject( events ) ) {
			var handle = elemData.handle;
			if ( handle ) {
				handle.elem = null;
			}

			delete elemData.events;
			delete elemData.handle;

			if ( jQuery.isEmptyObject( elemData ) ) {
				jQuery.removeData( elem, undefined, true );
			}
		}
	},

	// bubbling is internal
	trigger: function( event, data, elem /*, bubbling */ ) {
		// Event object or event type
		var type = event.type || event,
			bubbling = arguments[3];

		if ( !bubbling ) {
			event = typeof event === "object" ?
				// jQuery.Event object
				event[ jQuery.expando ] ? event :
				// Object literal
				jQuery.extend( jQuery.Event(type), event ) :
				// Just the event type (string)
				jQuery.Event(type);

			if ( type.indexOf("!") >= 0 ) {
				event.type = type = type.slice(0, -1);
				event.exclusive = true;
			}

			// Handle a global trigger
			if ( !elem ) {
				// Don't bubble custom events when global (to avoid too much overhead)
				event.stopPropagation();

				// Only trigger if we've ever bound an event for it
				if ( jQuery.event.global[ type ] ) {
					// XXX This code smells terrible. event.js should not be directly
					// inspecting the data cache
					jQuery.each( jQuery.cache, function() {
						// internalKey variable is just used to make it easier to find
						// and potentially change this stuff later; currently it just
						// points to jQuery.expando
						var internalKey = jQuery.expando,
							internalCache = this[ internalKey ];
						if ( internalCache && internalCache.events && internalCache.events[ type ] ) {
							jQuery.event.trigger( event, data, internalCache.handle.elem );
						}
					});
				}
			}

			// Handle triggering a single element

			// don't do events on text and comment nodes
			if ( !elem || elem.nodeType === 3 || elem.nodeType === 8 ) {
				return undefined;
			}

			// Clean up in case it is reused
			event.result = undefined;
			event.target = elem;

			// Clone the incoming data, if any
			data = jQuery.makeArray( data );
			data.unshift( event );
		}

		event.currentTarget = elem;

		// Trigger the event, it is assumed that "handle" is a function
		var handle = jQuery._data( elem, "handle" );

		if ( handle ) {
			handle.apply( elem, data );
		}

		var parent = elem.parentNode || elem.ownerDocument;

		// Trigger an inline bound script
		try {
			if ( !(elem && elem.nodeName && jQuery.noData[elem.nodeName.toLowerCase()]) ) {
				if ( elem[ "on" + type ] && elem[ "on" + type ].apply( elem, data ) === false ) {
					event.result = false;
					event.preventDefault();
				}
			}

		// prevent IE from throwing an error for some elements with some event types, see #3533
		} catch (inlineError) {}

		if ( !event.isPropagationStopped() && parent ) {
			jQuery.event.trigger( event, data, parent, true );

		} else if ( !event.isDefaultPrevented() ) {
			var old,
				target = event.target,
				targetType = type.replace( rnamespaces, "" ),
				isClick = jQuery.nodeName( target, "a" ) && targetType === "click",
				special = jQuery.event.special[ targetType ] || {};

			if ( (!special._default || special._default.call( elem, event ) === false) &&
				!isClick && !(target && target.nodeName && jQuery.noData[target.nodeName.toLowerCase()]) ) {

				try {
					if ( target[ targetType ] ) {
						// Make sure that we don't accidentally re-trigger the onFOO events
						old = target[ "on" + targetType ];

						if ( old ) {
							target[ "on" + targetType ] = null;
						}

						jQuery.event.triggered = true;
						target[ targetType ]();
					}

				// prevent IE from throwing an error for some elements with some event types, see #3533
				} catch (triggerError) {}

				if ( old ) {
					target[ "on" + targetType ] = old;
				}

				jQuery.event.triggered = false;
			}
		}
	},

	handle: function( event ) {
		var all, handlers, namespaces, namespace_re, events,
			namespace_sort = [],
			args = jQuery.makeArray( arguments );

		event = args[0] = jQuery.event.fix( event || window.event );
		event.currentTarget = this;

		// Namespaced event handlers
		all = event.type.indexOf(".") < 0 && !event.exclusive;

		if ( !all ) {
			namespaces = event.type.split(".");
			event.type = namespaces.shift();
			namespace_sort = namespaces.slice(0).sort();
			namespace_re = new RegExp("(^|\\.)" + namespace_sort.join("\\.(?:.*\\.)?") + "(\\.|$)");
		}

		event.namespace = event.namespace || namespace_sort.join(".");

		events = jQuery._data(this, "events");

		handlers = (events || {})[ event.type ];

		if ( events && handlers ) {
			// Clone the handlers to prevent manipulation
			handlers = handlers.slice(0);

			for ( var j = 0, l = handlers.length; j < l; j++ ) {
				var handleObj = handlers[ j ];

				// Filter the functions by class
				if ( all || namespace_re.test( handleObj.namespace ) ) {
					// Pass in a reference to the handler function itself
					// So that we can later remove it
					event.handler = handleObj.handler;
					event.data = handleObj.data;
					event.handleObj = handleObj;

					var ret = handleObj.handler.apply( this, args );

					if ( ret !== undefined ) {
						event.result = ret;
						if ( ret === false ) {
							event.preventDefault();
							event.stopPropagation();
						}
					}

					if ( event.isImmediatePropagationStopped() ) {
						break;
					}
				}
			}
		}

		return event.result;
	},

	props: "altKey attrChange attrName bubbles button cancelable charCode clientX clientY ctrlKey currentTarget data detail eventPhase fromElement handler keyCode layerX layerY metaKey newValue offsetX offsetY pageX pageY prevValue relatedNode relatedTarget screenX screenY shiftKey srcElement target toElement view wheelDelta which".split(" "),

	fix: function( event ) {
		if ( event[ jQuery.expando ] ) {
			return event;
		}

		// store a copy of the original event object
		// and "clone" to set read-only properties
		var originalEvent = event;
		event = jQuery.Event( originalEvent );

		for ( var i = this.props.length, prop; i; ) {
			prop = this.props[ --i ];
			event[ prop ] = originalEvent[ prop ];
		}

		// Fix target property, if necessary
		if ( !event.target ) {
			// Fixes #1925 where srcElement might not be defined either
			event.target = event.srcElement || document;
		}

		// check if target is a textnode (safari)
		if ( event.target.nodeType === 3 ) {
			event.target = event.target.parentNode;
		}

		// Add relatedTarget, if necessary
		if ( !event.relatedTarget && event.fromElement ) {
			event.relatedTarget = event.fromElement === event.target ? event.toElement : event.fromElement;
		}

		// Calculate pageX/Y if missing and clientX/Y available
		if ( event.pageX == null && event.clientX != null ) {
			var doc = document.documentElement,
				body = document.body;

			event.pageX = event.clientX + (doc && doc.scrollLeft || body && body.scrollLeft || 0) - (doc && doc.clientLeft || body && body.clientLeft || 0);
			event.pageY = event.clientY + (doc && doc.scrollTop  || body && body.scrollTop  || 0) - (doc && doc.clientTop  || body && body.clientTop  || 0);
		}

		// Add which for key events
		if ( event.which == null && (event.charCode != null || event.keyCode != null) ) {
			event.which = event.charCode != null ? event.charCode : event.keyCode;
		}

		// Add metaKey to non-Mac browsers (use ctrl for PC's and Meta for Macs)
		if ( !event.metaKey && event.ctrlKey ) {
			event.metaKey = event.ctrlKey;
		}

		// Add which for click: 1 === left; 2 === middle; 3 === right
		// Note: button is not normalized, so don't use it
		if ( !event.which && event.button !== undefined ) {
			event.which = (event.button & 1 ? 1 : ( event.button & 2 ? 3 : ( event.button & 4 ? 2 : 0 ) ));
		}

		return event;
	},

	// Deprecated, use jQuery.guid instead
	guid: 1E8,

	// Deprecated, use jQuery.proxy instead
	proxy: jQuery.proxy,

	special: {
		ready: {
			// Make sure the ready event is setup
			setup: jQuery.bindReady,
			teardown: jQuery.noop
		},

		live: {
			add: function( handleObj ) {
				jQuery.event.add( this,
					liveConvert( handleObj.origType, handleObj.selector ),
					jQuery.extend({}, handleObj, {handler: liveHandler, guid: handleObj.handler.guid}) );
			},

			remove: function( handleObj ) {
				jQuery.event.remove( this, liveConvert( handleObj.origType, handleObj.selector ), handleObj );
			}
		},

		beforeunload: {
			setup: function( data, namespaces, eventHandle ) {
				// We only want to do this special case on windows
				if ( jQuery.isWindow( this ) ) {
					this.onbeforeunload = eventHandle;
				}
			},

			teardown: function( namespaces, eventHandle ) {
				if ( this.onbeforeunload === eventHandle ) {
					this.onbeforeunload = null;
				}
			}
		}
	}
};

jQuery.removeEvent = document.removeEventListener ?
	function( elem, type, handle ) {
		if ( elem.removeEventListener ) {
			elem.removeEventListener( type, handle, false );
		}
	} :
	function( elem, type, handle ) {
		if ( elem.detachEvent ) {
			elem.detachEvent( "on" + type, handle );
		}
	};

jQuery.Event = function( src ) {
	// Allow instantiation without the 'new' keyword
	if ( !this.preventDefault ) {
		return new jQuery.Event( src );
	}

	// Event object
	if ( src && src.type ) {
		this.originalEvent = src;
		this.type = src.type;

		// Events bubbling up the document may have been marked as prevented
		// by a handler lower down the tree; reflect the correct value.
		this.isDefaultPrevented = (src.defaultPrevented || src.returnValue === false ||
			src.getPreventDefault && src.getPreventDefault()) ? returnTrue : returnFalse;

	// Event type
	} else {
		this.type = src;
	}

	// timeStamp is buggy for some events on Firefox(#3843)
	// So we won't rely on the native value
	this.timeStamp = jQuery.now();

	// Mark it as fixed
	this[ jQuery.expando ] = true;
};

function returnFalse() {
	return false;
}
function returnTrue() {
	return true;
}

// jQuery.Event is based on DOM3 Events as specified by the ECMAScript Language Binding
// http://www.w3.org/TR/2003/WD-DOM-Level-3-Events-20030331/ecma-script-binding.html
jQuery.Event.prototype = {
	preventDefault: function() {
		this.isDefaultPrevented = returnTrue;

		var e = this.originalEvent;
		if ( !e ) {
			return;
		}

		// if preventDefault exists run it on the original event
		if ( e.preventDefault ) {
			e.preventDefault();

		// otherwise set the returnValue property of the original event to false (IE)
		} else {
			e.returnValue = false;
		}
	},
	stopPropagation: function() {
		this.isPropagationStopped = returnTrue;

		var e = this.originalEvent;
		if ( !e ) {
			return;
		}
		// if stopPropagation exists run it on the original event
		if ( e.stopPropagation ) {
			e.stopPropagation();
		}
		// otherwise set the cancelBubble property of the original event to true (IE)
		e.cancelBubble = true;
	},
	stopImmediatePropagation: function() {
		this.isImmediatePropagationStopped = returnTrue;
		this.stopPropagation();
	},
	isDefaultPrevented: returnFalse,
	isPropagationStopped: returnFalse,
	isImmediatePropagationStopped: returnFalse
};

// Checks if an event happened on an element within another element
// Used in jQuery.event.special.mouseenter and mouseleave handlers
var withinElement = function( event ) {
	// Check if mouse(over|out) are still within the same parent element
	var parent = event.relatedTarget;

	// Firefox sometimes assigns relatedTarget a XUL element
	// which we cannot access the parentNode property of
	try {

		// Chrome does something similar, the parentNode property
		// can be accessed but is null.
		if ( parent !== document && !parent.parentNode ) {
			return;
		}
		// Traverse up the tree
		while ( parent && parent !== this ) {
			parent = parent.parentNode;
		}

		if ( parent !== this ) {
			// set the correct event type
			event.type = event.data;

			// handle event if we actually just moused on to a non sub-element
			jQuery.event.handle.apply( this, arguments );
		}

	// assuming we've left the element since we most likely mousedover a xul element
	} catch(e) { }
},

// In case of event delegation, we only need to rename the event.type,
// liveHandler will take care of the rest.
delegate = function( event ) {
	event.type = event.data;
	jQuery.event.handle.apply( this, arguments );
};

// Create mouseenter and mouseleave events
jQuery.each({
	mouseenter: "mouseover",
	mouseleave: "mouseout"
}, function( orig, fix ) {
	jQuery.event.special[ orig ] = {
		setup: function( data ) {
			jQuery.event.add( this, fix, data && data.selector ? delegate : withinElement, orig );
		},
		teardown: function( data ) {
			jQuery.event.remove( this, fix, data && data.selector ? delegate : withinElement );
		}
	};
});

// submit delegation
if ( !jQuery.support.submitBubbles ) {

	jQuery.event.special.submit = {
		setup: function( data, namespaces ) {
			if ( this.nodeName && this.nodeName.toLowerCase() !== "form" ) {
				jQuery.event.add(this, "click.specialSubmit", function( e ) {
					var elem = e.target,
						type = elem.type;

					if ( (type === "submit" || type === "image") && jQuery( elem ).closest("form").length ) {
						trigger( "submit", this, arguments );
					}
				});

				jQuery.event.add(this, "keypress.specialSubmit", function( e ) {
					var elem = e.target,
						type = elem.type;

					if ( (type === "text" || type === "password") && jQuery( elem ).closest("form").length && e.keyCode === 13 ) {
						trigger( "submit", this, arguments );
					}
				});

			} else {
				return false;
			}
		},

		teardown: function( namespaces ) {
			jQuery.event.remove( this, ".specialSubmit" );
		}
	};

}

// change delegation, happens here so we have bind.
if ( !jQuery.support.changeBubbles ) {

	var changeFilters,

	getVal = function( elem ) {
		var type = elem.type, val = elem.value;

		if ( type === "radio" || type === "checkbox" ) {
			val = elem.checked;

		} else if ( type === "select-multiple" ) {
			val = elem.selectedIndex > -1 ?
				jQuery.map( elem.options, function( elem ) {
					return elem.selected;
				}).join("-") :
				"";

		} else if ( elem.nodeName.toLowerCase() === "select" ) {
			val = elem.selectedIndex;
		}

		return val;
	},

	testChange = function testChange( e ) {
		var elem = e.target, data, val;

		if ( !rformElems.test( elem.nodeName ) || elem.readOnly ) {
			return;
		}

		data = jQuery._data( elem, "_change_data" );
		val = getVal(elem);

		// the current data will be also retrieved by beforeactivate
		if ( e.type !== "focusout" || elem.type !== "radio" ) {
			jQuery._data( elem, "_change_data", val );
		}

		if ( data === undefined || val === data ) {
			return;
		}

		if ( data != null || val ) {
			e.type = "change";
			e.liveFired = undefined;
			jQuery.event.trigger( e, arguments[1], elem );
		}
	};

	jQuery.event.special.change = {
		filters: {
			focusout: testChange,

			beforedeactivate: testChange,

			click: function( e ) {
				var elem = e.target, type = elem.type;

				if ( type === "radio" || type === "checkbox" || elem.nodeName.toLowerCase() === "select" ) {
					testChange.call( this, e );
				}
			},

			// Change has to be called before submit
			// Keydown will be called before keypress, which is used in submit-event delegation
			keydown: function( e ) {
				var elem = e.target, type = elem.type;

				if ( (e.keyCode === 13 && elem.nodeName.toLowerCase() !== "textarea") ||
					(e.keyCode === 32 && (type === "checkbox" || type === "radio")) ||
					type === "select-multiple" ) {
					testChange.call( this, e );
				}
			},

			// Beforeactivate happens also before the previous element is blurred
			// with this event you can't trigger a change event, but you can store
			// information
			beforeactivate: function( e ) {
				var elem = e.target;
				jQuery._data( elem, "_change_data", getVal(elem) );
			}
		},

		setup: function( data, namespaces ) {
			if ( this.type === "file" ) {
				return false;
			}

			for ( var type in changeFilters ) {
				jQuery.event.add( this, type + ".specialChange", changeFilters[type] );
			}

			return rformElems.test( this.nodeName );
		},

		teardown: function( namespaces ) {
			jQuery.event.remove( this, ".specialChange" );

			return rformElems.test( this.nodeName );
		}
	};

	changeFilters = jQuery.event.special.change.filters;

	// Handle when the input is .focus()'d
	changeFilters.focus = changeFilters.beforeactivate;
}

function trigger( type, elem, args ) {
	// Piggyback on a donor event to simulate a different one.
	// Fake originalEvent to avoid donor's stopPropagation, but if the
	// simulated event prevents default then we do the same on the donor.
	// Don't pass args or remember liveFired; they apply to the donor event.
	var event = jQuery.extend( {}, args[ 0 ] );
	event.type = type;
	event.originalEvent = {};
	event.liveFired = undefined;
	jQuery.event.handle.call( elem, event );
	if ( event.isDefaultPrevented() ) {
		args[ 0 ].preventDefault();
	}
}

// Create "bubbling" focus and blur events
if ( document.addEventListener ) {
	jQuery.each({ focus: "focusin", blur: "focusout" }, function( orig, fix ) {
		jQuery.event.special[ fix ] = {
			setup: function() {
				this.addEventListener( orig, handler, true );
			},
			teardown: function() {
				this.removeEventListener( orig, handler, true );
			}
		};

		function handler( e ) {
			e = jQuery.event.fix( e );
			e.type = fix;
			return jQuery.event.handle.call( this, e );
		}
	});
}

jQuery.each(["bind", "one"], function( i, name ) {
	jQuery.fn[ name ] = function( type, data, fn ) {
		// Handle object literals
		if ( typeof type === "object" ) {
			for ( var key in type ) {
				this[ name ](key, data, type[key], fn);
			}
			return this;
		}

		if ( jQuery.isFunction( data ) || data === false ) {
			fn = data;
			data = undefined;
		}

		var handler = name === "one" ? jQuery.proxy( fn, function( event ) {
			jQuery( this ).unbind( event, handler );
			return fn.apply( this, arguments );
		}) : fn;

		if ( type === "unload" && name !== "one" ) {
			this.one( type, data, fn );

		} else {
			for ( var i = 0, l = this.length; i < l; i++ ) {
				jQuery.event.add( this[i], type, handler, data );
			}
		}

		return this;
	};
});

jQuery.fn.extend({
	unbind: function( type, fn ) {
		// Handle object literals
		if ( typeof type === "object" && !type.preventDefault ) {
			for ( var key in type ) {
				this.unbind(key, type[key]);
			}

		} else {
			for ( var i = 0, l = this.length; i < l; i++ ) {
				jQuery.event.remove( this[i], type, fn );
			}
		}

		return this;
	},

	delegate: function( selector, types, data, fn ) {
		return this.live( types, data, fn, selector );
	},

	undelegate: function( selector, types, fn ) {
		if ( arguments.length === 0 ) {
				return this.unbind( "live" );

		} else {
			return this.die( types, null, fn, selector );
		}
	},

	trigger: function( type, data ) {
		return this.each(function() {
			jQuery.event.trigger( type, data, this );
		});
	},

	triggerHandler: function( type, data ) {
		if ( this[0] ) {
			var event = jQuery.Event( type );
			event.preventDefault();
			event.stopPropagation();
			jQuery.event.trigger( event, data, this[0] );
			return event.result;
		}
	},

	toggle: function( fn ) {
		// Save reference to arguments for access in closure
		var args = arguments,
			i = 1;

		// link all the functions, so any of them can unbind this click handler
		while ( i < args.length ) {
			jQuery.proxy( fn, args[ i++ ] );
		}

		return this.click( jQuery.proxy( fn, function( event ) {
			// Figure out which function to execute
			var lastToggle = ( jQuery._data( this, "lastToggle" + fn.guid ) || 0 ) % i;
			jQuery._data( this, "lastToggle" + fn.guid, lastToggle + 1 );

			// Make sure that clicks stop
			event.preventDefault();

			// and execute the function
			return args[ lastToggle ].apply( this, arguments ) || false;
		}));
	},

	hover: function( fnOver, fnOut ) {
		return this.mouseenter( fnOver ).mouseleave( fnOut || fnOver );
	}
});

var liveMap = {
	focus: "focusin",
	blur: "focusout",
	mouseenter: "mouseover",
	mouseleave: "mouseout"
};

jQuery.each(["live", "die"], function( i, name ) {
	jQuery.fn[ name ] = function( types, data, fn, origSelector /* Internal Use Only */ ) {
		var type, i = 0, match, namespaces, preType,
			selector = origSelector || this.selector,
			context = origSelector ? this : jQuery( this.context );

		if ( typeof types === "object" && !types.preventDefault ) {
			for ( var key in types ) {
				context[ name ]( key, data, types[key], selector );
			}

			return this;
		}

		if ( jQuery.isFunction( data ) ) {
			fn = data;
			data = undefined;
		}

		types = (types || "").split(" ");

		while ( (type = types[ i++ ]) != null ) {
			match = rnamespaces.exec( type );
			namespaces = "";

			if ( match )  {
				namespaces = match[0];
				type = type.replace( rnamespaces, "" );
			}

			if ( type === "hover" ) {
				types.push( "mouseenter" + namespaces, "mouseleave" + namespaces );
				continue;
			}

			preType = type;

			if ( type === "focus" || type === "blur" ) {
				types.push( liveMap[ type ] + namespaces );
				type = type + namespaces;

			} else {
				type = (liveMap[ type ] || type) + namespaces;
			}

			if ( name === "live" ) {
				// bind live handler
				for ( var j = 0, l = context.length; j < l; j++ ) {
					jQuery.event.add( context[j], "live." + liveConvert( type, selector ),
						{ data: data, selector: selector, handler: fn, origType: type, origHandler: fn, preType: preType } );
				}

			} else {
				// unbind live handler
				context.unbind( "live." + liveConvert( type, selector ), fn );
			}
		}

		return this;
	};
});

function liveHandler( event ) {
	var stop, maxLevel, related, match, handleObj, elem, j, i, l, data, close, namespace, ret,
		elems = [],
		selectors = [],
		events = jQuery._data( this, "events" );

	// Make sure we avoid non-left-click bubbling in Firefox (#3861) and disabled elements in IE (#6911)
	if ( event.liveFired === this || !events || !events.live || event.target.disabled || event.button && event.type === "click" ) {
		return;
	}

	if ( event.namespace ) {
		namespace = new RegExp("(^|\\.)" + event.namespace.split(".").join("\\.(?:.*\\.)?") + "(\\.|$)");
	}

	event.liveFired = this;

	var live = events.live.slice(0);

	for ( j = 0; j < live.length; j++ ) {
		handleObj = live[j];

		if ( handleObj.origType.replace( rnamespaces, "" ) === event.type ) {
			selectors.push( handleObj.selector );

		} else {
			live.splice( j--, 1 );
		}
	}

	match = jQuery( event.target ).closest( selectors, event.currentTarget );

	for ( i = 0, l = match.length; i < l; i++ ) {
		close = match[i];

		for ( j = 0; j < live.length; j++ ) {
			handleObj = live[j];

			if ( close.selector === handleObj.selector && (!namespace || namespace.test( handleObj.namespace )) && !close.elem.disabled ) {
				elem = close.elem;
				related = null;

				// Those two events require additional checking
				if ( handleObj.preType === "mouseenter" || handleObj.preType === "mouseleave" ) {
					event.type = handleObj.preType;
					related = jQuery( event.relatedTarget ).closest( handleObj.selector )[0];
				}

				if ( !related || related !== elem ) {
					elems.push({ elem: elem, handleObj: handleObj, level: close.level });
				}
			}
		}
	}

	for ( i = 0, l = elems.length; i < l; i++ ) {
		match = elems[i];

		if ( maxLevel && match.level > maxLevel ) {
			break;
		}

		event.currentTarget = match.elem;
		event.data = match.handleObj.data;
		event.handleObj = match.handleObj;

		ret = match.handleObj.origHandler.apply( match.elem, arguments );

		if ( ret === false || event.isPropagationStopped() ) {
			maxLevel = match.level;

			if ( ret === false ) {
				stop = false;
			}
			if ( event.isImmediatePropagationStopped() ) {
				break;
			}
		}
	}

	return stop;
}

function liveConvert( type, selector ) {
	return (type && type !== "*" ? type + "." : "") + selector.replace(rperiod, "`").replace(rspace, "&");
}

jQuery.each( ("blur focus focusin focusout load resize scroll unload click dblclick " +
	"mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave " +
	"change select submit keydown keypress keyup error").split(" "), function( i, name ) {

	// Handle event binding
	jQuery.fn[ name ] = function( data, fn ) {
		if ( fn == null ) {
			fn = data;
			data = null;
		}

		return arguments.length > 0 ?
			this.bind( name, data, fn ) :
			this.trigger( name );
	};

	if ( jQuery.attrFn ) {
		jQuery.attrFn[ name ] = true;
	}
});


/*!
 * Sizzle CSS Selector Engine
 *  Copyright 2011, The Dojo Foundation
 *  Released under the MIT, BSD, and GPL Licenses.
 *  More information: http://sizzlejs.com/
 */
(function(){

var chunker = /((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^\[\]]*\]|['"][^'"]*['"]|[^\[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,
	done = 0,
	toString = Object.prototype.toString,
	hasDuplicate = false,
	baseHasDuplicate = true,
	rBackslash = /\\/g,
	rNonWord = /\W/;

// Here we check if the JavaScript engine is using some sort of
// optimization where it does not always call our comparision
// function. If that is the case, discard the hasDuplicate value.
//   Thus far that includes Google Chrome.
[0, 0].sort(function() {
	baseHasDuplicate = false;
	return 0;
});

var Sizzle = function( selector, context, results, seed ) {
	results = results || [];
	context = context || document;

	var origContext = context;

	if ( context.nodeType !== 1 && context.nodeType !== 9 ) {
		return [];
	}
	
	if ( !selector || typeof selector !== "string" ) {
		return results;
	}

	var m, set, checkSet, extra, ret, cur, pop, i,
		prune = true,
		contextXML = Sizzle.isXML( context ),
		parts = [],
		soFar = selector;
	
	// Reset the position of the chunker regexp (start from head)
	do {
		chunker.exec( "" );
		m = chunker.exec( soFar );

		if ( m ) {
			soFar = m[3];
		
			parts.push( m[1] );
		
			if ( m[2] ) {
				extra = m[3];
				break;
			}
		}
	} while ( m );

	if ( parts.length > 1 && origPOS.exec( selector ) ) {

		if ( parts.length === 2 && Expr.relative[ parts[0] ] ) {
			set = posProcess( parts[0] + parts[1], context );

		} else {
			set = Expr.relative[ parts[0] ] ?
				[ context ] :
				Sizzle( parts.shift(), context );

			while ( parts.length ) {
				selector = parts.shift();

				if ( Expr.relative[ selector ] ) {
					selector += parts.shift();
				}
				
				set = posProcess( selector, set );
			}
		}

	} else {
		// Take a shortcut and set the context if the root selector is an ID
		// (but not if it'll be faster if the inner selector is an ID)
		if ( !seed && parts.length > 1 && context.nodeType === 9 && !contextXML &&
				Expr.match.ID.test(parts[0]) && !Expr.match.ID.test(parts[parts.length - 1]) ) {

			ret = Sizzle.find( parts.shift(), context, contextXML );
			context = ret.expr ?
				Sizzle.filter( ret.expr, ret.set )[0] :
				ret.set[0];
		}

		if ( context ) {
			ret = seed ?
				{ expr: parts.pop(), set: makeArray(seed) } :
				Sizzle.find( parts.pop(), parts.length === 1 && (parts[0] === "~" || parts[0] === "+") && context.parentNode ? context.parentNode : context, contextXML );

			set = ret.expr ?
				Sizzle.filter( ret.expr, ret.set ) :
				ret.set;

			if ( parts.length > 0 ) {
				checkSet = makeArray( set );

			} else {
				prune = false;
			}

			while ( parts.length ) {
				cur = parts.pop();
				pop = cur;

				if ( !Expr.relative[ cur ] ) {
					cur = "";
				} else {
					pop = parts.pop();
				}

				if ( pop == null ) {
					pop = context;
				}

				Expr.relative[ cur ]( checkSet, pop, contextXML );
			}

		} else {
			checkSet = parts = [];
		}
	}

	if ( !checkSet ) {
		checkSet = set;
	}

	if ( !checkSet ) {
		Sizzle.error( cur || selector );
	}

	if ( toString.call(checkSet) === "[object Array]" ) {
		if ( !prune ) {
			results.push.apply( results, checkSet );

		} else if ( context && context.nodeType === 1 ) {
			for ( i = 0; checkSet[i] != null; i++ ) {
				if ( checkSet[i] && (checkSet[i] === true || checkSet[i].nodeType === 1 && Sizzle.contains(context, checkSet[i])) ) {
					results.push( set[i] );
				}
			}

		} else {
			for ( i = 0; checkSet[i] != null; i++ ) {
				if ( checkSet[i] && checkSet[i].nodeType === 1 ) {
					results.push( set[i] );
				}
			}
		}

	} else {
		makeArray( checkSet, results );
	}

	if ( extra ) {
		Sizzle( extra, origContext, results, seed );
		Sizzle.uniqueSort( results );
	}

	return results;
};

Sizzle.uniqueSort = function( results ) {
	if ( sortOrder ) {
		hasDuplicate = baseHasDuplicate;
		results.sort( sortOrder );

		if ( hasDuplicate ) {
			for ( var i = 1; i < results.length; i++ ) {
				if ( results[i] === results[ i - 1 ] ) {
					results.splice( i--, 1 );
				}
			}
		}
	}

	return results;
};

Sizzle.matches = function( expr, set ) {
	return Sizzle( expr, null, null, set );
};

Sizzle.matchesSelector = function( node, expr ) {
	return Sizzle( expr, null, null, [node] ).length > 0;
};

Sizzle.find = function( expr, context, isXML ) {
	var set;

	if ( !expr ) {
		return [];
	}

	for ( var i = 0, l = Expr.order.length; i < l; i++ ) {
		var match,
			type = Expr.order[i];
		
		if ( (match = Expr.leftMatch[ type ].exec( expr )) ) {
			var left = match[1];
			match.splice( 1, 1 );

			if ( left.substr( left.length - 1 ) !== "\\" ) {
				match[1] = (match[1] || "").replace( rBackslash, "" );
				set = Expr.find[ type ]( match, context, isXML );

				if ( set != null ) {
					expr = expr.replace( Expr.match[ type ], "" );
					break;
				}
			}
		}
	}

	if ( !set ) {
		set = typeof context.getElementsByTagName !== "undefined" ?
			context.getElementsByTagName( "*" ) :
			[];
	}

	return { set: set, expr: expr };
};

Sizzle.filter = function( expr, set, inplace, not ) {
	var match, anyFound,
		old = expr,
		result = [],
		curLoop = set,
		isXMLFilter = set && set[0] && Sizzle.isXML( set[0] );

	while ( expr && set.length ) {
		for ( var type in Expr.filter ) {
			if ( (match = Expr.leftMatch[ type ].exec( expr )) != null && match[2] ) {
				var found, item,
					filter = Expr.filter[ type ],
					left = match[1];

				anyFound = false;

				match.splice(1,1);

				if ( left.substr( left.length - 1 ) === "\\" ) {
					continue;
				}

				if ( curLoop === result ) {
					result = [];
				}

				if ( Expr.preFilter[ type ] ) {
					match = Expr.preFilter[ type ]( match, curLoop, inplace, result, not, isXMLFilter );

					if ( !match ) {
						anyFound = found = true;

					} else if ( match === true ) {
						continue;
					}
				}

				if ( match ) {
					for ( var i = 0; (item = curLoop[i]) != null; i++ ) {
						if ( item ) {
							found = filter( item, match, i, curLoop );
							var pass = not ^ !!found;

							if ( inplace && found != null ) {
								if ( pass ) {
									anyFound = true;

								} else {
									curLoop[i] = false;
								}

							} else if ( pass ) {
								result.push( item );
								anyFound = true;
							}
						}
					}
				}

				if ( found !== undefined ) {
					if ( !inplace ) {
						curLoop = result;
					}

					expr = expr.replace( Expr.match[ type ], "" );

					if ( !anyFound ) {
						return [];
					}

					break;
				}
			}
		}

		// Improper expression
		if ( expr === old ) {
			if ( anyFound == null ) {
				Sizzle.error( expr );

			} else {
				break;
			}
		}

		old = expr;
	}

	return curLoop;
};

Sizzle.error = function( msg ) {
	throw "Syntax error, unrecognized expression: " + msg;
};

var Expr = Sizzle.selectors = {
	order: [ "ID", "NAME", "TAG" ],

	match: {
		ID: /#((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,
		CLASS: /\.((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,
		NAME: /\[name=['"]*((?:[\w\u00c0-\uFFFF\-]|\\.)+)['"]*\]/,
		ATTR: /\[\s*((?:[\w\u00c0-\uFFFF\-]|\\.)+)\s*(?:(\S?=)\s*(?:(['"])(.*?)\3|(#?(?:[\w\u00c0-\uFFFF\-]|\\.)*)|)|)\s*\]/,
		TAG: /^((?:[\w\u00c0-\uFFFF\*\-]|\\.)+)/,
		CHILD: /:(only|nth|last|first)-child(?:\(\s*(even|odd|(?:[+\-]?\d+|(?:[+\-]?\d*)?n\s*(?:[+\-]\s*\d+)?))\s*\))?/,
		POS: /:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^\-]|$)/,
		PSEUDO: /:((?:[\w\u00c0-\uFFFF\-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/
	},

	leftMatch: {},

	attrMap: {
		"class": "className",
		"for": "htmlFor"
	},

	attrHandle: {
		href: function( elem ) {
			return elem.getAttribute( "href" );
		},
		type: function( elem ) {
			return elem.getAttribute( "type" );
		}
	},

	relative: {
		"+": function(checkSet, part){
			var isPartStr = typeof part === "string",
				isTag = isPartStr && !rNonWord.test( part ),
				isPartStrNotTag = isPartStr && !isTag;

			if ( isTag ) {
				part = part.toLowerCase();
			}

			for ( var i = 0, l = checkSet.length, elem; i < l; i++ ) {
				if ( (elem = checkSet[i]) ) {
					while ( (elem = elem.previousSibling) && elem.nodeType !== 1 ) {}

					checkSet[i] = isPartStrNotTag || elem && elem.nodeName.toLowerCase() === part ?
						elem || false :
						elem === part;
				}
			}

			if ( isPartStrNotTag ) {
				Sizzle.filter( part, checkSet, true );
			}
		},

		">": function( checkSet, part ) {
			var elem,
				isPartStr = typeof part === "string",
				i = 0,
				l = checkSet.length;

			if ( isPartStr && !rNonWord.test( part ) ) {
				part = part.toLowerCase();

				for ( ; i < l; i++ ) {
					elem = checkSet[i];

					if ( elem ) {
						var parent = elem.parentNode;
						checkSet[i] = parent.nodeName.toLowerCase() === part ? parent : false;
					}
				}

			} else {
				for ( ; i < l; i++ ) {
					elem = checkSet[i];

					if ( elem ) {
						checkSet[i] = isPartStr ?
							elem.parentNode :
							elem.parentNode === part;
					}
				}

				if ( isPartStr ) {
					Sizzle.filter( part, checkSet, true );
				}
			}
		},

		"": function(checkSet, part, isXML){
			var nodeCheck,
				doneName = done++,
				checkFn = dirCheck;

			if ( typeof part === "string" && !rNonWord.test( part ) ) {
				part = part.toLowerCase();
				nodeCheck = part;
				checkFn = dirNodeCheck;
			}

			checkFn( "parentNode", part, doneName, checkSet, nodeCheck, isXML );
		},

		"~": function( checkSet, part, isXML ) {
			var nodeCheck,
				doneName = done++,
				checkFn = dirCheck;

			if ( typeof part === "string" && !rNonWord.test( part ) ) {
				part = part.toLowerCase();
				nodeCheck = part;
				checkFn = dirNodeCheck;
			}

			checkFn( "previousSibling", part, doneName, checkSet, nodeCheck, isXML );
		}
	},

	find: {
		ID: function( match, context, isXML ) {
			if ( typeof context.getElementById !== "undefined" && !isXML ) {
				var m = context.getElementById(match[1]);
				// Check parentNode to catch when Blackberry 4.6 returns
				// nodes that are no longer in the document #6963
				return m && m.parentNode ? [m] : [];
			}
		},

		NAME: function( match, context ) {
			if ( typeof context.getElementsByName !== "undefined" ) {
				var ret = [],
					results = context.getElementsByName( match[1] );

				for ( var i = 0, l = results.length; i < l; i++ ) {
					if ( results[i].getAttribute("name") === match[1] ) {
						ret.push( results[i] );
					}
				}

				return ret.length === 0 ? null : ret;
			}
		},

		TAG: function( match, context ) {
			if ( typeof context.getElementsByTagName !== "undefined" ) {
				return context.getElementsByTagName( match[1] );
			}
		}
	},
	preFilter: {
		CLASS: function( match, curLoop, inplace, result, not, isXML ) {
			match = " " + match[1].replace( rBackslash, "" ) + " ";

			if ( isXML ) {
				return match;
			}

			for ( var i = 0, elem; (elem = curLoop[i]) != null; i++ ) {
				if ( elem ) {
					if ( not ^ (elem.className && (" " + elem.className + " ").replace(/[\t\n\r]/g, " ").indexOf(match) >= 0) ) {
						if ( !inplace ) {
							result.push( elem );
						}

					} else if ( inplace ) {
						curLoop[i] = false;
					}
				}
			}

			return false;
		},

		ID: function( match ) {
			return match[1].replace( rBackslash, "" );
		},

		TAG: function( match, curLoop ) {
			return match[1].replace( rBackslash, "" ).toLowerCase();
		},

		CHILD: function( match ) {
			if ( match[1] === "nth" ) {
				if ( !match[2] ) {
					Sizzle.error( match[0] );
				}

				match[2] = match[2].replace(/^\+|\s*/g, '');

				// parse equations like 'even', 'odd', '5', '2n', '3n+2', '4n-1', '-n+6'
				var test = /(-?)(\d*)(?:n([+\-]?\d*))?/.exec(
					match[2] === "even" && "2n" || match[2] === "odd" && "2n+1" ||
					!/\D/.test( match[2] ) && "0n+" + match[2] || match[2]);

				// calculate the numbers (first)n+(last) including if they are negative
				match[2] = (test[1] + (test[2] || 1)) - 0;
				match[3] = test[3] - 0;
			}
			else if ( match[2] ) {
				Sizzle.error( match[0] );
			}

			// TODO: Move to normal caching system
			match[0] = done++;

			return match;
		},

		ATTR: function( match, curLoop, inplace, result, not, isXML ) {
			var name = match[1] = match[1].replace( rBackslash, "" );
			
			if ( !isXML && Expr.attrMap[name] ) {
				match[1] = Expr.attrMap[name];
			}

			// Handle if an un-quoted value was used
			match[4] = ( match[4] || match[5] || "" ).replace( rBackslash, "" );

			if ( match[2] === "~=" ) {
				match[4] = " " + match[4] + " ";
			}

			return match;
		},

		PSEUDO: function( match, curLoop, inplace, result, not ) {
			if ( match[1] === "not" ) {
				// If we're dealing with a complex expression, or a simple one
				if ( ( chunker.exec(match[3]) || "" ).length > 1 || /^\w/.test(match[3]) ) {
					match[3] = Sizzle(match[3], null, null, curLoop);

				} else {
					var ret = Sizzle.filter(match[3], curLoop, inplace, true ^ not);

					if ( !inplace ) {
						result.push.apply( result, ret );
					}

					return false;
				}

			} else if ( Expr.match.POS.test( match[0] ) || Expr.match.CHILD.test( match[0] ) ) {
				return true;
			}
			
			return match;
		},

		POS: function( match ) {
			match.unshift( true );

			return match;
		}
	},
	
	filters: {
		enabled: function( elem ) {
			return elem.disabled === false && elem.type !== "hidden";
		},

		disabled: function( elem ) {
			return elem.disabled === true;
		},

		checked: function( elem ) {
			return elem.checked === true;
		},
		
		selected: function( elem ) {
			// Accessing this property makes selected-by-default
			// options in Safari work properly
			if ( elem.parentNode ) {
				elem.parentNode.selectedIndex;
			}
			
			return elem.selected === true;
		},

		parent: function( elem ) {
			return !!elem.firstChild;
		},

		empty: function( elem ) {
			return !elem.firstChild;
		},

		has: function( elem, i, match ) {
			return !!Sizzle( match[3], elem ).length;
		},

		header: function( elem ) {
			return (/h\d/i).test( elem.nodeName );
		},

		text: function( elem ) {
			// IE6 and 7 will map elem.type to 'text' for new HTML5 types (search, etc) 
			// use getAttribute instead to test this case
			return "text" === elem.getAttribute( 'type' );
		},
		radio: function( elem ) {
			return "radio" === elem.type;
		},

		checkbox: function( elem ) {
			return "checkbox" === elem.type;
		},

		file: function( elem ) {
			return "file" === elem.type;
		},
		password: function( elem ) {
			return "password" === elem.type;
		},

		submit: function( elem ) {
			return "submit" === elem.type;
		},

		image: function( elem ) {
			return "image" === elem.type;
		},

		reset: function( elem ) {
			return "reset" === elem.type;
		},

		button: function( elem ) {
			return "button" === elem.type || elem.nodeName.toLowerCase() === "button";
		},

		input: function( elem ) {
			return (/input|select|textarea|button/i).test( elem.nodeName );
		}
	},
	setFilters: {
		first: function( elem, i ) {
			return i === 0;
		},

		last: function( elem, i, match, array ) {
			return i === array.length - 1;
		},

		even: function( elem, i ) {
			return i % 2 === 0;
		},

		odd: function( elem, i ) {
			return i % 2 === 1;
		},

		lt: function( elem, i, match ) {
			return i < match[3] - 0;
		},

		gt: function( elem, i, match ) {
			return i > match[3] - 0;
		},

		nth: function( elem, i, match ) {
			return match[3] - 0 === i;
		},

		eq: function( elem, i, match ) {
			return match[3] - 0 === i;
		}
	},
	filter: {
		PSEUDO: function( elem, match, i, array ) {
			var name = match[1],
				filter = Expr.filters[ name ];

			if ( filter ) {
				return filter( elem, i, match, array );

			} else if ( name === "contains" ) {
				return (elem.textContent || elem.innerText || Sizzle.getText([ elem ]) || "").indexOf(match[3]) >= 0;

			} else if ( name === "not" ) {
				var not = match[3];

				for ( var j = 0, l = not.length; j < l; j++ ) {
					if ( not[j] === elem ) {
						return false;
					}
				}

				return true;

			} else {
				Sizzle.error( name );
			}
		},

		CHILD: function( elem, match ) {
			var type = match[1],
				node = elem;

			switch ( type ) {
				case "only":
				case "first":
					while ( (node = node.previousSibling) )	 {
						if ( node.nodeType === 1 ) { 
							return false; 
						}
					}

					if ( type === "first" ) { 
						return true; 
					}

					node = elem;

				case "last":
					while ( (node = node.nextSibling) )	 {
						if ( node.nodeType === 1 ) { 
							return false; 
						}
					}

					return true;

				case "nth":
					var first = match[2],
						last = match[3];

					if ( first === 1 && last === 0 ) {
						return true;
					}
					
					var doneName = match[0],
						parent = elem.parentNode;
	
					if ( parent && (parent.sizcache !== doneName || !elem.nodeIndex) ) {
						var count = 0;
						
						for ( node = parent.firstChild; node; node = node.nextSibling ) {
							if ( node.nodeType === 1 ) {
								node.nodeIndex = ++count;
							}
						} 

						parent.sizcache = doneName;
					}
					
					var diff = elem.nodeIndex - last;

					if ( first === 0 ) {
						return diff === 0;

					} else {
						return ( diff % first === 0 && diff / first >= 0 );
					}
			}
		},

		ID: function( elem, match ) {
			return elem.nodeType === 1 && elem.getAttribute("id") === match;
		},

		TAG: function( elem, match ) {
			return (match === "*" && elem.nodeType === 1) || elem.nodeName.toLowerCase() === match;
		},
		
		CLASS: function( elem, match ) {
			return (" " + (elem.className || elem.getAttribute("class")) + " ")
				.indexOf( match ) > -1;
		},

		ATTR: function( elem, match ) {
			var name = match[1],
				result = Expr.attrHandle[ name ] ?
					Expr.attrHandle[ name ]( elem ) :
					elem[ name ] != null ?
						elem[ name ] :
						elem.getAttribute( name ),
				value = result + "",
				type = match[2],
				check = match[4];

			return result == null ?
				type === "!=" :
				type === "=" ?
				value === check :
				type === "*=" ?
				value.indexOf(check) >= 0 :
				type === "~=" ?
				(" " + value + " ").indexOf(check) >= 0 :
				!check ?
				value && result !== false :
				type === "!=" ?
				value !== check :
				type === "^=" ?
				value.indexOf(check) === 0 :
				type === "$=" ?
				value.substr(value.length - check.length) === check :
				type === "|=" ?
				value === check || value.substr(0, check.length + 1) === check + "-" :
				false;
		},

		POS: function( elem, match, i, array ) {
			var name = match[2],
				filter = Expr.setFilters[ name ];

			if ( filter ) {
				return filter( elem, i, match, array );
			}
		}
	}
};

var origPOS = Expr.match.POS,
	fescape = function(all, num){
		return "\\" + (num - 0 + 1);
	};

for ( var type in Expr.match ) {
	Expr.match[ type ] = new RegExp( Expr.match[ type ].source + (/(?![^\[]*\])(?![^\(]*\))/.source) );
	Expr.leftMatch[ type ] = new RegExp( /(^(?:.|\r|\n)*?)/.source + Expr.match[ type ].source.replace(/\\(\d+)/g, fescape) );
}

var makeArray = function( array, results ) {
	array = Array.prototype.slice.call( array, 0 );

	if ( results ) {
		results.push.apply( results, array );
		return results;
	}
	
	return array;
};

// Perform a simple check to determine if the browser is capable of
// converting a NodeList to an array using builtin methods.
// Also verifies that the returned array holds DOM nodes
// (which is not the case in the Blackberry browser)
try {
	Array.prototype.slice.call( document.documentElement.childNodes, 0 )[0].nodeType;

// Provide a fallback method if it does not work
} catch( e ) {
	makeArray = function( array, results ) {
		var i = 0,
			ret = results || [];

		if ( toString.call(array) === "[object Array]" ) {
			Array.prototype.push.apply( ret, array );

		} else {
			if ( typeof array.length === "number" ) {
				for ( var l = array.length; i < l; i++ ) {
					ret.push( array[i] );
				}

			} else {
				for ( ; array[i]; i++ ) {
					ret.push( array[i] );
				}
			}
		}

		return ret;
	};
}

var sortOrder, siblingCheck;

if ( document.documentElement.compareDocumentPosition ) {
	sortOrder = function( a, b ) {
		if ( a === b ) {
			hasDuplicate = true;
			return 0;
		}

		if ( !a.compareDocumentPosition || !b.compareDocumentPosition ) {
			return a.compareDocumentPosition ? -1 : 1;
		}

		return a.compareDocumentPosition(b) & 4 ? -1 : 1;
	};

} else {
	sortOrder = function( a, b ) {
		var al, bl,
			ap = [],
			bp = [],
			aup = a.parentNode,
			bup = b.parentNode,
			cur = aup;

		// The nodes are identical, we can exit early
		if ( a === b ) {
			hasDuplicate = true;
			return 0;

		// If the nodes are siblings (or identical) we can do a quick check
		} else if ( aup === bup ) {
			return siblingCheck( a, b );

		// If no parents were found then the nodes are disconnected
		} else if ( !aup ) {
			return -1;

		} else if ( !bup ) {
			return 1;
		}

		// Otherwise they're somewhere else in the tree so we need
		// to build up a full list of the parentNodes for comparison
		while ( cur ) {
			ap.unshift( cur );
			cur = cur.parentNode;
		}

		cur = bup;

		while ( cur ) {
			bp.unshift( cur );
			cur = cur.parentNode;
		}

		al = ap.length;
		bl = bp.length;

		// Start walking down the tree looking for a discrepancy
		for ( var i = 0; i < al && i < bl; i++ ) {
			if ( ap[i] !== bp[i] ) {
				return siblingCheck( ap[i], bp[i] );
			}
		}

		// We ended someplace up the tree so do a sibling check
		return i === al ?
			siblingCheck( a, bp[i], -1 ) :
			siblingCheck( ap[i], b, 1 );
	};

	siblingCheck = function( a, b, ret ) {
		if ( a === b ) {
			return ret;
		}

		var cur = a.nextSibling;

		while ( cur ) {
			if ( cur === b ) {
				return -1;
			}

			cur = cur.nextSibling;
		}

		return 1;
	};
}

// Utility function for retreiving the text value of an array of DOM nodes
Sizzle.getText = function( elems ) {
	var ret = "", elem;

	for ( var i = 0; elems[i]; i++ ) {
		elem = elems[i];

		// Get the text from text nodes and CDATA nodes
		if ( elem.nodeType === 3 || elem.nodeType === 4 ) {
			ret += elem.nodeValue;

		// Traverse everything else, except comment nodes
		} else if ( elem.nodeType !== 8 ) {
			ret += Sizzle.getText( elem.childNodes );
		}
	}

	return ret;
};

// Check to see if the browser returns elements by name when
// querying by getElementById (and provide a workaround)
(function(){
	// We're going to inject a fake input element with a specified name
	var form = document.createElement("div"),
		id = "script" + (new Date()).getTime(),
		root = document.documentElement;

	form.innerHTML = "<a name='" + id + "'/>";

	// Inject it into the root element, check its status, and remove it quickly
	root.insertBefore( form, root.firstChild );

	// The workaround has to do additional checks after a getElementById
	// Which slows things down for other browsers (hence the branching)
	if ( document.getElementById( id ) ) {
		Expr.find.ID = function( match, context, isXML ) {
			if ( typeof context.getElementById !== "undefined" && !isXML ) {
				var m = context.getElementById(match[1]);

				return m ?
					m.id === match[1] || typeof m.getAttributeNode !== "undefined" && m.getAttributeNode("id").nodeValue === match[1] ?
						[m] :
						undefined :
					[];
			}
		};

		Expr.filter.ID = function( elem, match ) {
			var node = typeof elem.getAttributeNode !== "undefined" && elem.getAttributeNode("id");

			return elem.nodeType === 1 && node && node.nodeValue === match;
		};
	}

	root.removeChild( form );

	// release memory in IE
	root = form = null;
})();

(function(){
	// Check to see if the browser returns only elements
	// when doing getElementsByTagName("*")

	// Create a fake element
	var div = document.createElement("div");
	div.appendChild( document.createComment("") );

	// Make sure no comments are found
	if ( div.getElementsByTagName("*").length > 0 ) {
		Expr.find.TAG = function( match, context ) {
			var results = context.getElementsByTagName( match[1] );

			// Filter out possible comments
			if ( match[1] === "*" ) {
				var tmp = [];

				for ( var i = 0; results[i]; i++ ) {
					if ( results[i].nodeType === 1 ) {
						tmp.push( results[i] );
					}
				}

				results = tmp;
			}

			return results;
		};
	}

	// Check to see if an attribute returns normalized href attributes
	div.innerHTML = "<a href='#'></a>";

	if ( div.firstChild && typeof div.firstChild.getAttribute !== "undefined" &&
			div.firstChild.getAttribute("href") !== "#" ) {

		Expr.attrHandle.href = function( elem ) {
			return elem.getAttribute( "href", 2 );
		};
	}

	// release memory in IE
	div = null;
})();

if ( document.querySelectorAll ) {
	(function(){
		var oldSizzle = Sizzle,
			div = document.createElement("div"),
			id = "__sizzle__";

		div.innerHTML = "<p class='TEST'></p>";

		// Safari can't handle uppercase or unicode characters when
		// in quirks mode.
		if ( div.querySelectorAll && div.querySelectorAll(".TEST").length === 0 ) {
			return;
		}
	
		Sizzle = function( query, context, extra, seed ) {
			context = context || document;

			// Only use querySelectorAll on non-XML documents
			// (ID selectors don't work in non-HTML documents)
			if ( !seed && !Sizzle.isXML(context) ) {
				// See if we find a selector to speed up
				var match = /^(\w+$)|^\.([\w\-]+$)|^#([\w\-]+$)/.exec( query );
				
				if ( match && (context.nodeType === 1 || context.nodeType === 9) ) {
					// Speed-up: Sizzle("TAG")
					if ( match[1] ) {
						return makeArray( context.getElementsByTagName( query ), extra );
					
					// Speed-up: Sizzle(".CLASS")
					} else if ( match[2] && Expr.find.CLASS && context.getElementsByClassName ) {
						return makeArray( context.getElementsByClassName( match[2] ), extra );
					}
				}
				
				if ( context.nodeType === 9 ) {
					// Speed-up: Sizzle("body")
					// The body element only exists once, optimize finding it
					if ( query === "body" && context.body ) {
						return makeArray( [ context.body ], extra );
						
					// Speed-up: Sizzle("#ID")
					} else if ( match && match[3] ) {
						var elem = context.getElementById( match[3] );

						// Check parentNode to catch when Blackberry 4.6 returns
						// nodes that are no longer in the document #6963
						if ( elem && elem.parentNode ) {
							// Handle the case where IE and Opera return items
							// by name instead of ID
							if ( elem.id === match[3] ) {
								return makeArray( [ elem ], extra );
							}
							
						} else {
							return makeArray( [], extra );
						}
					}
					
					try {
						return makeArray( context.querySelectorAll(query), extra );
					} catch(qsaError) {}

				// qSA works strangely on Element-rooted queries
				// We can work around this by specifying an extra ID on the root
				// and working up from there (Thanks to Andrew Dupont for the technique)
				// IE 8 doesn't work on object elements
				} else if ( context.nodeType === 1 && context.nodeName.toLowerCase() !== "object" ) {
					var oldContext = context,
						old = context.getAttribute( "id" ),
						nid = old || id,
						hasParent = context.parentNode,
						relativeHierarchySelector = /^\s*[+~]/.test( query );

					if ( !old ) {
						context.setAttribute( "id", nid );
					} else {
						nid = nid.replace( /'/g, "\\$&" );
					}
					if ( relativeHierarchySelector && hasParent ) {
						context = context.parentNode;
					}

					try {
						if ( !relativeHierarchySelector || hasParent ) {
							return makeArray( context.querySelectorAll( "[id='" + nid + "'] " + query ), extra );
						}

					} catch(pseudoError) {
					} finally {
						if ( !old ) {
							oldContext.removeAttribute( "id" );
						}
					}
				}
			}
		
			return oldSizzle(query, context, extra, seed);
		};

		for ( var prop in oldSizzle ) {
			Sizzle[ prop ] = oldSizzle[ prop ];
		}

		// release memory in IE
		div = null;
	})();
}

(function(){
	var html = document.documentElement,
		matches = html.matchesSelector || html.mozMatchesSelector || html.webkitMatchesSelector || html.msMatchesSelector,
		pseudoWorks = false;

	try {
		// This should fail with an exception
		// Gecko does not error, returns false instead
		matches.call( document.documentElement, "[test!='']:sizzle" );
	
	} catch( pseudoError ) {
		pseudoWorks = true;
	}

	if ( matches ) {
		Sizzle.matchesSelector = function( node, expr ) {
			// Make sure that attribute selectors are quoted
			expr = expr.replace(/\=\s*([^'"\]]*)\s*\]/g, "='$1']");

			if ( !Sizzle.isXML( node ) ) {
				try { 
					if ( pseudoWorks || !Expr.match.PSEUDO.test( expr ) && !/!=/.test( expr ) ) {
						return matches.call( node, expr );
					}
				} catch(e) {}
			}

			return Sizzle(expr, null, null, [node]).length > 0;
		};
	}
})();

(function(){
	var div = document.createElement("div");

	div.innerHTML = "<div class='test e'></div><div class='test'></div>";

	// Opera can't find a second classname (in 9.6)
	// Also, make sure that getElementsByClassName actually exists
	if ( !div.getElementsByClassName || div.getElementsByClassName("e").length === 0 ) {
		return;
	}

	// Safari caches class attributes, doesn't catch changes (in 3.2)
	div.lastChild.className = "e";

	if ( div.getElementsByClassName("e").length === 1 ) {
		return;
	}
	
	Expr.order.splice(1, 0, "CLASS");
	Expr.find.CLASS = function( match, context, isXML ) {
		if ( typeof context.getElementsByClassName !== "undefined" && !isXML ) {
			return context.getElementsByClassName(match[1]);
		}
	};

	// release memory in IE
	div = null;
})();

function dirNodeCheck( dir, cur, doneName, checkSet, nodeCheck, isXML ) {
	for ( var i = 0, l = checkSet.length; i < l; i++ ) {
		var elem = checkSet[i];

		if ( elem ) {
			var match = false;

			elem = elem[dir];

			while ( elem ) {
				if ( elem.sizcache === doneName ) {
					match = checkSet[elem.sizset];
					break;
				}

				if ( elem.nodeType === 1 && !isXML ){
					elem.sizcache = doneName;
					elem.sizset = i;
				}

				if ( elem.nodeName.toLowerCase() === cur ) {
					match = elem;
					break;
				}

				elem = elem[dir];
			}

			checkSet[i] = match;
		}
	}
}

function dirCheck( dir, cur, doneName, checkSet, nodeCheck, isXML ) {
	for ( var i = 0, l = checkSet.length; i < l; i++ ) {
		var elem = checkSet[i];

		if ( elem ) {
			var match = false;
			
			elem = elem[dir];

			while ( elem ) {
				if ( elem.sizcache === doneName ) {
					match = checkSet[elem.sizset];
					break;
				}

				if ( elem.nodeType === 1 ) {
					if ( !isXML ) {
						elem.sizcache = doneName;
						elem.sizset = i;
					}

					if ( typeof cur !== "string" ) {
						if ( elem === cur ) {
							match = true;
							break;
						}

					} else if ( Sizzle.filter( cur, [elem] ).length > 0 ) {
						match = elem;
						break;
					}
				}

				elem = elem[dir];
			}

			checkSet[i] = match;
		}
	}
}

if ( document.documentElement.contains ) {
	Sizzle.contains = function( a, b ) {
		return a !== b && (a.contains ? a.contains(b) : true);
	};

} else if ( document.documentElement.compareDocumentPosition ) {
	Sizzle.contains = function( a, b ) {
		return !!(a.compareDocumentPosition(b) & 16);
	};

} else {
	Sizzle.contains = function() {
		return false;
	};
}

Sizzle.isXML = function( elem ) {
	// documentElement is verified for cases where it doesn't yet exist
	// (such as loading iframes in IE - #4833) 
	var documentElement = (elem ? elem.ownerDocument || elem : 0).documentElement;

	return documentElement ? documentElement.nodeName !== "HTML" : false;
};

var posProcess = function( selector, context ) {
	var match,
		tmpSet = [],
		later = "",
		root = context.nodeType ? [context] : context;

	// Position selectors must be done after the filter
	// And so must :not(positional) so we move all PSEUDOs to the end
	while ( (match = Expr.match.PSEUDO.exec( selector )) ) {
		later += match[0];
		selector = selector.replace( Expr.match.PSEUDO, "" );
	}

	selector = Expr.relative[selector] ? selector + "*" : selector;

	for ( var i = 0, l = root.length; i < l; i++ ) {
		Sizzle( selector, root[i], tmpSet );
	}

	return Sizzle.filter( later, tmpSet );
};

// EXPOSE
jQuery.find = Sizzle;
jQuery.expr = Sizzle.selectors;
jQuery.expr[":"] = jQuery.expr.filters;
jQuery.unique = Sizzle.uniqueSort;
jQuery.text = Sizzle.getText;
jQuery.isXMLDoc = Sizzle.isXML;
jQuery.contains = Sizzle.contains;


})();


var runtil = /Until$/,
	rparentsprev = /^(?:parents|prevUntil|prevAll)/,
	// Note: This RegExp should be improved, or likely pulled from Sizzle
	rmultiselector = /,/,
	isSimple = /^.[^:#\[\.,]*$/,
	slice = Array.prototype.slice,
	POS = jQuery.expr.match.POS,
	// methods guaranteed to produce a unique set when starting from a unique set
	guaranteedUnique = {
		children: true,
		contents: true,
		next: true,
		prev: true
	};

jQuery.fn.extend({
	find: function( selector ) {
		var ret = this.pushStack( "", "find", selector ),
			length = 0;

		for ( var i = 0, l = this.length; i < l; i++ ) {
			length = ret.length;
			jQuery.find( selector, this[i], ret );

			if ( i > 0 ) {
				// Make sure that the results are unique
				for ( var n = length; n < ret.length; n++ ) {
					for ( var r = 0; r < length; r++ ) {
						if ( ret[r] === ret[n] ) {
							ret.splice(n--, 1);
							break;
						}
					}
				}
			}
		}

		return ret;
	},

	has: function( target ) {
		var targets = jQuery( target );
		return this.filter(function() {
			for ( var i = 0, l = targets.length; i < l; i++ ) {
				if ( jQuery.contains( this, targets[i] ) ) {
					return true;
				}
			}
		});
	},

	not: function( selector ) {
		return this.pushStack( winnow(this, selector, false), "not", selector);
	},

	filter: function( selector ) {
		return this.pushStack( winnow(this, selector, true), "filter", selector );
	},

	is: function( selector ) {
		return !!selector && jQuery.filter( selector, this ).length > 0;
	},

	closest: function( selectors, context ) {
		var ret = [], i, l, cur = this[0];

		if ( jQuery.isArray( selectors ) ) {
			var match, selector,
				matches = {},
				level = 1;

			if ( cur && selectors.length ) {
				for ( i = 0, l = selectors.length; i < l; i++ ) {
					selector = selectors[i];

					if ( !matches[selector] ) {
						matches[selector] = jQuery.expr.match.POS.test( selector ) ?
							jQuery( selector, context || this.context ) :
							selector;
					}
				}

				while ( cur && cur.ownerDocument && cur !== context ) {
					for ( selector in matches ) {
						match = matches[selector];

						if ( match.jquery ? match.index(cur) > -1 : jQuery(cur).is(match) ) {
							ret.push({ selector: selector, elem: cur, level: level });
						}
					}

					cur = cur.parentNode;
					level++;
				}
			}

			return ret;
		}

		var pos = POS.test( selectors ) ?
			jQuery( selectors, context || this.context ) : null;

		for ( i = 0, l = this.length; i < l; i++ ) {
			cur = this[i];

			while ( cur ) {
				if ( pos ? pos.index(cur) > -1 : jQuery.find.matchesSelector(cur, selectors) ) {
					ret.push( cur );
					break;

				} else {
					cur = cur.parentNode;
					if ( !cur || !cur.ownerDocument || cur === context ) {
						break;
					}
				}
			}
		}

		ret = ret.length > 1 ? jQuery.unique(ret) : ret;

		return this.pushStack( ret, "closest", selectors );
	},

	// Determine the position of an element within
	// the matched set of elements
	index: function( elem ) {
		if ( !elem || typeof elem === "string" ) {
			return jQuery.inArray( this[0],
				// If it receives a string, the selector is used
				// If it receives nothing, the siblings are used
				elem ? jQuery( elem ) : this.parent().children() );
		}
		// Locate the position of the desired element
		return jQuery.inArray(
			// If it receives a jQuery object, the first element is used
			elem.jquery ? elem[0] : elem, this );
	},

	add: function( selector, context ) {
		var set = typeof selector === "string" ?
				jQuery( selector, context ) :
				jQuery.makeArray( selector ),
			all = jQuery.merge( this.get(), set );

		return this.pushStack( isDisconnected( set[0] ) || isDisconnected( all[0] ) ?
			all :
			jQuery.unique( all ) );
	},

	andSelf: function() {
		return this.add( this.prevObject );
	}
});

// A painfully simple check to see if an element is disconnected
// from a document (should be improved, where feasible).
function isDisconnected( node ) {
	return !node || !node.parentNode || node.parentNode.nodeType === 11;
}

jQuery.each({
	parent: function( elem ) {
		var parent = elem.parentNode;
		return parent && parent.nodeType !== 11 ? parent : null;
	},
	parents: function( elem ) {
		return jQuery.dir( elem, "parentNode" );
	},
	parentsUntil: function( elem, i, until ) {
		return jQuery.dir( elem, "parentNode", until );
	},
	next: function( elem ) {
		return jQuery.nth( elem, 2, "nextSibling" );
	},
	prev: function( elem ) {
		return jQuery.nth( elem, 2, "previousSibling" );
	},
	nextAll: function( elem ) {
		return jQuery.dir( elem, "nextSibling" );
	},
	prevAll: function( elem ) {
		return jQuery.dir( elem, "previousSibling" );
	},
	nextUntil: function( elem, i, until ) {
		return jQuery.dir( elem, "nextSibling", until );
	},
	prevUntil: function( elem, i, until ) {
		return jQuery.dir( elem, "previousSibling", until );
	},
	siblings: function( elem ) {
		return jQuery.sibling( elem.parentNode.firstChild, elem );
	},
	children: function( elem ) {
		return jQuery.sibling( elem.firstChild );
	},
	contents: function( elem ) {
		return jQuery.nodeName( elem, "iframe" ) ?
			elem.contentDocument || elem.contentWindow.document :
			jQuery.makeArray( elem.childNodes );
	}
}, function( name, fn ) {
	jQuery.fn[ name ] = function( until, selector ) {
		var ret = jQuery.map( this, fn, until ),
			// The variable 'args' was introduced in
			// https://github.com/jquery/jquery/commit/52a0238
			// to work around a bug in Chrome 10 (Dev) and should be removed when the bug is fixed.
			// http://code.google.com/p/v8/issues/detail?id=1050
			args = slice.call(arguments);

		if ( !runtil.test( name ) ) {
			selector = until;
		}

		if ( selector && typeof selector === "string" ) {
			ret = jQuery.filter( selector, ret );
		}

		ret = this.length > 1 && !guaranteedUnique[ name ] ? jQuery.unique( ret ) : ret;

		if ( (this.length > 1 || rmultiselector.test( selector )) && rparentsprev.test( name ) ) {
			ret = ret.reverse();
		}

		return this.pushStack( ret, name, args.join(",") );
	};
});

jQuery.extend({
	filter: function( expr, elems, not ) {
		if ( not ) {
			expr = ":not(" + expr + ")";
		}

		return elems.length === 1 ?
			jQuery.find.matchesSelector(elems[0], expr) ? [ elems[0] ] : [] :
			jQuery.find.matches(expr, elems);
	},

	dir: function( elem, dir, until ) {
		var matched = [],
			cur = elem[ dir ];

		while ( cur && cur.nodeType !== 9 && (until === undefined || cur.nodeType !== 1 || !jQuery( cur ).is( until )) ) {
			if ( cur.nodeType === 1 ) {
				matched.push( cur );
			}
			cur = cur[dir];
		}
		return matched;
	},

	nth: function( cur, result, dir, elem ) {
		result = result || 1;
		var num = 0;

		for ( ; cur; cur = cur[dir] ) {
			if ( cur.nodeType === 1 && ++num === result ) {
				break;
			}
		}

		return cur;
	},

	sibling: function( n, elem ) {
		var r = [];

		for ( ; n; n = n.nextSibling ) {
			if ( n.nodeType === 1 && n !== elem ) {
				r.push( n );
			}
		}

		return r;
	}
});

// Implement the identical functionality for filter and not
function winnow( elements, qualifier, keep ) {
	if ( jQuery.isFunction( qualifier ) ) {
		return jQuery.grep(elements, function( elem, i ) {
			var retVal = !!qualifier.call( elem, i, elem );
			return retVal === keep;
		});

	} else if ( qualifier.nodeType ) {
		return jQuery.grep(elements, function( elem, i ) {
			return (elem === qualifier) === keep;
		});

	} else if ( typeof qualifier === "string" ) {
		var filtered = jQuery.grep(elements, function( elem ) {
			return elem.nodeType === 1;
		});

		if ( isSimple.test( qualifier ) ) {
			return jQuery.filter(qualifier, filtered, !keep);
		} else {
			qualifier = jQuery.filter( qualifier, filtered );
		}
	}

	return jQuery.grep(elements, function( elem, i ) {
		return (jQuery.inArray( elem, qualifier ) >= 0) === keep;
	});
}




var rinlinejQuery = / jQuery\d+="(?:\d+|null)"/g,
	rleadingWhitespace = /^\s+/,
	rxhtmlTag = /<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/ig,
	rtagName = /<([\w:]+)/,
	rtbody = /<tbody/i,
	rhtml = /<|&#?\w+;/,
	rnocache = /<(?:script|object|embed|option|style)/i,
	// checked="checked" or checked
	rchecked = /checked\s*(?:[^=]|=\s*.checked.)/i,
	wrapMap = {
		option: [ 1, "<select multiple='multiple'>", "</select>" ],
		legend: [ 1, "<fieldset>", "</fieldset>" ],
		thead: [ 1, "<table>", "</table>" ],
		tr: [ 2, "<table><tbody>", "</tbody></table>" ],
		td: [ 3, "<table><tbody><tr>", "</tr></tbody></table>" ],
		col: [ 2, "<table><tbody></tbody><colgroup>", "</colgroup></table>" ],
		area: [ 1, "<map>", "</map>" ],
		_default: [ 0, "", "" ]
	};

wrapMap.optgroup = wrapMap.option;
wrapMap.tbody = wrapMap.tfoot = wrapMap.colgroup = wrapMap.caption = wrapMap.thead;
wrapMap.th = wrapMap.td;

// IE can't serialize <link> and <script> tags normally
if ( !jQuery.support.htmlSerialize ) {
	wrapMap._default = [ 1, "div<div>", "</div>" ];
}

jQuery.fn.extend({
	text: function( text ) {
		if ( jQuery.isFunction(text) ) {
			return this.each(function(i) {
				var self = jQuery( this );

				self.text( text.call(this, i, self.text()) );
			});
		}

		if ( typeof text !== "object" && text !== undefined ) {
			return this.empty().append( (this[0] && this[0].ownerDocument || document).createTextNode( text ) );
		}

		return jQuery.text( this );
	},

	wrapAll: function( html ) {
		if ( jQuery.isFunction( html ) ) {
			return this.each(function(i) {
				jQuery(this).wrapAll( html.call(this, i) );
			});
		}

		if ( this[0] ) {
			// The elements to wrap the target around
			var wrap = jQuery( html, this[0].ownerDocument ).eq(0).clone(true);

			if ( this[0].parentNode ) {
				wrap.insertBefore( this[0] );
			}

			wrap.map(function() {
				var elem = this;

				while ( elem.firstChild && elem.firstChild.nodeType === 1 ) {
					elem = elem.firstChild;
				}

				return elem;
			}).append(this);
		}

		return this;
	},

	wrapInner: function( html ) {
		if ( jQuery.isFunction( html ) ) {
			return this.each(function(i) {
				jQuery(this).wrapInner( html.call(this, i) );
			});
		}

		return this.each(function() {
			var self = jQuery( this ),
				contents = self.contents();

			if ( contents.length ) {
				contents.wrapAll( html );

			} else {
				self.append( html );
			}
		});
	},

	wrap: function( html ) {
		return this.each(function() {
			jQuery( this ).wrapAll( html );
		});
	},

	unwrap: function() {
		return this.parent().each(function() {
			if ( !jQuery.nodeName( this, "body" ) ) {
				jQuery( this ).replaceWith( this.childNodes );
			}
		}).end();
	},

	append: function() {
		return this.domManip(arguments, true, function( elem ) {
			if ( this.nodeType === 1 ) {
				this.appendChild( elem );
			}
		});
	},

	prepend: function() {
		return this.domManip(arguments, true, function( elem ) {
			if ( this.nodeType === 1 ) {
				this.insertBefore( elem, this.firstChild );
			}
		});
	},

	before: function() {
		if ( this[0] && this[0].parentNode ) {
			return this.domManip(arguments, false, function( elem ) {
				this.parentNode.insertBefore( elem, this );
			});
		} else if ( arguments.length ) {
			var set = jQuery(arguments[0]);
			set.push.apply( set, this.toArray() );
			return this.pushStack( set, "before", arguments );
		}
	},

	after: function() {
		if ( this[0] && this[0].parentNode ) {
			return this.domManip(arguments, false, function( elem ) {
				this.parentNode.insertBefore( elem, this.nextSibling );
			});
		} else if ( arguments.length ) {
			var set = this.pushStack( this, "after", arguments );
			set.push.apply( set, jQuery(arguments[0]).toArray() );
			return set;
		}
	},

	// keepData is for internal use only--do not document
	remove: function( selector, keepData ) {
		for ( var i = 0, elem; (elem = this[i]) != null; i++ ) {
			if ( !selector || jQuery.filter( selector, [ elem ] ).length ) {
				if ( !keepData && elem.nodeType === 1 ) {
					jQuery.cleanData( elem.getElementsByTagName("*") );
					jQuery.cleanData( [ elem ] );
				}

				if ( elem.parentNode ) {
					elem.parentNode.removeChild( elem );
				}
			}
		}

		return this;
	},

	empty: function() {
		for ( var i = 0, elem; (elem = this[i]) != null; i++ ) {
			// Remove element nodes and prevent memory leaks
			if ( elem.nodeType === 1 ) {
				jQuery.cleanData( elem.getElementsByTagName("*") );
			}

			// Remove any remaining nodes
			while ( elem.firstChild ) {
				elem.removeChild( elem.firstChild );
			}
		}

		return this;
	},

	clone: function( dataAndEvents, deepDataAndEvents ) {
		dataAndEvents = dataAndEvents == null ? false : dataAndEvents;
		deepDataAndEvents = deepDataAndEvents == null ? dataAndEvents : deepDataAndEvents;

		return this.map( function () {
			return jQuery.clone( this, dataAndEvents, deepDataAndEvents );
		});
	},

	html: function( value ) {
		if ( value === undefined ) {
			return this[0] && this[0].nodeType === 1 ?
				this[0].innerHTML.replace(rinlinejQuery, "") :
				null;

		// See if we can take a shortcut and just use innerHTML
		} else if ( typeof value === "string" && !rnocache.test( value ) &&
			(jQuery.support.leadingWhitespace || !rleadingWhitespace.test( value )) &&
			!wrapMap[ (rtagName.exec( value ) || ["", ""])[1].toLowerCase() ] ) {

			value = value.replace(rxhtmlTag, "<$1></$2>");

			try {
				for ( var i = 0, l = this.length; i < l; i++ ) {
					// Remove element nodes and prevent memory leaks
					if ( this[i].nodeType === 1 ) {
						jQuery.cleanData( this[i].getElementsByTagName("*") );
						this[i].innerHTML = value;
					}
				}

			// If using innerHTML throws an exception, use the fallback method
			} catch(e) {
				this.empty().append( value );
			}

		} else if ( jQuery.isFunction( value ) ) {
			this.each(function(i){
				var self = jQuery( this );

				self.html( value.call(this, i, self.html()) );
			});

		} else {
			this.empty().append( value );
		}

		return this;
	},

	replaceWith: function( value ) {
		if ( this[0] && this[0].parentNode ) {
			// Make sure that the elements are removed from the DOM before they are inserted
			// this can help fix replacing a parent with child elements
			if ( jQuery.isFunction( value ) ) {
				return this.each(function(i) {
					var self = jQuery(this), old = self.html();
					self.replaceWith( value.call( this, i, old ) );
				});
			}

			if ( typeof value !== "string" ) {
				value = jQuery( value ).detach();
			}

			return this.each(function() {
				var next = this.nextSibling,
					parent = this.parentNode;

				jQuery( this ).remove();

				if ( next ) {
					jQuery(next).before( value );
				} else {
					jQuery(parent).append( value );
				}
			});
		} else {
			return this.pushStack( jQuery(jQuery.isFunction(value) ? value() : value), "replaceWith", value );
		}
	},

	detach: function( selector ) {
		return this.remove( selector, true );
	},

	domManip: function( args, table, callback ) {
		var results, first, fragment, parent,
			value = args[0],
			scripts = [];

		// We can't cloneNode fragments that contain checked, in WebKit
		if ( !jQuery.support.checkClone && arguments.length === 3 && typeof value === "string" && rchecked.test( value ) ) {
			return this.each(function() {
				jQuery(this).domManip( args, table, callback, true );
			});
		}

		if ( jQuery.isFunction(value) ) {
			return this.each(function(i) {
				var self = jQuery(this);
				args[0] = value.call(this, i, table ? self.html() : undefined);
				self.domManip( args, table, callback );
			});
		}

		if ( this[0] ) {
			parent = value && value.parentNode;

			// If we're in a fragment, just use that instead of building a new one
			if ( jQuery.support.parentNode && parent && parent.nodeType === 11 && parent.childNodes.length === this.length ) {
				results = { fragment: parent };

			} else {
				results = jQuery.buildFragment( args, this, scripts );
			}

			fragment = results.fragment;

			if ( fragment.childNodes.length === 1 ) {
				first = fragment = fragment.firstChild;
			} else {
				first = fragment.firstChild;
			}

			if ( first ) {
				table = table && jQuery.nodeName( first, "tr" );

				for ( var i = 0, l = this.length, lastIndex = l - 1; i < l; i++ ) {
					callback.call(
						table ?
							root(this[i], first) :
							this[i],
						// Make sure that we do not leak memory by inadvertently discarding
						// the original fragment (which might have attached data) instead of
						// using it; in addition, use the original fragment object for the last
						// item instead of first because it can end up being emptied incorrectly
						// in certain situations (Bug #8070).
						// Fragments from the fragment cache must always be cloned and never used
						// in place.
						results.cacheable || (l > 1 && i < lastIndex) ?
							jQuery.clone( fragment, true, true ) :
							fragment
					);
				}
			}

			if ( scripts.length ) {
				jQuery.each( scripts, evalScript );
			}
		}

		return this;
	}
});

function root( elem, cur ) {
	return jQuery.nodeName(elem, "table") ?
		(elem.getElementsByTagName("tbody")[0] ||
		elem.appendChild(elem.ownerDocument.createElement("tbody"))) :
		elem;
}

function cloneCopyEvent( src, dest ) {

	if ( dest.nodeType !== 1 || !jQuery.hasData( src ) ) {
		return;
	}

	var internalKey = jQuery.expando,
		oldData = jQuery.data( src ),
		curData = jQuery.data( dest, oldData );

	// Switch to use the internal data object, if it exists, for the next
	// stage of data copying
	if ( (oldData = oldData[ internalKey ]) ) {
		var events = oldData.events;
				curData = curData[ internalKey ] = jQuery.extend({}, oldData);

		if ( events ) {
			delete curData.handle;
			curData.events = {};

			for ( var type in events ) {
				for ( var i = 0, l = events[ type ].length; i < l; i++ ) {
					jQuery.event.add( dest, type + ( events[ type ][ i ].namespace ? "." : "" ) + events[ type ][ i ].namespace, events[ type ][ i ], events[ type ][ i ].data );
				}
			}
		}
	}
}

function cloneFixAttributes(src, dest) {
	// We do not need to do anything for non-Elements
	if ( dest.nodeType !== 1 ) {
		return;
	}

	var nodeName = dest.nodeName.toLowerCase();

	// clearAttributes removes the attributes, which we don't want,
	// but also removes the attachEvent events, which we *do* want
	dest.clearAttributes();

	// mergeAttributes, in contrast, only merges back on the
	// original attributes, not the events
	dest.mergeAttributes(src);

	// IE6-8 fail to clone children inside object elements that use
	// the proprietary classid attribute value (rather than the type
	// attribute) to identify the type of content to display
	if ( nodeName === "object" ) {
		dest.outerHTML = src.outerHTML;

	} else if ( nodeName === "input" && (src.type === "checkbox" || src.type === "radio") ) {
		// IE6-8 fails to persist the checked state of a cloned checkbox
		// or radio button. Worse, IE6-7 fail to give the cloned element
		// a checked appearance if the defaultChecked value isn't also set
		if ( src.checked ) {
			dest.defaultChecked = dest.checked = src.checked;
		}

		// IE6-7 get confused and end up setting the value of a cloned
		// checkbox/radio button to an empty string instead of "on"
		if ( dest.value !== src.value ) {
			dest.value = src.value;
		}

	// IE6-8 fails to return the selected option to the default selected
	// state when cloning options
	} else if ( nodeName === "option" ) {
		dest.selected = src.defaultSelected;

	// IE6-8 fails to set the defaultValue to the correct value when
	// cloning other types of input fields
	} else if ( nodeName === "input" || nodeName === "textarea" ) {
		dest.defaultValue = src.defaultValue;
	}

	// Event data gets referenced instead of copied if the expando
	// gets copied too
	dest.removeAttribute( jQuery.expando );
}

jQuery.buildFragment = function( args, nodes, scripts ) {
	var fragment, cacheable, cacheresults,
		doc = (nodes && nodes[0] ? nodes[0].ownerDocument || nodes[0] : document);

	// Only cache "small" (1/2 KB) HTML strings that are associated with the main document
	// Cloning options loses the selected state, so don't cache them
	// IE 6 doesn't like it when you put <object> or <embed> elements in a fragment
	// Also, WebKit does not clone 'checked' attributes on cloneNode, so don't cache
	if ( args.length === 1 && typeof args[0] === "string" && args[0].length < 512 && doc === document &&
		args[0].charAt(0) === "<" && !rnocache.test( args[0] ) && (jQuery.support.checkClone || !rchecked.test( args[0] )) ) {

		cacheable = true;
		cacheresults = jQuery.fragments[ args[0] ];
		if ( cacheresults ) {
			if ( cacheresults !== 1 ) {
				fragment = cacheresults;
			}
		}
	}

	if ( !fragment ) {
		fragment = doc.createDocumentFragment();
		jQuery.clean( args, doc, fragment, scripts );
	}

	if ( cacheable ) {
		jQuery.fragments[ args[0] ] = cacheresults ? fragment : 1;
	}

	return { fragment: fragment, cacheable: cacheable };
};

jQuery.fragments = {};

jQuery.each({
	appendTo: "append",
	prependTo: "prepend",
	insertBefore: "before",
	insertAfter: "after",
	replaceAll: "replaceWith"
}, function( name, original ) {
	jQuery.fn[ name ] = function( selector ) {
		var ret = [],
			insert = jQuery( selector ),
			parent = this.length === 1 && this[0].parentNode;

		if ( parent && parent.nodeType === 11 && parent.childNodes.length === 1 && insert.length === 1 ) {
			insert[ original ]( this[0] );
			return this;

		} else {
			for ( var i = 0, l = insert.length; i < l; i++ ) {
				var elems = (i > 0 ? this.clone(true) : this).get();
				jQuery( insert[i] )[ original ]( elems );
				ret = ret.concat( elems );
			}

			return this.pushStack( ret, name, insert.selector );
		}
	};
});

function getAll( elem ) {
	if ( "getElementsByTagName" in elem ) {
		return elem.getElementsByTagName( "*" );
	
	} else if ( "querySelectorAll" in elem ) {
		return elem.querySelectorAll( "*" );

	} else {
		return [];
	}
}

jQuery.extend({
	clone: function( elem, dataAndEvents, deepDataAndEvents ) {
		var clone = elem.cloneNode(true),
				srcElements,
				destElements,
				i;

		if ( (!jQuery.support.noCloneEvent || !jQuery.support.noCloneChecked) &&
				(elem.nodeType === 1 || elem.nodeType === 11) && !jQuery.isXMLDoc(elem) ) {
			// IE copies events bound via attachEvent when using cloneNode.
			// Calling detachEvent on the clone will also remove the events
			// from the original. In order to get around this, we use some
			// proprietary methods to clear the events. Thanks to MooTools
			// guys for this hotness.

			cloneFixAttributes( elem, clone );

			// Using Sizzle here is crazy slow, so we use getElementsByTagName
			// instead
			srcElements = getAll( elem );
			destElements = getAll( clone );

			// Weird iteration because IE will replace the length property
			// with an element if you are cloning the body and one of the
			// elements on the page has a name or id of "length"
			for ( i = 0; srcElements[i]; ++i ) {
				cloneFixAttributes( srcElements[i], destElements[i] );
			}
		}

		// Copy the events from the original to the clone
		if ( dataAndEvents ) {
			cloneCopyEvent( elem, clone );

			if ( deepDataAndEvents ) {
				srcElements = getAll( elem );
				destElements = getAll( clone );

				for ( i = 0; srcElements[i]; ++i ) {
					cloneCopyEvent( srcElements[i], destElements[i] );
				}
			}
		}

		// Return the cloned set
		return clone;
},
	clean: function( elems, context, fragment, scripts ) {
		context = context || document;

		// !context.createElement fails in IE with an error but returns typeof 'object'
		if ( typeof context.createElement === "undefined" ) {
			context = context.ownerDocument || context[0] && context[0].ownerDocument || document;
		}

		var ret = [];

		for ( var i = 0, elem; (elem = elems[i]) != null; i++ ) {
			if ( typeof elem === "number" ) {
				elem += "";
			}

			if ( !elem ) {
				continue;
			}

			// Convert html string into DOM nodes
			if ( typeof elem === "string" && !rhtml.test( elem ) ) {
				elem = context.createTextNode( elem );

			} else if ( typeof elem === "string" ) {
				// Fix "XHTML"-style tags in all browsers
				elem = elem.replace(rxhtmlTag, "<$1></$2>");

				// Trim whitespace, otherwise indexOf won't work as expected
				var tag = (rtagName.exec( elem ) || ["", ""])[1].toLowerCase(),
					wrap = wrapMap[ tag ] || wrapMap._default,
					depth = wrap[0],
					div = context.createElement("div");

				// Go to html and back, then peel off extra wrappers
				div.innerHTML = wrap[1] + elem + wrap[2];

				// Move to the right depth
				while ( depth-- ) {
					div = div.lastChild;
				}

				// Remove IE's autoinserted <tbody> from table fragments
				if ( !jQuery.support.tbody ) {

					// String was a <table>, *may* have spurious <tbody>
					var hasBody = rtbody.test(elem),
						tbody = tag === "table" && !hasBody ?
							div.firstChild && div.firstChild.childNodes :

							// String was a bare <thead> or <tfoot>
							wrap[1] === "<table>" && !hasBody ?
								div.childNodes :
								[];

					for ( var j = tbody.length - 1; j >= 0 ; --j ) {
						if ( jQuery.nodeName( tbody[ j ], "tbody" ) && !tbody[ j ].childNodes.length ) {
							tbody[ j ].parentNode.removeChild( tbody[ j ] );
						}
					}

				}

				// IE completely kills leading whitespace when innerHTML is used
				if ( !jQuery.support.leadingWhitespace && rleadingWhitespace.test( elem ) ) {
					div.insertBefore( context.createTextNode( rleadingWhitespace.exec(elem)[0] ), div.firstChild );
				}

				elem = div.childNodes;
			}

			if ( elem.nodeType ) {
				ret.push( elem );
			} else {
				ret = jQuery.merge( ret, elem );
			}
		}

		if ( fragment ) {
			for ( i = 0; ret[i]; i++ ) {
				if ( scripts && jQuery.nodeName( ret[i], "script" ) && (!ret[i].type || ret[i].type.toLowerCase() === "text/javascript") ) {
					scripts.push( ret[i].parentNode ? ret[i].parentNode.removeChild( ret[i] ) : ret[i] );

				} else {
					if ( ret[i].nodeType === 1 ) {
						ret.splice.apply( ret, [i + 1, 0].concat(jQuery.makeArray(ret[i].getElementsByTagName("script"))) );
					}
					fragment.appendChild( ret[i] );
				}
			}
		}

		return ret;
	},

	cleanData: function( elems ) {
		var data, id, cache = jQuery.cache, internalKey = jQuery.expando, special = jQuery.event.special,
			deleteExpando = jQuery.support.deleteExpando;

		for ( var i = 0, elem; (elem = elems[i]) != null; i++ ) {
			if ( elem.nodeName && jQuery.noData[elem.nodeName.toLowerCase()] ) {
				continue;
			}

			id = elem[ jQuery.expando ];

			if ( id ) {
				data = cache[ id ] && cache[ id ][ internalKey ];

				if ( data && data.events ) {
					for ( var type in data.events ) {
						if ( special[ type ] ) {
							jQuery.event.remove( elem, type );

						// This is a shortcut to avoid jQuery.event.remove's overhead
						} else {
							jQuery.removeEvent( elem, type, data.handle );
						}
					}

					// Null the DOM reference to avoid IE6/7/8 leak (#7054)
					if ( data.handle ) {
						data.handle.elem = null;
					}
				}

				if ( deleteExpando ) {
					delete elem[ jQuery.expando ];

				} else if ( elem.removeAttribute ) {
					elem.removeAttribute( jQuery.expando );
				}

				delete cache[ id ];
			}
		}
	}
});

function evalScript( i, elem ) {
	if ( elem.src ) {
		jQuery.ajax({
			url: elem.src,
			async: false,
			dataType: "script"
		});
	} else {
		jQuery.globalEval( elem.text || elem.textContent || elem.innerHTML || "" );
	}

	if ( elem.parentNode ) {
		elem.parentNode.removeChild( elem );
	}
}




var ralpha = /alpha\([^)]*\)/i,
	ropacity = /opacity=([^)]*)/,
	rdashAlpha = /-([a-z])/ig,
	rupper = /([A-Z])/g,
	rnumpx = /^-?\d+(?:px)?$/i,
	rnum = /^-?\d/,

	cssShow = { position: "absolute", visibility: "hidden", display: "block" },
	cssWidth = [ "Left", "Right" ],
	cssHeight = [ "Top", "Bottom" ],
	curCSS,

	getComputedStyle,
	currentStyle,

	fcamelCase = function( all, letter ) {
		return letter.toUpperCase();
	};

jQuery.fn.css = function( name, value ) {
	// Setting 'undefined' is a no-op
	if ( arguments.length === 2 && value === undefined ) {
		return this;
	}

	return jQuery.access( this, name, value, true, function( elem, name, value ) {
		return value !== undefined ?
			jQuery.style( elem, name, value ) :
			jQuery.css( elem, name );
	});
};

jQuery.extend({
	// Add in style property hooks for overriding the default
	// behavior of getting and setting a style property
	cssHooks: {
		opacity: {
			get: function( elem, computed ) {
				if ( computed ) {
					// We should always get a number back from opacity
					var ret = curCSS( elem, "opacity", "opacity" );
					return ret === "" ? "1" : ret;

				} else {
					return elem.style.opacity;
				}
			}
		}
	},

	// Exclude the following css properties to add px
	cssNumber: {
		"zIndex": true,
		"fontWeight": true,
		"opacity": true,
		"zoom": true,
		"lineHeight": true
	},

	// Add in properties whose names you wish to fix before
	// setting or getting the value
	cssProps: {
		// normalize float css property
		"float": jQuery.support.cssFloat ? "cssFloat" : "styleFloat"
	},

	// Get and set the style property on a DOM Node
	style: function( elem, name, value, extra ) {
		// Don't set styles on text and comment nodes
		if ( !elem || elem.nodeType === 3 || elem.nodeType === 8 || !elem.style ) {
			return;
		}

		// Make sure that we're working with the right name
		var ret, origName = jQuery.camelCase( name ),
			style = elem.style, hooks = jQuery.cssHooks[ origName ];

		name = jQuery.cssProps[ origName ] || origName;

		// Check if we're setting a value
		if ( value !== undefined ) {
			// Make sure that NaN and null values aren't set. See: #7116
			if ( typeof value === "number" && isNaN( value ) || value == null ) {
				return;
			}

			// If a number was passed in, add 'px' to the (except for certain CSS properties)
			if ( typeof value === "number" && !jQuery.cssNumber[ origName ] ) {
				value += "px";
			}

			// If a hook was provided, use that value, otherwise just set the specified value
			if ( !hooks || !("set" in hooks) || (value = hooks.set( elem, value )) !== undefined ) {
				// Wrapped to prevent IE from throwing errors when 'invalid' values are provided
				// Fixes bug #5509
				try {
					style[ name ] = value;
				} catch(e) {}
			}

		} else {
			// If a hook was provided get the non-computed value from there
			if ( hooks && "get" in hooks && (ret = hooks.get( elem, false, extra )) !== undefined ) {
				return ret;
			}

			// Otherwise just get the value from the style object
			return style[ name ];
		}
	},

	css: function( elem, name, extra ) {
		// Make sure that we're working with the right name
		var ret, origName = jQuery.camelCase( name ),
			hooks = jQuery.cssHooks[ origName ];

		name = jQuery.cssProps[ origName ] || origName;

		// If a hook was provided get the computed value from there
		if ( hooks && "get" in hooks && (ret = hooks.get( elem, true, extra )) !== undefined ) {
			return ret;

		// Otherwise, if a way to get the computed value exists, use that
		} else if ( curCSS ) {
			return curCSS( elem, name, origName );
		}
	},

	// A method for quickly swapping in/out CSS properties to get correct calculations
	swap: function( elem, options, callback ) {
		var old = {};

		// Remember the old values, and insert the new ones
		for ( var name in options ) {
			old[ name ] = elem.style[ name ];
			elem.style[ name ] = options[ name ];
		}

		callback.call( elem );

		// Revert the old values
		for ( name in options ) {
			elem.style[ name ] = old[ name ];
		}
	},

	camelCase: function( string ) {
		return string.replace( rdashAlpha, fcamelCase );
	}
});

// DEPRECATED, Use jQuery.css() instead
jQuery.curCSS = jQuery.css;

jQuery.each(["height", "width"], function( i, name ) {
	jQuery.cssHooks[ name ] = {
		get: function( elem, computed, extra ) {
			var val;

			if ( computed ) {
				if ( elem.offsetWidth !== 0 ) {
					val = getWH( elem, name, extra );

				} else {
					jQuery.swap( elem, cssShow, function() {
						val = getWH( elem, name, extra );
					});
				}

				if ( val <= 0 ) {
					val = curCSS( elem, name, name );

					if ( val === "0px" && currentStyle ) {
						val = currentStyle( elem, name, name );
					}

					if ( val != null ) {
						// Should return "auto" instead of 0, use 0 for
						// temporary backwards-compat
						return val === "" || val === "auto" ? "0px" : val;
					}
				}

				if ( val < 0 || val == null ) {
					val = elem.style[ name ];

					// Should return "auto" instead of 0, use 0 for
					// temporary backwards-compat
					return val === "" || val === "auto" ? "0px" : val;
				}

				return typeof val === "string" ? val : val + "px";
			}
		},

		set: function( elem, value ) {
			if ( rnumpx.test( value ) ) {
				// ignore negative width and height values #1599
				value = parseFloat(value);

				if ( value >= 0 ) {
					return value + "px";
				}

			} else {
				return value;
			}
		}
	};
});

if ( !jQuery.support.opacity ) {
	jQuery.cssHooks.opacity = {
		get: function( elem, computed ) {
			// IE uses filters for opacity
			return ropacity.test((computed && elem.currentStyle ? elem.currentStyle.filter : elem.style.filter) || "") ?
				(parseFloat(RegExp.$1) / 100) + "" :
				computed ? "1" : "";
		},

		set: function( elem, value ) {
			var style = elem.style;

			// IE has trouble with opacity if it does not have layout
			// Force it by setting the zoom level
			style.zoom = 1;

			// Set the alpha filter to set the opacity
			var opacity = jQuery.isNaN(value) ?
				"" :
				"alpha(opacity=" + value * 100 + ")",
				filter = style.filter || "";

			style.filter = ralpha.test(filter) ?
				filter.replace(ralpha, opacity) :
				style.filter + ' ' + opacity;
		}
	};
}

if ( document.defaultView && document.defaultView.getComputedStyle ) {
	getComputedStyle = function( elem, newName, name ) {
		var ret, defaultView, computedStyle;

		name = name.replace( rupper, "-$1" ).toLowerCase();

		if ( !(defaultView = elem.ownerDocument.defaultView) ) {
			return undefined;
		}

		if ( (computedStyle = defaultView.getComputedStyle( elem, null )) ) {
			ret = computedStyle.getPropertyValue( name );
			if ( ret === "" && !jQuery.contains( elem.ownerDocument.documentElement, elem ) ) {
				ret = jQuery.style( elem, name );
			}
		}

		return ret;
	};
}

if ( document.documentElement.currentStyle ) {
	currentStyle = function( elem, name ) {
		var left,
			ret = elem.currentStyle && elem.currentStyle[ name ],
			rsLeft = elem.runtimeStyle && elem.runtimeStyle[ name ],
			style = elem.style;

		// From the awesome hack by Dean Edwards
		// http://erik.eae.net/archives/2007/07/27/18.54.15/#comment-102291

		// If we're not dealing with a regular pixel number
		// but a number that has a weird ending, we need to convert it to pixels
		if ( !rnumpx.test( ret ) && rnum.test( ret ) ) {
			// Remember the original values
			left = style.left;

			// Put in the new values to get a computed value out
			if ( rsLeft ) {
				elem.runtimeStyle.left = elem.currentStyle.left;
			}
			style.left = name === "fontSize" ? "1em" : (ret || 0);
			ret = style.pixelLeft + "px";

			// Revert the changed values
			style.left = left;
			if ( rsLeft ) {
				elem.runtimeStyle.left = rsLeft;
			}
		}

		return ret === "" ? "auto" : ret;
	};
}

curCSS = getComputedStyle || currentStyle;

function getWH( elem, name, extra ) {
	var which = name === "width" ? cssWidth : cssHeight,
		val = name === "width" ? elem.offsetWidth : elem.offsetHeight;

	if ( extra === "border" ) {
		return val;
	}

	jQuery.each( which, function() {
		if ( !extra ) {
			val -= parseFloat(jQuery.css( elem, "padding" + this )) || 0;
		}

		if ( extra === "margin" ) {
			val += parseFloat(jQuery.css( elem, "margin" + this )) || 0;

		} else {
			val -= parseFloat(jQuery.css( elem, "border" + this + "Width" )) || 0;
		}
	});

	return val;
}

if ( jQuery.expr && jQuery.expr.filters ) {
	jQuery.expr.filters.hidden = function( elem ) {
		var width = elem.offsetWidth,
			height = elem.offsetHeight;

		return (width === 0 && height === 0) || (!jQuery.support.reliableHiddenOffsets && (elem.style.display || jQuery.css( elem, "display" )) === "none");
	};

	jQuery.expr.filters.visible = function( elem ) {
		return !jQuery.expr.filters.hidden( elem );
	};
}




var r20 = /%20/g,
	rbracket = /\[\]$/,
	rCRLF = /\r?\n/g,
	rhash = /#.*$/,
	rheaders = /^(.*?):[ \t]*([^\r\n]*)\r?$/mg, // IE leaves an \r character at EOL
	rinput = /^(?:color|date|datetime|email|hidden|month|number|password|range|search|tel|text|time|url|week)$/i,
	// #7653, #8125, #8152: local protocol detection
	rlocalProtocol = /(?:^file|^widget|\-extension):$/,
	rnoContent = /^(?:GET|HEAD)$/,
	rprotocol = /^\/\//,
	rquery = /\?/,
	rscript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
	rselectTextarea = /^(?:select|textarea)/i,
	rspacesAjax = /\s+/,
	rts = /([?&])_=[^&]*/,
	rucHeaders = /(^|\-)([a-z])/g,
	rucHeadersFunc = function( _, $1, $2 ) {
		return $1 + $2.toUpperCase();
	},
	rurl = /^([\w\+\.\-]+:)\/\/([^\/?#:]*)(?::(\d+))?/,

	// Keep a copy of the old load method
	_load = jQuery.fn.load,

	/* Prefilters
	 * 1) They are useful to introduce custom dataTypes (see ajax/jsonp.js for an example)
	 * 2) These are called:
	 *    - BEFORE asking for a transport
	 *    - AFTER param serialization (s.data is a string if s.processData is true)
	 * 3) key is the dataType
	 * 4) the catchall symbol "*" can be used
	 * 5) execution will start with transport dataType and THEN continue down to "*" if needed
	 */
	prefilters = {},

	/* Transports bindings
	 * 1) key is the dataType
	 * 2) the catchall symbol "*" can be used
	 * 3) selection will start with transport dataType and THEN go to "*" if needed
	 */
	transports = {},

	// Document location
	ajaxLocation,

	// Document location segments
	ajaxLocParts;

// #8138, IE may throw an exception when accessing
// a field from document.location if document.domain has been set
try {
	ajaxLocation = document.location.href;
} catch( e ) {
	// Use the href attribute of an A element
	// since IE will modify it given document.location
	ajaxLocation = document.createElement( "a" );
	ajaxLocation.href = "";
	ajaxLocation = ajaxLocation.href;
}

// Segment location into parts
ajaxLocParts = rurl.exec( ajaxLocation.toLowerCase() );

// Base "constructor" for jQuery.ajaxPrefilter and jQuery.ajaxTransport
function addToPrefiltersOrTransports( structure ) {

	// dataTypeExpression is optional and defaults to "*"
	return function( dataTypeExpression, func ) {

		if ( typeof dataTypeExpression !== "string" ) {
			func = dataTypeExpression;
			dataTypeExpression = "*";
		}

		if ( jQuery.isFunction( func ) ) {
			var dataTypes = dataTypeExpression.toLowerCase().split( rspacesAjax ),
				i = 0,
				length = dataTypes.length,
				dataType,
				list,
				placeBefore;

			// For each dataType in the dataTypeExpression
			for(; i < length; i++ ) {
				dataType = dataTypes[ i ];
				// We control if we're asked to add before
				// any existing element
				placeBefore = /^\+/.test( dataType );
				if ( placeBefore ) {
					dataType = dataType.substr( 1 ) || "*";
				}
				list = structure[ dataType ] = structure[ dataType ] || [];
				// then we add to the structure accordingly
				list[ placeBefore ? "unshift" : "push" ]( func );
			}
		}
	};
}

//Base inspection function for prefilters and transports
function inspectPrefiltersOrTransports( structure, options, originalOptions, jqXHR,
		dataType /* internal */, inspected /* internal */ ) {

	dataType = dataType || options.dataTypes[ 0 ];
	inspected = inspected || {};

	inspected[ dataType ] = true;

	var list = structure[ dataType ],
		i = 0,
		length = list ? list.length : 0,
		executeOnly = ( structure === prefilters ),
		selection;

	for(; i < length && ( executeOnly || !selection ); i++ ) {
		selection = list[ i ]( options, originalOptions, jqXHR );
		// If we got redirected to another dataType
		// we try there if executing only and not done already
		if ( typeof selection === "string" ) {
			if ( !executeOnly || inspected[ selection ] ) {
				selection = undefined;
			} else {
				options.dataTypes.unshift( selection );
				selection = inspectPrefiltersOrTransports(
						structure, options, originalOptions, jqXHR, selection, inspected );
			}
		}
	}
	// If we're only executing or nothing was selected
	// we try the catchall dataType if not done already
	if ( ( executeOnly || !selection ) && !inspected[ "*" ] ) {
		selection = inspectPrefiltersOrTransports(
				structure, options, originalOptions, jqXHR, "*", inspected );
	}
	// unnecessary when only executing (prefilters)
	// but it'll be ignored by the caller in that case
	return selection;
}

jQuery.fn.extend({
	load: function( url, params, callback ) {
		if ( typeof url !== "string" && _load ) {
			return _load.apply( this, arguments );

		// Don't do a request if no elements are being requested
		} else if ( !this.length ) {
			return this;
		}

		var off = url.indexOf( " " );
		if ( off >= 0 ) {
			var selector = url.slice( off, url.length );
			url = url.slice( 0, off );
		}

		// Default to a GET request
		var type = "GET";

		// If the second parameter was provided
		if ( params ) {
			// If it's a function
			if ( jQuery.isFunction( params ) ) {
				// We assume that it's the callback
				callback = params;
				params = undefined;

			// Otherwise, build a param string
			} else if ( typeof params === "object" ) {
				params = jQuery.param( params, jQuery.ajaxSettings.traditional );
				type = "POST";
			}
		}

		var self = this;

		// Request the remote document
		jQuery.ajax({
			url: url,
			type: type,
			dataType: "html",
			data: params,
			// Complete callback (responseText is used internally)
			complete: function( jqXHR, status, responseText ) {
				// Store the response as specified by the jqXHR object
				responseText = jqXHR.responseText;
				// If successful, inject the HTML into all the matched elements
				if ( jqXHR.isResolved() ) {
					// #4825: Get the actual response in case
					// a dataFilter is present in ajaxSettings
					jqXHR.done(function( r ) {
						responseText = r;
					});
					// See if a selector was specified
					self.html( selector ?
						// Create a dummy div to hold the results
						jQuery("<div>")
							// inject the contents of the document in, removing the scripts
							// to avoid any 'Permission Denied' errors in IE
							.append(responseText.replace(rscript, ""))

							// Locate the specified elements
							.find(selector) :

						// If not, just inject the full result
						responseText );
				}

				if ( callback ) {
					self.each( callback, [ responseText, status, jqXHR ] );
				}
			}
		});

		return this;
	},

	serialize: function() {
		return jQuery.param( this.serializeArray() );
	},

	serializeArray: function() {
		return this.map(function(){
			return this.elements ? jQuery.makeArray( this.elements ) : this;
		})
		.filter(function(){
			return this.name && !this.disabled &&
				( this.checked || rselectTextarea.test( this.nodeName ) ||
					rinput.test( this.type ) );
		})
		.map(function( i, elem ){
			var val = jQuery( this ).val();

			return val == null ?
				null :
				jQuery.isArray( val ) ?
					jQuery.map( val, function( val, i ){
						return { name: elem.name, value: val.replace( rCRLF, "\r\n" ) };
					}) :
					{ name: elem.name, value: val.replace( rCRLF, "\r\n" ) };
		}).get();
	}
});

// Attach a bunch of functions for handling common AJAX events
jQuery.each( "ajaxStart ajaxStop ajaxComplete ajaxError ajaxSuccess ajaxSend".split( " " ), function( i, o ){
	jQuery.fn[ o ] = function( f ){
		return this.bind( o, f );
	};
} );

jQuery.each( [ "get", "post" ], function( i, method ) {
	jQuery[ method ] = function( url, data, callback, type ) {
		// shift arguments if data argument was omitted
		if ( jQuery.isFunction( data ) ) {
			type = type || callback;
			callback = data;
			data = undefined;
		}

		return jQuery.ajax({
			type: method,
			url: url,
			data: data,
			success: callback,
			dataType: type
		});
	};
} );

jQuery.extend({

	getScript: function( url, callback ) {
		return jQuery.get( url, undefined, callback, "script" );
	},

	getJSON: function( url, data, callback ) {
		return jQuery.get( url, data, callback, "json" );
	},

	// Creates a full fledged settings object into target
	// with both ajaxSettings and settings fields.
	// If target is omitted, writes into ajaxSettings.
	ajaxSetup: function ( target, settings ) {
		if ( !settings ) {
			// Only one parameter, we extend ajaxSettings
			settings = target;
			target = jQuery.extend( true, jQuery.ajaxSettings, settings );
		} else {
			// target was provided, we extend into it
			jQuery.extend( true, target, jQuery.ajaxSettings, settings );
		}
		// Flatten fields we don't want deep extended
		for( var field in { context: 1, url: 1 } ) {
			if ( field in settings ) {
				target[ field ] = settings[ field ];
			} else if( field in jQuery.ajaxSettings ) {
				target[ field ] = jQuery.ajaxSettings[ field ];
			}
		}
		return target;
	},

	ajaxSettings: {
		url: ajaxLocation,
		isLocal: rlocalProtocol.test( ajaxLocParts[ 1 ] ),
		global: true,
		type: "GET",
		contentType: "application/x-www-form-urlencoded",
		processData: true,
		async: true,
		/*
		timeout: 0,
		data: null,
		dataType: null,
		username: null,
		password: null,
		cache: null,
		traditional: false,
		headers: {},
		crossDomain: null,
		*/

		accepts: {
			xml: "application/xml, text/xml",
			html: "text/html",
			text: "text/plain",
			json: "application/json, text/javascript",
			"*": "*/*"
		},

		contents: {
			xml: /xml/,
			html: /html/,
			json: /json/
		},

		responseFields: {
			xml: "responseXML",
			text: "responseText"
		},

		// List of data converters
		// 1) key format is "source_type destination_type" (a single space in-between)
		// 2) the catchall symbol "*" can be used for source_type
		converters: {

			// Convert anything to text
			"* text": window.String,

			// Text to html (true = no transformation)
			"text html": true,

			// Evaluate text as a json expression
			"text json": jQuery.parseJSON,

			// Parse text as xml
			"text xml": jQuery.parseXML
		}
	},

	ajaxPrefilter: addToPrefiltersOrTransports( prefilters ),
	ajaxTransport: addToPrefiltersOrTransports( transports ),

	// Main method
	ajax: function( url, options ) {

		// If url is an object, simulate pre-1.5 signature
		if ( typeof url === "object" ) {
			options = url;
			url = undefined;
		}

		// Force options to be an object
		options = options || {};

		var // Create the final options object
			s = jQuery.ajaxSetup( {}, options ),
			// Callbacks context
			callbackContext = s.context || s,
			// Context for global events
			// It's the callbackContext if one was provided in the options
			// and if it's a DOM node or a jQuery collection
			globalEventContext = callbackContext !== s &&
				( callbackContext.nodeType || callbackContext instanceof jQuery ) ?
						jQuery( callbackContext ) : jQuery.event,
			// Deferreds
			deferred = jQuery.Deferred(),
			completeDeferred = jQuery._Deferred(),
			// Status-dependent callbacks
			statusCode = s.statusCode || {},
			// ifModified key
			ifModifiedKey,
			// Headers (they are sent all at once)
			requestHeaders = {},
			// Response headers
			responseHeadersString,
			responseHeaders,
			// transport
			transport,
			// timeout handle
			timeoutTimer,
			// Cross-domain detection vars
			parts,
			// The jqXHR state
			state = 0,
			// To know if global events are to be dispatched
			fireGlobals,
			// Loop variable
			i,
			// Fake xhr
			jqXHR = {

				readyState: 0,

				// Caches the header
				setRequestHeader: function( name, value ) {
					if ( !state ) {
						requestHeaders[ name.toLowerCase().replace( rucHeaders, rucHeadersFunc ) ] = value;
					}
					return this;
				},

				// Raw string
				getAllResponseHeaders: function() {
					return state === 2 ? responseHeadersString : null;
				},

				// Builds headers hashtable if needed
				getResponseHeader: function( key ) {
					var match;
					if ( state === 2 ) {
						if ( !responseHeaders ) {
							responseHeaders = {};
							while( ( match = rheaders.exec( responseHeadersString ) ) ) {
								responseHeaders[ match[1].toLowerCase() ] = match[ 2 ];
							}
						}
						match = responseHeaders[ key.toLowerCase() ];
					}
					return match === undefined ? null : match;
				},

				// Overrides response content-type header
				overrideMimeType: function( type ) {
					if ( !state ) {
						s.mimeType = type;
					}
					return this;
				},

				// Cancel the request
				abort: function( statusText ) {
					statusText = statusText || "abort";
					if ( transport ) {
						transport.abort( statusText );
					}
					done( 0, statusText );
					return this;
				}
			};

		// Callback for when everything is done
		// It is defined here because jslint complains if it is declared
		// at the end of the function (which would be more logical and readable)
		function done( status, statusText, responses, headers ) {

			// Called once
			if ( state === 2 ) {
				return;
			}

			// State is "done" now
			state = 2;

			// Clear timeout if it exists
			if ( timeoutTimer ) {
				clearTimeout( timeoutTimer );
			}

			// Dereference transport for early garbage collection
			// (no matter how long the jqXHR object will be used)
			transport = undefined;

			// Cache response headers
			responseHeadersString = headers || "";

			// Set readyState
			jqXHR.readyState = status ? 4 : 0;

			var isSuccess,
				success,
				error,
				response = responses ? ajaxHandleResponses( s, jqXHR, responses ) : undefined,
				lastModified,
				etag;

			// If successful, handle type chaining
			if ( status >= 200 && status < 300 || status === 304 ) {

				// Set the If-Modified-Since and/or If-None-Match header, if in ifModified mode.
				if ( s.ifModified ) {

					if ( ( lastModified = jqXHR.getResponseHeader( "Last-Modified" ) ) ) {
						jQuery.lastModified[ ifModifiedKey ] = lastModified;
					}
					if ( ( etag = jqXHR.getResponseHeader( "Etag" ) ) ) {
						jQuery.etag[ ifModifiedKey ] = etag;
					}
				}

				// If not modified
				if ( status === 304 ) {

					statusText = "notmodified";
					isSuccess = true;

				// If we have data
				} else {

					try {
						success = ajaxConvert( s, response );
						statusText = "success";
						isSuccess = true;
					} catch(e) {
						// We have a parsererror
						statusText = "parsererror";
						error = e;
					}
				}
			} else {
				// We extract error from statusText
				// then normalize statusText and status for non-aborts
				error = statusText;
				if( !statusText || status ) {
					statusText = "error";
					if ( status < 0 ) {
						status = 0;
					}
				}
			}

			// Set data for the fake xhr object
			jqXHR.status = status;
			jqXHR.statusText = statusText;

			// Success/Error
			if ( isSuccess ) {
				deferred.resolveWith( callbackContext, [ success, statusText, jqXHR ] );
			} else {
				deferred.rejectWith( callbackContext, [ jqXHR, statusText, error ] );
			}

			// Status-dependent callbacks
			jqXHR.statusCode( statusCode );
			statusCode = undefined;

			if ( fireGlobals ) {
				globalEventContext.trigger( "ajax" + ( isSuccess ? "Success" : "Error" ),
						[ jqXHR, s, isSuccess ? success : error ] );
			}

			// Complete
			completeDeferred.resolveWith( callbackContext, [ jqXHR, statusText ] );

			if ( fireGlobals ) {
				globalEventContext.trigger( "ajaxComplete", [ jqXHR, s] );
				// Handle the global AJAX counter
				if ( !( --jQuery.active ) ) {
					jQuery.event.trigger( "ajaxStop" );
				}
			}
		}

		// Attach deferreds
		deferred.promise( jqXHR );
		jqXHR.success = jqXHR.done;
		jqXHR.error = jqXHR.fail;
		jqXHR.complete = completeDeferred.done;

		// Status-dependent callbacks
		jqXHR.statusCode = function( map ) {
			if ( map ) {
				var tmp;
				if ( state < 2 ) {
					for( tmp in map ) {
						statusCode[ tmp ] = [ statusCode[tmp], map[tmp] ];
					}
				} else {
					tmp = map[ jqXHR.status ];
					jqXHR.then( tmp, tmp );
				}
			}
			return this;
		};

		// Remove hash character (#7531: and string promotion)
		// Add protocol if not provided (#5866: IE7 issue with protocol-less urls)
		// We also use the url parameter if available
		s.url = ( ( url || s.url ) + "" ).replace( rhash, "" ).replace( rprotocol, ajaxLocParts[ 1 ] + "//" );

		// Extract dataTypes list
		s.dataTypes = jQuery.trim( s.dataType || "*" ).toLowerCase().split( rspacesAjax );

		// Determine if a cross-domain request is in order
		if ( !s.crossDomain ) {
			parts = rurl.exec( s.url.toLowerCase() );
			s.crossDomain = !!( parts &&
				( parts[ 1 ] != ajaxLocParts[ 1 ] || parts[ 2 ] != ajaxLocParts[ 2 ] ||
					( parts[ 3 ] || ( parts[ 1 ] === "http:" ? 80 : 443 ) ) !=
						( ajaxLocParts[ 3 ] || ( ajaxLocParts[ 1 ] === "http:" ? 80 : 443 ) ) )
			);
		}

		// Convert data if not already a string
		if ( s.data && s.processData && typeof s.data !== "string" ) {
			s.data = jQuery.param( s.data, s.traditional );
		}

		// Apply prefilters
		inspectPrefiltersOrTransports( prefilters, s, options, jqXHR );

		// If request was aborted inside a prefiler, stop there
		if ( state === 2 ) {
			return false;
		}

		// We can fire global events as of now if asked to
		fireGlobals = s.global;

		// Uppercase the type
		s.type = s.type.toUpperCase();

		// Determine if request has content
		s.hasContent = !rnoContent.test( s.type );

		// Watch for a new set of requests
		if ( fireGlobals && jQuery.active++ === 0 ) {
			jQuery.event.trigger( "ajaxStart" );
		}

		// More options handling for requests with no content
		if ( !s.hasContent ) {

			// If data is available, append data to url
			if ( s.data ) {
				s.url += ( rquery.test( s.url ) ? "&" : "?" ) + s.data;
			}

			// Get ifModifiedKey before adding the anti-cache parameter
			ifModifiedKey = s.url;

			// Add anti-cache in url if needed
			if ( s.cache === false ) {

				var ts = jQuery.now(),
					// try replacing _= if it is there
					ret = s.url.replace( rts, "$1_=" + ts );

				// if nothing was replaced, add timestamp to the end
				s.url = ret + ( (ret === s.url ) ? ( rquery.test( s.url ) ? "&" : "?" ) + "_=" + ts : "" );
			}
		}

		// Set the correct header, if data is being sent
		if ( s.data && s.hasContent && s.contentType !== false || options.contentType ) {
			requestHeaders[ "Content-Type" ] = s.contentType;
		}

		// Set the If-Modified-Since and/or If-None-Match header, if in ifModified mode.
		if ( s.ifModified ) {
			ifModifiedKey = ifModifiedKey || s.url;
			if ( jQuery.lastModified[ ifModifiedKey ] ) {
				requestHeaders[ "If-Modified-Since" ] = jQuery.lastModified[ ifModifiedKey ];
			}
			if ( jQuery.etag[ ifModifiedKey ] ) {
				requestHeaders[ "If-None-Match" ] = jQuery.etag[ ifModifiedKey ];
			}
		}

		// Set the Accepts header for the server, depending on the dataType
		requestHeaders.Accept = s.dataTypes[ 0 ] && s.accepts[ s.dataTypes[0] ] ?
			s.accepts[ s.dataTypes[0] ] + ( s.dataTypes[ 0 ] !== "*" ? ", */*; q=0.01" : "" ) :
			s.accepts[ "*" ];

		// Check for headers option
		for ( i in s.headers ) {
			jqXHR.setRequestHeader( i, s.headers[ i ] );
		}

		// Allow custom headers/mimetypes and early abort
		if ( s.beforeSend && ( s.beforeSend.call( callbackContext, jqXHR, s ) === false || state === 2 ) ) {
				// Abort if not done already
				jqXHR.abort();
				return false;

		}

		// Install callbacks on deferreds
		for ( i in { success: 1, error: 1, complete: 1 } ) {
			jqXHR[ i ]( s[ i ] );
		}

		// Get transport
		transport = inspectPrefiltersOrTransports( transports, s, options, jqXHR );

		// If no transport, we auto-abort
		if ( !transport ) {
			done( -1, "No Transport" );
		} else {
			jqXHR.readyState = 1;
			// Send global event
			if ( fireGlobals ) {
				globalEventContext.trigger( "ajaxSend", [ jqXHR, s ] );
			}
			// Timeout
			if ( s.async && s.timeout > 0 ) {
				timeoutTimer = setTimeout( function(){
					jqXHR.abort( "timeout" );
				}, s.timeout );
			}

			try {
				state = 1;
				transport.send( requestHeaders, done );
			} catch (e) {
				// Propagate exception as error if not done
				if ( status < 2 ) {
					done( -1, e );
				// Simply rethrow otherwise
				} else {
					jQuery.error( e );
				}
			}
		}

		return jqXHR;
	},

	// Serialize an array of form elements or a set of
	// key/values into a query string
	param: function( a, traditional ) {
		var s = [],
			add = function( key, value ) {
				// If value is a function, invoke it and return its value
				value = jQuery.isFunction( value ) ? value() : value;
				s[ s.length ] = encodeURIComponent( key ) + "=" + encodeURIComponent( value );
			};

		// Set traditional to true for jQuery <= 1.3.2 behavior.
		if ( traditional === undefined ) {
			traditional = jQuery.ajaxSettings.traditional;
		}

		// If an array was passed in, assume that it is an array of form elements.
		if ( jQuery.isArray( a ) || ( a.jquery && !jQuery.isPlainObject( a ) ) ) {
			// Serialize the form elements
			jQuery.each( a, function() {
				add( this.name, this.value );
			} );

		} else {
			// If traditional, encode the "old" way (the way 1.3.2 or older
			// did it), otherwise encode params recursively.
			for ( var prefix in a ) {
				buildParams( prefix, a[ prefix ], traditional, add );
			}
		}

		// Return the resulting serialization
		return s.join( "&" ).replace( r20, "+" );
	}
});

function buildParams( prefix, obj, traditional, add ) {
	if ( jQuery.isArray( obj ) && obj.length ) {
		// Serialize array item.
		jQuery.each( obj, function( i, v ) {
			if ( traditional || rbracket.test( prefix ) ) {
				// Treat each array item as a scalar.
				add( prefix, v );

			} else {
				// If array item is non-scalar (array or object), encode its
				// numeric index to resolve deserialization ambiguity issues.
				// Note that rack (as of 1.0.0) can't currently deserialize
				// nested arrays properly, and attempting to do so may cause
				// a server error. Possible fixes are to modify rack's
				// deserialization algorithm or to provide an option or flag
				// to force array serialization to be shallow.
				buildParams( prefix + "[" + ( typeof v === "object" || jQuery.isArray(v) ? i : "" ) + "]", v, traditional, add );
			}
		});

	} else if ( !traditional && obj != null && typeof obj === "object" ) {
		// If we see an array here, it is empty and should be treated as an empty
		// object
		if ( jQuery.isArray( obj ) || jQuery.isEmptyObject( obj ) ) {
			add( prefix, "" );

		// Serialize object item.
		} else {
			for ( var name in obj ) {
				buildParams( prefix + "[" + name + "]", obj[ name ], traditional, add );
			}
		}

	} else {
		// Serialize scalar item.
		add( prefix, obj );
	}
}

// This is still on the jQuery object... for now
// Want to move this to jQuery.ajax some day
jQuery.extend({

	// Counter for holding the number of active queries
	active: 0,

	// Last-Modified header cache for next request
	lastModified: {},
	etag: {}

});

/* Handles responses to an ajax request:
 * - sets all responseXXX fields accordingly
 * - finds the right dataType (mediates between content-type and expected dataType)
 * - returns the corresponding response
 */
function ajaxHandleResponses( s, jqXHR, responses ) {

	var contents = s.contents,
		dataTypes = s.dataTypes,
		responseFields = s.responseFields,
		ct,
		type,
		finalDataType,
		firstDataType;

	// Fill responseXXX fields
	for( type in responseFields ) {
		if ( type in responses ) {
			jqXHR[ responseFields[type] ] = responses[ type ];
		}
	}

	// Remove auto dataType and get content-type in the process
	while( dataTypes[ 0 ] === "*" ) {
		dataTypes.shift();
		if ( ct === undefined ) {
			ct = s.mimeType || jqXHR.getResponseHeader( "content-type" );
		}
	}

	// Check if we're dealing with a known content-type
	if ( ct ) {
		for ( type in contents ) {
			if ( contents[ type ] && contents[ type ].test( ct ) ) {
				dataTypes.unshift( type );
				break;
			}
		}
	}

	// Check to see if we have a response for the expected dataType
	if ( dataTypes[ 0 ] in responses ) {
		finalDataType = dataTypes[ 0 ];
	} else {
		// Try convertible dataTypes
		for ( type in responses ) {
			if ( !dataTypes[ 0 ] || s.converters[ type + " " + dataTypes[0] ] ) {
				finalDataType = type;
				break;
			}
			if ( !firstDataType ) {
				firstDataType = type;
			}
		}
		// Or just use first one
		finalDataType = finalDataType || firstDataType;
	}

	// If we found a dataType
	// We add the dataType to the list if needed
	// and return the corresponding response
	if ( finalDataType ) {
		if ( finalDataType !== dataTypes[ 0 ] ) {
			dataTypes.unshift( finalDataType );
		}
		return responses[ finalDataType ];
	}
}

// Chain conversions given the request and the original response
function ajaxConvert( s, response ) {

	// Apply the dataFilter if provided
	if ( s.dataFilter ) {
		response = s.dataFilter( response, s.dataType );
	}

	var dataTypes = s.dataTypes,
		converters = {},
		i,
		key,
		length = dataTypes.length,
		tmp,
		// Current and previous dataTypes
		current = dataTypes[ 0 ],
		prev,
		// Conversion expression
		conversion,
		// Conversion function
		conv,
		// Conversion functions (transitive conversion)
		conv1,
		conv2;

	// For each dataType in the chain
	for( i = 1; i < length; i++ ) {

		// Create converters map
		// with lowercased keys
		if ( i === 1 ) {
			for( key in s.converters ) {
				if( typeof key === "string" ) {
					converters[ key.toLowerCase() ] = s.converters[ key ];
				}
			}
		}

		// Get the dataTypes
		prev = current;
		current = dataTypes[ i ];

		// If current is auto dataType, update it to prev
		if( current === "*" ) {
			current = prev;
		// If no auto and dataTypes are actually different
		} else if ( prev !== "*" && prev !== current ) {

			// Get the converter
			conversion = prev + " " + current;
			conv = converters[ conversion ] || converters[ "* " + current ];

			// If there is no direct converter, search transitively
			if ( !conv ) {
				conv2 = undefined;
				for( conv1 in converters ) {
					tmp = conv1.split( " " );
					if ( tmp[ 0 ] === prev || tmp[ 0 ] === "*" ) {
						conv2 = converters[ tmp[1] + " " + current ];
						if ( conv2 ) {
							conv1 = converters[ conv1 ];
							if ( conv1 === true ) {
								conv = conv2;
							} else if ( conv2 === true ) {
								conv = conv1;
							}
							break;
						}
					}
				}
			}
			// If we found no converter, dispatch an error
			if ( !( conv || conv2 ) ) {
				jQuery.error( "No conversion from " + conversion.replace(" "," to ") );
			}
			// If found converter is not an equivalence
			if ( conv !== true ) {
				// Convert with 1 or 2 converters accordingly
				response = conv ? conv( response ) : conv2( conv1(response) );
			}
		}
	}
	return response;
}




var jsc = jQuery.now(),
	jsre = /(\=)\?(&|$)|()\?\?()/i;

// Default jsonp settings
jQuery.ajaxSetup({
	jsonp: "callback",
	jsonpCallback: function() {
		return jQuery.expando + "_" + ( jsc++ );
	}
});

// Detect, normalize options and install callbacks for jsonp requests
jQuery.ajaxPrefilter( "json jsonp", function( s, originalSettings, jqXHR ) {

	var dataIsString = ( typeof s.data === "string" );

	if ( s.dataTypes[ 0 ] === "jsonp" ||
		originalSettings.jsonpCallback ||
		originalSettings.jsonp != null ||
		s.jsonp !== false && ( jsre.test( s.url ) ||
				dataIsString && jsre.test( s.data ) ) ) {

		var responseContainer,
			jsonpCallback = s.jsonpCallback =
				jQuery.isFunction( s.jsonpCallback ) ? s.jsonpCallback() : s.jsonpCallback,
			previous = window[ jsonpCallback ],
			url = s.url,
			data = s.data,
			replace = "$1" + jsonpCallback + "$2",
			cleanUp = function() {
				// Set callback back to previous value
				window[ jsonpCallback ] = previous;
				// Call if it was a function and we have a response
				if ( responseContainer && jQuery.isFunction( previous ) ) {
					window[ jsonpCallback ]( responseContainer[ 0 ] );
				}
			};

		if ( s.jsonp !== false ) {
			url = url.replace( jsre, replace );
			if ( s.url === url ) {
				if ( dataIsString ) {
					data = data.replace( jsre, replace );
				}
				if ( s.data === data ) {
					// Add callback manually
					url += (/\?/.test( url ) ? "&" : "?") + s.jsonp + "=" + jsonpCallback;
				}
			}
		}

		s.url = url;
		s.data = data;

		// Install callback
		window[ jsonpCallback ] = function( response ) {
			responseContainer = [ response ];
		};

		// Install cleanUp function
		jqXHR.then( cleanUp, cleanUp );

		// Use data converter to retrieve json after script execution
		s.converters["script json"] = function() {
			if ( !responseContainer ) {
				jQuery.error( jsonpCallback + " was not called" );
			}
			return responseContainer[ 0 ];
		};

		// force json dataType
		s.dataTypes[ 0 ] = "json";

		// Delegate to script
		return "script";
	}
} );




// Install script dataType
jQuery.ajaxSetup({
	accepts: {
		script: "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"
	},
	contents: {
		script: /javascript|ecmascript/
	},
	converters: {
		"text script": function( text ) {
			jQuery.globalEval( text );
			return text;
		}
	}
});

// Handle cache's special case and global
jQuery.ajaxPrefilter( "script", function( s ) {
	if ( s.cache === undefined ) {
		s.cache = false;
	}
	if ( s.crossDomain ) {
		s.type = "GET";
		s.global = false;
	}
} );

// Bind script tag hack transport
jQuery.ajaxTransport( "script", function(s) {

	// This transport only deals with cross domain requests
	if ( s.crossDomain ) {

		var script,
			head = document.head || document.getElementsByTagName( "head" )[0] || document.documentElement;

		return {

			send: function( _, callback ) {

				script = document.createElement( "script" );

				script.async = "async";

				if ( s.scriptCharset ) {
					script.charset = s.scriptCharset;
				}

				script.src = s.url;

				// Attach handlers for all browsers
				script.onload = script.onreadystatechange = function( _, isAbort ) {

					if ( !script.readyState || /loaded|complete/.test( script.readyState ) ) {

						// Handle memory leak in IE
						script.onload = script.onreadystatechange = null;

						// Remove the script
						if ( head && script.parentNode ) {
							head.removeChild( script );
						}

						// Dereference the script
						script = undefined;

						// Callback if not abort
						if ( !isAbort ) {
							callback( 200, "success" );
						}
					}
				};
				// Use insertBefore instead of appendChild  to circumvent an IE6 bug.
				// This arises when a base node is used (#2709 and #4378).
				head.insertBefore( script, head.firstChild );
			},

			abort: function() {
				if ( script ) {
					script.onload( 0, 1 );
				}
			}
		};
	}
} );




var // #5280: next active xhr id and list of active xhrs' callbacks
	xhrId = jQuery.now(),
	xhrCallbacks,

	// XHR used to determine supports properties
	testXHR;

// #5280: Internet Explorer will keep connections alive if we don't abort on unload
function xhrOnUnloadAbort() {
	jQuery( window ).unload(function() {
		// Abort all pending requests
		for ( var key in xhrCallbacks ) {
			xhrCallbacks[ key ]( 0, 1 );
		}
	});
}

// Functions to create xhrs
function createStandardXHR() {
	try {
		return new window.XMLHttpRequest();
	} catch( e ) {}
}

function createActiveXHR() {
	try {
		return new window.ActiveXObject( "Microsoft.XMLHTTP" );
	} catch( e ) {}
}

// Create the request object
// (This is still attached to ajaxSettings for backward compatibility)
jQuery.ajaxSettings.xhr = window.ActiveXObject ?
	/* Microsoft failed to properly
	 * implement the XMLHttpRequest in IE7 (can't request local files),
	 * so we use the ActiveXObject when it is available
	 * Additionally XMLHttpRequest can be disabled in IE7/IE8 so
	 * we need a fallback.
	 */
	function() {
		return !this.isLocal && createStandardXHR() || createActiveXHR();
	} :
	// For all other browsers, use the standard XMLHttpRequest object
	createStandardXHR;

// Test if we can create an xhr object
testXHR = jQuery.ajaxSettings.xhr();
jQuery.support.ajax = !!testXHR;

// Does this browser support crossDomain XHR requests
jQuery.support.cors = testXHR && ( "withCredentials" in testXHR );

// No need for the temporary xhr anymore
testXHR = undefined;

// Create transport if the browser can provide an xhr
if ( jQuery.support.ajax ) {

	jQuery.ajaxTransport(function( s ) {
		// Cross domain only allowed if supported through XMLHttpRequest
		if ( !s.crossDomain || jQuery.support.cors ) {

			var callback;

			return {
				send: function( headers, complete ) {

					// Get a new xhr
					var xhr = s.xhr(),
						handle,
						i;

					// Open the socket
					// Passing null username, generates a login popup on Opera (#2865)
					if ( s.username ) {
						xhr.open( s.type, s.url, s.async, s.username, s.password );
					} else {
						xhr.open( s.type, s.url, s.async );
					}

					// Apply custom fields if provided
					if ( s.xhrFields ) {
						for ( i in s.xhrFields ) {
							xhr[ i ] = s.xhrFields[ i ];
						}
					}

					// Override mime type if needed
					if ( s.mimeType && xhr.overrideMimeType ) {
						xhr.overrideMimeType( s.mimeType );
					}

					// Requested-With header
					// Not set for crossDomain requests with no content
					// (see why at http://trac.dojotoolkit.org/ticket/9486)
					// Won't change header if already provided
					if ( !( s.crossDomain && !s.hasContent ) && !headers["X-Requested-With"] ) {
						headers[ "X-Requested-With" ] = "XMLHttpRequest";
					}

					// Need an extra try/catch for cross domain requests in Firefox 3
					try {
						for ( i in headers ) {
							xhr.setRequestHeader( i, headers[ i ] );
						}
					} catch( _ ) {}

					// Do send the request
					// This may raise an exception which is actually
					// handled in jQuery.ajax (so no try/catch here)
					xhr.send( ( s.hasContent && s.data ) || null );

					// Listener
					callback = function( _, isAbort ) {

						var status,
							statusText,
							responseHeaders,
							responses,
							xml;

						// Firefox throws exceptions when accessing properties
						// of an xhr when a network error occured
						// http://helpful.knobs-dials.com/index.php/Component_returned_failure_code:_0x80040111_(NS_ERROR_NOT_AVAILABLE)
						try {

							// Was never called and is aborted or complete
							if ( callback && ( isAbort || xhr.readyState === 4 ) ) {

								// Only called once
								callback = undefined;

								// Do not keep as active anymore
								if ( handle ) {
									xhr.onreadystatechange = jQuery.noop;
									delete xhrCallbacks[ handle ];
								}

								// If it's an abort
								if ( isAbort ) {
									// Abort it manually if needed
									if ( xhr.readyState !== 4 ) {
										xhr.abort();
									}
								} else {
									status = xhr.status;
									responseHeaders = xhr.getAllResponseHeaders();
									responses = {};
									xml = xhr.responseXML;

									// Construct response list
									if ( xml && xml.documentElement /* #4958 */ ) {
										responses.xml = xml;
									}
									responses.text = xhr.responseText;

									// Firefox throws an exception when accessing
									// statusText for faulty cross-domain requests
									try {
										statusText = xhr.statusText;
									} catch( e ) {
										// We normalize with Webkit giving an empty statusText
										statusText = "";
									}

									// Filter status for non standard behaviors

									// If the request is local and we have data: assume a success
									// (success with no data won't get notified, that's the best we
									// can do given current implementations)
									if ( !status && s.isLocal && !s.crossDomain ) {
										status = responses.text ? 200 : 404;
									// IE - #1450: sometimes returns 1223 when it should be 204
									} else if ( status === 1223 ) {
										status = 204;
									}
								}
							}
						} catch( firefoxAccessException ) {
							if ( !isAbort ) {
								complete( -1, firefoxAccessException );
							}
						}

						// Call complete if needed
						if ( responses ) {
							complete( status, statusText, responses, responseHeaders );
						}
					};

					// if we're in sync mode or it's in cache
					// and has been retrieved directly (IE6 & IE7)
					// we need to manually fire the callback
					if ( !s.async || xhr.readyState === 4 ) {
						callback();
					} else {
						// Create the active xhrs callbacks list if needed
						// and attach the unload handler
						if ( !xhrCallbacks ) {
							xhrCallbacks = {};
							xhrOnUnloadAbort();
						}
						// Add to list of active xhrs callbacks
						handle = xhrId++;
						xhr.onreadystatechange = xhrCallbacks[ handle ] = callback;
					}
				},

				abort: function() {
					if ( callback ) {
						callback(0,1);
					}
				}
			};
		}
	});
}




var elemdisplay = {},
	rfxtypes = /^(?:toggle|show|hide)$/,
	rfxnum = /^([+\-]=)?([\d+.\-]+)([a-z%]*)$/i,
	timerId,
	fxAttrs = [
		// height animations
		[ "height", "marginTop", "marginBottom", "paddingTop", "paddingBottom" ],
		// width animations
		[ "width", "marginLeft", "marginRight", "paddingLeft", "paddingRight" ],
		// opacity animations
		[ "opacity" ]
	];

jQuery.fn.extend({
	show: function( speed, easing, callback ) {
		var elem, display;

		if ( speed || speed === 0 ) {
			return this.animate( genFx("show", 3), speed, easing, callback);

		} else {
			for ( var i = 0, j = this.length; i < j; i++ ) {
				elem = this[i];
				display = elem.style.display;

				// Reset the inline display of this element to learn if it is
				// being hidden by cascaded rules or not
				if ( !jQuery._data(elem, "olddisplay") && display === "none" ) {
					display = elem.style.display = "";
				}

				// Set elements which have been overridden with display: none
				// in a stylesheet to whatever the default browser style is
				// for such an element
				if ( display === "" && jQuery.css( elem, "display" ) === "none" ) {
					jQuery._data(elem, "olddisplay", defaultDisplay(elem.nodeName));
				}
			}

			// Set the display of most of the elements in a second loop
			// to avoid the constant reflow
			for ( i = 0; i < j; i++ ) {
				elem = this[i];
				display = elem.style.display;

				if ( display === "" || display === "none" ) {
					elem.style.display = jQuery._data(elem, "olddisplay") || "";
				}
			}

			return this;
		}
	},

	hide: function( speed, easing, callback ) {
		if ( speed || speed === 0 ) {
			return this.animate( genFx("hide", 3), speed, easing, callback);

		} else {
			for ( var i = 0, j = this.length; i < j; i++ ) {
				var display = jQuery.css( this[i], "display" );

				if ( display !== "none" && !jQuery._data( this[i], "olddisplay" ) ) {
					jQuery._data( this[i], "olddisplay", display );
				}
			}

			// Set the display of the elements in a second loop
			// to avoid the constant reflow
			for ( i = 0; i < j; i++ ) {
				this[i].style.display = "none";
			}

			return this;
		}
	},

	// Save the old toggle function
	_toggle: jQuery.fn.toggle,

	toggle: function( fn, fn2, callback ) {
		var bool = typeof fn === "boolean";

		if ( jQuery.isFunction(fn) && jQuery.isFunction(fn2) ) {
			this._toggle.apply( this, arguments );

		} else if ( fn == null || bool ) {
			this.each(function() {
				var state = bool ? fn : jQuery(this).is(":hidden");
				jQuery(this)[ state ? "show" : "hide" ]();
			});

		} else {
			this.animate(genFx("toggle", 3), fn, fn2, callback);
		}

		return this;
	},

	fadeTo: function( speed, to, easing, callback ) {
		return this.filter(":hidden").css("opacity", 0).show().end()
					.animate({opacity: to}, speed, easing, callback);
	},

	animate: function( prop, speed, easing, callback ) {
		var optall = jQuery.speed(speed, easing, callback);

		if ( jQuery.isEmptyObject( prop ) ) {
			return this.each( optall.complete );
		}

		return this[ optall.queue === false ? "each" : "queue" ](function() {
			// XXX 'this' does not always have a nodeName when running the
			// test suite

			var opt = jQuery.extend({}, optall), p,
				isElement = this.nodeType === 1,
				hidden = isElement && jQuery(this).is(":hidden"),
				self = this;

			for ( p in prop ) {
				var name = jQuery.camelCase( p );

				if ( p !== name ) {
					prop[ name ] = prop[ p ];
					delete prop[ p ];
					p = name;
				}

				if ( prop[p] === "hide" && hidden || prop[p] === "show" && !hidden ) {
					return opt.complete.call(this);
				}

				if ( isElement && ( p === "height" || p === "width" ) ) {
					// Make sure that nothing sneaks out
					// Record all 3 overflow attributes because IE does not
					// change the overflow attribute when overflowX and
					// overflowY are set to the same value
					opt.overflow = [ this.style.overflow, this.style.overflowX, this.style.overflowY ];

					// Set display property to inline-block for height/width
					// animations on inline elements that are having width/height
					// animated
					if ( jQuery.css( this, "display" ) === "inline" &&
							jQuery.css( this, "float" ) === "none" ) {
						if ( !jQuery.support.inlineBlockNeedsLayout ) {
							this.style.display = "inline-block";

						} else {
							var display = defaultDisplay(this.nodeName);

							// inline-level elements accept inline-block;
							// block-level elements need to be inline with layout
							if ( display === "inline" ) {
								this.style.display = "inline-block";

							} else {
								this.style.display = "inline";
								this.style.zoom = 1;
							}
						}
					}
				}

				if ( jQuery.isArray( prop[p] ) ) {
					// Create (if needed) and add to specialEasing
					(opt.specialEasing = opt.specialEasing || {})[p] = prop[p][1];
					prop[p] = prop[p][0];
				}
			}

			if ( opt.overflow != null ) {
				this.style.overflow = "hidden";
			}

			opt.curAnim = jQuery.extend({}, prop);

			jQuery.each( prop, function( name, val ) {
				var e = new jQuery.fx( self, opt, name );

				if ( rfxtypes.test(val) ) {
					e[ val === "toggle" ? hidden ? "show" : "hide" : val ]( prop );

				} else {
					var parts = rfxnum.exec(val),
						start = e.cur();

					if ( parts ) {
						var end = parseFloat( parts[2] ),
							unit = parts[3] || ( jQuery.cssNumber[ name ] ? "" : "px" );

						// We need to compute starting value
						if ( unit !== "px" ) {
							jQuery.style( self, name, (end || 1) + unit);
							start = ((end || 1) / e.cur()) * start;
							jQuery.style( self, name, start + unit);
						}

						// If a +=/-= token was provided, we're doing a relative animation
						if ( parts[1] ) {
							end = ((parts[1] === "-=" ? -1 : 1) * end) + start;
						}

						e.custom( start, end, unit );

					} else {
						e.custom( start, val, "" );
					}
				}
			});

			// For JS strict compliance
			return true;
		});
	},

	stop: function( clearQueue, gotoEnd ) {
		var timers = jQuery.timers;

		if ( clearQueue ) {
			this.queue([]);
		}

		this.each(function() {
			// go in reverse order so anything added to the queue during the loop is ignored
			for ( var i = timers.length - 1; i >= 0; i-- ) {
				if ( timers[i].elem === this ) {
					if (gotoEnd) {
						// force the next step to be the last
						timers[i](true);
					}

					timers.splice(i, 1);
				}
			}
		});

		// start the next in the queue if the last step wasn't forced
		if ( !gotoEnd ) {
			this.dequeue();
		}

		return this;
	}

});

function genFx( type, num ) {
	var obj = {};

	jQuery.each( fxAttrs.concat.apply([], fxAttrs.slice(0,num)), function() {
		obj[ this ] = type;
	});

	return obj;
}

// Generate shortcuts for custom animations
jQuery.each({
	slideDown: genFx("show", 1),
	slideUp: genFx("hide", 1),
	slideToggle: genFx("toggle", 1),
	fadeIn: { opacity: "show" },
	fadeOut: { opacity: "hide" },
	fadeToggle: { opacity: "toggle" }
}, function( name, props ) {
	jQuery.fn[ name ] = function( speed, easing, callback ) {
		return this.animate( props, speed, easing, callback );
	};
});

jQuery.extend({
	speed: function( speed, easing, fn ) {
		var opt = speed && typeof speed === "object" ? jQuery.extend({}, speed) : {
			complete: fn || !fn && easing ||
				jQuery.isFunction( speed ) && speed,
			duration: speed,
			easing: fn && easing || easing && !jQuery.isFunction(easing) && easing
		};

		opt.duration = jQuery.fx.off ? 0 : typeof opt.duration === "number" ? opt.duration :
			opt.duration in jQuery.fx.speeds ? jQuery.fx.speeds[opt.duration] : jQuery.fx.speeds._default;

		// Queueing
		opt.old = opt.complete;
		opt.complete = function() {
			if ( opt.queue !== false ) {
				jQuery(this).dequeue();
			}
			if ( jQuery.isFunction( opt.old ) ) {
				opt.old.call( this );
			}
		};

		return opt;
	},

	easing: {
		linear: function( p, n, firstNum, diff ) {
			return firstNum + diff * p;
		},
		swing: function( p, n, firstNum, diff ) {
			return ((-Math.cos(p*Math.PI)/2) + 0.5) * diff + firstNum;
		}
	},

	timers: [],

	fx: function( elem, options, prop ) {
		this.options = options;
		this.elem = elem;
		this.prop = prop;

		if ( !options.orig ) {
			options.orig = {};
		}
	}

});

jQuery.fx.prototype = {
	// Simple function for setting a style value
	update: function() {
		if ( this.options.step ) {
			this.options.step.call( this.elem, this.now, this );
		}

		(jQuery.fx.step[this.prop] || jQuery.fx.step._default)( this );
	},

	// Get the current size
	cur: function() {
		if ( this.elem[this.prop] != null && (!this.elem.style || this.elem.style[this.prop] == null) ) {
			return this.elem[ this.prop ];
		}

		var parsed,
			r = jQuery.css( this.elem, this.prop );
		// Empty strings, null, undefined and "auto" are converted to 0,
		// complex values such as "rotate(1rad)" are returned as is,
		// simple values such as "10px" are parsed to Float.
		return isNaN( parsed = parseFloat( r ) ) ? !r || r === "auto" ? 0 : r : parsed;
	},

	// Start an animation from one number to another
	custom: function( from, to, unit ) {
		var self = this,
			fx = jQuery.fx;

		this.startTime = jQuery.now();
		this.start = from;
		this.end = to;
		this.unit = unit || this.unit || ( jQuery.cssNumber[ this.prop ] ? "" : "px" );
		this.now = this.start;
		this.pos = this.state = 0;

		function t( gotoEnd ) {
			return self.step(gotoEnd);
		}

		t.elem = this.elem;

		if ( t() && jQuery.timers.push(t) && !timerId ) {
			timerId = setInterval(fx.tick, fx.interval);
		}
	},

	// Simple 'show' function
	show: function() {
		// Remember where we started, so that we can go back to it later
		this.options.orig[this.prop] = jQuery.style( this.elem, this.prop );
		this.options.show = true;

		// Begin the animation
		// Make sure that we start at a small width/height to avoid any
		// flash of content
		this.custom(this.prop === "width" || this.prop === "height" ? 1 : 0, this.cur());

		// Start by showing the element
		jQuery( this.elem ).show();
	},

	// Simple 'hide' function
	hide: function() {
		// Remember where we started, so that we can go back to it later
		this.options.orig[this.prop] = jQuery.style( this.elem, this.prop );
		this.options.hide = true;

		// Begin the animation
		this.custom(this.cur(), 0);
	},

	// Each step of an animation
	step: function( gotoEnd ) {
		var t = jQuery.now(), done = true;

		if ( gotoEnd || t >= this.options.duration + this.startTime ) {
			this.now = this.end;
			this.pos = this.state = 1;
			this.update();

			this.options.curAnim[ this.prop ] = true;

			for ( var i in this.options.curAnim ) {
				if ( this.options.curAnim[i] !== true ) {
					done = false;
				}
			}

			if ( done ) {
				// Reset the overflow
				if ( this.options.overflow != null && !jQuery.support.shrinkWrapBlocks ) {
					var elem = this.elem,
						options = this.options;

					jQuery.each( [ "", "X", "Y" ], function (index, value) {
						elem.style[ "overflow" + value ] = options.overflow[index];
					} );
				}

				// Hide the element if the "hide" operation was done
				if ( this.options.hide ) {
					jQuery(this.elem).hide();
				}

				// Reset the properties, if the item has been hidden or shown
				if ( this.options.hide || this.options.show ) {
					for ( var p in this.options.curAnim ) {
						jQuery.style( this.elem, p, this.options.orig[p] );
					}
				}

				// Execute the complete function
				this.options.complete.call( this.elem );
			}

			return false;

		} else {
			var n = t - this.startTime;
			this.state = n / this.options.duration;

			// Perform the easing function, defaults to swing
			var specialEasing = this.options.specialEasing && this.options.specialEasing[this.prop];
			var defaultEasing = this.options.easing || (jQuery.easing.swing ? "swing" : "linear");
			this.pos = jQuery.easing[specialEasing || defaultEasing](this.state, n, 0, 1, this.options.duration);
			this.now = this.start + ((this.end - this.start) * this.pos);

			// Perform the next step of the animation
			this.update();
		}

		return true;
	}
};

jQuery.extend( jQuery.fx, {
	tick: function() {
		var timers = jQuery.timers;

		for ( var i = 0; i < timers.length; i++ ) {
			if ( !timers[i]() ) {
				timers.splice(i--, 1);
			}
		}

		if ( !timers.length ) {
			jQuery.fx.stop();
		}
	},

	interval: 13,

	stop: function() {
		clearInterval( timerId );
		timerId = null;
	},

	speeds: {
		slow: 600,
		fast: 200,
		// Default speed
		_default: 400
	},

	step: {
		opacity: function( fx ) {
			jQuery.style( fx.elem, "opacity", fx.now );
		},

		_default: function( fx ) {
			if ( fx.elem.style && fx.elem.style[ fx.prop ] != null ) {
				fx.elem.style[ fx.prop ] = (fx.prop === "width" || fx.prop === "height" ? Math.max(0, fx.now) : fx.now) + fx.unit;
			} else {
				fx.elem[ fx.prop ] = fx.now;
			}
		}
	}
});

if ( jQuery.expr && jQuery.expr.filters ) {
	jQuery.expr.filters.animated = function( elem ) {
		return jQuery.grep(jQuery.timers, function( fn ) {
			return elem === fn.elem;
		}).length;
	};
}

function defaultDisplay( nodeName ) {
	if ( !elemdisplay[ nodeName ] ) {
		var elem = jQuery("<" + nodeName + ">").appendTo("body"),
			display = elem.css("display");

		elem.remove();

		if ( display === "none" || display === "" ) {
			display = "block";
		}

		elemdisplay[ nodeName ] = display;
	}

	return elemdisplay[ nodeName ];
}




var rtable = /^t(?:able|d|h)$/i,
	rroot = /^(?:body|html)$/i;

if ( "getBoundingClientRect" in document.documentElement ) {
	jQuery.fn.offset = function( options ) {
		var elem = this[0], box;

		if ( options ) {
			return this.each(function( i ) {
				jQuery.offset.setOffset( this, options, i );
			});
		}

		if ( !elem || !elem.ownerDocument ) {
			return null;
		}

		if ( elem === elem.ownerDocument.body ) {
			return jQuery.offset.bodyOffset( elem );
		}

		try {
			box = elem.getBoundingClientRect();
		} catch(e) {}

		var doc = elem.ownerDocument,
			docElem = doc.documentElement;

		// Make sure we're not dealing with a disconnected DOM node
		if ( !box || !jQuery.contains( docElem, elem ) ) {
			return box ? { top: box.top, left: box.left } : { top: 0, left: 0 };
		}

		var body = doc.body,
			win = getWindow(doc),
			clientTop  = docElem.clientTop  || body.clientTop  || 0,
			clientLeft = docElem.clientLeft || body.clientLeft || 0,
			scrollTop  = (win.pageYOffset || jQuery.support.boxModel && docElem.scrollTop  || body.scrollTop ),
			scrollLeft = (win.pageXOffset || jQuery.support.boxModel && docElem.scrollLeft || body.scrollLeft),
			top  = box.top  + scrollTop  - clientTop,
			left = box.left + scrollLeft - clientLeft;

		return { top: top, left: left };
	};

} else {
	jQuery.fn.offset = function( options ) {
		var elem = this[0];

		if ( options ) {
			return this.each(function( i ) {
				jQuery.offset.setOffset( this, options, i );
			});
		}

		if ( !elem || !elem.ownerDocument ) {
			return null;
		}

		if ( elem === elem.ownerDocument.body ) {
			return jQuery.offset.bodyOffset( elem );
		}

		jQuery.offset.initialize();

		var computedStyle,
			offsetParent = elem.offsetParent,
			prevOffsetParent = elem,
			doc = elem.ownerDocument,
			docElem = doc.documentElement,
			body = doc.body,
			defaultView = doc.defaultView,
			prevComputedStyle = defaultView ? defaultView.getComputedStyle( elem, null ) : elem.currentStyle,
			top = elem.offsetTop,
			left = elem.offsetLeft;

		while ( (elem = elem.parentNode) && elem !== body && elem !== docElem ) {
			if ( jQuery.offset.supportsFixedPosition && prevComputedStyle.position === "fixed" ) {
				break;
			}

			computedStyle = defaultView ? defaultView.getComputedStyle(elem, null) : elem.currentStyle;
			top  -= elem.scrollTop;
			left -= elem.scrollLeft;

			if ( elem === offsetParent ) {
				top  += elem.offsetTop;
				left += elem.offsetLeft;

				if ( jQuery.offset.doesNotAddBorder && !(jQuery.offset.doesAddBorderForTableAndCells && rtable.test(elem.nodeName)) ) {
					top  += parseFloat( computedStyle.borderTopWidth  ) || 0;
					left += parseFloat( computedStyle.borderLeftWidth ) || 0;
				}

				prevOffsetParent = offsetParent;
				offsetParent = elem.offsetParent;
			}

			if ( jQuery.offset.subtractsBorderForOverflowNotVisible && computedStyle.overflow !== "visible" ) {
				top  += parseFloat( computedStyle.borderTopWidth  ) || 0;
				left += parseFloat( computedStyle.borderLeftWidth ) || 0;
			}

			prevComputedStyle = computedStyle;
		}

		if ( prevComputedStyle.position === "relative" || prevComputedStyle.position === "static" ) {
			top  += body.offsetTop;
			left += body.offsetLeft;
		}

		if ( jQuery.offset.supportsFixedPosition && prevComputedStyle.position === "fixed" ) {
			top  += Math.max( docElem.scrollTop, body.scrollTop );
			left += Math.max( docElem.scrollLeft, body.scrollLeft );
		}

		return { top: top, left: left };
	};
}

jQuery.offset = {
	initialize: function() {
		var body = document.body, container = document.createElement("div"), innerDiv, checkDiv, table, td, bodyMarginTop = parseFloat( jQuery.css(body, "marginTop") ) || 0,
			html = "<div style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;'><div></div></div><table style='position:absolute;top:0;left:0;margin:0;border:5px solid #000;padding:0;width:1px;height:1px;' cellpadding='0' cellspacing='0'><tr><td></td></tr></table>";

		jQuery.extend( container.style, { position: "absolute", top: 0, left: 0, margin: 0, border: 0, width: "1px", height: "1px", visibility: "hidden" } );

		container.innerHTML = html;
		body.insertBefore( container, body.firstChild );
		innerDiv = container.firstChild;
		checkDiv = innerDiv.firstChild;
		td = innerDiv.nextSibling.firstChild.firstChild;

		this.doesNotAddBorder = (checkDiv.offsetTop !== 5);
		this.doesAddBorderForTableAndCells = (td.offsetTop === 5);

		checkDiv.style.position = "fixed";
		checkDiv.style.top = "20px";

		// safari subtracts parent border width here which is 5px
		this.supportsFixedPosition = (checkDiv.offsetTop === 20 || checkDiv.offsetTop === 15);
		checkDiv.style.position = checkDiv.style.top = "";

		innerDiv.style.overflow = "hidden";
		innerDiv.style.position = "relative";

		this.subtractsBorderForOverflowNotVisible = (checkDiv.offsetTop === -5);

		this.doesNotIncludeMarginInBodyOffset = (body.offsetTop !== bodyMarginTop);

		body.removeChild( container );
		body = container = innerDiv = checkDiv = table = td = null;
		jQuery.offset.initialize = jQuery.noop;
	},

	bodyOffset: function( body ) {
		var top = body.offsetTop,
			left = body.offsetLeft;

		jQuery.offset.initialize();

		if ( jQuery.offset.doesNotIncludeMarginInBodyOffset ) {
			top  += parseFloat( jQuery.css(body, "marginTop") ) || 0;
			left += parseFloat( jQuery.css(body, "marginLeft") ) || 0;
		}

		return { top: top, left: left };
	},

	setOffset: function( elem, options, i ) {
		var position = jQuery.css( elem, "position" );

		// set position first, in-case top/left are set even on static elem
		if ( position === "static" ) {
			elem.style.position = "relative";
		}

		var curElem = jQuery( elem ),
			curOffset = curElem.offset(),
			curCSSTop = jQuery.css( elem, "top" ),
			curCSSLeft = jQuery.css( elem, "left" ),
			calculatePosition = (position === "absolute" && jQuery.inArray('auto', [curCSSTop, curCSSLeft]) > -1),
			props = {}, curPosition = {}, curTop, curLeft;

		// need to be able to calculate position if either top or left is auto and position is absolute
		if ( calculatePosition ) {
			curPosition = curElem.position();
		}

		curTop  = calculatePosition ? curPosition.top  : parseInt( curCSSTop,  10 ) || 0;
		curLeft = calculatePosition ? curPosition.left : parseInt( curCSSLeft, 10 ) || 0;

		if ( jQuery.isFunction( options ) ) {
			options = options.call( elem, i, curOffset );
		}

		if (options.top != null) {
			props.top = (options.top - curOffset.top) + curTop;
		}
		if (options.left != null) {
			props.left = (options.left - curOffset.left) + curLeft;
		}

		if ( "using" in options ) {
			options.using.call( elem, props );
		} else {
			curElem.css( props );
		}
	}
};


jQuery.fn.extend({
	position: function() {
		if ( !this[0] ) {
			return null;
		}

		var elem = this[0],

		// Get *real* offsetParent
		offsetParent = this.offsetParent(),

		// Get correct offsets
		offset       = this.offset(),
		parentOffset = rroot.test(offsetParent[0].nodeName) ? { top: 0, left: 0 } : offsetParent.offset();

		// Subtract element margins
		// note: when an element has margin: auto the offsetLeft and marginLeft
		// are the same in Safari causing offset.left to incorrectly be 0
		offset.top  -= parseFloat( jQuery.css(elem, "marginTop") ) || 0;
		offset.left -= parseFloat( jQuery.css(elem, "marginLeft") ) || 0;

		// Add offsetParent borders
		parentOffset.top  += parseFloat( jQuery.css(offsetParent[0], "borderTopWidth") ) || 0;
		parentOffset.left += parseFloat( jQuery.css(offsetParent[0], "borderLeftWidth") ) || 0;

		// Subtract the two offsets
		return {
			top:  offset.top  - parentOffset.top,
			left: offset.left - parentOffset.left
		};
	},

	offsetParent: function() {
		return this.map(function() {
			var offsetParent = this.offsetParent || document.body;
			while ( offsetParent && (!rroot.test(offsetParent.nodeName) && jQuery.css(offsetParent, "position") === "static") ) {
				offsetParent = offsetParent.offsetParent;
			}
			return offsetParent;
		});
	}
});


// Create scrollLeft and scrollTop methods
jQuery.each( ["Left", "Top"], function( i, name ) {
	var method = "scroll" + name;

	jQuery.fn[ method ] = function(val) {
		var elem = this[0], win;

		if ( !elem ) {
			return null;
		}

		if ( val !== undefined ) {
			// Set the scroll offset
			return this.each(function() {
				win = getWindow( this );

				if ( win ) {
					win.scrollTo(
						!i ? val : jQuery(win).scrollLeft(),
						i ? val : jQuery(win).scrollTop()
					);

				} else {
					this[ method ] = val;
				}
			});
		} else {
			win = getWindow( elem );

			// Return the scroll offset
			return win ? ("pageXOffset" in win) ? win[ i ? "pageYOffset" : "pageXOffset" ] :
				jQuery.support.boxModel && win.document.documentElement[ method ] ||
					win.document.body[ method ] :
				elem[ method ];
		}
	};
});

function getWindow( elem ) {
	return jQuery.isWindow( elem ) ?
		elem :
		elem.nodeType === 9 ?
			elem.defaultView || elem.parentWindow :
			false;
}




// Create innerHeight, innerWidth, outerHeight and outerWidth methods
jQuery.each([ "Height", "Width" ], function( i, name ) {

	var type = name.toLowerCase();

	// innerHeight and innerWidth
	jQuery.fn["inner" + name] = function() {
		return this[0] ?
			parseFloat( jQuery.css( this[0], type, "padding" ) ) :
			null;
	};

	// outerHeight and outerWidth
	jQuery.fn["outer" + name] = function( margin ) {
		return this[0] ?
			parseFloat( jQuery.css( this[0], type, margin ? "margin" : "border" ) ) :
			null;
	};

	jQuery.fn[ type ] = function( size ) {
		// Get window width or height
		var elem = this[0];
		if ( !elem ) {
			return size == null ? null : this;
		}

		if ( jQuery.isFunction( size ) ) {
			return this.each(function( i ) {
				var self = jQuery( this );
				self[ type ]( size.call( this, i, self[ type ]() ) );
			});
		}

		if ( jQuery.isWindow( elem ) ) {
			// Everyone else use document.documentElement or document.body depending on Quirks vs Standards mode
			// 3rd condition allows Nokia support, as it supports the docElem prop but not CSS1Compat
			var docElemProp = elem.document.documentElement[ "client" + name ];
			return elem.document.compatMode === "CSS1Compat" && docElemProp ||
				elem.document.body[ "client" + name ] || docElemProp;

		// Get document width or height
		} else if ( elem.nodeType === 9 ) {
			// Either scroll[Width/Height] or offset[Width/Height], whichever is greater
			return Math.max(
				elem.documentElement["client" + name],
				elem.body["scroll" + name], elem.documentElement["scroll" + name],
				elem.body["offset" + name], elem.documentElement["offset" + name]
			);

		// Get or set width or height on the element
		} else if ( size === undefined ) {
			var orig = jQuery.css( elem, type ),
				ret = parseFloat( orig );

			return jQuery.isNaN( ret ) ? orig : ret;

		// Set the width or height on the element (default to pixels if value is unitless)
		} else {
			return this.css( type, typeof size === "string" ? size : size + "px" );
		}
	};

});


window.jQuery = window.$ = jQuery;
})(window);
/**
 * Dual licensed under the Apache License 2.0 and the MIT license.
 * $Revision$ $Date: 2010-10-18 22:29:51 +0200 (Mon, 18 Oct 2010) $
 */
(function($)
{
    // Remap cometd JSON functions to jquery JSON functions
    org.cometd.JSON.toJSON = JSON.stringify;
    org.cometd.JSON.fromJSON = JSON.parse;

    function _setHeaders(xhr, headers)
    {
        if (headers)
        {
            for (var headerName in headers)
            {
                if (headerName.toLowerCase() === 'content-type')
                {
                    continue;
                }
                xhr.setRequestHeader(headerName, headers[headerName]);
            }
        }
    }

    // The default cometd instance
    $.cometd = new org.cometd.Cometd();

    // Remap toolkit-specific transport calls
    $.cometd.LongPollingTransport = function()
    {
        var _super = new org.cometd.LongPollingTransport();
        var that = org.cometd.Transport.derive(_super);

        that.xhrSend = function(packet)
        {
            return $.ajax({
                url: packet.url,
                async: packet.sync !== true,
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                data: packet.body,
                beforeSend: function(xhr)
                {
                    _setHeaders(xhr, packet.headers);
                    // Returning false will abort the XHR send
                    return true;
                },
                success: packet.onSuccess,
                error: function(xhr, reason, exception)
                {
                    packet.onError(reason, exception);
                }
            });
        };

        return that;
    };

    $.cometd.CallbackPollingTransport = function()
    {
        var _super = new org.cometd.CallbackPollingTransport();
        var that = org.cometd.Transport.derive(_super);

        that.jsonpSend = function(packet)
        {
            $.ajax({
                url: packet.url,
                async: packet.sync !== true,
                type: 'GET',
                dataType: 'jsonp',
                jsonp: 'jsonp',
                data: {
                    // In callback-polling, the content must be sent via the 'message' parameter
                    message: packet.body
                },
                beforeSend: function(xhr)
                {
                    _setHeaders(xhr, packet.headers);
                    // Returning false will abort the XHR send
                    return true;
                },
                success: packet.onSuccess,
                error: function(xhr, reason, exception)
                {
                    packet.onError(reason, exception);
                }
            });
        };

        return that;
    };

    if (window.WebSocket)
    {
        $.cometd.registerTransport('websocket', new org.cometd.WebSocketTransport());
    }
    $.cometd.registerTransport('long-polling', new $.cometd.LongPollingTransport());
    $.cometd.registerTransport('callback-polling', new $.cometd.CallbackPollingTransport());

})(jQuery);

/*
 * jQuery Templating Plugin
 * Copyright 2010, John Resig
 * Dual licensed under the MIT or GPL Version 2 licenses.
 */
(function( jQuery, undefined ){
	var oldManip = jQuery.fn.domManip, tmplItmAtt = "_tmplitem", htmlExpr = /^[^<]*(<[\w\W]+>)[^>]*$|\{\{\! /,
		newTmplItems = {}, wrappedItems = {}, appendToTmplItems, topTmplItem = { key: 0, data: {} }, itemKey = 0, cloneIndex = 0, stack = [];

	function newTmplItem( options, parentItem, fn, data ) {
		// Returns a template item data structure for a new rendered instance of a template (a 'template item').
		// The content field is a hierarchical array of strings and nested items (to be
		// removed and replaced by nodes field of dom elements, once inserted in DOM).
		var newItem = {
			data: data || (parentItem ? parentItem.data : {}),
			_wrap: parentItem ? parentItem._wrap : null,
			tmpl: null,
			parent: parentItem || null,
			nodes: [],
			calls: tiCalls,
			nest: tiNest,
			wrap: tiWrap,
			html: tiHtml,
			update: tiUpdate
		};
		if ( options ) {
			jQuery.extend( newItem, options, { nodes: [], parent: parentItem } );
		}
		if ( fn ) {
			// Build the hierarchical content to be used during insertion into DOM
			newItem.tmpl = fn;
			newItem._ctnt = newItem._ctnt || newItem.tmpl( jQuery, newItem );
			newItem.key = ++itemKey;
			// Keep track of new template item, until it is stored as jQuery Data on DOM element
			(stack.length ? wrappedItems : newTmplItems)[itemKey] = newItem;
		}
		return newItem;
	}

	// Override appendTo etc., in order to provide support for targeting multiple elements. (This code would disappear if integrated in jquery core).
	jQuery.each({
		appendTo: "append",
		prependTo: "prepend",
		insertBefore: "before",
		insertAfter: "after",
		replaceAll: "replaceWith"
	}, function( name, original ) {
		jQuery.fn[ name ] = function( selector ) {
			var ret = [], insert = jQuery( selector ), elems, i, l, tmplItems,
				parent = this.length === 1 && this[0].parentNode;

			appendToTmplItems = newTmplItems || {};
			if ( parent && parent.nodeType === 11 && parent.childNodes.length === 1 && insert.length === 1 ) {
				insert[ original ]( this[0] );
				ret = this;
			} else {
				for ( i = 0, l = insert.length; i < l; i++ ) {
					cloneIndex = i;
					elems = (i > 0 ? this.clone(true) : this).get();
					jQuery.fn[ original ].apply( jQuery(insert[i]), elems );
					ret = ret.concat( elems );
				}
				cloneIndex = 0;
				ret = this.pushStack( ret, name, insert.selector );
			}
			tmplItems = appendToTmplItems;
			appendToTmplItems = null;
			jQuery.tmpl.complete( tmplItems );
			return ret;
		};
	});

	jQuery.fn.extend({
		// Use first wrapped element as template markup.
		// Return wrapped set of template items, obtained by rendering template against data.
		tmpl: function( data, options, parentItem ) {
			return jQuery.tmpl( this[0], data, options, parentItem );
		},

		// Find which rendered template item the first wrapped DOM element belongs to
		tmplItem: function() {
			return jQuery.tmplItem( this[0] );
		},

		// Consider the first wrapped element as a template declaration, and get the compiled template or store it as a named template.
		template: function( name ) {
			return jQuery.template( name, this[0] );
		},

		domManip: function( args, table, callback, options ) {
			// This appears to be a bug in the appendTo, etc. implementation
			// it should be doing .call() instead of .apply(). See #6227
			if ( args[0] && args[0].nodeType ) {
				var dmArgs = jQuery.makeArray( arguments ), argsLength = args.length, i = 0, tmplItem;
				while ( i < argsLength && !(tmplItem = jQuery.data( args[i++], "tmplItem" ))) {}
				if ( argsLength > 1 ) {
					dmArgs[0] = [jQuery.makeArray( args )];
				}
				if ( tmplItem && cloneIndex ) {
					dmArgs[2] = function( fragClone ) {
						// Handler called by oldManip when rendered template has been inserted into DOM.
						jQuery.tmpl.afterManip( this, fragClone, callback );
					};
				}
				oldManip.apply( this, dmArgs );
			} else {
				oldManip.apply( this, arguments );
			}
			cloneIndex = 0;
			if ( !appendToTmplItems ) {
				jQuery.tmpl.complete( newTmplItems );
			}
			return this;
		}
	});

	jQuery.extend({
		// Return wrapped set of template items, obtained by rendering template against data.
		tmpl: function( tmpl, data, options, parentItem ) {
			var ret, topLevel = !parentItem;
			if ( topLevel ) {
				// This is a top-level tmpl call (not from a nested template using {{tmpl}})
				parentItem = topTmplItem;
				tmpl = jQuery.template[tmpl] || jQuery.template( null, tmpl );
				wrappedItems = {}; // Any wrapped items will be rebuilt, since this is top level
			} else if ( !tmpl ) {
				// The template item is already associated with DOM - this is a refresh.
				// Re-evaluate rendered template for the parentItem
				tmpl = parentItem.tmpl;
				newTmplItems[parentItem.key] = parentItem;
				parentItem.nodes = [];
				if ( parentItem.wrapped ) {
					updateWrapped( parentItem, parentItem.wrapped );
				}
				// Rebuild, without creating a new template item
				return jQuery( build( parentItem, null, parentItem.tmpl( jQuery, parentItem ) ));
			}
			if ( !tmpl ) {
				return []; // Could throw...
			}
			if ( typeof data === "function" ) {
				data = data.call( parentItem || {} );
			}
			if ( options && options.wrapped ) {
				updateWrapped( options, options.wrapped );
			}
			ret = jQuery.isArray( data ) ? 
				jQuery.map( data, function( dataItem ) {
					return dataItem ? newTmplItem( options, parentItem, tmpl, dataItem ) : null;
				}) :
				[ newTmplItem( options, parentItem, tmpl, data ) ];
			return topLevel ? jQuery( build( parentItem, null, ret ) ) : ret;
		},

		// Return rendered template item for an element.
		tmplItem: function( elem ) {
			var tmplItem;
			if ( elem instanceof jQuery ) {
				elem = elem[0];
			}
			while ( elem && elem.nodeType === 1 && !(tmplItem = jQuery.data( elem, "tmplItem" )) && (elem = elem.parentNode) ) {}
			return tmplItem || topTmplItem;
		},

		// Set:
		// Use $.template( name, tmpl ) to cache a named template,
		// where tmpl is a template string, a script element or a jQuery instance wrapping a script element, etc.
		// Use $( "selector" ).template( name ) to provide access by name to a script block template declaration.

		// Get:
		// Use $.template( name ) to access a cached template.
		// Also $( selectorToScriptBlock ).template(), or $.template( null, templateString )
		// will return the compiled template, without adding a name reference.
		// If templateString includes at least one HTML tag, $.template( templateString ) is equivalent
		// to $.template( null, templateString )
		template: function( name, tmpl ) {
			if (tmpl) {
				// Compile template and associate with name
				if ( typeof tmpl === "string" ) {
					// This is an HTML string being passed directly in.
					tmpl = buildTmplFn( tmpl )
				} else if ( tmpl instanceof jQuery ) {
					tmpl = tmpl[0] || {};
				}
				if ( tmpl.nodeType ) {
					// If this is a template block, use cached copy, or generate tmpl function and cache.
					tmpl = jQuery.data( tmpl, "tmpl" ) || jQuery.data( tmpl, "tmpl", buildTmplFn( tmpl.innerHTML ));
				}
				return typeof name === "string" ? (jQuery.template[name] = tmpl) : tmpl;
			}
			// Return named compiled template
			return name ? (typeof name !== "string" ? jQuery.template( null, name ): 
				(jQuery.template[name] || 
					// If not in map, treat as a selector. (If integrated with core, use quickExpr.exec) 
					jQuery.template( null, htmlExpr.test( name ) ? name : jQuery( name )))) : null; 
		},

		encode: function( text ) {
			// Do HTML encoding replacing < > & and ' and " by corresponding entities.
			return ("" + text).split("<").join("&lt;").split(">").join("&gt;").split('"').join("&#34;").split("'").join("&#39;");
		}
	});

	jQuery.extend( jQuery.tmpl, {
		tag: {
			"tmpl": {
				_default: { $2: "null" },
				open: "if($notnull_1){_=_.concat($item.nest($1,$2));}"
				// tmpl target parameter can be of type function, so use $1, not $1a (so not auto detection of functions)
				// This means that {{tmpl foo}} treats foo as a template (which IS a function). 
				// Explicit parens can be used if foo is a function that returns a template: {{tmpl foo()}}.
			},
			"wrap": {
				_default: { $2: "null" },
				open: "$item.calls(_,$1,$2);_=[];",
				close: "call=$item.calls();_=call._.concat($item.wrap(call,_));"
			},
			"each": {
				_default: { $2: "$index, $value" },
				open: "if($notnull_1){$.each($1a,function($2){with(this){",
				close: "}});}"
			},
			"if": {
				open: "if(($notnull_1) && $1a){",
				close: "}"
			},
			"else": {
				_default: { $1: "true" },
				open: "}else if(($notnull_1) && $1a){"
			},
			"html": {
				// Unecoded expression evaluation. 
				open: "if($notnull_1){_.push($1a);}"
			},
			"=": {
				// Encoded expression evaluation. Abbreviated form is ${}.
				_default: { $1: "$data" },
				open: "if($notnull_1){_.push($.encode($1a));}"
			},
			"!": {
				// Comment tag. Skipped by parser
				open: ""
			}
		},

		// This stub can be overridden, e.g. in jquery.tmplPlus for providing rendered events
		complete: function( items ) {
			newTmplItems = {};
		},

		// Call this from code which overrides domManip, or equivalent
		// Manage cloning/storing template items etc.
		afterManip: function afterManip( elem, fragClone, callback ) {
			// Provides cloned fragment ready for fixup prior to and after insertion into DOM
			var content = fragClone.nodeType === 11 ?
				jQuery.makeArray(fragClone.childNodes) :
				fragClone.nodeType === 1 ? [fragClone] : [];

			// Return fragment to original caller (e.g. append) for DOM insertion
			callback.call( elem, fragClone );

			// Fragment has been inserted:- Add inserted nodes to tmplItem data structure. Replace inserted element annotations by jQuery.data.
			storeTmplItems( content );
			cloneIndex++;
		}
	});

	//========================== Private helper functions, used by code above ==========================

	function build( tmplItem, nested, content ) {
		// Convert hierarchical content into flat string array 
		// and finally return array of fragments ready for DOM insertion
		var frag, ret = content ? jQuery.map( content, function( item ) {
			return (typeof item === "string") ? 
				// Insert template item annotations, to be converted to jQuery.data( "tmplItem" ) when elems are inserted into DOM.
				(tmplItem.key ? item.replace( /(<\w+)(?=[\s>])(?![^>]*_tmplitem)([^>]*)/g, "$1 " + tmplItmAtt + "=\"" + tmplItem.key + "\" $2" ) : item) :
				// This is a child template item. Build nested template.
				build( item, tmplItem, item._ctnt );
		}) : 
		// If content is not defined, insert tmplItem directly. Not a template item. May be a string, or a string array, e.g. from {{html $item.html()}}. 
		tmplItem;
		if ( nested ) {
			return ret;
		}

		// top-level template
		ret = ret.join("");

		// Support templates which have initial or final text nodes, or consist only of text
		// Also support HTML entities within the HTML markup.
		ret.replace( /^\s*([^<\s][^<]*)?(<[\w\W]+>)([^>]*[^>\s])?\s*$/, function( all, before, middle, after) {
			frag = jQuery( middle ).get();

			storeTmplItems( frag );
			if ( before ) {
				frag = unencode( before ).concat(frag);
			}
			if ( after ) {
				frag = frag.concat(unencode( after ));
			}
		});
		return frag ? frag : unencode( ret );
	}

	function unencode( text ) {
		// Use createElement, since createTextNode will not render HTML entities correctly
		var el = document.createElement( "div" );
		el.innerHTML = text;
		return jQuery.makeArray(el.childNodes);
	}

	// Generate a reusable function that will serve to render a template against data
	function buildTmplFn( markup ) {
		return new Function("jQuery","$item",
			"var $=jQuery,call,_=[],$data=$item.data;" +

			// Introduce the data as local variables using with(){}
			"with($data){_.push('" +

			// Convert the template into pure JavaScript
			jQuery.trim(markup)
				.replace( /([\\'])/g, "\\$1" )
				.replace( /[\r\t\n]/g, " " )
				.replace( /\$\{([^\}]*)\}/g, "{{= $1}}" )
				.replace( /\{\{(\/?)(\w+|.)(?:\(((?:[^\}]|\}(?!\}))*?)?\))?(?:\s+(.*?)?)?(\(((?:[^\}]|\}(?!\}))*?)\))?\s*\}\}/g,
				function( all, slash, type, fnargs, target, parens, args ) {
					var tag = jQuery.tmpl.tag[ type ], def, expr, exprAutoFnDetect;
					if ( !tag ) {
						throw "Template command not found: " + type;
					}
					def = tag._default || [];
					if ( parens && !/\w$/.test(target)) {
						target += parens;
						parens = "";
					}
					if ( target ) {
						target = unescape( target ); 
						args = args ? ("," + unescape( args ) + ")") : (parens ? ")" : "");
						// Support for target being things like a.toLowerCase();
						// In that case don't call with template item as 'this' pointer. Just evaluate...
						expr = parens ? (target.indexOf(".") > -1 ? target + parens : ("(" + target + ").call($item" + args)) : target;
						exprAutoFnDetect = parens ? expr : "(typeof(" + target + ")==='function'?(" + target + ").call($item):(" + target + "))";
					} else {
						exprAutoFnDetect = expr = def.$1 || "null";
					}
					fnargs = unescape( fnargs );
					return "');" + 
						tag[ slash ? "close" : "open" ]
							.split( "$notnull_1" ).join( target ? "typeof(" + target + ")!=='undefined' && (" + target + ")!=null" : "true" )
							.split( "$1a" ).join( exprAutoFnDetect )
							.split( "$1" ).join( expr )
							.split( "$2" ).join( fnargs ?
								fnargs.replace( /\s*([^\(]+)\s*(\((.*?)\))?/g, function( all, name, parens, params ) {
									params = params ? ("," + params + ")") : (parens ? ")" : "");
									return params ? ("(" + name + ").call($item" + params) : all;
								})
								: (def.$2||"")
							) +
						"_.push('";
				}) +
			"');}return _;"
		);
	}
	function updateWrapped( options, wrapped ) {
		// Build the wrapped content. 
		options._wrap = build( options, true, 
			// Suport imperative scenario in which options.wrapped can be set to a selector or an HTML string.
			jQuery.isArray( wrapped ) ? wrapped : [htmlExpr.test( wrapped ) ? wrapped : jQuery( wrapped ).html()]
		).join("");
	}

	function unescape( args ) {
		return args ? args.replace( /\\'/g, "'").replace(/\\\\/g, "\\" ) : null;
	}
	function outerHtml( elem ) {
		var div = document.createElement("div");
		div.appendChild( elem.cloneNode(true) );
		return div.innerHTML;
	}

	// Store template items in jQuery.data(), ensuring a unique tmplItem data data structure for each rendered template instance.
	function storeTmplItems( content ) {
		var keySuffix = "_" + cloneIndex, elem, elems, newClonedItems = {}, i, l, m;
		for ( i = 0, l = content.length; i < l; i++ ) {
			if ( (elem = content[i]).nodeType !== 1 ) {
				continue;
			}
			elems = elem.getElementsByTagName("*");
			for ( m = elems.length - 1; m >= 0; m-- ) {
				processItemKey( elems[m] );
			}
			processItemKey( elem );
		}
		function processItemKey( el ) {
			var pntKey, pntNode = el, pntItem, tmplItem, key;
			// Ensure that each rendered template inserted into the DOM has its own template item,
			if ( (key = el.getAttribute( tmplItmAtt ))) {
				while ( pntNode.parentNode && (pntNode = pntNode.parentNode).nodeType === 1 && !(pntKey = pntNode.getAttribute( tmplItmAtt ))) { }
				if ( pntKey !== key ) {
					// The next ancestor with a _tmplitem expando is on a different key than this one.
					// So this is a top-level element within this template item
					// Set pntNode to the key of the parentNode, or to 0 if pntNode.parentNode is null, or pntNode is a fragment.
					pntNode = pntNode.parentNode ? (pntNode.nodeType === 11 ? 0 : (pntNode.getAttribute( tmplItmAtt ) || 0)) : 0;
					if ( !(tmplItem = newTmplItems[key]) ) {
						// The item is for wrapped content, and was copied from the temporary parent wrappedItem.
						tmplItem = wrappedItems[key];
						tmplItem = newTmplItem( tmplItem, newTmplItems[pntNode]||wrappedItems[pntNode], null, true );
						tmplItem.key = ++itemKey;
						newTmplItems[itemKey] = tmplItem;
					}
					if ( cloneIndex ) {
						cloneTmplItem( key );
					}
				}
				el.removeAttribute( tmplItmAtt );
			} else if ( cloneIndex && (tmplItem = jQuery.data( el, "tmplItem" )) ) {
				// This was a rendered element, cloned during append or appendTo etc.
				// TmplItem stored in jQuery data has already been cloned in cloneCopyEvent. We must replace it with a fresh cloned tmplItem.
				cloneTmplItem( tmplItem.key );
				newTmplItems[tmplItem.key] = tmplItem;
				pntNode = jQuery.data( el.parentNode, "tmplItem" );
				pntNode = pntNode ? pntNode.key : 0;
			}
			if ( tmplItem ) {
				pntItem = tmplItem;
				// Find the template item of the parent element. 
				// (Using !=, not !==, since pntItem.key is number, and pntNode may be a string)
				while ( pntItem && pntItem.key != pntNode ) { 
					// Add this element as a top-level node for this rendered template item, as well as for any
					// ancestor items between this item and the item of its parent element
					pntItem.nodes.push( el );
					pntItem = pntItem.parent;
				}
				// Delete content built during rendering - reduce API surface area and memory use, and avoid exposing of stale data after rendering...
				delete tmplItem._ctnt;
				delete tmplItem._wrap;
				// Store template item as jQuery data on the element
				jQuery.data( el, "tmplItem", tmplItem );
			}
			function cloneTmplItem( key ) {
				key = key + keySuffix;
				tmplItem = newClonedItems[key] = 
					(newClonedItems[key] || newTmplItem( tmplItem, newTmplItems[tmplItem.parent.key + keySuffix] || tmplItem.parent, null, true ));
			}
		}
	}

	//---- Helper functions for template item ----

	function tiCalls( content, tmpl, data, options ) {
		if ( !content ) {
			return stack.pop();
		}
		stack.push({ _: content, tmpl: tmpl, item:this, data: data, options: options });
	}

	function tiNest( tmpl, data, options ) {
		// nested template, using {{tmpl}} tag
		return jQuery.tmpl( jQuery.template( tmpl ), data, options, this );
	}

	function tiWrap( call, wrapped ) {
		// nested template, using {{wrap}} tag
		var options = call.options || {};
		options.wrapped = wrapped;
		// Apply the template, which may incorporate wrapped content, 
		return jQuery.tmpl( jQuery.template( call.tmpl ), call.data, options, call.item );
	}

	function tiHtml( filter, textOnly ) {
		var wrapped = this._wrap;
		return jQuery.map(
			jQuery( jQuery.isArray( wrapped ) ? wrapped.join("") : wrapped ).filter( filter || "*" ),
			function(e) {
				return textOnly ?
					e.innerText || e.textContent :
					e.outerHTML || outerHtml(e);
			});
	}

	function tiUpdate() {
		var coll = this.nodes;
		jQuery.tmpl( null, null, null, this).insertBefore( coll[0] );
		jQuery( coll ).remove();
	}
})( jQuery );
// jquery.jsonp 1.1.0 (c)2009 Julian Aubourg | MIT License
// http://code.google.com/p/jquery-jsonp/
(function(d){var b=function(n){return n!==undefined&&n!==null},m=function(p,n,o){b(p)&&p.apply(n,o)},e=function(n){setTimeout(n,0)},f="",a="&",k="?",l="success",g="error",i=d("head"),h={},c={callback:"C",url:location.href},j=function(s){s=d.extend({},c,s);var r=s.beforeSend,A=0;s.abort=function(){A=1};if(b(r)&&(r(s,s)===false||A)){return s}var q=s.success,o=s.complete,v=s.error,C=s.dataFilter,G=s.callbackParameter,w=s.callback,D=s.cache,n=s.pageCache,t=s.url,I=s.data,x=s.timeout,z,H,F,E;t=b(t)?t:f;I=b(I)?((typeof I)=="string"?I:d.param(I)):f;b(G)&&(I+=(I==f?f:a)+escape(G)+"=?");!D&&!n&&(I+=(I==f?f:a)+"_"+(new Date()).getTime()+"=");z=t.split(k);if(I!=f){H=I.split(k);E=z.length-1;E&&(z[E]+=a+H.shift());z=z.concat(H)}F=z.length-2;F&&(z[F]+=w+z.pop());var p=z.join(k),B=function(J){b(C)&&(J=C.apply(s,[J]));m(q,s,[J,l]);m(o,s,[s,l])},y=function(J){m(v,s,[s,J]);m(o,s,[s,J])},u=h[p];if(n&&b(u)){e(function(){b(u.s)?B(u.s):y(g)});return s}e(function(){if(A){return}var J=d("<iframe />").appendTo(i),L=J[0],N=L.contentWindow||L.contentDocument,P=N.document,K,Q,R=function(S,T){n&&!b(T)&&(h[p]=f);K();y(b(T)?T:g)},M=function(T){N[T]=undefined;try{delete N[T]}catch(S){}},O=w=="E"?"X":"E";if(!b(P)){P=N;N=P.getParentNode()}P.open();N[w]=function(S){A=1;n&&(h[p]={s:S});e(function(){K();B(S)})};N[O]=function(S){(!S||S=="complete")&&!A++&&e(R)};s.abort=K=function(){clearTimeout(Q);P.open();M(O);M(w);P.write(f);P.close();J.remove()};P.write(['<html><head><script src="',p,'" onload="',O,'()" onreadystatechange="',O,'(this.readyState)"><\/script></head><body onload="',O,'()"></body></html>'].join(f));P.close();x>0&&(Q=setTimeout(function(){!A&&R(f,"timeout")},x))});return s};j.setup=function(n){d.extend(c,n)};d.jsonp=j})(jQuery);
/*!
 * jQuery UI 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI
 */
(function(c,j){function k(a){return!c(a).parents().andSelf().filter(function(){return c.curCSS(this,"visibility")==="hidden"||c.expr.filters.hidden(this)}).length}c.ui=c.ui||{};if(!c.ui.version){c.extend(c.ui,{version:"1.8.10",keyCode:{ALT:18,BACKSPACE:8,CAPS_LOCK:20,COMMA:188,COMMAND:91,COMMAND_LEFT:91,COMMAND_RIGHT:93,CONTROL:17,DELETE:46,DOWN:40,END:35,ENTER:13,ESCAPE:27,HOME:36,INSERT:45,LEFT:37,MENU:93,NUMPAD_ADD:107,NUMPAD_DECIMAL:110,NUMPAD_DIVIDE:111,NUMPAD_ENTER:108,NUMPAD_MULTIPLY:106,
NUMPAD_SUBTRACT:109,PAGE_DOWN:34,PAGE_UP:33,PERIOD:190,RIGHT:39,SHIFT:16,SPACE:32,TAB:9,UP:38,WINDOWS:91}});c.fn.extend({_focus:c.fn.focus,focus:function(a,b){return typeof a==="number"?this.each(function(){var d=this;setTimeout(function(){c(d).focus();b&&b.call(d)},a)}):this._focus.apply(this,arguments)},scrollParent:function(){var a;a=c.browser.msie&&/(static|relative)/.test(this.css("position"))||/absolute/.test(this.css("position"))?this.parents().filter(function(){return/(relative|absolute|fixed)/.test(c.curCSS(this,
"position",1))&&/(auto|scroll)/.test(c.curCSS(this,"overflow",1)+c.curCSS(this,"overflow-y",1)+c.curCSS(this,"overflow-x",1))}).eq(0):this.parents().filter(function(){return/(auto|scroll)/.test(c.curCSS(this,"overflow",1)+c.curCSS(this,"overflow-y",1)+c.curCSS(this,"overflow-x",1))}).eq(0);return/fixed/.test(this.css("position"))||!a.length?c(document):a},zIndex:function(a){if(a!==j)return this.css("zIndex",a);if(this.length){a=c(this[0]);for(var b;a.length&&a[0]!==document;){b=a.css("position");
if(b==="absolute"||b==="relative"||b==="fixed"){b=parseInt(a.css("zIndex"),10);if(!isNaN(b)&&b!==0)return b}a=a.parent()}}return 0},disableSelection:function(){return this.bind((c.support.selectstart?"selectstart":"mousedown")+".ui-disableSelection",function(a){a.preventDefault()})},enableSelection:function(){return this.unbind(".ui-disableSelection")}});c.each(["Width","Height"],function(a,b){function d(f,g,l,m){c.each(e,function(){g-=parseFloat(c.curCSS(f,"padding"+this,true))||0;if(l)g-=parseFloat(c.curCSS(f,
"border"+this+"Width",true))||0;if(m)g-=parseFloat(c.curCSS(f,"margin"+this,true))||0});return g}var e=b==="Width"?["Left","Right"]:["Top","Bottom"],h=b.toLowerCase(),i={innerWidth:c.fn.innerWidth,innerHeight:c.fn.innerHeight,outerWidth:c.fn.outerWidth,outerHeight:c.fn.outerHeight};c.fn["inner"+b]=function(f){if(f===j)return i["inner"+b].call(this);return this.each(function(){c(this).css(h,d(this,f)+"px")})};c.fn["outer"+b]=function(f,g){if(typeof f!=="number")return i["outer"+b].call(this,f);return this.each(function(){c(this).css(h,
d(this,f,true,g)+"px")})}});c.extend(c.expr[":"],{data:function(a,b,d){return!!c.data(a,d[3])},focusable:function(a){var b=a.nodeName.toLowerCase(),d=c.attr(a,"tabindex");if("area"===b){b=a.parentNode;d=b.name;if(!a.href||!d||b.nodeName.toLowerCase()!=="map")return false;a=c("img[usemap=#"+d+"]")[0];return!!a&&k(a)}return(/input|select|textarea|button|object/.test(b)?!a.disabled:"a"==b?a.href||!isNaN(d):!isNaN(d))&&k(a)},tabbable:function(a){var b=c.attr(a,"tabindex");return(isNaN(b)||b>=0)&&c(a).is(":focusable")}});
c(function(){var a=document.body,b=a.appendChild(b=document.createElement("div"));c.extend(b.style,{minHeight:"100px",height:"auto",padding:0,borderWidth:0});c.support.minHeight=b.offsetHeight===100;c.support.selectstart="onselectstart"in b;a.removeChild(b).style.display="none"});c.extend(c.ui,{plugin:{add:function(a,b,d){a=c.ui[a].prototype;for(var e in d){a.plugins[e]=a.plugins[e]||[];a.plugins[e].push([b,d[e]])}},call:function(a,b,d){if((b=a.plugins[b])&&a.element[0].parentNode)for(var e=0;e<b.length;e++)a.options[b[e][0]]&&
b[e][1].apply(a.element,d)}},contains:function(a,b){return document.compareDocumentPosition?a.compareDocumentPosition(b)&16:a!==b&&a.contains(b)},hasScroll:function(a,b){if(c(a).css("overflow")==="hidden")return false;b=b&&b==="left"?"scrollLeft":"scrollTop";var d=false;if(a[b]>0)return true;a[b]=1;d=a[b]>0;a[b]=0;return d},isOverAxis:function(a,b,d){return a>b&&a<b+d},isOver:function(a,b,d,e,h,i){return c.ui.isOverAxis(a,d,h)&&c.ui.isOverAxis(b,e,i)}})}})(jQuery);
;/*!
 * jQuery UI Widget 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Widget
 */
(function(b,j){if(b.cleanData){var k=b.cleanData;b.cleanData=function(a){for(var c=0,d;(d=a[c])!=null;c++)b(d).triggerHandler("remove");k(a)}}else{var l=b.fn.remove;b.fn.remove=function(a,c){return this.each(function(){if(!c)if(!a||b.filter(a,[this]).length)b("*",this).add([this]).each(function(){b(this).triggerHandler("remove")});return l.call(b(this),a,c)})}}b.widget=function(a,c,d){var e=a.split(".")[0],f;a=a.split(".")[1];f=e+"-"+a;if(!d){d=c;c=b.Widget}b.expr[":"][f]=function(h){return!!b.data(h,
a)};b[e]=b[e]||{};b[e][a]=function(h,g){arguments.length&&this._createWidget(h,g)};c=new c;c.options=b.extend(true,{},c.options);b[e][a].prototype=b.extend(true,c,{namespace:e,widgetName:a,widgetEventPrefix:b[e][a].prototype.widgetEventPrefix||a,widgetBaseClass:f},d);b.widget.bridge(a,b[e][a])};b.widget.bridge=function(a,c){b.fn[a]=function(d){var e=typeof d==="string",f=Array.prototype.slice.call(arguments,1),h=this;d=!e&&f.length?b.extend.apply(null,[true,d].concat(f)):d;if(e&&d.charAt(0)==="_")return h;
e?this.each(function(){var g=b.data(this,a),i=g&&b.isFunction(g[d])?g[d].apply(g,f):g;if(i!==g&&i!==j){h=i;return false}}):this.each(function(){var g=b.data(this,a);g?g.option(d||{})._init():b.data(this,a,new c(d,this))});return h}};b.Widget=function(a,c){arguments.length&&this._createWidget(a,c)};b.Widget.prototype={widgetName:"widget",widgetEventPrefix:"",options:{disabled:false},_createWidget:function(a,c){b.data(c,this.widgetName,this);this.element=b(c);this.options=b.extend(true,{},this.options,
this._getCreateOptions(),a);var d=this;this.element.bind("remove."+this.widgetName,function(){d.destroy()});this._create();this._trigger("create");this._init()},_getCreateOptions:function(){return b.metadata&&b.metadata.get(this.element[0])[this.widgetName]},_create:function(){},_init:function(){},destroy:function(){this.element.unbind("."+this.widgetName).removeData(this.widgetName);this.widget().unbind("."+this.widgetName).removeAttr("aria-disabled").removeClass(this.widgetBaseClass+"-disabled ui-state-disabled")},
widget:function(){return this.element},option:function(a,c){var d=a;if(arguments.length===0)return b.extend({},this.options);if(typeof a==="string"){if(c===j)return this.options[a];d={};d[a]=c}this._setOptions(d);return this},_setOptions:function(a){var c=this;b.each(a,function(d,e){c._setOption(d,e)});return this},_setOption:function(a,c){this.options[a]=c;if(a==="disabled")this.widget()[c?"addClass":"removeClass"](this.widgetBaseClass+"-disabled ui-state-disabled").attr("aria-disabled",c);return this},
enable:function(){return this._setOption("disabled",false)},disable:function(){return this._setOption("disabled",true)},_trigger:function(a,c,d){var e=this.options[a];c=b.Event(c);c.type=(a===this.widgetEventPrefix?a:this.widgetEventPrefix+a).toLowerCase();d=d||{};if(c.originalEvent){a=b.event.props.length;for(var f;a;){f=b.event.props[--a];c[f]=c.originalEvent[f]}}this.element.trigger(c,d);return!(b.isFunction(e)&&e.call(this.element[0],c,d)===false||c.isDefaultPrevented())}}})(jQuery);
;/*!
 * jQuery UI Mouse 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Mouse
 *
 * Depends:
 *	jquery.ui.widget.js
 */
(function(c){c.widget("ui.mouse",{options:{cancel:":input,option",distance:1,delay:0},_mouseInit:function(){var a=this;this.element.bind("mousedown."+this.widgetName,function(b){return a._mouseDown(b)}).bind("click."+this.widgetName,function(b){if(true===c.data(b.target,a.widgetName+".preventClickEvent")){c.removeData(b.target,a.widgetName+".preventClickEvent");b.stopImmediatePropagation();return false}});this.started=false},_mouseDestroy:function(){this.element.unbind("."+this.widgetName)},_mouseDown:function(a){a.originalEvent=
a.originalEvent||{};if(!a.originalEvent.mouseHandled){this._mouseStarted&&this._mouseUp(a);this._mouseDownEvent=a;var b=this,e=a.which==1,f=typeof this.options.cancel=="string"?c(a.target).parents().add(a.target).filter(this.options.cancel).length:false;if(!e||f||!this._mouseCapture(a))return true;this.mouseDelayMet=!this.options.delay;if(!this.mouseDelayMet)this._mouseDelayTimer=setTimeout(function(){b.mouseDelayMet=true},this.options.delay);if(this._mouseDistanceMet(a)&&this._mouseDelayMet(a)){this._mouseStarted=
this._mouseStart(a)!==false;if(!this._mouseStarted){a.preventDefault();return true}}this._mouseMoveDelegate=function(d){return b._mouseMove(d)};this._mouseUpDelegate=function(d){return b._mouseUp(d)};c(document).bind("mousemove."+this.widgetName,this._mouseMoveDelegate).bind("mouseup."+this.widgetName,this._mouseUpDelegate);a.preventDefault();return a.originalEvent.mouseHandled=true}},_mouseMove:function(a){if(c.browser.msie&&!(document.documentMode>=9)&&!a.button)return this._mouseUp(a);if(this._mouseStarted){this._mouseDrag(a);
return a.preventDefault()}if(this._mouseDistanceMet(a)&&this._mouseDelayMet(a))(this._mouseStarted=this._mouseStart(this._mouseDownEvent,a)!==false)?this._mouseDrag(a):this._mouseUp(a);return!this._mouseStarted},_mouseUp:function(a){c(document).unbind("mousemove."+this.widgetName,this._mouseMoveDelegate).unbind("mouseup."+this.widgetName,this._mouseUpDelegate);if(this._mouseStarted){this._mouseStarted=false;a.target==this._mouseDownEvent.target&&c.data(a.target,this.widgetName+".preventClickEvent",
true);this._mouseStop(a)}return false},_mouseDistanceMet:function(a){return Math.max(Math.abs(this._mouseDownEvent.pageX-a.pageX),Math.abs(this._mouseDownEvent.pageY-a.pageY))>=this.options.distance},_mouseDelayMet:function(){return this.mouseDelayMet},_mouseStart:function(){},_mouseDrag:function(){},_mouseStop:function(){},_mouseCapture:function(){return true}})})(jQuery);
;/*
 * jQuery UI Position 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Position
 */
(function(c){c.ui=c.ui||{};var n=/left|center|right/,o=/top|center|bottom/,t=c.fn.position,u=c.fn.offset;c.fn.position=function(b){if(!b||!b.of)return t.apply(this,arguments);b=c.extend({},b);var a=c(b.of),d=a[0],g=(b.collision||"flip").split(" "),e=b.offset?b.offset.split(" "):[0,0],h,k,j;if(d.nodeType===9){h=a.width();k=a.height();j={top:0,left:0}}else if(d.setTimeout){h=a.width();k=a.height();j={top:a.scrollTop(),left:a.scrollLeft()}}else if(d.preventDefault){b.at="left top";h=k=0;j={top:b.of.pageY,
left:b.of.pageX}}else{h=a.outerWidth();k=a.outerHeight();j=a.offset()}c.each(["my","at"],function(){var f=(b[this]||"").split(" ");if(f.length===1)f=n.test(f[0])?f.concat(["center"]):o.test(f[0])?["center"].concat(f):["center","center"];f[0]=n.test(f[0])?f[0]:"center";f[1]=o.test(f[1])?f[1]:"center";b[this]=f});if(g.length===1)g[1]=g[0];e[0]=parseInt(e[0],10)||0;if(e.length===1)e[1]=e[0];e[1]=parseInt(e[1],10)||0;if(b.at[0]==="right")j.left+=h;else if(b.at[0]==="center")j.left+=h/2;if(b.at[1]==="bottom")j.top+=
k;else if(b.at[1]==="center")j.top+=k/2;j.left+=e[0];j.top+=e[1];return this.each(function(){var f=c(this),l=f.outerWidth(),m=f.outerHeight(),p=parseInt(c.curCSS(this,"marginLeft",true))||0,q=parseInt(c.curCSS(this,"marginTop",true))||0,v=l+p+(parseInt(c.curCSS(this,"marginRight",true))||0),w=m+q+(parseInt(c.curCSS(this,"marginBottom",true))||0),i=c.extend({},j),r;if(b.my[0]==="right")i.left-=l;else if(b.my[0]==="center")i.left-=l/2;if(b.my[1]==="bottom")i.top-=m;else if(b.my[1]==="center")i.top-=
m/2;i.left=Math.round(i.left);i.top=Math.round(i.top);r={left:i.left-p,top:i.top-q};c.each(["left","top"],function(s,x){c.ui.position[g[s]]&&c.ui.position[g[s]][x](i,{targetWidth:h,targetHeight:k,elemWidth:l,elemHeight:m,collisionPosition:r,collisionWidth:v,collisionHeight:w,offset:e,my:b.my,at:b.at})});c.fn.bgiframe&&f.bgiframe();f.offset(c.extend(i,{using:b.using}))})};c.ui.position={fit:{left:function(b,a){var d=c(window);d=a.collisionPosition.left+a.collisionWidth-d.width()-d.scrollLeft();b.left=
d>0?b.left-d:Math.max(b.left-a.collisionPosition.left,b.left)},top:function(b,a){var d=c(window);d=a.collisionPosition.top+a.collisionHeight-d.height()-d.scrollTop();b.top=d>0?b.top-d:Math.max(b.top-a.collisionPosition.top,b.top)}},flip:{left:function(b,a){if(a.at[0]!=="center"){var d=c(window);d=a.collisionPosition.left+a.collisionWidth-d.width()-d.scrollLeft();var g=a.my[0]==="left"?-a.elemWidth:a.my[0]==="right"?a.elemWidth:0,e=a.at[0]==="left"?a.targetWidth:-a.targetWidth,h=-2*a.offset[0];b.left+=
a.collisionPosition.left<0?g+e+h:d>0?g+e+h:0}},top:function(b,a){if(a.at[1]!=="center"){var d=c(window);d=a.collisionPosition.top+a.collisionHeight-d.height()-d.scrollTop();var g=a.my[1]==="top"?-a.elemHeight:a.my[1]==="bottom"?a.elemHeight:0,e=a.at[1]==="top"?a.targetHeight:-a.targetHeight,h=-2*a.offset[1];b.top+=a.collisionPosition.top<0?g+e+h:d>0?g+e+h:0}}}};if(!c.offset.setOffset){c.offset.setOffset=function(b,a){if(/static/.test(c.curCSS(b,"position")))b.style.position="relative";var d=c(b),
g=d.offset(),e=parseInt(c.curCSS(b,"top",true),10)||0,h=parseInt(c.curCSS(b,"left",true),10)||0;g={top:a.top-g.top+e,left:a.left-g.left+h};"using"in a?a.using.call(b,g):d.css(g)};c.fn.offset=function(b){var a=this[0];if(!a||!a.ownerDocument)return null;if(b)return this.each(function(){c.offset.setOffset(this,b)});return u.call(this)}}})(jQuery);
;/*
 * jQuery UI Draggable 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Draggables
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.mouse.js
 *	jquery.ui.widget.js
 */
(function(d){d.widget("ui.draggable",d.ui.mouse,{widgetEventPrefix:"drag",options:{addClasses:true,appendTo:"parent",axis:false,connectToSortable:false,containment:false,cursor:"auto",cursorAt:false,grid:false,handle:false,helper:"original",iframeFix:false,opacity:false,refreshPositions:false,revert:false,revertDuration:500,scope:"default",scroll:true,scrollSensitivity:20,scrollSpeed:20,snap:false,snapMode:"both",snapTolerance:20,stack:false,zIndex:false},_create:function(){if(this.options.helper==
"original"&&!/^(?:r|a|f)/.test(this.element.css("position")))this.element[0].style.position="relative";this.options.addClasses&&this.element.addClass("ui-draggable");this.options.disabled&&this.element.addClass("ui-draggable-disabled");this._mouseInit()},destroy:function(){if(this.element.data("draggable")){this.element.removeData("draggable").unbind(".draggable").removeClass("ui-draggable ui-draggable-dragging ui-draggable-disabled");this._mouseDestroy();return this}},_mouseCapture:function(a){var b=
this.options;if(this.helper||b.disabled||d(a.target).is(".ui-resizable-handle"))return false;this.handle=this._getHandle(a);if(!this.handle)return false;return true},_mouseStart:function(a){var b=this.options;this.helper=this._createHelper(a);this._cacheHelperProportions();if(d.ui.ddmanager)d.ui.ddmanager.current=this;this._cacheMargins();this.cssPosition=this.helper.css("position");this.scrollParent=this.helper.scrollParent();this.offset=this.positionAbs=this.element.offset();this.offset={top:this.offset.top-
this.margins.top,left:this.offset.left-this.margins.left};d.extend(this.offset,{click:{left:a.pageX-this.offset.left,top:a.pageY-this.offset.top},parent:this._getParentOffset(),relative:this._getRelativeOffset()});this.originalPosition=this.position=this._generatePosition(a);this.originalPageX=a.pageX;this.originalPageY=a.pageY;b.cursorAt&&this._adjustOffsetFromHelper(b.cursorAt);b.containment&&this._setContainment();if(this._trigger("start",a)===false){this._clear();return false}this._cacheHelperProportions();
d.ui.ddmanager&&!b.dropBehaviour&&d.ui.ddmanager.prepareOffsets(this,a);this.helper.addClass("ui-draggable-dragging");this._mouseDrag(a,true);return true},_mouseDrag:function(a,b){this.position=this._generatePosition(a);this.positionAbs=this._convertPositionTo("absolute");if(!b){b=this._uiHash();if(this._trigger("drag",a,b)===false){this._mouseUp({});return false}this.position=b.position}if(!this.options.axis||this.options.axis!="y")this.helper[0].style.left=this.position.left+"px";if(!this.options.axis||
this.options.axis!="x")this.helper[0].style.top=this.position.top+"px";d.ui.ddmanager&&d.ui.ddmanager.drag(this,a);return false},_mouseStop:function(a){var b=false;if(d.ui.ddmanager&&!this.options.dropBehaviour)b=d.ui.ddmanager.drop(this,a);if(this.dropped){b=this.dropped;this.dropped=false}if((!this.element[0]||!this.element[0].parentNode)&&this.options.helper=="original")return false;if(this.options.revert=="invalid"&&!b||this.options.revert=="valid"&&b||this.options.revert===true||d.isFunction(this.options.revert)&&
this.options.revert.call(this.element,b)){var c=this;d(this.helper).animate(this.originalPosition,parseInt(this.options.revertDuration,10),function(){c._trigger("stop",a)!==false&&c._clear()})}else this._trigger("stop",a)!==false&&this._clear();return false},cancel:function(){this.helper.is(".ui-draggable-dragging")?this._mouseUp({}):this._clear();return this},_getHandle:function(a){var b=!this.options.handle||!d(this.options.handle,this.element).length?true:false;d(this.options.handle,this.element).find("*").andSelf().each(function(){if(this==
a.target)b=true});return b},_createHelper:function(a){var b=this.options;a=d.isFunction(b.helper)?d(b.helper.apply(this.element[0],[a])):b.helper=="clone"?this.element.clone():this.element;a.parents("body").length||a.appendTo(b.appendTo=="parent"?this.element[0].parentNode:b.appendTo);a[0]!=this.element[0]&&!/(fixed|absolute)/.test(a.css("position"))&&a.css("position","absolute");return a},_adjustOffsetFromHelper:function(a){if(typeof a=="string")a=a.split(" ");if(d.isArray(a))a={left:+a[0],top:+a[1]||
0};if("left"in a)this.offset.click.left=a.left+this.margins.left;if("right"in a)this.offset.click.left=this.helperProportions.width-a.right+this.margins.left;if("top"in a)this.offset.click.top=a.top+this.margins.top;if("bottom"in a)this.offset.click.top=this.helperProportions.height-a.bottom+this.margins.top},_getParentOffset:function(){this.offsetParent=this.helper.offsetParent();var a=this.offsetParent.offset();if(this.cssPosition=="absolute"&&this.scrollParent[0]!=document&&d.ui.contains(this.scrollParent[0],
this.offsetParent[0])){a.left+=this.scrollParent.scrollLeft();a.top+=this.scrollParent.scrollTop()}if(this.offsetParent[0]==document.body||this.offsetParent[0].tagName&&this.offsetParent[0].tagName.toLowerCase()=="html"&&d.browser.msie)a={top:0,left:0};return{top:a.top+(parseInt(this.offsetParent.css("borderTopWidth"),10)||0),left:a.left+(parseInt(this.offsetParent.css("borderLeftWidth"),10)||0)}},_getRelativeOffset:function(){if(this.cssPosition=="relative"){var a=this.element.position();return{top:a.top-
(parseInt(this.helper.css("top"),10)||0)+this.scrollParent.scrollTop(),left:a.left-(parseInt(this.helper.css("left"),10)||0)+this.scrollParent.scrollLeft()}}else return{top:0,left:0}},_cacheMargins:function(){this.margins={left:parseInt(this.element.css("marginLeft"),10)||0,top:parseInt(this.element.css("marginTop"),10)||0}},_cacheHelperProportions:function(){this.helperProportions={width:this.helper.outerWidth(),height:this.helper.outerHeight()}},_setContainment:function(){var a=this.options;if(a.containment==
"parent")a.containment=this.helper[0].parentNode;if(a.containment=="document"||a.containment=="window")this.containment=[(a.containment=="document"?0:d(window).scrollLeft())-this.offset.relative.left-this.offset.parent.left,(a.containment=="document"?0:d(window).scrollTop())-this.offset.relative.top-this.offset.parent.top,(a.containment=="document"?0:d(window).scrollLeft())+d(a.containment=="document"?document:window).width()-this.helperProportions.width-this.margins.left,(a.containment=="document"?
0:d(window).scrollTop())+(d(a.containment=="document"?document:window).height()||document.body.parentNode.scrollHeight)-this.helperProportions.height-this.margins.top];if(!/^(document|window|parent)$/.test(a.containment)&&a.containment.constructor!=Array){var b=d(a.containment)[0];if(b){a=d(a.containment).offset();var c=d(b).css("overflow")!="hidden";this.containment=[a.left+(parseInt(d(b).css("borderLeftWidth"),10)||0)+(parseInt(d(b).css("paddingLeft"),10)||0)-this.margins.left,a.top+(parseInt(d(b).css("borderTopWidth"),
10)||0)+(parseInt(d(b).css("paddingTop"),10)||0)-this.margins.top,a.left+(c?Math.max(b.scrollWidth,b.offsetWidth):b.offsetWidth)-(parseInt(d(b).css("borderLeftWidth"),10)||0)-(parseInt(d(b).css("paddingRight"),10)||0)-this.helperProportions.width-this.margins.left,a.top+(c?Math.max(b.scrollHeight,b.offsetHeight):b.offsetHeight)-(parseInt(d(b).css("borderTopWidth"),10)||0)-(parseInt(d(b).css("paddingBottom"),10)||0)-this.helperProportions.height-this.margins.top]}}else if(a.containment.constructor==
Array)this.containment=a.containment},_convertPositionTo:function(a,b){if(!b)b=this.position;a=a=="absolute"?1:-1;var c=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&d.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,f=/(html|body)/i.test(c[0].tagName);return{top:b.top+this.offset.relative.top*a+this.offset.parent.top*a-(d.browser.safari&&d.browser.version<526&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollTop():
f?0:c.scrollTop())*a),left:b.left+this.offset.relative.left*a+this.offset.parent.left*a-(d.browser.safari&&d.browser.version<526&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():f?0:c.scrollLeft())*a)}},_generatePosition:function(a){var b=this.options,c=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&d.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,f=/(html|body)/i.test(c[0].tagName),e=a.pageX,g=a.pageY;
if(this.originalPosition){if(this.containment){if(a.pageX-this.offset.click.left<this.containment[0])e=this.containment[0]+this.offset.click.left;if(a.pageY-this.offset.click.top<this.containment[1])g=this.containment[1]+this.offset.click.top;if(a.pageX-this.offset.click.left>this.containment[2])e=this.containment[2]+this.offset.click.left;if(a.pageY-this.offset.click.top>this.containment[3])g=this.containment[3]+this.offset.click.top}if(b.grid){g=this.originalPageY+Math.round((g-this.originalPageY)/
b.grid[1])*b.grid[1];g=this.containment?!(g-this.offset.click.top<this.containment[1]||g-this.offset.click.top>this.containment[3])?g:!(g-this.offset.click.top<this.containment[1])?g-b.grid[1]:g+b.grid[1]:g;e=this.originalPageX+Math.round((e-this.originalPageX)/b.grid[0])*b.grid[0];e=this.containment?!(e-this.offset.click.left<this.containment[0]||e-this.offset.click.left>this.containment[2])?e:!(e-this.offset.click.left<this.containment[0])?e-b.grid[0]:e+b.grid[0]:e}}return{top:g-this.offset.click.top-
this.offset.relative.top-this.offset.parent.top+(d.browser.safari&&d.browser.version<526&&this.cssPosition=="fixed"?0:this.cssPosition=="fixed"?-this.scrollParent.scrollTop():f?0:c.scrollTop()),left:e-this.offset.click.left-this.offset.relative.left-this.offset.parent.left+(d.browser.safari&&d.browser.version<526&&this.cssPosition=="fixed"?0:this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():f?0:c.scrollLeft())}},_clear:function(){this.helper.removeClass("ui-draggable-dragging");this.helper[0]!=
this.element[0]&&!this.cancelHelperRemoval&&this.helper.remove();this.helper=null;this.cancelHelperRemoval=false},_trigger:function(a,b,c){c=c||this._uiHash();d.ui.plugin.call(this,a,[b,c]);if(a=="drag")this.positionAbs=this._convertPositionTo("absolute");return d.Widget.prototype._trigger.call(this,a,b,c)},plugins:{},_uiHash:function(){return{helper:this.helper,position:this.position,originalPosition:this.originalPosition,offset:this.positionAbs}}});d.extend(d.ui.draggable,{version:"1.8.10"});
d.ui.plugin.add("draggable","connectToSortable",{start:function(a,b){var c=d(this).data("draggable"),f=c.options,e=d.extend({},b,{item:c.element});c.sortables=[];d(f.connectToSortable).each(function(){var g=d.data(this,"sortable");if(g&&!g.options.disabled){c.sortables.push({instance:g,shouldRevert:g.options.revert});g._refreshItems();g._trigger("activate",a,e)}})},stop:function(a,b){var c=d(this).data("draggable"),f=d.extend({},b,{item:c.element});d.each(c.sortables,function(){if(this.instance.isOver){this.instance.isOver=
0;c.cancelHelperRemoval=true;this.instance.cancelHelperRemoval=false;if(this.shouldRevert)this.instance.options.revert=true;this.instance._mouseStop(a);this.instance.options.helper=this.instance.options._helper;c.options.helper=="original"&&this.instance.currentItem.css({top:"auto",left:"auto"})}else{this.instance.cancelHelperRemoval=false;this.instance._trigger("deactivate",a,f)}})},drag:function(a,b){var c=d(this).data("draggable"),f=this;d.each(c.sortables,function(){this.instance.positionAbs=
c.positionAbs;this.instance.helperProportions=c.helperProportions;this.instance.offset.click=c.offset.click;if(this.instance._intersectsWith(this.instance.containerCache)){if(!this.instance.isOver){this.instance.isOver=1;this.instance.currentItem=d(f).clone().appendTo(this.instance.element).data("sortable-item",true);this.instance.options._helper=this.instance.options.helper;this.instance.options.helper=function(){return b.helper[0]};a.target=this.instance.currentItem[0];this.instance._mouseCapture(a,
true);this.instance._mouseStart(a,true,true);this.instance.offset.click.top=c.offset.click.top;this.instance.offset.click.left=c.offset.click.left;this.instance.offset.parent.left-=c.offset.parent.left-this.instance.offset.parent.left;this.instance.offset.parent.top-=c.offset.parent.top-this.instance.offset.parent.top;c._trigger("toSortable",a);c.dropped=this.instance.element;c.currentItem=c.element;this.instance.fromOutside=c}this.instance.currentItem&&this.instance._mouseDrag(a)}else if(this.instance.isOver){this.instance.isOver=
0;this.instance.cancelHelperRemoval=true;this.instance.options.revert=false;this.instance._trigger("out",a,this.instance._uiHash(this.instance));this.instance._mouseStop(a,true);this.instance.options.helper=this.instance.options._helper;this.instance.currentItem.remove();this.instance.placeholder&&this.instance.placeholder.remove();c._trigger("fromSortable",a);c.dropped=false}})}});d.ui.plugin.add("draggable","cursor",{start:function(){var a=d("body"),b=d(this).data("draggable").options;if(a.css("cursor"))b._cursor=
a.css("cursor");a.css("cursor",b.cursor)},stop:function(){var a=d(this).data("draggable").options;a._cursor&&d("body").css("cursor",a._cursor)}});d.ui.plugin.add("draggable","iframeFix",{start:function(){var a=d(this).data("draggable").options;d(a.iframeFix===true?"iframe":a.iframeFix).each(function(){d('<div class="ui-draggable-iframeFix" style="background: #fff;"></div>').css({width:this.offsetWidth+"px",height:this.offsetHeight+"px",position:"absolute",opacity:"0.001",zIndex:1E3}).css(d(this).offset()).appendTo("body")})},
stop:function(){d("div.ui-draggable-iframeFix").each(function(){this.parentNode.removeChild(this)})}});d.ui.plugin.add("draggable","opacity",{start:function(a,b){a=d(b.helper);b=d(this).data("draggable").options;if(a.css("opacity"))b._opacity=a.css("opacity");a.css("opacity",b.opacity)},stop:function(a,b){a=d(this).data("draggable").options;a._opacity&&d(b.helper).css("opacity",a._opacity)}});d.ui.plugin.add("draggable","scroll",{start:function(){var a=d(this).data("draggable");if(a.scrollParent[0]!=
document&&a.scrollParent[0].tagName!="HTML")a.overflowOffset=a.scrollParent.offset()},drag:function(a){var b=d(this).data("draggable"),c=b.options,f=false;if(b.scrollParent[0]!=document&&b.scrollParent[0].tagName!="HTML"){if(!c.axis||c.axis!="x")if(b.overflowOffset.top+b.scrollParent[0].offsetHeight-a.pageY<c.scrollSensitivity)b.scrollParent[0].scrollTop=f=b.scrollParent[0].scrollTop+c.scrollSpeed;else if(a.pageY-b.overflowOffset.top<c.scrollSensitivity)b.scrollParent[0].scrollTop=f=b.scrollParent[0].scrollTop-
c.scrollSpeed;if(!c.axis||c.axis!="y")if(b.overflowOffset.left+b.scrollParent[0].offsetWidth-a.pageX<c.scrollSensitivity)b.scrollParent[0].scrollLeft=f=b.scrollParent[0].scrollLeft+c.scrollSpeed;else if(a.pageX-b.overflowOffset.left<c.scrollSensitivity)b.scrollParent[0].scrollLeft=f=b.scrollParent[0].scrollLeft-c.scrollSpeed}else{if(!c.axis||c.axis!="x")if(a.pageY-d(document).scrollTop()<c.scrollSensitivity)f=d(document).scrollTop(d(document).scrollTop()-c.scrollSpeed);else if(d(window).height()-
(a.pageY-d(document).scrollTop())<c.scrollSensitivity)f=d(document).scrollTop(d(document).scrollTop()+c.scrollSpeed);if(!c.axis||c.axis!="y")if(a.pageX-d(document).scrollLeft()<c.scrollSensitivity)f=d(document).scrollLeft(d(document).scrollLeft()-c.scrollSpeed);else if(d(window).width()-(a.pageX-d(document).scrollLeft())<c.scrollSensitivity)f=d(document).scrollLeft(d(document).scrollLeft()+c.scrollSpeed)}f!==false&&d.ui.ddmanager&&!c.dropBehaviour&&d.ui.ddmanager.prepareOffsets(b,a)}});d.ui.plugin.add("draggable",
"snap",{start:function(){var a=d(this).data("draggable"),b=a.options;a.snapElements=[];d(b.snap.constructor!=String?b.snap.items||":data(draggable)":b.snap).each(function(){var c=d(this),f=c.offset();this!=a.element[0]&&a.snapElements.push({item:this,width:c.outerWidth(),height:c.outerHeight(),top:f.top,left:f.left})})},drag:function(a,b){for(var c=d(this).data("draggable"),f=c.options,e=f.snapTolerance,g=b.offset.left,n=g+c.helperProportions.width,m=b.offset.top,o=m+c.helperProportions.height,h=
c.snapElements.length-1;h>=0;h--){var i=c.snapElements[h].left,k=i+c.snapElements[h].width,j=c.snapElements[h].top,l=j+c.snapElements[h].height;if(i-e<g&&g<k+e&&j-e<m&&m<l+e||i-e<g&&g<k+e&&j-e<o&&o<l+e||i-e<n&&n<k+e&&j-e<m&&m<l+e||i-e<n&&n<k+e&&j-e<o&&o<l+e){if(f.snapMode!="inner"){var p=Math.abs(j-o)<=e,q=Math.abs(l-m)<=e,r=Math.abs(i-n)<=e,s=Math.abs(k-g)<=e;if(p)b.position.top=c._convertPositionTo("relative",{top:j-c.helperProportions.height,left:0}).top-c.margins.top;if(q)b.position.top=c._convertPositionTo("relative",
{top:l,left:0}).top-c.margins.top;if(r)b.position.left=c._convertPositionTo("relative",{top:0,left:i-c.helperProportions.width}).left-c.margins.left;if(s)b.position.left=c._convertPositionTo("relative",{top:0,left:k}).left-c.margins.left}var t=p||q||r||s;if(f.snapMode!="outer"){p=Math.abs(j-m)<=e;q=Math.abs(l-o)<=e;r=Math.abs(i-g)<=e;s=Math.abs(k-n)<=e;if(p)b.position.top=c._convertPositionTo("relative",{top:j,left:0}).top-c.margins.top;if(q)b.position.top=c._convertPositionTo("relative",{top:l-c.helperProportions.height,
left:0}).top-c.margins.top;if(r)b.position.left=c._convertPositionTo("relative",{top:0,left:i}).left-c.margins.left;if(s)b.position.left=c._convertPositionTo("relative",{top:0,left:k-c.helperProportions.width}).left-c.margins.left}if(!c.snapElements[h].snapping&&(p||q||r||s||t))c.options.snap.snap&&c.options.snap.snap.call(c.element,a,d.extend(c._uiHash(),{snapItem:c.snapElements[h].item}));c.snapElements[h].snapping=p||q||r||s||t}else{c.snapElements[h].snapping&&c.options.snap.release&&c.options.snap.release.call(c.element,
a,d.extend(c._uiHash(),{snapItem:c.snapElements[h].item}));c.snapElements[h].snapping=false}}}});d.ui.plugin.add("draggable","stack",{start:function(){var a=d(this).data("draggable").options;a=d.makeArray(d(a.stack)).sort(function(c,f){return(parseInt(d(c).css("zIndex"),10)||0)-(parseInt(d(f).css("zIndex"),10)||0)});if(a.length){var b=parseInt(a[0].style.zIndex)||0;d(a).each(function(c){this.style.zIndex=b+c});this[0].style.zIndex=b+a.length}}});d.ui.plugin.add("draggable","zIndex",{start:function(a,
b){a=d(b.helper);b=d(this).data("draggable").options;if(a.css("zIndex"))b._zIndex=a.css("zIndex");a.css("zIndex",b.zIndex)},stop:function(a,b){a=d(this).data("draggable").options;a._zIndex&&d(b.helper).css("zIndex",a._zIndex)}})})(jQuery);
;/*
 * jQuery UI Droppable 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Droppables
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 *	jquery.ui.mouse.js
 *	jquery.ui.draggable.js
 */
(function(d){d.widget("ui.droppable",{widgetEventPrefix:"drop",options:{accept:"*",activeClass:false,addClasses:true,greedy:false,hoverClass:false,scope:"default",tolerance:"intersect"},_create:function(){var a=this.options,b=a.accept;this.isover=0;this.isout=1;this.accept=d.isFunction(b)?b:function(c){return c.is(b)};this.proportions={width:this.element[0].offsetWidth,height:this.element[0].offsetHeight};d.ui.ddmanager.droppables[a.scope]=d.ui.ddmanager.droppables[a.scope]||[];d.ui.ddmanager.droppables[a.scope].push(this);
a.addClasses&&this.element.addClass("ui-droppable")},destroy:function(){for(var a=d.ui.ddmanager.droppables[this.options.scope],b=0;b<a.length;b++)a[b]==this&&a.splice(b,1);this.element.removeClass("ui-droppable ui-droppable-disabled").removeData("droppable").unbind(".droppable");return this},_setOption:function(a,b){if(a=="accept")this.accept=d.isFunction(b)?b:function(c){return c.is(b)};d.Widget.prototype._setOption.apply(this,arguments)},_activate:function(a){var b=d.ui.ddmanager.current;this.options.activeClass&&
this.element.addClass(this.options.activeClass);b&&this._trigger("activate",a,this.ui(b))},_deactivate:function(a){var b=d.ui.ddmanager.current;this.options.activeClass&&this.element.removeClass(this.options.activeClass);b&&this._trigger("deactivate",a,this.ui(b))},_over:function(a){var b=d.ui.ddmanager.current;if(!(!b||(b.currentItem||b.element)[0]==this.element[0]))if(this.accept.call(this.element[0],b.currentItem||b.element)){this.options.hoverClass&&this.element.addClass(this.options.hoverClass);
this._trigger("over",a,this.ui(b))}},_out:function(a){var b=d.ui.ddmanager.current;if(!(!b||(b.currentItem||b.element)[0]==this.element[0]))if(this.accept.call(this.element[0],b.currentItem||b.element)){this.options.hoverClass&&this.element.removeClass(this.options.hoverClass);this._trigger("out",a,this.ui(b))}},_drop:function(a,b){var c=b||d.ui.ddmanager.current;if(!c||(c.currentItem||c.element)[0]==this.element[0])return false;var e=false;this.element.find(":data(droppable)").not(".ui-draggable-dragging").each(function(){var g=
d.data(this,"droppable");if(g.options.greedy&&!g.options.disabled&&g.options.scope==c.options.scope&&g.accept.call(g.element[0],c.currentItem||c.element)&&d.ui.intersect(c,d.extend(g,{offset:g.element.offset()}),g.options.tolerance)){e=true;return false}});if(e)return false;if(this.accept.call(this.element[0],c.currentItem||c.element)){this.options.activeClass&&this.element.removeClass(this.options.activeClass);this.options.hoverClass&&this.element.removeClass(this.options.hoverClass);this._trigger("drop",
a,this.ui(c));return this.element}return false},ui:function(a){return{draggable:a.currentItem||a.element,helper:a.helper,position:a.position,offset:a.positionAbs}}});d.extend(d.ui.droppable,{version:"1.8.10"});d.ui.intersect=function(a,b,c){if(!b.offset)return false;var e=(a.positionAbs||a.position.absolute).left,g=e+a.helperProportions.width,f=(a.positionAbs||a.position.absolute).top,h=f+a.helperProportions.height,i=b.offset.left,k=i+b.proportions.width,j=b.offset.top,l=j+b.proportions.height;
switch(c){case "fit":return i<=e&&g<=k&&j<=f&&h<=l;case "intersect":return i<e+a.helperProportions.width/2&&g-a.helperProportions.width/2<k&&j<f+a.helperProportions.height/2&&h-a.helperProportions.height/2<l;case "pointer":return d.ui.isOver((a.positionAbs||a.position.absolute).top+(a.clickOffset||a.offset.click).top,(a.positionAbs||a.position.absolute).left+(a.clickOffset||a.offset.click).left,j,i,b.proportions.height,b.proportions.width);case "touch":return(f>=j&&f<=l||h>=j&&h<=l||f<j&&h>l)&&(e>=
i&&e<=k||g>=i&&g<=k||e<i&&g>k);default:return false}};d.ui.ddmanager={current:null,droppables:{"default":[]},prepareOffsets:function(a,b){var c=d.ui.ddmanager.droppables[a.options.scope]||[],e=b?b.type:null,g=(a.currentItem||a.element).find(":data(droppable)").andSelf(),f=0;a:for(;f<c.length;f++)if(!(c[f].options.disabled||a&&!c[f].accept.call(c[f].element[0],a.currentItem||a.element))){for(var h=0;h<g.length;h++)if(g[h]==c[f].element[0]){c[f].proportions.height=0;continue a}c[f].visible=c[f].element.css("display")!=
"none";if(c[f].visible){c[f].offset=c[f].element.offset();c[f].proportions={width:c[f].element[0].offsetWidth,height:c[f].element[0].offsetHeight};e=="mousedown"&&c[f]._activate.call(c[f],b)}}},drop:function(a,b){var c=false;d.each(d.ui.ddmanager.droppables[a.options.scope]||[],function(){if(this.options){if(!this.options.disabled&&this.visible&&d.ui.intersect(a,this,this.options.tolerance))c=c||this._drop.call(this,b);if(!this.options.disabled&&this.visible&&this.accept.call(this.element[0],a.currentItem||
a.element)){this.isout=1;this.isover=0;this._deactivate.call(this,b)}}});return c},drag:function(a,b){a.options.refreshPositions&&d.ui.ddmanager.prepareOffsets(a,b);d.each(d.ui.ddmanager.droppables[a.options.scope]||[],function(){if(!(this.options.disabled||this.greedyChild||!this.visible)){var c=d.ui.intersect(a,this,this.options.tolerance);if(c=!c&&this.isover==1?"isout":c&&this.isover==0?"isover":null){var e;if(this.options.greedy){var g=this.element.parents(":data(droppable):eq(0)");if(g.length){e=
d.data(g[0],"droppable");e.greedyChild=c=="isover"?1:0}}if(e&&c=="isover"){e.isover=0;e.isout=1;e._out.call(e,b)}this[c]=1;this[c=="isout"?"isover":"isout"]=0;this[c=="isover"?"_over":"_out"].call(this,b);if(e&&c=="isout"){e.isout=0;e.isover=1;e._over.call(e,b)}}}})}}})(jQuery);
;/*
 * jQuery UI Resizable 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Resizables
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.mouse.js
 *	jquery.ui.widget.js
 */
(function(e){e.widget("ui.resizable",e.ui.mouse,{widgetEventPrefix:"resize",options:{alsoResize:false,animate:false,animateDuration:"slow",animateEasing:"swing",aspectRatio:false,autoHide:false,containment:false,ghost:false,grid:false,handles:"e,s,se",helper:false,maxHeight:null,maxWidth:null,minHeight:10,minWidth:10,zIndex:1E3},_create:function(){var b=this,a=this.options;this.element.addClass("ui-resizable");e.extend(this,{_aspectRatio:!!a.aspectRatio,aspectRatio:a.aspectRatio,originalElement:this.element,
_proportionallyResizeElements:[],_helper:a.helper||a.ghost||a.animate?a.helper||"ui-resizable-helper":null});if(this.element[0].nodeName.match(/canvas|textarea|input|select|button|img/i)){/relative/.test(this.element.css("position"))&&e.browser.opera&&this.element.css({position:"relative",top:"auto",left:"auto"});this.element.wrap(e('<div class="ui-wrapper" style="overflow: hidden;"></div>').css({position:this.element.css("position"),width:this.element.outerWidth(),height:this.element.outerHeight(),
top:this.element.css("top"),left:this.element.css("left")}));this.element=this.element.parent().data("resizable",this.element.data("resizable"));this.elementIsWrapper=true;this.element.css({marginLeft:this.originalElement.css("marginLeft"),marginTop:this.originalElement.css("marginTop"),marginRight:this.originalElement.css("marginRight"),marginBottom:this.originalElement.css("marginBottom")});this.originalElement.css({marginLeft:0,marginTop:0,marginRight:0,marginBottom:0});this.originalResizeStyle=
this.originalElement.css("resize");this.originalElement.css("resize","none");this._proportionallyResizeElements.push(this.originalElement.css({position:"static",zoom:1,display:"block"}));this.originalElement.css({margin:this.originalElement.css("margin")});this._proportionallyResize()}this.handles=a.handles||(!e(".ui-resizable-handle",this.element).length?"e,s,se":{n:".ui-resizable-n",e:".ui-resizable-e",s:".ui-resizable-s",w:".ui-resizable-w",se:".ui-resizable-se",sw:".ui-resizable-sw",ne:".ui-resizable-ne",
nw:".ui-resizable-nw"});if(this.handles.constructor==String){if(this.handles=="all")this.handles="n,e,s,w,se,sw,ne,nw";var c=this.handles.split(",");this.handles={};for(var d=0;d<c.length;d++){var f=e.trim(c[d]),g=e('<div class="ui-resizable-handle '+("ui-resizable-"+f)+'"></div>');/sw|se|ne|nw/.test(f)&&g.css({zIndex:++a.zIndex});"se"==f&&g.addClass("ui-icon ui-icon-gripsmall-diagonal-se");this.handles[f]=".ui-resizable-"+f;this.element.append(g)}}this._renderAxis=function(h){h=h||this.element;for(var i in this.handles){if(this.handles[i].constructor==
String)this.handles[i]=e(this.handles[i],this.element).show();if(this.elementIsWrapper&&this.originalElement[0].nodeName.match(/textarea|input|select|button/i)){var j=e(this.handles[i],this.element),k=0;k=/sw|ne|nw|se|n|s/.test(i)?j.outerHeight():j.outerWidth();j=["padding",/ne|nw|n/.test(i)?"Top":/se|sw|s/.test(i)?"Bottom":/^e$/.test(i)?"Right":"Left"].join("");h.css(j,k);this._proportionallyResize()}e(this.handles[i])}};this._renderAxis(this.element);this._handles=e(".ui-resizable-handle",this.element).disableSelection();
this._handles.mouseover(function(){if(!b.resizing){if(this.className)var h=this.className.match(/ui-resizable-(se|sw|ne|nw|n|e|s|w)/i);b.axis=h&&h[1]?h[1]:"se"}});if(a.autoHide){this._handles.hide();e(this.element).addClass("ui-resizable-autohide").hover(function(){e(this).removeClass("ui-resizable-autohide");b._handles.show()},function(){if(!b.resizing){e(this).addClass("ui-resizable-autohide");b._handles.hide()}})}this._mouseInit()},destroy:function(){this._mouseDestroy();var b=function(c){e(c).removeClass("ui-resizable ui-resizable-disabled ui-resizable-resizing").removeData("resizable").unbind(".resizable").find(".ui-resizable-handle").remove()};
if(this.elementIsWrapper){b(this.element);var a=this.element;a.after(this.originalElement.css({position:a.css("position"),width:a.outerWidth(),height:a.outerHeight(),top:a.css("top"),left:a.css("left")})).remove()}this.originalElement.css("resize",this.originalResizeStyle);b(this.originalElement);return this},_mouseCapture:function(b){var a=false;for(var c in this.handles)if(e(this.handles[c])[0]==b.target)a=true;return!this.options.disabled&&a},_mouseStart:function(b){var a=this.options,c=this.element.position(),
d=this.element;this.resizing=true;this.documentScroll={top:e(document).scrollTop(),left:e(document).scrollLeft()};if(d.is(".ui-draggable")||/absolute/.test(d.css("position")))d.css({position:"absolute",top:c.top,left:c.left});e.browser.opera&&/relative/.test(d.css("position"))&&d.css({position:"relative",top:"auto",left:"auto"});this._renderProxy();c=m(this.helper.css("left"));var f=m(this.helper.css("top"));if(a.containment){c+=e(a.containment).scrollLeft()||0;f+=e(a.containment).scrollTop()||0}this.offset=
this.helper.offset();this.position={left:c,top:f};this.size=this._helper?{width:d.outerWidth(),height:d.outerHeight()}:{width:d.width(),height:d.height()};this.originalSize=this._helper?{width:d.outerWidth(),height:d.outerHeight()}:{width:d.width(),height:d.height()};this.originalPosition={left:c,top:f};this.sizeDiff={width:d.outerWidth()-d.width(),height:d.outerHeight()-d.height()};this.originalMousePosition={left:b.pageX,top:b.pageY};this.aspectRatio=typeof a.aspectRatio=="number"?a.aspectRatio:
this.originalSize.width/this.originalSize.height||1;a=e(".ui-resizable-"+this.axis).css("cursor");e("body").css("cursor",a=="auto"?this.axis+"-resize":a);d.addClass("ui-resizable-resizing");this._propagate("start",b);return true},_mouseDrag:function(b){var a=this.helper,c=this.originalMousePosition,d=this._change[this.axis];if(!d)return false;c=d.apply(this,[b,b.pageX-c.left||0,b.pageY-c.top||0]);if(this._aspectRatio||b.shiftKey)c=this._updateRatio(c,b);c=this._respectSize(c,b);this._propagate("resize",
b);a.css({top:this.position.top+"px",left:this.position.left+"px",width:this.size.width+"px",height:this.size.height+"px"});!this._helper&&this._proportionallyResizeElements.length&&this._proportionallyResize();this._updateCache(c);this._trigger("resize",b,this.ui());return false},_mouseStop:function(b){this.resizing=false;var a=this.options,c=this;if(this._helper){var d=this._proportionallyResizeElements,f=d.length&&/textarea/i.test(d[0].nodeName);d=f&&e.ui.hasScroll(d[0],"left")?0:c.sizeDiff.height;
f=f?0:c.sizeDiff.width;f={width:c.helper.width()-f,height:c.helper.height()-d};d=parseInt(c.element.css("left"),10)+(c.position.left-c.originalPosition.left)||null;var g=parseInt(c.element.css("top"),10)+(c.position.top-c.originalPosition.top)||null;a.animate||this.element.css(e.extend(f,{top:g,left:d}));c.helper.height(c.size.height);c.helper.width(c.size.width);this._helper&&!a.animate&&this._proportionallyResize()}e("body").css("cursor","auto");this.element.removeClass("ui-resizable-resizing");
this._propagate("stop",b);this._helper&&this.helper.remove();return false},_updateCache:function(b){this.offset=this.helper.offset();if(l(b.left))this.position.left=b.left;if(l(b.top))this.position.top=b.top;if(l(b.height))this.size.height=b.height;if(l(b.width))this.size.width=b.width},_updateRatio:function(b){var a=this.position,c=this.size,d=this.axis;if(b.height)b.width=c.height*this.aspectRatio;else if(b.width)b.height=c.width/this.aspectRatio;if(d=="sw"){b.left=a.left+(c.width-b.width);b.top=
null}if(d=="nw"){b.top=a.top+(c.height-b.height);b.left=a.left+(c.width-b.width)}return b},_respectSize:function(b){var a=this.options,c=this.axis,d=l(b.width)&&a.maxWidth&&a.maxWidth<b.width,f=l(b.height)&&a.maxHeight&&a.maxHeight<b.height,g=l(b.width)&&a.minWidth&&a.minWidth>b.width,h=l(b.height)&&a.minHeight&&a.minHeight>b.height;if(g)b.width=a.minWidth;if(h)b.height=a.minHeight;if(d)b.width=a.maxWidth;if(f)b.height=a.maxHeight;var i=this.originalPosition.left+this.originalSize.width,j=this.position.top+
this.size.height,k=/sw|nw|w/.test(c);c=/nw|ne|n/.test(c);if(g&&k)b.left=i-a.minWidth;if(d&&k)b.left=i-a.maxWidth;if(h&&c)b.top=j-a.minHeight;if(f&&c)b.top=j-a.maxHeight;if((a=!b.width&&!b.height)&&!b.left&&b.top)b.top=null;else if(a&&!b.top&&b.left)b.left=null;return b},_proportionallyResize:function(){if(this._proportionallyResizeElements.length)for(var b=this.helper||this.element,a=0;a<this._proportionallyResizeElements.length;a++){var c=this._proportionallyResizeElements[a];if(!this.borderDif){var d=
[c.css("borderTopWidth"),c.css("borderRightWidth"),c.css("borderBottomWidth"),c.css("borderLeftWidth")],f=[c.css("paddingTop"),c.css("paddingRight"),c.css("paddingBottom"),c.css("paddingLeft")];this.borderDif=e.map(d,function(g,h){g=parseInt(g,10)||0;h=parseInt(f[h],10)||0;return g+h})}e.browser.msie&&(e(b).is(":hidden")||e(b).parents(":hidden").length)||c.css({height:b.height()-this.borderDif[0]-this.borderDif[2]||0,width:b.width()-this.borderDif[1]-this.borderDif[3]||0})}},_renderProxy:function(){var b=
this.options;this.elementOffset=this.element.offset();if(this._helper){this.helper=this.helper||e('<div style="overflow:hidden;"></div>');var a=e.browser.msie&&e.browser.version<7,c=a?1:0;a=a?2:-1;this.helper.addClass(this._helper).css({width:this.element.outerWidth()+a,height:this.element.outerHeight()+a,position:"absolute",left:this.elementOffset.left-c+"px",top:this.elementOffset.top-c+"px",zIndex:++b.zIndex});this.helper.appendTo("body").disableSelection()}else this.helper=this.element},_change:{e:function(b,
a){return{width:this.originalSize.width+a}},w:function(b,a){return{left:this.originalPosition.left+a,width:this.originalSize.width-a}},n:function(b,a,c){return{top:this.originalPosition.top+c,height:this.originalSize.height-c}},s:function(b,a,c){return{height:this.originalSize.height+c}},se:function(b,a,c){return e.extend(this._change.s.apply(this,arguments),this._change.e.apply(this,[b,a,c]))},sw:function(b,a,c){return e.extend(this._change.s.apply(this,arguments),this._change.w.apply(this,[b,a,
c]))},ne:function(b,a,c){return e.extend(this._change.n.apply(this,arguments),this._change.e.apply(this,[b,a,c]))},nw:function(b,a,c){return e.extend(this._change.n.apply(this,arguments),this._change.w.apply(this,[b,a,c]))}},_propagate:function(b,a){e.ui.plugin.call(this,b,[a,this.ui()]);b!="resize"&&this._trigger(b,a,this.ui())},plugins:{},ui:function(){return{originalElement:this.originalElement,element:this.element,helper:this.helper,position:this.position,size:this.size,originalSize:this.originalSize,
originalPosition:this.originalPosition}}});e.extend(e.ui.resizable,{version:"1.8.10"});e.ui.plugin.add("resizable","alsoResize",{start:function(){var b=e(this).data("resizable").options,a=function(c){e(c).each(function(){var d=e(this);d.data("resizable-alsoresize",{width:parseInt(d.width(),10),height:parseInt(d.height(),10),left:parseInt(d.css("left"),10),top:parseInt(d.css("top"),10),position:d.css("position")})})};if(typeof b.alsoResize=="object"&&!b.alsoResize.parentNode)if(b.alsoResize.length){b.alsoResize=
b.alsoResize[0];a(b.alsoResize)}else e.each(b.alsoResize,function(c){a(c)});else a(b.alsoResize)},resize:function(b,a){var c=e(this).data("resizable");b=c.options;var d=c.originalSize,f=c.originalPosition,g={height:c.size.height-d.height||0,width:c.size.width-d.width||0,top:c.position.top-f.top||0,left:c.position.left-f.left||0},h=function(i,j){e(i).each(function(){var k=e(this),q=e(this).data("resizable-alsoresize"),p={},r=j&&j.length?j:k.parents(a.originalElement[0]).length?["width","height"]:["width",
"height","top","left"];e.each(r,function(n,o){if((n=(q[o]||0)+(g[o]||0))&&n>=0)p[o]=n||null});if(e.browser.opera&&/relative/.test(k.css("position"))){c._revertToRelativePosition=true;k.css({position:"absolute",top:"auto",left:"auto"})}k.css(p)})};typeof b.alsoResize=="object"&&!b.alsoResize.nodeType?e.each(b.alsoResize,function(i,j){h(i,j)}):h(b.alsoResize)},stop:function(){var b=e(this).data("resizable"),a=b.options,c=function(d){e(d).each(function(){var f=e(this);f.css({position:f.data("resizable-alsoresize").position})})};
if(b._revertToRelativePosition){b._revertToRelativePosition=false;typeof a.alsoResize=="object"&&!a.alsoResize.nodeType?e.each(a.alsoResize,function(d){c(d)}):c(a.alsoResize)}e(this).removeData("resizable-alsoresize")}});e.ui.plugin.add("resizable","animate",{stop:function(b){var a=e(this).data("resizable"),c=a.options,d=a._proportionallyResizeElements,f=d.length&&/textarea/i.test(d[0].nodeName),g=f&&e.ui.hasScroll(d[0],"left")?0:a.sizeDiff.height;f={width:a.size.width-(f?0:a.sizeDiff.width),height:a.size.height-
g};g=parseInt(a.element.css("left"),10)+(a.position.left-a.originalPosition.left)||null;var h=parseInt(a.element.css("top"),10)+(a.position.top-a.originalPosition.top)||null;a.element.animate(e.extend(f,h&&g?{top:h,left:g}:{}),{duration:c.animateDuration,easing:c.animateEasing,step:function(){var i={width:parseInt(a.element.css("width"),10),height:parseInt(a.element.css("height"),10),top:parseInt(a.element.css("top"),10),left:parseInt(a.element.css("left"),10)};d&&d.length&&e(d[0]).css({width:i.width,
height:i.height});a._updateCache(i);a._propagate("resize",b)}})}});e.ui.plugin.add("resizable","containment",{start:function(){var b=e(this).data("resizable"),a=b.element,c=b.options.containment;if(a=c instanceof e?c.get(0):/parent/.test(c)?a.parent().get(0):c){b.containerElement=e(a);if(/document/.test(c)||c==document){b.containerOffset={left:0,top:0};b.containerPosition={left:0,top:0};b.parentData={element:e(document),left:0,top:0,width:e(document).width(),height:e(document).height()||document.body.parentNode.scrollHeight}}else{var d=
e(a),f=[];e(["Top","Right","Left","Bottom"]).each(function(i,j){f[i]=m(d.css("padding"+j))});b.containerOffset=d.offset();b.containerPosition=d.position();b.containerSize={height:d.innerHeight()-f[3],width:d.innerWidth()-f[1]};c=b.containerOffset;var g=b.containerSize.height,h=b.containerSize.width;h=e.ui.hasScroll(a,"left")?a.scrollWidth:h;g=e.ui.hasScroll(a)?a.scrollHeight:g;b.parentData={element:a,left:c.left,top:c.top,width:h,height:g}}}},resize:function(b){var a=e(this).data("resizable"),c=a.options,
d=a.containerOffset,f=a.position;b=a._aspectRatio||b.shiftKey;var g={top:0,left:0},h=a.containerElement;if(h[0]!=document&&/static/.test(h.css("position")))g=d;if(f.left<(a._helper?d.left:0)){a.size.width+=a._helper?a.position.left-d.left:a.position.left-g.left;if(b)a.size.height=a.size.width/c.aspectRatio;a.position.left=c.helper?d.left:0}if(f.top<(a._helper?d.top:0)){a.size.height+=a._helper?a.position.top-d.top:a.position.top;if(b)a.size.width=a.size.height*c.aspectRatio;a.position.top=a._helper?
d.top:0}a.offset.left=a.parentData.left+a.position.left;a.offset.top=a.parentData.top+a.position.top;c=Math.abs((a._helper?a.offset.left-g.left:a.offset.left-g.left)+a.sizeDiff.width);d=Math.abs((a._helper?a.offset.top-g.top:a.offset.top-d.top)+a.sizeDiff.height);f=a.containerElement.get(0)==a.element.parent().get(0);g=/relative|absolute/.test(a.containerElement.css("position"));if(f&&g)c-=a.parentData.left;if(c+a.size.width>=a.parentData.width){a.size.width=a.parentData.width-c;if(b)a.size.height=
a.size.width/a.aspectRatio}if(d+a.size.height>=a.parentData.height){a.size.height=a.parentData.height-d;if(b)a.size.width=a.size.height*a.aspectRatio}},stop:function(){var b=e(this).data("resizable"),a=b.options,c=b.containerOffset,d=b.containerPosition,f=b.containerElement,g=e(b.helper),h=g.offset(),i=g.outerWidth()-b.sizeDiff.width;g=g.outerHeight()-b.sizeDiff.height;b._helper&&!a.animate&&/relative/.test(f.css("position"))&&e(this).css({left:h.left-d.left-c.left,width:i,height:g});b._helper&&!a.animate&&
/static/.test(f.css("position"))&&e(this).css({left:h.left-d.left-c.left,width:i,height:g})}});e.ui.plugin.add("resizable","ghost",{start:function(){var b=e(this).data("resizable"),a=b.options,c=b.size;b.ghost=b.originalElement.clone();b.ghost.css({opacity:0.25,display:"block",position:"relative",height:c.height,width:c.width,margin:0,left:0,top:0}).addClass("ui-resizable-ghost").addClass(typeof a.ghost=="string"?a.ghost:"");b.ghost.appendTo(b.helper)},resize:function(){var b=e(this).data("resizable");
b.ghost&&b.ghost.css({position:"relative",height:b.size.height,width:b.size.width})},stop:function(){var b=e(this).data("resizable");b.ghost&&b.helper&&b.helper.get(0).removeChild(b.ghost.get(0))}});e.ui.plugin.add("resizable","grid",{resize:function(){var b=e(this).data("resizable"),a=b.options,c=b.size,d=b.originalSize,f=b.originalPosition,g=b.axis;a.grid=typeof a.grid=="number"?[a.grid,a.grid]:a.grid;var h=Math.round((c.width-d.width)/(a.grid[0]||1))*(a.grid[0]||1);a=Math.round((c.height-d.height)/
(a.grid[1]||1))*(a.grid[1]||1);if(/^(se|s|e)$/.test(g)){b.size.width=d.width+h;b.size.height=d.height+a}else if(/^(ne)$/.test(g)){b.size.width=d.width+h;b.size.height=d.height+a;b.position.top=f.top-a}else{if(/^(sw)$/.test(g)){b.size.width=d.width+h;b.size.height=d.height+a}else{b.size.width=d.width+h;b.size.height=d.height+a;b.position.top=f.top-a}b.position.left=f.left-h}}});var m=function(b){return parseInt(b,10)||0},l=function(b){return!isNaN(parseInt(b,10))}})(jQuery);
;/*
 * jQuery UI Selectable 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Selectables
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.mouse.js
 *	jquery.ui.widget.js
 */
(function(e){e.widget("ui.selectable",e.ui.mouse,{options:{appendTo:"body",autoRefresh:true,distance:0,filter:"*",tolerance:"touch"},_create:function(){var c=this;this.element.addClass("ui-selectable");this.dragged=false;var f;this.refresh=function(){f=e(c.options.filter,c.element[0]);f.each(function(){var d=e(this),b=d.offset();e.data(this,"selectable-item",{element:this,$element:d,left:b.left,top:b.top,right:b.left+d.outerWidth(),bottom:b.top+d.outerHeight(),startselected:false,selected:d.hasClass("ui-selected"),
selecting:d.hasClass("ui-selecting"),unselecting:d.hasClass("ui-unselecting")})})};this.refresh();this.selectees=f.addClass("ui-selectee");this._mouseInit();this.helper=e("<div class='ui-selectable-helper'></div>")},destroy:function(){this.selectees.removeClass("ui-selectee").removeData("selectable-item");this.element.removeClass("ui-selectable ui-selectable-disabled").removeData("selectable").unbind(".selectable");this._mouseDestroy();return this},_mouseStart:function(c){var f=this;this.opos=[c.pageX,
c.pageY];if(!this.options.disabled){var d=this.options;this.selectees=e(d.filter,this.element[0]);this._trigger("start",c);e(d.appendTo).append(this.helper);this.helper.css({left:c.clientX,top:c.clientY,width:0,height:0});d.autoRefresh&&this.refresh();this.selectees.filter(".ui-selected").each(function(){var b=e.data(this,"selectable-item");b.startselected=true;if(!c.metaKey){b.$element.removeClass("ui-selected");b.selected=false;b.$element.addClass("ui-unselecting");b.unselecting=true;f._trigger("unselecting",
c,{unselecting:b.element})}});e(c.target).parents().andSelf().each(function(){var b=e.data(this,"selectable-item");if(b){var g=!c.metaKey||!b.$element.hasClass("ui-selected");b.$element.removeClass(g?"ui-unselecting":"ui-selected").addClass(g?"ui-selecting":"ui-unselecting");b.unselecting=!g;b.selecting=g;(b.selected=g)?f._trigger("selecting",c,{selecting:b.element}):f._trigger("unselecting",c,{unselecting:b.element});return false}})}},_mouseDrag:function(c){var f=this;this.dragged=true;if(!this.options.disabled){var d=
this.options,b=this.opos[0],g=this.opos[1],h=c.pageX,i=c.pageY;if(b>h){var j=h;h=b;b=j}if(g>i){j=i;i=g;g=j}this.helper.css({left:b,top:g,width:h-b,height:i-g});this.selectees.each(function(){var a=e.data(this,"selectable-item");if(!(!a||a.element==f.element[0])){var k=false;if(d.tolerance=="touch")k=!(a.left>h||a.right<b||a.top>i||a.bottom<g);else if(d.tolerance=="fit")k=a.left>b&&a.right<h&&a.top>g&&a.bottom<i;if(k){if(a.selected){a.$element.removeClass("ui-selected");a.selected=false}if(a.unselecting){a.$element.removeClass("ui-unselecting");
a.unselecting=false}if(!a.selecting){a.$element.addClass("ui-selecting");a.selecting=true;f._trigger("selecting",c,{selecting:a.element})}}else{if(a.selecting)if(c.metaKey&&a.startselected){a.$element.removeClass("ui-selecting");a.selecting=false;a.$element.addClass("ui-selected");a.selected=true}else{a.$element.removeClass("ui-selecting");a.selecting=false;if(a.startselected){a.$element.addClass("ui-unselecting");a.unselecting=true}f._trigger("unselecting",c,{unselecting:a.element})}if(a.selected)if(!c.metaKey&&
!a.startselected){a.$element.removeClass("ui-selected");a.selected=false;a.$element.addClass("ui-unselecting");a.unselecting=true;f._trigger("unselecting",c,{unselecting:a.element})}}}});return false}},_mouseStop:function(c){var f=this;this.dragged=false;e(".ui-unselecting",this.element[0]).each(function(){var d=e.data(this,"selectable-item");d.$element.removeClass("ui-unselecting");d.unselecting=false;d.startselected=false;f._trigger("unselected",c,{unselected:d.element})});e(".ui-selecting",this.element[0]).each(function(){var d=
e.data(this,"selectable-item");d.$element.removeClass("ui-selecting").addClass("ui-selected");d.selecting=false;d.selected=true;d.startselected=true;f._trigger("selected",c,{selected:d.element})});this._trigger("stop",c);this.helper.remove();return false}});e.extend(e.ui.selectable,{version:"1.8.10"})})(jQuery);
;/*
 * jQuery UI Sortable 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Sortables
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.mouse.js
 *	jquery.ui.widget.js
 */
(function(d){d.widget("ui.sortable",d.ui.mouse,{widgetEventPrefix:"sort",options:{appendTo:"parent",axis:false,connectWith:false,containment:false,cursor:"auto",cursorAt:false,dropOnEmpty:true,forcePlaceholderSize:false,forceHelperSize:false,grid:false,handle:false,helper:"original",items:"> *",opacity:false,placeholder:false,revert:false,scroll:true,scrollSensitivity:20,scrollSpeed:20,scope:"default",tolerance:"intersect",zIndex:1E3},_create:function(){this.containerCache={};this.element.addClass("ui-sortable");
this.refresh();this.floating=this.items.length?/left|right/.test(this.items[0].item.css("float")):false;this.offset=this.element.offset();this._mouseInit()},destroy:function(){this.element.removeClass("ui-sortable ui-sortable-disabled").removeData("sortable").unbind(".sortable");this._mouseDestroy();for(var a=this.items.length-1;a>=0;a--)this.items[a].item.removeData("sortable-item");return this},_setOption:function(a,b){if(a==="disabled"){this.options[a]=b;this.widget()[b?"addClass":"removeClass"]("ui-sortable-disabled")}else d.Widget.prototype._setOption.apply(this,
arguments)},_mouseCapture:function(a,b){if(this.reverting)return false;if(this.options.disabled||this.options.type=="static")return false;this._refreshItems(a);var c=null,e=this;d(a.target).parents().each(function(){if(d.data(this,"sortable-item")==e){c=d(this);return false}});if(d.data(a.target,"sortable-item")==e)c=d(a.target);if(!c)return false;if(this.options.handle&&!b){var f=false;d(this.options.handle,c).find("*").andSelf().each(function(){if(this==a.target)f=true});if(!f)return false}this.currentItem=
c;this._removeCurrentsFromItems();return true},_mouseStart:function(a,b,c){b=this.options;var e=this;this.currentContainer=this;this.refreshPositions();this.helper=this._createHelper(a);this._cacheHelperProportions();this._cacheMargins();this.scrollParent=this.helper.scrollParent();this.offset=this.currentItem.offset();this.offset={top:this.offset.top-this.margins.top,left:this.offset.left-this.margins.left};this.helper.css("position","absolute");this.cssPosition=this.helper.css("position");d.extend(this.offset,
{click:{left:a.pageX-this.offset.left,top:a.pageY-this.offset.top},parent:this._getParentOffset(),relative:this._getRelativeOffset()});this.originalPosition=this._generatePosition(a);this.originalPageX=a.pageX;this.originalPageY=a.pageY;b.cursorAt&&this._adjustOffsetFromHelper(b.cursorAt);this.domPosition={prev:this.currentItem.prev()[0],parent:this.currentItem.parent()[0]};this.helper[0]!=this.currentItem[0]&&this.currentItem.hide();this._createPlaceholder();b.containment&&this._setContainment();
if(b.cursor){if(d("body").css("cursor"))this._storedCursor=d("body").css("cursor");d("body").css("cursor",b.cursor)}if(b.opacity){if(this.helper.css("opacity"))this._storedOpacity=this.helper.css("opacity");this.helper.css("opacity",b.opacity)}if(b.zIndex){if(this.helper.css("zIndex"))this._storedZIndex=this.helper.css("zIndex");this.helper.css("zIndex",b.zIndex)}if(this.scrollParent[0]!=document&&this.scrollParent[0].tagName!="HTML")this.overflowOffset=this.scrollParent.offset();this._trigger("start",
a,this._uiHash());this._preserveHelperProportions||this._cacheHelperProportions();if(!c)for(c=this.containers.length-1;c>=0;c--)this.containers[c]._trigger("activate",a,e._uiHash(this));if(d.ui.ddmanager)d.ui.ddmanager.current=this;d.ui.ddmanager&&!b.dropBehaviour&&d.ui.ddmanager.prepareOffsets(this,a);this.dragging=true;this.helper.addClass("ui-sortable-helper");this._mouseDrag(a);return true},_mouseDrag:function(a){this.position=this._generatePosition(a);this.positionAbs=this._convertPositionTo("absolute");
if(!this.lastPositionAbs)this.lastPositionAbs=this.positionAbs;if(this.options.scroll){var b=this.options,c=false;if(this.scrollParent[0]!=document&&this.scrollParent[0].tagName!="HTML"){if(this.overflowOffset.top+this.scrollParent[0].offsetHeight-a.pageY<b.scrollSensitivity)this.scrollParent[0].scrollTop=c=this.scrollParent[0].scrollTop+b.scrollSpeed;else if(a.pageY-this.overflowOffset.top<b.scrollSensitivity)this.scrollParent[0].scrollTop=c=this.scrollParent[0].scrollTop-b.scrollSpeed;if(this.overflowOffset.left+
this.scrollParent[0].offsetWidth-a.pageX<b.scrollSensitivity)this.scrollParent[0].scrollLeft=c=this.scrollParent[0].scrollLeft+b.scrollSpeed;else if(a.pageX-this.overflowOffset.left<b.scrollSensitivity)this.scrollParent[0].scrollLeft=c=this.scrollParent[0].scrollLeft-b.scrollSpeed}else{if(a.pageY-d(document).scrollTop()<b.scrollSensitivity)c=d(document).scrollTop(d(document).scrollTop()-b.scrollSpeed);else if(d(window).height()-(a.pageY-d(document).scrollTop())<b.scrollSensitivity)c=d(document).scrollTop(d(document).scrollTop()+
b.scrollSpeed);if(a.pageX-d(document).scrollLeft()<b.scrollSensitivity)c=d(document).scrollLeft(d(document).scrollLeft()-b.scrollSpeed);else if(d(window).width()-(a.pageX-d(document).scrollLeft())<b.scrollSensitivity)c=d(document).scrollLeft(d(document).scrollLeft()+b.scrollSpeed)}c!==false&&d.ui.ddmanager&&!b.dropBehaviour&&d.ui.ddmanager.prepareOffsets(this,a)}this.positionAbs=this._convertPositionTo("absolute");if(!this.options.axis||this.options.axis!="y")this.helper[0].style.left=this.position.left+
"px";if(!this.options.axis||this.options.axis!="x")this.helper[0].style.top=this.position.top+"px";for(b=this.items.length-1;b>=0;b--){c=this.items[b];var e=c.item[0],f=this._intersectsWithPointer(c);if(f)if(e!=this.currentItem[0]&&this.placeholder[f==1?"next":"prev"]()[0]!=e&&!d.ui.contains(this.placeholder[0],e)&&(this.options.type=="semi-dynamic"?!d.ui.contains(this.element[0],e):true)){this.direction=f==1?"down":"up";if(this.options.tolerance=="pointer"||this._intersectsWithSides(c))this._rearrange(a,
c);else break;this._trigger("change",a,this._uiHash());break}}this._contactContainers(a);d.ui.ddmanager&&d.ui.ddmanager.drag(this,a);this._trigger("sort",a,this._uiHash());this.lastPositionAbs=this.positionAbs;return false},_mouseStop:function(a,b){if(a){d.ui.ddmanager&&!this.options.dropBehaviour&&d.ui.ddmanager.drop(this,a);if(this.options.revert){var c=this;b=c.placeholder.offset();c.reverting=true;d(this.helper).animate({left:b.left-this.offset.parent.left-c.margins.left+(this.offsetParent[0]==
document.body?0:this.offsetParent[0].scrollLeft),top:b.top-this.offset.parent.top-c.margins.top+(this.offsetParent[0]==document.body?0:this.offsetParent[0].scrollTop)},parseInt(this.options.revert,10)||500,function(){c._clear(a)})}else this._clear(a,b);return false}},cancel:function(){var a=this;if(this.dragging){this._mouseUp({target:null});this.options.helper=="original"?this.currentItem.css(this._storedCSS).removeClass("ui-sortable-helper"):this.currentItem.show();for(var b=this.containers.length-
1;b>=0;b--){this.containers[b]._trigger("deactivate",null,a._uiHash(this));if(this.containers[b].containerCache.over){this.containers[b]._trigger("out",null,a._uiHash(this));this.containers[b].containerCache.over=0}}}if(this.placeholder){this.placeholder[0].parentNode&&this.placeholder[0].parentNode.removeChild(this.placeholder[0]);this.options.helper!="original"&&this.helper&&this.helper[0].parentNode&&this.helper.remove();d.extend(this,{helper:null,dragging:false,reverting:false,_noFinalSort:null});
this.domPosition.prev?d(this.domPosition.prev).after(this.currentItem):d(this.domPosition.parent).prepend(this.currentItem)}return this},serialize:function(a){var b=this._getItemsAsjQuery(a&&a.connected),c=[];a=a||{};d(b).each(function(){var e=(d(a.item||this).attr(a.attribute||"id")||"").match(a.expression||/(.+)[-=_](.+)/);if(e)c.push((a.key||e[1]+"[]")+"="+(a.key&&a.expression?e[1]:e[2]))});!c.length&&a.key&&c.push(a.key+"=");return c.join("&")},toArray:function(a){var b=this._getItemsAsjQuery(a&&
a.connected),c=[];a=a||{};b.each(function(){c.push(d(a.item||this).attr(a.attribute||"id")||"")});return c},_intersectsWith:function(a){var b=this.positionAbs.left,c=b+this.helperProportions.width,e=this.positionAbs.top,f=e+this.helperProportions.height,g=a.left,h=g+a.width,i=a.top,k=i+a.height,j=this.offset.click.top,l=this.offset.click.left;j=e+j>i&&e+j<k&&b+l>g&&b+l<h;return this.options.tolerance=="pointer"||this.options.forcePointerForContainers||this.options.tolerance!="pointer"&&this.helperProportions[this.floating?
"width":"height"]>a[this.floating?"width":"height"]?j:g<b+this.helperProportions.width/2&&c-this.helperProportions.width/2<h&&i<e+this.helperProportions.height/2&&f-this.helperProportions.height/2<k},_intersectsWithPointer:function(a){var b=d.ui.isOverAxis(this.positionAbs.top+this.offset.click.top,a.top,a.height);a=d.ui.isOverAxis(this.positionAbs.left+this.offset.click.left,a.left,a.width);b=b&&a;a=this._getDragVerticalDirection();var c=this._getDragHorizontalDirection();if(!b)return false;return this.floating?
c&&c=="right"||a=="down"?2:1:a&&(a=="down"?2:1)},_intersectsWithSides:function(a){var b=d.ui.isOverAxis(this.positionAbs.top+this.offset.click.top,a.top+a.height/2,a.height);a=d.ui.isOverAxis(this.positionAbs.left+this.offset.click.left,a.left+a.width/2,a.width);var c=this._getDragVerticalDirection(),e=this._getDragHorizontalDirection();return this.floating&&e?e=="right"&&a||e=="left"&&!a:c&&(c=="down"&&b||c=="up"&&!b)},_getDragVerticalDirection:function(){var a=this.positionAbs.top-this.lastPositionAbs.top;
return a!=0&&(a>0?"down":"up")},_getDragHorizontalDirection:function(){var a=this.positionAbs.left-this.lastPositionAbs.left;return a!=0&&(a>0?"right":"left")},refresh:function(a){this._refreshItems(a);this.refreshPositions();return this},_connectWith:function(){var a=this.options;return a.connectWith.constructor==String?[a.connectWith]:a.connectWith},_getItemsAsjQuery:function(a){var b=[],c=[],e=this._connectWith();if(e&&a)for(a=e.length-1;a>=0;a--)for(var f=d(e[a]),g=f.length-1;g>=0;g--){var h=
d.data(f[g],"sortable");if(h&&h!=this&&!h.options.disabled)c.push([d.isFunction(h.options.items)?h.options.items.call(h.element):d(h.options.items,h.element).not(".ui-sortable-helper").not(".ui-sortable-placeholder"),h])}c.push([d.isFunction(this.options.items)?this.options.items.call(this.element,null,{options:this.options,item:this.currentItem}):d(this.options.items,this.element).not(".ui-sortable-helper").not(".ui-sortable-placeholder"),this]);for(a=c.length-1;a>=0;a--)c[a][0].each(function(){b.push(this)});
return d(b)},_removeCurrentsFromItems:function(){for(var a=this.currentItem.find(":data(sortable-item)"),b=0;b<this.items.length;b++)for(var c=0;c<a.length;c++)a[c]==this.items[b].item[0]&&this.items.splice(b,1)},_refreshItems:function(a){this.items=[];this.containers=[this];var b=this.items,c=[[d.isFunction(this.options.items)?this.options.items.call(this.element[0],a,{item:this.currentItem}):d(this.options.items,this.element),this]],e=this._connectWith();if(e)for(var f=e.length-1;f>=0;f--)for(var g=
d(e[f]),h=g.length-1;h>=0;h--){var i=d.data(g[h],"sortable");if(i&&i!=this&&!i.options.disabled){c.push([d.isFunction(i.options.items)?i.options.items.call(i.element[0],a,{item:this.currentItem}):d(i.options.items,i.element),i]);this.containers.push(i)}}for(f=c.length-1;f>=0;f--){a=c[f][1];e=c[f][0];h=0;for(g=e.length;h<g;h++){i=d(e[h]);i.data("sortable-item",a);b.push({item:i,instance:a,width:0,height:0,left:0,top:0})}}},refreshPositions:function(a){if(this.offsetParent&&this.helper)this.offset.parent=
this._getParentOffset();for(var b=this.items.length-1;b>=0;b--){var c=this.items[b],e=this.options.toleranceElement?d(this.options.toleranceElement,c.item):c.item;if(!a){c.width=e.outerWidth();c.height=e.outerHeight()}e=e.offset();c.left=e.left;c.top=e.top}if(this.options.custom&&this.options.custom.refreshContainers)this.options.custom.refreshContainers.call(this);else for(b=this.containers.length-1;b>=0;b--){e=this.containers[b].element.offset();this.containers[b].containerCache.left=e.left;this.containers[b].containerCache.top=
e.top;this.containers[b].containerCache.width=this.containers[b].element.outerWidth();this.containers[b].containerCache.height=this.containers[b].element.outerHeight()}return this},_createPlaceholder:function(a){var b=a||this,c=b.options;if(!c.placeholder||c.placeholder.constructor==String){var e=c.placeholder;c.placeholder={element:function(){var f=d(document.createElement(b.currentItem[0].nodeName)).addClass(e||b.currentItem[0].className+" ui-sortable-placeholder").removeClass("ui-sortable-helper")[0];
if(!e)f.style.visibility="hidden";return f},update:function(f,g){if(!(e&&!c.forcePlaceholderSize)){g.height()||g.height(b.currentItem.innerHeight()-parseInt(b.currentItem.css("paddingTop")||0,10)-parseInt(b.currentItem.css("paddingBottom")||0,10));g.width()||g.width(b.currentItem.innerWidth()-parseInt(b.currentItem.css("paddingLeft")||0,10)-parseInt(b.currentItem.css("paddingRight")||0,10))}}}}b.placeholder=d(c.placeholder.element.call(b.element,b.currentItem));b.currentItem.after(b.placeholder);
c.placeholder.update(b,b.placeholder)},_contactContainers:function(a){for(var b=null,c=null,e=this.containers.length-1;e>=0;e--)if(!d.ui.contains(this.currentItem[0],this.containers[e].element[0]))if(this._intersectsWith(this.containers[e].containerCache)){if(!(b&&d.ui.contains(this.containers[e].element[0],b.element[0]))){b=this.containers[e];c=e}}else if(this.containers[e].containerCache.over){this.containers[e]._trigger("out",a,this._uiHash(this));this.containers[e].containerCache.over=0}if(b)if(this.containers.length===
1){this.containers[c]._trigger("over",a,this._uiHash(this));this.containers[c].containerCache.over=1}else if(this.currentContainer!=this.containers[c]){b=1E4;e=null;for(var f=this.positionAbs[this.containers[c].floating?"left":"top"],g=this.items.length-1;g>=0;g--)if(d.ui.contains(this.containers[c].element[0],this.items[g].item[0])){var h=this.items[g][this.containers[c].floating?"left":"top"];if(Math.abs(h-f)<b){b=Math.abs(h-f);e=this.items[g]}}if(e||this.options.dropOnEmpty){this.currentContainer=
this.containers[c];e?this._rearrange(a,e,null,true):this._rearrange(a,null,this.containers[c].element,true);this._trigger("change",a,this._uiHash());this.containers[c]._trigger("change",a,this._uiHash(this));this.options.placeholder.update(this.currentContainer,this.placeholder);this.containers[c]._trigger("over",a,this._uiHash(this));this.containers[c].containerCache.over=1}}},_createHelper:function(a){var b=this.options;a=d.isFunction(b.helper)?d(b.helper.apply(this.element[0],[a,this.currentItem])):
b.helper=="clone"?this.currentItem.clone():this.currentItem;a.parents("body").length||d(b.appendTo!="parent"?b.appendTo:this.currentItem[0].parentNode)[0].appendChild(a[0]);if(a[0]==this.currentItem[0])this._storedCSS={width:this.currentItem[0].style.width,height:this.currentItem[0].style.height,position:this.currentItem.css("position"),top:this.currentItem.css("top"),left:this.currentItem.css("left")};if(a[0].style.width==""||b.forceHelperSize)a.width(this.currentItem.width());if(a[0].style.height==
""||b.forceHelperSize)a.height(this.currentItem.height());return a},_adjustOffsetFromHelper:function(a){if(typeof a=="string")a=a.split(" ");if(d.isArray(a))a={left:+a[0],top:+a[1]||0};if("left"in a)this.offset.click.left=a.left+this.margins.left;if("right"in a)this.offset.click.left=this.helperProportions.width-a.right+this.margins.left;if("top"in a)this.offset.click.top=a.top+this.margins.top;if("bottom"in a)this.offset.click.top=this.helperProportions.height-a.bottom+this.margins.top},_getParentOffset:function(){this.offsetParent=
this.helper.offsetParent();var a=this.offsetParent.offset();if(this.cssPosition=="absolute"&&this.scrollParent[0]!=document&&d.ui.contains(this.scrollParent[0],this.offsetParent[0])){a.left+=this.scrollParent.scrollLeft();a.top+=this.scrollParent.scrollTop()}if(this.offsetParent[0]==document.body||this.offsetParent[0].tagName&&this.offsetParent[0].tagName.toLowerCase()=="html"&&d.browser.msie)a={top:0,left:0};return{top:a.top+(parseInt(this.offsetParent.css("borderTopWidth"),10)||0),left:a.left+(parseInt(this.offsetParent.css("borderLeftWidth"),
10)||0)}},_getRelativeOffset:function(){if(this.cssPosition=="relative"){var a=this.currentItem.position();return{top:a.top-(parseInt(this.helper.css("top"),10)||0)+this.scrollParent.scrollTop(),left:a.left-(parseInt(this.helper.css("left"),10)||0)+this.scrollParent.scrollLeft()}}else return{top:0,left:0}},_cacheMargins:function(){this.margins={left:parseInt(this.currentItem.css("marginLeft"),10)||0,top:parseInt(this.currentItem.css("marginTop"),10)||0}},_cacheHelperProportions:function(){this.helperProportions=
{width:this.helper.outerWidth(),height:this.helper.outerHeight()}},_setContainment:function(){var a=this.options;if(a.containment=="parent")a.containment=this.helper[0].parentNode;if(a.containment=="document"||a.containment=="window")this.containment=[0-this.offset.relative.left-this.offset.parent.left,0-this.offset.relative.top-this.offset.parent.top,d(a.containment=="document"?document:window).width()-this.helperProportions.width-this.margins.left,(d(a.containment=="document"?document:window).height()||
document.body.parentNode.scrollHeight)-this.helperProportions.height-this.margins.top];if(!/^(document|window|parent)$/.test(a.containment)){var b=d(a.containment)[0];a=d(a.containment).offset();var c=d(b).css("overflow")!="hidden";this.containment=[a.left+(parseInt(d(b).css("borderLeftWidth"),10)||0)+(parseInt(d(b).css("paddingLeft"),10)||0)-this.margins.left,a.top+(parseInt(d(b).css("borderTopWidth"),10)||0)+(parseInt(d(b).css("paddingTop"),10)||0)-this.margins.top,a.left+(c?Math.max(b.scrollWidth,
b.offsetWidth):b.offsetWidth)-(parseInt(d(b).css("borderLeftWidth"),10)||0)-(parseInt(d(b).css("paddingRight"),10)||0)-this.helperProportions.width-this.margins.left,a.top+(c?Math.max(b.scrollHeight,b.offsetHeight):b.offsetHeight)-(parseInt(d(b).css("borderTopWidth"),10)||0)-(parseInt(d(b).css("paddingBottom"),10)||0)-this.helperProportions.height-this.margins.top]}},_convertPositionTo:function(a,b){if(!b)b=this.position;a=a=="absolute"?1:-1;var c=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=
document&&d.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,e=/(html|body)/i.test(c[0].tagName);return{top:b.top+this.offset.relative.top*a+this.offset.parent.top*a-(d.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollTop():e?0:c.scrollTop())*a),left:b.left+this.offset.relative.left*a+this.offset.parent.left*a-(d.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():
e?0:c.scrollLeft())*a)}},_generatePosition:function(a){var b=this.options,c=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&d.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,e=/(html|body)/i.test(c[0].tagName);if(this.cssPosition=="relative"&&!(this.scrollParent[0]!=document&&this.scrollParent[0]!=this.offsetParent[0]))this.offset.relative=this._getRelativeOffset();var f=a.pageX,g=a.pageY;if(this.originalPosition){if(this.containment){if(a.pageX-
this.offset.click.left<this.containment[0])f=this.containment[0]+this.offset.click.left;if(a.pageY-this.offset.click.top<this.containment[1])g=this.containment[1]+this.offset.click.top;if(a.pageX-this.offset.click.left>this.containment[2])f=this.containment[2]+this.offset.click.left;if(a.pageY-this.offset.click.top>this.containment[3])g=this.containment[3]+this.offset.click.top}if(b.grid){g=this.originalPageY+Math.round((g-this.originalPageY)/b.grid[1])*b.grid[1];g=this.containment?!(g-this.offset.click.top<
this.containment[1]||g-this.offset.click.top>this.containment[3])?g:!(g-this.offset.click.top<this.containment[1])?g-b.grid[1]:g+b.grid[1]:g;f=this.originalPageX+Math.round((f-this.originalPageX)/b.grid[0])*b.grid[0];f=this.containment?!(f-this.offset.click.left<this.containment[0]||f-this.offset.click.left>this.containment[2])?f:!(f-this.offset.click.left<this.containment[0])?f-b.grid[0]:f+b.grid[0]:f}}return{top:g-this.offset.click.top-this.offset.relative.top-this.offset.parent.top+(d.browser.safari&&
this.cssPosition=="fixed"?0:this.cssPosition=="fixed"?-this.scrollParent.scrollTop():e?0:c.scrollTop()),left:f-this.offset.click.left-this.offset.relative.left-this.offset.parent.left+(d.browser.safari&&this.cssPosition=="fixed"?0:this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():e?0:c.scrollLeft())}},_rearrange:function(a,b,c,e){c?c[0].appendChild(this.placeholder[0]):b.item[0].parentNode.insertBefore(this.placeholder[0],this.direction=="down"?b.item[0]:b.item[0].nextSibling);this.counter=
this.counter?++this.counter:1;var f=this,g=this.counter;window.setTimeout(function(){g==f.counter&&f.refreshPositions(!e)},0)},_clear:function(a,b){this.reverting=false;var c=[];!this._noFinalSort&&this.currentItem[0].parentNode&&this.placeholder.before(this.currentItem);this._noFinalSort=null;if(this.helper[0]==this.currentItem[0]){for(var e in this._storedCSS)if(this._storedCSS[e]=="auto"||this._storedCSS[e]=="static")this._storedCSS[e]="";this.currentItem.css(this._storedCSS).removeClass("ui-sortable-helper")}else this.currentItem.show();
this.fromOutside&&!b&&c.push(function(f){this._trigger("receive",f,this._uiHash(this.fromOutside))});if((this.fromOutside||this.domPosition.prev!=this.currentItem.prev().not(".ui-sortable-helper")[0]||this.domPosition.parent!=this.currentItem.parent()[0])&&!b)c.push(function(f){this._trigger("update",f,this._uiHash())});if(!d.ui.contains(this.element[0],this.currentItem[0])){b||c.push(function(f){this._trigger("remove",f,this._uiHash())});for(e=this.containers.length-1;e>=0;e--)if(d.ui.contains(this.containers[e].element[0],
this.currentItem[0])&&!b){c.push(function(f){return function(g){f._trigger("receive",g,this._uiHash(this))}}.call(this,this.containers[e]));c.push(function(f){return function(g){f._trigger("update",g,this._uiHash(this))}}.call(this,this.containers[e]))}}for(e=this.containers.length-1;e>=0;e--){b||c.push(function(f){return function(g){f._trigger("deactivate",g,this._uiHash(this))}}.call(this,this.containers[e]));if(this.containers[e].containerCache.over){c.push(function(f){return function(g){f._trigger("out",
g,this._uiHash(this))}}.call(this,this.containers[e]));this.containers[e].containerCache.over=0}}this._storedCursor&&d("body").css("cursor",this._storedCursor);this._storedOpacity&&this.helper.css("opacity",this._storedOpacity);if(this._storedZIndex)this.helper.css("zIndex",this._storedZIndex=="auto"?"":this._storedZIndex);this.dragging=false;if(this.cancelHelperRemoval){if(!b){this._trigger("beforeStop",a,this._uiHash());for(e=0;e<c.length;e++)c[e].call(this,a);this._trigger("stop",a,this._uiHash())}return false}b||
this._trigger("beforeStop",a,this._uiHash());this.placeholder[0].parentNode.removeChild(this.placeholder[0]);this.helper[0]!=this.currentItem[0]&&this.helper.remove();this.helper=null;if(!b){for(e=0;e<c.length;e++)c[e].call(this,a);this._trigger("stop",a,this._uiHash())}this.fromOutside=false;return true},_trigger:function(){d.Widget.prototype._trigger.apply(this,arguments)===false&&this.cancel()},_uiHash:function(a){var b=a||this;return{helper:b.helper,placeholder:b.placeholder||d([]),position:b.position,
originalPosition:b.originalPosition,offset:b.positionAbs,item:b.currentItem,sender:a?a.element:null}}});d.extend(d.ui.sortable,{version:"1.8.10"})})(jQuery);
;/*
 * jQuery UI Dialog 1.8.10
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Dialog
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 *  jquery.ui.button.js
 *	jquery.ui.draggable.js
 *	jquery.ui.mouse.js
 *	jquery.ui.position.js
 *	jquery.ui.resizable.js
 */
(function(c,j){var k={buttons:true,height:true,maxHeight:true,maxWidth:true,minHeight:true,minWidth:true,width:true},l={maxHeight:true,maxWidth:true,minHeight:true,minWidth:true};c.widget("ui.dialog",{options:{autoOpen:true,buttons:{},closeOnEscape:true,closeText:"close",dialogClass:"",draggable:true,hide:null,height:"auto",maxHeight:false,maxWidth:false,minHeight:150,minWidth:150,modal:false,position:{my:"center",at:"center",collision:"fit",using:function(a){var b=c(this).css(a).offset().top;b<0&&
c(this).css("top",a.top-b)}},resizable:true,show:null,stack:true,title:"",width:300,zIndex:1E3},_create:function(){this.originalTitle=this.element.attr("title");if(typeof this.originalTitle!=="string")this.originalTitle="";this.options.title=this.options.title||this.originalTitle;var a=this,b=a.options,d=b.title||"&#160;",e=c.ui.dialog.getTitleId(a.element),g=(a.uiDialog=c("<div></div>")).appendTo(document.body).hide().addClass("ui-dialog ui-widget ui-widget-content ui-corner-all "+b.dialogClass).css({zIndex:b.zIndex}).attr("tabIndex",
-1).css("outline",0).keydown(function(i){if(b.closeOnEscape&&i.keyCode&&i.keyCode===c.ui.keyCode.ESCAPE){a.close(i);i.preventDefault()}}).attr({role:"dialog","aria-labelledby":e}).mousedown(function(i){a.moveToTop(false,i)});a.element.show().removeAttr("title").addClass("ui-dialog-content ui-widget-content").appendTo(g);var f=(a.uiDialogTitlebar=c("<div></div>")).addClass("ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix").prependTo(g),h=c('<a href="#"></a>').addClass("ui-dialog-titlebar-close ui-corner-all").attr("role",
"button").hover(function(){h.addClass("ui-state-hover")},function(){h.removeClass("ui-state-hover")}).focus(function(){h.addClass("ui-state-focus")}).blur(function(){h.removeClass("ui-state-focus")}).click(function(i){a.close(i);return false}).appendTo(f);(a.uiDialogTitlebarCloseText=c("<span></span>")).addClass("ui-icon ui-icon-closethick").text(b.closeText).appendTo(h);c("<span></span>").addClass("ui-dialog-title").attr("id",e).html(d).prependTo(f);if(c.isFunction(b.beforeclose)&&!c.isFunction(b.beforeClose))b.beforeClose=
b.beforeclose;f.find("*").add(f).disableSelection();b.draggable&&c.fn.draggable&&a._makeDraggable();b.resizable&&c.fn.resizable&&a._makeResizable();a._createButtons(b.buttons);a._isOpen=false;c.fn.bgiframe&&g.bgiframe()},_init:function(){this.options.autoOpen&&this.open()},destroy:function(){var a=this;a.overlay&&a.overlay.destroy();a.uiDialog.hide();a.element.unbind(".dialog").removeData("dialog").removeClass("ui-dialog-content ui-widget-content").hide().appendTo("body");a.uiDialog.remove();a.originalTitle&&
a.element.attr("title",a.originalTitle);return a},widget:function(){return this.uiDialog},close:function(a){var b=this,d,e;if(false!==b._trigger("beforeClose",a)){b.overlay&&b.overlay.destroy();b.uiDialog.unbind("keypress.ui-dialog");b._isOpen=false;if(b.options.hide)b.uiDialog.hide(b.options.hide,function(){b._trigger("close",a)});else{b.uiDialog.hide();b._trigger("close",a)}c.ui.dialog.overlay.resize();if(b.options.modal){d=0;c(".ui-dialog").each(function(){if(this!==b.uiDialog[0]){e=c(this).css("z-index");
isNaN(e)||(d=Math.max(d,e))}});c.ui.dialog.maxZ=d}return b}},isOpen:function(){return this._isOpen},moveToTop:function(a,b){var d=this,e=d.options;if(e.modal&&!a||!e.stack&&!e.modal)return d._trigger("focus",b);if(e.zIndex>c.ui.dialog.maxZ)c.ui.dialog.maxZ=e.zIndex;if(d.overlay){c.ui.dialog.maxZ+=1;d.overlay.$el.css("z-index",c.ui.dialog.overlay.maxZ=c.ui.dialog.maxZ)}a={scrollTop:d.element.attr("scrollTop"),scrollLeft:d.element.attr("scrollLeft")};c.ui.dialog.maxZ+=1;d.uiDialog.css("z-index",c.ui.dialog.maxZ);
d.element.attr(a);d._trigger("focus",b);return d},open:function(){if(!this._isOpen){var a=this,b=a.options,d=a.uiDialog;a.overlay=b.modal?new c.ui.dialog.overlay(a):null;a._size();a._position(b.position);d.show(b.show);a.moveToTop(true);b.modal&&d.bind("keypress.ui-dialog",function(e){if(e.keyCode===c.ui.keyCode.TAB){var g=c(":tabbable",this),f=g.filter(":first");g=g.filter(":last");if(e.target===g[0]&&!e.shiftKey){f.focus(1);return false}else if(e.target===f[0]&&e.shiftKey){g.focus(1);return false}}});
c(a.element.find(":tabbable").get().concat(d.find(".ui-dialog-buttonpane :tabbable").get().concat(d.get()))).eq(0).focus();a._isOpen=true;a._trigger("open");return a}},_createButtons:function(a){var b=this,d=false,e=c("<div></div>").addClass("ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"),g=c("<div></div>").addClass("ui-dialog-buttonset").appendTo(e);b.uiDialog.find(".ui-dialog-buttonpane").remove();typeof a==="object"&&a!==null&&c.each(a,function(){return!(d=true)});if(d){c.each(a,function(f,
h){h=c.isFunction(h)?{click:h,text:f}:h;f=c('<button type="button"></button>').attr(h,true).unbind("click").click(function(){h.click.apply(b.element[0],arguments)}).appendTo(g);c.fn.button&&f.button()});e.appendTo(b.uiDialog)}},_makeDraggable:function(){function a(f){return{position:f.position,offset:f.offset}}var b=this,d=b.options,e=c(document),g;b.uiDialog.draggable({cancel:".ui-dialog-content, .ui-dialog-titlebar-close",handle:".ui-dialog-titlebar",containment:"document",start:function(f,h){g=
d.height==="auto"?"auto":c(this).height();c(this).height(c(this).height()).addClass("ui-dialog-dragging");b._trigger("dragStart",f,a(h))},drag:function(f,h){b._trigger("drag",f,a(h))},stop:function(f,h){d.position=[h.position.left-e.scrollLeft(),h.position.top-e.scrollTop()];c(this).removeClass("ui-dialog-dragging").height(g);b._trigger("dragStop",f,a(h));c.ui.dialog.overlay.resize()}})},_makeResizable:function(a){function b(f){return{originalPosition:f.originalPosition,originalSize:f.originalSize,
position:f.position,size:f.size}}a=a===j?this.options.resizable:a;var d=this,e=d.options,g=d.uiDialog.css("position");a=typeof a==="string"?a:"n,e,s,w,se,sw,ne,nw";d.uiDialog.resizable({cancel:".ui-dialog-content",containment:"document",alsoResize:d.element,maxWidth:e.maxWidth,maxHeight:e.maxHeight,minWidth:e.minWidth,minHeight:d._minHeight(),handles:a,start:function(f,h){c(this).addClass("ui-dialog-resizing");d._trigger("resizeStart",f,b(h))},resize:function(f,h){d._trigger("resize",f,b(h))},stop:function(f,
h){c(this).removeClass("ui-dialog-resizing");e.height=c(this).height();e.width=c(this).width();d._trigger("resizeStop",f,b(h));c.ui.dialog.overlay.resize()}}).css("position",g).find(".ui-resizable-se").addClass("ui-icon ui-icon-grip-diagonal-se")},_minHeight:function(){var a=this.options;return a.height==="auto"?a.minHeight:Math.min(a.minHeight,a.height)},_position:function(a){var b=[],d=[0,0],e;if(a){if(typeof a==="string"||typeof a==="object"&&"0"in a){b=a.split?a.split(" "):[a[0],a[1]];if(b.length===
1)b[1]=b[0];c.each(["left","top"],function(g,f){if(+b[g]===b[g]){d[g]=b[g];b[g]=f}});a={my:b.join(" "),at:b.join(" "),offset:d.join(" ")}}a=c.extend({},c.ui.dialog.prototype.options.position,a)}else a=c.ui.dialog.prototype.options.position;(e=this.uiDialog.is(":visible"))||this.uiDialog.show();this.uiDialog.css({top:0,left:0}).position(c.extend({of:window},a));e||this.uiDialog.hide()},_setOptions:function(a){var b=this,d={},e=false;c.each(a,function(g,f){b._setOption(g,f);if(g in k)e=true;if(g in
l)d[g]=f});e&&this._size();this.uiDialog.is(":data(resizable)")&&this.uiDialog.resizable("option",d)},_setOption:function(a,b){var d=this,e=d.uiDialog;switch(a){case "beforeclose":a="beforeClose";break;case "buttons":d._createButtons(b);break;case "closeText":d.uiDialogTitlebarCloseText.text(""+b);break;case "dialogClass":e.removeClass(d.options.dialogClass).addClass("ui-dialog ui-widget ui-widget-content ui-corner-all "+b);break;case "disabled":b?e.addClass("ui-dialog-disabled"):e.removeClass("ui-dialog-disabled");
break;case "draggable":var g=e.is(":data(draggable)");g&&!b&&e.draggable("destroy");!g&&b&&d._makeDraggable();break;case "position":d._position(b);break;case "resizable":(g=e.is(":data(resizable)"))&&!b&&e.resizable("destroy");g&&typeof b==="string"&&e.resizable("option","handles",b);!g&&b!==false&&d._makeResizable(b);break;case "title":c(".ui-dialog-title",d.uiDialogTitlebar).html(""+(b||"&#160;"));break}c.Widget.prototype._setOption.apply(d,arguments)},_size:function(){var a=this.options,b,d,e=
this.uiDialog.is(":visible");this.element.show().css({width:"auto",minHeight:0,height:0});if(a.minWidth>a.width)a.width=a.minWidth;b=this.uiDialog.css({height:"auto",width:a.width}).height();d=Math.max(0,a.minHeight-b);if(a.height==="auto")if(c.support.minHeight)this.element.css({minHeight:d,height:"auto"});else{this.uiDialog.show();a=this.element.css("height","auto").height();e||this.uiDialog.hide();this.element.height(Math.max(a,d))}else this.element.height(Math.max(a.height-b,0));this.uiDialog.is(":data(resizable)")&&
this.uiDialog.resizable("option","minHeight",this._minHeight())}});c.extend(c.ui.dialog,{version:"1.8.10",uuid:0,maxZ:0,getTitleId:function(a){a=a.attr("id");if(!a){this.uuid+=1;a=this.uuid}return"ui-dialog-title-"+a},overlay:function(a){this.$el=c.ui.dialog.overlay.create(a)}});c.extend(c.ui.dialog.overlay,{instances:[],oldInstances:[],maxZ:0,events:c.map("focus,mousedown,mouseup,keydown,keypress,click".split(","),function(a){return a+".dialog-overlay"}).join(" "),create:function(a){if(this.instances.length===
0){setTimeout(function(){c.ui.dialog.overlay.instances.length&&c(document).bind(c.ui.dialog.overlay.events,function(d){if(c(d.target).zIndex()<c.ui.dialog.overlay.maxZ)return false})},1);c(document).bind("keydown.dialog-overlay",function(d){if(a.options.closeOnEscape&&d.keyCode&&d.keyCode===c.ui.keyCode.ESCAPE){a.close(d);d.preventDefault()}});c(window).bind("resize.dialog-overlay",c.ui.dialog.overlay.resize)}var b=(this.oldInstances.pop()||c("<div></div>").addClass("ui-widget-overlay")).appendTo(document.body).css({width:this.width(),
height:this.height()});c.fn.bgiframe&&b.bgiframe();this.instances.push(b);return b},destroy:function(a){var b=c.inArray(a,this.instances);b!=-1&&this.oldInstances.push(this.instances.splice(b,1)[0]);this.instances.length===0&&c([document,window]).unbind(".dialog-overlay");a.remove();var d=0;c.each(this.instances,function(){d=Math.max(d,this.css("z-index"))});this.maxZ=d},height:function(){var a,b;if(c.browser.msie&&c.browser.version<7){a=Math.max(document.documentElement.scrollHeight,document.body.scrollHeight);
b=Math.max(document.documentElement.offsetHeight,document.body.offsetHeight);return a<b?c(window).height()+"px":a+"px"}else return c(document).height()+"px"},width:function(){var a,b;if(c.browser.msie&&c.browser.version<7){a=Math.max(document.documentElement.scrollWidth,document.body.scrollWidth);b=Math.max(document.documentElement.offsetWidth,document.body.offsetWidth);return a<b?c(window).width()+"px":a+"px"}else return c(document).width()+"px"},resize:function(){var a=c([]);c.each(c.ui.dialog.overlay.instances,
function(){a=a.add(this)});a.css({width:0,height:0}).css({width:c.ui.dialog.overlay.width(),height:c.ui.dialog.overlay.height()})}});c.extend(c.ui.dialog.overlay.prototype,{destroy:function(){c.ui.dialog.overlay.destroy(this.$el)}})})(jQuery);
;
/*
 * jQuery UI Datepicker @VERSION
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Datepicker
 *
 * Depends:
 *	jquery.ui.core.js
 */

/**
 * @note OPENGAMMA EDITS
 * @note Added in 'displayOnly' option to turn off interactivity in the calendar.
 * @note Essentially turns it into a display calendar not a date picker.
 */

(function( $, undefined ) {

$.extend($.ui, { datepicker: { version: "@VERSION" } });

var PROP_NAME = 'datepicker';
var dpuuid = new Date().getTime();

/* Date picker manager.
   Use the singleton instance of this class, $.datepicker, to interact with the date picker.
   Settings for (groups of) date pickers are maintained in an instance object,
   allowing multiple different settings on the same page. */

function Datepicker() {
	this.debug = false; // Change this to true to start debugging
	this._curInst = null; // The current instance in use
	this._keyEvent = false; // If the last event was a key event
	this._disabledInputs = []; // List of date picker inputs that have been disabled
	this._datepickerShowing = false; // True if the popup picker is showing , false if not
	this._inDialog = false; // True if showing within a "dialog", false if not
	this._mainDivId = 'ui-datepicker-div'; // The ID of the main datepicker division
	this._inlineClass = 'ui-datepicker-inline'; // The name of the inline marker class
	this._appendClass = 'ui-datepicker-append'; // The name of the append marker class
	this._triggerClass = 'ui-datepicker-trigger'; // The name of the trigger marker class
	this._dialogClass = 'ui-datepicker-dialog'; // The name of the dialog marker class
	this._disableClass = 'ui-datepicker-disabled'; // The name of the disabled covering marker class
	this._unselectableClass = 'ui-datepicker-unselectable'; // The name of the unselectable cell marker class
	this._currentClass = 'ui-datepicker-current-day'; // The name of the current day marker class
	this._dayOverClass = 'ui-datepicker-days-cell-over'; // The name of the day hover marker class
	this.regional = []; // Available regional settings, indexed by language code
	this.regional[''] = { // Default regional settings
		closeText: 'Done', // Display text for close link
		prevText: 'Prev', // Display text for previous month link
		nextText: 'Next', // Display text for next month link
		currentText: 'Today', // Display text for current month link
		monthNames: ['January','February','March','April','May','June',
			'July','August','September','October','November','December'], // Names of months for drop-down and formatting
		monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'], // For formatting
		dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'], // For formatting
		dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'], // For formatting
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'], // Column headings for days starting at Sunday
		weekHeader: 'Wk', // Column header for week of the year
		dateFormat: 'mm/dd/yy', // See format options on parseDate
		firstDay: 0, // The first day of the week, Sun = 0, Mon = 1, ...
		isRTL: false, // True if right-to-left language, false if left-to-right
		showMonthAfterYear: false, // True if the year select precedes month, false for month then year
		yearSuffix: '' // Additional text to append to the year in the month headers
	};
	this._defaults = { // Global defaults for all the date picker instances
		showOn: 'focus', // 'focus' for popup on focus,
			// 'button' for trigger button, or 'both' for either
		showAnim: 'fadeIn', // Name of jQuery animation for popup
		showOptions: {}, // Options for enhanced animations
		defaultDate: null, // Used when field is blank: actual date,
			// +/-number for offset from today, null for today
		appendText: '', // Display text following the input box, e.g. showing the format
		buttonText: '...', // Text for trigger button
		buttonImage: '', // URL for trigger button image
		buttonImageOnly: false, // True if the image appears alone, false if it appears on a button
		hideIfNoPrevNext: false, // True to hide next/previous month links
			// if not applicable, false to just disable them
		navigationAsDateFormat: false, // True if date formatting applied to prev/today/next links
		gotoCurrent: false, // True if today link goes back to current selection instead
		changeMonth: false, // True if month can be selected directly, false if only prev/next
		changeYear: false, // True if year can be selected directly, false if only prev/next
		yearRange: 'c-10:c+10', // Range of years to display in drop-down,
			// either relative to today's year (-nn:+nn), relative to currently displayed year
			// (c-nn:c+nn), absolute (nnnn:nnnn), or a combination of the above (nnnn:-n)
		showOtherMonths: false, // True to show dates in other months, false to leave blank
		selectOtherMonths: false, // True to allow selection of dates in other months, false for unselectable
		showWeek: false, // True to show week of the year, false to not show it
		calculateWeek: this.iso8601Week, // How to calculate the week of the year,
			// takes a Date and returns the number of the week for it
		shortYearCutoff: '+10', // Short year values < this are in the current century,
			// > this are in the previous century,
			// string value starting with '+' for current year + value
		minDate: null, // The earliest selectable date, or null for no limit
		maxDate: null, // The latest selectable date, or null for no limit
		duration: 'fast', // Duration of display/closure
		beforeShowDay: null, // Function that takes a date and returns an array with
			// [0] = true if selectable, false if not, [1] = custom CSS class name(s) or '',
			// [2] = cell title (optional), e.g. $.datepicker.noWeekends
		beforeShow: null, // Function that takes an input field and
			// returns a set of custom settings for the date picker
		onSelect: null, // Define a callback function when a date is selected
		onChangeMonthYear: null, // Define a callback function when the month or year is changed
		onClose: null, // Define a callback function when the datepicker is closed
		numberOfMonths: 1, // Number of months to show at a time
		showCurrentAtPos: 0, // The position in multipe months at which to show the current month (starting at 0)
		stepMonths: 1, // Number of months to step back/forward
		stepBigMonths: 12, // Number of months to step back/forward for the big links
		altField: '', // Selector for an alternate field to store selected dates into
		altFormat: '', // The date format to use for the alternate field
		constrainInput: true, // The input is constrained by the current date format
		showButtonPanel: false, // True to show button panel, false to not show it
        autoSize: false, // True to size the input for the date format, false to leave as is
        displayOnly: false, // True to remove interactivity from the calendar dates (OG CUSTOM)
        specialDates: null // Object that contains special dates to highlight, for example bank holidays (OG CUSTOM)
	};
	$.extend(this._defaults, this.regional['']);
	this.dpDiv = $('<div id="' + this._mainDivId + '" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all"></div>');
}

$.extend(Datepicker.prototype, {
	/* Class name added to elements to indicate already configured with a date picker. */
	markerClassName: 'hasDatepicker',

	/* Debug logging (if enabled). */
	log: function () {
		if (this.debug)
			console.log.apply('', arguments);
	},

	// TODO rename to "widget" when switching to widget factory
	_widgetDatepicker: function() {
		return this.dpDiv;
	},

	/* Override the default settings for all instances of the date picker.
	   @param  settings  object - the new settings to use as defaults (anonymous object)
	   @return the manager object */
	setDefaults: function(settings) {
		extendRemove(this._defaults, settings || {});
		return this;
	},

	/* Attach the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span
	   @param  settings  object - the new settings to use for this date picker instance (anonymous) */
	_attachDatepicker: function(target, settings) {
		// check for settings on the control itself - in namespace 'date:'
		var inlineSettings = null;
		for (var attrName in this._defaults) {
			var attrValue = target.getAttribute('date:' + attrName);
			if (attrValue) {
				inlineSettings = inlineSettings || {};
				try {
					inlineSettings[attrName] = eval(attrValue);
				} catch (err) {
					inlineSettings[attrName] = attrValue;
				}
			}
		}
		var nodeName = target.nodeName.toLowerCase();
		var inline = (nodeName == 'div' || nodeName == 'span');
		if (!target.id) {
			this.uuid += 1;
			target.id = 'dp' + this.uuid;
		}
		var inst = this._newInst($(target), inline);
		inst.settings = $.extend({}, settings || {}, inlineSettings || {});
		if (nodeName == 'input') {
			this._connectDatepicker(target, inst);
		} else if (inline) {
			this._inlineDatepicker(target, inst);
		}
	},

	/* Create a new instance object. */
	_newInst: function(target, inline) {
		var id = target[0].id.replace(/([^A-Za-z0-9_-])/g, '\\\\$1'); // escape jQuery meta chars
		return {id: id, input: target, // associated target
			selectedDay: 0, selectedMonth: 0, selectedYear: 0, // current selection
			drawMonth: 0, drawYear: 0, // month being drawn
			inline: inline, // is datepicker inline or not
			dpDiv: (!inline ? this.dpDiv : // presentation div
			$('<div class="' + this._inlineClass + ' ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all"></div>'))};
	},

	/* Attach the date picker to an input field. */
	_connectDatepicker: function(target, inst) {
		var input = $(target);
		inst.append = $([]);
		inst.trigger = $([]);
		if (input.hasClass(this.markerClassName))
			return;
		this._attachments(input, inst);
		input.addClass(this.markerClassName).keydown(this._doKeyDown).
			keypress(this._doKeyPress).keyup(this._doKeyUp).
			bind("setData.datepicker", function(event, key, value) {
				inst.settings[key] = value;
			}).bind("getData.datepicker", function(event, key) {
				return this._get(inst, key);
			});
		this._autoSize(inst);
		$.data(target, PROP_NAME, inst);
	},

	/* Make attachments based on settings. */
	_attachments: function(input, inst) {
		var appendText = this._get(inst, 'appendText');
		var isRTL = this._get(inst, 'isRTL');
		if (inst.append)
			inst.append.remove();
		if (appendText) {
			inst.append = $('<span class="' + this._appendClass + '">' + appendText + '</span>');
			input[isRTL ? 'before' : 'after'](inst.append);
		}
		input.unbind('focus', this._showDatepicker);
		if (inst.trigger)
			inst.trigger.remove();
		var showOn = this._get(inst, 'showOn');
		if (showOn == 'focus' || showOn == 'both') // pop-up date picker when in the marked field
			input.focus(this._showDatepicker);
		if (showOn == 'button' || showOn == 'both') { // pop-up date picker when button clicked
			var buttonText = this._get(inst, 'buttonText');
			var buttonImage = this._get(inst, 'buttonImage');
			inst.trigger = $(this._get(inst, 'buttonImageOnly') ?
				$('<img/>').addClass(this._triggerClass).
					attr({ src: buttonImage, alt: buttonText, title: buttonText }) :
				$('<button type="button"></button>').addClass(this._triggerClass).
					html(buttonImage == '' ? buttonText : $('<img/>').attr(
					{ src:buttonImage, alt:buttonText, title:buttonText })));
			input[isRTL ? 'before' : 'after'](inst.trigger);
			inst.trigger.click(function() {
				if ($.datepicker._datepickerShowing && $.datepicker._lastInput == input[0])
					$.datepicker._hideDatepicker();
				else
					$.datepicker._showDatepicker(input[0]);
				return false;
			});
		}
	},

	/* Apply the maximum length for the date format. */
	_autoSize: function(inst) {
		if (this._get(inst, 'autoSize') && !inst.inline) {
			var date = new Date(2009, 12 - 1, 20); // Ensure double digits
			var dateFormat = this._get(inst, 'dateFormat');
			if (dateFormat.match(/[DM]/)) {
				var findMax = function(names) {
					var max = 0;
					var maxI = 0;
					for (var i = 0; i < names.length; i++) {
						if (names[i].length > max) {
							max = names[i].length;
							maxI = i;
						}
					}
					return maxI;
				};
				date.setMonth(findMax(this._get(inst, (dateFormat.match(/MM/) ?
					'monthNames' : 'monthNamesShort'))));
				date.setDate(findMax(this._get(inst, (dateFormat.match(/DD/) ?
					'dayNames' : 'dayNamesShort'))) + 20 - date.getDay());
			}
			inst.input.attr('size', this._formatDate(inst, date).length);
		}
	},

	/* Attach an inline date picker to a div. */
	_inlineDatepicker: function(target, inst) {
		var divSpan = $(target);
		if (divSpan.hasClass(this.markerClassName))
			return;
		divSpan.addClass(this.markerClassName).append(inst.dpDiv).
			bind("setData.datepicker", function(event, key, value){
				inst.settings[key] = value;
			}).bind("getData.datepicker", function(event, key){
				return this._get(inst, key);
			});
		$.data(target, PROP_NAME, inst);
		this._setDate(inst, this._getDefaultDate(inst), true);
		this._updateDatepicker(inst);
		this._updateAlternate(inst);
		inst.dpDiv.show();
	},

	/* Pop-up the date picker in a "dialog" box.
	   @param  input     element - ignored
	   @param  date      string or Date - the initial date to display
	   @param  onSelect  function - the function to call when a date is selected
	   @param  settings  object - update the dialog date picker instance's settings (anonymous object)
	   @param  pos       int[2] - coordinates for the dialog's position within the screen or
	                     event - with x/y coordinates or
	                     leave empty for default (screen centre)
	   @return the manager object */
	_dialogDatepicker: function(input, date, onSelect, settings, pos) {
		var inst = this._dialogInst; // internal instance
		if (!inst) {
			this.uuid += 1;
			var id = 'dp' + this.uuid;
			this._dialogInput = $('<input type="text" id="' + id +
				'" style="position: absolute; top: -100px; width: 0px; z-index: -10;"/>');
			this._dialogInput.keydown(this._doKeyDown);
			$('body').append(this._dialogInput);
			inst = this._dialogInst = this._newInst(this._dialogInput, false);
			inst.settings = {};
			$.data(this._dialogInput[0], PROP_NAME, inst);
		}
		extendRemove(inst.settings, settings || {});
		date = (date && date.constructor == Date ? this._formatDate(inst, date) : date);
		this._dialogInput.val(date);

		this._pos = (pos ? (pos.length ? pos : [pos.pageX, pos.pageY]) : null);
		if (!this._pos) {
			var browserWidth = document.documentElement.clientWidth;
			var browserHeight = document.documentElement.clientHeight;
			var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
			var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
			this._pos = // should use actual width/height below
				[(browserWidth / 2) - 100 + scrollX, (browserHeight / 2) - 150 + scrollY];
		}

		// move input on screen for focus, but hidden behind dialog
		this._dialogInput.css('left', (this._pos[0] + 20) + 'px').css('top', this._pos[1] + 'px');
		inst.settings.onSelect = onSelect;
		this._inDialog = true;
		this.dpDiv.addClass(this._dialogClass);
		this._showDatepicker(this._dialogInput[0]);
		if ($.blockUI)
			$.blockUI(this.dpDiv);
		$.data(this._dialogInput[0], PROP_NAME, inst);
		return this;
	},

	/* Detach a datepicker from its control.
	   @param  target    element - the target input field or division or span */
	_destroyDatepicker: function(target) {
		var $target = $(target);
		var inst = $.data(target, PROP_NAME);
		if (!$target.hasClass(this.markerClassName)) {
			return;
		}
		var nodeName = target.nodeName.toLowerCase();
		$.removeData(target, PROP_NAME);
		if (nodeName == 'input') {
			inst.append.remove();
			inst.trigger.remove();
			$target.removeClass(this.markerClassName).
				unbind('focus', this._showDatepicker).
				unbind('keydown', this._doKeyDown).
				unbind('keypress', this._doKeyPress).
				unbind('keyup', this._doKeyUp);
		} else if (nodeName == 'div' || nodeName == 'span')
			$target.removeClass(this.markerClassName).empty();
	},

	/* Enable the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span */
	_enableDatepicker: function(target) {
		var $target = $(target);
		var inst = $.data(target, PROP_NAME);
		if (!$target.hasClass(this.markerClassName)) {
			return;
		}
		var nodeName = target.nodeName.toLowerCase();
		if (nodeName == 'input') {
			target.disabled = false;
			inst.trigger.filter('button').
				each(function() { this.disabled = false; }).end().
				filter('img').css({opacity: '1.0', cursor: ''});
		}
		else if (nodeName == 'div' || nodeName == 'span') {
			var inline = $target.children('.' + this._inlineClass);
			inline.children().removeClass('ui-state-disabled');
		}
		this._disabledInputs = $.map(this._disabledInputs,
			function(value) { return (value == target ? null : value); }); // delete entry
	},

	/* Disable the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span */
	_disableDatepicker: function(target) {
		var $target = $(target);
		var inst = $.data(target, PROP_NAME);
		if (!$target.hasClass(this.markerClassName)) {
			return;
		}
		var nodeName = target.nodeName.toLowerCase();
		if (nodeName == 'input') {
			target.disabled = true;
			inst.trigger.filter('button').
				each(function() { this.disabled = true; }).end().
				filter('img').css({opacity: '0.5', cursor: 'default'});
		}
		else if (nodeName == 'div' || nodeName == 'span') {
			var inline = $target.children('.' + this._inlineClass);
			inline.children().addClass('ui-state-disabled');
		}
		this._disabledInputs = $.map(this._disabledInputs,
			function(value) { return (value == target ? null : value); }); // delete entry
		this._disabledInputs[this._disabledInputs.length] = target;
	},

	/* Is the first field in a jQuery collection disabled as a datepicker?
	   @param  target    element - the target input field or division or span
	   @return boolean - true if disabled, false if enabled */
	_isDisabledDatepicker: function(target) {
		if (!target) {
			return false;
		}
		for (var i = 0; i < this._disabledInputs.length; i++) {
			if (this._disabledInputs[i] == target)
				return true;
		}
		return false;
	},

	/* Retrieve the instance data for the target control.
	   @param  target  element - the target input field or division or span
	   @return  object - the associated instance data
	   @throws  error if a jQuery problem getting data */
	_getInst: function(target) {
		try {
			return $.data(target, PROP_NAME);
		}
		catch (err) {
			throw 'Missing instance data for this datepicker';
		}
	},

	/* Update or retrieve the settings for a date picker attached to an input field or division.
	   @param  target  element - the target input field or division or span
	   @param  name    object - the new settings to update or
	                   string - the name of the setting to change or retrieve,
	                   when retrieving also 'all' for all instance settings or
	                   'defaults' for all global defaults
	   @param  value   any - the new value for the setting
	                   (omit if above is an object or to retrieve a value) */
	_optionDatepicker: function(target, name, value) {
		var inst = this._getInst(target);
		if (arguments.length == 2 && typeof name == 'string') {
			return (name == 'defaults' ? $.extend({}, $.datepicker._defaults) :
				(inst ? (name == 'all' ? $.extend({}, inst.settings) :
				this._get(inst, name)) : null));
		}
		var settings = name || {};
		if (typeof name == 'string') {
			settings = {};
			settings[name] = value;
		}
		if (inst) {
			if (this._curInst == inst) {
				this._hideDatepicker();
			}
			var date = this._getDateDatepicker(target, true);
			extendRemove(inst.settings, settings);
			this._attachments($(target), inst);
			this._autoSize(inst);
			this._setDateDatepicker(target, date);
			this._updateDatepicker(inst);
		}
	},

	// change method deprecated
	_changeDatepicker: function(target, name, value) {
		this._optionDatepicker(target, name, value);
	},

	/* Redraw the date picker attached to an input field or division.
	   @param  target  element - the target input field or division or span */
	_refreshDatepicker: function(target) {
		var inst = this._getInst(target);
		if (inst) {
			this._updateDatepicker(inst);
		}
	},

	/* Set the dates for a jQuery selection.
	   @param  target   element - the target input field or division or span
	   @param  date     Date - the new date */
	_setDateDatepicker: function(target, date) {
		var inst = this._getInst(target);
		if (inst) {
			this._setDate(inst, date);
			this._updateDatepicker(inst);
			this._updateAlternate(inst);
		}
	},

	/* Get the date(s) for the first entry in a jQuery selection.
	   @param  target     element - the target input field or division or span
	   @param  noDefault  boolean - true if no default date is to be used
	   @return Date - the current date */
	_getDateDatepicker: function(target, noDefault) {
		var inst = this._getInst(target);
		if (inst && !inst.inline)
			this._setDateFromField(inst, noDefault);
		return (inst ? this._getDate(inst) : null);
	},

	/* Handle keystrokes. */
	_doKeyDown: function(event) {
		var inst = $.datepicker._getInst(event.target);
		var handled = true;
		var isRTL = inst.dpDiv.is('.ui-datepicker-rtl');
		inst._keyEvent = true;
		if ($.datepicker._datepickerShowing)
			switch (event.keyCode) {
				case 9: $.datepicker._hideDatepicker();
						handled = false;
						break; // hide on tab out
				case 13: var sel = $('td.' + $.datepicker._dayOverClass + ':not(.' +
									$.datepicker._currentClass + ')', inst.dpDiv);
						if (sel[0])
							$.datepicker._selectDay(event.target, inst.selectedMonth, inst.selectedYear, sel[0]);
						else
							$.datepicker._hideDatepicker();
						return false; // don't submit the form
						break; // select the value on enter
				case 27: $.datepicker._hideDatepicker();
						break; // hide on escape
				case 33: $.datepicker._adjustDate(event.target, (event.ctrlKey ?
							-$.datepicker._get(inst, 'stepBigMonths') :
							-$.datepicker._get(inst, 'stepMonths')), 'M');
						break; // previous month/year on page up/+ ctrl
				case 34: $.datepicker._adjustDate(event.target, (event.ctrlKey ?
							+$.datepicker._get(inst, 'stepBigMonths') :
							+$.datepicker._get(inst, 'stepMonths')), 'M');
						break; // next month/year on page down/+ ctrl
				case 35: if (event.ctrlKey || event.metaKey) $.datepicker._clearDate(event.target);
						handled = event.ctrlKey || event.metaKey;
						break; // clear on ctrl or command +end
				case 36: if (event.ctrlKey || event.metaKey) $.datepicker._gotoToday(event.target);
						handled = event.ctrlKey || event.metaKey;
						break; // current on ctrl or command +home
				case 37: if (event.ctrlKey || event.metaKey) $.datepicker._adjustDate(event.target, (isRTL ? +1 : -1), 'D');
						handled = event.ctrlKey || event.metaKey;
						// -1 day on ctrl or command +left
						if (event.originalEvent.altKey) $.datepicker._adjustDate(event.target, (event.ctrlKey ?
									-$.datepicker._get(inst, 'stepBigMonths') :
									-$.datepicker._get(inst, 'stepMonths')), 'M');
						// next month/year on alt +left on Mac
						break;
				case 38: if (event.ctrlKey || event.metaKey) $.datepicker._adjustDate(event.target, -7, 'D');
						handled = event.ctrlKey || event.metaKey;
						break; // -1 week on ctrl or command +up
				case 39: if (event.ctrlKey || event.metaKey) $.datepicker._adjustDate(event.target, (isRTL ? -1 : +1), 'D');
						handled = event.ctrlKey || event.metaKey;
						// +1 day on ctrl or command +right
						if (event.originalEvent.altKey) $.datepicker._adjustDate(event.target, (event.ctrlKey ?
									+$.datepicker._get(inst, 'stepBigMonths') :
									+$.datepicker._get(inst, 'stepMonths')), 'M');
						// next month/year on alt +right
						break;
				case 40: if (event.ctrlKey || event.metaKey) $.datepicker._adjustDate(event.target, +7, 'D');
						handled = event.ctrlKey || event.metaKey;
						break; // +1 week on ctrl or command +down
				default: handled = false;
			}
		else if (event.keyCode == 36 && event.ctrlKey) // display the date picker on ctrl+home
			$.datepicker._showDatepicker(this);
		else {
			handled = false;
		}
		if (handled) {
			event.preventDefault();
			event.stopPropagation();
		}
	},

	/* Filter entered characters - based on date format. */
	_doKeyPress: function(event) {
		var inst = $.datepicker._getInst(event.target);
		if ($.datepicker._get(inst, 'constrainInput')) {
			var chars = $.datepicker._possibleChars($.datepicker._get(inst, 'dateFormat'));
			var chr = String.fromCharCode(event.charCode == undefined ? event.keyCode : event.charCode);
			return event.ctrlKey || event.metaKey || (chr < ' ' || !chars || chars.indexOf(chr) > -1);
		}
	},

	/* Synchronise manual entry and field/alternate field. */
	_doKeyUp: function(event) {
		var inst = $.datepicker._getInst(event.target);
		if (inst.input.val() != inst.lastVal) {
			try {
				var date = $.datepicker.parseDate($.datepicker._get(inst, 'dateFormat'),
					(inst.input ? inst.input.val() : null),
					$.datepicker._getFormatConfig(inst));
				if (date) { // only if valid
					$.datepicker._setDateFromField(inst);
					$.datepicker._updateAlternate(inst);
					$.datepicker._updateDatepicker(inst);
				}
			}
			catch (event) {
				$.datepicker.log(event);
			}
		}
		return true;
	},

	/* Pop-up the date picker for a given input field.
	   @param  input  element - the input field attached to the date picker or
	                  event - if triggered by focus */
	_showDatepicker: function(input) {
		input = input.target || input;
		if (input.nodeName.toLowerCase() != 'input') // find from button/image trigger
			input = $('input', input.parentNode)[0];
		if ($.datepicker._isDisabledDatepicker(input) || $.datepicker._lastInput == input) // already here
			return;
		var inst = $.datepicker._getInst(input);
		if ($.datepicker._curInst && $.datepicker._curInst != inst) {
			$.datepicker._curInst.dpDiv.stop(true, true);
		}
		var beforeShow = $.datepicker._get(inst, 'beforeShow');
		extendRemove(inst.settings, (beforeShow ? beforeShow.apply(input, [input, inst]) : {}));
		inst.lastVal = null;
		$.datepicker._lastInput = input;
		$.datepicker._setDateFromField(inst);
		if ($.datepicker._inDialog) // hide cursor
			input.value = '';
		if (!$.datepicker._pos) { // position below input
			$.datepicker._pos = $.datepicker._findPos(input);
			$.datepicker._pos[1] += input.offsetHeight; // add the height
		}
		var isFixed = false;
		$(input).parents().each(function() {
			isFixed |= $(this).css('position') == 'fixed';
			return !isFixed;
		});
		if (isFixed && $.browser.opera) { // correction for Opera when fixed and scrolled
			$.datepicker._pos[0] -= document.documentElement.scrollLeft;
			$.datepicker._pos[1] -= document.documentElement.scrollTop;
		}
		var offset = {left: $.datepicker._pos[0], top: $.datepicker._pos[1]};
		$.datepicker._pos = null;
		//to avoid flashes on Firefox
		inst.dpDiv.empty();
		// determine sizing offscreen
		inst.dpDiv.css({position: 'absolute', display: 'block', top: '-1000px'});
		$.datepicker._updateDatepicker(inst);
		// fix width for dynamic number of date pickers
		// and adjust position before showing
		offset = $.datepicker._checkOffset(inst, offset, isFixed);
		inst.dpDiv.css({position: ($.datepicker._inDialog && $.blockUI ?
			'static' : (isFixed ? 'fixed' : 'absolute')), display: 'none',
			left: offset.left + 'px', top: offset.top + 'px'});
		if (!inst.inline) {
			var showAnim = $.datepicker._get(inst, 'showAnim');
			var duration = $.datepicker._get(inst, 'duration');
			var postProcess = function() {
				$.datepicker._datepickerShowing = true;
				var cover = inst.dpDiv.find('iframe.ui-datepicker-cover'); // IE6- only
				if( !! cover.length ){
					var borders = $.datepicker._getBorders(inst.dpDiv);
					cover.css({left: -borders[0], top: -borders[1],
						width: inst.dpDiv.outerWidth(), height: inst.dpDiv.outerHeight()});
				}
			};
			inst.dpDiv.zIndex($(input).zIndex()+1);
			if ($.effects && $.effects[showAnim])
				inst.dpDiv.show(showAnim, $.datepicker._get(inst, 'showOptions'), duration, postProcess);
			else
				inst.dpDiv[showAnim || 'show']((showAnim ? duration : null), postProcess);
			if (!showAnim || !duration)
				postProcess();
			if (inst.input.is(':visible') && !inst.input.is(':disabled'))
				inst.input.focus();
			$.datepicker._curInst = inst;
		}
	},

	/* Generate the date picker content. */
	_updateDatepicker: function(inst) {
		var self = this;
		var borders = $.datepicker._getBorders(inst.dpDiv);
		inst.dpDiv.empty().append(this._generateHTML(inst));
		var cover = inst.dpDiv.find('iframe.ui-datepicker-cover'); // IE6- only
		if( !!cover.length ){ //avoid call to outerXXXX() when not in IE6
			cover.css({left: -borders[0], top: -borders[1], width: inst.dpDiv.outerWidth(), height: inst.dpDiv.outerHeight()})
		}
		inst.dpDiv.find('button, .ui-datepicker-prev, .ui-datepicker-next, .ui-datepicker-calendar td a')
				.bind('mouseout', function(){
					$(this).removeClass('ui-state-hover');
					if(this.className.indexOf('ui-datepicker-prev') != -1) $(this).removeClass('ui-datepicker-prev-hover');
					if(this.className.indexOf('ui-datepicker-next') != -1) $(this).removeClass('ui-datepicker-next-hover');
				})
				.bind('mouseover', function(){
					if (!self._isDisabledDatepicker( inst.inline ? inst.dpDiv.parent()[0] : inst.input[0])) {
						$(this).parents('.ui-datepicker-calendar').find('a').removeClass('ui-state-hover');
						$(this).addClass('ui-state-hover');
						if(this.className.indexOf('ui-datepicker-prev') != -1) $(this).addClass('ui-datepicker-prev-hover');
						if(this.className.indexOf('ui-datepicker-next') != -1) $(this).addClass('ui-datepicker-next-hover');
					}
				})
			.end()
			.find('.' + this._dayOverClass + ' a')
				.trigger('mouseover')
			.end();
		var numMonths = this._getNumberOfMonths(inst);
		var cols = numMonths[1];
		var width = 17;
		if (cols > 1)
			inst.dpDiv.addClass('ui-datepicker-multi-' + cols).css('width', (width * cols) + 'em');
		else
			inst.dpDiv.removeClass('ui-datepicker-multi-2 ui-datepicker-multi-3 ui-datepicker-multi-4').width('');
		inst.dpDiv[(numMonths[0] != 1 || numMonths[1] != 1 ? 'add' : 'remove') +
			'Class']('ui-datepicker-multi');
		inst.dpDiv[(this._get(inst, 'isRTL') ? 'add' : 'remove') +
			'Class']('ui-datepicker-rtl');
		if (inst == $.datepicker._curInst && $.datepicker._datepickerShowing && inst.input &&
				inst.input.is(':visible') && !inst.input.is(':disabled'))
			inst.input.focus();
		// deffered render of the years select (to avoid flashes on Firefox)
		if( inst.yearshtml ){
			var origyearshtml = inst.yearshtml;
			setTimeout(function(){
				//assure that inst.yearshtml didn't change.
				if( origyearshtml === inst.yearshtml ){
					inst.dpDiv.find('select.ui-datepicker-year:first').replaceWith(inst.yearshtml);
				}
				origyearshtml = inst.yearshtml = null;
			}, 0);
		}
	},

	/* Retrieve the size of left and top borders for an element.
	   @param  elem  (jQuery object) the element of interest
	   @return  (number[2]) the left and top borders */
	_getBorders: function(elem) {
		var convert = function(value) {
			return {thin: 1, medium: 2, thick: 3}[value] || value;
		};
		return [parseFloat(convert(elem.css('border-left-width'))),
			parseFloat(convert(elem.css('border-top-width')))];
	},

	/* Check positioning to remain on screen. */
	_checkOffset: function(inst, offset, isFixed) {
		var dpWidth = inst.dpDiv.outerWidth();
		var dpHeight = inst.dpDiv.outerHeight();
		var inputWidth = inst.input ? inst.input.outerWidth() : 0;
		var inputHeight = inst.input ? inst.input.outerHeight() : 0;
		var viewWidth = document.documentElement.clientWidth + $(document).scrollLeft();
		var viewHeight = document.documentElement.clientHeight + $(document).scrollTop();

		offset.left -= (this._get(inst, 'isRTL') ? (dpWidth - inputWidth) : 0);
		offset.left -= (isFixed && offset.left == inst.input.offset().left) ? $(document).scrollLeft() : 0;
		offset.top -= (isFixed && offset.top == (inst.input.offset().top + inputHeight)) ? $(document).scrollTop() : 0;

		// now check if datepicker is showing outside window viewport - move to a better place if so.
		offset.left -= Math.min(offset.left, (offset.left + dpWidth > viewWidth && viewWidth > dpWidth) ?
			Math.abs(offset.left + dpWidth - viewWidth) : 0);
		offset.top -= Math.min(offset.top, (offset.top + dpHeight > viewHeight && viewHeight > dpHeight) ?
			Math.abs(dpHeight + inputHeight) : 0);

		return offset;
	},

	/* Find an object's position on the screen. */
	_findPos: function(obj) {
		var inst = this._getInst(obj);
		var isRTL = this._get(inst, 'isRTL');
        while (obj && (obj.type == 'hidden' || obj.nodeType != 1)) {
            obj = obj[isRTL ? 'previousSibling' : 'nextSibling'];
        }
        var position = $(obj).offset();
	    return [position.left, position.top];
	},

	/* Hide the date picker from view.
	   @param  input  element - the input field attached to the date picker */
	_hideDatepicker: function(input) {
		var inst = this._curInst;
		if (!inst || (input && inst != $.data(input, PROP_NAME)))
			return;
		if (this._datepickerShowing) {
			var showAnim = this._get(inst, 'showAnim');
			var duration = this._get(inst, 'duration');
			var postProcess = function() {
				$.datepicker._tidyDialog(inst);
				this._curInst = null;
			};
			if ($.effects && $.effects[showAnim])
				inst.dpDiv.hide(showAnim, $.datepicker._get(inst, 'showOptions'), duration, postProcess);
			else
				inst.dpDiv[(showAnim == 'slideDown' ? 'slideUp' :
					(showAnim == 'fadeIn' ? 'fadeOut' : 'hide'))]((showAnim ? duration : null), postProcess);
			if (!showAnim)
				postProcess();
			var onClose = this._get(inst, 'onClose');
			if (onClose)
				onClose.apply((inst.input ? inst.input[0] : null),
					[(inst.input ? inst.input.val() : ''), inst]);  // trigger custom callback
			this._datepickerShowing = false;
			this._lastInput = null;
			if (this._inDialog) {
				this._dialogInput.css({ position: 'absolute', left: '0', top: '-100px' });
				if ($.blockUI) {
					$.unblockUI();
					$('body').append(this.dpDiv);
				}
			}
			this._inDialog = false;
		}
	},

	/* Tidy up after a dialog display. */
	_tidyDialog: function(inst) {
		inst.dpDiv.removeClass(this._dialogClass).unbind('.ui-datepicker-calendar');
	},

	/* Close date picker if clicked elsewhere. */
	_checkExternalClick: function(event) {
		if (!$.datepicker._curInst)
			return;
		var $target = $(event.target);
		if ($target[0].id != $.datepicker._mainDivId &&
				$target.parents('#' + $.datepicker._mainDivId).length == 0 &&
				!$target.hasClass($.datepicker.markerClassName) &&
				!$target.hasClass($.datepicker._triggerClass) &&
				$.datepicker._datepickerShowing && !($.datepicker._inDialog && $.blockUI))
			$.datepicker._hideDatepicker();
	},

	/* Adjust one of the date sub-fields. */
	_adjustDate: function(id, offset, period) {
		var target = $(id);
		var inst = this._getInst(target[0]);
		if (this._isDisabledDatepicker(target[0])) {
			return;
		}
		this._adjustInstDate(inst, offset +
			(period == 'M' ? this._get(inst, 'showCurrentAtPos') : 0), // undo positioning
			period);
		this._updateDatepicker(inst);
	},

	/* Action for current link. */
	_gotoToday: function(id) {
		var target = $(id);
		var inst = this._getInst(target[0]);
		if (this._get(inst, 'gotoCurrent') && inst.currentDay) {
			inst.selectedDay = inst.currentDay;
			inst.drawMonth = inst.selectedMonth = inst.currentMonth;
			inst.drawYear = inst.selectedYear = inst.currentYear;
		}
		else {
			var date = new Date();
			inst.selectedDay = date.getDate();
			inst.drawMonth = inst.selectedMonth = date.getMonth();
			inst.drawYear = inst.selectedYear = date.getFullYear();
		}
		this._notifyChange(inst);
		this._adjustDate(target);
	},

	/* Action for selecting a new month/year. */
	_selectMonthYear: function(id, select, period) {
		var target = $(id);
		var inst = this._getInst(target[0]);
		inst._selectingMonthYear = false;
		inst['selected' + (period == 'M' ? 'Month' : 'Year')] =
		inst['draw' + (period == 'M' ? 'Month' : 'Year')] =
			parseInt(select.options[select.selectedIndex].value,10);
		this._notifyChange(inst);
		this._adjustDate(target);
	},

	/* Restore input focus after not changing month/year. */
	_clickMonthYear: function(id) {
		var target = $(id);
		var inst = this._getInst(target[0]);
		if (inst.input && inst._selectingMonthYear) {
			setTimeout(function() {
				inst.input.focus();
			}, 0);
		}
		inst._selectingMonthYear = !inst._selectingMonthYear;
	},

	/* Action for selecting a day. */
	_selectDay: function(id, month, year, td) {
		var target = $(id);
		if ($(td).hasClass(this._unselectableClass) || this._isDisabledDatepicker(target[0])) {
			return;
		}
		var inst = this._getInst(target[0]);
		inst.selectedDay = inst.currentDay = $('a', td).html();
		inst.selectedMonth = inst.currentMonth = month;
		inst.selectedYear = inst.currentYear = year;
		this._selectDate(id, this._formatDate(inst,
			inst.currentDay, inst.currentMonth, inst.currentYear));
	},

	/* Erase the input field and hide the date picker. */
	_clearDate: function(id) {
		var target = $(id);
		var inst = this._getInst(target[0]);
		this._selectDate(target, '');
	},

	/* Update the input field with the selected date. */
	_selectDate: function(id, dateStr) {
		var target = $(id);
		var inst = this._getInst(target[0]);
		dateStr = (dateStr != null ? dateStr : this._formatDate(inst));
		if (inst.input)
			inst.input.val(dateStr);
		this._updateAlternate(inst);
		var onSelect = this._get(inst, 'onSelect');
		if (onSelect)
			onSelect.apply((inst.input ? inst.input[0] : null), [dateStr, inst]);  // trigger custom callback
		else if (inst.input)
			inst.input.trigger('change'); // fire the change event
		if (inst.inline)
			this._updateDatepicker(inst);
		else {
			this._hideDatepicker();
			this._lastInput = inst.input[0];
			if (typeof(inst.input[0]) != 'object')
				inst.input.focus(); // restore focus
			this._lastInput = null;
		}
	},

	/* Update any alternate field to synchronise with the main field. */
	_updateAlternate: function(inst) {
		var altField = this._get(inst, 'altField');
		if (altField) { // update alternate field too
			var altFormat = this._get(inst, 'altFormat') || this._get(inst, 'dateFormat');
			var date = this._getDate(inst);
			var dateStr = this.formatDate(altFormat, date, this._getFormatConfig(inst));
			$(altField).each(function() { $(this).val(dateStr); });
		}
	},

	/* Set as beforeShowDay function to prevent selection of weekends.
	   @param  date  Date - the date to customise
	   @return [boolean, string] - is this date selectable?, what is its CSS class? */
	noWeekends: function(date) {
		var day = date.getDay();
		return [(day > 0 && day < 6), ''];
	},

	/* Set as calculateWeek to determine the week of the year based on the ISO 8601 definition.
	   @param  date  Date - the date to get the week for
	   @return  number - the number of the week within the year that contains this date */
	iso8601Week: function(date) {
		var checkDate = new Date(date.getTime());
		// Find Thursday of this week starting on Monday
		checkDate.setDate(checkDate.getDate() + 4 - (checkDate.getDay() || 7));
		var time = checkDate.getTime();
		checkDate.setMonth(0); // Compare with Jan 1
		checkDate.setDate(1);
		return Math.floor(Math.round((time - checkDate) / 86400000) / 7) + 1;
	},

	/* Parse a string value into a date object.
	   See formatDate below for the possible formats.

	   @param  format    string - the expected format of the date
	   @param  value     string - the date in the above format
	   @param  settings  Object - attributes include:
	                     shortYearCutoff  number - the cutoff year for determining the century (optional)
	                     dayNamesShort    string[7] - abbreviated names of the days from Sunday (optional)
	                     dayNames         string[7] - names of the days from Sunday (optional)
	                     monthNamesShort  string[12] - abbreviated names of the months (optional)
	                     monthNames       string[12] - names of the months (optional)
	   @return  Date - the extracted date value or null if value is blank */
	parseDate: function (format, value, settings) {
		if (format == null || value == null)
			throw 'Invalid arguments';
		value = (typeof value == 'object' ? value.toString() : value + '');
		if (value == '')
			return null;
		var shortYearCutoff = (settings ? settings.shortYearCutoff : null) || this._defaults.shortYearCutoff;
		shortYearCutoff = (typeof shortYearCutoff != 'string' ? shortYearCutoff :
				new Date().getFullYear() % 100 + parseInt(shortYearCutoff, 10));
		var dayNamesShort = (settings ? settings.dayNamesShort : null) || this._defaults.dayNamesShort;
		var dayNames = (settings ? settings.dayNames : null) || this._defaults.dayNames;
		var monthNamesShort = (settings ? settings.monthNamesShort : null) || this._defaults.monthNamesShort;
		var monthNames = (settings ? settings.monthNames : null) || this._defaults.monthNames;
		var year = -1;
		var month = -1;
		var day = -1;
		var doy = -1;
		var literal = false;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;
		};
		// Extract a number from the string value
		var getNumber = function(match) {
			var isDoubled = lookAhead(match);
			var size = (match == '@' ? 14 : (match == '!' ? 20 :
				(match == 'y' && isDoubled ? 4 : (match == 'o' ? 3 : 2))));
			var digits = new RegExp('^\\d{1,' + size + '}');
			var num = value.substring(iValue).match(digits);
			if (!num)
				throw 'Missing number at position ' + iValue;
			iValue += num[0].length;
			return parseInt(num[0], 10);
		};
		// Extract a name from the string value and convert to an index
		var getName = function(match, shortNames, longNames) {
			var names = (lookAhead(match) ? longNames : shortNames);
			for (var i = 0; i < names.length; i++) {
				if (value.substr(iValue, names[i].length).toLowerCase() == names[i].toLowerCase()) {
					iValue += names[i].length;
					return i + 1;
				}
			}
			throw 'Unknown name at position ' + iValue;
		};
		// Confirm that a literal character matches the string value
		var checkLiteral = function() {
			if (value.charAt(iValue) != format.charAt(iFormat))
				throw 'Unexpected literal at position ' + iValue;
			iValue++;
		};
		var iValue = 0;
		for (var iFormat = 0; iFormat < format.length; iFormat++) {
			if (literal)
				if (format.charAt(iFormat) == "'" && !lookAhead("'"))
					literal = false;
				else
					checkLiteral();
			else
				switch (format.charAt(iFormat)) {
					case 'd':
						day = getNumber('d');
						break;
					case 'D':
						getName('D', dayNamesShort, dayNames);
						break;
					case 'o':
						doy = getNumber('o');
						break;
					case 'm':
						month = getNumber('m');
						break;
					case 'M':
						month = getName('M', monthNamesShort, monthNames);
						break;
					case 'y':
						year = getNumber('y');
						break;
					case '@':
						var date = new Date(getNumber('@'));
						year = date.getFullYear();
						month = date.getMonth() + 1;
						day = date.getDate();
						break;
					case '!':
						var date = new Date((getNumber('!') - this._ticksTo1970) / 10000);
						year = date.getFullYear();
						month = date.getMonth() + 1;
						day = date.getDate();
						break;
					case "'":
						if (lookAhead("'"))
							checkLiteral();
						else
							literal = true;
						break;
					default:
						checkLiteral();
				}
		}
		if (year == -1)
			year = new Date().getFullYear();
		else if (year < 100)
			year += new Date().getFullYear() - new Date().getFullYear() % 100 +
				(year <= shortYearCutoff ? 0 : -100);
		if (doy > -1) {
			month = 1;
			day = doy;
			do {
				var dim = this._getDaysInMonth(year, month - 1);
				if (day <= dim)
					break;
				month++;
				day -= dim;
			} while (true);
		}
		var date = this._daylightSavingAdjust(new Date(year, month - 1, day));
		if (date.getFullYear() != year || date.getMonth() + 1 != month || date.getDate() != day)
			throw 'Invalid date'; // E.g. 31/02/*
		return date;
	},

	/* Standard date formats. */
	ATOM: 'yy-mm-dd', // RFC 3339 (ISO 8601)
	COOKIE: 'D, dd M yy',
	ISO_8601: 'yy-mm-dd',
	RFC_822: 'D, d M y',
	RFC_850: 'DD, dd-M-y',
	RFC_1036: 'D, d M y',
	RFC_1123: 'D, d M yy',
	RFC_2822: 'D, d M yy',
	RSS: 'D, d M y', // RFC 822
	TICKS: '!',
	TIMESTAMP: '@',
	W3C: 'yy-mm-dd', // ISO 8601

	_ticksTo1970: (((1970 - 1) * 365 + Math.floor(1970 / 4) - Math.floor(1970 / 100) +
		Math.floor(1970 / 400)) * 24 * 60 * 60 * 10000000),

	/* Format a date object into a string value.
	   The format can be combinations of the following:
	   d  - day of month (no leading zero)
	   dd - day of month (two digit)
	   o  - day of year (no leading zeros)
	   oo - day of year (three digit)
	   D  - day name short
	   DD - day name long
	   m  - month of year (no leading zero)
	   mm - month of year (two digit)
	   M  - month name short
	   MM - month name long
	   y  - year (two digit)
	   yy - year (four digit)
	   @ - Unix timestamp (ms since 01/01/1970)
	   ! - Windows ticks (100ns since 01/01/0001)
	   '...' - literal text
	   '' - single quote

	   @param  format    string - the desired format of the date
	   @param  date      Date - the date value to format
	   @param  settings  Object - attributes include:
	                     dayNamesShort    string[7] - abbreviated names of the days from Sunday (optional)
	                     dayNames         string[7] - names of the days from Sunday (optional)
	                     monthNamesShort  string[12] - abbreviated names of the months (optional)
	                     monthNames       string[12] - names of the months (optional)
	   @return  string - the date in the above format */
	formatDate: function (format, date, settings) {
		if (!date)
			return '';
		var dayNamesShort = (settings ? settings.dayNamesShort : null) || this._defaults.dayNamesShort;
		var dayNames = (settings ? settings.dayNames : null) || this._defaults.dayNames;
		var monthNamesShort = (settings ? settings.monthNamesShort : null) || this._defaults.monthNamesShort;
		var monthNames = (settings ? settings.monthNames : null) || this._defaults.monthNames;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;
		};
		// Format a number, with leading zero if necessary
		var formatNumber = function(match, value, len) {
			var num = '' + value;
			if (lookAhead(match))
				while (num.length < len)
					num = '0' + num;
			return num;
		};
		// Format a name, short or long as requested
		var formatName = function(match, value, shortNames, longNames) {
			return (lookAhead(match) ? longNames[value] : shortNames[value]);
		};
		var output = '';
		var literal = false;
		if (date)
			for (var iFormat = 0; iFormat < format.length; iFormat++) {
				if (literal)
					if (format.charAt(iFormat) == "'" && !lookAhead("'"))
						literal = false;
					else
						output += format.charAt(iFormat);
				else
					switch (format.charAt(iFormat)) {
						case 'd':
							output += formatNumber('d', date.getDate(), 2);
							break;
						case 'D':
							output += formatName('D', date.getDay(), dayNamesShort, dayNames);
							break;
						case 'o':
							output += formatNumber('o',
								(date.getTime() - new Date(date.getFullYear(), 0, 0).getTime()) / 86400000, 3);
							break;
						case 'm':
							output += formatNumber('m', date.getMonth() + 1, 2);
							break;
						case 'M':
							output += formatName('M', date.getMonth(), monthNamesShort, monthNames);
							break;
						case 'y':
							output += (lookAhead('y') ? date.getFullYear() :
								(date.getYear() % 100 < 10 ? '0' : '') + date.getYear() % 100);
							break;
						case '@':
							output += date.getTime();
							break;
						case '!':
							output += date.getTime() * 10000 + this._ticksTo1970;
							break;
						case "'":
							if (lookAhead("'"))
								output += "'";
							else
								literal = true;
							break;
						default:
							output += format.charAt(iFormat);
					}
			}
		return output;
	},

	/* Extract all possible characters from the date format. */
	_possibleChars: function (format) {
		var chars = '';
		var literal = false;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;
		};
		for (var iFormat = 0; iFormat < format.length; iFormat++)
			if (literal)
				if (format.charAt(iFormat) == "'" && !lookAhead("'"))
					literal = false;
				else
					chars += format.charAt(iFormat);
			else
				switch (format.charAt(iFormat)) {
					case 'd': case 'm': case 'y': case '@':
						chars += '0123456789';
						break;
					case 'D': case 'M':
						return null; // Accept anything
					case "'":
						if (lookAhead("'"))
							chars += "'";
						else
							literal = true;
						break;
					default:
						chars += format.charAt(iFormat);
				}
		return chars;
	},

	/* Get a setting value, defaulting if necessary. */
	_get: function(inst, name) {
		return inst.settings[name] !== undefined ?
			inst.settings[name] : this._defaults[name];
	},

	/* Parse existing date and initialise date picker. */
	_setDateFromField: function(inst, noDefault) {
		if (inst.input.val() == inst.lastVal) {
			return;
		}
		var dateFormat = this._get(inst, 'dateFormat');
		var dates = inst.lastVal = inst.input ? inst.input.val() : null;
		var date, defaultDate;
		date = defaultDate = this._getDefaultDate(inst);
		var settings = this._getFormatConfig(inst);
		try {
			date = this.parseDate(dateFormat, dates, settings) || defaultDate;
		} catch (event) {
			this.log(event);
			dates = (noDefault ? '' : dates);
		}
		inst.selectedDay = date.getDate();
		inst.drawMonth = inst.selectedMonth = date.getMonth();
		inst.drawYear = inst.selectedYear = date.getFullYear();
		inst.currentDay = (dates ? date.getDate() : 0);
		inst.currentMonth = (dates ? date.getMonth() : 0);
		inst.currentYear = (dates ? date.getFullYear() : 0);
		this._adjustInstDate(inst);
	},

	/* Retrieve the default date shown on opening. */
	_getDefaultDate: function(inst) {
		return this._restrictMinMax(inst,
			this._determineDate(inst, this._get(inst, 'defaultDate'), new Date()));
	},

	/* A date may be specified as an exact value or a relative one. */
	_determineDate: function(inst, date, defaultDate) {
		var offsetNumeric = function(offset) {
			var date = new Date();
			date.setDate(date.getDate() + offset);
			return date;
		};
		var offsetString = function(offset) {
			try {
				return $.datepicker.parseDate($.datepicker._get(inst, 'dateFormat'),
					offset, $.datepicker._getFormatConfig(inst));
			}
			catch (e) {
				// Ignore
			}
			var date = (offset.toLowerCase().match(/^c/) ?
				$.datepicker._getDate(inst) : null) || new Date();
			var year = date.getFullYear();
			var month = date.getMonth();
			var day = date.getDate();
			var pattern = /([+-]?[0-9]+)\s*(d|D|w|W|m|M|y|Y)?/g;
			var matches = pattern.exec(offset);
			while (matches) {
				switch (matches[2] || 'd') {
					case 'd' : case 'D' :
						day += parseInt(matches[1],10); break;
					case 'w' : case 'W' :
						day += parseInt(matches[1],10) * 7; break;
					case 'm' : case 'M' :
						month += parseInt(matches[1],10);
						day = Math.min(day, $.datepicker._getDaysInMonth(year, month));
						break;
					case 'y': case 'Y' :
						year += parseInt(matches[1],10);
						day = Math.min(day, $.datepicker._getDaysInMonth(year, month));
						break;
				}
				matches = pattern.exec(offset);
			}
			return new Date(year, month, day);
		};
		var newDate = (date == null || date === '' ? defaultDate : (typeof date == 'string' ? offsetString(date) :
			(typeof date == 'number' ? (isNaN(date) ? defaultDate : offsetNumeric(date)) : new Date(date.getTime()))));
		newDate = (newDate && newDate.toString() == 'Invalid Date' ? defaultDate : newDate);
		if (newDate) {
			newDate.setHours(0);
			newDate.setMinutes(0);
			newDate.setSeconds(0);
			newDate.setMilliseconds(0);
		}
		return this._daylightSavingAdjust(newDate);
	},

	/* Handle switch to/from daylight saving.
	   Hours may be non-zero on daylight saving cut-over:
	   > 12 when midnight changeover, but then cannot generate
	   midnight datetime, so jump to 1AM, otherwise reset.
	   @param  date  (Date) the date to check
	   @return  (Date) the corrected date */
	_daylightSavingAdjust: function(date) {
		if (!date) return null;
		date.setHours(date.getHours() > 12 ? date.getHours() + 2 : 0);
		return date;
	},

	/* Set the date(s) directly. */
	_setDate: function(inst, date, noChange) {
		var clear = !date;
		var origMonth = inst.selectedMonth;
		var origYear = inst.selectedYear;
		var newDate = this._restrictMinMax(inst, this._determineDate(inst, date, new Date()));
		inst.selectedDay = inst.currentDay = newDate.getDate();
		inst.drawMonth = inst.selectedMonth = inst.currentMonth = newDate.getMonth();
		inst.drawYear = inst.selectedYear = inst.currentYear = newDate.getFullYear();
		if ((origMonth != inst.selectedMonth || origYear != inst.selectedYear) && !noChange)
			this._notifyChange(inst);
		this._adjustInstDate(inst);
		if (inst.input) {
			inst.input.val(clear ? '' : this._formatDate(inst));
		}
	},

	/* Retrieve the date(s) directly. */
	_getDate: function(inst) {
		var startDate = (!inst.currentYear || (inst.input && inst.input.val() == '') ? null :
			this._daylightSavingAdjust(new Date(
			inst.currentYear, inst.currentMonth, inst.currentDay)));
			return startDate;
	},

    /* OG custom */
    _isSpecialDate: function (arr, year, month, day) {
        var i = arr.length, str;
        while(i--) {
            str = arr[i].split('-');
            if (str[0] == month && str[1] == day) {
                return true
            }
        }
    },

	/* Generate the HTML for the current state of the date picker. */
	_generateHTML: function(inst) {
		var today = new Date();
		today = this._daylightSavingAdjust(
			new Date(today.getFullYear(), today.getMonth(), today.getDate())); // clear time
		var isRTL = this._get(inst, 'isRTL');
		var showButtonPanel = this._get(inst, 'showButtonPanel');
		var hideIfNoPrevNext = this._get(inst, 'hideIfNoPrevNext');
		var navigationAsDateFormat = this._get(inst, 'navigationAsDateFormat');
		var numMonths = this._getNumberOfMonths(inst);
		var showCurrentAtPos = this._get(inst, 'showCurrentAtPos');
		var stepMonths = this._get(inst, 'stepMonths');
		var isMultiMonth = (numMonths[0] != 1 || numMonths[1] != 1);
		var currentDate = this._daylightSavingAdjust((!inst.currentDay ? new Date(9999, 9, 9) :
			new Date(inst.currentYear, inst.currentMonth, inst.currentDay)));
		var minDate = this._getMinMaxDate(inst, 'min');
		var maxDate = this._getMinMaxDate(inst, 'max');
		var drawMonth = inst.drawMonth - showCurrentAtPos;
        var drawYear = inst.drawYear;
        var displayOnly = this._get(inst, 'displayOnly');
        var specialDates = this._get(inst, 'specialDates');
        specialDates = specialDates[drawYear]; // Array of special dates for one year (day, month)

		if (drawMonth < 0) {
			drawMonth += 12;
			drawYear--;
		}
		if (maxDate) {
			var maxDraw = this._daylightSavingAdjust(new Date(maxDate.getFullYear(),
				maxDate.getMonth() - (numMonths[0] * numMonths[1]) + 1, maxDate.getDate()));
			maxDraw = (minDate && maxDraw < minDate ? minDate : maxDraw);
			while (this._daylightSavingAdjust(new Date(drawYear, drawMonth, 1)) > maxDraw) {
				drawMonth--;
				if (drawMonth < 0) {
					drawMonth = 11;
					drawYear--;
				}
			}
		}
		inst.drawMonth = drawMonth;
		inst.drawYear = drawYear;
		var prevText = this._get(inst, 'prevText');
		prevText = (!navigationAsDateFormat ? prevText : this.formatDate(prevText,
			this._daylightSavingAdjust(new Date(drawYear, drawMonth - stepMonths, 1)),
			this._getFormatConfig(inst)));
		var prev = (this._canAdjustMonth(inst, -1, drawYear, drawMonth) ?
			'<a class="ui-datepicker-prev ui-corner-all" onclick="DP_jQuery_' + dpuuid +
			'.datepicker._adjustDate(\'#' + inst.id + '\', -' + stepMonths + ', \'M\');"' +
			' title="' + prevText + '"><span class="ui-icon ui-icon-circle-triangle-' + ( isRTL ? 'e' : 'w') + '">' + prevText + '</span></a>' :
			(hideIfNoPrevNext ? '' : '<a class="ui-datepicker-prev ui-corner-all ui-state-disabled" title="'+ prevText +'"><span class="ui-icon ui-icon-circle-triangle-' + ( isRTL ? 'e' : 'w') + '">' + prevText + '</span></a>'));
		var nextText = this._get(inst, 'nextText');
		nextText = (!navigationAsDateFormat ? nextText : this.formatDate(nextText,
			this._daylightSavingAdjust(new Date(drawYear, drawMonth + stepMonths, 1)),
			this._getFormatConfig(inst)));
		var next = (this._canAdjustMonth(inst, +1, drawYear, drawMonth) ?
			'<a class="ui-datepicker-next ui-corner-all" onclick="DP_jQuery_' + dpuuid +
			'.datepicker._adjustDate(\'#' + inst.id + '\', +' + stepMonths + ', \'M\');"' +
			' title="' + nextText + '"><span class="ui-icon ui-icon-circle-triangle-' + ( isRTL ? 'w' : 'e') + '">' + nextText + '</span></a>' :
			(hideIfNoPrevNext ? '' : '<a class="ui-datepicker-next ui-corner-all ui-state-disabled" title="'+ nextText + '"><span class="ui-icon ui-icon-circle-triangle-' + ( isRTL ? 'w' : 'e') + '">' + nextText + '</span></a>'));
		var currentText = this._get(inst, 'currentText');
		var gotoDate = (this._get(inst, 'gotoCurrent') && inst.currentDay ? currentDate : today);
		currentText = (!navigationAsDateFormat ? currentText :
			this.formatDate(currentText, gotoDate, this._getFormatConfig(inst)));
		var controls = (!inst.inline ? '<button type="button" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" onclick="DP_jQuery_' + dpuuid +
			'.datepicker._hideDatepicker();">' + this._get(inst, 'closeText') + '</button>' : '');
		var buttonPanel = (showButtonPanel) ? '<div class="ui-datepicker-buttonpane ui-widget-content">' + (isRTL ? controls : '') +
			(this._isInRange(inst, gotoDate) ? '<button type="button" class="ui-datepicker-current ui-state-default ui-priority-secondary ui-corner-all" onclick="DP_jQuery_' + dpuuid +
			'.datepicker._gotoToday(\'#' + inst.id + '\');"' +
			'>' + currentText + '</button>' : '') + (isRTL ? '' : controls) + '</div>' : '';
		var firstDay = parseInt(this._get(inst, 'firstDay'),10);
		firstDay = (isNaN(firstDay) ? 0 : firstDay);
		var showWeek = this._get(inst, 'showWeek');
		var dayNames = this._get(inst, 'dayNames');
		var dayNamesShort = this._get(inst, 'dayNamesShort');
		var dayNamesMin = this._get(inst, 'dayNamesMin');
		var monthNames = this._get(inst, 'monthNames');
		var monthNamesShort = this._get(inst, 'monthNamesShort');
		var beforeShowDay = this._get(inst, 'beforeShowDay');
		var showOtherMonths = this._get(inst, 'showOtherMonths');
		var selectOtherMonths = this._get(inst, 'selectOtherMonths');
		var calculateWeek = this._get(inst, 'calculateWeek') || this.iso8601Week;
		var defaultDate = this._getDefaultDate(inst);
		var html = '';
		for (var row = 0; row < numMonths[0]; row++) {
			var group = '';
			for (var col = 0; col < numMonths[1]; col++) {
				var selectedDate = this._daylightSavingAdjust(new Date(drawYear, drawMonth, inst.selectedDay));
				var cornerClass = ' ui-corner-all';
				var calender = '';
				if (isMultiMonth) {
					calender += '<div class="ui-datepicker-group';
					if (numMonths[1] > 1)
						switch (col) {
							case 0: calender += ' ui-datepicker-group-first';
								cornerClass = ' ui-corner-' + (isRTL ? 'right' : 'left'); break;
							case numMonths[1]-1: calender += ' ui-datepicker-group-last';
								cornerClass = ' ui-corner-' + (isRTL ? 'left' : 'right'); break;
							default: calender += ' ui-datepicker-group-middle'; cornerClass = ''; break;
						}
					calender += '">';
				}
				calender += '<div class="ui-datepicker-header ui-widget-header ui-helper-clearfix' + cornerClass + '">' +
					(/all|left/.test(cornerClass) && row == 0 ? (isRTL ? next : prev) : '') +
					(/all|right/.test(cornerClass) && row == 0 ? (isRTL ? prev : next) : '') +
					this._generateMonthYearHeader(inst, drawMonth, drawYear, minDate, maxDate,
					row > 0 || col > 0, monthNames, monthNamesShort) + // draw month headers
					'</div><table class="ui-datepicker-calendar"><thead>' +
					'<tr>';
				var thead = (showWeek ? '<th class="ui-datepicker-week-col">' + this._get(inst, 'weekHeader') + '</th>' : '');
				for (var dow = 0; dow < 7; dow++) { // days of the week
					var day = (dow + firstDay) % 7;
					thead += '<th' + ((dow + firstDay + 6) % 7 >= 5 ? ' class="ui-datepicker-week-end"' : '') + '>' +
						'<span title="' + dayNames[day] + '">' + dayNamesMin[day] + '</span></th>';
				}
				calender += thead + '</tr></thead><tbody>';
				var daysInMonth = this._getDaysInMonth(drawYear, drawMonth);
				if (drawYear == inst.selectedYear && drawMonth == inst.selectedMonth)
					inst.selectedDay = Math.min(inst.selectedDay, daysInMonth);
				var leadDays = (this._getFirstDayOfMonth(drawYear, drawMonth) - firstDay + 7) % 7;
				var numRows = (isMultiMonth ? 6 : Math.ceil((leadDays + daysInMonth) / 7)); // calculate the number of rows to generate
				var printDate = this._daylightSavingAdjust(new Date(drawYear, drawMonth, 1 - leadDays));
				for (var dRow = 0; dRow < numRows; dRow++) { // create date picker rows
					calender += '<tr>';
					var tbody = (!showWeek ? '' : '<td class="ui-datepicker-week-col">' +
						this._get(inst, 'calculateWeek')(printDate) + '</td>');
					for (var dow = 0; dow < 7; dow++) { // create date picker days
                        var isSpecialDate = this._isSpecialDate(specialDates, drawYear, drawMonth + 1, printDate.getDate());
						var daySettings = (beforeShowDay ?
							beforeShowDay.apply((inst.input ? inst.input[0] : null), [printDate]) : [true, '']);
						var otherMonth = (printDate.getMonth() != drawMonth);
						var unselectable = (otherMonth && !selectOtherMonths) || !daySettings[0] ||
							(minDate && printDate < minDate) || (maxDate && printDate > maxDate);
						tbody += '<td class="' +
							((dow + firstDay + 6) % 7 >= 5 ? ' ui-datepicker-week-end' : '') + // highlight weekends
							(otherMonth ? ' ui-datepicker-other-month' : '') + // highlight days from other months
							((printDate.getTime() == selectedDate.getTime() && drawMonth == inst.selectedMonth && inst._keyEvent) || // user pressed key
							(defaultDate.getTime() == printDate.getTime() && defaultDate.getTime() == selectedDate.getTime()) ?
							// or defaultDate is current printedDate and defaultDate is selectedDate
							' ' + this._dayOverClass : '') + // highlight selected day
							(unselectable ? ' ' + this._unselectableClass + ' ui-state-disabled': '') +  // highlight unselectable days
							(otherMonth && !showOtherMonths ? '' : ' ' + daySettings[1] + // highlight custom dates
							(printDate.getTime() == currentDate.getTime() ? ' ' + this._currentClass : '') + // highlight selected day
							(printDate.getTime() == today.getTime() ? ' ui-datepicker-today' : '')) + '"' + // highlight today (if different)
							((!otherMonth || showOtherMonths) && daySettings[2] ? ' title="' + daySettings[2] + '"' : '') + // cell title
							(unselectable ? '' : ' onclick="DP_jQuery_' + dpuuid + '.datepicker._selectDay(\'#' +
							inst.id + '\',' + printDate.getMonth() + ',' + printDate.getFullYear() + ', this);return false;"') + '>' + // actions
							(otherMonth && !showOtherMonths ? '&#xa0;' : // display for other months
							((displayOnly ? '<span class="ui-state-displayonly' : '<a class="ui-state-default') +
                            (printDate.getTime() == today.getTime() ? ' ui-state-specialDate' : '') +
                            (printDate.getTime() == today.getTime() ? ' ui-state-highlight' : '') +
                            (printDate.getTime() == currentDate.getTime() ? ' ui-state-active' : '') + // highlight selected day
                            (isSpecialDate ? ' ui-state-specialDate' : '') +
                            (otherMonth ? ' ui-priority-secondary' : '') + // distinguish dates from other months
                            (displayOnly ? '">' + printDate.getDate() + '</span></td>' : '" href="#">' + printDate.getDate() + '</a></td>')));
						printDate.setDate(printDate.getDate() + 1);
						printDate = this._daylightSavingAdjust(printDate);
					}
					calender += tbody + '</tr>';
				}
				drawMonth++;
				if (drawMonth > 11) {
					drawMonth = 0;
					drawYear++;
				}
				calender += '</tbody></table>' + (isMultiMonth ? '</div>' +
							((numMonths[0] > 0 && col == numMonths[1]-1) ? '<div class="ui-datepicker-row-break"></div>' : '') : '');
				group += calender;
			}
			html += group;
		}
		html += buttonPanel + ($.browser.msie && parseInt($.browser.version,10) < 7 && !inst.inline ?
			'<iframe src="javascript:false;" class="ui-datepicker-cover" frameborder="0"></iframe>' : '');
		inst._keyEvent = false;
		return html;
	},

	/* Generate the month and year header. */
	_generateMonthYearHeader: function(inst, drawMonth, drawYear, minDate, maxDate,
			secondary, monthNames, monthNamesShort) {
		var changeMonth = this._get(inst, 'changeMonth');
		var changeYear = this._get(inst, 'changeYear');
		var showMonthAfterYear = this._get(inst, 'showMonthAfterYear');
		var html = '<div class="ui-datepicker-title">';
		var monthHtml = '';
		// month selection
		if (secondary || !changeMonth)
			monthHtml += '<span class="ui-datepicker-month">' + monthNames[drawMonth] + '</span>';
		else {
			var inMinYear = (minDate && minDate.getFullYear() == drawYear);
			var inMaxYear = (maxDate && maxDate.getFullYear() == drawYear);
			monthHtml += '<select class="ui-datepicker-month" ' +
				'onchange="DP_jQuery_' + dpuuid + '.datepicker._selectMonthYear(\'#' + inst.id + '\', this, \'M\');" ' +
				'onclick="DP_jQuery_' + dpuuid + '.datepicker._clickMonthYear(\'#' + inst.id + '\');"' +
			 	'>';
			for (var month = 0; month < 12; month++) {
				if ((!inMinYear || month >= minDate.getMonth()) &&
						(!inMaxYear || month <= maxDate.getMonth()))
					monthHtml += '<option value="' + month + '"' +
						(month == drawMonth ? ' selected="selected"' : '') +
						'>' + monthNamesShort[month] + '</option>';
			}
			monthHtml += '</select>';
		}
		if (!showMonthAfterYear)
			html += monthHtml + (secondary || !(changeMonth && changeYear) ? '&#xa0;' : '');
		// year selection
		inst.yearshtml = '';
		if (secondary || !changeYear)
			html += '<span class="ui-datepicker-year">' + drawYear + '</span>';
		else {
			// determine range of years to display
			var years = this._get(inst, 'yearRange').split(':');
			var thisYear = new Date().getFullYear();
			var determineYear = function(value) {
				var year = (value.match(/c[+-].*/) ? drawYear + parseInt(value.substring(1), 10) :
					(value.match(/[+-].*/) ? thisYear + parseInt(value, 10) :
					parseInt(value, 10)));
				return (isNaN(year) ? thisYear : year);
			};
			var year = determineYear(years[0]);
			var endYear = Math.max(year, determineYear(years[1] || ''));
			year = (minDate ? Math.max(year, minDate.getFullYear()) : year);
			endYear = (maxDate ? Math.min(endYear, maxDate.getFullYear()) : endYear);
			inst.yearshtml += '<select class="ui-datepicker-year" ' +
				'onchange="DP_jQuery_' + dpuuid + '.datepicker._selectMonthYear(\'#' + inst.id + '\', this, \'Y\');" ' +
				'onclick="DP_jQuery_' + dpuuid + '.datepicker._clickMonthYear(\'#' + inst.id + '\');"' +
				'>';
			for (; year <= endYear; year++) {
				inst.yearshtml += '<option value="' + year + '"' +
					(year == drawYear ? ' selected="selected"' : '') +
					'>' + year + '</option>';
			}
			inst.yearshtml += '</select>';
			//when showing there is no need for later update
			if( ! $.browser.mozilla ){
				html += inst.yearshtml;
				inst.yearshtml = null;
			} else {
				// will be replaced later with inst.yearshtml
				html += '<select class="ui-datepicker-year"><option value="' + drawYear + '" selected="selected">' + drawYear + '</option></select>';
			}
		}
		html += this._get(inst, 'yearSuffix');
		if (showMonthAfterYear)
			html += (secondary || !(changeMonth && changeYear) ? '&#xa0;' : '') + monthHtml;
		html += '</div>'; // Close datepicker_header
		return html;
	},

	/* Adjust one of the date sub-fields. */
	_adjustInstDate: function(inst, offset, period) {
		var year = inst.drawYear + (period == 'Y' ? offset : 0);
		var month = inst.drawMonth + (period == 'M' ? offset : 0);
		var day = Math.min(inst.selectedDay, this._getDaysInMonth(year, month)) +
			(period == 'D' ? offset : 0);
		var date = this._restrictMinMax(inst,
			this._daylightSavingAdjust(new Date(year, month, day)));
		inst.selectedDay = date.getDate();
		inst.drawMonth = inst.selectedMonth = date.getMonth();
		inst.drawYear = inst.selectedYear = date.getFullYear();
		if (period == 'M' || period == 'Y')
			this._notifyChange(inst);
	},

	/* Ensure a date is within any min/max bounds. */
	_restrictMinMax: function(inst, date) {
		var minDate = this._getMinMaxDate(inst, 'min');
		var maxDate = this._getMinMaxDate(inst, 'max');
		var newDate = (minDate && date < minDate ? minDate : date);
		newDate = (maxDate && newDate > maxDate ? maxDate : newDate);
		return newDate;
	},

	/* Notify change of month/year. */
	_notifyChange: function(inst) {
		var onChange = this._get(inst, 'onChangeMonthYear');
		if (onChange)
			onChange.apply((inst.input ? inst.input[0] : null),
				[inst.selectedYear, inst.selectedMonth + 1, inst]);
	},

	/* Determine the number of months to show. */
	_getNumberOfMonths: function(inst) {
		var numMonths = this._get(inst, 'numberOfMonths');
		return (numMonths == null ? [1, 1] : (typeof numMonths == 'number' ? [1, numMonths] : numMonths));
	},

	/* Determine the current maximum date - ensure no time components are set. */
	_getMinMaxDate: function(inst, minMax) {
		return this._determineDate(inst, this._get(inst, minMax + 'Date'), null);
	},

	/* Find the number of days in a given month. */
	_getDaysInMonth: function(year, month) {
		return 32 - new Date(year, month, 32).getDate();
	},

	/* Find the day of the week of the first of a month. */
	_getFirstDayOfMonth: function(year, month) {
		return new Date(year, month, 1).getDay();
	},

	/* Determines if we should allow a "next/prev" month display change. */
	_canAdjustMonth: function(inst, offset, curYear, curMonth) {
		var numMonths = this._getNumberOfMonths(inst);
		var date = this._daylightSavingAdjust(new Date(curYear,
			curMonth + (offset < 0 ? offset : numMonths[0] * numMonths[1]), 1));
		if (offset < 0)
			date.setDate(this._getDaysInMonth(date.getFullYear(), date.getMonth()));
		return this._isInRange(inst, date);
	},

	/* Is the given date in the accepted range? */
	_isInRange: function(inst, date) {
		var minDate = this._getMinMaxDate(inst, 'min');
		var maxDate = this._getMinMaxDate(inst, 'max');
		return ((!minDate || date.getTime() >= minDate.getTime()) &&
			(!maxDate || date.getTime() <= maxDate.getTime()));
	},

	/* Provide the configuration settings for formatting/parsing. */
	_getFormatConfig: function(inst) {
		var shortYearCutoff = this._get(inst, 'shortYearCutoff');
		shortYearCutoff = (typeof shortYearCutoff != 'string' ? shortYearCutoff :
			new Date().getFullYear() % 100 + parseInt(shortYearCutoff, 10));
		return {shortYearCutoff: shortYearCutoff,
			dayNamesShort: this._get(inst, 'dayNamesShort'), dayNames: this._get(inst, 'dayNames'),
			monthNamesShort: this._get(inst, 'monthNamesShort'), monthNames: this._get(inst, 'monthNames')};
	},

	/* Format the given date for display. */
	_formatDate: function(inst, day, month, year) {
		if (!day) {
			inst.currentDay = inst.selectedDay;
			inst.currentMonth = inst.selectedMonth;
			inst.currentYear = inst.selectedYear;
		}
		var date = (day ? (typeof day == 'object' ? day :
			this._daylightSavingAdjust(new Date(year, month, day))) :
			this._daylightSavingAdjust(new Date(inst.currentYear, inst.currentMonth, inst.currentDay)));
		return this.formatDate(this._get(inst, 'dateFormat'), date, this._getFormatConfig(inst));
	}
});

/* jQuery extend now ignores nulls! */
function extendRemove(target, props) {
	$.extend(target, props);
	for (var name in props)
		if (props[name] == null || props[name] == undefined)
			target[name] = props[name];
	return target;
};

/* Determine whether an object is an array. */
function isArray(a) {
	return (a && (($.browser.safari && typeof a == 'object' && a.length) ||
		(a.constructor && a.constructor.toString().match(/\Array\(\)/))));
};

/* Invoke the datepicker functionality.
   @param  options  string - a command, optionally followed by additional parameters or
                    Object - settings for attaching new datepicker functionality
   @return  jQuery object */
$.fn.datepicker = function(options){

	/* Initialise the date picker. */
	if (!$.datepicker.initialized) {
		$(document).mousedown($.datepicker._checkExternalClick).
			find('body').append($.datepicker.dpDiv);
		$.datepicker.initialized = true;
	}

	var otherArgs = Array.prototype.slice.call(arguments, 1);
	if (typeof options == 'string' && (options == 'isDisabled' || options == 'getDate' || options == 'widget'))
		return $.datepicker['_' + options + 'Datepicker'].
			apply($.datepicker, [this[0]].concat(otherArgs));
	if (options == 'option' && arguments.length == 2 && typeof arguments[1] == 'string')
		return $.datepicker['_' + options + 'Datepicker'].
			apply($.datepicker, [this[0]].concat(otherArgs));
	return this.each(function() {
		typeof options == 'string' ?
			$.datepicker['_' + options + 'Datepicker'].
				apply($.datepicker, [this].concat(otherArgs)) :
			$.datepicker._attachDatepicker(this, options);
	});
};

$.datepicker = new Datepicker(); // singleton instance
$.datepicker.initialized = false;
$.datepicker.uuid = new Date().getTime();
$.datepicker.version = "@VERSION";

// Workaround for #4055
// Add another global to avoid noConflict issues with inline event handlers
window['DP_jQuery_' + dpuuid] = $;

})(jQuery);
/*! 
 * jquery.event.drag - v 2.0.0 
 * Copyright (c) 2010 Three Dub Media - http://threedubmedia.com
 * Open Source MIT License - http://threedubmedia.com/code/license
 */
;(function(f){f.fn.drag=function(b,a,d){var e=typeof b=="string"?b:"",k=f.isFunction(b)?b:f.isFunction(a)?a:null;if(e.indexOf("drag")!==0)e="drag"+e;d=(b==k?a:d)||{};return k?this.bind(e,d,k):this.trigger(e)};var i=f.event,h=i.special,c=h.drag={defaults:{which:1,distance:0,not:":input",handle:null,relative:false,drop:true,click:false},datakey:"dragdata",livekey:"livedrag",add:function(b){var a=f.data(this,c.datakey),d=b.data||{};a.related+=1;if(!a.live&&b.selector){a.live=true;i.add(this,"draginit."+ c.livekey,c.delegate)}f.each(c.defaults,function(e){if(d[e]!==undefined)a[e]=d[e]})},remove:function(){f.data(this,c.datakey).related-=1},setup:function(){if(!f.data(this,c.datakey)){var b=f.extend({related:0},c.defaults);f.data(this,c.datakey,b);i.add(this,"mousedown",c.init,b);this.attachEvent&&this.attachEvent("ondragstart",c.dontstart)}},teardown:function(){if(!f.data(this,c.datakey).related){f.removeData(this,c.datakey);i.remove(this,"mousedown",c.init);i.remove(this,"draginit",c.delegate);c.textselect(true); this.detachEvent&&this.detachEvent("ondragstart",c.dontstart)}},init:function(b){var a=b.data,d;if(!(a.which>0&&b.which!=a.which))if(!f(b.target).is(a.not))if(!(a.handle&&!f(b.target).closest(a.handle,b.currentTarget).length)){a.propagates=1;a.interactions=[c.interaction(this,a)];a.target=b.target;a.pageX=b.pageX;a.pageY=b.pageY;a.dragging=null;d=c.hijack(b,"draginit",a);if(a.propagates){if((d=c.flatten(d))&&d.length){a.interactions=[];f.each(d,function(){a.interactions.push(c.interaction(this,a))})}a.propagates= a.interactions.length;a.drop!==false&&h.drop&&h.drop.handler(b,a);c.textselect(false);i.add(document,"mousemove mouseup",c.handler,a);return false}}},interaction:function(b,a){return{drag:b,callback:new c.callback,droppable:[],offset:f(b)[a.relative?"position":"offset"]()||{top:0,left:0}}},handler:function(b){var a=b.data;switch(b.type){case !a.dragging&&"mousemove":if(Math.pow(b.pageX-a.pageX,2)+Math.pow(b.pageY-a.pageY,2)<Math.pow(a.distance,2))break;b.target=a.target;c.hijack(b,"dragstart",a); if(a.propagates)a.dragging=true;case "mousemove":if(a.dragging){c.hijack(b,"drag",a);if(a.propagates){a.drop!==false&&h.drop&&h.drop.handler(b,a);break}b.type="mouseup"}case "mouseup":i.remove(document,"mousemove mouseup",c.handler);if(a.dragging){a.drop!==false&&h.drop&&h.drop.handler(b,a);c.hijack(b,"dragend",a)}c.textselect(true);if(a.click===false&&a.dragging){jQuery.event.triggered=true;setTimeout(function(){jQuery.event.triggered=false},20);a.dragging=false}break}},delegate:function(b){var a= [],d,e=f.data(this,"events")||{};f.each(e.live||[],function(k,j){if(j.preType.indexOf("drag")===0)if(d=f(b.target).closest(j.selector,b.currentTarget)[0]){i.add(d,j.origType+"."+c.livekey,j.origHandler,j.data);f.inArray(d,a)<0&&a.push(d)}});if(!a.length)return false;return f(a).bind("dragend."+c.livekey,function(){i.remove(this,"."+c.livekey)})},hijack:function(b,a,d,e,k){if(d){var j={event:b.originalEvent,type:b.type},n=a.indexOf("drop")?"drag":"drop",l,o=e||0,g,m;e=!isNaN(e)?e:d.interactions.length; b.type=a;b.originalEvent=null;d.results=[];do if(g=d.interactions[o])if(!(a!=="dragend"&&g.cancelled)){m=c.properties(b,d,g);g.results=[];f(k||g[n]||d.droppable).each(function(q,p){l=(m.target=p)?i.handle.call(p,b,m):null;if(l===false){if(n=="drag"){g.cancelled=true;d.propagates-=1}if(a=="drop")g[n][q]=null}else if(a=="dropinit")g.droppable.push(c.element(l)||p);if(a=="dragstart")g.proxy=f(c.element(l)||g.drag)[0];g.results.push(l);delete b.result;if(a!=="dropinit")return l});d.results[o]=c.flatten(g.results); if(a=="dropinit")g.droppable=c.flatten(g.droppable);a=="dragstart"&&!g.cancelled&&m.update()}while(++o<e);b.type=j.type;b.originalEvent=j.event;return c.flatten(d.results)}},properties:function(b,a,d){var e=d.callback;e.drag=d.drag;e.proxy=d.proxy||d.drag;e.startX=a.pageX;e.startY=a.pageY;e.deltaX=b.pageX-a.pageX;e.deltaY=b.pageY-a.pageY;e.originalX=d.offset.left;e.originalY=d.offset.top;e.offsetX=b.pageX-(a.pageX-e.originalX);e.offsetY=b.pageY-(a.pageY-e.originalY);e.drop=c.flatten((d.drop||[]).slice()); e.available=c.flatten((d.droppable||[]).slice());return e},element:function(b){if(b&&(b.jquery||b.nodeType==1))return b},flatten:function(b){return f.map(b,function(a){return a&&a.jquery?f.makeArray(a):a&&a.length?c.flatten(a):a})},textselect:function(b){f(document)[b?"unbind":"bind"]("selectstart",c.dontstart).attr("unselectable",b?"off":"on").css("MozUserSelect",b?"":"none")},dontstart:function(){return false},callback:function(){}};c.callback.prototype={update:function(){h.drop&&this.available.length&& f.each(this.available,function(b){h.drop.locate(this,b)})}};h.draginit=h.dragstart=h.dragend=c})(jQuery);
/* Javascript plotting library for jQuery, v. 0.6.
 *
 * Released under the MIT license by IOLA, December 2007.
 *
 */

// first an inline dependency, jquery.colorhelpers.js, we inline it here
// for convenience

/* Plugin for jQuery for working with colors.
 * 
 * Version 1.0.
 * 
 * Inspiration from jQuery color animation plugin by John Resig.
 *
 * Released under the MIT license by Ole Laursen, October 2009.
 *
 * Examples:
 *
 *   $.color.parse("#fff").scale('rgb', 0.25).add('a', -0.5).toString()
 *   var c = $.color.extract($("#mydiv"), 'background-color');
 *   console.log(c.r, c.g, c.b, c.a);
 *   $.color.make(100, 50, 25, 0.4).toString() // returns "rgba(100,50,25,0.4)"
 *
 * Note that .scale() and .add() work in-place instead of returning
 * new objects.
 */ 
(function(){jQuery.color={};jQuery.color.make=function(E,D,B,C){var F={};F.r=E||0;F.g=D||0;F.b=B||0;F.a=C!=null?C:1;F.add=function(I,H){for(var G=0;G<I.length;++G){F[I.charAt(G)]+=H}return F.normalize()};F.scale=function(I,H){for(var G=0;G<I.length;++G){F[I.charAt(G)]*=H}return F.normalize()};F.toString=function(){if(F.a>=1){return"rgb("+[F.r,F.g,F.b].join(",")+")"}else{return"rgba("+[F.r,F.g,F.b,F.a].join(",")+")"}};F.normalize=function(){function G(I,J,H){return J<I?I:(J>H?H:J)}F.r=G(0,parseInt(F.r),255);F.g=G(0,parseInt(F.g),255);F.b=G(0,parseInt(F.b),255);F.a=G(0,F.a,1);return F};F.clone=function(){return jQuery.color.make(F.r,F.b,F.g,F.a)};return F.normalize()};jQuery.color.extract=function(C,B){var D;do{D=C.css(B).toLowerCase();if(D!=""&&D!="transparent"){break}C=C.parent()}while(!jQuery.nodeName(C.get(0),"body"));if(D=="rgba(0, 0, 0, 0)"){D="transparent"}return jQuery.color.parse(D)};jQuery.color.parse=function(E){var D,B=jQuery.color.make;if(D=/rgb\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*\)/.exec(E)){return B(parseInt(D[1],10),parseInt(D[2],10),parseInt(D[3],10))}if(D=/rgba\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]+(?:\.[0-9]+)?)\s*\)/.exec(E)){return B(parseInt(D[1],10),parseInt(D[2],10),parseInt(D[3],10),parseFloat(D[4]))}if(D=/rgb\(\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*\)/.exec(E)){return B(parseFloat(D[1])*2.55,parseFloat(D[2])*2.55,parseFloat(D[3])*2.55)}if(D=/rgba\(\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\s*\)/.exec(E)){return B(parseFloat(D[1])*2.55,parseFloat(D[2])*2.55,parseFloat(D[3])*2.55,parseFloat(D[4]))}if(D=/#([a-fA-F0-9]{2})([a-fA-F0-9]{2})([a-fA-F0-9]{2})/.exec(E)){return B(parseInt(D[1],16),parseInt(D[2],16),parseInt(D[3],16))}if(D=/#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])/.exec(E)){return B(parseInt(D[1]+D[1],16),parseInt(D[2]+D[2],16),parseInt(D[3]+D[3],16))}var C=jQuery.trim(E).toLowerCase();if(C=="transparent"){return B(255,255,255,0)}else{D=A[C];return B(D[0],D[1],D[2])}};var A={aqua:[0,255,255],azure:[240,255,255],beige:[245,245,220],black:[0,0,0],blue:[0,0,255],brown:[165,42,42],cyan:[0,255,255],darkblue:[0,0,139],darkcyan:[0,139,139],darkgrey:[169,169,169],darkgreen:[0,100,0],darkkhaki:[189,183,107],darkmagenta:[139,0,139],darkolivegreen:[85,107,47],darkorange:[255,140,0],darkorchid:[153,50,204],darkred:[139,0,0],darksalmon:[233,150,122],darkviolet:[148,0,211],fuchsia:[255,0,255],gold:[255,215,0],green:[0,128,0],indigo:[75,0,130],khaki:[240,230,140],lightblue:[173,216,230],lightcyan:[224,255,255],lightgreen:[144,238,144],lightgrey:[211,211,211],lightpink:[255,182,193],lightyellow:[255,255,224],lime:[0,255,0],magenta:[255,0,255],maroon:[128,0,0],navy:[0,0,128],olive:[128,128,0],orange:[255,165,0],pink:[255,192,203],purple:[128,0,128],violet:[128,0,128],red:[255,0,0],silver:[192,192,192],white:[255,255,255],yellow:[255,255,0]}})();

// the actual Flot code
(function($) {
    function Plot(placeholder, data_, options_, plugins) {
        // data is on the form:
        //   [ series1, series2 ... ]
        // where series is either just the data as [ [x1, y1], [x2, y2], ... ]
        // or { data: [ [x1, y1], [x2, y2], ... ], label: "some label", ... }
        
        var series = [],
            options = {
                // the color theme used for graphs
                colors: ["#edc240", "#afd8f8", "#cb4b4b", "#4da74d", "#9440ed"],
                legend: {
                    show: true,
                    noColumns: 1, // number of colums in legend table
                    labelFormatter: null, // fn: string -> string
                    labelBoxBorderColor: "#ccc", // border color for the little label boxes
                    container: null, // container (as jQuery object) to put legend in, null means default on top of graph
                    position: "ne", // position of default legend container within plot
                    margin: 5, // distance from grid edge to default legend container within plot
                    backgroundColor: null, // null means auto-detect
                    backgroundOpacity: 0.85 // set to 0 to avoid background
                },
                xaxis: {
                    mode: null, // null or "time"
                    transform: null, // null or f: number -> number to transform axis
                    inverseTransform: null, // if transform is set, this should be the inverse function
                    min: null, // min. value to show, null means set automatically
                    max: null, // max. value to show, null means set automatically
                    autoscaleMargin: null, // margin in % to add if auto-setting min/max
                    ticks: null, // either [1, 3] or [[1, "a"], 3] or (fn: axis info -> ticks) or app. number of ticks for auto-ticks
                    tickFormatter: null, // fn: number -> string
                    labelWidth: null, // size of tick labels in pixels
                    labelHeight: null,
                    
                    // mode specific options
                    tickDecimals: null, // no. of decimals, null means auto
                    tickSize: null, // number or [number, "unit"]
                    minTickSize: null, // number or [number, "unit"]
                    monthNames: null, // list of names of months
                    timeformat: null, // format string to use
                    twelveHourClock: false // 12 or 24 time in time mode
                },
                yaxis: {
                    autoscaleMargin: 0.02
                },
                x2axis: {
                    autoscaleMargin: null
                },
                y2axis: {
                    autoscaleMargin: 0.02
                },
                series: {
                    points: {
                        show: false,
                        radius: 3,
                        lineWidth: 2, // in pixels
                        fill: true,
                        fillColor: "#ffffff"
                    },
                    lines: {
                        // we don't put in show: false so we can see
                        // whether lines were actively disabled 
                        lineWidth: 2, // in pixels
                        fill: false,
                        fillColor: null,
                        steps: false
                    },
                    bars: {
                        show: false,
                        lineWidth: 2, // in pixels
                        barWidth: 1, // in units of the x axis
                        fill: true,
                        fillColor: null,
                        align: "left", // or "center" 
                        horizontal: false // when horizontal, left is now top
                    },
                    shadowSize: 3
                },
                grid: {
                    show: true,
                    aboveData: false,
                    color: "#545454", // primary color used for outline and labels
                    backgroundColor: null, // null for transparent, else color
                    tickColor: "rgba(0,0,0,0.15)", // color used for the ticks
                    labelMargin: 5, // in pixels
                    borderWidth: 2, // in pixels
                    borderColor: null, // set if different from the grid color
                    markings: null, // array of ranges or fn: axes -> array of ranges
                    markingsColor: "#f4f4f4",
                    markingsLineWidth: 2,
                    // interactive stuff
                    clickable: false,
                    hoverable: false,
                    autoHighlight: true, // highlight in case mouse is near
                    mouseActiveRadius: 10 // how far the mouse can be away to activate an item
                },
                hooks: {}
            },
        canvas = null,      // the canvas for the plot itself
        overlay = null,     // canvas for interactive stuff on top of plot
        eventHolder = null, // jQuery object that events should be bound to
        ctx = null, octx = null,
        axes = { xaxis: {}, yaxis: {}, x2axis: {}, y2axis: {} },
        plotOffset = { left: 0, right: 0, top: 0, bottom: 0},
        canvasWidth = 0, canvasHeight = 0,
        plotWidth = 0, plotHeight = 0,
        hooks = {
            processOptions: [],
            processRawData: [],
            processDatapoints: [],
            draw: [],
            bindEvents: [],
            drawOverlay: []
        },
        plot = this;

        // public functions
        plot.setData = setData;
        plot.setupGrid = setupGrid;
        plot.draw = draw;
        plot.getPlaceholder = function() { return placeholder; };
        plot.getCanvas = function() { return canvas; };
        plot.getPlotOffset = function() { return plotOffset; };
        plot.width = function () { return plotWidth; };
        plot.height = function () { return plotHeight; };
        plot.offset = function () {
            var o = eventHolder.offset();
            o.left += plotOffset.left;
            o.top += plotOffset.top;
            return o;
        };
        plot.getData = function() { return series; };
        plot.getAxes = function() { return axes; };
        plot.getOptions = function() { return options; };
        plot.highlight = highlight;
        plot.unhighlight = unhighlight;
        plot.triggerRedrawOverlay = triggerRedrawOverlay;
        plot.pointOffset = function(point) {
            return { left: parseInt(axisSpecToRealAxis(point, "xaxis").p2c(+point.x) + plotOffset.left),
                     top: parseInt(axisSpecToRealAxis(point, "yaxis").p2c(+point.y) + plotOffset.top) };
        };
        

        // public attributes
        plot.hooks = hooks;
        
        // initialize
        initPlugins(plot);
        parseOptions(options_);
        constructCanvas();
        setData(data_);
        setupGrid();
        draw();
        bindEvents();


        function executeHooks(hook, args) {
            args = [plot].concat(args);
            for (var i = 0; i < hook.length; ++i)
                hook[i].apply(this, args);
        }

        function initPlugins() {
            for (var i = 0; i < plugins.length; ++i) {
                var p = plugins[i];
                p.init(plot);
                if (p.options)
                    $.extend(true, options, p.options);
            }
        }
        
        function parseOptions(opts) {
            $.extend(true, options, opts);
            if (options.grid.borderColor == null)
                options.grid.borderColor = options.grid.color;
            // backwards compatibility, to be removed in future
            if (options.xaxis.noTicks && options.xaxis.ticks == null)
                options.xaxis.ticks = options.xaxis.noTicks;
            if (options.yaxis.noTicks && options.yaxis.ticks == null)
                options.yaxis.ticks = options.yaxis.noTicks;
            if (options.grid.coloredAreas)
                options.grid.markings = options.grid.coloredAreas;
            if (options.grid.coloredAreasColor)
                options.grid.markingsColor = options.grid.coloredAreasColor;
            if (options.lines)
                $.extend(true, options.series.lines, options.lines);
            if (options.points)
                $.extend(true, options.series.points, options.points);
            if (options.bars)
                $.extend(true, options.series.bars, options.bars);
            if (options.shadowSize)
                options.series.shadowSize = options.shadowSize;

            for (var n in hooks)
                if (options.hooks[n] && options.hooks[n].length)
                    hooks[n] = hooks[n].concat(options.hooks[n]);

            executeHooks(hooks.processOptions, [options]);
        }

        function setData(d) {
            series = parseData(d);
            fillInSeriesOptions();
            processData();
        }
        
        function parseData(d) {
            var res = [];
            for (var i = 0; i < d.length; ++i) {
                var s = $.extend(true, {}, options.series);

                if (d[i].data) {
                    s.data = d[i].data; // move the data instead of deep-copy
                    delete d[i].data;

                    $.extend(true, s, d[i]);

                    d[i].data = s.data;
                }
                else
                    s.data = d[i];
                res.push(s);
            }

            return res;
        }
        
        function axisSpecToRealAxis(obj, attr) {
            var a = obj[attr];
            if (!a || a == 1)
                return axes[attr];
            if (typeof a == "number")
                return axes[attr.charAt(0) + a + attr.slice(1)];
            return a; // assume it's OK
        }
        
        function fillInSeriesOptions() {
            var i;
            
            // collect what we already got of colors
            var neededColors = series.length,
                usedColors = [],
                assignedColors = [];
            for (i = 0; i < series.length; ++i) {
                var sc = series[i].color;
                if (sc != null) {
                    --neededColors;
                    if (typeof sc == "number")
                        assignedColors.push(sc);
                    else
                        usedColors.push($.color.parse(series[i].color));
                }
            }
            
            // we might need to generate more colors if higher indices
            // are assigned
            for (i = 0; i < assignedColors.length; ++i) {
                neededColors = Math.max(neededColors, assignedColors[i] + 1);
            }

            // produce colors as needed
            var colors = [], variation = 0;
            i = 0;
            while (colors.length < neededColors) {
                var c;
                if (options.colors.length == i) // check degenerate case
                    c = $.color.make(100, 100, 100);
                else
                    c = $.color.parse(options.colors[i]);

                // vary color if needed
                var sign = variation % 2 == 1 ? -1 : 1;
                c.scale('rgb', 1 + sign * Math.ceil(variation / 2) * 0.2)

                // FIXME: if we're getting to close to something else,
                // we should probably skip this one
                colors.push(c);
                
                ++i;
                if (i >= options.colors.length) {
                    i = 0;
                    ++variation;
                }
            }

            // fill in the options
            var colori = 0, s;
            for (i = 0; i < series.length; ++i) {
                s = series[i];
                
                // assign colors
                if (s.color == null) {
                    s.color = colors[colori].toString();
                    ++colori;
                }
                else if (typeof s.color == "number")
                    s.color = colors[s.color].toString();

                // turn on lines automatically in case nothing is set
                if (s.lines.show == null) {
                    var v, show = true;
                    for (v in s)
                        if (s[v].show) {
                            show = false;
                            break;
                        }
                    if (show)
                        s.lines.show = true;
                }

                // setup axes
                s.xaxis = axisSpecToRealAxis(s, "xaxis");
                s.yaxis = axisSpecToRealAxis(s, "yaxis");
            }
        }
        
        function processData() {
            var topSentry = Number.POSITIVE_INFINITY,
                bottomSentry = Number.NEGATIVE_INFINITY,
                i, j, k, m, length,
                s, points, ps, x, y, axis, val, f, p;

            for (axis in axes) {
                axes[axis].datamin = topSentry;
                axes[axis].datamax = bottomSentry;
                axes[axis].used = false;
            }

            function updateAxis(axis, min, max) {
                if (min < axis.datamin)
                    axis.datamin = min;
                if (max > axis.datamax)
                    axis.datamax = max;
            }

            for (i = 0; i < series.length; ++i) {
                s = series[i];
                s.datapoints = { points: [] };
                
                executeHooks(hooks.processRawData, [ s, s.data, s.datapoints ]);
            }
            
            // first pass: clean and copy data
            for (i = 0; i < series.length; ++i) {
                s = series[i];

                var data = s.data, format = s.datapoints.format;

                if (!format) {
                    format = [];
                    // find out how to copy
                    format.push({ x: true, number: true, required: true });
                    format.push({ y: true, number: true, required: true });

                    if (s.bars.show)
                        format.push({ y: true, number: true, required: false, defaultValue: 0 });
                    
                    s.datapoints.format = format;
                }

                if (s.datapoints.pointsize != null)
                    continue; // already filled in

                if (s.datapoints.pointsize == null)
                    s.datapoints.pointsize = format.length;
                
                ps = s.datapoints.pointsize;
                points = s.datapoints.points;

                insertSteps = s.lines.show && s.lines.steps;
                s.xaxis.used = s.yaxis.used = true;
                
                for (j = k = 0; j < data.length; ++j, k += ps) {
                    p = data[j];

                    var nullify = p == null;
                    if (!nullify) {
                        for (m = 0; m < ps; ++m) {
                            val = p[m];
                            f = format[m];

                            if (f) {
                                if (f.number && val != null) {
                                    val = +val; // convert to number
                                    if (isNaN(val))
                                        val = null;
                                }

                                if (val == null) {
                                    if (f.required)
                                        nullify = true;
                                    
                                    if (f.defaultValue != null)
                                        val = f.defaultValue;
                                }
                            }
                            
                            points[k + m] = val;
                        }
                    }
                    
                    if (nullify) {
                        for (m = 0; m < ps; ++m) {
                            val = points[k + m];
                            if (val != null) {
                                f = format[m];
                                // extract min/max info
                                if (f.x)
                                    updateAxis(s.xaxis, val, val);
                                if (f.y)
                                    updateAxis(s.yaxis, val, val);
                            }
                            points[k + m] = null;
                        }
                    }
                    else {
                        // a little bit of line specific stuff that
                        // perhaps shouldn't be here, but lacking
                        // better means...
                        if (insertSteps && k > 0
                            && points[k - ps] != null
                            && points[k - ps] != points[k]
                            && points[k - ps + 1] != points[k + 1]) {
                            // copy the point to make room for a middle point
                            for (m = 0; m < ps; ++m)
                                points[k + ps + m] = points[k + m];

                            // middle point has same y
                            points[k + 1] = points[k - ps + 1];

                            // we've added a point, better reflect that
                            k += ps;
                        }
                    }
                }
            }

            // give the hooks a chance to run
            for (i = 0; i < series.length; ++i) {
                s = series[i];
                
                executeHooks(hooks.processDatapoints, [ s, s.datapoints]);
            }

            // second pass: find datamax/datamin for auto-scaling
            for (i = 0; i < series.length; ++i) {
                s = series[i];
                points = s.datapoints.points,
                ps = s.datapoints.pointsize;

                var xmin = topSentry, ymin = topSentry,
                    xmax = bottomSentry, ymax = bottomSentry;
                
                for (j = 0; j < points.length; j += ps) {
                    if (points[j] == null)
                        continue;

                    for (m = 0; m < ps; ++m) {
                        val = points[j + m];
                        f = format[m];
                        if (!f)
                            continue;
                        
                        if (f.x) {
                            if (val < xmin)
                                xmin = val;
                            if (val > xmax)
                                xmax = val;
                        }
                        if (f.y) {
                            if (val < ymin)
                                ymin = val;
                            if (val > ymax)
                                ymax = val;
                        }
                    }
                }
                
                if (s.bars.show) {
                    // make sure we got room for the bar on the dancing floor
                    var delta = s.bars.align == "left" ? 0 : -s.bars.barWidth/2;
                    if (s.bars.horizontal) {
                        ymin += delta;
                        ymax += delta + s.bars.barWidth;
                    }
                    else {
                        xmin += delta;
                        xmax += delta + s.bars.barWidth;
                    }
                }
                
                updateAxis(s.xaxis, xmin, xmax);
                updateAxis(s.yaxis, ymin, ymax);
            }

            for (axis in axes) {
                if (axes[axis].datamin == topSentry)
                    axes[axis].datamin = null;
                if (axes[axis].datamax == bottomSentry)
                    axes[axis].datamax = null;
            }
        }

        function constructCanvas() {
            function makeCanvas(width, height) {
                var c = document.createElement('canvas');
                c.width = width;
                c.height = height;
                if ($.browser.msie) // excanvas hack
                    c = window.G_vmlCanvasManager.initElement(c);
                return c;
            }
            
            canvasWidth = placeholder.width();
            canvasHeight = placeholder.height();
            placeholder.html(""); // clear placeholder
            if (placeholder.css("position") == 'static')
                placeholder.css("position", "relative"); // for positioning labels and overlay

            if (canvasWidth <= 0 || canvasHeight <= 0)
                throw "Invalid dimensions for plot, width = " + canvasWidth + ", height = " + canvasHeight;

            if ($.browser.msie) // excanvas hack
                window.G_vmlCanvasManager.init_(document); // make sure everything is setup
            
            // the canvas
            canvas = $(makeCanvas(canvasWidth, canvasHeight)).appendTo(placeholder).get(0);
            ctx = canvas.getContext("2d");

            // overlay canvas for interactive features
            overlay = $(makeCanvas(canvasWidth, canvasHeight)).css({ position: 'absolute', left: 0, top: 0 }).appendTo(placeholder).get(0);
            octx = overlay.getContext("2d");
            octx.stroke();
        }

        function bindEvents() {
            // we include the canvas in the event holder too, because IE 7
            // sometimes has trouble with the stacking order
            eventHolder = $([overlay, canvas]);

            // bind events
            if (options.grid.hoverable)
                eventHolder.mousemove(onMouseMove);

            if (options.grid.clickable)
                eventHolder.click(onClick);

            executeHooks(hooks.bindEvents, [eventHolder]);
        }

        function setupGrid() {
            function setTransformationHelpers(axis, o) {
                function identity(x) { return x; }
                
                var s, m, t = o.transform || identity,
                    it = o.inverseTransform;
                    
                // add transformation helpers
                if (axis == axes.xaxis || axis == axes.x2axis) {
                    // precompute how much the axis is scaling a point
                    // in canvas space
                    s = axis.scale = plotWidth / (t(axis.max) - t(axis.min));
                    m = t(axis.min);

                    // data point to canvas coordinate
                    if (t == identity) // slight optimization
                        axis.p2c = function (p) { return (p - m) * s; };
                    else
                        axis.p2c = function (p) { return (t(p) - m) * s; };
                    // canvas coordinate to data point
                    if (!it)
                        axis.c2p = function (c) { return m + c / s; };
                    else
                        axis.c2p = function (c) { return it(m + c / s); };
                }
                else {
                    s = axis.scale = plotHeight / (t(axis.max) - t(axis.min));
                    m = t(axis.max);
                    
                    if (t == identity)
                        axis.p2c = function (p) { return (m - p) * s; };
                    else
                        axis.p2c = function (p) { return (m - t(p)) * s; };
                    if (!it)
                        axis.c2p = function (c) { return m - c / s; };
                    else
                        axis.c2p = function (c) { return it(m - c / s); };
                }
            }

            function measureLabels(axis, axisOptions) {
                var i, labels = [], l;
                
                axis.labelWidth = axisOptions.labelWidth;
                axis.labelHeight = axisOptions.labelHeight;

                if (axis == axes.xaxis || axis == axes.x2axis) {
                    // to avoid measuring the widths of the labels, we
                    // construct fixed-size boxes and put the labels inside
                    // them, we don't need the exact figures and the
                    // fixed-size box content is easy to center
                    if (axis.labelWidth == null)
                        axis.labelWidth = canvasWidth / (axis.ticks.length > 0 ? axis.ticks.length : 1);

                    // measure x label heights
                    if (axis.labelHeight == null) {
                        labels = [];
                        for (i = 0; i < axis.ticks.length; ++i) {
                            l = axis.ticks[i].label;
                            if (l)
                                labels.push('<div class="tickLabel" style="float:left;width:' + axis.labelWidth + 'px">' + l + '</div>');
                        }
                        
                        if (labels.length > 0) {
                            var dummyDiv = $('<div style="position:absolute;top:-10000px;width:10000px;font-size:smaller">'
                                             + labels.join("") + '<div style="clear:left"></div></div>').appendTo(placeholder);
                            axis.labelHeight = dummyDiv.height();
                            dummyDiv.remove();
                        }
                    }
                }
                else if (axis.labelWidth == null || axis.labelHeight == null) {
                    // calculate y label dimensions
                    for (i = 0; i < axis.ticks.length; ++i) {
                        l = axis.ticks[i].label;
                        if (l)
                            labels.push('<div class="tickLabel">' + l + '</div>');
                    }
                    
                    if (labels.length > 0) {
                        var dummyDiv = $('<div style="position:absolute;top:-10000px;font-size:smaller">'
                                         + labels.join("") + '</div>').appendTo(placeholder);
                        if (axis.labelWidth == null)
                            axis.labelWidth = dummyDiv.width();
                        if (axis.labelHeight == null)
                            axis.labelHeight = dummyDiv.find("div").height();
                        dummyDiv.remove();
                    }
                    
                }

                if (axis.labelWidth == null)
                    axis.labelWidth = 0;
                if (axis.labelHeight == null)
                    axis.labelHeight = 0;
            }
            
            function setGridSpacing() {
                // get the most space needed around the grid for things
                // that may stick out
                var maxOutset = options.grid.borderWidth;
                for (i = 0; i < series.length; ++i)
                    maxOutset = Math.max(maxOutset, 2 * (series[i].points.radius + series[i].points.lineWidth/2));
                
                plotOffset.left = plotOffset.right = plotOffset.top = plotOffset.bottom = maxOutset;
                
                var margin = options.grid.labelMargin + options.grid.borderWidth;
                
                if (axes.xaxis.labelHeight > 0)
                    plotOffset.bottom = Math.max(maxOutset, axes.xaxis.labelHeight + margin);
                if (axes.yaxis.labelWidth > 0)
                    plotOffset.left = Math.max(maxOutset, axes.yaxis.labelWidth + margin);
                if (axes.x2axis.labelHeight > 0)
                    plotOffset.top = Math.max(maxOutset, axes.x2axis.labelHeight + margin);
                if (axes.y2axis.labelWidth > 0)
                    plotOffset.right = Math.max(maxOutset, axes.y2axis.labelWidth + margin);
            
                plotWidth = canvasWidth - plotOffset.left - plotOffset.right;
                plotHeight = canvasHeight - plotOffset.bottom - plotOffset.top;
            }
            
            var axis;
            for (axis in axes)
                setRange(axes[axis], options[axis]);
            
            if (options.grid.show) {
                for (axis in axes) {
                    prepareTickGeneration(axes[axis], options[axis]);
                    setTicks(axes[axis], options[axis]);
                    measureLabels(axes[axis], options[axis]);
                }

                setGridSpacing();
            }
            else {
                plotOffset.left = plotOffset.right = plotOffset.top = plotOffset.bottom = 0;
                plotWidth = canvasWidth;
                plotHeight = canvasHeight;
            }
            
            for (axis in axes)
                setTransformationHelpers(axes[axis], options[axis]);

            if (options.grid.show)
                insertLabels();
            
            insertLegend();
        }
        
        function setRange(axis, axisOptions) {
            var min = +(axisOptions.min != null ? axisOptions.min : axis.datamin),
                max = +(axisOptions.max != null ? axisOptions.max : axis.datamax),
                delta = max - min;

            if (delta == 0.0) {
                // degenerate case
                var widen = max == 0 ? 1 : 0.01;

                if (axisOptions.min == null)
                    min -= widen;
                // alway widen max if we couldn't widen min to ensure we
                // don't fall into min == max which doesn't work
                if (axisOptions.max == null || axisOptions.min != null)
                    max += widen;
            }
            else {
                // consider autoscaling
                var margin = axisOptions.autoscaleMargin;
                if (margin != null) {
                    if (axisOptions.min == null) {
                        min -= delta * margin;
                        // make sure we don't go below zero if all values
                        // are positive
                        if (min < 0 && axis.datamin != null && axis.datamin >= 0)
                            min = 0;
                    }
                    if (axisOptions.max == null) {
                        max += delta * margin;
                        if (max > 0 && axis.datamax != null && axis.datamax <= 0)
                            max = 0;
                    }
                }
            }
            axis.min = min;
            axis.max = max;
        }

        function prepareTickGeneration(axis, axisOptions) {
            // estimate number of ticks
            var noTicks;
            if (typeof axisOptions.ticks == "number" && axisOptions.ticks > 0)
                noTicks = axisOptions.ticks;
            else if (axis == axes.xaxis || axis == axes.x2axis)
                 // heuristic based on the model a*sqrt(x) fitted to
                 // some reasonable data points
                noTicks = 0.3 * Math.sqrt(canvasWidth);
            else
                noTicks = 0.3 * Math.sqrt(canvasHeight);
            
            var delta = (axis.max - axis.min) / noTicks,
                size, generator, unit, formatter, i, magn, norm;

            if (axisOptions.mode == "time") {
                // pretty handling of time
                
                // map of app. size of time units in milliseconds
                var timeUnitSize = {
                    "second": 1000,
                    "minute": 60 * 1000,
                    "hour": 60 * 60 * 1000,
                    "day": 24 * 60 * 60 * 1000,
                    "month": 30 * 24 * 60 * 60 * 1000,
                    "year": 365.2425 * 24 * 60 * 60 * 1000
                };


                // the allowed tick sizes, after 1 year we use
                // an integer algorithm
                var spec = [
                    [1, "second"], [2, "second"], [5, "second"], [10, "second"],
                    [30, "second"], 
                    [1, "minute"], [2, "minute"], [5, "minute"], [10, "minute"],
                    [30, "minute"], 
                    [1, "hour"], [2, "hour"], [4, "hour"],
                    [8, "hour"], [12, "hour"],
                    [1, "day"], [2, "day"], [3, "day"],
                    [0.25, "month"], [0.5, "month"], [1, "month"],
                    [2, "month"], [3, "month"], [6, "month"],
                    [1, "year"]
                ];

                var minSize = 0;
                if (axisOptions.minTickSize != null) {
                    if (typeof axisOptions.tickSize == "number")
                        minSize = axisOptions.tickSize;
                    else
                        minSize = axisOptions.minTickSize[0] * timeUnitSize[axisOptions.minTickSize[1]];
                }

                for (i = 0; i < spec.length - 1; ++i)
                    if (delta < (spec[i][0] * timeUnitSize[spec[i][1]]
                                 + spec[i + 1][0] * timeUnitSize[spec[i + 1][1]]) / 2
                       && spec[i][0] * timeUnitSize[spec[i][1]] >= minSize)
                        break;
                size = spec[i][0];
                unit = spec[i][1];
                
                // special-case the possibility of several years
                if (unit == "year") {
                    magn = Math.pow(10, Math.floor(Math.log(delta / timeUnitSize.year) / Math.LN10));
                    norm = (delta / timeUnitSize.year) / magn;
                    if (norm < 1.5)
                        size = 1;
                    else if (norm < 3)
                        size = 2;
                    else if (norm < 7.5)
                        size = 5;
                    else
                        size = 10;

                    size *= magn;
                }

                if (axisOptions.tickSize) {
                    size = axisOptions.tickSize[0];
                    unit = axisOptions.tickSize[1];
                }
                
                generator = function(axis) {
                    var ticks = [],
                        tickSize = axis.tickSize[0], unit = axis.tickSize[1],
                        d = new Date(axis.min);
                    
                    var step = tickSize * timeUnitSize[unit];

                    if (unit == "second")
                        d.setUTCSeconds(floorInBase(d.getUTCSeconds(), tickSize));
                    if (unit == "minute")
                        d.setUTCMinutes(floorInBase(d.getUTCMinutes(), tickSize));
                    if (unit == "hour")
                        d.setUTCHours(floorInBase(d.getUTCHours(), tickSize));
                    if (unit == "month")
                        d.setUTCMonth(floorInBase(d.getUTCMonth(), tickSize));
                    if (unit == "year")
                        d.setUTCFullYear(floorInBase(d.getUTCFullYear(), tickSize));
                    
                    // reset smaller components
                    d.setUTCMilliseconds(0);
                    if (step >= timeUnitSize.minute)
                        d.setUTCSeconds(0);
                    if (step >= timeUnitSize.hour)
                        d.setUTCMinutes(0);
                    if (step >= timeUnitSize.day)
                        d.setUTCHours(0);
                    if (step >= timeUnitSize.day * 4)
                        d.setUTCDate(1);
                    if (step >= timeUnitSize.year)
                        d.setUTCMonth(0);


                    var carry = 0, v = Number.NaN, prev;
                    do {
                        prev = v;
                        v = d.getTime();
                        ticks.push({ v: v, label: axis.tickFormatter(v, axis) });
                        if (unit == "month") {
                            if (tickSize < 1) {
                                // a bit complicated - we'll divide the month
                                // up but we need to take care of fractions
                                // so we don't end up in the middle of a day
                                d.setUTCDate(1);
                                var start = d.getTime();
                                d.setUTCMonth(d.getUTCMonth() + 1);
                                var end = d.getTime();
                                d.setTime(v + carry * timeUnitSize.hour + (end - start) * tickSize);
                                carry = d.getUTCHours();
                                d.setUTCHours(0);
                            }
                            else
                                d.setUTCMonth(d.getUTCMonth() + tickSize);
                        }
                        else if (unit == "year") {
                            d.setUTCFullYear(d.getUTCFullYear() + tickSize);
                        }
                        else
                            d.setTime(v + step);
                    } while (v < axis.max && v != prev);

                    return ticks;
                };

                formatter = function (v, axis) {
                    var d = new Date(v);

                    // first check global format
                    if (axisOptions.timeformat != null)
                        return $.plot.formatDate(d, axisOptions.timeformat, axisOptions.monthNames);
                    
                    var t = axis.tickSize[0] * timeUnitSize[axis.tickSize[1]];
                    var span = axis.max - axis.min;
                    var suffix = (axisOptions.twelveHourClock) ? " %p" : "";
                    
                    if (t < timeUnitSize.minute)
                        fmt = "%h:%M:%S" + suffix;
                    else if (t < timeUnitSize.day) {
                        if (span < 2 * timeUnitSize.day)
                            fmt = "%h:%M" + suffix;
                        else
                            fmt = "%b %d %h:%M" + suffix;
                    }
                    else if (t < timeUnitSize.month)
                        fmt = "%b %d";
                    else if (t < timeUnitSize.year) {
                        if (span < timeUnitSize.year)
                            fmt = "%b";
                        else
                            fmt = "%b %y";
                    }
                    else
                        fmt = "%y";
                    
                    return $.plot.formatDate(d, fmt, axisOptions.monthNames);
                };
            }
            else {
                // pretty rounding of base-10 numbers
                var maxDec = axisOptions.tickDecimals;
                var dec = -Math.floor(Math.log(delta) / Math.LN10);
                if (maxDec != null && dec > maxDec)
                    dec = maxDec;

                magn = Math.pow(10, -dec);
                norm = delta / magn; // norm is between 1.0 and 10.0
                
                if (norm < 1.5)
                    size = 1;
                else if (norm < 3) {
                    size = 2;
                    // special case for 2.5, requires an extra decimal
                    if (norm > 2.25 && (maxDec == null || dec + 1 <= maxDec)) {
                        size = 2.5;
                        ++dec;
                    }
                }
                else if (norm < 7.5)
                    size = 5;
                else
                    size = 10;

                size *= magn;
                
                if (axisOptions.minTickSize != null && size < axisOptions.minTickSize)
                    size = axisOptions.minTickSize;

                if (axisOptions.tickSize != null)
                    size = axisOptions.tickSize;

                axis.tickDecimals = Math.max(0, (maxDec != null) ? maxDec : dec);

                generator = function (axis) {
                    var ticks = [];

                    // spew out all possible ticks
                    var start = floorInBase(axis.min, axis.tickSize),
                        i = 0, v = Number.NaN, prev;
                    do {
                        prev = v;
                        v = start + i * axis.tickSize;
                        ticks.push({ v: v, label: axis.tickFormatter(v, axis) });
                        ++i;
                    } while (v < axis.max && v != prev);
                    return ticks;
                };

                formatter = function (v, axis) {
                    return v.toFixed(axis.tickDecimals);
                };
            }

            axis.tickSize = unit ? [size, unit] : size;
            axis.tickGenerator = generator;
            if ($.isFunction(axisOptions.tickFormatter))
                axis.tickFormatter = function (v, axis) { return "" + axisOptions.tickFormatter(v, axis); };
            else
                axis.tickFormatter = formatter;
        }
        
        function setTicks(axis, axisOptions) {
            axis.ticks = [];

            if (!axis.used)
                return;
            
            if (axisOptions.ticks == null)
                axis.ticks = axis.tickGenerator(axis);
            else if (typeof axisOptions.ticks == "number") {
                if (axisOptions.ticks > 0)
                    axis.ticks = axis.tickGenerator(axis);
            }
            else if (axisOptions.ticks) {
                var ticks = axisOptions.ticks;

                if ($.isFunction(ticks))
                    // generate the ticks
                    ticks = ticks({ min: axis.min, max: axis.max });
                
                // clean up the user-supplied ticks, copy them over
                var i, v;
                for (i = 0; i < ticks.length; ++i) {
                    var label = null;
                    var t = ticks[i];
                    if (typeof t == "object") {
                        v = t[0];
                        if (t.length > 1)
                            label = t[1];
                    }
                    else
                        v = t;
                    if (label == null)
                        label = axis.tickFormatter(v, axis);
                    axis.ticks[i] = { v: v, label: label };
                }
            }

            if (axisOptions.autoscaleMargin != null && axis.ticks.length > 0) {
                // snap to ticks
                if (axisOptions.min == null)
                    axis.min = Math.min(axis.min, axis.ticks[0].v);
                if (axisOptions.max == null && axis.ticks.length > 1)
                    axis.max = Math.max(axis.max, axis.ticks[axis.ticks.length - 1].v);
            }
        }
      
        function draw() {
            ctx.clearRect(0, 0, canvasWidth, canvasHeight);

            var grid = options.grid;
            
            if (grid.show && !grid.aboveData)
                drawGrid();

            for (var i = 0; i < series.length; ++i)
                drawSeries(series[i]);

            executeHooks(hooks.draw, [ctx]);
            
            if (grid.show && grid.aboveData)
                drawGrid();
        }

        function extractRange(ranges, coord) {
            var firstAxis = coord + "axis",
                secondaryAxis = coord + "2axis",
                axis, from, to, reverse;

            if (ranges[firstAxis]) {
                axis = axes[firstAxis];
                from = ranges[firstAxis].from;
                to = ranges[firstAxis].to;
            }
            else if (ranges[secondaryAxis]) {
                axis = axes[secondaryAxis];
                from = ranges[secondaryAxis].from;
                to = ranges[secondaryAxis].to;
            }
            else {
                // backwards-compat stuff - to be removed in future
                axis = axes[firstAxis];
                from = ranges[coord + "1"];
                to = ranges[coord + "2"];
            }

            // auto-reverse as an added bonus
            if (from != null && to != null && from > to)
                return { from: to, to: from, axis: axis };
            
            return { from: from, to: to, axis: axis };
        }
        
        function drawGrid() {
            var i;
            
            ctx.save();
            ctx.translate(plotOffset.left, plotOffset.top);

            // draw background, if any
            if (options.grid.backgroundColor) {
                ctx.fillStyle = getColorOrGradient(options.grid.backgroundColor, plotHeight, 0, "rgba(255, 255, 255, 0)");
                ctx.fillRect(0, 0, plotWidth, plotHeight);
            }

            // draw markings
            var markings = options.grid.markings;
            if (markings) {
                if ($.isFunction(markings))
                    // xmin etc. are backwards-compatible, to be removed in future
                    markings = markings({ xmin: axes.xaxis.min, xmax: axes.xaxis.max, ymin: axes.yaxis.min, ymax: axes.yaxis.max, xaxis: axes.xaxis, yaxis: axes.yaxis, x2axis: axes.x2axis, y2axis: axes.y2axis });

                for (i = 0; i < markings.length; ++i) {
                    var m = markings[i],
                        xrange = extractRange(m, "x"),
                        yrange = extractRange(m, "y");

                    // fill in missing
                    if (xrange.from == null)
                        xrange.from = xrange.axis.min;
                    if (xrange.to == null)
                        xrange.to = xrange.axis.max;
                    if (yrange.from == null)
                        yrange.from = yrange.axis.min;
                    if (yrange.to == null)
                        yrange.to = yrange.axis.max;

                    // clip
                    if (xrange.to < xrange.axis.min || xrange.from > xrange.axis.max ||
                        yrange.to < yrange.axis.min || yrange.from > yrange.axis.max)
                        continue;

                    xrange.from = Math.max(xrange.from, xrange.axis.min);
                    xrange.to = Math.min(xrange.to, xrange.axis.max);
                    yrange.from = Math.max(yrange.from, yrange.axis.min);
                    yrange.to = Math.min(yrange.to, yrange.axis.max);

                    if (xrange.from == xrange.to && yrange.from == yrange.to)
                        continue;

                    // then draw
                    xrange.from = xrange.axis.p2c(xrange.from);
                    xrange.to = xrange.axis.p2c(xrange.to);
                    yrange.from = yrange.axis.p2c(yrange.from);
                    yrange.to = yrange.axis.p2c(yrange.to);
                    
                    if (xrange.from == xrange.to || yrange.from == yrange.to) {
                        // draw line
                        ctx.beginPath();
                        ctx.strokeStyle = m.color || options.grid.markingsColor;
                        ctx.lineWidth = m.lineWidth || options.grid.markingsLineWidth;
                        //ctx.moveTo(Math.floor(xrange.from), yrange.from);
                        //ctx.lineTo(Math.floor(xrange.to), yrange.to);
                        ctx.moveTo(xrange.from, yrange.from);
                        ctx.lineTo(xrange.to, yrange.to);
                        ctx.stroke();
                    }
                    else {
                        // fill area
                        ctx.fillStyle = m.color || options.grid.markingsColor;
                        ctx.fillRect(xrange.from, yrange.to,
                                     xrange.to - xrange.from,
                                     yrange.from - yrange.to);
                    }
                }
            }
            
            // draw the inner grid
            ctx.lineWidth = 1;
            ctx.strokeStyle = options.grid.tickColor;
            ctx.beginPath();
            var v, axis = axes.xaxis;
            for (i = 0; i < axis.ticks.length; ++i) {
                v = axis.ticks[i].v;
                if (v <= axis.min || v >= axes.xaxis.max)
                    continue;   // skip those lying on the axes

                ctx.moveTo(Math.floor(axis.p2c(v)) + ctx.lineWidth/2, 0);
                ctx.lineTo(Math.floor(axis.p2c(v)) + ctx.lineWidth/2, plotHeight);
            }

            axis = axes.yaxis;
            for (i = 0; i < axis.ticks.length; ++i) {
                v = axis.ticks[i].v;
                if (v <= axis.min || v >= axis.max)
                    continue;

                ctx.moveTo(0, Math.floor(axis.p2c(v)) + ctx.lineWidth/2);
                ctx.lineTo(plotWidth, Math.floor(axis.p2c(v)) + ctx.lineWidth/2);
            }

            axis = axes.x2axis;
            for (i = 0; i < axis.ticks.length; ++i) {
                v = axis.ticks[i].v;
                if (v <= axis.min || v >= axis.max)
                    continue;
    
                ctx.moveTo(Math.floor(axis.p2c(v)) + ctx.lineWidth/2, -5);
                ctx.lineTo(Math.floor(axis.p2c(v)) + ctx.lineWidth/2, 5);
            }

            axis = axes.y2axis;
            for (i = 0; i < axis.ticks.length; ++i) {
                v = axis.ticks[i].v;
                if (v <= axis.min || v >= axis.max)
                    continue;

                ctx.moveTo(plotWidth-5, Math.floor(axis.p2c(v)) + ctx.lineWidth/2);
                ctx.lineTo(plotWidth+5, Math.floor(axis.p2c(v)) + ctx.lineWidth/2);
            }
            
            ctx.stroke();
            
            if (options.grid.borderWidth) {
                // draw border
                var bw = options.grid.borderWidth;
                ctx.lineWidth = bw;
                ctx.strokeStyle = options.grid.borderColor;
                ctx.strokeRect(-bw/2, -bw/2, plotWidth + bw, plotHeight + bw);
            }

            ctx.restore();
        }

        function insertLabels() {
            placeholder.find(".tickLabels").remove();
            
            var html = ['<div class="tickLabels" style="font-size:smaller;color:' + options.grid.color + '">'];

            function addLabels(axis, labelGenerator) {
                for (var i = 0; i < axis.ticks.length; ++i) {
                    var tick = axis.ticks[i];
                    if (!tick.label || tick.v < axis.min || tick.v > axis.max)
                        continue;
                    html.push(labelGenerator(tick, axis));
                }
            }

            var margin = options.grid.labelMargin + options.grid.borderWidth;
            
            addLabels(axes.xaxis, function (tick, axis) {
                return '<div style="position:absolute;top:' + (plotOffset.top + plotHeight + margin) + 'px;left:' + Math.round(plotOffset.left + axis.p2c(tick.v) - axis.labelWidth/2) + 'px;width:' + axis.labelWidth + 'px;text-align:center" class="tickLabel">' + tick.label + "</div>";
            });
            
            
            addLabels(axes.yaxis, function (tick, axis) {
                return '<div style="position:absolute;top:' + Math.round(plotOffset.top + axis.p2c(tick.v) - axis.labelHeight/2) + 'px;right:' + (plotOffset.right + plotWidth + margin) + 'px;width:' + axis.labelWidth + 'px;text-align:right" class="tickLabel">' + tick.label + "</div>";
            });
            
            addLabels(axes.x2axis, function (tick, axis) {
                return '<div style="position:absolute;bottom:' + (plotOffset.bottom + plotHeight + margin) + 'px;left:' + Math.round(plotOffset.left + axis.p2c(tick.v) - axis.labelWidth/2) + 'px;width:' + axis.labelWidth + 'px;text-align:center" class="tickLabel">' + tick.label + "</div>";
            });
            
            addLabels(axes.y2axis, function (tick, axis) {
                return '<div style="position:absolute;top:' + Math.round(plotOffset.top + axis.p2c(tick.v) - axis.labelHeight/2) + 'px;left:' + (plotOffset.left + plotWidth + margin) +'px;width:' + axis.labelWidth + 'px;text-align:left" class="tickLabel">' + tick.label + "</div>";
            });

            html.push('</div>');
            
            placeholder.append(html.join(""));
        }

        function drawSeries(series) {
            if (series.lines.show)
                drawSeriesLines(series);
            if (series.bars.show)
                drawSeriesBars(series);
            if (series.points.show)
                drawSeriesPoints(series);
        }
        
        function drawSeriesLines(series) {
            function plotLine(datapoints, xoffset, yoffset, axisx, axisy) {
                var points = datapoints.points,
                    ps = datapoints.pointsize,
                    prevx = null, prevy = null;
                
                ctx.beginPath();
                for (var i = ps; i < points.length; i += ps) {
                    var x1 = points[i - ps], y1 = points[i - ps + 1],
                        x2 = points[i], y2 = points[i + 1];
                    
                    if (x1 == null || x2 == null)
                        continue;

                    // clip with ymin
                    if (y1 <= y2 && y1 < axisy.min) {
                        if (y2 < axisy.min)
                            continue;   // line segment is outside
                        // compute new intersection point
                        x1 = (axisy.min - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y1 = axisy.min;
                    }
                    else if (y2 <= y1 && y2 < axisy.min) {
                        if (y1 < axisy.min)
                            continue;
                        x2 = (axisy.min - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y2 = axisy.min;
                    }

                    // clip with ymax
                    if (y1 >= y2 && y1 > axisy.max) {
                        if (y2 > axisy.max)
                            continue;
                        x1 = (axisy.max - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y1 = axisy.max;
                    }
                    else if (y2 >= y1 && y2 > axisy.max) {
                        if (y1 > axisy.max)
                            continue;
                        x2 = (axisy.max - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y2 = axisy.max;
                    }

                    // clip with xmin
                    if (x1 <= x2 && x1 < axisx.min) {
                        if (x2 < axisx.min)
                            continue;
                        y1 = (axisx.min - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x1 = axisx.min;
                    }
                    else if (x2 <= x1 && x2 < axisx.min) {
                        if (x1 < axisx.min)
                            continue;
                        y2 = (axisx.min - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x2 = axisx.min;
                    }

                    // clip with xmax
                    if (x1 >= x2 && x1 > axisx.max) {
                        if (x2 > axisx.max)
                            continue;
                        y1 = (axisx.max - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x1 = axisx.max;
                    }
                    else if (x2 >= x1 && x2 > axisx.max) {
                        if (x1 > axisx.max)
                            continue;
                        y2 = (axisx.max - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x2 = axisx.max;
                    }

                    if (x1 != prevx || y1 != prevy)
                        ctx.moveTo(axisx.p2c(x1) + xoffset, axisy.p2c(y1) + yoffset);
                    
                    prevx = x2;
                    prevy = y2;
                    ctx.lineTo(axisx.p2c(x2) + xoffset, axisy.p2c(y2) + yoffset);
                }
                ctx.stroke();
            }

            function plotLineArea(datapoints, axisx, axisy) {
                var points = datapoints.points,
                    ps = datapoints.pointsize,
                    bottom = Math.min(Math.max(0, axisy.min), axisy.max),
                    top, lastX = 0, areaOpen = false;
                
                for (var i = ps; i < points.length; i += ps) {
                    var x1 = points[i - ps], y1 = points[i - ps + 1],
                        x2 = points[i], y2 = points[i + 1];
                    
                    if (areaOpen && x1 != null && x2 == null) {
                        // close area
                        ctx.lineTo(axisx.p2c(lastX), axisy.p2c(bottom));
                        ctx.fill();
                        areaOpen = false;
                        continue;
                    }

                    if (x1 == null || x2 == null)
                        continue;

                    // clip x values
                    
                    // clip with xmin
                    if (x1 <= x2 && x1 < axisx.min) {
                        if (x2 < axisx.min)
                            continue;
                        y1 = (axisx.min - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x1 = axisx.min;
                    }
                    else if (x2 <= x1 && x2 < axisx.min) {
                        if (x1 < axisx.min)
                            continue;
                        y2 = (axisx.min - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x2 = axisx.min;
                    }

                    // clip with xmax
                    if (x1 >= x2 && x1 > axisx.max) {
                        if (x2 > axisx.max)
                            continue;
                        y1 = (axisx.max - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x1 = axisx.max;
                    }
                    else if (x2 >= x1 && x2 > axisx.max) {
                        if (x1 > axisx.max)
                            continue;
                        y2 = (axisx.max - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x2 = axisx.max;
                    }

                    if (!areaOpen) {
                        // open area
                        ctx.beginPath();
                        ctx.moveTo(axisx.p2c(x1), axisy.p2c(bottom));
                        areaOpen = true;
                    }
                    
                    // now first check the case where both is outside
                    if (y1 >= axisy.max && y2 >= axisy.max) {
                        ctx.lineTo(axisx.p2c(x1), axisy.p2c(axisy.max));
                        ctx.lineTo(axisx.p2c(x2), axisy.p2c(axisy.max));
                        lastX = x2;
                        continue;
                    }
                    else if (y1 <= axisy.min && y2 <= axisy.min) {
                        ctx.lineTo(axisx.p2c(x1), axisy.p2c(axisy.min));
                        ctx.lineTo(axisx.p2c(x2), axisy.p2c(axisy.min));
                        lastX = x2;
                        continue;
                    }
                    
                    // else it's a bit more complicated, there might
                    // be two rectangles and two triangles we need to fill
                    // in; to find these keep track of the current x values
                    var x1old = x1, x2old = x2;

                    // and clip the y values, without shortcutting
                    
                    // clip with ymin
                    if (y1 <= y2 && y1 < axisy.min && y2 >= axisy.min) {
                        x1 = (axisy.min - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y1 = axisy.min;
                    }
                    else if (y2 <= y1 && y2 < axisy.min && y1 >= axisy.min) {
                        x2 = (axisy.min - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y2 = axisy.min;
                    }

                    // clip with ymax
                    if (y1 >= y2 && y1 > axisy.max && y2 <= axisy.max) {
                        x1 = (axisy.max - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y1 = axisy.max;
                    }
                    else if (y2 >= y1 && y2 > axisy.max && y1 <= axisy.max) {
                        x2 = (axisy.max - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y2 = axisy.max;
                    }


                    // if the x value was changed we got a rectangle
                    // to fill
                    if (x1 != x1old) {
                        if (y1 <= axisy.min)
                            top = axisy.min;
                        else
                            top = axisy.max;
                        
                        ctx.lineTo(axisx.p2c(x1old), axisy.p2c(top));
                        ctx.lineTo(axisx.p2c(x1), axisy.p2c(top));
                    }
                    
                    // fill the triangles
                    ctx.lineTo(axisx.p2c(x1), axisy.p2c(y1));
                    ctx.lineTo(axisx.p2c(x2), axisy.p2c(y2));

                    // fill the other rectangle if it's there
                    if (x2 != x2old) {
                        if (y2 <= axisy.min)
                            top = axisy.min;
                        else
                            top = axisy.max;
                        
                        ctx.lineTo(axisx.p2c(x2), axisy.p2c(top));
                        ctx.lineTo(axisx.p2c(x2old), axisy.p2c(top));
                    }

                    lastX = Math.max(x2, x2old);
                }

                if (areaOpen) {
                    ctx.lineTo(axisx.p2c(lastX), axisy.p2c(bottom));
                    ctx.fill();
                }
            }
            
            ctx.save();
            ctx.translate(plotOffset.left, plotOffset.top);
            ctx.lineJoin = "round";

            var lw = series.lines.lineWidth,
                sw = series.shadowSize;
            // FIXME: consider another form of shadow when filling is turned on
            if (lw > 0 && sw > 0) {
                // draw shadow as a thick and thin line with transparency
                ctx.lineWidth = sw;
                ctx.strokeStyle = "rgba(0,0,0,0.1)";
                // position shadow at angle from the mid of line
                var angle = Math.PI/18;
                plotLine(series.datapoints, Math.sin(angle) * (lw/2 + sw/2), Math.cos(angle) * (lw/2 + sw/2), series.xaxis, series.yaxis);
                ctx.lineWidth = sw/2;
                plotLine(series.datapoints, Math.sin(angle) * (lw/2 + sw/4), Math.cos(angle) * (lw/2 + sw/4), series.xaxis, series.yaxis);
            }

            ctx.lineWidth = lw;
            ctx.strokeStyle = series.color;
            var fillStyle = getFillStyle(series.lines, series.color, 0, plotHeight);
            if (fillStyle) {
                ctx.fillStyle = fillStyle;
                plotLineArea(series.datapoints, series.xaxis, series.yaxis);
            }

            if (lw > 0)
                plotLine(series.datapoints, 0, 0, series.xaxis, series.yaxis);
            ctx.restore();
        }

        function drawSeriesPoints(series) {
            function plotPoints(datapoints, radius, fillStyle, offset, circumference, axisx, axisy) {
                var points = datapoints.points, ps = datapoints.pointsize;
                
                for (var i = 0; i < points.length; i += ps) {
                    var x = points[i], y = points[i + 1];
                    if (x == null || x < axisx.min || x > axisx.max || y < axisy.min || y > axisy.max)
                        continue;
                    
                    ctx.beginPath();
                    ctx.arc(axisx.p2c(x), axisy.p2c(y) + offset, radius, 0, circumference, false);
                    if (fillStyle) {
                        ctx.fillStyle = fillStyle;
                        ctx.fill();
                    }
                    ctx.stroke();
                }
            }
            
            ctx.save();
            ctx.translate(plotOffset.left, plotOffset.top);

            var lw = series.lines.lineWidth,
                sw = series.shadowSize,
                radius = series.points.radius;
            if (lw > 0 && sw > 0) {
                // draw shadow in two steps
                var w = sw / 2;
                ctx.lineWidth = w;
                ctx.strokeStyle = "rgba(0,0,0,0.1)";
                plotPoints(series.datapoints, radius, null, w + w/2, Math.PI,
                           series.xaxis, series.yaxis);

                ctx.strokeStyle = "rgba(0,0,0,0.2)";
                plotPoints(series.datapoints, radius, null, w/2, Math.PI,
                           series.xaxis, series.yaxis);
            }

            ctx.lineWidth = lw;
            ctx.strokeStyle = series.color;
            plotPoints(series.datapoints, radius,
                       getFillStyle(series.points, series.color), 0, 2 * Math.PI,
                       series.xaxis, series.yaxis);
            ctx.restore();
        }

        function drawBar(x, y, b, barLeft, barRight, offset, fillStyleCallback, axisx, axisy, c, horizontal) {
            var left, right, bottom, top,
                drawLeft, drawRight, drawTop, drawBottom,
                tmp;

            if (horizontal) {
                drawBottom = drawRight = drawTop = true;
                drawLeft = false;
                left = b;
                right = x;
                top = y + barLeft;
                bottom = y + barRight;

                // account for negative bars
                if (right < left) {
                    tmp = right;
                    right = left;
                    left = tmp;
                    drawLeft = true;
                    drawRight = false;
                }
            }
            else {
                drawLeft = drawRight = drawTop = true;
                drawBottom = false;
                left = x + barLeft;
                right = x + barRight;
                bottom = b;
                top = y;

                // account for negative bars
                if (top < bottom) {
                    tmp = top;
                    top = bottom;
                    bottom = tmp;
                    drawBottom = true;
                    drawTop = false;
                }
            }
           
            // clip
            if (right < axisx.min || left > axisx.max ||
                top < axisy.min || bottom > axisy.max)
                return;
            
            if (left < axisx.min) {
                left = axisx.min;
                drawLeft = false;
            }

            if (right > axisx.max) {
                right = axisx.max;
                drawRight = false;
            }

            if (bottom < axisy.min) {
                bottom = axisy.min;
                drawBottom = false;
            }
            
            if (top > axisy.max) {
                top = axisy.max;
                drawTop = false;
            }

            left = axisx.p2c(left);
            bottom = axisy.p2c(bottom);
            right = axisx.p2c(right);
            top = axisy.p2c(top);
            
            // fill the bar
            if (fillStyleCallback) {
                c.beginPath();
                c.moveTo(left, bottom);
                c.lineTo(left, top);
                c.lineTo(right, top);
                c.lineTo(right, bottom);
                c.fillStyle = fillStyleCallback(bottom, top);
                c.fill();
            }

            // draw outline
            if (drawLeft || drawRight || drawTop || drawBottom) {
                c.beginPath();

                // FIXME: inline moveTo is buggy with excanvas
                c.moveTo(left, bottom + offset);
                if (drawLeft)
                    c.lineTo(left, top + offset);
                else
                    c.moveTo(left, top + offset);
                if (drawTop)
                    c.lineTo(right, top + offset);
                else
                    c.moveTo(right, top + offset);
                if (drawRight)
                    c.lineTo(right, bottom + offset);
                else
                    c.moveTo(right, bottom + offset);
                if (drawBottom)
                    c.lineTo(left, bottom + offset);
                else
                    c.moveTo(left, bottom + offset);
                c.stroke();
            }
        }
        
        function drawSeriesBars(series) {
            function plotBars(datapoints, barLeft, barRight, offset, fillStyleCallback, axisx, axisy) {
                var points = datapoints.points, ps = datapoints.pointsize;
                
                for (var i = 0; i < points.length; i += ps) {
                    if (points[i] == null)
                        continue;
                    drawBar(points[i], points[i + 1], points[i + 2], barLeft, barRight, offset, fillStyleCallback, axisx, axisy, ctx, series.bars.horizontal);
                }
            }

            ctx.save();
            ctx.translate(plotOffset.left, plotOffset.top);

            // FIXME: figure out a way to add shadows (for instance along the right edge)
            ctx.lineWidth = series.bars.lineWidth;
            ctx.strokeStyle = series.color;
            var barLeft = series.bars.align == "left" ? 0 : -series.bars.barWidth/2;
            var fillStyleCallback = series.bars.fill ? function (bottom, top) { return getFillStyle(series.bars, series.color, bottom, top); } : null;
            plotBars(series.datapoints, barLeft, barLeft + series.bars.barWidth, 0, fillStyleCallback, series.xaxis, series.yaxis);
            ctx.restore();
        }

        function getFillStyle(filloptions, seriesColor, bottom, top) {
            var fill = filloptions.fill;
            if (!fill)
                return null;

            if (filloptions.fillColor)
                return getColorOrGradient(filloptions.fillColor, bottom, top, seriesColor);
            
            var c = $.color.parse(seriesColor);
            c.a = typeof fill == "number" ? fill : 0.4;
            c.normalize();
            return c.toString();
        }
        
        function insertLegend() {
            placeholder.find(".legend").remove();

            if (!options.legend.show)
                return;
            
            var fragments = [], rowStarted = false,
                lf = options.legend.labelFormatter, s, label;
            for (i = 0; i < series.length; ++i) {
                s = series[i];
                label = s.label;
                if (!label)
                    continue;
                
                if (i % options.legend.noColumns == 0) {
                    if (rowStarted)
                        fragments.push('</tr>');
                    fragments.push('<tr>');
                    rowStarted = true;
                }

                if (lf)
                    label = lf(label, s);
                
                fragments.push(
                    '<td class="legendColorBox"><div style="border:1px solid ' + options.legend.labelBoxBorderColor + ';padding:1px"><div style="width:4px;height:0;border:5px solid ' + s.color + ';overflow:hidden"></div></div></td>' +
                    '<td class="legendLabel">' + label + '</td>');
            }
            if (rowStarted)
                fragments.push('</tr>');
            
            if (fragments.length == 0)
                return;

            var table = '<table style="font-size:smaller;color:' + options.grid.color + '">' + fragments.join("") + '</table>';
            if (options.legend.container != null)
                $(options.legend.container).html(table);
            else {
                var pos = "",
                    p = options.legend.position,
                    m = options.legend.margin;
                if (m[0] == null)
                    m = [m, m];
                if (p.charAt(0) == "n")
                    pos += 'top:' + (m[1] + plotOffset.top) + 'px;';
                else if (p.charAt(0) == "s")
                    pos += 'bottom:' + (m[1] + plotOffset.bottom) + 'px;';
                if (p.charAt(1) == "e")
                    pos += 'right:' + (m[0] + plotOffset.right) + 'px;';
                else if (p.charAt(1) == "w")
                    pos += 'left:' + (m[0] + plotOffset.left) + 'px;';
                var legend = $('<div class="legend">' + table.replace('style="', 'style="position:absolute;' + pos +';') + '</div>').appendTo(placeholder);
                if (options.legend.backgroundOpacity != 0.0) {
                    // put in the transparent background
                    // separately to avoid blended labels and
                    // label boxes
                    var c = options.legend.backgroundColor;
                    if (c == null) {
                        c = options.grid.backgroundColor;
                        if (c && typeof c == "string")
                            c = $.color.parse(c);
                        else
                            c = $.color.extract(legend, 'background-color');
                        c.a = 1;
                        c = c.toString();
                    }
                    var div = legend.children();
                    $('<div style="position:absolute;width:' + div.width() + 'px;height:' + div.height() + 'px;' + pos +'background-color:' + c + ';"> </div>').prependTo(legend).css('opacity', options.legend.backgroundOpacity);
                }
            }
        }


        // interactive features
        
        var highlights = [],
            redrawTimeout = null;
        
        // returns the data item the mouse is over, or null if none is found
        function findNearbyItem(mouseX, mouseY, seriesFilter) {
            var maxDistance = options.grid.mouseActiveRadius,
                smallestDistance = maxDistance * maxDistance + 1,
                item = null, foundPoint = false, i, j;

            for (i = 0; i < series.length; ++i) {
                if (!seriesFilter(series[i]))
                    continue;
                
                var s = series[i],
                    axisx = s.xaxis,
                    axisy = s.yaxis,
                    points = s.datapoints.points,
                    ps = s.datapoints.pointsize,
                    mx = axisx.c2p(mouseX), // precompute some stuff to make the loop faster
                    my = axisy.c2p(mouseY),
                    maxx = maxDistance / axisx.scale,
                    maxy = maxDistance / axisy.scale;

                if (s.lines.show || s.points.show) {
                    for (j = 0; j < points.length; j += ps) {
                        var x = points[j], y = points[j + 1];
                        if (x == null)
                            continue;
                        
                        // For points and lines, the cursor must be within a
                        // certain distance to the data point
                        if (x - mx > maxx || x - mx < -maxx ||
                            y - my > maxy || y - my < -maxy)
                            continue;

                        // We have to calculate distances in pixels, not in
                        // data units, because the scales of the axes may be different
                        var dx = Math.abs(axisx.p2c(x) - mouseX),
                            dy = Math.abs(axisy.p2c(y) - mouseY),
                            dist = dx * dx + dy * dy; // we save the sqrt

                        // use <= to ensure last point takes precedence
                        // (last generally means on top of)
                        if (dist <= smallestDistance) {
                            smallestDistance = dist;
                            item = [i, j / ps];
                        }
                    }
                }
                    
                if (s.bars.show && !item) { // no other point can be nearby
                    var barLeft = s.bars.align == "left" ? 0 : -s.bars.barWidth/2,
                        barRight = barLeft + s.bars.barWidth;
                    
                    for (j = 0; j < points.length; j += ps) {
                        var x = points[j], y = points[j + 1], b = points[j + 2];
                        if (x == null)
                            continue;
  
                        // for a bar graph, the cursor must be inside the bar
                        if (series[i].bars.horizontal ? 
                            (mx <= Math.max(b, x) && mx >= Math.min(b, x) && 
                             my >= y + barLeft && my <= y + barRight) :
                            (mx >= x + barLeft && mx <= x + barRight &&
                             my >= Math.min(b, y) && my <= Math.max(b, y)))
                                item = [i, j / ps];
                    }
                }
            }

            if (item) {
                i = item[0];
                j = item[1];
                ps = series[i].datapoints.pointsize;
                
                return { datapoint: series[i].datapoints.points.slice(j * ps, (j + 1) * ps),
                         dataIndex: j,
                         series: series[i],
                         seriesIndex: i };
            }
            
            return null;
        }

        function onMouseMove(e) {
            if (options.grid.hoverable)
                triggerClickHoverEvent("plothover", e,
                                       function (s) { return s["hoverable"] != false; });
        }
        
        function onClick(e) {
            triggerClickHoverEvent("plotclick", e,
                                   function (s) { return s["clickable"] != false; });
        }

        // trigger click or hover event (they send the same parameters
        // so we share their code)
        function triggerClickHoverEvent(eventname, event, seriesFilter) {
            var offset = eventHolder.offset(),
                pos = { pageX: event.pageX, pageY: event.pageY },
                canvasX = event.pageX - offset.left - plotOffset.left,
                canvasY = event.pageY - offset.top - plotOffset.top;

            if (axes.xaxis.used)
                pos.x = axes.xaxis.c2p(canvasX);
            if (axes.yaxis.used)
                pos.y = axes.yaxis.c2p(canvasY);
            if (axes.x2axis.used)
                pos.x2 = axes.x2axis.c2p(canvasX);
            if (axes.y2axis.used)
                pos.y2 = axes.y2axis.c2p(canvasY);

            var item = findNearbyItem(canvasX, canvasY, seriesFilter);

            if (item) {
                // fill in mouse pos for any listeners out there
                item.pageX = parseInt(item.series.xaxis.p2c(item.datapoint[0]) + offset.left + plotOffset.left);
                item.pageY = parseInt(item.series.yaxis.p2c(item.datapoint[1]) + offset.top + plotOffset.top);
            }

            if (options.grid.autoHighlight) {
                // clear auto-highlights
                for (var i = 0; i < highlights.length; ++i) {
                    var h = highlights[i];
                    if (h.auto == eventname &&
                        !(item && h.series == item.series && h.point == item.datapoint))
                        unhighlight(h.series, h.point);
                }
                
                if (item)
                    highlight(item.series, item.datapoint, eventname);
            }
            
            placeholder.trigger(eventname, [ pos, item ]);
        }

        function triggerRedrawOverlay() {
            if (!redrawTimeout)
                redrawTimeout = setTimeout(drawOverlay, 30);
        }

        function drawOverlay() {
            redrawTimeout = null;

            // draw highlights
            octx.save();
            octx.clearRect(0, 0, canvasWidth, canvasHeight);
            octx.translate(plotOffset.left, plotOffset.top);
            
            var i, hi;
            for (i = 0; i < highlights.length; ++i) {
                hi = highlights[i];

                if (hi.series.bars.show)
                    drawBarHighlight(hi.series, hi.point);
                else
                    drawPointHighlight(hi.series, hi.point);
            }
            octx.restore();
            
            executeHooks(hooks.drawOverlay, [octx]);
        }
        
        function highlight(s, point, auto) {
            if (typeof s == "number")
                s = series[s];

            if (typeof point == "number")
                point = s.data[point];

            var i = indexOfHighlight(s, point);
            if (i == -1) {
                highlights.push({ series: s, point: point, auto: auto });

                triggerRedrawOverlay();
            }
            else if (!auto)
                highlights[i].auto = false;
        }
            
        function unhighlight(s, point) {
            if (s == null && point == null) {
                highlights = [];
                triggerRedrawOverlay();
            }
            
            if (typeof s == "number")
                s = series[s];

            if (typeof point == "number")
                point = s.data[point];

            var i = indexOfHighlight(s, point);
            if (i != -1) {
                highlights.splice(i, 1);

                triggerRedrawOverlay();
            }
        }
        
        function indexOfHighlight(s, p) {
            for (var i = 0; i < highlights.length; ++i) {
                var h = highlights[i];
                if (h.series == s && h.point[0] == p[0]
                    && h.point[1] == p[1])
                    return i;
            }
            return -1;
        }
        
        function drawPointHighlight(series, point) {
            var x = point[0], y = point[1],
                axisx = series.xaxis, axisy = series.yaxis;
            
            if (x < axisx.min || x > axisx.max || y < axisy.min || y > axisy.max)
                return;
            
            var pointRadius = series.points.radius + series.points.lineWidth / 2;
            octx.lineWidth = pointRadius;
            octx.strokeStyle = $.color.parse(series.color).scale('a', 0.5).toString();
            var radius = 1.5 * pointRadius;
            octx.beginPath();
            octx.arc(axisx.p2c(x), axisy.p2c(y), radius, 0, 2 * Math.PI, false);
            octx.stroke();
        }

        function drawBarHighlight(series, point) {
            octx.lineWidth = series.bars.lineWidth;
            octx.strokeStyle = $.color.parse(series.color).scale('a', 0.5).toString();
            var fillStyle = $.color.parse(series.color).scale('a', 0.5).toString();
            var barLeft = series.bars.align == "left" ? 0 : -series.bars.barWidth/2;
            drawBar(point[0], point[1], point[2] || 0, barLeft, barLeft + series.bars.barWidth,
                    0, function () { return fillStyle; }, series.xaxis, series.yaxis, octx, series.bars.horizontal);
        }

        function getColorOrGradient(spec, bottom, top, defaultColor) {
            if (typeof spec == "string")
                return spec;
            else {
                // assume this is a gradient spec; IE currently only
                // supports a simple vertical gradient properly, so that's
                // what we support too
                var gradient = ctx.createLinearGradient(0, top, 0, bottom);
                
                for (var i = 0, l = spec.colors.length; i < l; ++i) {
                    var c = spec.colors[i];
                    if (typeof c != "string") {
                        c = $.color.parse(defaultColor).scale('rgb', c.brightness);
                        c.a *= c.opacity;
                        c = c.toString();
                    }
                    gradient.addColorStop(i / (l - 1), c);
                }
                
                return gradient;
            }
        }
    }

    $.plot = function(placeholder, data, options) {
        var plot = new Plot($(placeholder), data, options, $.plot.plugins);
        /*var t0 = new Date();
        var t1 = new Date();
        var tstr = "time used (msecs): " + (t1.getTime() - t0.getTime())
        if (window.console)
            console.log(tstr);
        else
            alert(tstr);*/
        return plot;
    };

    $.plot.plugins = [];

    // returns a string with the date d formatted according to fmt
    $.plot.formatDate = function(d, fmt, monthNames) {
        var leftPad = function(n) {
            n = "" + n;
            return n.length == 1 ? "0" + n : n;
        };
        
        var r = [];
        var escape = false;
        var hours = d.getUTCHours();
        var isAM = hours < 12;
        if (monthNames == null)
            monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

        if (fmt.search(/%p|%P/) != -1) {
            if (hours > 12) {
                hours = hours - 12;
            } else if (hours == 0) {
                hours = 12;
            }
        }
        for (var i = 0; i < fmt.length; ++i) {
            var c = fmt.charAt(i);
            
            if (escape) {
                switch (c) {
                case 'h': c = "" + hours; break;
                case 'H': c = leftPad(hours); break;
                case 'M': c = leftPad(d.getUTCMinutes()); break;
                case 'S': c = leftPad(d.getUTCSeconds()); break;
                case 'd': c = "" + d.getUTCDate(); break;
                case 'm': c = "" + (d.getUTCMonth() + 1); break;
                case 'y': c = "" + d.getUTCFullYear(); break;
                case 'b': c = "" + monthNames[d.getUTCMonth()]; break;
                case 'p': c = (isAM) ? ("" + "am") : ("" + "pm"); break;
                case 'P': c = (isAM) ? ("" + "AM") : ("" + "PM"); break;
                }
                r.push(c);
                escape = false;
            }
            else {
                if (c == "%")
                    escape = true;
                else
                    r.push(c);
            }
        }
        return r.join("");
    };
    
    // round to nearby lower multiple of base
    function floorInBase(n, base) {
        return base * Math.floor(n / base);
    }
    
})(jQuery);

/***
 * Contains core SlickGrid classes.
 * @module Core
 * @namespace Slick
 */

(function($) {
    // register namespace
    $.extend(true, window, {
        "Slick": {
            "Event":        Event,
            "EventData":    EventData,
            "Range":        Range,
            "NonDataRow":   NonDataItem,
            "Group":        Group,
            "GroupTotals":  GroupTotals,
            "EditorLock":   EditorLock,

            /***
             * A global singleton editor lock.
             * @class GlobalEditorLock
             * @static
             * @constructor
             */
            "GlobalEditorLock": new EditorLock()
        }
    });

    /***
     * An event object for passing data to event handlers and letting them control propagation.
     * <p>This is pretty much identical to how W3C and jQuery implement events.</p>
     * @class EventData
     * @constructor
     */
    function EventData() {
        var isPropagationStopped = false;
        var isImmediatePropagationStopped = false;

        /***
         * Stops event from propagating up the DOM tree.
         * @method stopPropagation
         */
        this.stopPropagation = function() {
            isPropagationStopped = true;
        };

        /***
         * Returns whether stopPropagation was called on this event object.
         * @method isPropagationStopped
         * @return {Boolean}
         */
        this.isPropagationStopped = function() {
            return isPropagationStopped;
        };

        /***
         * Prevents the rest of the handlers from being executed.
         * @method stopImmediatePropagation
         */
        this.stopImmediatePropagation = function() {
            isImmediatePropagationStopped = true;
        };

        /***
         * Returns whether stopImmediatePropagation was called on this event object.\
         * @method isImmediatePropagationStopped
         * @return {Boolean}
         */
        this.isImmediatePropagationStopped = function() {
            return isImmediatePropagationStopped;
        }
    }

    /***
     * A simple publisher-subscriber implementation.
     * @class Event
     * @constructor
     */
    function Event() {
        var handlers = [];

        /***
         * Adds an event handler to be called when the event is fired.
         * <p>Event handler will receive two arguments - an <code>EventData</code> and the <code>data</code>
         * object the event was fired with.<p>
         * @method subscribe
         * @param fn {Function} Event handler.
         */
        this.subscribe = function(fn) {
            handlers.push(fn);
        };

        /***
         * Removes an event handler added with <code>subscribe(fn)</code>.
         * @method unsubscribe
         * @param fn {Function} Event handler to be removed.
         */
        this.unsubscribe = function(fn) {
            for (var i = handlers.length - 1; i >= 0; i--) {
                if (handlers[i] === fn) {
                    handlers.splice(i, 1);
                }
            }
        };

        /***
         * Fires an event notifying all subscribers.
         * @method notify
         * @param args {Object} Additional data object to be passed to all handlers.
         * @param e {EventData}
         *      Optional.
         *      An <code>EventData</code> object to be passed to all handlers.
         *      For DOM events, an existing W3C/jQuery event object can be passed in.
         * @param scope {Object}
         *      Optional.
         *      The scope ("this") within which the handler will be executed.
         *      If not specified, the scope will be set to the <code>Event</code> instance.
         */
        this.notify = function(args, e, scope) {
            e = e || new EventData();
            scope = scope || this;

            var returnValue;
            for (var i = 0; i < handlers.length && !(e.isPropagationStopped() || e.isImmediatePropagationStopped()); i++) {
                returnValue = handlers[i].call(scope, e, args);
            }

            return returnValue;
        };
    }

    /***
     * A structure containing a range of cells.
     * @class Range
     * @constructor
     * @param fromRow {Integer} Starting row.
     * @param fromCell {Integer} Starting cell.
     * @param toRow {Integer} Optional. Ending row. Defaults to <code>fromRow</code>.
     * @param toCell {Integer} Optional. Ending cell. Defaults to <code>fromCell</code>.
     */
    function Range(fromRow, fromCell, toRow, toCell) {
        if (toRow === undefined && toCell === undefined) {
            toRow = fromRow;
            toCell = fromCell;
        }

        /***
         * @property fromRow
         * @type {Integer}
         */
        this.fromRow = Math.min(fromRow, toRow);

        /***
         * @property fromCell
         * @type {Integer}
         */
        this.fromCell = Math.min(fromCell, toCell);

        /***
         * @property toRow
         * @type {Integer}
         */
        this.toRow = Math.max(fromRow, toRow);

        /***
         * @property toCell
         * @type {Integer}
         */
        this.toCell = Math.max(fromCell, toCell);

        /***
         * Returns whether a range represents a single row.
         * @method isSingleRow
         * @return {Boolean}
         */
        this.isSingleRow = function() {
            return this.fromRow == this.toRow;
        };

        /***
         * Returns whether a range represents a single cell.
         * @method isSingleCell
         * @return {Boolean}
         */
        this.isSingleCell = function() {
            return this.fromRow == this.toRow && this.fromCell == this.toCell;
        };

        /***
         * Returns whether a range contains a given cell.
         * @method contains
         * @param row {Integer}
         * @param cell {Integer}
         * @return {Boolean}
         */
        this.contains = function(row, cell) {
            return row >= this.fromRow && row <= this.toRow &&
                   cell >= this.fromCell && cell <= this.toCell;
        };

        /***
         * Returns a readable representation of a range.
         * @method toString
         * @return {String}
         */
        this.toString = function() {
            if (this.isSingleCell()) {
                return "(" + this.fromRow + ":" + this.fromCell + ")";
            }
            else {
                return "(" + this.fromRow + ":" + this.fromCell + " - " + this.toRow + ":" + this.toCell + ")";
            }
        }
    }


    /***
     * A base class that all special / non-data rows (like Group and GroupTotals) derive from.
     * @class NonDataItem
     * @constructor
     */
    function NonDataItem() {
        this.__nonDataRow = true;
    }


    /***
     * Information about a group of rows.
     * @class Group
     * @extends Slick.NonDataItem
     * @constructor
     */
    function Group() {
        this.__group = true;
        this.__updated = false;

        /***
         * Number of rows in the group.
         * @property count
         * @type {Integer}
         */
        this.count = 0;

        /***
         * Grouping value.
         * @property value
         * @type {Object}
         */
        this.value = null;

        /***
         * Formatted display value of the group.
         * @property title
         * @type {String}
         */
        this.title = null;

        /***
         * Whether a group is collapsed.
         * @property collapsed
         * @type {Boolean}
         */
        this.collapsed = false;

        /***
         * GroupTotals, if any.
         * @property totals
         * @type {GroupTotals}
         */
        this.totals = null;
    }

    Group.prototype = new NonDataItem();

    /***
     * Compares two Group instances.
     * @method equals
     * @return {Boolean}
     * @param group {Group} Group instance to compare to.
     */
    Group.prototype.equals = function(group) {
        return this.value === group.value &&
               this.count === group.count &&
               this.collapsed === group.collapsed;
    };

    /***
     * Information about group totals.
     * An instance of GroupTotals will be created for each totals row and passed to the aggregators
     * so that they can store arbitrary data in it.  That data can later be accessed by group totals
     * formatters during the display.
     * @class GroupTotals
     * @extends Slick.NonDataItem
     * @constructor
     */
    function GroupTotals() {
        this.__groupTotals = true;

        /***
         * Parent Group.
         * @param group
         * @type {Group}
         */
        this.group = null;
    }

    GroupTotals.prototype = new NonDataItem();

    /***
     * A locking helper to track the active edit controller and ensure that only a single controller
     * can be active at a time.  This prevents a whole class of state and validation synchronization
     * issues.  An edit controller (such as SlickGrid) can query if an active edit is in progress
     * and attempt a commit or cancel before proceeding.
     * @class EditorLock
     * @constructor
     */
    function EditorLock() {
        var activeEditController = null;

        /***
         * Returns true if a specified edit controller is active (has the edit lock).
         * If the parameter is not specified, returns true if any edit controller is active.
         * @method isActive
         * @param editController {EditController}
         * @return {Boolean}
         */
        this.isActive = function(editController) {
            return (editController ? activeEditController === editController : activeEditController !== null);
        };

        /***
         * Sets the specified edit controller as the active edit controller (acquire edit lock).
         * If another edit controller is already active, and exception will be thrown.
         * @method activate
         * @param editController {EditController} edit controller acquiring the lock
         */
        this.activate = function(editController) {
            if (editController === activeEditController) { // already activated?
                return;
            }
            if (activeEditController !== null) {
                throw "SlickGrid.EditorLock.activate: an editController is still active, can't activate another editController";
            }
            if (!editController.commitCurrentEdit) {
                throw "SlickGrid.EditorLock.activate: editController must implement .commitCurrentEdit()";
            }
            if (!editController.cancelCurrentEdit) {
                throw "SlickGrid.EditorLock.activate: editController must implement .cancelCurrentEdit()";
            }
            activeEditController = editController;
        };

        /***
         * Unsets the specified edit controller as the active edit controller (release edit lock).
         * If the specified edit controller is not the active one, an exception will be thrown.
         * @method deactivate
         * @param editController {EditController} edit controller releasing the lock
         */
        this.deactivate = function(editController) {
            if (activeEditController !== editController) {
                throw "SlickGrid.EditorLock.deactivate: specified editController is not the currently active one";
            }
            activeEditController = null;
        };

        /***
         * Attempts to commit the current edit by calling "commitCurrentEdit" method on the active edit
         * controller and returns whether the commit attempt was successful (commit may fail due to validation
         * errors, etc.).  Edit controller's "commitCurrentEdit" must return true if the commit has succeeded
         * and false otherwise.  If no edit controller is active, returns true.
         * @method commitCurrentEdit
         * @return {Boolean}
         */
        this.commitCurrentEdit = function() {
            return (activeEditController ? activeEditController.commitCurrentEdit() : true);
        };

        /***
         * Attempts to cancel the current edit by calling "cancelCurrentEdit" method on the active edit
         * controller and returns whether the edit was successfully cancelled.  If no edit controller is
         * active, returns true.
         * @method cancelCurrentEdit
         * @return {Boolean}
         */
        this.cancelCurrentEdit = function cancelCurrentEdit() {
            return (activeEditController ? activeEditController.cancelCurrentEdit() : true);
        };
    }
})(jQuery);



/**
 * @license
 * (c) 2009-2010 Michael Leibman
 * michael{dot}leibman{at}gmail{dot}com
 * http://github.com/mleibman/slickgrid
 * Distributed under MIT license.
 * All rights reserved.
 *
 * SlickGrid v2.0 alpha
 *
 * NOTES:
 *     Cell/row DOM manipulations are done directly bypassing jQuery's DOM manipulation methods.
 *     This increases the speed dramatically, but can only be done safely because there are no event handlers
 *     or data associated with any cell/row DOM nodes.  Cell editors must make sure they implement .destroy()
 *     and do proper cleanup.
 */

// make sure required JavaScript modules are loaded
if (typeof jQuery === "undefined") {
    throw "SlickGrid requires jquery module to be loaded";
}
if (!jQuery.fn.drag) {
    throw "SlickGrid requires jquery.event.drag module to be loaded";
}
if (typeof Slick === "undefined") {
    throw "slick.core.js not loaded";
}


(function($) {
    // Slick.Grid
    $.extend(true, window, {
        Slick: {
            Grid: SlickGrid
        }
    });

    var scrollbarDimensions; // shared across all grids on this page

    //////////////////////////////////////////////////////////////////////////////////////////////
    // SlickGrid class implementation (available as Slick.Grid)

    /**
     * @param {Node}              container   Container node to create the grid in.
     * @param {Array,Object}      data        An array of objects for databinding.
     * @param {Array}             columns     An array of column definitions.
     * @param {Object}            options     Grid options.
     **/
    function SlickGrid(container,data,columns,options) {
        /// <summary>
        /// Create and manage virtual grid in the specified $container,
        /// connecting it to the specified data source. Data is presented
        /// as a grid with the specified columns and data.length rows.
        /// Options alter behaviour of the grid.
        /// </summary>

        // settings
        var defaults = {
            headerHeight: 25,
            rowHeight: 25,
            defaultColumnWidth: 80,
            enableAddRow: false,
            leaveSpaceForNewRows: false,
            editable: false,
            autoEdit: true,
            enableCellNavigation: true,
            enableCellRangeSelection: false,
            enableColumnReorder: true,
            asyncEditorLoading: false,
            asyncEditorLoadDelay: 100,
            forceFitColumns: false,
            enableAsyncPostRender: false,
            asyncPostRenderDelay: 60,
            autoHeight: false,
            editorLock: Slick.GlobalEditorLock,
            showHeaderRow: false,
            headerRowHeight: 25,
            showTopPanel: false,
            topPanelHeight: 25,
            syncColumnCellResize: false,
            formatterFactory: null,
            editorFactory: null,
            cellFlashingCssClass: "flashing",
            selectedCellCssClass: "selected",
            multiSelect: true
        },
        gridData;

        var columnDefaults = {
            name: "",
            resizable: true,
            sortable: false,
            minWidth: 30,
            rerenderOnResize: false,
            unselectable: false,
            groupTotalsFormatter: null,
            headerCssClass: null
        };

        // scroller
        var maxSupportedCssHeight;      // browser's breaking point
        var th;                         // virtual height
        var h;                          // real scrollable height
        var ph;                         // page height
        var n;                          // number of pages
        var cj;                         // "jumpiness" coefficient

        var page = 0;                   // current page
        var offset = 0;                 // current page offset
        var scrollDir = 1;

        // private
        var $container;
        var uid = "slickgrid_" + Math.round(1000000 * Math.random());
        var self = this;
        var $headerScroller;
        var $headers;
        var $headerRow, $headerRowScroller;
        var $topPanelScroller;
        var $topPanel;
        var $viewport;
        var $canvas;
        var $style;
        var stylesheet;
        var viewportH, viewportW;
        var viewportHasHScroll;
        var headerColumnWidthDiff, headerColumnHeightDiff, cellWidthDiff, cellHeightDiff;  // padding+border
        var absoluteColumnMinWidth;

        var activeRow, activeCell;
        var activeCellNode = null;
        var currentEditor = null;
        var serializedEditorValue;
        var editController;

        var rowsCache = {};
        var renderedRows = 0;
        var numVisibleRows;
        var prevScrollTop = 0;
        var scrollTop = 0;
        var lastRenderedScrollTop = 0;
        var prevScrollLeft = 0;
        var avgRowRenderTime = 10;

        var selectionModel;
        var selectedRows = [];

        var plugins = [];
        var cellCssClasses = {};

        var columnsById = {};
        var sortColumnId;
        var sortAsc = true;

        // async call handles
        var h_editorLoader = null;
        var h_render = null;
        var h_postrender = null;
        var postProcessedRows = {};
        var postProcessToRow = null;
        var postProcessFromRow = null;

        // perf counters
        var counter_rows_rendered = 0;
        var counter_rows_removed = 0;


        //////////////////////////////////////////////////////////////////////////////////////////////
        // Initialization

        function init() {
            /// <summary>
            /// Initialize 'this' (self) instance of a SlickGrid.
            /// This function is called by the constructor.
            /// </summary>

            $container = $(container);

            gridData = data;

            maxSupportedCssHeight = getMaxSupportedCssHeight();

            scrollbarDimensions = scrollbarDimensions || measureScrollbar(); // skip measurement if already have dimensions
            options = $.extend({},defaults,options);
            columnDefaults.width = options.defaultColumnWidth;

            // validate loaded JavaScript modules against requested options
            if (options.enableColumnReorder && !$.fn.sortable) {
                throw new Error("SlickGrid's \"enableColumnReorder = true\" option requires jquery-ui.sortable module to be loaded");
            }

            editController = {
                "commitCurrentEdit": commitCurrentEdit,
                "cancelCurrentEdit": cancelCurrentEdit
            };

            $container
                .empty()
                .attr("tabIndex",0)
                .attr("hideFocus",true)
                .css("overflow","hidden")
                .css("outline",0)
                .addClass(uid)
                .addClass("ui-widget");

            // set up a positioning container if needed
            if (!/relative|absolute|fixed/.test($container.css("position")))
                $container.css("position","relative");

            $headerScroller = $("<div class='slick-header ui-state-default' style='overflow:hidden;position:relative;' />").appendTo($container);
            $headers = $("<div class='slick-header-columns' style='width:10000px; left:-1000px' />").appendTo($headerScroller);

            $headerRowScroller = $("<div class='slick-headerrow ui-state-default' style='overflow:hidden;position:relative;' />").appendTo($container);
            $headerRow = $("<div class='slick-headerrow-columns' style='width:10000px;' />").appendTo($headerRowScroller);

            $topPanelScroller = $("<div class='slick-top-panel-scroller ui-state-default' style='overflow:hidden;position:relative;' />").appendTo($container);
            $topPanel = $("<div class='slick-top-panel' style='width:10000px' />").appendTo($topPanelScroller);

            if (!options.showTopPanel) {
                $topPanelScroller.hide();
            }

            if (!options.showHeaderRow) {
                $headerRowScroller.hide();
            }

            $viewport = $("<div class='slick-viewport' tabIndex='0' hideFocus style='width:100%;overflow-x:auto;outline:0;position:relative;overflow-y:auto;'>").appendTo($container);
            $canvas = $("<div class='grid-canvas' tabIndex='0' hideFocus style='width:100%' />").appendTo($viewport);

            // header columns and cells may have different padding/border skewing width calculations (box-sizing, hello?)
            // calculate the diff so we can set consistent sizes
            measureCellPaddingAndBorder();

            // for usability reasons, all text selection in SlickGrid is disabled
            // with the exception of input and textarea elements (selection must
            // be enabled there so that editors work as expected); note that
            // selection in grid cells (grid body) is already unavailable in
            // all browsers except IE
            disableSelection($headers); // disable all text selection in header (including input and textarea)
            $viewport.bind("selectstart.ui", function (event) { return $(event.target).is("input,textarea"); }); // disable text selection in grid cells except in input and textarea elements (this is IE-specific, because selectstart event will only fire in IE)

            viewportW = parseFloat($.css($container[0], "width", true));

            createColumnHeaders();
            setupColumnSort();
            createCssRules();
            resizeAndRender();

            bindAncestorScrollEvents();
            $viewport.bind("scroll.slickgrid", handleScroll);
            $container.bind("resize.slickgrid", resizeAndRender);
            $headerScroller
                .bind("contextmenu.slickgrid", handleHeaderContextMenu)
                .bind("click.slickgrid", handleHeaderClick);

            $canvas
                .bind("keydown.slickgrid", handleKeyDown)
                .bind("click.slickgrid", handleClick)
                .bind("dblclick.slickgrid", handleDblClick)
                .bind("contextmenu.slickgrid", handleContextMenu)
                .bind("draginit", handleDragInit)
                .bind("dragstart", handleDragStart)
                .bind("drag", handleDrag)
                .bind("dragend", handleDragEnd);

            $canvas.delegate(".slick-cell", "mouseenter", handleMouseEnter);
            $canvas.delegate(".slick-cell", "mouseleave", handleMouseLeave);
        }

        function registerPlugin(plugin) {
            plugins.unshift(plugin);
            plugin.init(self);
        }

        function unregisterPlugin(plugin) {
            for (var i = plugins.length; i >= 0; i--) {
                if (plugins[i] === plugin) {
                    if (plugins[i].destroy) {
                        plugins[i].destroy();
                    }
                    plugins.splice(i, 1);
                    break;
                }
            }
        }

        function setSelectionModel(model) {
            if (selectionModel) {
                selectionModel.onSelectedRangesChanged.unsubscribe(handleSelectedRangesChanged);
                if (selectionModel.destroy) {
                    selectionModel.destroy();
                }
            }

            selectionModel = model;
            selectionModel.init(self);

            selectionModel.onSelectedRangesChanged.subscribe(handleSelectedRangesChanged);
        }

        function getSelectionModel() {
            return selectionModel;
        }

        function getCanvasNode() {
            return $canvas[0];
        }

        function measureScrollbar() {
            /// <summary>
            /// Measure width of a vertical scrollbar
            /// and height of a horizontal scrollbar.
            /// </summary
            /// <returns>
            /// { width: pixelWidth, height: pixelHeight }
            /// </returns>
            var $c = $("<div style='position:absolute; top:-10000px; left:-10000px; width:100px; height:100px; overflow:scroll;'></div>").appendTo("body");
            var dim = { width: $c.width() - $c[0].clientWidth, height: $c.height() - $c[0].clientHeight };
            $c.remove();
            return dim;
        }

        function getRowWidth() {
            var rowWidth = 0;
            var i = columns.length;
            while (i--) {
                rowWidth += (columns[i].currentWidth || columns[i].width || columnDefaults.width);
            }
            return rowWidth;
        }

        function setCanvasWidth(width) {
//            $canvas.width(width);
            viewportHasHScroll = (width > viewportW - scrollbarDimensions.width);
        }

        function disableSelection($target) {
            /// <summary>
            /// Disable text selection (using mouse) in
            /// the specified target.
            /// </summary
            if ($target && $target.jquery) {
                $target.attr('unselectable', 'on').css('MozUserSelect', 'none').bind('selectstart.ui', function() { return false; }); // from jquery:ui.core.js 1.7.2
            }
        }

        function getMaxSupportedCssHeight() {
            var increment = 1000000;
            var supportedHeight = 0;
            // FF reports the height back but still renders blank after ~6M px
            var testUpTo = ($.browser.mozilla) ? 5000000 : 1000000000;
            var div = $("<div style='display:none' />").appendTo(document.body);

            while (supportedHeight <= testUpTo) {
                div.css("height", supportedHeight + increment);
                if (div.height() !== supportedHeight + increment)
                    break;
                else
                    supportedHeight += increment;
            }

            div.remove();
            return supportedHeight;
        }

        // TODO:  this is static.  need to handle page mutation.
        function bindAncestorScrollEvents() {
            var elem = $canvas[0];
            while ((elem = elem.parentNode) != document.body) {
                // bind to scroll containers only
                if (elem == $viewport[0] || elem.scrollWidth != elem.clientWidth || elem.scrollHeight != elem.clientHeight)
                    $(elem).bind("scroll.slickgrid", handleActiveCellPositionChange);
            }
        }

        function unbindAncestorScrollEvents() {
            $canvas.parents().unbind("scroll.slickgrid");
        }

        function updateColumnHeader(columnId, title, toolTip) {
            var idx = getColumnIndex(columnId);
            var $header = $headers.children().eq(idx);
            if ($header) {
                columns[idx].name = title;
                columns[idx].toolTip = toolTip;
                $header
                    .attr("title", toolTip || title || "")
                    .children().eq(0).html(title);
            }
        }

        function getHeaderRow() {
            return $headerRow[0];
        }

        function getHeaderRowColumn(columnId) {
            var idx = getColumnIndex(columnId);
            var $header = $headerRow.children().eq(idx);
            return $header && $header[0];
        }


        function createColumnHeaders() {
            var i;

            function hoverBegin() {
                $(this).addClass("ui-state-hover");
            }
            function hoverEnd() {
                $(this).removeClass("ui-state-hover");
            }

            $headers.empty();
            $headerRow.empty();
            columnsById = {};

            for (i = 0; i < columns.length; i++) {
                var m = columns[i] = $.extend({},columnDefaults,columns[i]);
                columnsById[m.id] = i;

                var header = $("<div class='ui-state-default slick-header-column' id='" + uid + m.id + "' />")
                    .html("<span class='slick-column-name'>" + m.name + "</span>")
                    .width((m.currentWidth || m.width) - headerColumnWidthDiff)
                    .attr("title", m.toolTip || m.name || "")
                    .data("fieldId", m.id)
                    .addClass(m.headerCssClass || "")
                    .appendTo($headers);

                if (options.enableColumnReorder || m.sortable) {
                    header.hover(hoverBegin, hoverEnd);
                }

                if (m.sortable) {
                    header.append("<span class='slick-sort-indicator' />");
                }

                if (options.showHeaderRow) {
                    $("<div class='ui-state-default slick-headerrow-column c" + i + "'></div>").appendTo($headerRow);
                }
            }

            setSortColumn(sortColumnId,sortAsc);
            setupColumnResize();
            if (options.enableColumnReorder) {
                setupColumnReorder();
            }
        }

        function setupColumnSort() {
            $headers.click(function(e) {
                if ($(e.target).hasClass("slick-resizable-handle")) {
                    return;
                }

                var $col = $(e.target).closest(".slick-header-column");
                if (!$col.length)
                    return;

                var column = columns[getSiblingIndex($col[0])];
                if (column.sortable) {
                    if (!getEditorLock().commitCurrentEdit())
                        return;

                    if (column.id === sortColumnId) {
                        sortAsc = !sortAsc;
                    }
                    else {
                        sortColumnId = column.id;
                        sortAsc = true;
                    }

                    setSortColumn(sortColumnId,sortAsc);
                    trigger(self.onSort, {sortCol:column,sortAsc:sortAsc});
                }
            });
        }

        function setupColumnReorder() {
            $headers.sortable({
                containment: "parent",
                axis: "x",
                cursor: "default",
                tolerance: "intersection",
                helper: "clone",
                placeholder: "slick-sortable-placeholder ui-state-default slick-header-column",
                forcePlaceholderSize: true,
                start: function(e, ui) { $(ui.helper).addClass("slick-header-column-active"); },
                beforeStop: function(e, ui) { $(ui.helper).removeClass("slick-header-column-active"); },
                stop: function(e) {
                    if (!getEditorLock().commitCurrentEdit()) {
                        $(this).sortable("cancel");
                        return;
                    }

                    var reorderedIds = $headers.sortable("toArray");
                    var reorderedColumns = [];
                    for (var i=0; i<reorderedIds.length; i++) {
                        reorderedColumns.push(columns[getColumnIndex(reorderedIds[i].replace(uid,""))]);
                    }
                    setColumns(reorderedColumns);

                    trigger(self.onColumnsReordered, {});
                    e.stopPropagation();
                    setupColumnResize();
                }
            });
        }

        function setupColumnResize() {
            var $col, j, c, pageX, columnElements, minPageX, maxPageX, firstResizable, lastResizable, originalCanvasWidth;
            columnElements = $headers.children();
            columnElements.find(".slick-resizable-handle").remove();
            columnElements.each(function(i,e) {
                if (columns[i].resizable) {
                    if (firstResizable === undefined) { firstResizable = i; }
                    lastResizable = i;
                }
            });
            if (firstResizable === undefined) {
                return;
            }
            columnElements.each(function(i,e) {
                if (i < firstResizable || (options.forceFitColumns && i >= lastResizable)) { return; }
                $col = $(e);
                $("<div class='slick-resizable-handle' />")
                    .appendTo(e)
                    .bind("dragstart", function(e,dd) {
                        if (!getEditorLock().commitCurrentEdit()) { return false; }
                        pageX = e.pageX;
                        $(this).parent().addClass("slick-header-column-active");
                        var shrinkLeewayOnRight = null, stretchLeewayOnRight = null;
                        // lock each column's width option to current width
                        columnElements.each(function(i,e) { columns[i].previousWidth = $(e).outerWidth(); });
                        if (options.forceFitColumns) {
                            shrinkLeewayOnRight = 0;
                            stretchLeewayOnRight = 0;
                            // colums on right affect maxPageX/minPageX
                            for (j = i + 1; j < columnElements.length; j++) {
                                c = columns[j];
                                if (c.resizable) {
                                    if (stretchLeewayOnRight !== null) {
                                        if (c.maxWidth) {
                                            stretchLeewayOnRight += c.maxWidth - c.previousWidth;
                                        }
                                        else {
                                            stretchLeewayOnRight = null;
                                        }
                                    }
                                    shrinkLeewayOnRight += c.previousWidth - Math.max(c.minWidth || 0, absoluteColumnMinWidth);
                                }
                            }
                        }
                        var shrinkLeewayOnLeft = 0, stretchLeewayOnLeft = 0;
                        for (j = 0; j <= i; j++) {
                            // columns on left only affect minPageX
                            c = columns[j];
                            if (c.resizable) {
                                if (stretchLeewayOnLeft !== null) {
                                    if (c.maxWidth) {
                                        stretchLeewayOnLeft += c.maxWidth - c.previousWidth;
                                    }
                                    else {
                                        stretchLeewayOnLeft = null;
                                    }
                                }
                                shrinkLeewayOnLeft += c.previousWidth - Math.max(c.minWidth || 0, absoluteColumnMinWidth);
                            }
                        }
                        if (shrinkLeewayOnRight === null) { shrinkLeewayOnRight = 100000; }
                        if (shrinkLeewayOnLeft === null) { shrinkLeewayOnLeft = 100000; }
                        if (stretchLeewayOnRight === null) { stretchLeewayOnRight = 100000; }
                        if (stretchLeewayOnLeft === null) { stretchLeewayOnLeft = 100000; }
                        maxPageX = pageX + Math.min(shrinkLeewayOnRight, stretchLeewayOnLeft);
                        minPageX = pageX - Math.min(shrinkLeewayOnLeft, stretchLeewayOnRight);
                        originalCanvasWidth = $canvas.width();
                    })
                    .bind("drag", function(e,dd) {
                        var actualMinWidth, d = Math.min(maxPageX, Math.max(minPageX, e.pageX)) - pageX, x, ci;
                        if (d < 0) { // shrink column
                            x = d;
                            for (j = i; j >= 0; j--) {
                                c = columns[j];
                                if (c.resizable) {
                                    actualMinWidth = Math.max(c.minWidth || 0, absoluteColumnMinWidth);
                                    if (x && c.previousWidth + x < actualMinWidth) {
                                        x += c.previousWidth - actualMinWidth;
                                        styleColumnWidth(j, actualMinWidth, options.syncColumnCellResize);
                                    } else {
                                        styleColumnWidth(j, c.previousWidth + x, options.syncColumnCellResize);
                                        x = 0;
                                    }
                                }
                            }

                            if (options.forceFitColumns) {
                                x = -d;
                                for (j = i + 1; j < columnElements.length; j++) {
                                    c = columns[j];
                                    if (c.resizable) {
                                        if (x && c.maxWidth && (c.maxWidth - c.previousWidth < x)) {
                                            x -= c.maxWidth - c.previousWidth;
                                            styleColumnWidth(j, c.maxWidth, options.syncColumnCellResize);
                                        } else {
                                            styleColumnWidth(j, c.previousWidth + x, options.syncColumnCellResize);
                                            x = 0;
                                        }
                                    }
                                }
                            } else if (options.syncColumnCellResize) {
                                setCanvasWidth(originalCanvasWidth + d);
                            }
                        } else { // stretch column
                            x = d;
                            for (j = i; j >= 0; j--) {
                                c = columns[j];
                                if (c.resizable) {
                                    if (x && c.maxWidth && (c.maxWidth - c.previousWidth < x)) {
                                        x -= c.maxWidth - c.previousWidth;
                                        styleColumnWidth(j, c.maxWidth, options.syncColumnCellResize);
                                    } else {
                                        styleColumnWidth(j, c.previousWidth + x, options.syncColumnCellResize);
                                        x = 0;
                                    }
                                }
                            }

                            if (options.forceFitColumns) {
                                x = -d;
                                for (j = i + 1; j < columnElements.length; j++) {
                                    c = columns[j];
                                    if (c.resizable) {
                                        actualMinWidth = Math.max(c.minWidth || 0, absoluteColumnMinWidth);
                                        if (x && c.previousWidth + x < actualMinWidth) {
                                            x += c.previousWidth - actualMinWidth;
                                            styleColumnWidth(j, actualMinWidth, options.syncColumnCellResize);
                                        } else {
                                            styleColumnWidth(j, c.previousWidth + x, options.syncColumnCellResize);
                                            x = 0;
                                        }
                                    }
                                }
                            } else if (options.syncColumnCellResize) {
                                setCanvasWidth(originalCanvasWidth + d);
                            }
                        }
                    })
                    .bind("dragend", function(e,dd) {
                        var newWidth;
                        $(this).parent().removeClass("slick-header-column-active");
                        for (j = 0; j < columnElements.length; j++) {
                            c = columns[j];
                            newWidth = $(columnElements[j]).outerWidth();

                            if (c.previousWidth !== newWidth && c.rerenderOnResize) {
                                invalidateAllRows();
                            }
                            if (options.forceFitColumns) {
                                c.width = Math.floor(c.width * (newWidth - c.previousWidth) / c.previousWidth) + c.width;
                            } else {
                                c.width = newWidth;
                            }
                            if (!options.syncColumnCellResize && c.previousWidth !== newWidth) {
                                styleColumnWidth(j, newWidth, true);
                            }
                        }
                        resizeCanvas();
                        trigger(self.onColumnsResized, {});
                    });
                });
        }

        function getVBoxDelta($el) {
            var p = ["borderTopWidth", "borderBottomWidth", "paddingTop", "paddingBottom"];
            var delta = 0;
            $.each(p, function(n,val) { delta += parseFloat($el.css(val)) || 0; });
            return delta;
        }

        function measureCellPaddingAndBorder() {
            var el;
            var h = ["borderLeftWidth", "borderRightWidth", "paddingLeft", "paddingRight"];
            var v = ["borderTopWidth", "borderBottomWidth", "paddingTop", "paddingBottom"];

            el = $("<div class='ui-state-default slick-header-column' style='visibility:hidden'>-</div>").appendTo($headers);
            headerColumnWidthDiff = headerColumnHeightDiff = 0;
            $.each(h, function(n,val) { headerColumnWidthDiff += parseFloat(el.css(val)) || 0; });
            $.each(v, function(n,val) { headerColumnHeightDiff += parseFloat(el.css(val)) || 0; });
            el.remove();

            var r = $("<div class='slick-row' />").appendTo($canvas);
            el = $("<div class='slick-cell' id='' style='visibility:hidden'>-</div>").appendTo(r);
            cellWidthDiff = cellHeightDiff = 0;
            $.each(h, function(n,val) { cellWidthDiff += parseFloat(el.css(val)) || 0; });
            $.each(v, function(n,val) { cellHeightDiff += parseFloat(el.css(val)) || 0; });
            r.remove();

            absoluteColumnMinWidth = Math.max(headerColumnWidthDiff,cellWidthDiff);
        }

        function createCssRules() {
            $style = $("<style type='text/css' rel='stylesheet' />").appendTo($("head"));
            var rowHeight = (options.rowHeight - cellHeightDiff);

            var rules = [
                "." + uid + " .slick-header-column { left: 1000px; }",
                "." + uid + " .slick-top-panel { height:" + options.topPanelHeight + "px; }",
                "." + uid + " .slick-headerrow-columns { height:" + options.headerRowHeight + "px; }",
                "." + uid + " .slick-cell { height:" + rowHeight + "px; }",
                "." + uid + " .slick-row { min-width:" + getRowWidth() + "px; }"
            ];

            for (var i=0; i<columns.length; i++) {
                rules.push(
                    "." + uid + " .c" + i + " { " +
                    "width:" + ((columns[i].currentWidth || columns[i].width) - cellWidthDiff) + "px; " +
                    " } ");
            }

            if ($style[0].styleSheet) { // IE
                $style[0].styleSheet.cssText = rules.join("");
            }
            else {
                $style[0].appendChild(document.createTextNode(rules.join(" ")));
            }

            var sheets = document.styleSheets;
            for (var i=0; i<sheets.length; i++) {
                if ((sheets[i].ownerNode || sheets[i].owningElement) == $style[0]) {
                    stylesheet = sheets[i];
                    break;
                }
            }
        }

        function findCssRule(selector) {
            var rules = (stylesheet.cssRules || stylesheet.rules);

            for (var i=0; i<rules.length; i++) {
                if (rules[i].selectorText == selector)
                    return rules[i];
            }

            return null;
        }

        function findCssRuleForCell(index) {
            return findCssRule("." + uid + " .c" + index);
        }

        function removeCssRules() {
            $style.remove();
        }

        function destroy() {
            getEditorLock().cancelCurrentEdit();

            trigger(self.onBeforeDestroy, {});

            for (var i = 0; i < plugins.length; i++) {
                unregisterPlugin(plugin);
            }

            if (options.enableColumnReorder && $headers.sortable)
                $headers.sortable("destroy");

            unbindAncestorScrollEvents();
            $container.unbind(".slickgrid");
            removeCssRules();

            $canvas.unbind("draginit dragstart dragend drag");
            $container.empty().removeClass(uid);
        }


        //////////////////////////////////////////////////////////////////////////////////////////////
        // General

        function trigger(evt, args, e) {
            e = e || new Slick.EventData();
            args = args || {};
            args.grid = self;
            return evt.notify(args, e, self);
        }

        function getEditorLock() {
            return options.editorLock;
        }

        function getEditController() {
            return editController;
        }

        function getColumnIndex(id) {
            return columnsById[id];
        }

        function autosizeColumns() {
            var i, c,
                widths = [],
                shrinkLeeway = 0,
                availWidth = (options.autoHeight ? viewportW : viewportW - scrollbarDimensions.width), // with AutoHeight, we do not need to accomodate the vertical scroll bar
                total = 0,
                existingTotal = 0;

            for (i = 0; i < columns.length; i++) {
                c = columns[i];
                widths.push(c.width);
                existingTotal += c.width;
                shrinkLeeway += c.width - Math.max(c.minWidth || 0, absoluteColumnMinWidth);
            }

            total = existingTotal;

            invalidateAllRows();

            // shrink
            while (total > availWidth) {
                if (!shrinkLeeway) { return; }
                var shrinkProportion = (total - availWidth) / shrinkLeeway;
                for (i = 0; i < columns.length && total > availWidth; i++) {
                    c = columns[i];
                    if (!c.resizable || c.minWidth === c.width || c.width === absoluteColumnMinWidth) { continue; }
                    var shrinkSize = Math.floor(shrinkProportion * (c.width - Math.max(c.minWidth || 0, absoluteColumnMinWidth))) || 1;
                    total -= shrinkSize;
                    widths[i] -= shrinkSize;
                }
            }

            // grow
            var previousTotal = total;
            while (total < availWidth) {
                var growProportion = availWidth / total;
                for (i = 0; i < columns.length && total < availWidth; i++) {
                    c = columns[i];
                    if (!c.resizable || c.maxWidth <= c.width) { continue; }
                    var growSize = Math.min(Math.floor(growProportion * c.width) - c.width, (c.maxWidth - c.width) || 1000000) || 1;
                    total += growSize;
                    widths[i] += growSize;
                }
                if (previousTotal == total) break; // if total is not changing, will result in infinite loop
                previousTotal = total;
            }

            for (i=0; i<columns.length; i++) {
                styleColumnWidth(i, columns[i].currentWidth = widths[i], true);
            }

            resizeCanvas();
        }

        function styleColumnWidth(index,width,styleCells) {
            columns[index].currentWidth = width;
            $headers.children().eq(index).css("width", width - headerColumnWidthDiff);
            if (styleCells) {
                findCssRuleForCell(index).style.width = (width - cellWidthDiff) + "px";
                findCssRule("." + uid + " .slick-row").style.minWidth = getRowWidth() + "px";
            }
        }

        function setSortColumn(columnId, ascending) {
            sortColumnId = columnId;
            sortAsc = ascending;
            var columnIndex = getColumnIndex(sortColumnId);

            $headers.children().removeClass("slick-header-column-sorted");
            $headers.find(".slick-sort-indicator").removeClass("slick-sort-indicator-asc slick-sort-indicator-desc");

            if (columnIndex != null) {
                $headers.children().eq(columnIndex)
                    .addClass("slick-header-column-sorted")
                    .find(".slick-sort-indicator")
                        .addClass(sortAsc ? "slick-sort-indicator-asc" : "slick-sort-indicator-desc");
            }
        }

        function handleSelectedRangesChanged(e, ranges) {
            selectedRows = [];
            var hash = {};
            for (var i = 0; i < ranges.length; i++) {
                for (var j = ranges[i].fromRow; j <= ranges[i].toRow; j++) {
                    if (!hash[j]) {  // prevent duplicates
                        selectedRows.push(j);
                    }
                    hash[j] = {};
                    for (var k = ranges[i].fromCell; k <= ranges[i].toCell; k++) {
                        hash[j][columns[k].id] = options.selectedCellCssClass;
                    }
                }
            }

            setCellCssStyles(options.selectedCellCssClass, hash);

            trigger(self.onSelectedRowsChanged, {rows:getSelectedRows()}, e);
        }

        function getColumns() {
            return columns;
        }

        function setColumns(columnDefinitions) {
            columns = columnDefinitions;
            invalidateAllRows();
            createColumnHeaders();
            removeCssRules();
            createCssRules();
            resizeAndRender();
            handleScroll();
        }

        function getOptions() {
            return options;
        }

        function setOptions(args) {
            if (!getEditorLock().commitCurrentEdit()) {
                return;
            }

            makeActiveCellNormal();

            if (options.enableAddRow !== args.enableAddRow) {
                invalidateRow(getDataLength());
            }

            options = $.extend(options,args);

            render();
        }

        function setData(newData,scrollToTop) {
            invalidateAllRows();
            gridData = newData;
            if (scrollToTop)
                scrollTo(0);
        }

        function getData() {
            return gridData;
        }

        function getDataLength() {
            if (gridData.getLength) {
                return gridData.getLength();
            }
            else {
                return gridData.length;
            }
        }

        function getDataItem(i) {
            if (gridData.getItem) {
                return gridData.getItem(i);
            }
            else {
                return gridData[i];
            }
        }

        function getTopPanel() {
            return $topPanel[0];
        }

        function showTopPanel() {
            options.showTopPanel = true;
            $topPanelScroller.slideDown("fast", resizeCanvas);
        }

        function hideTopPanel() {
            options.showTopPanel = false;
            $topPanelScroller.slideUp("fast", resizeCanvas);
        }

        function showHeaderRowColumns() {
            options.showHeaderRow = true;
            $headerRowScroller.slideDown("fast", resizeCanvas);
        }

        function hideHeaderRowColumns() {
            options.showHeaderRow = false;
            $headerRowScroller.slideUp("fast", resizeCanvas);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Rendering / Scrolling

        function scrollTo(y) {
            var oldOffset = offset;

            page = Math.min(n-1, Math.floor(y / ph));
            offset = Math.round(page * cj);
            var newScrollTop = y - offset;

            if (offset != oldOffset) {
                var range = getVisibleRange(newScrollTop);
                cleanupRows(range.top,range.bottom);
                updateRowPositions();
            }

            if (prevScrollTop != newScrollTop) {
                scrollDir = (prevScrollTop + oldOffset < newScrollTop + offset) ? 1 : -1;
                $viewport[0].scrollTop = (lastRenderedScrollTop = scrollTop = prevScrollTop = newScrollTop);

                trigger(self.onViewportChanged, {});
            }
        }

        function defaultFormatter(row, cell, value, columnDef, dataContext) {
            return (value === null || value === undefined) ? "" : value;
        }

        function getFormatter(column) {
            return column.formatter ||
                    (options.formatterFactory && options.formatterFactory.getFormatter(column)) ||
                    defaultFormatter;
        }

        function getEditor(column) {
            return column.editor || (options.editorFactory && options.editorFactory.getEditor(column));
        }

        function appendRowHtml(stringArray,row) {
            var d = getDataItem(row);
            var dataLoading = row < getDataLength() && !d;
            var cellCss;
            var css = "slick-row " +
                (dataLoading ? " loading" : "") +
                (row % 2 == 1 ? ' odd' : ' even');

            // if the user has specified a function to provide additional per-row css classes, call it here
            if (options.rowCssClasses) {
                css += " " + options.rowCssClasses(d);
            }

            if (d instanceof Slick.GroupTotals) {
                css += " slick-group-totals";
            }

            stringArray.push("<div class='ui-widget-content " + css + "' row='" + row + "' style='top:" + (options.rowHeight*row-offset) + "px'>");

            if (d instanceof Slick.Group) {
                stringArray.push("<div class='slick-cell slick-group" + (row === activeRow ? " active" : "") + "'>");
                stringArray.push("<span class='slick-group-toggle " + (d.collapsed ? "collapsed" : "expanded") + "'></span>");
                stringArray.push(d.title);
                stringArray.push("</div>");
            }
            else {
                for (var i=0, cols=columns.length; i<cols; i++) {
                    var m = columns[i];

                    cellCss = "slick-cell c" + i + (m.cssClass ? " " + m.cssClass : "");
                    if (row === activeRow && i === activeCell) {
                        cellCss += (" active");
                    }

                    if (!(d instanceof Slick.NonDataRow)) {
                        // TODO:  merge them together in the setter
                        for (var key in cellCssClasses) {
                            if (cellCssClasses[key][row] && cellCssClasses[key][row][m.id]) {
                                cellCss += (" " + cellCssClasses[key][row][m.id]);
                            }
                        }
                    }

                    stringArray.push("<div class='" + cellCss + "'>");

                    // if there is a corresponding row (if not, this is the Add New row or this data hasn't been loaded yet)
                    if (d) {
                        if (d instanceof Slick.GroupTotals) {
                            if (m.groupTotalsFormatter) {
                                stringArray.push(m.groupTotalsFormatter(d, m));
                            }
                        }
                        else {
                            stringArray.push(getFormatter(m)(row, i, d[m.field], m, d));
                        }
                    }

                    stringArray.push("</div>");
                }
            }

            stringArray.push("</div>");
        }

        function cleanupRows(rangeToKeep) {
            for (var i in rowsCache) {
                if (((i = parseInt(i, 10)) !== activeRow) && (i < rangeToKeep.top || i > rangeToKeep.bottom)) {
                    removeRowFromCache(i);
                }
            }
        }

        function invalidate() {
           updateRowCount();
           invalidateAllRows();
           render();
        }

        function invalidateAllRows() {
            if (currentEditor) {
                makeActiveCellNormal();
            }
            for (var row in rowsCache) {
                removeRowFromCache(row);
            }
        }

        function removeRowFromCache(row) {
            var node = rowsCache[row];
            if (!node) { return; }
            $canvas[0].removeChild(node);

            delete rowsCache[row];
            delete postProcessedRows[row];
            renderedRows--;
            counter_rows_removed++;
        }

        function invalidateRows(rows) {
            var i, rl;
            if (!rows || !rows.length) { return; }
            scrollDir = 0;
            for (i=0, rl=rows.length; i<rl; i++) {
                if (currentEditor && activeRow === i) {
                    makeActiveCellNormal();
                }

                if (rowsCache[rows[i]]) {
                    removeRowFromCache(rows[i]);
                }
            }
        }

        function invalidateRow(row) {
            invalidateRows([row]);
        }

        function updateCell(row,cell) {
            var cellNode = getCellNode(row,cell);
            if (!cellNode) {
                return;
            }

            var m = columns[cell], d = getDataItem(row);
            if (currentEditor && activeRow === row && activeCell === cell) {
                currentEditor.loadValue(d);
            }
            else {
                cellNode.innerHTML = d ? getFormatter(m)(row, cell, d[m.field], m, d) : "";
                invalidatePostProcessingResults(row);
            }
        }

        function updateRow(row) {
            if (!rowsCache[row]) { return; }

            $(rowsCache[row]).children().each(function(i) {
                var m = columns[i];
                if (row === activeRow && i === activeCell && currentEditor) {
                    currentEditor.loadValue(getDataItem(activeRow));
                }
                else if (getDataItem(row)) {
                    this.innerHTML = getFormatter(m)(row, i, getDataItem(row)[m.field], m, getDataItem(row));
                }
                else {
                    this.innerHTML = "";
                }
            });

            invalidatePostProcessingResults(row);
        }

        function getViewportHeight() {
            return parseFloat($.css($container[0], "height", true)) -
                options.headerHeight -
                getVBoxDelta($headers) -
                (options.showTopPanel ? options.topPanelHeight + getVBoxDelta($topPanelScroller) : 0) -
                (options.showHeaderRow ? options.headerRowHeight + getVBoxDelta($headerRowScroller) : 0);
        }

        function resizeCanvas() {
            if (options.autoHeight) {
                viewportH = options.rowHeight * (getDataLength() + (options.enableAddRow ? 1 : 0) + (options.leaveSpaceForNewRows? numVisibleRows - 1 : 0));
            }
            else {
                viewportH = getViewportHeight();
            }

            numVisibleRows = Math.ceil(viewportH / options.rowHeight);
            viewportW = parseFloat($.css($container[0], "width", true));
            $viewport.height(viewportH - 5); // TODO: for some reason the grid is off by 5px, go find out why!

            var w = 0, i = columns.length;
            while (i--) {
                w += columns[i].currentWidth || columns[i].width;
            }
            setCanvasWidth(w);

            updateRowCount();
            render();
        }

        function resizeAndRender() {
            if (options.forceFitColumns) {
                autosizeColumns();
            } else {
                resizeCanvas();
            }
        }

        function updateRowCount() {
            var newRowCount = getDataLength() + (options.enableAddRow?1:0) + (options.leaveSpaceForNewRows?numVisibleRows-1:0);
            var oldH = h;

            // remove the rows that are now outside of the data range
            // this helps avoid redundant calls to .removeRow() when the size of the data decreased by thousands of rows
            var l = options.enableAddRow ? getDataLength() : getDataLength() - 1;
            for (var i in rowsCache) {
                if (i >= l) {
                    removeRowFromCache(i);
                }
            }
            th = Math.max(options.rowHeight * newRowCount, viewportH - scrollbarDimensions.height);
            if (th < maxSupportedCssHeight) {
                // just one page
                h = ph = th;
                n = 1;
                cj = 0;
            }
            else {
                // break into pages
                h = maxSupportedCssHeight;
                ph = h / 100;
                n = Math.floor(th / ph);
                cj = (th - h) / (n - 1);
            }

            if (h !== oldH) {
                $canvas.css("height",h);
                scrollTop = $viewport[0].scrollTop;
            }

            var oldScrollTopInRange = (scrollTop + offset <= th - viewportH);

            if (th == 0 || scrollTop == 0) {
                page = offset = 0;
            }
            else if (oldScrollTopInRange) {
                // maintain virtual position
                scrollTo(scrollTop+offset);
            }
            else {
                // scroll to bottom
                scrollTo(th-viewportH);
            }

            if (h != oldH && options.autoHeight) {
                resizeCanvas();
            }
        }

        function getVisibleRange(viewportTop) {
            if (viewportTop == null)
                viewportTop = scrollTop;

            return {
                top: Math.floor((scrollTop+offset)/options.rowHeight),
                bottom: Math.ceil((scrollTop+offset+viewportH)/options.rowHeight)
            };
        }

        function getRenderedRange(viewportTop) {
            var range = getVisibleRange(viewportTop);
            var buffer = Math.round(viewportH/options.rowHeight);
            var minBuffer = 3;

            if (scrollDir == -1) {
                range.top -= buffer;
                range.bottom += minBuffer;
            }
            else if (scrollDir == 1) {
                range.top -= minBuffer;
                range.bottom += buffer;
            }
            else {
                range.top -= minBuffer;
                range.bottom += minBuffer;
            }

            range.top = Math.max(0,range.top);
            range.bottom = Math.min(options.enableAddRow ? getDataLength() : getDataLength() - 1,range.bottom);

            return range;
        }

        function renderRows(range) {
            var i, l,
                parentNode = $canvas[0],
                rowsBefore = renderedRows,
                stringArray = [],
                rows = [],
                startTimestamp = new Date(),
                needToReselectCell = false;

            for (i = range.top; i <= range.bottom; i++) {
                if (rowsCache[i]) { continue; }
                renderedRows++;
                rows.push(i);
                appendRowHtml(stringArray,i);
                if (activeCellNode && activeRow === i) {
                    needToReselectCell = true;
                }
                counter_rows_rendered++;
            }

            var x = document.createElement("div");
            x.innerHTML = stringArray.join("");

            for (i = 0, l = x.childNodes.length; i < l; i++) {
                rowsCache[rows[i]] = parentNode.appendChild(x.firstChild);
            }

            if (needToReselectCell) {
                activeCellNode = getCellNode(activeRow,activeCell);
            }

            if (renderedRows - rowsBefore > 5) {
                avgRowRenderTime = (new Date() - startTimestamp) / (renderedRows - rowsBefore);
            }
        }

        function startPostProcessing() {
            if (!options.enableAsyncPostRender) { return; }
            clearTimeout(h_postrender);
            h_postrender = setTimeout(asyncPostProcessRows, options.asyncPostRenderDelay);
        }

        function invalidatePostProcessingResults(row) {
            delete postProcessedRows[row];
            postProcessFromRow = Math.min(postProcessFromRow,row);
            postProcessToRow = Math.max(postProcessToRow,row);
            startPostProcessing();
        }

        function updateRowPositions() {
            for (var row in rowsCache) {
                rowsCache[row].style.top = (row*options.rowHeight-offset) + "px";
            }
        }

        function render() {
            var visible = getVisibleRange();
            var rendered = getRenderedRange();

            // remove rows no longer in the viewport
            cleanupRows(rendered);

            // add new rows
            renderRows(rendered);

            postProcessFromRow = visible.top;
            postProcessToRow = Math.min(options.enableAddRow ? getDataLength() : getDataLength() - 1, visible.bottom);
            startPostProcessing();

            lastRenderedScrollTop = scrollTop;
            h_render = null;
        }

        function handleScroll() {
            scrollTop = $viewport[0].scrollTop;
            var scrollLeft = $viewport[0].scrollLeft;
            var scrollDist = Math.abs(scrollTop - prevScrollTop);

            if (scrollLeft !== prevScrollLeft) {
                prevScrollLeft = scrollLeft;
                $headerScroller[0].scrollLeft = scrollLeft;
                $topPanelScroller[0].scrollLeft = scrollLeft;
                $headerRowScroller[0].scrollLeft = scrollLeft;
            }

            if (scrollDist) {
                scrollDir = prevScrollTop < scrollTop ? 1 : -1;
                prevScrollTop = scrollTop;

                // switch virtual pages if needed
                if (scrollDist < viewportH) {
                    scrollTo(scrollTop + offset);
                }
                else {
                    var oldOffset = offset;
                    page = Math.min(n - 1, Math.floor(scrollTop * ((th - viewportH) / (h - viewportH)) * (1 / ph)));
                    offset = Math.round(page * cj);
                    if (oldOffset != offset)
                        invalidateAllRows();
                }

                if (h_render)
                    clearTimeout(h_render);

                if (Math.abs(lastRenderedScrollTop - scrollTop) < viewportH)
                    render();
                else
                    h_render = setTimeout(render, 50);

                trigger(self.onViewportChanged, {});
            }

            trigger(self.onScroll, {scrollLeft:scrollLeft, scrollTop:scrollTop});
        }

        function asyncPostProcessRows() {
            while (postProcessFromRow <= postProcessToRow) {
                var row = (scrollDir >= 0) ? postProcessFromRow++ : postProcessToRow--;
                var rowNode = rowsCache[row];
                if (!rowNode || postProcessedRows[row] || row>=getDataLength()) { continue; }

                var d = getDataItem(row), cellNodes = rowNode.childNodes;
                for (var i=0, j=0, l=columns.length; i<l; ++i) {
                    var m = columns[i];
                    if (m.asyncPostRender) { m.asyncPostRender(cellNodes[j], postProcessFromRow, d, m); }
                    ++j;
                }

                postProcessedRows[row] = true;
                h_postrender = setTimeout(asyncPostProcessRows, options.asyncPostRenderDelay);
                return;
            }
        }

        function addCellCssStyles(key,hash) {
            if (cellCssClasses[key]) {
                throw "addCellCssStyles: cell CSS hash with key '" + key + "' already exists.";
            }

            cellCssClasses[key] = hash;

            for (var row in rowsCache) {
                if (hash[row]) {
                    if (!(getDataItem(row) instanceof Slick.NonDataRow)) {
                        for (var columnId in hash[row]) {
                            $(rowsCache[row]).children().eq(getColumnIndex(columnId))
                                .addClass(hash[row][columnId]);
                        }
                    }
                }
            }
        }

        function removeCellCssStyles(key) {
            if (!cellCssClasses[key]) {
                return;
            }

            for (var row in rowsCache) {
                if (cellCssClasses[key][row]) {
                    for (var columnId in cellCssClasses[key][row]) {
                        $(rowsCache[row]).children().eq(getColumnIndex(columnId))
                            .removeClass(cellCssClasses[key][row][columnId]);
                    }
                }
            }

            delete cellCssClasses[key];
        }

        function setCellCssStyles(key,hash) {
            removeCellCssStyles(key);
            addCellCssStyles(key,hash);
        }

        function flashCell(row, cell, speed) {
            speed = speed || 100;
            if (rowsCache[row]) {
                var $cell = $(getCellNode(row,cell));

                function toggleCellClass(times) {
                    if (!times) return;
                    setTimeout(function() {
                        $cell.queue(function() {
                            $cell.toggleClass(options.cellFlashingCssClass).dequeue();
                            toggleCellClass(times-1);
                        });
                    },
                    speed);
                }

                toggleCellClass(4);
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Interactivity

        function getSiblingIndex(node) {
            var idx = 0;
            while (node && node.previousSibling) {
                idx++;
                node = node.previousSibling;
            }
            return idx;
        }

        function handleDragInit(e,dd) {
            var cell = getCellFromEvent(e);
            if (!cell || !cellExists(cell.row, cell.cell)) {
                return false;
            }

            retval = trigger(self.onDragInit, dd, e);
            if (e.isImmediatePropagationStopped()) {
                return retval;
            }
        }

        function handleDragStart(e,dd) {
            var cell = getCellFromEvent(e);
            if (!cell || !cellExists(cell.row, cell.cell)) {
                return false;
            }

            var retval = trigger(self.onDragStart, dd, e);
            if (e.isImmediatePropagationStopped()) {
                return retval;
            }

            return false;
        }

        function handleDrag(e,dd) {
            return trigger(self.onDrag, dd, e);
        }

        function handleDragEnd(e,dd) {
            trigger(self.onDragEnd, dd, e);
        }

        function handleKeyDown(e) {
            trigger(self.onKeyDown, {}, e);
            var handled = e.isImmediatePropagationStopped();

            if (!handled) {
                if (!e.shiftKey && !e.altKey && !e.ctrlKey) {
                    if (e.which == 27) {
                        if (!getEditorLock().isActive()) {
                            return; // no editing mode to cancel, allow bubbling and default processing (exit without cancelling the event)
                        }
                        cancelEditAndSetFocus();
                    }
                    else if (e.which == 37) {
                        navigateLeft();
                    }
                    else if (e.which == 39) {
                        navigateRight();
                    }
                    else if (e.which == 38) {
                        navigateUp();
                    }
                    else if (e.which == 40) {
                        navigateDown();
                    }
                    else if (e.which == 9) {
                        navigateNext();
                    }
                    else if (e.which == 13) {
                        if (options.editable) {
                            if (currentEditor) {
                                // adding new row
                                if (activeRow === getDataLength()) {
                                    navigateDown();
                                }
                                else {
                                    commitEditAndSetFocus();
                                }
                            } else {
                                if (getEditorLock().commitCurrentEdit()) {
                                    makeActiveCellEditable();
                                }
                            }
                        }
                    }
                    else
                        return;
                }
                else if (e.which == 9 && e.shiftKey && !e.ctrlKey && !e.altKey) {
                    navigatePrev();
                }
                else
                    return;
            }

            // the event has been handled so don't let parent element (bubbling/propagation) or browser (default) handle it
            e.stopPropagation();
            e.preventDefault();
            try {
                e.originalEvent.keyCode = 0; // prevent default behaviour for special keys in IE browsers (F3, F5, etc.)
            }
            catch (error) {} // ignore exceptions - setting the original event's keycode throws access denied exception for "Ctrl" (hitting control key only, nothing else), "Shift" (maybe others)
        }

        function handleClick(e) {
            var cell = getCellFromEvent(e);
            if (!cell || (currentEditor !== null && activeRow == cell.row && activeCell == cell.cell)) {
                return;
            }

            trigger(self.onClick, {row:cell.row, cell:cell.cell}, e);
            if (e.isImmediatePropagationStopped()) {
                return;
            }

            if (options.enableCellNavigation && !columns[cell.cell].unselectable) {
                if (!getEditorLock().isActive() || getEditorLock().commitCurrentEdit()) {
                    scrollRowIntoView(cell.row,false);
                    setActiveCellInternal(getCellNode(cell.row,cell.cell), (cell.row === getDataLength()) || options.autoEdit);
                }
            }
        }

        function handleContextMenu(e) {
            var $cell = $(e.target).closest(".slick-cell", $canvas);
            if ($cell.length === 0) { return; }

            // are we editing this cell?
            if (activeCellNode === $cell[0] && currentEditor !== null) { return; }

            trigger(self.onContextMenu, {}, e);
        }

        function handleDblClick(e) {
            var cell = getCellFromEvent(e);
            if (!cell || (currentEditor !== null && activeRow == cell.row && activeCell == cell.cell)) {
                return;
            }

            trigger(self.onDblClick, {row:cell.row, cell:cell.cell}, e);
            if (e.isImmediatePropagationStopped()) {
                return;
            }

            if (options.editable) {
                gotoCell(cell.row, cell.cell, true);
            }
        }

        function handleHeaderContextMenu(e) {
            var $header = $(e.target).closest(".slick-header-column", ".slick-header-columns");
            var column = $header && columns[self.getColumnIndex($header.data("fieldId"))];
            trigger(self.onHeaderContextMenu, {column: column}, e);
        }

        function handleHeaderClick(e) {
            var $header = $(e.target).closest(".slick-header-column", ".slick-header-columns");
            var column = $header && columns[self.getColumnIndex($header.data("fieldId"))];
            trigger(self.onHeaderClick, {column: column}, e);
        }

        function handleMouseEnter(e) {
            trigger(self.onMouseEnter, {}, e);
        }

        function handleMouseLeave(e) {
            trigger(self.onMouseLeave, {}, e);
        }

        function cellExists(row,cell) {
            return !(row < 0 || row >= getDataLength() || cell < 0 || cell >= columns.length);
        }

        function getCellFromPoint(x,y) {
            var row = Math.floor((y+offset)/options.rowHeight);
            var cell = 0;

            var w = 0;
            for (var i=0; i<columns.length && w<x; i++) {
                w += (columns[i].currentWidth || columns[i].width);
                cell++;
            }

            if (cell < 0) {
                cell = 0;
            }

            return {row:row,cell:cell-1};
        }

        function getCellFromEvent(e) {
            var $cell = $(e.target).closest(".slick-cell", $canvas);
            if (!$cell.length)
                return null;

            return {
                row: $cell.parent().attr("row") | 0,
                cell: getSiblingIndex($cell[0])
            };
        }

        function getCellNodeBox(row,cell) {
             if (!cellExists(row,cell))
                 return null;

             var y1 = row * options.rowHeight - offset;
             var y2 = y1 + options.rowHeight - 1;
             var x1 = 0;
             for (var i=0; i<cell; i++) {
                 x1 += (columns[i].currentWidth || columns[i].width);
             }
             var x2 = x1 + (columns[cell].currentWidth || columns[cell].width);

             return {
                 top: y1,
                 left: x1,
                 bottom: y2,
                 right: x2
             };
         }

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Cell switching

        function resetActiveCell() {
            setActiveCellInternal(null,false);
        }

        function setFocus() {
            // IE tries to scroll the viewport so that the item being focused is aligned to the left border
            // IE-specific .setActive() sets the focus, but doesn't scroll
            if ($.browser.msie) {
                $canvas[0].setActive();
            }
            else {
                $canvas[0].focus();
            }
        }

        function scrollActiveCellIntoView() {
            if (activeCellNode) {
                var left = $(activeCellNode).position().left,
                    right = left + $(activeCellNode).outerWidth(),
                    scrollLeft = $viewport.scrollLeft(),
                    scrollRight = scrollLeft + $viewport.width();

                if (left < scrollLeft)
                    $viewport.scrollLeft(left);
                else if (right > scrollRight)
                    $viewport.scrollLeft(Math.min(left, right - $viewport[0].clientWidth));
            }
        }

        function setActiveCellInternal(newCell, editMode) {
            if (activeCellNode !== null) {
                makeActiveCellNormal();
                $(activeCellNode).removeClass("active");
            }

            var activeCellChanged = (activeCellNode !== newCell);
            activeCellNode = newCell;

            if (activeCellNode != null) {
                activeRow = parseInt($(activeCellNode).parent().attr("row"), 10);
                activeCell = getSiblingIndex(activeCellNode);

                $(activeCellNode).addClass("active");

                if (options.editable && editMode && isCellPotentiallyEditable(activeRow,activeCell)) {
                    clearTimeout(h_editorLoader);

                    if (options.asyncEditorLoading) {
                        h_editorLoader = setTimeout(function() { makeActiveCellEditable(); }, options.asyncEditorLoadDelay);
                    }
                    else {
                        makeActiveCellEditable();
                    }
                }
                else {
                      setFocus();
                }
            }
            else {
                activeRow = null;
                activeCell = null;
            }

            if (activeCellChanged) {
                scrollActiveCellIntoView();
                trigger(self.onActiveCellChanged, getActiveCell());
            }
        }

        function clearTextSelection() {
            if (document.selection && document.selection.empty) {
                document.selection.empty();
            }
            else if (window.getSelection) {
                var sel = window.getSelection();
                if (sel && sel.removeAllRanges) {
                    sel.removeAllRanges();
                }
            }
        }

        function isCellPotentiallyEditable(row,cell) {
            // is the data for this row loaded?
            if (row < getDataLength() && !getDataItem(row)) {
                return false;
            }

            // is this a special/non-data row?
            if (getDataItem(row) instanceof Slick.NonDataRow) {
                return false;
            }

            // are we in the Add New row?  can we create new from this cell?
            if (columns[cell].cannotTriggerInsert && row >= getDataLength()) {
                return false;
            }

            // does this cell have an editor?
            if (!getEditor(columns[cell])) {
                return false;
            }

            return true;
        }

        function makeActiveCellNormal() {
            if (!currentEditor) { return; }
            trigger(self.onBeforeCellEditorDestroy, {editor:currentEditor});
            currentEditor.destroy();
            currentEditor = null;

            if (activeCellNode) {
                $(activeCellNode).removeClass("editable invalid");

                if (getDataItem(activeRow)) {
                    var column = columns[activeCell];
                    activeCellNode.innerHTML = getFormatter(column)(activeRow, activeCell, getDataItem(activeRow)[column.field], column, getDataItem(activeRow));
                    invalidatePostProcessingResults(activeRow);
                }
            }

            // if there previously was text selected on a page (such as selected text in the edit cell just removed),
            // IE can't set focus to anything else correctly
            if ($.browser.msie) { clearTextSelection(); }

            getEditorLock().deactivate(editController);
        }

        function makeActiveCellEditable(editor) {
            if (!activeCellNode) { return; }
            if (!options.editable) {
                throw "Grid : makeActiveCellEditable : should never get called when options.editable is false";
            }

            // cancel pending async call if there is one
            clearTimeout(h_editorLoader);

            if (!isCellPotentiallyEditable(activeRow,activeCell)) {
                return;
            }

            var columnDef = columns[activeCell];
            var item = getDataItem(activeRow);

            if (trigger(self.onBeforeEditCell, {row:activeRow, cell:activeCell, item:item, column:columnDef}) === false) {
                setFocus();
                return;
            }

            getEditorLock().activate(editController);
            $(activeCellNode).addClass("editable");

            // don't clear the cell if a custom editor is passed through
            if (!editor) {
                activeCellNode.innerHTML = "";
            }

            currentEditor = new (editor || getEditor(columnDef))({
                grid: self,
                gridPosition: absBox($container[0]),
                position: absBox(activeCellNode),
                container: activeCellNode,
                column: columnDef,
                item: item || {},
                commitChanges: commitEditAndSetFocus,
                cancelChanges: cancelEditAndSetFocus
            });

            if (item)
                currentEditor.loadValue(item);

            serializedEditorValue = currentEditor.serializeValue();

            if (currentEditor.position)
                handleActiveCellPositionChange();
        }

        function commitEditAndSetFocus() {
            // if the commit fails, it would do so due to a validation error
            // if so, do not steal the focus from the editor
            if (getEditorLock().commitCurrentEdit()) {
                  setFocus();

                if (options.autoEdit) {
                    navigateDown();
                }
            }
        }

        function cancelEditAndSetFocus() {
            if (getEditorLock().cancelCurrentEdit()) {
                  setFocus();
            }
        }

        function absBox(elem) {
            var box = {top:elem.offsetTop, left:elem.offsetLeft, bottom:0, right:0, width:$(elem).outerWidth(), height:$(elem).outerHeight(), visible:true};
            box.bottom = box.top + box.height;
            box.right = box.left + box.width;

            // walk up the tree
            var offsetParent = elem.offsetParent;
            while ((elem = elem.parentNode) != document.body) {
                if (box.visible && elem.scrollHeight != elem.offsetHeight && $(elem).css("overflowY") != "visible")
                    box.visible = box.bottom > elem.scrollTop && box.top < elem.scrollTop + elem.clientHeight;

                if (box.visible && elem.scrollWidth != elem.offsetWidth && $(elem).css("overflowX") != "visible")
                    box.visible = box.right > elem.scrollLeft && box.left < elem.scrollLeft + elem.clientWidth;

                box.left -= elem.scrollLeft;
                box.top -= elem.scrollTop;

                if (elem === offsetParent) {
                    box.left += elem.offsetLeft;
                    box.top += elem.offsetTop;
                    offsetParent = elem.offsetParent;
                }

                box.bottom = box.top + box.height;
                box.right = box.left + box.width;
            }

            return box;
        }

        function getActiveCellPosition(){
            return absBox(activeCellNode);
        }

        function getGridPosition(){
            return absBox($container[0])
        }

        function handleActiveCellPositionChange() {
            if (!activeCellNode) return;
            var cellBox;

            trigger(self.onActiveCellPositionChanged, {});

            if (currentEditor) {
                cellBox = cellBox || getActiveCellPosition();
                if (currentEditor.show && currentEditor.hide) {
                    if (!cellBox.visible)
                        currentEditor.hide();
                    else
                        currentEditor.show();
                }

                if (currentEditor.position)
                    currentEditor.position(cellBox);
            }
        }

        function getCellEditor() {
            return currentEditor;
        }

        function getActiveCell() {
            if (!activeCellNode)
                return null;
            else
                return {row: activeRow, cell: activeCell};
        }

        function getActiveCellNode() {
            return activeCellNode;
        }

        function scrollRowIntoView(row, doPaging) {
            var rowAtTop = row * options.rowHeight;
            var rowAtBottom = (row + 1) * options.rowHeight - viewportH + (viewportHasHScroll?scrollbarDimensions.height:0);

            // need to page down?
            if ((row + 1) * options.rowHeight > scrollTop + viewportH + offset) {
                scrollTo(doPaging ? rowAtTop : rowAtBottom);
                render();
            }

            // or page up?
            else if (row * options.rowHeight < scrollTop + offset) {
                scrollTo(doPaging ? rowAtBottom : rowAtTop);
                render();
            }
        }

        function gotoDir(dy, dx, rollover) {
            if (!activeCellNode || !options.enableCellNavigation) { return; }
            if (!getEditorLock().commitCurrentEdit()) { return; }

            function selectableCellFilter() {
                return !columns[getSiblingIndex(this)].unselectable
            }

            var nextRow = rowsCache[activeRow + dy];
            var nextCell = (nextRow && activeCell + dx >= 0)
                    ? $(nextRow).children().eq(activeCell+dx).filter(selectableCellFilter)
                    : null;

            if (nextCell && !nextCell.length) {
                var nodes = $(nextRow).children()
                        .filter(function(index) { return (dx>0) ? index > activeCell + dx : index < activeCell + dx })
                        .filter(selectableCellFilter);

                if (nodes && nodes.length) {
                nextCell = (dx>0)
                            ? nodes.eq(0)
                            : nodes.eq(nodes.length-1);
                }
            }

            if (rollover && dy === 0 && !(nextRow && nextCell && nextCell.length)) {
                if (!nextCell || !nextCell.length) {
                    nextRow = rowsCache[activeRow + dy + ((dx>0)?1:-1)];
                    var nodes = $(nextRow).children().filter(selectableCellFilter);
                    if (dx > 0) {
                        nextCell = nextRow
                                ? nodes.eq(0)
                                : null;
                    }
                    else {
                        nextCell = nextRow
                                ? nodes.eq(nodes.length-1)
                                : null;
                    }
                }
            }

            if (nextRow && nextCell && nextCell.length) {
                // if selecting the 'add new' row, start editing right away
                var row = parseInt($(nextRow).attr("row"), 10);
                var isAddNewRow = (row == getDataLength());
                scrollRowIntoView(row,!isAddNewRow);
                setActiveCellInternal(nextCell[0], isAddNewRow || options.autoEdit);

                // if no editor was created, set the focus back on the grid
                if (!currentEditor) {
                      setFocus();
                }
            }
            else {
                  setFocus();
            }
        }

        function getCellNode(row, cell) {
            if (rowsCache[row]) {
                return $(rowsCache[row]).children().eq(cell)[0];
            }
            return null;
        }

        function setActiveCell(row, cell) {
            if (row > getDataLength() || row < 0 || cell >= columns.length || cell < 0) {
                return;
            }

            if (!options.enableCellNavigation) {
                return;
            }

            scrollRowIntoView(row,false);
            setActiveCellInternal(getCellNode(row,cell),false);
        }

        function canCellBeSelected(row,cell) {
            return row < getDataLength()
                    && row >= 0
                    && cell < columns.length
                    && cell >= 0
                    && !columns[cell].unselectable
                    && !(getDataItem(row) instanceof Slick.NonDataRow);
        }

        function gotoCell(row, cell, forceEdit) {
            if (row > getDataLength() || row < 0 || cell >= columns.length || cell < 0) { return; }
            if (!options.enableCellNavigation || columns[cell].unselectable) { return; }

            if (!getEditorLock().commitCurrentEdit()) { return; }

            scrollRowIntoView(row,false);

            var newCell = null;
            if (!columns[cell].unselectable) {
                newCell = getCellNode(row,cell);
            }

            // if selecting the 'add new' row, start editing right away
            setActiveCellInternal(newCell, forceEdit || (row === getDataLength()) || options.autoEdit);

            // if no editor was created, set the focus back on the grid
            if (!currentEditor) {
                  setFocus();
            }
        }

        function navigateUp() {
            gotoDir(-1, 0, false);
        }

        function navigateDown() {
            gotoDir(1, 0, false);
        }

        function navigateLeft() {
            gotoDir(0, -1, false);
        }

        function navigateRight() {
            gotoDir(0, 1, false);
        }

        function navigatePrev() {
            gotoDir(0, -1, true);
        }

        function navigateNext() {
            gotoDir(0, 1, true);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////
        // IEditor implementation for the editor lock

        function commitCurrentEdit() {
            var item = getDataItem(activeRow);
            var column = columns[activeCell];

            if (currentEditor) {
                if (currentEditor.isValueChanged()) {
                    var validationResults = currentEditor.validate();

                    if (validationResults.valid) {
                        if (activeRow < getDataLength()) {
                            var editCommand = {
                                row: activeRow,
                                cell: activeCell,
                                editor: currentEditor,
                                serializedValue: currentEditor.serializeValue(),
                                prevSerializedValue: serializedEditorValue,
                                execute: function() {
                                    this.editor.applyValue(item,this.serializedValue);
                                    updateRow(this.row);
                                },
                                undo: function() {
                                    this.editor.applyValue(item,this.prevSerializedValue);
                                    updateRow(this.row);
                                }
                            };

                            if (options.editCommandHandler) {
                                makeActiveCellNormal();
                                options.editCommandHandler(item,column,editCommand);

                            }
                            else {
                                editCommand.execute();
                                makeActiveCellNormal();
                            }

                            trigger(self.onCellChange, {
                                row: activeRow,
                                cell: activeCell,
                                item: item
                            });
                        }
                        else {
                            var newItem = {};
                            currentEditor.applyValue(newItem,currentEditor.serializeValue());
                            makeActiveCellNormal();
                            trigger(self.onAddNewRow, {item:newItem, column:column});
                        }

                        // check whether the lock has been re-acquired by event handlers
                        return !getEditorLock().isActive();
                    }
                    else {
                        // TODO: remove and put in onValidationError handlers in examples
                        $(activeCellNode).addClass("invalid");
                        $(activeCellNode).stop(true,true).effect("highlight", {color:"red"}, 300);

                        trigger(self.onValidationError, {
                            editor: currentEditor,
                            cellNode: activeCellNode,
                            validationResults: validationResults,
                            row: activeRow,
                            cell: activeCell,
                            column: column
                        });

                        currentEditor.focus();
                        return false;
                    }
                }

                makeActiveCellNormal();
            }
            return true;
        }

        function cancelCurrentEdit() {
            makeActiveCellNormal();
            return true;
        }

        function rowsToRanges(rows) {
            var ranges = [];
            var lastCell = columns.length - 1;
            for (var i = 0; i < rows.length; i++) {
                ranges.push(new Slick.Range(rows[i], 0, rows[i], lastCell));
            }
            return ranges;
        }

        function getSelectedRows() {
            if (!selectionModel) {
                throw "Selection model is not set";
            }
            return selectedRows;
        }

        function setSelectedRows(rows) {
            if (!selectionModel) {
                throw "Selection model is not set";
            }
            selectionModel.setSelectedRanges(rowsToRanges(rows));
        }


        //////////////////////////////////////////////////////////////////////////////////////////////
        // Debug

        this.debug = function() {
            var s = "";

            s += ("\n" + "counter_rows_rendered:  " + counter_rows_rendered);
            s += ("\n" + "counter_rows_removed:  " + counter_rows_removed);
            s += ("\n" + "renderedRows:  " + renderedRows);
            s += ("\n" + "numVisibleRows:  " + numVisibleRows);
            s += ("\n" + "maxSupportedCssHeight:  " + maxSupportedCssHeight);
            s += ("\n" + "n(umber of pages):  " + n);
            s += ("\n" + "(current) page:  " + page);
            s += ("\n" + "page height (ph):  " + ph);
            s += ("\n" + "scrollDir:  " + scrollDir);

            alert(s);
        };

        // a debug helper to be able to access private members
        this.eval = function(expr) {
            return eval(expr);
        };

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Public API

        $.extend(this, {
            "slickGridVersion": "2.0a1",

            // Events
            "onScroll":                     new Slick.Event(),
            "onSort":                       new Slick.Event(),
            "onHeaderContextMenu":          new Slick.Event(),
            "onHeaderClick":                new Slick.Event(),
            "onMouseEnter":                 new Slick.Event(),
            "onMouseLeave":                 new Slick.Event(),
            "onClick":                      new Slick.Event(),
            "onDblClick":                   new Slick.Event(),
            "onContextMenu":                new Slick.Event(),
            "onKeyDown":                    new Slick.Event(),
            "onAddNewRow":                  new Slick.Event(),
            "onValidationError":            new Slick.Event(),
            "onViewportChanged":            new Slick.Event(),
            "onColumnsReordered":           new Slick.Event(),
            "onColumnsResized":             new Slick.Event(),
            "onCellChange":                 new Slick.Event(),
            "onBeforeEditCell":             new Slick.Event(),
            "onBeforeCellEditorDestroy":    new Slick.Event(),
            "onBeforeDestroy":              new Slick.Event(),
            "onActiveCellChanged":          new Slick.Event(),
            "onActiveCellPositionChanged":  new Slick.Event(),
            "onDragInit":                   new Slick.Event(),
            "onDragStart":                  new Slick.Event(),
            "onDrag":                       new Slick.Event(),
            "onDragEnd":                    new Slick.Event(),
            "onSelectedRowsChanged":        new Slick.Event(),

            // Methods
            "registerPlugin":               registerPlugin,
            "unregisterPlugin":             unregisterPlugin,
            "getColumns":                   getColumns,
            "setColumns":                   setColumns,
            "getColumnIndex":               getColumnIndex,
            "updateColumnHeader":           updateColumnHeader,
            "setSortColumn":                setSortColumn,
            "autosizeColumns":              autosizeColumns,
            "getOptions":                   getOptions,
            "setOptions":                   setOptions,
            "getData":                      getData,
            "getDataLength":                getDataLength,
            "getDataItem":                  getDataItem,
            "setData":                      setData,
            "getSelectionModel":            getSelectionModel,
            "setSelectionModel":            setSelectionModel,
            "getSelectedRows":              getSelectedRows,
            "setSelectedRows":              setSelectedRows,

            "render":                       render,
            "invalidate":                   invalidate,
            "invalidateRow":                invalidateRow,
            "invalidateRows":               invalidateRows,
            "invalidateAllRows":            invalidateAllRows,
            "updateCell":                   updateCell,
            "updateRow":                    updateRow,
            "getViewport":                  getVisibleRange,
            "resizeCanvas":                 resizeCanvas,
            "updateRowCount":               updateRowCount,
            "scrollRowIntoView":            scrollRowIntoView,
            "getCanvasNode":                getCanvasNode,

            "getCellFromPoint":             getCellFromPoint,
            "getCellFromEvent":             getCellFromEvent,
            "getActiveCell":                getActiveCell,
            "setActiveCell":                setActiveCell,
            "getActiveCellNode":            getActiveCellNode,
            "getActiveCellPosition":        getActiveCellPosition,
            "resetActiveCell":              resetActiveCell,
            "editActiveCell":               makeActiveCellEditable,
            "getCellEditor":                getCellEditor,
            "getCellNode":                  getCellNode,
            "getCellNodeBox":               getCellNodeBox,
            "canCellBeSelected":            canCellBeSelected,
            "navigatePrev":                 navigatePrev,
            "navigateNext":                 navigateNext,
            "navigateUp":                   navigateUp,
            "navigateDown":                 navigateDown,
            "navigateLeft":                 navigateLeft,
            "navigateRight":                navigateRight,
            "gotoCell":                     gotoCell,
            "getTopPanel":                  getTopPanel,
            "showTopPanel":                 showTopPanel,
            "hideTopPanel":                 hideTopPanel,
            "showHeaderRowColumns":         showHeaderRowColumns,
            "hideHeaderRowColumns":         hideHeaderRowColumns,
            "getHeaderRow":                 getHeaderRow,
            "getHeaderRowColumn":           getHeaderRowColumn,
            "getGridPosition":              getGridPosition,
            "flashCell":                    flashCell,
            "addCellCssStyles":             addCellCssStyles,
            "setCellCssStyles":             setCellCssStyles,
            "removeCellCssStyles":          removeCellCssStyles,

            "destroy":                      destroy,

            // IEditor implementation
            "getEditorLock":                getEditorLock,
            "getEditController":            getEditController
        });

        init();
    }
}(jQuery));
(function($) {
    // register namespace
    $.extend(true, window, {
        "Slick": {
            "RowSelectionModel":    RowSelectionModel
        }
    });

    function RowSelectionModel(options) {
        var _grid;
        var _ranges = [];
        var _self = this;
        var _options;
        var _defaults = {
            selectActiveRow: true
        };

        function init(grid) {
            _options = $.extend(true, {}, _defaults, options);
            _grid = grid;
            _grid.onActiveCellChanged.subscribe(handleActiveCellChange);
            _grid.onKeyDown.subscribe(handleKeyDown);
            _grid.onClick.subscribe(handleClick);
        }

        function destroy() {
            _grid.onActiveCellChanged.unsubscribe(handleActiveCellChange);
            _grid.onKeyDown.unsubscribe(handleKeyDown);
            _grid.onClick.unsubscribe(handleClick);
        }

        function rangesToRows(ranges) {
            var rows = [];
            for (var i = 0; i < ranges.length; i++) {
                for (var j = ranges[i].fromRow; j <= ranges[i].toRow; j++) {
                    rows.push(j);
                }
            }
            return rows;
        }

        function rowsToRanges(rows) {
            var ranges = [];
            var lastCell = _grid.getColumns().length - 1;
            for (var i = 0; i < rows.length; i++) {
                ranges.push(new Slick.Range(rows[i], 0, rows[i], lastCell));
            }
            return ranges;
        }

        function getRowsRange(from,to) {
            var i, rows = [];
            for (i = from; i <= to; i++) {
                rows.push(i);
            }
            for (i = to; i < from; i++) {
                rows.push(i);
            }
            return rows;
        }

        function getSelectedRows() {
            return rangesToRows(_ranges);
        }

        function setSelectedRows(rows) {
            setSelectedRanges(rowsToRanges(rows));
        }

        function setSelectedRanges(ranges) {
            _ranges = ranges;
            _self.onSelectedRangesChanged.notify(_ranges);
        }

        function getSelectedRanges() {
            return _ranges;
        }

        function handleActiveCellChange(e, data) {
            if (_options.selectActiveRow) {
                setSelectedRanges([new Slick.Range(data.row, 0, data.row, _grid.getColumns().length - 1)]);
            }
        }

        function handleKeyDown(e) {
            var activeRow = _grid.getActiveCell();
            if (activeRow && e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey && (e.which == 38 || e.which == 40)) {
                var selectedRows = getSelectedRows();
                selectedRows.sort(function(x,y) { return x-y });

                if (!selectedRows.length) {
                    selectedRows = [activeRow.row];
                }

                var top = selectedRows[0];
                var bottom = selectedRows[selectedRows.length - 1];
                var active;

                if (e.which == 40) {
                    active = activeRow.row < bottom || top == bottom ? ++bottom : ++top;
                }
                else {
                    active = activeRow.row < bottom ? --bottom : --top;
                }

                if (active >= 0 && active < _grid.getDataLength()) {
                    _grid.scrollRowIntoView(active);
                    _ranges = rowsToRanges(getRowsRange(top,bottom));
                    setSelectedRanges(_ranges);
                }

                e.preventDefault();
                e.stopPropagation();
            }
        }

        function handleClick(e) {
            var cell = _grid.getCellFromEvent(e);
            if (!cell || !_grid.canCellBeSelected(cell.row, cell.cell)) {
                return false;
            }

            var selection = rangesToRows(_ranges);
            var idx = $.inArray(cell.row, selection);

            if (!e.ctrlKey && !e.shiftKey && !e.metaKey) {
                return false;
            }
            else if (_grid.getOptions().multiSelect) {
                if (idx === -1 && (e.ctrlKey || e.metaKey)) {
                    selection.push(cell.row);
                    _grid.setActiveCell(cell.row, cell.cell);
                }
                else if (idx !== -1 && (e.ctrlKey || e.metaKey)) {
                    selection = $.grep(selection, function(o, i) { return (o !== cell.row); });
                    _grid.setActiveCell(cell.row, cell.cell);
                }
                else if (selection.length && e.shiftKey) {
                    var last = selection.pop();
                    var from = Math.min(cell.row, last);
                    var to = Math.max(cell.row, last);
                    selection = [];
                    for (var i = from; i <= to; i++) {
                        if (i !== last) {
                            selection.push(i);
                        }
                    }
                    selection.push(last);
                    _grid.setActiveCell(cell.row, cell.cell);
                }
            }

            _ranges = rowsToRanges(selection);
            setSelectedRanges(_ranges);
            e.stopImmediatePropagation();

            return true;
        }

        $.extend(this, {
            "getSelectedRows":              getSelectedRows,
            "setSelectedRows":              setSelectedRows,

            "getSelectedRanges":            getSelectedRanges,
            "setSelectedRanges":            setSelectedRanges,

            "init":                         init,
            "destroy":                      destroy,

            "onSelectedRangesChanged":        new Slick.Event()
        });
    }
})(jQuery);
/*
 * This script extends JQuery.
 *
 * registerNewModule is a class that helps manage large javascripts codebases.
 * All scripts should be registered with this module and supply a namespaced name and a function.
 * The function can optionally return an init method that will automatically get initialized by this script.
 * This script also allows you to set default methods and properties for all scripts. For example
 * the imageUrl property stores the path or the images folder.
 */

(function ($, window, document, undefined) {
    var top_level = window, default_module = {
        imageUrl: '/images',
        liveDataRoot: '/jax/',
        htmlRoot: '/prototype/modules/',
        appRoot: '/prototype/',
        obj: function () {return {};}
    };
    var check_dependencies = function (module) {
        if (!$.isArray(module.dependencies)){
            if (typeof console !== 'undefined' && console.log)
                console.log('[warning] ' + module.name + ' does not define dependencies');
            return;
        };
        module.dependencies.map(function (dependency) {
            try {
                dependency.split('.').reduce(function (acc, val) {
                    if (typeof acc[val] === 'undefined') throw new Error; else return acc[val];
                }, top_level);
            }catch(error){
                throw new ReferenceError(module + ' requires ' + dependency);
            };
        });
    };
    $.extend({
        registerNewModule: function (module) {
            var name = module.name, new_module, levels, scanned, last, last_parent;
            if (!name) throw new TypeError('registerNewModule: module name is undefined');
            if (typeof module.obj !== 'function')
                throw new TypeError('registerNewModule: ' + name + '.obj is undefined');
            check_dependencies(module);
            new_module = $.extend({}, default_module, module).obj();
            levels = name.split('.');
            scanned = levels.reduce(function (acc, val, index) {
                return acc.concat(index ? acc[index - 1] + '.' + val : val);
            }, []);
            scanned.map(function (level) {
                var hier = level.split('.'), last = hier.pop(),
                    last_parent = hier.reduce(function (acc, val) {return acc[val];}, top_level);
                return last_parent[last] || (last_parent[last] = {});
            });
            last = levels.pop();
            last_parent = levels.reduce(function (acc, val) {return acc[val];}, top_level);
            if (last_parent[last].name) throw new Error('registerNewModule: ' + name + ' already exists');
            last_parent[last] = new_module;
            if (typeof new_module.init === 'function') new_module.init();
            delete new_module.init;
        }
    });
})(jQuery, this, document);
$.registerNewModule({
    name: 'og.api.common',
    dependencies: [],
    obj: function () {
        var debug_flag = false;
        /** @private */
        var output = function () {
            if (debug_flag && typeof console !== 'undefined' && console.log)
                console.log.apply(null, Array.prototype.slice.call(arguments));
        };
        return {
            debug: output.partial('[debug]'),
            end_loading: function () {/*global ajax loading events end here*/},
            set_debug: function (mode) {debug_flag = mode;},
            start_loading: function (loading_method) {
                if (loading_method) loading_method();
                /*global ajax loading events start here*/
            },
            warn: output.partial('[warning]')
        };
    }
});
// API call for making and caching static requests
$.registerNewModule({
    name: 'og.api.static',
    dependencies: ['og.api.common'],
    obj: function () {
        var html_cache = {}, module = this, html_root = module.htmlRoot,
            start_loading = og.api.common.start_loading, end_loading = og.api.common.end_loading;
        /**
         * takes a module name (like <code>'og.common.details.foo'</code>) and returns a path:
         * <code>app_root/modules/common/details/og.common.details.foo.jsp</code>
         * not public, but documented because <code>og.api['static']/code> uses it
         * @see og.api['static']
         * @inner
         */
        var module_path = function (page) {
            return html_root + [page.split('.').slice(1, -1).join('/'), page.toLowerCase() + '.jsp'].join('/');
        };
        return function (config) {
            if (typeof config === 'undefined') throw new TypeError('static: config is undefined');
            if (typeof config.url !== 'string' && typeof config.module !== 'string')
                throw new TypeError('static: either config.url or config.module must be a string');
            var url = config.url || module_path(config.module),
                do_not_cache = config.do_not_cache, clear_cache = config.clear_cache,
                handler = function (html) {
                    if (!do_not_cache) html_cache[url] = html;
                    end_loading();
                    config.handler(html);
                };
            start_loading(config.loading);
            if (clear_cache) delete html_cache[url];
            if (url in html_cache) return (do_not_cache = true) && handler(html_cache[url]);
            $.ajax({url: url, success: handler});
        };
    }
});
/**
 * provides wrappers for the REST API
 */
$.registerNewModule({
    name: 'og.api.rest',
    dependencies: ['og.api.common'],
    obj: function () {
        var module = this, app_root = module.appRoot, live_data_root = module.liveDataRoot, api, common = og.api.common,
            start_loading = common.start_loading, end_loading = common.end_loading, debug = common.debug,
            request_id = 0, outstanding_requests = {}, PAGE_SIZE = 50, PAGE = 1;
        /** @private */
        var request = function (method, config) {
            var id = request_id++;
            start_loading(config.loading);
            outstanding_requests[id] = true;
            outstanding_requests[id] = $.ajax({
                url: live_data_root + method.join('/'),
                type: config.type || 'GET',
                data: config.data,
                headers: {'Accept': 'application/json'},
                dataType: 'json',
                beforeSend: function (xhr, req) {
                    var aborted = !(id in outstanding_requests);
                    debug((aborted ? 'ABORTED: ' : '') + req.type + ' ' + req.url + ' HTTP/1.1' +
                        (req.type !== 'GET' ? '\n\n' + req.data : ''));
                    if (aborted) return false;
                },
                error: function (xhr, status, error) {
                    delete outstanding_requests[id];
                    config.handler({error: true, message: error, data: null, meta: {}});
                },
                success: function (data, status, xhr) {
                    delete outstanding_requests[id];
                    var meta = {}, location = xhr.getResponseHeader('Location');
                    if (location) meta.id = location.split('/').pop();
                    config.handler({error: false, message: status, data: data, meta: meta});
                },
                complete: end_loading
            });
            return id;
        };
        /** @private */
        var check = (function () {
            /** @private */
            var check_common_params = function (bundle) {
                var config = bundle.config, method = bundle.method;
                if (typeof config.handler !== 'function')
                    throw new TypeError(method + ': config.handler must be a function');
            };
            /** @private */
            var check_dependencies = function (bundle, dependencies) {
                var config = bundle.config, method = bundle.method;
                if (!$.isArray(dependencies)) dependencies = [dependencies];
                dependencies.map(function (dependency) {
                    if (!dependency.require || !dependency.fields)
                        throw new TypeError('check_dependencies: each dependency must have "fields" and "require"');
                    var require = dependency.require;
                    dependency.fields.map(function (field) {
                        if (typeof config[require] !== 'undefined') return;
                        if (typeof config[field] !== 'undefined')
                            throw new ReferenceError(method + ': ' + field + ' was referenced without ' + require);
                    });
                });
            };
            // if params.condition is true then params.fields' values must not exist in config
            // params.label is optional, but gives extra information
            /** @private */
            var check_empties = function (bundle, params) {
                var config = bundle.config, method = bundle.method;
                if (!$.isArray(params)) params = [params];
                params.map(function (obj) {
                    var condition = obj.condition, fields = obj.fields, label = obj.label || 'unknown reason';
                    if (!$.isArray(fields))
                        throw new TypeError('check_empties: obj.fields must be an array');
                    if (!condition) return;
                    fields.map(function (field) {
                        if (typeof config[field] === 'undefined') return;
                        throw new TypeError(method + ': ' + field + ' cannot be defined because: ' + label);
                    });
                });

            };
            /** @private */
            var check_required = function (bundle, params) {
                if (!$.isArray(params)) params = [params];
                var method = bundle.method,
                    exists = function (acc, val) {return acc + (val in bundle.config ? 1 : 0);};
                params.map(function (obj) {
                    var one_of = obj.one_of, all_of = obj.all_of,
                        condition = ('condition' in obj ? obj.condition : true);
                    if (condition && one_of && !one_of.reduce(exists, 0))
                        throw new TypeError(method + ': one of {' + one_of.join(' | ') + '} must be defined');
                    if (condition && all_of && all_of.length > all_of.reduce(exists, 0))
                        throw new TypeError(method + ': {' + all_of.join(' & ') + '} must be defined');
                });
            };
            return function (params) {
                var bundle = params.bundle, dependencies = params.dependencies,
                    empties = params.empties, required = params.required;
                [bundle, bundle.method, bundle.config].map(function (required) {
                    if (typeof required === 'undefined')
                        throw new TypeError('check: params.bundle must contain method and config');
                });
                check_common_params(bundle);
                if (required) check_required(bundle, required);
                if (empties) check_empties(bundle, empties);
                if (dependencies) check_dependencies(bundle, dependencies);
            }
        })();
        // convert all incoming params into strings (so for example, the value 0 ought to be truthy, not falsey)
        /** @private */
        var str = function (val) {return typeof val === 'undefined' ? '' : $.isArray(val) ? val.join('\n') : '' + val;};
        /** @private */
        var default_get = function (fields, api_fields, config) {
            var root = this.root, method = [root], data = {},
                handler = config.handler, loading = config.loading,
                id = str(config.id), version = str(config.version), version_search = version === '*',
                field_search = fields.reduce(function (acc, val) {return val in config || acc;}, false),
                page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
            check({
                bundle: {method: root + '#get', config: config},
                dependencies: [{fields: ['version'], require: 'id'}],
                empties: [{condition: field_search, label: 'search request', fields: ['version', 'id']}]
            });
            if (field_search || version_search || !id) data = {pageSize: page_size, page: page};
            if (field_search) fields.map(function (field, index) {
                if (field in config) data[(api_fields || fields)[index]] = config[field];
            });
            version = version ? [id, 'versions', version_search ? '' : version].join('/') : id;
            if (id) method.push(version);
            if (data.type) data.type = data.type.toUpperCase(); // all types are uppercase
            return request(method, {data: data, handler: handler, loading: loading});
        };
        /** @private */
        var default_del = function (config) {
            var root = this.root, method = [root], data = {method: 'DELETE'},
                handler = config.handler, loading = config.loading,
                id = str(config.id), version = str(config.version);
            check({
                bundle: {method: root + '#del', config: config},
                required: [{all_of: ['id']}],
                dependencies: [{fields: ['version'], require: 'id'}]
            });
            method.push(version ? [id, 'versions', version].join('/') : id);
            return request(method, {data: data, handler: handler, loading: loading, type: 'POST'});
        };
        /** @private */
        var not_available = function (method) {
            throw new Error(this.root + '#' + method + ' does not exist in the REST API');
        };
        /** @private */
        var not_implemented = function (method) {
            throw new Error(this.root + '#' + method + ' exists in the REST API, but does not have a JS counterpart');
        };
        return api = {
            abort: function (id) {
                var xhr = outstanding_requests[id];
                if (!xhr) return;
                delete outstanding_requests[id];
                if (typeof xhr === 'object' && 'abort' in xhr) xhr.abort();
            },
            batches: { // all requests that begin with /batches
                root: 'batches',
                get: function (config) {
                    var root = this.root, method = [root], data = {},
                        handler = config.handler, loading = config.loading,
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE,
                        observation_date = str(config.observation_date),
                        observation_time = str(config.observation_time),
                        is_id = !!observation_date && !!observation_time;
                    check({
                        bundle: {method: root + '#get', config: config},
                        empties: [{condition: is_id, label: 'unique batch requested', fields: ['page', 'page_size']}]
                    });
                    if (is_id){
                        method.push(observation_date, observation_time);
                    }else{
                        data = {pageSize: page_size, page: page};
                        if (observation_date) data.observationDate = observation_date;
                        if (observation_time) data.observationTime = observation_time;
                    };
                    return request(method, {data: data, handler: handler, loading: loading});
                },
                put: not_available.partial('put'),
                del: not_available.partial('del')
            },
            configs: { // all requests that begin with /configs
                root: 'configs',
                get: not_implemented.partial('get'),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            exchanges: { // all requests that begin with /exchanges
                root: 'exchanges',
                get: default_get.partial(['name'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            holidays: { // all requests that begin with /holidays
                root: 'holidays',
                get: default_get.partial(['name', 'type', 'currency'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            portfolios: { // all requests that begin with /portfolios
                root: 'portfolios',
                get: function (config) {
                    var root = this.root, method = [root], data = {},
                        handler = config.handler, loading = config.loading,
                        id = str(config.id), node = str(config.node), version = str(config.version),
                        name = str(config.name), name_search =  'name' in config, version_search = version === '*',
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
                    check({
                        bundle: {method: root + '#get', config: config},
                        dependencies: [{fields: ['node', 'version'], require: 'id'}],
                        empties: [
                            {condition: name_search, label: 'name exists', fields: ['node', 'version', 'id']},
                            {condition: version_search, label: 'version is *', fields: ['node']}
                        ]
                    });
                    if (version_search || name_search || !id) data = {pageSize: page_size, page: page};
                    if (name_search) data.name = name;
                    version = version ? [id, 'versions', version_search ? '' : version].join('/') : id;
                    if (id) method = method.concat(node ? [version, 'nodes', node] : version);
                    return request(method, {data: data, handler: handler, loading: loading});
                },
                put: function (config) {
                    var root = this.root, method = [root], data = {method: 'PUT'},
                        handler = config.handler, loading = config.loading,
                        name = str(config.name), id = str(config.id), version = str(config.version),
                        node = str(config.node), new_node = config.node === true, position = str(config.position);
                    check({
                        bundle: {method: root + '#put', config: config},
                        required: [
                            {one_of: ['name', 'id']},
                            {one_of: ['name', 'position']}
                        ],
                        dependencies: [
                            {fields: ['node', 'version', 'position'], require: 'id'},
                            {fields: ['position'], require: 'node'}
                        ],
                        empties: [
                            {condition: !!name, label: 'name exists', fields: ['position']},
                            {condition: new_node, label: 'node is not set to an ID', fields: ['position']}
                        ]
                    });
                    if (name) data.name = name;
                    version = version ? [id, 'versions', version].join('/') : id;
                    if (id) method.push(version);
                    if (new_node || node) method = method.concat(new_node ? 'nodes' : ['nodes', node]);
                    if (position){
                        method.push('positions');
                        data.uid = position;
                    };
                    if (!id || new_node || position) data.method = 'POST';
                    return request(method, {data: data, handler: handler, loading: loading, type: 'POST'});
                },
                del: function (config) {
                    var root = this.root, method = [root], data = {method: 'DELETE'},
                        handler = config.handler, loading = config.loading,
                        id = str(config.id), version = str(config.version), node = str(config.node);
                    check({
                        bundle: {method: root + '#del', config: config},
                        required: [{all_of: ['id']}],
                        dependencies: [{fields: ['node', 'version'], require: 'id'}]
                    });
                    method.push(version ? [id, 'versions', version].join('/') : id);
                    if (node) method.push('nodes', node);
                    return request(method, {data: data, handler: handler, loading: loading, type: 'POST'});
                }
            },
            positions: { // all requests that begin with /positions
                root: 'positions',
                get: default_get.partial(['min_quantity', 'max_quantity'], ['minquantity', 'maxquantity']),
                put: function (config) {
                    var root = this.root, method = [root], data = {method: 'PUT'},
                        handler = config.handler, loading = config.loading,
                        id = str(config.id), version = str(config.version),
                        fields = ['identifier', 'quantity', 'scheme_type'],
                        api_fields = ['idvalue', 'quantity', 'idscheme'];
                    check({
                        bundle: {method: root + '#put', config: config},
                        dependencies: [{fields: ['version'], require: 'id'}],
                        required: [{all_of: fields}]
                    });
                    fields.map(function (val, index) {if (val = str(config[val])) data[api_fields[index]] = val;});
                    if (id)
                        method.push(version ? [id, 'versions', version].join('/') : id);
                    else
                        data.method = 'POST';
                    return request(method, {data: data, handler: handler, loading: loading, type: 'POST'});
                },
                del: default_del
            },
            regions: { // all requests that begin with /regions
                root: 'regions',
                get: default_get.partial(['name', 'classification'], null),
                put: not_implemented.partial('put'),
                del: not_implemented.partial('del')
            },
            securities: { // all requests that begin with /securities
                root: 'securities',
                get: default_get.partial(['name', 'type'], null),
                put: function (config) {
                    var root = this.root, method = [root], data = {method: 'PUT'},
                        handler = config.handler, loading = config.loading,
                        id = str(config.id), fields = ['identifier', 'scheme_type'],
                        api_fields = ['idvalue', 'idscheme'];
                    check({
                        bundle: {method: root + '#put', config: config},
                        empties: [{condition: !!id, label: 'ID exists', fields: fields}],
                        required: [{condition: !id, all_of: fields}]
                    });
                    fields.map(function (val, index) {if (val = str(config[val])) data[api_fields[index]] = val;});
                    if (id) method.push(id); else data.method = 'POST';
                    return request(method, {data: data, handler: handler, loading: loading, type: 'POST'});
                },
                del: default_del
            },
            timeseries: { // all requests that begin with /timeseries
                root: 'timeseries',
                get: function (config) {
                    var root = this.root, method = [root], data = {},
                        handler = config.handler, loading = config.loading, id = str(config.id),
                        fields = ['identifier', 'data_source', 'data_provider', 'data_field', 'observation_time'],
                        api_fields = ['identifier', 'dataSource', 'dataProvider', 'dataField', 'observationTime'],
                        search = !id || fields.reduce(function (acc, val) {return val in config || acc;}, false),
                        page_size = str(config.page_size) || PAGE_SIZE, page = str(config.page) || PAGE;
                    check({
                        bundle: {method: root + '#get', config: config},
                        empties: [{condition: search, label: 'search request', fields: ['id']}]
                    });
                    if (search){
                        data = {pageSize: page_size, page: page};
                        fields.map(function (val, index) {if (val in config) data[api_fields[index]] = config[val];});
                    }else{
                        method.push(id);
                    };
                    return request(method, {data: data, handler: handler, loading: loading});
                },
                put: function (config) {
                    var root = this.root, method = [root], data = {method: 'POST'},
                        handler = config.handler, loading = config.loading,
                        fields = ['data_provider', 'data_field', 'start', 'end', 'scheme_type', 'identifier'],
                        api_fields = ['dataProvider', 'dataField', 'start', 'end', 'idscheme', 'idvalue'];
                    check({
                        bundle: {method: root + '#put', config: config},
                        required: [{all_of: fields}]
                    });
                    fields.map(function (val, index) {if (val = str(config[val])) data[api_fields[index]] = val;});
                    return request(method, {data: data, handler: handler, loading: loading, type: 'POST'});
                },
                del: default_del
            }
        };
    }
});

$.registerNewModule({
    name: 'og.api.live',
    obj: function () {
        return {};
    }
});
/**
 * maps URL hash routes
 */
$.registerNewModule({
    name: 'og.common.routes',
    dependencies: ['og.api.common'],
    obj: function () {
        var active_routes = {}, routes, added_routes = {}, flat_routes = [], last = '',
            scalar_regex = /^:([^:\?]+)(\??)$/, keyval_regex = /^([^:\?]+):(\??)$/,
            /** @private */
            parse = function (path) {
                // go with the longest matching route, making sure all routes have trailing slashes
                var route = flat_routes.filter(function (val) {
                        /([^\/])$/.exec(path); // this populates RegExp.$1 because replace won't
                        return ~path.replace(/([^\/])$/, RegExp.$1 + '/').indexOf(val);
                    })[0],
                    request = route && path.replace(route, '').split('/').reduce(function (acc, val) {
                        var split = val.split('=');
                        if (!val.length) return acc;
                        return (split.length === 2 ? acc.keyvals[split[0]] = split[1] : acc.scalars.push(val)) && acc;
                    }, {keyvals: {}, scalars: []});
                if (!route) return [];
                return active_routes[route].map(function (rule_set) {
                    var args = {}, scalars = rule_set.rules.scalars, keyvals = rule_set.rules.keyvals, method,
                        keyval_names = keyvals.reduce(function (acc, val) {return (acc[val.name] = null) || acc;}, {}),
                        required_scalars_length = scalars.filter(function (val) {return val.required;}).length,
                        required_keyvals = keyvals.filter(function (val) {return val.required;}),
                        required_keyvals_exist = required_keyvals.reduce(function (acc, val) {
                            return acc && val.name in request.keyvals;
                        }, true);
                    // not enough parameters are supplied in the request for this rule
                    if (required_scalars_length > request.scalars.length || !required_keyvals_exist) return null;
                    // too many parameters are supplied in the request for this rule
                    if (request.scalars.length > scalars.length) return null;
                    // a keyval parameter that is not recognized has been supplied
                    for (var keyval in request.keyvals) if (!(keyval in keyval_names)) return null;
                    // populate args scalars
                    request.scalars.map(function (scalar, index) {args[scalars[index].name] = scalar;});
                    // populate args keyvals
                    keyvals.map(function (keyval) {args[keyval.name] = request.keyvals[keyval.name];});
                    // make sure the rule's method actually exists and can be accessed
                    try{
                        method = rule_set.method.split('.').reduce(function (acc, val) {return acc[val];}, window);
                        if (typeof method !== 'function') throw new Error();
                    }catch(error){
                        throw new TypeError('parse: ' + rule_set.method + ' is undefined');
                    };
                    return {page: route, hash: routes.hash({route: rule_set.raw}, args), method: method, args: args};
                }).filter(function (val) {return !!val;}).sort(function (a, b) {return b.hash.length - a.hash.length;});
            },
            /** @private */
            compile = function (route) {
                var default_param,
                    // all requests can have these named params -- they are optional unless mandated by a supplied rule
                    default_params = {debug: false},
                    compiled_route = route.split('/').reduce(function (acc, val) {
                        var rule;
                        // first construct the name of the method
                        if (!~val.indexOf(':') && !acc.rules.scalars.length && !acc.rules.keyvals.length)
                            return acc.method.push(val) && acc;
                        // then construct the parameters
                        if (val.match(scalar_regex)){
                            rule = {name: RegExp.$1, required: !RegExp.$2};
                            acc.rules.scalars.push(rule);
                            if (!rule.required)
                                acc.last_optional = val;
                            else
                                if (rule.required && acc.last_optional)
                                    throw new Error('add: param "' + val + '" follows "' + acc.last_optional + '"');
                        };
                        if (val.match(keyval_regex)){
                            rule = {name: RegExp.$1, required: !RegExp.$2};
                            // a supplied rule should always supersede a default param
                            if (rule.name in default_params) delete default_params[rule.name];
                            acc.rules.keyvals.push(rule);
                        };
                        if (!rule) throw new SyntaxError('compile: the rule "' + val + '" was not understood');
                        return acc;
                    }, {method: [], rules: {scalars: [], keyvals: []}, last_optional: ''});
                // convert method back into a string with proper slashes
                compiled_route.method = compiled_route.method.concat('').join('/').replace(/\/\/$/, '/');
                // add any default params that haven't already been added by the rule definition
                for (default_param in default_params)
                    compiled_route.rules.keyvals.push({name: default_param, required: default_params[default_param]});
                return compiled_route;
            },
            /** @private */
            hash_handler = function () {
                var path = window.location.hash.substring(1) || '/', parsed;
                // simulate directory requests and add a slash if required (only if it's a path)
                if (path[0] === '/' && path[1] && path[path.length - 1] !== '/') return window.location.hash += '/';
                parsed = parse(path);
                // if the debug parameter exists, use it to set API debug mode
                if (parsed[0].args.debug) og.api.common.set_debug(parsed[0].args.debug === 'true');
                // fire requested methods with params from URL
                parsed.map(function (val) {val.method(val.args);});
                // set last to the longest hash
                if (parsed.length) last = parsed[0].hash;
            };
        return routes = {
            /** @ignore */
            init: function () {
                $(window).bind('hashchange', hash_handler);
                // in addition to binding hash change events to window, also fire it onload
                $(hash_handler);
            },
            add: function (route, method) {
                // overload route parameter to accept rule objects as they are exposed in individual modules
                // i.e. {route: 'some_string', method: 'some_string'}
                if (route.route && route.method){
                    method = route.method;
                    route = route.route;
                };
                var raw = route, id = [route, method].join('|||');
                if (typeof route !== 'string' || typeof method !== 'string' || !route.length || !method.length)
                    throw new TypeError('add: route and method must both be strings and not be empty');
                if (id in added_routes)
                    throw new Error('add: route "' + route + '" routing to "' + method + '" already exists');
                added_routes[id] = null;
                route = compile(raw);
                if (!active_routes[route.method]){
                    active_routes[route.method] = [];
                    // add route to list and sort in order of length descending
                    flat_routes = flat_routes.concat(route.method).sort(function (a, b) {return b.length - a.length;});
                };
                active_routes[route.method].push({method: method, rules: route.rules, raw: raw});
            },
            hash: function (rule, params) {
                var route = rule.route, params = params || {};
                if (typeof route !== 'string' || !route.length)
                    throw new TypeError('hash: rule.route must be a string and not be empty');
                route = compile(route);
                return route.method + route.rules.scalars.map(function (scalar) {
                    if (scalar.required && typeof params[scalar.name] === 'undefined')
                        throw new TypeError('hash: params.' + scalar.name + ' is undefined');
                    return typeof params[scalar.name] === 'undefined' ? null : params[scalar.name];
                }).concat(route.rules.keyvals.map(function (keyval) {
                    if (keyval.required && typeof params[keyval.name] === 'undefined')
                        throw new TypeError('hash: params.' + keyval.name + ' is undefined');
                    return typeof params[keyval.name] === 'undefined' ? null : keyval.name + '=' + params[keyval.name];
                })).filter(function (val) {return val !== null;}).join('/');
            },
            last: function () {return last;},
            parse: function (hash) {
                if (typeof hash !== 'string' || !hash.length)
                    throw new TypeError('parse: hash must be a string and not be empty');
                var parsed = parse(hash);
                if (!parsed.length) throw new SyntaxError('parse: the hash ' + hash + ' cannot be parsed');
                return {page: parsed[0].page, args: parsed[0].args};
            }
        };
    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.util.getUrlParam',
    obj: function () {

        return function (name) {
            return og.common.util.localCache.getCache('urlParamsObj')[name];
        }

    }
});
/**
 * Takes a parameter name and checks to see if that param already exists in the url.
 * If it does, it replaces it's value, or else it adds it.
 * It then returns the full parameter part of the url as a string.
 *
 * If the value supplied is an empty string it removes the type.
 *
 * @param {string}, {object} type
 * @param {string} str
 * @returns {string} curUrlParams
 */

$.registerNewModule({
    name: 'og.common.util.safeAddParamToUrl',
    obj: function () {

        function doAdd(type, str, curUrlParams) {

            var newParam,
                regExp;

            regExp = new RegExp(type + '=[A-Za-z0-9-_]*', 'gi');

            // If an empty string was sent, remove the param
            if (str === '') {
                curUrlParams = curUrlParams.replace(regExp, '');
            } else {
                /**
                 * If the param is currently empty, jut add the new value,
                 * otherwise replace the old param value pair
                 */
                if (curUrlParams.indexOf(type + '=') === -1) {
                    curUrlParams += '&' + type + '=' + str;
                } else {
                    newParam = type + '=' + str;
                    curUrlParams = curUrlParams.replace(regExp, newParam);
                }
            }

            curUrlParams = curUrlParams.replace(/&&/, '&').replace(/&$/, '');
            return curUrlParams;

        }



        return function (type, str) {

            var urlParamsString = window.location.href.split('#')[1].toLowerCase(),
                curUrlParams = '#' + urlParamsString,
                i;

            if (arguments.length === 2) {
                if (typeof(type) !== 'string') {
                    throw ('safeAddParamToUrl: You only supplied one parameter, therefor it should be an object, you supplied a ' + type);
                }
                curUrlParams = doAdd(type, str, curUrlParams);
            }

            if (arguments.length === 1) {
                if (typeof(type) !== 'object') {
                    throw ('safeAddParamToUrl: ' + type + ' should be an object. Alternatively you can supply two strings');
                }
                for (i in type) {
                    if (type.hasOwnProperty(i)) {
                        curUrlParams = doAdd(i, type[i], curUrlParams);
                    }
                }
            }

            return curUrlParams;

        }


        
    }
});
/**
 * Build an object that stores the current url parameters.
 * Also stores the individual params in localStorage
 */

$.registerNewModule({
    name: 'og.common.util.urlParamsObj',
    obj: function () {

        return function () {

            var hash = window.location.hash.substring(1),
                urlParamsString,
                validDetailsPageParams,
                urlParamsArr,
                urlParamsArrLen,
                urlParamsObj = {},
                valuesArr = [],
                i,
                localCache = og.common.util.localCache;

            // Temporarily redirect to portfolios as we have no homepage yet!
            if (!hash) window.location.href = '/prototype/#p=portfolios';
            urlParamsString = hash.toLowerCase();
            validDetailsPageParams = og.common.details.validDetailsPageParams;
            urlParamsArr = urlParamsString.split('&');
            urlParamsArrLen = urlParamsArr.length;

            while (urlParamsArrLen--) {
                valuesArr = urlParamsArr[urlParamsArrLen].split('=');
                urlParamsObj[valuesArr[0]] = valuesArr[1];
            }

            localCache.removeCache('urlParamsObj').setCache('urlParamsObj', urlParamsObj);
            return urlParamsObj;

        };

    }
});

/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * Stores the current url and diffs it against a new url.
 * @requires og.common.util.getUrlParam
 * @returns {object} changedApplicationSections An object that lists the changed page sections
 */

$.registerNewModule({
    name: 'og.common.util.urlStateChange',
    obj: function () {

        var curUrl = {};

        return function () {

            var getUrlParam = og.common.util.getUrlParam,
                newUrl = {},
                changedApplicationSections = {},
                detailsPages = og.common.details.validDetailsPageParams,
                detailsPagesLen = detailsPages.length,
                curParam;

            /**
             * Check if the search results need to be reloaded
             */
            newUrl.p = getUrlParam('p') || undefined;
            newUrl.search = getUrlParam('search') || undefined;
            newUrl.source = getUrlParam('source') || undefined;
            changedApplicationSections.search =
                    curUrl.p !== newUrl.p ||
                    curUrl.search !== newUrl.search ||
                    curUrl.source !== newUrl.source;

            /**
             * Check if the details need to be reloaded
             */
            newUrl.detailsPage = undefined;
            newUrl.subportfolio = getUrlParam('subportfolio');
               while(detailsPagesLen--) {
                curParam = detailsPages[detailsPagesLen];
                if (curParam === getUrlParam('d')) {
                    newUrl.detailsPage = curParam;
                    break;
                }
            }
            changedApplicationSections.details =
                curUrl.detailsPage !== newUrl.detailsPage ||
                curUrl.detailsPage !== newUrl.subportfolio;

            /**
             * Store new url as current
             */
            curUrl.p = newUrl.p;
            curUrl.search = newUrl.search;
            curUrl.detailsPage = newUrl.detailsPage;

            return changedApplicationSections;

        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * Get, Set and Remove items to a local cache
 *
 */

$.registerNewModule({
    name: 'og.common.util.localCache',
    obj: function () {

        var cache = [];

        function removeCache(value) {
            if (value) {
                delete cache[value];
            }
            return this
        }

        function setCache(key, value) {
            cache[key] = value;
        }

        function getCache(key) {
            return cache[key] || false;
        }

        return {
            setCache: setCache,
            getCache: getCache,
            removeCache: removeCache
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * This function fetches HTML asynchronously from a supplied URL
 * Before it does the request it checks the local cache
 * If an asynchronous request needs to be made it then caches the content after it gets it
 * @param {Function} callBack
 * @param {String} url
 */

$.registerNewModule({
    name: 'og.common.util.getHtml',
    obj: function () {

        return function (callBack, url) {
            var html,
                localCache = og.common.util.localCache;
            if (url !== url + "") {
                throw ("getHTML: url should be supplied as a string");
            }
            url = url + "";
            html = localCache.getCache(url);
            if (html) {
                callBack(html);
            } else {
                $.ajax({
                    url: url,
                    success: function(fetchedHtml) {
                        html = fetchedHtml;
                        localCache.setCache(url, fetchedHtml);
                        callBack(html);
                    }
                })
            }
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * TODO: update this script to support all urls.
 *       At the moment only one type of data url was really considered. 
 *
 */

$.registerNewModule({
    name: 'og.common.util.getUrl',
    obj: function () {

        var liveDataRoot = this.liveDataRoot,
            htmlRoot = this.htmlRoot,
            appRoot = this.appRoot;

        return function (options) {

            var url = '',
                urlType = options.urlType,
                dataType = options.dataType,
                id = options.id;

            /**
             * Required options
             */
            if (!urlType) { // data, html, application
                throw ('og.common.util.getUrl: You need to supply a urlType');
            }
            if (!dataType) { // portfolio, securities...
                throw ('og.common.util.getUrl: You need to supply a dataType');
            }

            switch (urlType) {
                case 'data':
                    url = liveDataRoot;
                    break;
                case 'html':
                    url = htmlRoot;
                    break;
                case 'application':
                    url = appRoot;
                    break;
            }

            if (id) {
                switch (true) {
                     case dataType === 'portfolio':
                         url += 'portfolios/DbPrt::' + id;
                         break;
                     case dataType === 'position':
                         url += 'positions/DbPos::' + id;
                         break;
                     case dataType === 'security':
                         url += 'securities/DbSec::' + id;
                         break;
                }
            }

            return url;

        }

    }
});
/**
 * window.og.common.util.ui.toggleTextOnFocus
 *
 * This function will remove the default text in an input
 * field (or other element) on focus and put it back on blur
 * unless new input has been added. Attach it to an element
 * by supplying a selector matching the element.
 *
 * @param {String} CSS selector
 *
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.util.ui.toggleTextOnFocus',
    obj: function () {

        function setSelector(element) {

            var arr = [];

            $(element).focus(function () {

                var value = $(this).attr('value');

                // Store the original value
                if (!arr[element]) {
                    arr[element] = value;
                }

                /*
                 * If the current value is the same as the original
                 * then clear the field
                 */
                if (value === arr[element]) {
                    $(this).attr('value', '');
                }

            });

            $(element).focusout(function () {

                /*
                 * If the field is left empty then repopulate it #
                 * with the original value
                 */
                if ($(element).attr('value') === '') {
                    $(this).attr('value', arr[element]);
                }

            });

        }

        return {
            setSelector: setSelector
        };
    }
});
/**
 * window.og.common.util.ui.expandHeightToWindowBottom
 *
 * This function sets the height of an element to exactly the amount of px
 * needed for it to expand down to the bottom of the browser window.
 *
 * @param {object} obj This object should supply a selector and an optional offset
 * e.g. {element:'#OG-sr .og-js-results', offsetpx: -5}
 *
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.util.ui.expandHeightToWindowBottom',
    obj: function () {

        return function (obj) {

            var elementOffset,
                height,
                newHeight,
                manualOffset = obj.offsetpx || 0;

            elementOffset = $(obj.element).offset() || 0;
            height = $(window).height() + manualOffset;

            newHeight = height - elementOffset.top;

            $(obj.element).css('height', newHeight);

        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 * @colordef #fffee5; light yellow for edit mode (on hover)
 */

$.registerNewModule({
    name: 'og.common.util.ui.contentEditable',
    obj: function () {

        var editMode = false,
            editOffCss = {
                'background-color': 'transparent'
            },
            editOnCss = {
                'background-color': '#fffee5'
            },
            editHtml;

        editHtml = '<input type="text" />'
                 + '<div style="position: absolute;">'
                 + '  <button class="OG-smallButton og-buttonSecondary og-js-cancel">cancel</button>'
                 + '  <button class="OG-smallButton og-buttonPrimary og-js-update">update</button>'
                 + '</div>';

        return function () {

            $('.OG-editable').hover(
                    function () {
                        if (editMode !== true) {
                            $(this).css(editOnCss);
                        }
                    },
                    function () {
                        $(this).css(editOffCss);
                    }
            );

            $('.OG-editable').click(

                function (e) {

                    var curContent,
                        $this = $(this),
                        $editableElement = $(e.target),
                        width, fontSize, lineHeight;

                    curContent = $this.html();
                    width = $this.css('width');
                    fontSize = $this.css('font-size');
                    lineHeight = $this.css('line-height');

                    function updateField() {
                        var getUrlParam = og.common.util.getUrlParam,
                            newContent,
                            editType,
                            dataObj = {};

                        editType = $this.attr('og-edit');
                        newContent = $editableElement.find('input[type=text]').attr('value');
                        $editableElement.html(newContent);
                        editMode = false;
                        dataObj['method'] = 'PUT';
                        dataObj[editType] = newContent;

                        $.ajax({
                            type: 'POST',
                            url: og.common.util.getUrl({
                                'urlType': 'data',
                                'dataType': getUrlParam('d'),
                                'id': getUrlParam('id')
                            }),
                            data: dataObj,
                            success: function () {
                                // TODO: give user feedback... loading... updated
                                // TODO: update item on search results side
                            }
                        });

                    }

                    function cancelUpdate() {
                        $editableElement.html(curContent);
                        editMode = false;
                    }
                    
                    if (editMode === false) {
                        editMode = true;
                        $this.css(editOffCss)
                             .html(editHtml)
                             .find('input[type=text]')
                             .css({
                                  'width': width,
                                  'font-size': fontSize,
                                  'line-height': lineHeight
                              })
                             .attr('value', curContent)
                             .select();
                    }

                    $this.find('.og-js-update').click(function (e) {
                        updateField();
                        e.stopImmediatePropagation();
                    });

                    $this.find('.og-js-cancel').click(function (e) {
                        cancelUpdate();
                        e.stopImmediatePropagation();
                    });

                    $this.keydown(function (e) {
                        if (e.keyCode + "" === '13') {
                            updateField();
                        }
                        if (e.keyCode + "" === '27') {
                            cancelUpdate();
                        }
                    });

                }

            );

        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 * @param {String} selector
 * @param {Array} data
 */

$.registerNewModule({
    name: 'og.common.util.ui.renderPlot',
    obj: function () {

        return function (selector, data) {

            /**
             * Current default plot settings
             */
            var options = {
                lines: {
                    lineWidth: 1,
                    fill: 1,
                    fillColor: '#f3f6fa'
                },
                grid: {
                    borderWidth: 1,
                    color: '#ccc'
                },
                xaxis: {
                    ticks: 6,
                    mode: "time"
                },
                yaxis: {
                    ticks: 5
                }
            },
            d = {
                color: '#487287',
                shadowSize: '0',
                yaxis: 2,
                data: data
            };
            $.plot($(selector), [d], options);
        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * Draggable resize bar that sits between two elements that require resizing.
 * The resize bar should sit in one of the other elements.
 * Stores the new size of each panel in localStorage 
 *
 * @param {String} leftPane CSS selector
 * @param {String} rightPane CSS selector
 * @param {String} resizeBar CSS selector 
 *
 * TODO: Doubclick to set to center (or 40/60?)
 * 
 */

$.registerNewModule({
    name: 'og.common.util.ui.resizePanes',
    obj: function () {

        return function (left_pane, right_pane, resize_bar) {

            var $lp = $(left_pane),
                $rp = $(right_pane),
                $rb = $(resize_bar);

            $rb.mousedown(function () {
                var $doc = $(document), $body = $('body'),
                    $rb = $(this), $rb_position, rb_width, rb_height, rb_left, rb_top, rb_margin_left, rb_margin_right, 
                    set_left_mouse_pos,
                    percentage_left, percentage_right,
                    doc_width = $doc.width(), doc_height = $doc.height(),
                    $tmp_bar = $('<div class="og-js-rb"></div>'),
                    $glass_pane = $('<div class="og-js-glass-pane"></div>');

                $lp.disableSelection(); // Undocumented JQuery UI method
                $rp.disableSelection();
                rb_width = $rb.width() * 2;
                rb_height = $rb.height();
                $rb_position = $rb.position();
                rb_left = $rb_position.left;
                rb_top = $rb_position.top;
                rb_margin_left = $rb.css('margin-left');
                rb_margin_right = $rb.css('margin-right');

                /**
                 * The glassPane prevents background elements from interfering
                 * during the resize process
                 */
                $('body > div').after($glass_pane)
                               .next()
                               .css({
                                   'position': 'absolute',
                                   'background': 'transparent',
                                   'left': '0',
                                   'top': '0',
                                   'width': doc_width + 'px',
                                   'height': doc_height + 'px'
                               });

                $body.mousemove(move_panes);

                function move_panes(e) {
                    var one_percent_in_px = doc_width / 100,
                        page_x = e.pageX;
                    percentage_left = page_x / one_percent_in_px;
                    percentage_right = 100 - percentage_left;
                    $lp.width(percentage_left + '%');
                    $rp.width(percentage_right + '%');
                    $tmp_bar.css('left', page_x + 'px');
                }

                /**
                 * Temporary resizeBar that sits over the real one
                 */
                $glass_pane.after($tmp_bar)
                          .next()
                          .css({
                                  'position': 'absolute',
                                  'background-color': '#ccc',
                                  'margin-left': rb_margin_left,
                                  'margin-right': rb_margin_right,
                                  'width': rb_width,
                                  'height': rb_height,
                                  'left': rb_left,
                                  'top': rb_top
                          });

                $glass_pane.one('mouseup', function() {
                    localStorage['resize_panes_' + left_pane] = percentage_left + '%';
                    localStorage['resize_panes_' + right_pane] = percentage_right + '%';
                    $body.unbind('mousemove', move_panes);
                    $lp.enableSelection(); // Undocumented JQuery UI method
                    $rp.enableSelection();
                    $tmp_bar.remove();
                    $glass_pane.remove();
                });

            });

        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.util.ui.tooltip',
    obj: function () {

        var classes = {
            'data_attributes': ['data-tooltip-level', 'data-tooltip-location'],
            'data-tooltip-level': ['danger', 'warning'],
            'data-tooltip-location': ['top', 'bottom']
        };

        return function () {

            /* Create a new tooltip if none already exists */
            if (!$('.OG-tooltip').length) $('body').append('<span class="OG-tooltip OG-shadow"></span>');
            var $tooltip = $('.OG-tooltip');

            /* Populate and display on hover */
            $('[data-tooltip]').hover(function (e) {
                var $offset = $(this).offset(),
                    $e_target = $(e.currentTarget),
                    tip = $e_target.attr('data-tooltip');
                /**
                 * Set up CSS classes for tooltip Style and Location
                 * Style: warning, danger...
                 * Location: top, bottom... 
                 */
                classes['data_attributes'].map(function (val) {
                    $e_target.attr(val)
                            ? $tooltip.addClass('og-' + $e_target.attr(val))
                            : classes[val].map(function (v) {$tooltip.removeClass('og-' + v)});
                });
                /**
                 * Position and display
                 * TODO: currently only supports location: top
                 */
                $tooltip.html(tip).css({
                        'left': $offset.left - (
                                    $tooltip.width() / 2
                                  - $(e.currentTarget).width() / 3
                                ) + 'px',
                        'top': $offset.top - 19 + 'px'
                }).show();
            });

            $('[data-tooltip]').mouseleave(function () {
                $tooltip.empty().hide();
            });

            return this

        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.slickgrid.formatters.timeseries',
    obj: function () {

        return function(row, cell, value, columnDef, dataContext) {

            if (value == null || value === '') return '<span class=og-no-content>' + '-' + '</span>';

            var date_info = /(?::[SE]:[0-9]{4}-[0-9]{2}-[0-9]{2}){1,2}/.exec(value) + '',
                end_date = /E:[0-9]{4}-[0-9]{2}-[0-9]{2}/.exec(value),
                start_date = /:S:[0-9]{4}-[0-9]{2}-[0-9]{2}/.exec(value),
                non_date_info = value.replace(date_info, ''),
                css_class,
                return_data;

            if (start_date) {
                if (end_date) {
                    css_class = 'OG-icon og-clock-stopped';
                    date_info = date_info.replace(':S:', 'Start: ').replace(':E:', ' - End: ');
                } else {
                    css_class = 'OG-icon og-clock-active';
                    date_info = date_info.replace(':S:', 'Start: ');
                }
            }

            if (end_date) {
                css_class = 'OG-icon og-clock-stopped';
                date_info = date_info.replace(':E:', 'Start: ? - End: ');
            }

            if (css_class) {
                return_data = '<span class="' + css_class + '" title="' + date_info + '">'
                            + '  <span>' + non_date_info + '</span>'
                            + '</span>';
            }

            return return_data || '<span class="og-no-clock"><span>' + value + '</span></span>';
        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.slickgrid.manager',
    obj: function () {

        return function (obj) {
            var DEAFULT_PAGESIZE = 20, data = {length: 0},
                on_data_loading = new Slick.Event(), on_data_loaded = new Slick.Event(),
                filter = obj.filter,
                observation_time = obj.observation_time,
                from_num, to_num, timer;

            // Add in message box
            $('#OG-sr').append('<div class="og-js-loading-search">loading...</div>')
                    .find('.og-js-loading-search')
                    .css({
                            '-moz-border-radius-topright': '2px',
                            '-webkit-border-radius-top-right': '2px',
                            'border-top-right-radius': '2px',
                            'position': 'absolute',
                            'background-color': '#313b44',
                            'bottom': '0',
                            'color': '#fff',
                            'padding': '2px 4px 0 2px',
                            'left': '0'
                       });

            /**
             * Fetches the data and populates the data object required for SlickGrid
             * @param {Number} from the starting row number
             * @param {Number} to the ending row number
             */
            function ensure_data(from, to) {
                var from_page = Math.floor(from / DEAFULT_PAGESIZE), to_page = Math.floor(to / DEAFULT_PAGESIZE),
                    request_page_size, request_page_number,
                    data_already_cached = false,
                    rest_obj;

                // Setup required page info
                while (data[from_page * DEAFULT_PAGESIZE] !== undefined && from_page < to_page) from_page++;
                while (data[to_page * DEAFULT_PAGESIZE] !== undefined && from_page < to_page) to_page--;
                request_page_size = (((to_page - from_page) * DEAFULT_PAGESIZE) + DEAFULT_PAGESIZE);
                request_page_number = Math.floor(from_page / (request_page_size / DEAFULT_PAGESIZE));

                // Check to see if the data has already been loaded
                from_num = request_page_number * request_page_size;
                to_num = from_num + request_page_size;
                for (var i = from_num; i < to_num; i++) data_already_cached = data[i] !== undefined || false;

                if (!data_already_cached) {
                    // Load new data
                    rest_obj = {
                        handler: function(r) {
                            var from, to, json_header;
                            if (r.error) {
                                clearTimeout(timer);
                                $('#OG-sr .og-js-loading-search').html(
                                        'oops, something bad happened (' + r.message + ')'
                                        ).show();
                                return;
                            }
                            json_header = r.data.header;
                            from = from_page * DEAFULT_PAGESIZE;
                            to = from + json_header.count;
                            data.length = parseInt(json_header.total);
                            // Create Data Object for slickgrid
                            $.each(r.data.data, function (i, row) {
                                var field_values = row.split('|'),
                                    field_names = json_header.dataFields;
                                data[from + i] = {};
                                $.each(field_names, function (k, field_name) {
                                    (function () { // this is only for timeseries
                                        var id_type, id_name,
                                            id = field_values[k].split('&'),
                                            q = id.length;
                                        if (id) {
                                            while (q--) {
                                                id_type = id[q].split('::')[0];
                                                id_name = id[q].split('::')[1];
                                                data[from + i][id_type] = id_name;
                                            }
                                        }
                                    })();
                                    data[from + i][field_name] = field_values[k];
                                });
                                if (filter) data[from + i].type = filter;
                                data[from + i].index = from + i;
                            });
                            on_data_loaded.notify({from: from,to: to});
                            $('#OG-sr .og-js-loading-search').hide();
                            clearTimeout(timer);
                        },
                        loading: function () {
                            $('#OG-sr .og-js-loading-search').html('loading...').show();
                            clearTimeout(timer);
                            timer = setTimeout(function () {
                                $('#OG-sr .og-js-loading-search').html('still loading...');
                            }, 3000);
                        },
                        page_size: request_page_size,
                        page: request_page_number
                    };
                    if (filter) {$.extend(rest_obj, rest_obj, {type: filter.toUpperCase()})}
                    if (observation_time) {$.extend(rest_obj, rest_obj, {type: observation_time})}
                    og.api.rest[obj.page_type].get(rest_obj);
                }

            }

            return {
                "data": data,                                  // properties
                "on_data_loading": on_data_loading,            // events
                "on_data_loaded": on_data_loaded,              // events
                "ensure_data": ensure_data                     // methods
            };
        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * This function does the initial page resizing and also resizes
 * elements when the user changes the browser width or height
 */

$.registerNewModule({
    name: 'og.common.layout.resize',
    obj: function () {

        var resize,
            expandHeightToWindowBottom = og.common.util.ui.expandHeightToWindowBottom;

        resize = function () {
            expandHeightToWindowBottom({element: '#OG-sr .og-js-results-slick', offsetpx: -3});
            expandHeightToWindowBottom({element: '.OG-resizeBar', offsetpx: -3});
            expandHeightToWindowBottom({element: '#OG-details .og-main', offsetpx: -3});
            expandHeightToWindowBottom({element: '#OG-analytics .og-main', offsetpx: -3});
        };

        return {
            init: function () {
                $(window).resize(function() {
                    resize();
                });
                og.common.util.ui.resizePanes('#OG-sr', '#OG-details', '.OG-resizeBar');
                resize();
            }
        };

    }
});
(function ($) {

    $('.og-search input').focus(function() {
      $(this).removeClass('background');
    });

    $('.og-search input').focusout(function() {
      $(this).addClass('background');
    });

})(jQuery);
/**
 * Dropdown menus for masthead
 *
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.masthead.menu',
    obj: function () {

        var keyboard;

        /*
         * TODO: setup keyboard shortcuts
         */
        keyboard = {
            'A': 'OG-JS-analytics',
            'P': 'OG-JS-portfolio',
            'S': 'OG-JS-securities',
            'E': 'OG-JS-exchanges',
            'T': 'OG-JS-timeSeries',
            'H': 'OG-JS-holidays',
            'R': 'OG-JS-regions',
            'C': 'OG-JS-configurations',
            'N': 'OG-JS-new',
            'F': 'OG-JS-feedback'
        };

        return {
            init: function () {
                $('.og-icon-header-dropdown').parent().hover(function () {
                    $(this).addClass('open').find('.og-dd-menu').show();
                });

                /*
                 * TODO: Add in delay
                 */
                $('.og-icon-header-dropdown').parent().mouseleave(function () {
                    $(this).removeClass('open').find('.og-dd-menu').hide();
                });

            },
            set_tab: function (name) {
                $('#OG-masthead li').removeClass('og-active');
                $('#OG-masthead .og-' + name).addClass('og-active');
            }
        };

    }
});
$.registerNewModule({
    name: 'og.common.details.core',
    obj: function () {

        /**
         * Generate HTML using a html template and json data.
         * This is the/a call back function for fetchAndMarryContent
         *
         * example:
         * Takes json like {"data": [{ISSUER_NAME_LABEL: "some name"}]},
         * and html like this <div>[ISSUER_NAME_LABEL]</div>,
         * does a replace to leave you with <div>some name</div>
         * Also Supports nested objects.
         *
         * @param json
         * @param html
         * @returns {String}
         *
         * TODO: replace with jquery tpl
         */
        return function(json, html) {

            var j, str;

            localStorage.html_temp = html; // a temp version is needed for recursion
            j = json.templateData || json;

            for (var i in j) {
                arguments.length === 3 ? str = arguments[2] : str = "";
                if (j.hasOwnProperty(i)) {
                    str === "" ? str = i : str = str + '.' + i;
                    if (typeof(j[i]) === 'object') {
                        arguments.callee(j[i], localStorage.html_temp, str);
                    } else {
                        localStorage.html_temp = localStorage.html_temp.replace('[' + str + ']', j[i] || '&nbsp;');
                    }
                }
            }
            return localStorage.html_temp.replace(/\[[a-zA-Z\.]+\]/g, '&nbsp;'); // Clean up any unused template code
        }
        
    }
});
/**
 * Build an object that stores all valid details page url parameters
 */
$.registerNewModule({
    name: 'og.common.details.validDetailsPageParams',
    obj: function () {

        return [
            "portfolio",
            "position",
            "security",
            "exchange",
            "timeseries",
            "holiday",
            "regions",
            "configuration",
            "batches"
        ]

    }
});



/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.timeseriesFunctions',
    obj: function () {

        /**
         * TODO: create generic date transformer
         * @param {String} selector
         * @param {json} data
         */
        function renderTimeSeriesTable(selector, data) {
            var html,
                i,
                iLen = data.data.length,
                date;
            html = '<table class="OG-table-style-01">'
                  +'  <thead>'
                  +'    <tr>'
                  +'      <td>' + data.fieldLabels[0] + '</td>'
                  +'      <td>' + data.fieldLabels[1] + '</td>'
                  +'    </tr>'
                  +'  </thead>'
                  +'  <tbody>';
            for (i = 0; i < iLen; i++) {

            date = new Date(data.data[i][0])

            html += '   <tr>'
                 +  '     <td>' + date.getDay() + '/' + date.getMonth() + '/' + date.getFullYear() + '</td>'
                 +  '     <td>' + data.data[i][1] + '</td>'
                 +  '   </tr>';
            }
            html +='  </tbody>'
                 + '</table>';

            $(selector).html(html);
        }

        function renderTimeSeriesIdentifiers(selector, json) {
            var html = "",
                i, tmpObj,
                len = json.length,
                date, tmpDate;
            for (i = 0; i < len; i++) {
                tmpObj = json[i];
                tmpDate = tmpObj.date;

                tmpDate.start
                    ? tmpDate.end
                        ? date = "(" + tmpDate.start + " - " + tmpDate.end + ")"     // Start and End Date true
                    : date = "(" + tmpDate.start + ")"                               // Start Date true
                : tmpDate.end
                    ? date = "( ? - " + tmpDate.end + ")"                            // End Date true
                : date = '';                                                         // Start and End Date !true

                html += "<div>"
                     +  "  <div>" + tmpObj.scheme.replace('_', ' ') + ":</div>"
                     +  "  <div><strong>" + tmpObj.value + "</strong><span>" + date + "</span></div>"
                     +  "</div>";
            }
            $(selector).html(html);
        }

        return {
            renderTimeSeriesTable: renderTimeSeriesTable,
            renderTimeSeriesIdentifiers: renderTimeSeriesIdentifiers
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.portfolioFunctions',
    obj: function () {

        var appRoot = this.appRoot;

        function renderPortfolioTableRows(selector, json) {
            if (!json.portfolios[0]) {
                $(selector).html('<tr><td>No Portfolios</td></tr>');
                return;
            }
            $(selector).html(json.portfolios.reduce(function (arr, val) {
                return (function () {
                    var url = '#' + og.common.routes.hash(
                        og.views.portfolios.rules['load_portfolios'], {
                        name: 'portfolios', id: json.templateData.id, node: val.id
                    });
                    return arr + "<tr><td><a href=" + url + ">" + val.name + "</a></td></tr>";
                })();
            }, ''));
        }

        function renderPositionTableRows(selector, json) {
            if (!json.positions[0]) {
                $(selector).html('<tr><td colspan="2">No Positions</td></tr>');
                return;
            }
            $(selector).html(json.positions.reduce(function (arr, val) {
                return (function () {
                    var url = '#' + og.common.routes.hash(
                        og.views.positions.rules['load_positions'], {
                        name: 'positions', id: val.id
                    });
                    return arr + '<tr>'
                               + '<td><a href=' + url + '>' + val.name + '</a></td>'
                               + '<td>' + val.quantity + '</td>'
                               + '</tr>';
                })();
            }, ''));
        }

        return {
            renderPortfolioTableRows: renderPortfolioTableRows,
            renderPositionTableRows: renderPositionTableRows
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.positionFunctions',
    obj: function () {

        var appRoot = this.appRoot;

        function render_main(selector, json) {
            var html  = '<td class="og-security"><a href=' + appRoot + '#/securities/' + json.security.uniqueId + '/type=' + json.security.security_type + '>';
                html  += json.security.name
                      + '</a></td>'
                      + '<td>' + json.security.security_type + '</td>'
                      + '<td><strong class="og-quantity OG-editable" og-edit="quantity">' + json.templateData.QUANTITY + '</strong></td>';
            $(selector).html(html);
        }

        
        function render_identifiers(selector, json) {
            $(selector).html(json.reduce(function(acc, val) {
                return acc + val.scheme + ': ' + val.value + '<br />';
            }, ''));
        }
        
        function render_trade_table_rows(selector, json) {
            if (!json[0]) {
                $(selector).html('<tr><td colspan="4">No Trades</td></tr>');
                return;
            }
            var html = "",
                i, tmpObj,
                len = json.length;
            for (i = 0; i < len; i++) {
                tmpObj = json[i];
                html += "<tr>"
                     +  "<td>" + tmpObj.id + "</td>"
                     +  "  <td>" + tmpObj.quantity + "</td>"
                     +  "  <td>" + tmpObj.counterParty + "</td>"
                     +  "  <td>" + tmpObj.date + "</td>"
                     +  "</tr>";
            }
            $(selector).html(html);
        }

        return {
            render_main: render_main,
            render_identifiers: render_identifiers,
            render_trade_table_rows: render_trade_table_rows
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.securityFunctions',
    obj: function () {

        function renderSecurityIdentifiers(selector, json) {
            var html = "", i;
            for (i in json) {
                if (json.hasOwnProperty(i)) {
                    html += "<div>"
                         +  "  <strong>" + json[i] + "</strong>"
                         +  "</div>";
                }
            }
            $(selector).html(html);

        }

        return {
            renderSecurityIdentifiers: renderSecurityIdentifiers
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.region_functions',
    obj: function () {

        var appRoot = this.appRoot;

        function render_keys(selector, json) {
            var html, lbl, data, i;
            html = lbl = data = "";
            for (i in json) lbl +=  '<div>' + i + '</div>';
            for (i in json) data += '<div><strong class="OG-editable" og-edit="name">' + json[i] + '</strong></div>';
            html += '<div class="og-lbl">' + lbl + '</div>';
            html += '<div>' + data + '</div>';
            $(selector).html(html);
        }

        function render_regions(selector, json) {
            $(selector).html(json.reduce(function (arr, val) {
                return (function () {
                    var url = '#' + og.common.routes.hash(
                        og.views.regions.rules['load_regions'], {
                        name: 'regions', id: val.id
                    });
                    return arr + '<tr><td><a href=' + url + '>' + val.name + '</a></td></tr>';
                })();
            }, '') || '<tr><td>No regions</td></tr>');
        }

        return {
            render_keys: render_keys,
            render_regions: render_regions
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.exchange_functions',
    obj: function () {

        var appRoot = this.appRoot;

        function render_info(selector, json) {

            var html, lbl, data, i;
            html = lbl = data = '';

            for (i in json.regionKey) lbl +=  '<div>' + i + '</div>';
            for (i in json.identifiers) lbl +=  '<div>' + i + '</div>';
            for (i in json.regionKey) data += '<div><strong class="OG-editable" og-edit="name">' + json.regionKey[i] + '</strong></div>';
            for (i in json.identifiers) data += '<div><strong class="OG-editable" og-edit="name">' + json.identifiers[i] + '</strong></div>';
            html += '<div class="og-lbl">' + lbl + '</div>';
            html += '<div>' + data + '</div>';
            $(selector).html(html);
        }

        return {
            render_info: render_info
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.registerNewModule({
    name: 'og.common.details.batch_functions',
    obj: function () {

        var appRoot = this.appRoot;

        function results(selector, json) {
            if (!json[0]) {
                $(selector).html('<tr><td colspan="5">No results</td></tr>');
                return
            }
            var html = '', i, data, len = json.length;
            for (i = 0; i < len; i++) {
                data = json[i].split('|');
                html += '<tr>'
                     + '  <td>' + data[0] + '</td>'
                     + '  <td>' + data[1] + '</td>'
                     + '  <td>' + data[2] + '</td>'
                     + '  <td>' + data[3] + '</td>'
                     + '  <td>' + data[4] + '</td>'
                     + '</tr>';
            }
            $(selector).html(html);
        }

        function errors(selector, json) {
            if (!json[0]) {
                $(selector).html('<tr><td colspan="7">No errors</td></tr>');
                return
            }
            var html = '', i, data, len = json.length;
            for (i = 0; i < len; i++) {
                data = json[i].split('|');
                html += '<tr>'
                     + '  <td>' + data[0] + '</td>'
                     + '  <td>' + data[1] + '</td>'
                     + '  <td>' + data[2] + '</td>'
                     + '  <td>' + data[3] + '</td>'
                     + '  <td>' + data[4] + '</td>'
                     + '  <td>' + data[5] + '</td>'
                     + '  <td>' + data[6] + '</td>'
                     + '</tr>';
            }
        }

        return {
            results: results,
            errors: errors
        };

    }
});
/**
 * Add/remove from favorites 
 */
$.registerNewModule({
    name: 'og.common.details.favorites',
    obj: function () {

        return function() {
            $('.OG-icon.og-favorites').click(function () {
                $(this).toggleClass('og-favorites-active');                
            })
        }

    }
});



/**
 * Delete old next and prev buttons and add in custom ones
 *
 * TODO: make select box work
 *
 */
$.registerNewModule({
    name: 'og.common.details.calendarUiChanges',
    obj: function () {

        function buildSelector(dates) {
            var html = '',
                i;
            
            for (i in dates) {
                html += '<option value="' + i + '">' + i + '</option>'
            }

            return '<select>' + html + '</select>';

        }

        function updatePagination() {

            var prev,
                next,
                curYear;

            curYear = parseFloat($('.ui-datepicker-year').html());
            $('.OG-holiday .og-js-prev').html(curYear - 1);
            $('.OG-holiday .og-js-next').html(curYear + 1);
            $('.OG-holiday .og-js-pagination select').val(curYear);

        }

        return function (dates) {

            var curYear = $('.ui-datepicker-year').html(),
                prev = $('.ui-datepicker-prev').html(+curYear - 1).addClass('og-js-prev'),
                next = $('.ui-datepicker-next').html(+curYear + 1).addClass('og-js-next'),

                onclick = $('.ui-datepicker-next').attr('onclick'),
                val = $(this).val(),
                diff, months;
                if (val < curYear) {
                    diff = curYear - val;
                    months = diff * 12;
                    //DP_jQuery_1296053815704.datepicker._adjustDate('#dp1296053815705', -months, 'M');
                } else {
                    diff = val - curYear;
                    months = diff * 12;
                }


            $('.OG-holiday .og-js-pagination').css('display', 'inline')
                                              .append(prev)
                                              .append(buildSelector(dates))
                                              .append(next)
                                              .find('select')
                                              .change(onclick)
                                              .css('display', 'inline');

            $('.OG-holiday .og-js-pagination select').val(curYear);
            
            $('.og-js-pagination a').click(function () {
                updatePagination();
            });

        }

    }
});



/**
 * TODO: needs implementing as a generic function
 */

$.registerNewModule({
    name: 'og.common.search_results.minimize',
    obj: function () {
        return function (min, max) {

                $(min).mousedown(function () {
                    $('#OG-sr').hide();
                    $('#OG-details').width('100%');
                    $('#OG-details .og-icon-maximize').show();
                    $('#OG-details .og-icon-minimize').hide();
                });

                $(max).mousedown(function () {
                    var details_width = localStorage['resize_panes_#OG-details'] || '60%',
                        sr_width = localStorage['resize_panes_#OG-sr'] || '40%';
                    $('#OG-sr').show().width(sr_width);
                    $('#OG-details').width(details_width);
                    $('#OG-details .og-icon-maximize').hide();
                    $('#OG-details .og-icon-minimize').show();
                });

                return this

            }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * @requires og.common.util.ui.toggleTextOnFocus.setSelector
 * @requires og.common.search_results.minimize
 * @requires og.common.util.getUrlParam
 * @requires og.common.util.safeAddParamToUrl
 * @requires og.common.util.urlParamsObj
 */

$.registerNewModule({
    name: 'og.common.search_results.filters',
    obj: function () {

        var safeAddParamToUrl = og.common.util.safeAddParamToUrl,
            getUrlParam = og.common.util.getUrlParam;

        function initSearchMinimizer() {
            og.common.search_results.minimize('.OG-JS-minimizeSearchResults', '.OG-JS-maximizeSearchResults');
        }

        function initFilters() {

            var urlParamsObj = og.common.util.urlParamsObj(),
                curProp;

            /**
             * Set current
             */
            for (var propName in urlParamsObj) {
                curProp = getUrlParam(propName);
                $('.og-js-filters select[og-prop=' + propName + ']').val(curProp);
            }

            /**
             * update url
             */
            $('.og-js-filters select').change(function () {

                var tld = window.location.href.split('#')[0].toLowerCase(),
                    prop,
                    value,
                    $this = $(this);

                value = $this.val();
                prop = $this.attr('og-prop') + "";
                if (value === 'all') value = '';
                value = safeAddParamToUrl(prop, value);
                window.location.href = tld + value;

            });

        }

        return function (pageName) {

            var filtersElement = $('.og-js-filters');
            switch (true) {

                case pageName === "timeseries":
                    filtersElement.html(
                            '<div class="OG-icon og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        +   '<select class="og-source" og-prop="source">'
                        +   '  <option selected="selected">all</option>'
                        +   '  <option value="bloomberg">Bloomberg</option>'
                        +   '  <option value="reuters">Reuters</option>'
                        +   '</select>'
                        +   '<select class="og-identifierType" og-prop="search">'
                        +   '  <option selected="selected">all</option>'
                        +   '  <option value="bloombergticker">Bloomberg Ticker</option>'
                        +   '  <option value="bloombergbuid">Bloomberg BUID</option>'
                        +   '  <option value="cusip">CUSIP</option>'
                        +   '  <option value="isin">ISIN</option>'
                        +   '  <option value="sedol1">SEDOL1</option>'
                        +   '</select>'
                        +   '<input type="text" class="og-identifierName" value="filter by identifiers" />'
                     );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-identifierName');
                    break;

                case pageName === "securities":
                    filtersElement.html(
                          '<div class="OG-icon og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        + '<select class="og-type" og-prop="search">'
                        +   '<option>all</option>'
                        +   '<option value="bond">Bond</option>'
                        +   '<option value="cash">Cash</option>'
                        +   '<option value="equity">Equity</option>'
                        +   '<option value="fra">FRA</option>'
                        +   '<option value="future">Future</option>'
                        +   '<option value="equity_option">Option</option>'
                        +   '<option value="swap">Swap</option>'
                        + '</select>'
                        + '<input class="og-name" type="text"  value="filter by name" />'
//                        + '<select class="og-quantity og-js-quantity" og-prop="sort">'
//                        +   '<option>sort</option>'
//                        +   '<option value="hightolow">High to low</option>'
//                        +   '<option value="lowtohigh">Low to high</option>'
//                        + '</select>'
//                        + '<input class="og-exchange" type="text" value="all" />'
//                        + '<input class="og-currency" type="text" value="all" />'
                     );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-name');
//                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-currency');
//                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-exchange');
                    break;

                case pageName === "portfolios":
                    filtersElement.html(
                          '<div class="OG-icon og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        + '<input type="text" class="og-name" value="filter by name" />'
                    );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-name');
                    break;

                /**
                 * TODO: positions need range filters
                 */
                case pageName === "positions":
                    filtersElement.html(
                          '<div class="OG-icon og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        + '<input type="text" class="og-name" value="filter by name" />'
                        + '<select class="og-quantity" og-prop="quantity">'
                        + '  <option value="all" selected="selected">quantity</option>'
                        + '</select>'
                        + '<select class="og-trades" og-prop="trades">'
                        + '  <option value="all" selected="selected">trades</option>'
                        + '</select>'
                    );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-name');
                    break;

                case pageName === "exchanges":
                    filtersElement.html(
                          '<div class="OG-icon og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        + '<input type="text" class="og-name" value="filter by name" />'
                     );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-name');
                    break;

                case pageName === "holidays":
                    filtersElement.html(
                          '<div class="OG-icon og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        + '<select class="og-type" og-prop="search">'
                        + '  <option value="all" selected="selected">all</option>'
                        + '  <option value="bank">Bank</option>'
                        + '  <option value="currency">Currency</option>'
                        + '  <option value="settlement">Settlement</option>'
                        + '  <option value="trading">Trading</option>'
                        + '</select>'
                        + '<input class="og-code" type="text"  value="all" />'
//                        + '<input class="og-name" type="text"  value="filter by name" />'
                     );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-name');
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-code');
                    break;

                case pageName === "regions":
                    filtersElement.html(
                          '<div class="og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                        + '<input type="text" class="og-name" value="filter by name" />'
                     );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-name');
                    break;

                case pageName === "batches":
                    filtersElement.html(
                          '<div class="og-minimize OG-JS-minimizeSearchResults"><span class="OG-icon og-arrow-1"></span></div>'
                          + '<input type="text" class="og-ob-date" value="date" />'
                          + '<input type="text" class="og-ob-time" value="time" />'
                     );
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-ob-date');
                    og.common.util.ui.toggleTextOnFocus.setSelector('.og-filters .og-ob-time');
                    break;

                default:
                    throw ("populateFilters: The PageName supplied didn't match any of the page types. Supplied page name: " + pageName);

            }

            initSearchMinimizer();
            initFilters();

        }

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * This function checks that the url parameters that are going to be used to call
 * data from the JSON files contain valid values
 */

$.registerNewModule({
    name: 'og.common.search_results.validateUrlParams',
    obj: function () {

        return function () {

            var validSearchParams = {
                'p': {
                    'portfolios': {},
                    'positions': {},
                    'securities': {
                        'type': [
                            'all',
                            'bond',
                            'cash',
                            'equity',
                            'fra',
                            'future',
                            'option',
                            'swap'
                        ],
                        'sort': [
                            'sort',
                            'hightolow',
                            'lowtohigh'
                        ]
                    },
                    'timeseries': {
                        'source': [
                            'all',
                            'bloomberg',
                            'reuters'
                        ],
                        'type': [
                            'all',
                            'bloombergticker',
                            'bloombergbuid',
                            'cusip',
                            'isin',
                            'sedol1'
                        ]
                    },
                    'exchanges': {},
                    'holidays': {
                        'type': [
                            'all',
                            'currency',
                            'settlement',
                            'bank',
                            'trading'
                        ]
                    },
                    'regions': {},
                    'configurations': {}
                }
            };


            /**
             * Compare the valid parameters against the current url params
             */
            (function () {
                var p = og.common.util.getUrlParam('p'),
                    urlParamsObj = og.common.util.urlParamsObj();

                if (p) {
                    if (validSearchParams.p.hasOwnProperty(p)) {

                        (function (validSearchParams) {
                            var propLen,
                                isValid,
                                prop;
                            for (prop in urlParamsObj) {
                                if (validSearchParams.hasOwnProperty(prop)) {
                                    propLen = validSearchParams[prop].length;
                                    isValid = false;
                                    while (propLen--) {
                                        if (validSearchParams[prop][propLen] === urlParamsObj[prop]) {
                                            isValid = true;
                                        }
                                    }
                                    if (isValid === false) {
                                        throw ('validateUrlParams: The value (' + urlParamsObj[prop] + ') you supplied for the url parameter ' + prop + ' is invalid');
                                    }
                                }
                            }
                        }(validSearchParams.p[p]));

                    }
                }
            }());

        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */
$.registerNewModule({
    name: 'og.common.search_results.core',
    obj: function () {

        return function (obj) {

            var slick_manager = og.common.slickgrid.manager(
                $.extend({}, obj.url, {'page_type': obj.page_type})),
                options = $.extend({}, obj.options, {
                    editable: false,
                    enableAddRow: false,
                    enableCellNavigation: false
                }),
                timer,
                grid = new Slick.Grid(obj.selector, slick_manager.data, obj.columns, options);
                //  grid.updateColumnHeader('name', "Name <em style='position:absolute; right:1px; bottom: 1px'>Add button here</em>", 'tests');

            // Handle click
            $('.og-js-results-slick').undelegate().delegate('[row]', 'click', function (e) {
                delete obj.url['new'];
                window.location.hash = og.common.routes.hash(
                        og.views[obj.page_type].rules['load_' + obj.page_type],
                        $.extend({}, obj.url, {
                            id: slick_manager.data[$(e.currentTarget).attr('row')].id,
                            type: slick_manager.data[$(e.currentTarget).attr('row')].type,
                            filter: slick_manager.data[$(e.currentTarget).attr('row')].filter,
                            observation_time: slick_manager.data[$(e.currentTarget).attr('row')].observationTime
                        })
                );
            });

            grid.onViewportChanged.subscribe(function () {
                clearTimeout(timer);
                timer = setTimeout(function () {
                    var vp = grid.getViewport();
                    slick_manager.ensure_data(vp.top, vp.bottom);
                }, 150);
            });

            // Prepare gird for new data
            slick_manager.on_data_loaded.subscribe(function (e, args) {
                for (var i = args.from; i <= args.to; i++) grid.invalidateRow(i);
                grid.updateRowCount();
                grid.render();
            });

            // load the first page
            grid.onViewportChanged.notify();
        };

    }
});
/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 *
 * @require og.common.util.urlStateChange
 * @require og.common.details.core
 * @require og.common.search_results.core
 */

$.registerNewModule({
    name: 'og.common.init',
    obj: function () {
        var load = function () {
            og.common.search_results.minimize('.OG-JS-minimize-search-results', '.OG-JS-maximize-search-results');
            og.common.util.ui.tooltip();
        };
        return {
            init: function () {
                load();
                $(window).bind('hashchange', load);
            }
        };
    }
});
/**
 * view for portfolios section
 */
$.registerNewModule({
    name: 'og.views.portfolios',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'portfolios',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'portfolios',
                'columns': [{id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'}]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_portfolios: {route: '/' + page_name + '/:id/:node?/new:?', method: module.name + '.load_' + page_name}
        };
        function hook_up_buttons(args) {
            // "New"
            $('#OG-details .og-js-new').unbind().bind('mousedown', function () {
                var new_name = prompt('New Portfolio Name');
                if (new_name) {
                    api[page_name].put({
                        handler: function (r) {
                            window.location.hash = og.common.routes.hash(
                                    og.views[page_name].rules['load_' + page_name], {id: r.meta.id, 'new': true}
                            )
                        },
                        name: new_name
                    });
                }
            });
            // "Delete"
            $('#OG-details .og-js-delete').unbind().bind('mousedown', function () {
                if (confirm('Delete Portfolio "' + $('[og-edit=name]').html()) + '"') {
                    api[page_name].del({
                        handler: function () {
                            window.location.hash = og.common.routes.hash(og.views[page_name].rules['load'], {})
                        }, id: args.id
                    });
                }
            });
        }
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                hook_up_buttons(args);
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_portfolios: function (args) {
                // Load search if changed
                if (routes.last()) { // If this isn't first load
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') { // If the page type has changed
                        masthead.menu.set_tab(page_name);
                        hook_up_buttons(args);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                    else hook_up_buttons(args); // If the page type is the same
                } else { // If first load
                    masthead.menu.set_tab(page_name);
                    hook_up_buttons(args);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                if (args['new']) search.core($.extend(search_options, search_options, {url: args}));
                // Load details
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.portfolioFunctions, ui = og.common.util.ui;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(json, html));
                            f.renderPortfolioTableRows(".OG-portfolio .og-js-portfolios", json);
                            f.renderPositionTableRows(".OG-portfolio .og-js-positions", json);
                            ui.toggleTextOnFocus.setSelector('.OG-portfolio .og-js-createPortfolio');
                            ui.toggleTextOnFocus.setSelector('.OG-portfolio .og-js-addPosition');
                            ui.contentEditable();
                            og.common.details.favorites();
                        }});
                    },
                    id: args.id,
                    node: args.node,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for positions section
 */
$.registerNewModule({
    name: 'og.views.positions',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'positions',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'positions',
                'columns': [
                    {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'},
                    {id: 'quantity', name: 'Quantity', field: 'quantity', width: 60},
                    {id: 'trades', name: 'Trades', field: 'trades', width: 50}
                ]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_positions: {route: '/' + page_name + '/:id/:node?', method: module.name + '.load_' + page_name}
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_positions: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.positionFunctions, ui = og.common.util.ui;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            f.render_main(".OG-position .og-js-main", json);
                            f.render_identifiers(".OG-position .og-js-identifiers", json.securities);
                            f.render_trade_table_rows(".OG-position .og-js-trades",json.trades);
                            ui.contentEditable();
                            og.common.details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for securities section
 */
$.registerNewModule({
    name: 'og.views.securities',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'securities',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'securities',
                'columns': [
                    {id: 'type', name: 'Type', field: 'type', width: 80},
                    {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'}
                ]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:', method: module.name + '.load_filter'},
            load_securities: {
                route: '/' + page_name + '/:id/type:/filter:?/:node?',
                method: module.name + '.load_' + page_name
            }
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_filter: function (args) {
                masthead.menu.set_tab(page_name);
                search.core($.extend(search_options, search_options, {url: args}));
            },
            load_securities: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.securityFunctions, ui = og.common.util.ui;
                        og.api['static']({module: 'og.views.' + page_name + '.' + args.type, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            f.renderSecurityIdentifiers(".OG-security .og-js-identifiers", json.identifiers);
                            ui.contentEditable();
                            og.common.details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for securities section
 */
$.registerNewModule({
    name: 'og.views.exchanges',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'exchanges',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'exchanges',
                'columns': [{id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'}]
            };
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_exchanges: {route: '/' + page_name + '/:id', method: module.name + '.load_' + page_name}
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_exchanges: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.exchange_functions, ui = og.common.util.ui;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            f.render_info(".OG-exchange .og-js-info", json);
                            ui.contentEditable();
                            og.common.details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for securities section
 */
$.registerNewModule({
    name: 'og.views.timeseries',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'timeseries',
            formatter = og.common.slickgrid.formatters.timeseries,
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'timeseries',
                'columns': [
                    {
                        id: 'datasource', name: 'Datasource', field: 'datasource',
                        width: 90, cssClass: 'og-uppercase'
                    },
                    {
                        id: 'BLOOMBERG_TICKER', name: 'Bloomberg Ticker', field: 'BLOOMBERG_TICKER',
                        width: 150, cssClass: 'og-link',
                        formatter: formatter
                    },
                    {
                        id: 'BLOOMBERG_BUID', name: 'Bloomberg BUID',  field: 'BLOOMBERG_BUID',
                        width: 150, cssClass: 'og-link',
                        formatter: formatter
                    },
                    {
                        id: 'CUSIP', name: 'CUSIP', field: 'CUSIP',
                        width: 70, cssClass: 'og-link',
                        formatter: formatter
                    },
                    {
                        id: 'ISIN', name: 'CUSIP', field: 'CUSIP',
                        width: 70, cssClass: 'og-link',
                        formatter: formatter
                    },
                    {
                        id: 'RIC', name: 'RIC', field: 'RIC',
                        width: 70, cssClass: 'og-link',
                        formatter: formatter
                    },
                    {
                        id: 'SEDOL1', name: 'SEDOL1', field: 'SEDOL1',
                        width: 70, cssClass: 'og-link',
                        formatter: formatter
                    }
                ]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_timeseries: {route: '/' + page_name + '/:id', method: module.name + '.load_' + page_name}
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_timeseries: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.timeseriesFunctions, ui = og.common.util.ui;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            f.renderTimeSeriesIdentifiers(".OG-timeseries .og-js-identifiers", json.identifiers);
                            ui.renderPlot(".OG-timeseries .og-js-timeseriesPlot", json.timeseries.data);
                            f.renderTimeSeriesTable(".OG-timeseries .og-js-table", {
                                "fieldLabels": json.timeseries.fieldLabels,
                                "data": json.timeseries.data
                            });
                            // Hook up CSV button
                            $('.OG-timeseries .og-js-timeSeriesCsv').click(function () {
                                window.location.href = 'http://localhost:8080/jax/timeseries/Tss::3535.csv';
                            });
                            ui.expandHeightToWindowBottom({element: '.OG-timeseries .og-dataPoints tbody'});
                            ui.expandHeightToWindowBottom({element: '.OG-timeseries .og-dataPoints table'});
                            og.common.details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for securities section
 */
$.registerNewModule({
    name: 'og.views.holidays',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'holidays',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'holidays',
                'columns': [
                    {id: 'name', name: 'Name', field: 'name', width: 100, cssClass: 'og-link'},
                    {id: 'type', name: 'Type', field: 'type', width: 200}
                ]};
        module.rules = {
            load: {route: '/' + page_name + '/filter:?', method: module.name + '.load'},
            load_holidays: {route: '/' + page_name + '/:id/type:/filter:?', method: module.name + '.load_' + page_name}
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_holidays: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, ui = og.common.util.ui, details = og.common.details;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            $('.OG-holiday .og-calendar').datepicker({
                                numberOfMonths: [4, 3],                     // Layout configuration
                                showCurrentAtPos: new Date().getMonth(),    // Makes the first month January
                                showButtonPanel: false,                     // Turns off default buttons
                                stepMonths: 12,                             // Pagination moves 1 year at a time
                                firstDay: 1,                                // Start the week on Monday
                                displayOnly: true,                          // This is an OG custom configuration
                                specialDates: json.dates                    // This is an OG custom configuration
                            });
                            ui.contentEditable();
                            details.favorites();
                            details.calendarUiChanges(json.dates);
                        }});
                    },
                    id: args.id,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for positions section
 */
$.registerNewModule({
    name: 'og.views.regions',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'regions',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'regions',
                'columns': [{id: 'name', name: 'Name', field: 'name',width: 300, cssClass: 'og-link'}]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_regions: {route: '/' + page_name + '/:id/:node?', method: module.name + '.load_' + page_name}
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_regions: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.region_functions;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            f.render_keys(".OG-region .og-js-keys", json.keys);
                            f.render_regions(".OG-region .og-js-parent_regions", json.parent);
                            f.render_regions(".OG-region .og-js-child_regions", json.child);
                            og.common.details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function() {}
                });
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for securities section
 */
$.registerNewModule({
    name: 'og.views.batches',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'batches',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'batches',
                'columns': [
                    {id: 'observationDate', name: 'ObservationDate', field: 'id', width: 130, cssClass: 'og-link'},
                    {id: 'observationTime', name: 'ObservationTime', field: 'observationTime', width: 130},
                    {id: 'status', name: 'Status', field: 'status', width: 130}
                ]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'},
            load_batches: {route: '/' + page_name + '/:id/:observation_time', method: module.name + '.load_' + page_name}
        };
        return {
            load: function (args) {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').show();
                $('#OG-analytics').hide();
                search.core($.extend(search_options, search_options, {url: args}));
                $('#OG-details .og-main').html('default ' + page_name + ' page');
            },
            load_batches: function (args) {
                // Load search if changed
                if (routes.last()) {
                    if (routes.parse(routes.last()).page !== '/' + page_name + '/') {
                        masthead.menu.set_tab(page_name);
                        search.core($.extend(search_options, search_options, {url: args}));
                    }
                } else {
                    masthead.menu.set_tab(page_name);
                    search.core($.extend(search_options, search_options, {url: args}));
                }
                // Setup details page
                api[page_name].get({
                    handler: function(j) {
                        var json = j.data, f = og.common.details.batch_functions, ui = og.common.util.ui;
                        og.api['static']({module: 'og.views.' + page_name, handler: function (html) {
                            $('#OG-details .og-main').html(og.common.details.core(j.data, html));
                            f.results('.OG-batch .og-js-results', json.data.batch_results);
                            f.errors('.OG-batch .og-js-errors', json.data.batch_errors);
                            ui.contentEditable();
                            og.common.details.favorites();
                        }});
                    },
                    observation_date: args.id,
                    observation_time: args.observation_time,
                    loading: function() {}
                });

            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
/**
 * view for portfolios section
 */
$.registerNewModule({
    name: 'og.views.analytics',
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this,
            masthead = og.common.masthead, search = og.common.search_results, details = og.common.details,
            page_name = 'analytics',
            search_options = {
                'selector': '.og-js-results-slick', 'page_type': 'analytics',
                'columns': [{id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link'}]};
        module.rules = {
            load: {route: '/' + page_name, method: module.name + '.load'}
        };
        return {
            load: function () {
                masthead.menu.set_tab(page_name);
                $('#OG-details, #OG-sr').hide();
                $('#OG-analytics').show();
                $('#OG-analytics .og-main').html('default ' + page_name + ' page');
            },
            init: function () {
                for (var rule in module.rules) routes.add(module.rules[rule]);
            },
            rules: module.rules
        };
    }
});
