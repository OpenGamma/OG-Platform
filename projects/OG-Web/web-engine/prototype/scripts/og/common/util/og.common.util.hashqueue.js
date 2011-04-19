/**
 * a hash table that automatically discards old items like a queue
 */
$.register_module({
    name: 'og.common.util.hashqueue',
    obj: function () {
        return function (size) {
            var hashqueue = this, data = {}, keys = [];
            if (typeof size !== 'number' || size < 1 || size % 1)
                throw new RangeError('hashqueue: size must be a positive integer');
            hashqueue.del = function (key) {
                if (!data.hasOwnProperty(key)) return;
                delete data[key];
                keys.splice(keys.indexOf(key), 1);
            };
            hashqueue.get = function (key) {return data[key];};
            hashqueue.set = function (key, value) {
                if (data.hasOwnProperty(key)) hashqueue.del(key);
                if (keys.length === size) delete data[keys.shift()];
                data[key] = value;
                keys.push(key);
            };
        };
    }
});