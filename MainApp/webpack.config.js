const path = require('path');
const Dotenv = require('dotenv-webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');

console.log("🚀 Webpack is running in:", process.env.NODE_ENV);

module.exports = {
    entry: './src/app.js', // Entry point
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist'), // Output directory
    },
    resolve: {
        alias: {
            jsModules: path.resolve(__dirname, '../jsModules/src'), // Ensure Webpack finds external modules
        },
        modules: [
            path.resolve(__dirname, 'node_modules'), // Ensure Webpack can find node_modules
            'node_modules',
        ],
        extensions: ['.js'], // Allow importing JS files without specifying `.js`
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                },
            },
        ],
    },
    mode: 'development',
    devServer: {
        static: './dist', // Folder where your bundle.js is located
        port: 4000, // Optional: Specify the port
        open: true, // Automatically open the browser
    },
    plugins: [
        new Dotenv({
            path: `./.env.${process.env.NODE_ENV}`, // Loads the correct .env file
        }),
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: '**/*.html', // Match all HTML files
                    context: path.resolve(__dirname, 'src'), // Base directory for matching
                    to: path.resolve(__dirname, 'dist'), // Output to `dist` while preserving structure
                },
            ],
        }),
    ],
};
