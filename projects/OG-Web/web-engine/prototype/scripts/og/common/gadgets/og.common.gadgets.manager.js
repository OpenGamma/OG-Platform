/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.manager',
    dependencies: [],
    obj: function () {
        var manager, collector, gadgets = [], interval = 5 * 60 * 1000, layout, first = true,
            resize = function () {
                return layout && first ? first = false : manager.clean().forEach(function (gadget) {gadget.resize();});
            };
        setTimeout((collector = function () {return manager.clean(), setTimeout(collector, interval);}), interval);
        $(function () {if (!(layout = !!og.views.common.layout)) $(window).on('resize', manager.resize);});
        return manager = {
            clean: function () {return gadgets = gadgets.filter(function (gadget) {return gadget.alive();});},
            gadgets: function () {return gadgets;},
            register: function (gadget) {manager.clean().push(gadget);},
            resize: (function (timeout) {
                return function () {timeout = clearTimeout(timeout) || setTimeout(resize, layout ? 0 : 200);};
            })(null)
        };
    }
});