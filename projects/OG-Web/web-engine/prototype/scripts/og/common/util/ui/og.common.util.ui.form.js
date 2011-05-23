/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.util.ui.Form',
    dependencies: ['og.api.text', 'og.api.rest'],
    obj: function () {
        var stall = 500, prefix = 'item_', api_text = og.api.text, api_rest = og.api.rest, Form, Block, Field;
        /**
         * @class Block
         */
        Block = function (form, config) {
            var block = this, self = 'Block', template = null, url = config.url, handlers = config.handlers || [],
                extras = config.extras;
            block.children = [];
            block.html = function (handler) {
                if (template === null) return setTimeout(block.html.partial(handler), stall);
                var total = block.children.length, done = 0, result = [], internal_handler = function () {
                    return handler(template ? $.tmpl(template, $.extend(result.reduce(function (acc, val, idx) {
                        return acc[prefix + idx] = val, acc;
                    }, {}), extras)) : result.join(''));
                };
                block.children.forEach(function (val, idx) {
                    if (typeof val.html === 'function') return val.html(function (html) {
                        result[idx] = html, (total === ++done) && internal_handler();
                    });
                    throw new TypeError(self + ': children[' + idx + '].html is not a function');
                });
            };
            // initialize Block
            if (url) api_text({handler: function (result) {template = result;}, url: url}); else template = '';
            if (form) form.attach(handlers);
        };
        /**
         * @class Field
         */
        Field = function (form, config) {
            var field = this, self = 'Field', template = null, url = config.url, handlers = config.handlers || [],
                generator = config.generator;
            field.html = function (handler) {
                if (template === null) return setTimeout(field.html.partial(handler), stall);
                generator(template, handler);
            };
            // initialize Field
            if (url) api_text({handler: function (result) {template = result;}, url: url}); else template = '';
            form.attach(handlers);
        };
        /**
         * @class Form
         */
        Form = function (config) {
            var form = new Block(null, config), selector = config.selector, $root = $(selector),
                form_events = {'form:load': [], 'form:unload': []};
            form.attach = function (handlers) {
                console.log('called form.attach with: ', handlers);
                handlers.filter(function (val) {
                    var form_event = form_events[val.type];
                    if (form_event) form_event.push(val);
                    return form_event;
                }).forEach(function (val) {
                    // real DOM events are going to go here
                });
            };
            form.Block = Block.partial(form);
            form.dom = form.html.partial(function (html) {
                $root.html(html);
                form_events['form:load'].forEach(function (val) {if (selector === val.selector) val.handler();});
            });
            form.Field = Field.partial(form);
            if (config.handlers) form.attach(config.handlers);
            return form;
        };
        return Form;
    }
});