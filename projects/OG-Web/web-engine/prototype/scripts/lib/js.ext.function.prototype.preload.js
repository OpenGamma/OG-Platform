/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

(function () {
    var has = 'hasOwnProperty', array = '[object Array]', object = 'object',
        concat = Array.prototype.concat,
        reduce = Array.prototype.reduce,
        slice = Array.prototype.slice,
        to_string = Object.prototype.toString;
    Function.prototype.preload = function () {
        var method = this, new_method, key, merge, clone, orig;
        clone = function (obj) {
            return typeof obj !== object || obj === null ? obj // primitives
                : to_string.call(obj) === array ? obj.map(clone) // arrays
                    : merge(obj); // objects
        };
        merge = function () {
            return reduce.call(arguments, function (acc, obj) {
                var key;
                if (!obj || typeof obj !== object || to_string.call(obj) === array)
                    throw new TypeError('merge: ' + to_string.call(obj) + ' is not mergeable');
                for (key in obj) if (obj[has](key)) acc[key] = clone(obj[key]);
                return acc;
            }, {});
        };
        orig = merge.apply(null, slice.call(arguments));
        new_method = function () {
            return arguments.length ? method.call(this, merge.apply(null, concat.apply([orig], arguments)))
                : method.call(this, orig);
        };
        // make sure you also bring along the old prototype
        new_method.prototype = method.prototype;
        // if the function instance has been extended, copy all of its properties
        for (key in method) if (method[has](key)) new_method[key] = method[key];
        return new_method;
    };
})();