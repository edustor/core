var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var path = require('path');


module.exports = {
    context: __dirname + "/src/frontend/es6",
    entry: {
        main: "./index"
    },
    output: {
        path: __dirname + "/static",
        filename: "[name].js"
    },
    module: {
        loaders: [{
            test: /\.jsx?$/,
            exclude: /(node_modules|bower_components)/,
            loader: 'babel', // 'babel-loader' is also a legal name to reference
            query: {
                presets: ['react', 'es2015', 'stage-1']
            }
        },
            {test: /\.(jpg|png|otf|svg)$/, loader: "file-loader"},
            {test: /\.styl$/, loader: 'style-loader!css-loader!stylus-loader'},
            {test: /\.css/, loader: 'style-loader!css-loader'},
            {test: /\.html$/, loader: 'html-loader?attrs=source:src'}
        ]
    },
    plugins: [
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                warnings: false
            },
            mangle: false,
            comments: false
        }),
        new HtmlWebpackPlugin({
            template: "index.html",
            inject: true
        })
    ],
    resolve: {
        alias: {
            styles: path.resolve(__dirname, "styles"),
            views: path.resolve(__dirname, "views")
            // Somehow this doesn't work with css/styl loader.
            //assets: path.resolve(__dirname, "frontend/assets")
        },
        extensions: ['', '.js', 'jsx', '.styl']
    },
    devtool: "source-map"
};