/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.default_details',
    dependencies: [
        'og.api.text',
        'og.views.common.layout',
        'og.common.util.ui.toolbar',
        'og.common.util.history'
    ],
    obj: function () {
        return function (page_name, title, options, handler) {
            og.api.text({module: 'og.views.default', handler: function (template) {
                var content, header, layout = og.views.common.layout,
                    $html = $.tmpl(template, {
                    name: title,
                    recent_list: og.common.util.history.get_html('history.' + page_name + '.recent') ||
                        'no recently viewed ' + page_name
                });
                header = $.outer($html.find('> header')[0]);
                content = $.outer($html.find('> section')[0]);
                $('.ui-layout-inner-center .ui-layout-header').html(header);
                $('.ui-layout-inner-center .ui-layout-content').html(content);
                layout.inner.options.south.onclose = null;
                layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                layout.inner.close('south'), $('.ui-layout-inner-south').empty();
                if (!og.views.common.layout.inner.state.south.isClosed) {og.views.common.versions.clear()}
                // if options are not passed in, do not create a toolbar
                if (options) og.common.util.ui.toolbar(options.toolbar['default']);
                layout.inner.resizeAll();
                if (handler) handler();
            }});
        }
    }
});