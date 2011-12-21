/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.currencypairs',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form,
            INDX = '<INDEX>',
            type_map = [
                ['0',                           Form.type.STR],
                ['*.*',                         Form.type.STR],
                [['*', '*', INDX].join('.'),    Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {}),
            constructor;
        constructor = og.views.config_forms['default'].preload({type_map: type_map});
        constructor.type_map = type_map;
        return constructor;
    }
});