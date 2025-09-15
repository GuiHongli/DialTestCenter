import path from 'path';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default {
  entry: './src/main.tsx',
  output: {
    path: path.resolve(__dirname, '../backend/src/main/resources/static'),
    filename: 'bundle.js',
    clean: true,
    publicPath: '/',
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: {
          loader: 'ts-loader',
          options: {
            transpileOnly: true  // 只转译，不做类型检查
          }
        },
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
    alias:{
      'eview-ui': '@cloudsop/eview-ui', // 新增
      'react': '@cloudsop/horizon', // 新增
      'react-dom/client': '@cloudsop/horizon', // 兼容react18的用法
      'react-dom': '@cloudsop/horizon', // 新增
      'react-is': '@cloudsop/horizon', // 新增
      'react-router-dom': '@cloudsop/horizon-router', // 新增
    }
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './index.html',
      filename: 'index.html',
      inject: true,
    }),
  ],
  devServer: {
    static: {
      directory: path.join(__dirname, '../backend/src/main/resources/static'),
    },
    port: 3000,
    hot: true,
    historyApiFallback: true,
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          const errorMessage = error.message || '';
          if (errorMessage.includes('ResizeObserver loop completed with undelivered notifications')) {
            return false;
          }
          return true;
        },
      },
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
};
