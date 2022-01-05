const path = require('path');

const {DefinePlugin} = require("webpack");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const {VueLoaderPlugin} = require('vue-loader')
const CopyWebpackPlugin = require("copy-webpack-plugin");

module.exports = {
    entry: {
        app: {
            import: './src/main.ts',
            dependOn: ['pixi']
        },
        fontawesome: './src/fontawesome.ts',
        pixi: 'pixi.js',
        axios: 'axios',
    },
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, "dist"),
        publicPath: "/",
        clean: true,
    },
    resolve: {
        extensions: ['.ts', '.js'],
        alias: {
            // 'src': path.resolve(__dirname, 'src')
            // 'vue': '@vue/runtime-dom', // ?????
        }
    },
    plugins: [
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: "assets/game/**"
                }
            ]
        }),
        new VueLoaderPlugin(),
        new HtmlWebpackPlugin({
            title: 'Origin',
            template: "./src/index.html",
            filename: 'index.html'
        }),
        new DefinePlugin({
            __VUE_OPTIONS_API__: true,
            __VUE_PROD_DEVTOOLS__: false,
        })
    ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loader: 'ts-loader',
                options: {
                    appendTsSuffixTo: [/\.vue$/],
                },
                exclude: /node_modules/,
            },
            {
                test: /\.vue$/,
                use: 'vue-loader'
            },
            {
                test: /\.s[ac]ss$/i,
                use: [
                    // Creates `style` nodes from JS strings
                    "style-loader",
                    // Translates CSS into CommonJS
                    "css-loader",
                    // Compiles Sass to CSS
                    "sass-loader"
                ]
            },
            {
                test: /\.(jpe?g|gif|png)$/,
                type: 'asset/resource'
            },
        ]
    },
};
