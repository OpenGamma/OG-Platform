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
                width: 600, height: 370,
                type: 'input',
                title: 'Import Portfolio',
                custom: '<iframe id="import" src="import.ftl" width="500" height="220" marginheight="0" marginwidth="0"\
                         frameborder="0" />',
                buttons: {
                    'Start Import': function () {
                        $('#import').contents().find('form').submit();
                        $(this).dialog('option', 'buttons', {'Close': function () {$(this).dialog('close');}});
                    },
                    'Cancel': function () {$(this).dialog('close');}
                }
            })
        }
    }
});