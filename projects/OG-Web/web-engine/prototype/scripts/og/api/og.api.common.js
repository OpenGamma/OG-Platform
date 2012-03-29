/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
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
                    },
                    /** @ignore */
                    check_empties = function (bundle, params) { // if condition then fields can't exist, optional label
                        var config = bundle.config, method = bundle.method, self = 'check_empties';
                        ($.isArray(params) ? params : [params]).forEach(function (obj) {
                            var condition = obj.condition, fields = obj.fields, label = obj.label || 'unknown reason';
                            if (!condition) return; // if condition isn't true, don't bother with anything else
                            if (!$.isArray(fields)) throw new TypeError(self + ': obj.fields must be an array');
                            fields.forEach(function (field) {
                                if (config[field] === void 0) return;
                                throw new TypeError(method + ': ' + field + ' cannot be defined because: ' + label);
                            });
                        });
                    },
                    /** @ignore */
                    check_required = function (bundle, params) {
                        var method = bundle.method,
                        /** @ignore */
                        exists = function (val) {return !!str(bundle.config[val]);}; // makes sure values are not empty
                        ($.isArray(params) ? params : [params]).forEach(function (obj) {
                            var one_of = obj.one_of, all_of = obj.all_of,
                                condition = 'condition' in obj ? obj.condition : true;
                            if (condition && one_of && !one_of.some(exists))
                                throw new TypeError(method + ': one of {' + one_of.join(' | ') + '} must be defined');
                            if (condition && all_of && !all_of.every(exists))
                                throw new TypeError(method + ': {' + all_of.join(' & ') + '} must be defined');
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
            end_loading: function () {/*global ajax loading events end here*/},
            start_loading: function (loading_method) {
                if (loading_method) loading_method();
                /*global ajax loading events start here*/
            },
            clear_cache: function (prefix) {
                try { // if cache is restricted, bail
                    if (!prefix) return cache.clear();
                    Object.keys(cache).forEach(function (key) {
                        if (key.indexOf(prefix) === 0) common.del_cache(key);
                        });
                } catch (error) {
                    return warn(module.name + ': clear_cache failed\n', error);
                }
            },
            get_cache: function (key) {
                try { // if cache is restricted, bail
                    return cache['getItem'](key) ? JSON.parse(cache['getItem'](key)) : null;
                } catch (error) {
                    warn(module.name + ': get_cache failed\n', error);
                    return null;
                }
            },
            set_cache: function (key, value) {
                try { // if the cache is too full, fail gracefully
                    cache['setItem'](key, JSON.stringify(value));
                } catch (error) {
                    warn(module.name + ': set_cache failed\n', error);
                    del_cache(key);
                }
            },
            del_cache: function (key) {
                try { // if cache is restricted, bail
                    cache['removeItem'](key);
                } catch (error) {
                    warn(module.name + ': del_cache failed\n', error);
                }
            },
            str: str
        };
    }
});