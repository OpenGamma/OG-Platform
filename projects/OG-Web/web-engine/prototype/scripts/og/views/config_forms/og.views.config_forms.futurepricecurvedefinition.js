/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.futurepricecurvedefinition',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form,
            INDX = '<INDEX>';
        return og.views.config_forms['default'].preload({
            type_map: [
                ['0', Form.type.STR],
                ['name', Form.type.STR],
                [['target', '0', INDX].join('.'), Form.type.STR],
                [['target', 'currency'].join('.'), Form.type.STR],
                [['xs', INDX].join('.'), Form.type.BYT]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {})
        });
    }
});