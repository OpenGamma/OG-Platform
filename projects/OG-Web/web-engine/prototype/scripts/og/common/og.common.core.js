/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * This script extends JQuery.
 *
 * register_module is a function that helps manage large javascripts codebases.
 * All scripts should be registered with this function and supply a namespaced name and an obj function.
 * The function can optionally return an init method that will automatically get initialized by this script.
 * This script also allows you to set default methods and properties for all scripts.
 */

(function ($, window, document, undefined) {
    var default_obj = {debug: window.location.hash.split('/').some(function (val) {return val === 'debug=true';})},
        check_dependencies,
        /** @private */
        output = function () {
            if (this.debug && typeof console !== 'undefined' && console.log)
                console.log.apply(console, Array.prototype.slice.call(arguments));
        },
        top_level = window, default_module = {
            live_data_root: '/jax/', html_root: '/prototype/modules/', obj: function () {return default_obj;}
        };
    $.extend(default_obj, {warn: output.partial('[warning]'), log: output});
    /** @private */
    check_dependencies = function (module) {
        if (!$.isArray(module.dependencies)) return default_obj.warn(module.name + ' does not define dependencies');
        module.dependencies.forEach(function (dependency) {
            try {
                dependency.split('.').reduce(function (acc, val) {
                    if (typeof acc[val] === 'undefined') throw new Error; else return acc[val];
                }, top_level);
            } catch (error) {
                throw new ReferenceError(module.name + ' requires ' + dependency);
            }
        });
    };
    $.extend({
        outer: function (node) {
            if (!node) return '';
            var div = document.createElement('div');
            div.appendChild(node);
            return div.innerHTML;
        },
        register_module: function (module) {
            var self = 'register_module', name = module.name, new_module, levels, last, last_parent;
            if (!name) throw new TypeError(self + ': module name is undefined');
            check_dependencies(module);
            new_module = $.extend({}, default_module, module).obj();
            levels = name.split('.');
            // initialize each tier and make sure it's an empty object if not already an object
            levels.reduce(function (acc, val) {return acc[val] || (acc[val] = {});}, top_level);
            // drill down to the second-to-last level
            last = levels.pop();
            last_parent = levels.reduce(function (acc, val) {return acc[val];}, top_level);
            // add module to namespace
            if (last_parent[last].name) throw new Error(self + ': ' + name + ' already exists');
            last_parent[last] = new_module;
            if (typeof new_module.init === 'function') new_module.init();
            delete new_module.init;
        }
    });
})(jQuery, this, document);