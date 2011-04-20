/**
 * a hash table that automatically discards old items like a queue
 */
$.register_module({
    name: 'og.common.util.hashqueue',
    dependencies: ['og.dev'],
    obj: function () {
        var warn = og.dev.warn;
        return function (instance) {
            var hashqueue = this, self = 'hashqueue', data, keys = [], size = instance, instance_type = typeof instance,
                parsed, has = 'hasOwnProperty', corrupted_message = self + ': corrupted data - keys or size do not match data';
            if (instance_type === 'string') parsed = JSON.parse(instance);
            if (instance_type === 'object') parsed = instance;
            if (parsed) size = parsed.size, data = parsed.data, keys = parsed.keys, parsed = (void 0);
            if (size < 1 || size % 1) throw new RangeError(self + ': size must be a positive integer');
            if (keys.some(function (key) {return !data[has](key)}) || (keys.length > size)) throw new Error(corrupted_message);
            if (data) (function () {
                var key, key_names = keys.reduce(function (acc, val) {return acc[val] = 0, acc;}, {});
                for (key in data) if (data[has](key) && !key_names[has](key)) throw new Error(corrupted_message);
            })(); else data = {};
            hashqueue.all = function () {
                var shallow_copy = {}, key;
                for (key in data) if (data[has](key)) shallow_copy[key] = data[key];
                return {data: shallow_copy, keys: keys.slice(), size: size};
            };
            hashqueue.del = function (key) {
                if (!data[has](key)) return;
                delete data[key];
                keys.splice(keys.indexOf(key), 1);
            };
            hashqueue.get = function (key) {return data[key];};
            hashqueue.set = function (key, value) {
                if (data[has](key)) hashqueue.del(key);
                if (keys.length === size) delete data[keys.shift()];
                data[key] = value;
                keys.push(key);
                return hashqueue;
            };
            hashqueue.serialize = function () {
                return JSON.stringify({data: data, keys: keys, size: size}, function (key, value) {
                    return typeof value === 'function' ? (warn(self + ': a function was lost in serialization'), null) : value;
                });
            };
            hashqueue.size = function () {return size;};
        };
    }
});