/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

(function () {
    var has = 'hasOwnProperty', slice = Array.prototype.slice;
    Function.prototype.partial = function () {
        var method = this, orig = arguments, orig_len = orig.length, key,
            new_method = function () {
                var arguments_len = arguments.length, args = [], i = 0, j = 0;
                if (!arguments_len) return method.apply(this, slice.call(orig));
                for (; i < orig_len; i++)
                    args.push(orig[i] === void 0 ? arguments[j++] : orig[i]);
                return method.apply(this, args.concat(slice.call(arguments, j, arguments_len)));
            };
        // make sure you also bring along the old prototype
        new_method.prototype = method.prototype;
        // if the function instance has been extended, copy all of its properties
        for (key in method) if (method[has](key)) new_method[key] = method[key];
        return new_method;
    };
})();