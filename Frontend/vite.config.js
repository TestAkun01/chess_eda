import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  server: {
    allowedHosts: ["www.holy-grails-war.test"],
  },
  plugins: [react(), tailwindcss()],
  define: {
    global: "globalThis", // 💡 ini baris penting
  },
});
