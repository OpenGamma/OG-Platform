/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.common',
    dependencies: [],
    obj: function () {
        var module = this, cache = window['sessionStorage'], common, warn = og.dev.warn, str = function (val) {
            return val === void 0 ? ''
                : $.isArray(val) ? val.join('\n')
                    : typeof val === 'object' ? JSON.stringify(val)
                        : '' + val;
        };
        return common = {
            cache_clear: function (prefix) {
                try { // if cache is restricted, bail
                    if (!prefix) return cache.clear();
                    Object.keys(cache).forEach(function (key) {if (key.indexOf(prefix) === 0) common.cache_del(key);});
                } catch (error) {
                    return warn(module.name + ': clear_cache failed\n', error);
                }
            },
            cache_del: function (key) {
                try { // if cache is restricted, bail
                    cache['removeItem'](key);
                } catch (error) {
                    warn(module.name + ': cache_del failed\n', error);
                }
            },
            cache_get: function (key) {
                try { // if cache is restricted, bail
                    return cache['getItem'](key) ? JSON.parse(cache['getItem'](key)) : null;
                } catch (error) {
                    warn(module.name + ': cache_get failed\n', error);
                    return null;
                }
            },
            cache_set: function (key, value) {
                try { // if the cache is too full, fail gracefully
                    cache['setItem'](key, JSON.stringify(value));
                } catch (error) {
                    warn(module.name + ': cache_set failed\n', error);
                    common.cache_del(key);
                }
            },
            check: (function () {
                var check_dependencies = function (bundle, dependencies) {
                        var config = bundle.config, method = bundle.method, self = 'check_dependencies';
                        (($.isArray(dependencies) ? dependencies : [dependencies])).forEach(function (dependency) {
                            if (!dependency.require || !dependency.fields)
                                throw new TypeError(self + ': each dependency must have "fields" and "require"');
                            if (config[dependency.require] !== void 0) return;
                            dependency.fields.forEach(function (field) {
                                if (config[field] === void 0) return;
                                throw new ReferenceError(method + ': ' + field + ' require(s) ' + dependency.require);
                            });
                        });
                };
                /** @ignore */
                var check_empties = function (bundle, params) { // if condition then fields can't exist, optional label
                    var config = bundle.config, method = bundle.method, self = 'check_empties';
                    ($.isArray(params) ? params : [params]).forEach(function (obj) {
                        var condition = 'condition' in obj ? obj.condition : true,
                            fields = obj.fields, label = obj.label || 'unknown reason';
                        if (!condition) return; // if condition isn't true, don't bother with anything else
                        if (!$.isArray(fields)) throw new TypeError(self + ': obj.fields must be an array');
                        fields.forEach(function (field) {
                            if (config[field] === void 0) return;
                            throw new TypeError(method + ': ' + field + ' cannot be defined because: ' + label);
                        });
                    });
                };
                /** @ignore */
                var check_required = function (bundle, params) {
                    var method = bundle.method,
                    /** @ignore */
                    exists = function (val) {return !!str(bundle.config[val]);}; // makes sure values are not empty
                    ($.isArray(params) ? params : [params]).forEach(function (obj) {
                        var one_of = obj.one_of, all_of = obj.all_of, either = obj.either, or = obj.or,
                            condition = 'condition' in obj ? obj.condition : true;
                        if (condition && one_of && !one_of.some(exists))
                            throw new TypeError(method + ': one of {' + one_of.join(' | ') + '} must be defined');
                        if (condition && all_of && !all_of.every(exists))
                            throw new TypeError(method + ': {' + all_of.join(' & ') + '} must be defined');
                        if (condition && either && or) {
                            if (either.some(exists) && or.some(exists))
                                throw new TypeError(method + ': only one of {' + either.join(' & ') + '} and {' +
                                    obj.or.join(' & ') + '} can exist');
                            if (!(either.every(exists) || or.every(exists)))
                                throw new TypeError(method + ': either {' + either.join(' & ') + '} or {' +
                                    obj.or.join(' & ') + '} must exist');
                        }
                    });
                };
                return function (params) {
                    [params.bundle, params.bundle.method, params.bundle.config].forEach(function (param) {
                        if (!param) throw new TypeError('check: params.bundle must contain method and config');
                    });
                    if (params.required) check_required(params.bundle, params.required);
                    if (params.empties) check_empties(params.bundle, params.empties);
                    if (params.dependencies) check_dependencies(params.bundle, params.dependencies);
                };
            })(),
            loading_end: function () {/*global ajax loading events end here*/},
            loading_start: function (loading_method) {
                if (loading_method) loading_method();
                /*global ajax loading events start here*/
            },
            str: str
        };
    }
});