/**
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.history',
    dependencies: ['og.common.util.HashQueue', 'og.dev'],
    obj: function () {
        var HashQueue = og.common.util.HashQueue, queues = {}, queue, module = this;
        queue = function (item) {
            if (typeof item !== 'string') throw new TypeError(module.name + ': item should be a string');
            if (queues[item]) return queues[item];
            try {
                queues[item] = new HashQueue(localStorage.getItem(item) || 10);
            } catch (error) {
                og.dev.warn('queues[' + item + '] failed to load ' + localStorage.getItem(item) + '\n' + error.message);
                localStorage.removeItem(item);
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
                    localStorage.setItem(item, history.serialize());
                } catch (error) {
                    og.dev.warn(item + ' storage failed\n', error);
                    localStorage.removeItem(item);
                }
            }
        }
    }
});