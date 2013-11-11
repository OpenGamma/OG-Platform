/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.common',
    dependencies: [],
    obj: function () {
        var module = this, common, warn = og.dev.warn, MAX_INT = Math.pow(2, 31) - 1, PAGE = 1, PAGE_SIZE = 50,
            request_id = 1, has = 'hasOwnProperty', cache = window['sessionStorage'];
        /** @ignore */
        var check_dependencies = function (bundle, dependencies) {
            var config = bundle.config, method = bundle.method, self = 'check_dependencies';
            (($.isArray(dependencies) ? dependencies : [dependencies])).forEach(function (dependency) {
                if (!dependency.require || !dependency.fields)
                    throw new TypeError(self + ': each dependency must have "fields" and "require"');
                if (config[has](dependency.require)) return;
                dependency.fields.forEach(function (field) {
                    if (config[has](field))
                        throw new ReferenceError(method + ': ' + field + ' require(s) ' + dependency.require);
                });
            });
        };
        /** @ignore */
        var check_empties = function (bundle, params) { // if condition then fields can't exist, optional label
            var config = bundle.config, method = bundle.method, self = 'check_empties';
            ($.isArray(params) ? params : [params]).forEach(function (obj) {
                var condition = obj[has]('condition') ? obj.condition : true,
                    fields = obj.fields, label = obj.label || 'unknown reason';
                if (!condition) return; // if condition isn't true, don't bother with anything else
                if (!$.isArray(fields)) throw new TypeError(self + ': obj.fields must be an array');
                fields.forEach(function (field) {
                    if (config[has](field))
                        throw new TypeError(method + ': ' + field + ' cannot be defined because: ' + label);
                });
            });
        };
        /** @ignore */
        var check_required = function (bundle, params) {
            var method = bundle.method;
            /** @ignore */
            var exists = function (val) {return !!str(bundle.config[val]); }; // makes sure values are not empty
            ($.isArray(params) ? params : [params]).forEach(function (obj) {
                var one_of = obj.one_of, all_of = obj.all_of, either = obj.either, or = obj.or,
                    condition = obj[has]('condition') ? obj.condition : true;
                if (!condition) return;
                if (one_of && !one_of.some(exists))
                    throw new TypeError(method + ': one of {' + one_of.join(' | ') + '} must be defined');
                if (all_of && !all_of.every(exists))
                    throw new TypeError(method + ': {' + all_of.join(' & ') + '} must be defined');
                if (!(either && or)) return;
                if (either.some(exists) && or.some(exists))
                    throw new TypeError(method + ': only one of {' + either.join(' & ') + '} and {' +
                        obj.or.join(' & ') + '} can exist');
                if (!(either.every(exists) || or.every(exists)))
                    throw new TypeError(method + ': either {' + either.join(' & ') + '} or {' +
                        obj.or.join(' & ') + '} must exist');
            });
        };
        var not_available = function (method) {
            throw new Error(this.root + '#' + method + ' does not exist in the REST API');
        };
        var not_implemented = function (method) {
            throw new Error(this.root + '#' + method + ' exists in the REST API, but does not have a JS version');
        };
        var str = function (val) { // convert all incoming params into strings (eg, 0 ought to be truthy, not falsey)
            return val === void 0 ? ''
                : $.isArray(val) ? val.join('\n')
                    : typeof val === 'object' ? JSON.stringify(val)
                        : '' + val;
        };
        common = {
            cache_clear: function (prefix) {
                try { // if cache is restricted, bail
                    if (!prefix) return cache.clear();
                    Object.keys(cache).forEach(function (key) {if (key.indexOf(prefix) === 0) common.cache_del(key);});
                } catch (error) {
                    return warn(module.name + ': cache_clear failed\n', error);
                }
            },
            cache_del: function (key) {
                // if cache is restricted, bail
                try {cache['removeItem'](key);} catch (error) {warn(module.name + ': cache_del failed\n', error);}
            },
            cache_get: function (key) {
                // if cache is restricted, bail
                try {return cache['getItem'](key) ? JSON.parse(cache['getItem'](key)) : null;}
                catch (error) {return warn(module.name + ': cache_get failed\n', error), null;}
            },
            cache_set: function (key, value) {
                // if the cache is too full, fail gracefully
                try {cache['setItem'](key, JSON.stringify(value));}
                catch (error) {
                    warn(module.name + ': cache_set failed\n', error);
                    common.cache_del(key);
                }
            },
            check: function (params) {
                [params.bundle, params.bundle.method, params.bundle.config].forEach(function (param) {
                    if (!param) throw new TypeError('check: params.bundle must contain method and config');
                });
                if (params.required) check_required(params.bundle, params.required);
                if (params.empties) check_empties(params.bundle, params.empties);
                if (params.dependencies) check_dependencies(params.bundle, params.dependencies);
                if (typeof params.bundle.config.handler !== 'function') params.bundle.config.handler = $.noop;
                if (params.bundle.config.page && (params.bundle.config.from || params.bundle.config.to))
                    throw new TypeError(params.bundle.method + ': config.page + config.from/to is ambiguous');
                if (str(params.bundle.config.to) && !str(params.bundle.config.from))
                    throw new TypeError(params.bundle.method + ': config.to requires config.from');
                if (params.bundle.config.page_size === '*' || params.bundle.config.page === '*')
                    params.bundle.config.page_size = MAX_INT, params.bundle.config.page = common.PAGE;
                return ['handler', 'loading', 'update', 'dependencies', 'cache_for', 'dry']
                    .reduce(function (acc, val) {
                        return (params.bundle.config[has](val)) && (acc[val] = params.bundle.config[val]), acc;
                    }, {type: 'GET'});
            },
            INSTANT: 0, /* 0ms */
            loading_end: function () {/*global ajax loading events end here*/},
            loading_start: function (loading_method) {
                if (loading_method) loading_method();
                /*global ajax loading events start here*/
            },
            paginate: function (config) {
                var from = str(config.from), to = str(config.to);
                return from ? {'pgIdx': from, 'pgSze': to ? +to - +from : str(config.page_size) || PAGE_SIZE}
                    : {'pgSze': str(config.page_size) || PAGE_SIZE, 'pgNum': str(config.page) || PAGE};
            },
            Promise: function () {
                var deferred = new $.Deferred, promise = deferred.promise();
                promise.abort = function () {return og.api.rest.abort(promise), promise;};
                promise.deferred = deferred;
                promise.id = ++request_id;
                return promise;
            },
            request_expired: function (request, current) {
                return (current.page !== request.current.page) || request.dependencies.some(function (field) {
                    return current.args[field] !== request.current.args[field];
                });
            },
            STALL: 500, /* 500ms */
            str: str
        };
        ['del', 'get', 'put'].forEach(function (val) {
            common['not_available_' + val] = not_available.partial(val);
            common['not_implemented_' + val] = not_implemented.partial(val);
        });
        return common;
    }
});