/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

$.register_module({
    name: 'og.common.util.ui.Form',
    dependencies: ['og.api.text'],
    obj: function () {
        var module = this, STALL = 500 /* 500ms */, id_count = 0, dummy = '<p/>', api_text = og.api.text, numbers = {},
            has = 'hasOwnProperty', form_template = Handlebars.compile('<form action="." id="{{id}}">' +
                '<div class="OG-form">{{{html}}}<input type="submit" style="display: none;"></div></form>');
        /** private */
        var jq_compile = function (name, template) {
            og.dev.warn(module.name + ': ' + name + ' is a deprecated template');
            return function (values) {return $(dummy).append($.tmpl(template, values)).html();};
        };
        /** @private */
        var templatize = function (name, html) {
            return !module || !html ? false : ~name.search(/tash$/) ? Handlebars.compile(html) : jq_compile(name, html);
        };
        /**
         * @class Block
         */
        var Block = function (form, config) {
            var block = this, config = config || {}, template = null, module = config.module, extras = config.extras,
                processor = config.processor;
            var wrap = function (html) {return config.wrap ? Handlebars.compile(config.wrap)({html: html}) : html;};
            block.children = config.children || [];
            block.form = form;
            block.html = function (handler) {
                if (template === null) return setTimeout(block.html.partial(handler), STALL);
                var total = block.children.length, done = 0, result = [],
                    internal_handler = function () {
                        handler(template ? template($.extend(
                            result.reduce(function (acc, val, idx) {return acc['item_' + idx] = val, acc;}, {}),
                            extras
                        )) : wrap(result.join('')));
                    };
                if (!total) return internal_handler();
                block.children.forEach(function (val, idx) {
                    var error_prefix;
                    if (val.html) return val
                        .html(function (html) {result[idx] = html, (total === ++done) && internal_handler();});
                    if (typeof val === 'string') return result[idx] = val, (total === ++done) && internal_handler();
                    error_prefix = module.name + '#html: children[' + idx + ']';
                    throw new TypeError(error_prefix + ' is neither a string nor does it have an html function');
                });
            };
            block.load = function () {
                if (form.events['form:load']) // mimic a form load event
                    form.events['form:load'].forEach(function (val) {if (val.origin === block) val.handler();});
                block.children.forEach(function (child) {if (child.load) child.load();});
            };
            block.process = function (data, errors) {
                block.children.forEach(function (child) {if (child.process) child.process(data, errors);});
                try {if (processor) processor(data);} catch (error) {errors.push(error);}
            };
            $.when(module ? api_text({module: module}) : void 0)
                .then(function (result) {template = templatize(module, result);});
            if (form && config.handlers && config.handlers.length) form.attach.call(block, config.handlers);
        };
        Block.prototype.off = function (type) {
            var form = this.form || this, origin = this,
                selector = typeof arguments[1] === 'string' ? arguments[1] : null,
                handler = typeof arguments[1] === 'function' ? arguments[1] : arguments[2];
            if (0 === type.indexOf('form:')) return og.common.events.off.call(form, type, handler), origin;
            if (!form.dom_events[type]) return origin;
            form.dom_events[type] = form.dom_events[type].filter(function (val) {
                if (val.type !== type) return true;
                if (handler && selector) return val.handler !== handler && val.selector !== selector;
                if (handler) return val.handler !== handler;
                if (selector) return val.selector !== selector;
                return false;
            });
            if (form.dom_events[type].length) return origin;
            delete form.dom_events[type];
            form.root.off(type);
            return origin;
        };
        Block.prototype.on = function (type) {
            var form = this.form || this, origin = this,
                selector = typeof arguments[1] === 'string' ? arguments[1] : null,
                handler = typeof arguments[1] === 'function' ? arguments[1] : arguments[2],
                context = typeof arguments[2] !== 'function' ? arguments[2] : arguments[3];
            if (0 === type.indexOf('form:')) {
                og.common.events.on.call(form, type, handler, context);
                return form.events[type][form.events[type].length - 1].origin = origin;
            }
            if (!selector) throw new TypeError(module.name + '#on: selector is not defined');
            if (form.dom_events[type])
                return form.dom_events[type].push({type: type, selector: selector, handler: handler}), origin;
            form.dom_events[type] = [{type: type, selector: selector, handler: handler}];
            form.root.on(type, delegator.bind(form));
            return origin;
        };
        /** @private */
        var delegator = function (event) {
                var form = this, $target = $(event.target), results = [];
                form.dom_events[event.type].forEach(function (val) {
                    if (!$target.is(val.selector) && !$target.parent(val.selector).length) return;
                    var result = val.handler(event);
                    results.push(typeof result === 'undefined' ? true : !!result);
                });
                if (results.length && !results.some(Boolean)) return false;
        };
        /**
         * @class Field
         */
        var Field = function (form, config) {
            var field = this, template = null, module = config.module, extras = config.extras,
                generator = config.generator, processor = config.processor;
            field.form = form;
            field.html = function (handler) {
                if (template === null) return setTimeout(field.html.partial(handler), STALL);
                if (extras && template) return handler(template(extras)); else generator(handler, template);
            };
            field.load = function () {
                if (form.events['form:load']) // mimic a form load event
                    form.events['form:load'].forEach(function (val) {if (val.origin === field) val.handler();});
            };
            field.process = function (data, errors) {
                try {if (processor) processor(data);} catch (error) {errors.push(error);}
            };
            $.when(module ? api_text({module: module}) : void 0)
                .then(function (result) {template = templatize(module, result);});
            if (config.handlers && config.handlers.length) form.attach.call(field, config.handlers);
        };
        Field.prototype.on = function () {
            var field = this, form = field.form;
            return form ? form.on.apply(field, Array.prototype.slice.call(arguments)) : field;
        };
        Field.prototype.off = function () {
            var field = this, form = field.form;
            return form ? form.off.apply(field, Array.prototype.slice.call(arguments)) : field;
        };
        /**
         * @class Form
         */
        var Form = function (config) {
            var form = new Block(null, config), selector = config.selector, $form, type_map = config.type_map;
            /** @private */
            var build_meta = function (data, path, warns) {
                    var result = {}, key, empty = '<EMPTY>', index = '<INDEX>', null_path = path === null, new_path;
                    if ($.isArray(data)) return data.map(function (val, idx) {
                        var value = build_meta(val, null_path ? idx : [path, index].join('.'), warns);
                        if ((value === Form.type.IND) && (val !== null)) value = Form.type.STR;
                        return value in numbers ? ((data[idx] = +data[idx]), value) : value;
                    });
                    if (data === null || typeof data !== 'object') // no empty string keys at root level
                        return !(result = type_map[path] || find_in_meta(path)) ? (warns.push(path), 'BADTYPE'): result;
                    for (key in data) {
                        new_path = null_path ? key.replace(/\./g, '') // if a key has dots in it, drop them, it is
                            : [path, key.replace(/\./g, '') || empty].join('.'); // a wildcard anyway
                        result[key] = build_meta(data[key], new_path, warns);
                        if (result[key] in numbers) {
                            if (typeof data[key] === 'number') continue;
                            if (data[key].length) data[key] = +data[key]; else delete data[key];
                        }
                        // INDs that are not null need to be re-typed as STRs
                        if ((result[key] === Form.type.IND) && (data[key] !== null)) result[key] = Form.type.STR;
                    }
                    return result;
            };
            /** @private */
            var find_in_meta = (function (memo) {
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
            })([]);
            /** @private */
            var submit_handler = function (event, extras) {
                    var result = form.compile();
                    $.extend(true, result.extras, extras);
                    if (event && event.preventDefault) event.preventDefault();
                    try {
                        og.common.events.fire.call(form, 'form:submit', result);
                    } catch (error) {
                        og.dev.warn(module.name + '#submit_handler a form:submit handler failed with:', error);
                    }
            };
            form.attach = function (handlers) {
                og.dev.warn(module.name + '#attach is deprecated: ', handlers);
                var context = this; // might not === form
                handlers.forEach(function (val) {form.on.call(context, val.type, val.selector, val.handler);});
            };
            form.Block = Block.partial(form);
            form.compile = function () {
                var raw = $form.serializeArray(), built_meta, meta_warns = [],
                    data = form.data ? $.extend(true, {}, form.data) : null, errors = [];
                if (data) raw.forEach(function (value) {
                    var hier = value.name.split('.'), last = hier.pop();
                    try {
                        hier.reduce(function (acc, level) {
                            return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                        }, data)[last] = value.value;
                    } catch (error) {
                        data = null;
                        error = new Error(module.name + '#compile: could not drill down to data.' + value.name);
                        og.common.events.fire.call(form, 'form:error', error);
                    }
                });
                form.process(data, errors);
                built_meta = type_map ? build_meta(data, null, meta_warns) : null;
                meta_warns = meta_warns.sort().reduce(function (acc, val) {
                    return acc[acc.length - 1] !== val ? (acc.push(val), acc) : acc;
                }, []).join('\n');
                if (meta_warns.length) og.dev.warn(module.name + '#build_meta needs these:', meta_warns);
                return {raw: raw, data: data, errors: errors, meta: built_meta, extras: {}};
            };
            form.data = config.data;
            form.dom = form.html.partial(function (html) {
                form.root.empty().append(form_template({id: form.id, html: html}));
                og.common.events.fire.call(form, 'form:load');
                ($form = $('#' + form.id)).unbind().submit(submit_handler);
            });
            form.dom_events = {};
            form.Field = Field.partial(form);
            form.id = 'gen_form_' + id_count++;
            form.root = $(selector).unbind();
            form.submit = submit_handler.partial(null);
            if (config.handlers) form.attach(config.handlers);
            return form;
        };
        Form.type =  {
            BOO: 'boolean',     BYT: 'byte',    DBL: 'double',
            IND: 'indicator',   SHR: 'short',   STR: 'string'
        };
        ['BYT', 'DBL', 'SHR'].forEach(function (val, idx) {numbers[Form.type[val]] = null;});
        return Form;
    }
});