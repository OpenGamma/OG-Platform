/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
Function.prototype.preload = function () {
    var method = this, new_method, key, has = 'hasOwnProperty',
        merge = function () {
            var self = 'merge', to_string = Object.prototype.toString, clone = function (obj) {
                return typeof obj !== 'object' || obj === null ? obj // primitives
                    : Object.prototype.toString.call(obj) === '[object Array]' ? obj.map(clone) // arrays
                        : merge(obj); // objects
            };
            return Array.prototype.reduce.call(arguments, function (acc, obj) {
                if (!obj || typeof obj !== 'object' || to_string.call(obj) === '[object Array]')
                    throw new TypeError(self + ': ' + to_string.call(obj) + ' is not mergeable');
                for (var key in obj) if (obj[has](key)) acc[key] = clone(obj[key]);
                return acc;
            }, {});
        },
        orig = merge.apply(null, Array.prototype.slice.call(arguments));
    new_method = function () {
        return arguments.length ? method.call(this, merge.apply(null, Array.prototype.concat.apply([orig], arguments)))
            : method.call(this, orig);
    };
    for (key in method) if (method[has](key)) new_method[key] = method[key];
    return new_method;
};