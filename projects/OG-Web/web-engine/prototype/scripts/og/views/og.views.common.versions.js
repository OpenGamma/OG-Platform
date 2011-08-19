/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.versions',
    dependencies: ['og.common.util.ui.message'],
    obj: function () {
        return function () {
            var cur = og.common.routes.current(), ui = og.common.util.ui;
            og.api.rest[cur.page.substring(1)].get({
                id: cur.args.id, version: '*',
                handler: function (r) {
                    var list = r.data.data.reduce(function (pre, cur) {
                        return pre + '<li>' + cur + '</li>';
                    }, '<ul>') + '</ul>';
                    $('.ui-layout-inner-south .ui-layout-content').html(list);
                    ui.message({location: '.ui-layout-inner-south', destroy: true});
                },
                loading: function () {
                    ui.message({
                        location: '.ui-layout-inner-south',
                        message: {0: 'loading...', 3000: 'still loading...'}
                    });
                }
            });
        }
    }
});