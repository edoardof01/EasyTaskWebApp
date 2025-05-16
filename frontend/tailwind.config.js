/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"  // Inclusi tutti i file HTML e TypeScript
  ],
  theme: {
    extend: {
      fontFamily: {
        baloo: ['Baloo', 'sans-serif'], // Aggiunta del font Baloo
      },
    },
  },
  plugins: [],
};
