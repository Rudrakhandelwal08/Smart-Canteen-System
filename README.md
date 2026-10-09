# BiteBox — Smart Canteen Ordering System

A student-friendly canteen ordering website built with **HTML, CSS, JavaScript and Core Java**. The Java backend runs locally using the JDK's built-in HTTP server, so there are no external Java libraries or database setup steps.

## Features
- Responsive website layout for desktop and mobile
- Menu served by the Java backend
- Search and category filters
- Add items, change quantities, and see the running subtotal
- Checkout with a customer name and order confirmation
- Server-side quantity and menu validation
- A text receipt saved to `receipts/` for each successful order
- OOP design with abstraction, inheritance, method overriding, encapsulation, constructors, polymorphism, arrays of objects, and a custom checked exception

> This is a college demonstration project. It does not collect payment or connect to a real canteen POS system.

## Requirements
- JDK 17 or newer (JDK 11 may also work with the features used here)
- A modern browser

Check Java is installed by running `java -version` and `javac -version` in a terminal.

## Run on Windows
1. Download or clone this repository.
2. Double-click `run.bat`.
3. Wait for the message `Smart Canteen is running at http://localhost:8091`.
4. Open <http://localhost:8091> in your browser.
5. Keep the terminal window open while you use the website. Press `Ctrl+C` in it to stop the server.

## Run on macOS / Linux
From the project folder:

```bash
chmod +x run.sh
./run.sh
```

Then open <http://localhost:8091>.

## Project structure

```text
smart-canteen-ordering-system/
├── public/
│   ├── index.html       # Website layout
│   ├── styles.css       # Responsive design
│   └── app.js           # Search, filters, cart, checkout UI
├── src/com/smartcanteen/
│   └── Main.java        # Java server and OOP model/service classes
├── docs/
│   ├── OOP-NOTES.md
│   ├── TEST-CASES.md
│   └── VIVA-QUESTIONS.md
├── run.bat
├── run.sh
└── README.md
```

## How it works
The browser requests `GET /api/menu` to load menu data from Java. When the customer checks out, the browser sends a form-encoded cart to `POST /api/checkout`. Java validates the order, calculates prices on the server, creates an order number, and writes a receipt file.

## GitHub submission
Create a **public** repository named `smart-canteen-ordering-system` on GitHub and upload the contents of this project folder. Copy the repository URL (it will look like `https://github.com/YOUR-USERNAME/smart-canteen-ordering-system`) into the Google Classroom sheet. Replace `YOUR-USERNAME` with your real GitHub username; that example is not a live repository link.

If you want other people to open the website through a public URL, this local Java server must also be deployed to a Java-capable hosting service. GitHub Pages alone can host the static frontend but cannot run this Java backend.
