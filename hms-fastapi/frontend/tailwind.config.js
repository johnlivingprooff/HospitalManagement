/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#e8f8fc',
          100: '#d1f1f9',
          200: '#a3e3f3',
          300: '#75d5ed',
          400: '#47c7e7',
          500: '#14b2ba',
          600: '#10929a',
          700: '#0c6d73',
          800: '#08484d',
          900: '#042426',
        },
        teal: {
          light: '#e8f8fc',
          DEFAULT: '#14b2ba',
          dark: '#10929a',
        },
      },
      fontFamily: {
        sans: ['TASA Orbiter', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
