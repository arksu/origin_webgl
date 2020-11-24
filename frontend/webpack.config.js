const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const {ProvidePlugin} = require("webpack");
const {VueLoaderPlugin} = require('vue-loader')

module.exports = {
    mode: 'development',
    entry: './src/main.ts',
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
                loader: 'vue-loader'
            },
            {
                test: /\.s[ac]ss$/,
                // test: /\.scss$/,
                use: [
                    {
                        loader: "style-loader" // creates style nodes from JS strings
                    },
                    {
                        loader: "css-loader" // translates CSS into CommonJS
                    },
                    {
                        loader: "sass-loader" // compiles Sass to CSS
                    }
                ]
            },
        ],
    },
    plugins: [
        new VueLoaderPlugin(),
        new HtmlWebpackPlugin({
            hash: true,
            title: 'Origin',
            template: "./src/index.html",
            filename: '../dist/index.html'
        }),
        new ProvidePlugin({
            process: 'process/browser',
        }),
    ],
    resolve: {
        fallback: {
            "crypto": require.resolve("crypto-browserify"),
            "stream": require.resolve("stream-browserify")
        },
        modules: ["node_modules"],
        extensions: [".js", ".ts", ".tsx", ".vue"],
        alias: {
            'vue': '@vue/runtime-dom'
        }
    },
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(path.join(__dirname, "..", "dist")),
    },
    devServer: {
        historyApiFallback: true,
        inline: true,
        hot: true,
        stats: 'minimal',
        compress: true,
        overlay: true
    },
    target: "web"
};