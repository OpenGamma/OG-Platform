/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.toolbar.upload',
    dependencies: [],
    obj: function () {
        return function () {
            ui.dialog({
                width: 400, height: 190,
                type: 'input',
                title: 'Import Portfolios',
                custom: '<div>test</div>',
                buttons: {
                    'OK': function () {$(this).dialog('close');},
                    'Cancel': function () {$(this).dialog('close');}
                }
            })
        }
    }
});