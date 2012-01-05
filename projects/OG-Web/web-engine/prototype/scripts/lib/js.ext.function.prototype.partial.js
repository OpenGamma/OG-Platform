/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
Function.prototype.partial = function () {
    var method = this, orig = arguments, orig_len = orig.length, i, j, args, arguments_len,
        new_method, key, has = 'hasOwnProperty';
    new_method = function () {
        if (!(arguments_len = arguments.length)) return method.apply(this, orig);
        for (args = [], i = 0, j = 0; i < orig_len; i += 1) args.push(orig[i] === undefined ? arguments[j++] : orig[i]);
        return method.apply(this, args.concat(Array.prototype.slice.call(arguments, j, arguments_len)));
    };
    for (key in method) if (method[has](key)) new_method[key] = method[key];
    return new_method;
};