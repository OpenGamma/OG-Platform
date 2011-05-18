/**
 * og.common.util.history
 */
$.register_module({
    name: 'og.common.util.history',
    dependencies: ['og.common.util.hashqueue', 'og.dev'],
    obj: function () {
        var hashqueue = og.common.util.hashqueue, queues = {}, queue, module = this;
        queue = function (item) {
            if (typeof item !== 'string') throw new TypeError(module.name + ': "item" should be a string');
            if (queues[item]) return queues[item];
            try {
                queues[item] = new hashqueue(localStorage.getItem(item) || 5);
            } catch (error) {
                og.dev.warn('queues[' + item + '] failed to load ' + localStorage.getItem(item) + '\n' + error.message);
                localStorage.removeItem(item);
                queues[item] = new hashqueue(5);
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
                localStorage.setItem(item, history.serialize());
            }
        }
    }
});