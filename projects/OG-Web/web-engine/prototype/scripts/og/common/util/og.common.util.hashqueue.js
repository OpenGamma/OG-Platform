/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * a hash table that automatically discards old items like a queue
 */
$.register_module({
    name: 'og.common.util.HashQueue',
    dependencies: ['og.dev'],
    obj: function () {
        var warn = og.dev.warn, module = this;
        return function (instance) {
            var hashqueue = this, data, keys = [], size = instance, type = typeof instance,
                has = 'hasOwnProperty', corrupted = module.name + ': corrupted data, key or size mismatch',
                parsed = type === 'string' ? JSON.parse(instance) : type === 'object' ? instance : null;
            if (parsed) size = parsed.size, data = parsed.data || data, keys = parsed.keys || keys, parsed = (void 0);
            if (!size || size < 1 || size % 1) throw new RangeError(module.name + ': size must be a positive integer');
            if (keys.some(function (key) {return !data[has](key);}) || (keys.length > size)) throw new Error(corrupted);
            if (data) (function () {
                var key, key_names = keys.reduce(function (acc, val) {return acc[val] = 0, acc;}, {});
                for (key in data) if (data[has](key) && !key_names[has](key)) throw new Error(corrupted);
            })(); else data = {};
            hashqueue.all = function () {
                var shallow_copy = {}, key;
                for (key in data) if (data[has](key)) shallow_copy[key] = data[key];
                return {data: shallow_copy, keys: keys.slice(), size: size};
            };
            hashqueue.del = function (key) {
                return !data[has](key) ? hashqueue : delete data[key], keys.splice(keys.indexOf(key), 1), hashqueue;
            };
            hashqueue.get = function (key) {return data[key];};
            hashqueue.set = function (key, value) {
                if (data[has](key)) hashqueue.del(key);
                if (keys.length === size) delete data[keys.shift()];
                return data[key] = value, keys.push(key), hashqueue;
            };
            hashqueue.serialize = function () {
                return JSON.stringify({data: data, keys: keys, size: size}, function (key, value) {
                    return typeof value === 'function' ? (warn(module.name + ': a function was not serialized'), null)
                        : value;
                });
            };
            hashqueue.size = function () {return size;};
        };
    }
});