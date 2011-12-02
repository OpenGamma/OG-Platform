/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.volatilitysurfacespecification',
    dependencies: ['og.common.util.ui'],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form,
            INDX = '<INDEX>', SFIP = 'surfaceInstrumentProvider', UCAS = 'useCallAboveStrikeValue',
            ZPFT = 'zeroPadFirstTenor', ZPST = 'zeroPadSecondTenor';
        return og.views.config_forms['default'].preload({
            type_map: [
                ['0',                                   Form.type.STR],
                ['currency',                            Form.type.STR],
                ['name',                                Form.type.STR],
                [[SFIP, '0'].join('.'),                 Form.type.STR],
                [[SFIP, 'DATA_FIELD_NAME'].join('.'),   Form.type.STR],
                [[SFIP, 'PREFIX'].join('.'),            Form.type.STR],
                [[SFIP, 'POSTFIX'].join('.'),           Form.type.STR],
                [[SFIP, 'type'].join('.'),              Form.type.STR],
                [[SFIP, UCAS].join('.'),                Form.type.DBL],
                [[SFIP, ZPFT].join('.'),                Form.type.BOO],
                [[SFIP, ZPST].join('.'),                Form.type.BOO],
                [['target', '0'].join('.'),             Form.type.STR],
                [['target', '0', INDX].join('.'),       Form.type.STR],
                [['target', 'Scheme'].join('.'),        Form.type.STR],
                [['target', 'Value'].join('.'),         Form.type.STR],
                [['target', 'currency'].join('.'),      Form.type.STR],
                [['target', 'currency1'].join('.'),     Form.type.STR],
                [['target', 'currency2'].join('.'),     Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {})
        });
    }
});