/**
 * og.common.util.history
 */
$.register_module({
    name: 'og.common.util.history',
    dependencies: ['og.common.util.hashqueue'],
    obj: function () {
        var hashqueue = og.common.util.hashqueue, queues = [], queue, self = this;
        queue = function (item) {
            if (typeof item !== 'string') throw new TypeError(self.name + ': "item" should be a string');
            return queues[item] || new hashqueue(localStorage.getItem(item) || 5);
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