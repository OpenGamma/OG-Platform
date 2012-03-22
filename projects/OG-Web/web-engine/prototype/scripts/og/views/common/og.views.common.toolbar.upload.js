/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.toolbar.upload',
    dependencies: ['og.common.util.ui.dialog'],
    obj: function () {
        return function () {
            og.common.util.ui.dialog({
                width: 600, height: 400,
                type: 'input',
                title: 'Import Portfolios',
                custom: '<iframe src="import.ftl" width="580" height="250" marginheight="0" marginwidth="0"\
                         frameborder="0" />',
                buttons: {
                    'OK': function () {$(this).dialog('close');},
                    'Cancel': function () {$(this).dialog('close');}
                }
            })
        }
    }
});