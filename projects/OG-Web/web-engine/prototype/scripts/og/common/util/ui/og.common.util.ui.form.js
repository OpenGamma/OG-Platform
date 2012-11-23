/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

$.register_module({
    name: 'og.common.util.ui.Form',
    dependencies: ['og.api.text'],
    obj: function () {
        var module = this, STALL = 500 /* 500ms */, id_count = 0, api_text = og.api.text, numbers = {},
            has = 'hasOwnProperty', form_template = Handlebars.compile('<form action="." id="{{id}}">' +
                '<div class="OG-form">{{{html}}}<input type="submit" style="display: none;"></div></form>');
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
        /** @private */
        var meta_build = function (input, type_map, find) {
            var result = {}, key, empty = '<EMPTY>', index = '<INDEX>',
                data = input.data, path = input.path, warns = input.warns, null_path = path === null, new_path;
            if ($.isArray(data)) return data.map(function (val, idx) {
                var value = meta_build({
                    data: val, path: null_path ? idx : [path, index].join('.'), warns: warns
                }, type_map, find);
                if ((value === Form.type.IND) && (val !== null)) value = Form.type.STR;
                return numbers[has](value) ? ((data[idx] = +data[idx]), value) : value;
            });
            if (data === null || typeof data !== 'object') // no empty string keys at root level
                return !(result = type_map[path] || find(path)) ? (warns.push(path), 'BADTYPE'): result;
            for (key in data) {
                new_path = null_path ? key.replace(/\./g, '') // if a key has dots in it, drop them, it is
                    : [path, key.replace(/\./g, '') || empty].join('.'); // a wildcard anyway
                result[key] = meta_build({data: data[key], path: new_path, warns: warns}, type_map, find);
                if (numbers[has](result[key])) {
                    if (typeof data[key] === 'number') continue;
                    if (data[key].length) data[key] = +data[key]; else delete data[key];
                }
                // INDs that are not null need to be re-typed as STRs
                if ((result[key] === Form.type.IND) && (data[key] !== null)) result[key] = Form.type.STR;
            }
            return result;
        };
        /** @private */
        var meta_find = function (memo, type_map) {
            var key, len;
            for (key in type_map) if (~key.indexOf('*')) memo.push({
                expr: new RegExp('^' + key.replace(/\./g, '\\.').replace(/\*/g, '[^\.]+') + '$'), value: type_map[key]
            });
            len = memo.length;
            return function (path) {
                for (var lcv = 0; lcv < len; lcv += 1) if (memo[lcv].expr.test(path)) return memo[lcv].value;
                return null
            };
        };
        /** @private */
        var submit_handler = function (form, extras) {
            var result = form.compile();
            $.extend(true, result.extras, extras);
            try {
                og.common.events.fire.call(form, 'form:submit', result);
            } catch (error) {
                og.dev.warn(module.name + '#submit_handler a form:submit handler failed with:', error);
            }
        };
        /** @class Form */
        var Form = function (config) {
            var form = this;
            og.common.util.ui.Block.call(form, null, config); // create a Block instance and extend it
            form.Block = og.common.util.ui.Block.partial(form);
            form.data = config.data;
            form.dom_events = {};
            form.id = 'gen_form_' + id_count++;
            form.parent = $(config.selector).unbind(); // no listeners on the selector
            form.root = null;
            form.submit = function (extras) {submit_handler(form, extras);}; // defined here so $ can't hijack this
        };
        Form.prototype = new og.common.util.ui.Block; // inherit Block prototype
        Form.prototype.compile = function () {
            var form = this, raw = form.root.serializeArray(), built_meta = null, meta_warns = [],
                data = form.data ? $.extend(true, {}, form.data) : null, errors = [], type_map = form.config.type_map;
            if (!form.meta_find && type_map) form.meta_find = meta_find([], type_map); // cache this function
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
            if (type_map)
                built_meta = meta_build({data: data, path: null, warns: meta_warns}, type_map, form.meta_find);
            meta_warns = meta_warns.sort().reduce(function (acc, val) {
                return acc[acc.length - 1] !== val ? (acc.push(val), acc) : acc;
            }, []).join(', ');
            if (meta_warns.length) throw new Error(module.name + '#meta_build needs: ' + meta_warns);
            return {raw: raw, data: data, errors: errors, meta: built_meta, extras: {}};
        };
        Form.prototype.dom = function () {
            var form = this;
            return form.html(function (html) {
                form.parent.empty().append(form_template({id: form.id, html: html}));
                og.common.events.fire.call(form, 'form:load');
                (form.root = $('#' + form.id)).unbind().submit(function (event) {return submit_handler(form), false;});
            });
        };
        Form.prototype.off = function (type) {
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
        Form.prototype.on = function (type) {
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
            form.parent.on(type, delegator.bind(form));
            return origin;
        };
        Form.prototype.template = null; // reset back to null because it got set to false in Block
        Form.type =  {BOO: 'boolean', BYT: 'byte', DBL: 'double', IND: 'indicator', SHR: 'short', STR: 'string'};
        ['BYT', 'DBL', 'SHR'].forEach(function (val, idx) {numbers[Form.type[val]] = null;});
        return Form;
    }
});