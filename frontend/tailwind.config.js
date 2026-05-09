/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            boxShadow: {
                'soft': '0 2px 8px rgba(0,0,0,0.05), 0 4px 12px rgba(0,0,0,0.03)',
                'card': '0 10px 25px -5px rgba(0,0,0,0.05), 0 8px 10px -6px rgba(0,0,0,0.02)',
                'card-hover': '0 20px 35px -12px rgba(0,0,0,0.1)',
            },
        },
    },
    plugins: [],
}