/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
Function.prototype.preload = function () {
    var method = this,
        merge = function () {
            var self = 'merge', result = {}, key, val, lcv, len = arguments.length,
                clone = function (obj) {
                    if (typeof obj !== 'object' || obj === null) return obj; // primitives
                    if (Object.prototype.toString.call(obj) === '[object Array]') return obj.map(clone); // arrays
                    if (typeof obj === 'object') return merge(obj); // objects
                };
            for (lcv = 0; lcv < len; lcv += 1)
                if (typeof arguments[lcv] !== 'object')
                    throw new TypeError(self + ': ' + arguments[lcv] + ' is not an object');
                else
                    for (key in arguments[lcv]) result[key] = clone(arguments[lcv][key]);
            return result;
        },
        orig = merge.apply(null, Array.prototype.slice.call(arguments)),
        new_method, key, has = 'hasOwnProperty';
    new_method = function () {
        return arguments.length ? method.call(this, merge.apply(null, Array.prototype.concat.apply([orig], arguments)))
            : method.call(this, orig);
    };
    for (key in method) if (method[has](key)) new_method[key] = method[key];
    return new_method;
};