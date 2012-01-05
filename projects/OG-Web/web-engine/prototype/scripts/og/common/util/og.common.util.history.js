/**
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.history',
    dependencies: ['og.common.util.HashQueue', 'og.dev'],
    obj: function () {
        var HashQueue = og.common.util.HashQueue, queues = {}, queue, module = this, cache = window['localStorage'];
        remove = function (item) {
            try { // if cahce is restricted, bail
                cache['removeItem'](item);
            } catch (error) {
                og.dev.warn(module.name + ': ' + item + ' remove failed\n', error);
            }
        },
        queue = function (item) {
            if (typeof item !== 'string') throw new TypeError(module.name + ': item should be a string');
            if (queues[item]) return queues[item];
            try {
                queues[item] = new HashQueue(cache['getItem'](item) || 10);
            } catch (error) {
                og.dev.warn(module.name + ': queues[' + item + '] failed to load\n' + error.message);
                remove(item);
                queues[item] = new HashQueue(10);
            }
            return queues[item];
        };
        return {
            get: function (item) {return queue(item).all();},
            get_html: function (item) {
                var obj = queue(item).all(), html = '', keys = obj.keys, i = keys.length;
                while(i--) {html += '<li><a href="#' + obj.data[keys[i]] + '">' + keys[i] + '</a></li>';}
                return html;
            },
            put: function (obj) {
                // TODO use interface checker to validate object
                var item = obj.item, value = obj.value, name = obj.name, history = queue(item).set(name, value);
                try {
                    cache['setItem'](item, history.serialize());
                } catch (error) {
                    og.dev.warn(module.name + ': ' + item + ' storage failed\n', error);
                    remove(item);
                }
            }
        }
    }
});