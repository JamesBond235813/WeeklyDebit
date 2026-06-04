import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {
      devtools: false,
    },
    vite: {
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // mock代理目标地址
            // target: 'http://localhost:5320/api',
            target: 'http://localhost:32800/api',
            ws: true,
          },
        },
      },
    },
  };
});
