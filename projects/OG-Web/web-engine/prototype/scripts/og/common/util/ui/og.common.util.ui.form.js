/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.util.ui.Form',
    dependencies: ['og.api.text', 'og.api.rest'],
    obj: function () {
        var stall = 500, item_prefix = 'item_', id_count = 1,
            form_template = '<form action="." id="${id}">' +
                '{{html html}}<input type="submit" style="display: none;"></form>',
            api_text = og.api.text, api_rest = og.api.rest,
            Form, Block, Field;
        /**
         * @class Block
         */
        Block = function (form, config) {
            var block = this, klass = 'Block', template = null, url = config.url, module = config.module,
                handlers = config.handlers || [], extras = config.extras, processor = config.processor;
            block.children = [];
            block.html = function (handler) {
                if (template === null) return setTimeout(block.html.partial(handler), stall);
                var self = 'html', total = block.children.length, done = 0, result = [],
                    internal_handler = function () {
                        var html = template ? $('<p></p>').append($.tmpl(template, $.extend(
                            result.reduce(function (acc, val, idx) {return acc[item_prefix + idx] = val, acc;}, {}),
                            extras
                        ))).html() : result.join('');
                        return handler(html);
                    };
                if (!block.children.length) return internal_handler();
                block.children.forEach(function (val, idx) {
                    if (typeof val.html === 'function') return val.html(function (html) {
                        result[idx] = html, (total === ++done) && internal_handler();
                    });
                    throw new TypeError(klass + '#' + self + ': children[' + idx + '].html is not a function');
                });
            };
            block.process = function (data, errors) {
                try {
                    if (processor) processor(data);
                } catch (error) {
                    errors.push(error);
                }
                block.children.forEach(function (child) {child.process(data, errors);});
            };
            // initialize Block
            if (url || module) {
                if (url) api_text({handler: function (result) {template = result;}, url: url});
                else if (module)  api_text({handler: function (result) {template = result;}, module: module});
            } else {
                template = '';
            }
            if (form) form.attach(handlers);
        };
        /**
         * @class Field
         */
        Field = function (form, config) {
            var field = this, klass = 'Field', template = null, url = config.url, module = config.module,
                handlers = config.handlers || [], generator = config.generator, processor = config.processor;
            field.html = function (handler) {
                if (template === null) return setTimeout(field.html.partial(handler), stall);
                generator(handler, template);
            };
            field.process = function (data, errors) {
                try {
                    if (processor) processor(data);
                } catch (error) {
                    errors.push(error);
                }
            };
            // initialize Field
            if (url || module) {
                if (url) api_text({handler: function (result) {template = result;}, url: url});
                else if (module)  api_text({handler: function (result) {template = result;}, module: module});
            } else {
                template = '';
            }
            form.attach(handlers);
        };
        /**
         * @class Form
         */
        Form = function (config) {
            var form = new Block(null, config), selector = config.selector, $root = $(selector), $form,
                klass = 'Form', form_events = {'form:load': [], 'form:unload': [], 'form:submit': [], 'form:error': []},
                dom_events = {},
                delegator = function (e) {
                    var $target = $(e.target), results = [];
                    dom_events[e.type].forEach(function (val) {
                        if (!$target.is(val.selector)) return;
                        var result = val.handler(e);
                        results.push(typeof result === 'undefined' ? true : !!result);
                    });
                    if (results.length && !results.some(Boolean)) return false;
                };
            form.attach = function (handlers) {
                var self = 'attach';
                handlers.forEach(function (val) {
                    val.type.split(' ').forEach(function (type) {
                        if (form_events[type]) return form_events[type].push(val);
                        if (!val.selector) throw new TypeError(klass + '#' + self + ': val.selector is not defined');
                        if (dom_events[type]) return dom_events[type].push(val);
                        dom_events[type] = [val];
                        $root.bind(type, delegator);
                    });
                });
            };
            form.Block = Block.partial(form);
            form.dom = form.html.partial(function (html) {
                $root.html($.tmpl(form_template, {id: form.id, html: html}));
                form_events['form:load'].forEach(function (val) {val.handler();});
                ($form = $('#' + form.id)).unbind().submit(function (e) {
                    var self = 'onsubmit', raw = $form.serializeArray(),
                        data = config.data ? $.extend(true, {}, config.data) : null, errors = [], result;
                    if (data) raw.forEach(function (value) {
                        var hier = value.name.split('.'), last = hier.pop();
                        try {
                            hier.reduce(function (acc, level) {
                                return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                            }, data)[last] = value.value;
                        } catch (error) {
                            data = null;
                            error = new Error(klass + '#' + self + ': could not drill down to data.' + value.name);
                            form_events['form:error'].forEach(function (val) {val.handler(error);});
                        }
                    });
                    form.process(data, errors);
                    result = {raw: raw, data: data, errors: errors};
                    return !!form_events['form:submit'].forEach(function (val) {val.handler(result);});
                });
            });
            form.Field = Field.partial(form);
            form.id = 'gen_form_' + id_count++;
            // initialize Form
            $root.unbind();
            if (config.handlers) form.attach(config.handlers);
            return form;
        };
        return Form;
    }
});