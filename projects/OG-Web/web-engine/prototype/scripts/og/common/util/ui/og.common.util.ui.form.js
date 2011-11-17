/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.util.ui.Form',
    dependencies: ['og.api.text'],
    obj: function () {
        var stall = 500, id_count = 0, dummy = '<p/>', api_text = og.api.text, numbers = {},
            form_template = '<form action="." id="${id}"><div class="OG-form">' +
                '{{html html}}<input type="submit" style="display: none;"></div></form>',
            Form, Block, Field;
        /**
         * @class Block
         */
        Block = function (form, config) {
            var block = this, klass = 'Block', config = config || {}, template = null, url = config.url,
                module = config.module, handlers = config.handlers || [], extras = config.extras,
                processor = config.processor,
                wrap = function (html) {
                    return config.wrap ? $(dummy).append($.tmpl(config.wrap, {html: html})).html() : html;
                };
            block.children = config.children || [];
            block.html = function (handler) {
                if (template === null) return setTimeout(block.html.partial(handler), stall);
                var self = 'html', total = block.children.length, done = 0, result = [],
                    internal_handler = function () {
                        var html = template ? $(dummy).append($.tmpl(template, $.extend(
                            result.reduce(function (acc, val, idx) {return acc['item_' + idx] = val, acc;}, {}),
                            extras
                        ))).html() : wrap(result.join(''));
                        return handler(html);
                    };
                if (!total) return internal_handler();
                block.children.forEach(function (val, idx) {
                    var error_prefix;
                    if (val.html) return val.html(function (html) {
                        result[idx] = html, (total === ++done) && internal_handler();
                    });
                    if (typeof val === 'string') return result[idx] = val, (total === ++done) && internal_handler();
                    error_prefix = klass + '#' + self + ': children[' + idx + ']';
                    throw new TypeError(error_prefix + ' is neither a string nor does it have an html function');
                });
            };
            block.load = function () {
                handlers.forEach(function (handler) {if (handler.type === 'form:load') handler.handler();});
                block.children.forEach(function (child) {if (child.load) child.load();});
            };
            block.process = function (data, errors) {
                block.children.forEach(function (child) {if (child.process) child.process(data, errors);});
                try {if (processor) processor(data);} catch (error) {errors.push(error);}
            };
            if (url || module) {
                if (url) api_text({handler: function (result) {template = result;}, url: url});
                else if (module) api_text({handler: function (result) {template = result;}, module: module});
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
                extras = config.extras, handlers = config.handlers || [], generator = config.generator,
                processor = config.processor;
            field.html = function (handler) {
                if (template === null) return setTimeout(field.html.partial(handler), stall);
                if (extras && template) return handler($(dummy).append($.tmpl(template, extras)).html());
                generator(handler, template);
            };
            field.load = function () {
                handlers.forEach(function (handler) {if (handler.type === 'form:load') handler.handler();});
            };
            field.process = function (data, errors) {
                try {if (processor) processor(data);} catch (error) {errors.push(error);}
            };
            if (url || module) {
                if (url) api_text({handler: function (result) {template = result;}, url: url});
                else if (module) api_text({handler: function (result) {template = result;}, module: module});
            } else {
                template = '';
            }
            form.attach(handlers);
        };
        /**
         * @class Form
         */
        Form = function (config) {
            var form = new Block(null, config), selector = config.selector, $root = $(selector), $form, dom_events = {},
                klass = 'Form', form_events = {'form:load': [], 'form:submit': [], 'form:error': []},
                delegator = function (e) {
                    var $target = $(e.target), results = [];
                    dom_events[e.type].forEach(function (val) {
                        if (!$target.is(val.selector) && !$target.parent(val.selector).length) return;
                        var result = val.handler(e);
                        results.push(typeof result === 'undefined' ? true : !!result);
                    });
                    if (results.length && !results.some(Boolean)) return false;
                },
                type_map = config.type_map,
                find_in_meta = (function (memo) {
                    var key, len;
                    for (key in type_map) if (~key.indexOf('*')) memo.push({
                        expr: new RegExp('^' + key.replace(/\./g, '\\.').replace(/\*/g, '[^\.]+') + '$'),
                        value: type_map[key]
                    });
                    len = memo.length;
                    return function (path) {
                        for (var lcv = 0; lcv < len; lcv += 1) if (memo[lcv].expr.test(path)) return memo[lcv].value;
                        return null
                    };
                })([]),
                build_meta = function (data, path, warns) {
                    var result = {}, key, empty = '<EMPTY>', index = '<INDEX>', null_path = path === null;
                    if ($.isArray(data)) return data.map(function (val, idx) {
                        var value = build_meta(val, null_path ? idx : [path, index].join('.'), warns);
                        if ((value === Form.type.IND) && (val !== null)) value = Form.type.STR;
                        return value in numbers ? ((data[idx] = +data[idx]), value) : value;
                    });
                    if (data === null || typeof data !== 'object') // no empty string keys at root level
                        return !(result = type_map[path] || find_in_meta(path)) ? (warns.push(path), 'BADTYPE'): result;
                    for (key in data) {
                        result[key] = build_meta(data[key], null_path ? key : [path, key || empty].join('.'), warns);
                        if (result[key] in numbers) data[key] = +data[key];
                        // INDs that are not null need to be re-typed as STRs
                        if ((result[key] === Form.type.IND) && (data[key] !== null)) result[key] = Form.type.STR;
                    }
                    return result;
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
                $root.html($(dummy).append($.tmpl(form_template, {id: form.id, html: html})).html());
                form_events['form:load'].forEach(function (val) {val.handler();});
                ($form = $('#' + form.id)).unbind().submit(function (e) {
                    var self = 'onsubmit', raw = $form.serializeArray(), built_meta, meta_warns = [],
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
                    built_meta = type_map ? build_meta(data, null, meta_warns) : null;
                    meta_warns = meta_warns.sort().reduce(function (acc, val) {
                        return acc[acc.length - 1] !== val ? (acc.push(val), acc) : acc;
                    }, []).join('\n');
                    if (meta_warns.length)
                        return og.dev.warn(klass + '#build_meta needs these:\n', meta_warns), false;
                    result = {raw: raw, data: data, errors: errors, meta: built_meta};
                    try {
                        form_events['form:submit'].forEach(function (val) {val.handler(result);});
                    } catch (error) {
                        og.dev.warn(klass + '#' + self + ' a form:submit handler failed with:\n', error);
                    }
                    return false;
                });
            });
            form.Field = Field.partial(form);
            form.id = 'gen_form_' + id_count++;
            $root.unbind();
            if (config.handlers) form.attach(config.handlers);
            return form;
        };
        Form.type =  {
            BOO: 'boolean',
            BYT: 'byte',
            DBL: 'double',
            IND: 'indicator',
            SHR: 'short',
            STR: 'string'
        };
        [Form.type.BYT, Form.type.DBL, Form.type.SHR].forEach(function (val, idx) {numbers[val] = null;});
        return Form;
    }
});