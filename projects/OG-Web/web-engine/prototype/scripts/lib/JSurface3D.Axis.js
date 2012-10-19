/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function () {
    if (!window.JSurface3D) throw new Error('JSurface3D.Axes requires JSurface3D');
    /**
     * Creates both axes
     * @return {THREE.Object3D}
     */
    window.JSurface3D.Axes = function (js3d) {
        var axes = new THREE.Object3D, x_axis, z_axis, settings = js3d.settings, data = js3d.data,
            x = {axis: 'x', spacing: js3d.adjusted_xs, labels: data.xs_labels, values: data.xs, label: data.xs_label},
            z = {axis: 'z', spacing: js3d.adjusted_zs, labels: data.zs_labels, values: data.zs, label: data.zs_label};
        /**
         * Creates an Axis with labels for the bottom grid
         * @param {Object} config
         * config.axis {String} x or z
         * config.spacing {Array} Array of numbers adjusted to fit units of mesh
         * config.labels {Array} Array of lables
         * config.label {String} Axis label
         * @return {THREE.Object3D}
         */
        var create_axis = function (config) {
            var mesh = new THREE.Object3D(), i, nth = Math.ceil(config.spacing.length / 6),
                lbl_arr = thin(config.values, nth), pos_arr = thin(config.spacing, nth),
                axis_len = settings['surface_' + config.axis];
            (function () { // axis values
                var value, n;
                for (i = 0; i < lbl_arr.length; i++) {
                    n = lbl_arr[i];
                    n = n % 1 === 0 ? n : (+n).toFixed(settings.precision_lbl).replace(/0+$/, '');
                    value = js3d.text3d(n+'', settings.font_color);
                    value.scale.set(0.1, 0.1, 0.1);
                    if (config.axis === 'y') {
                        value.position.x = pos_arr[i] - 6;
                        value.position.z = config.right
                            ? -(THREE.FontUtils.drawText(n).offset * 0.2) + 18
                            : 2;
                        value.rotation.z = -Math.PI * 0.5;
                        value.rotation.x = -Math.PI * 0.5;
                    } else {
                        value.rotation.x = -Math.PI * 0.5;
                        value.position.x = pos_arr[i] - ((THREE.FontUtils.drawText(n).offset * 0.2) / 2);
                        value.position.y = 0.1;
                        value.position.z = 12;
                    }
                    value.matrixAutoUpdate = false;
                    value.updateMatrix();
                    mesh.add(value);
                }
            }());
            (function () { // axis label
                if (!config.label) return;
                var label = js3d.text3d(config.label, settings.font_color_axis_labels, true, true);
                label.scale.set(0.2, 0.2, 0.1);
                label.rotation.x = -Math.PI * 0.5;
                label.position.x = -(axis_len / 2) -3;
                label.position.y = 1;
                label.position.z = 25;
                label.matrixAutoUpdate = false;
                label.updateMatrix();
                mesh.add(label);
            }());
            (function () { // axis ticks
                var canvas = document.createElement('canvas'),
                    ctx = canvas.getContext('2d'),
                    plane = new THREE.PlaneGeometry(axis_len, 5, 0, 0),
                    axis = new THREE.Mesh(plane, js3d.matlib.texture(js3d.buffers.surface.add(new THREE.Texture(canvas)))),
                    tick_stop_pos =  settings.tick_length + 0.5,
                    labels = thin(config.spacing.map(function (val) {
                        // if not y axis offset half. y planes start at 0, x and z start at minus half width
                        var offset = config.axis === 'y' ? 0 : axis_len / 2;
                        return (val + offset) * (settings.texture_size / axis_len);
                    }), nth);
                canvas.width = settings.texture_size;
                canvas.height = 32;
                ctx.beginPath();
                ctx.lineWidth = 2;
                for (i = 0; i < labels.length; i++)
                    ctx.moveTo(labels[i] + 0.5, tick_stop_pos), ctx.lineTo(labels[i] + 0.5, 0);
                ctx.moveTo(0.5, tick_stop_pos);
                ctx.lineTo(0.5, 0.5);
                ctx.lineTo(canvas.width - 0.5, 0.5);
                ctx.lineTo(canvas.width - 0.5, tick_stop_pos);
                ctx.stroke();
                axis.material.map.needsUpdate = true;
                axis.doubleSided = true;
                if (config.axis === 'y') {
                    if (config.right) axis.rotation.x = Math.PI, axis.position.z = 20;
                    axis.position.x = (axis_len / 2) - 4;
                } else {
                    axis.position.z = 5;
                }
                axis.matrixAutoUpdate = false;
                axis.updateMatrix();
                mesh.add(axis);
            }());
            return mesh;
        };
        /**
         * Remove every nth item in Array keeping the first and last,
         * also spesificaly remove the second last (as we want to keep the last)
         * @param {Array} arr
         * @param {Number} nth
         * @returns {Array}
         */
        var thin = function (arr, nth) {
            if (!nth || nth === 1) return arr;
            var len = arr.length;
            return arr.filter(function (val, i) {
                return ((i === 0) || !(i % nth) || (i === (len -1))) || (i === (len -2)) && false
            });
        };
        x_axis = create_axis(x);
        z_axis = create_axis(z);
        x_axis.position.z = settings.surface_z / 2 + settings.axis_offset;
        z_axis.position.x = -settings.surface_x / 2 - settings.axis_offset;
        z_axis.rotation.y = -Math.PI * 0.5;
        axes.add(x_axis);
        axes.add(z_axis);
        return axes;
    };
})();