
package com.smartcanteen;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws IOException {

        CanteenService canteen = new CanteenService();

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8091), 0
        );

        // Send the food menu to the website
        server.createContext("/api/menu", exchange -> {
            String result = canteen.getMenuAsJson();
            sendResponse(exchange, 200, result, "application/json");
        });

        // Receive an order from the website
        server.createContext("/api/checkout", exchange -> {

            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, 405,
                    "{\"error\":\"Use POST to place an order.\"}",
                    "application/json");
                return;
            }

            try {
                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                String cart = getField(body, "cart");
                String customer = getField(body, "customer");

                if (customer == null || customer.trim().isEmpty()) {
                    customer = "Guest";
                }

                String result = canteen.placeOrder(customer, cart);

                sendResponse(exchange, 200, result, "application/json");

            } catch (Exception e) {
                sendResponse(exchange, 400,
                    "{\"error\":\"Please check your order and try again.\"}",
                    "application/json");
            }
        });

        // Open the website files
        server.createContext("/", exchange -> {
            String page = exchange.getRequestURI().getPath();

            if (page.equals("/")) {
                page = "/index.html";
            }

            Path folder = Paths.get("public").toAbsolutePath().normalize();
            Path file = folder.resolve(page.substring(1)).normalize();

            if (!file.startsWith(folder) || !Files.isRegularFile(file)) {
                sendResponse(exchange, 404, "Page not found", "text/plain");
                return;
            }

            String type = "text/plain";

            if (page.endsWith(".html")) {
                type = "text/html";
            } else if (page.endsWith(".css")) {
                type = "text/css";
            } else if (page.endsWith(".js")) {
                type = "application/javascript";
            }

            byte[] data = Files.readAllBytes(file);

            exchange.getResponseHeaders().set("Content-Type", type);
            exchange.sendResponseHeaders(200, data.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(data);
            }
        });

        server.start();

        System.out.println("BiteBox is running at http://localhost:8091");
        System.out.println("Keep this window open while using the website.");
    }

    // Send a response to the browser
    public static void sendResponse(
        HttpExchange exchange, int status, String message, String type
    ) throws IOException {

        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", type + "; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, data.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }

    // Read a value sent by the website
    public static String getField(String body, String field)
        throws IOException {

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], "UTF-8");

                if (key.equals(field)) {
                    return URLDecoder.decode(parts[1], "UTF-8");
                }
            }
        }

        return "";
    }
}


// Parent class for menu items
class MenuItem {

    private int id;
    private String name;
    private double price;
    private String category;

    public MenuItem(int id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    // Method to calculate the price
    public double calculatePrice(int quantity) {
        return price * quantity;
    }

    public String getType() {
        return "food";
    }
}


// Child class for food
class FoodItem extends MenuItem {

    public FoodItem(int id, String name, double price, String category) {
        super(id, name, price, category);
    }

    @Override
    public double calculatePrice(int quantity) {
        return getPrice() * quantity;
    }
}


// Child class for drinks
class DrinkItem extends MenuItem {

    public DrinkItem(int id, String name, double price, String category) {
        super(id, name, price, category);
    }

    @Override
    public String getType() {
        return "drink";
    }
}


// Handles the canteen menu and orders
class CanteenService {

    private MenuItem[] menu = {
        new FoodItem(1, "Classic Veg Burger", 89, "Burgers"),
        new FoodItem(2, "Crispy Paneer Wrap", 119, "Wraps"),
        new FoodItem(3, "Margherita Pizza", 149, "Pizza"),
        new FoodItem(4, "Masala Fries", 69, "Sides"),
        new FoodItem(5, "Cheese Sandwich", 99, "Sandwiches"),
        new FoodItem(6, "Veg Hakka Noodles", 129, "Bowls"),
        new DrinkItem(7, "Cold Coffee", 79, "Drinks"),
        new DrinkItem(8, "Fresh Lime Soda", 49, "Drinks"),
        new DrinkItem(9, "Iced Lemon Tea", 59, "Drinks"),
        new FoodItem(10, "Chocolate Brownie", 65, "Desserts")
    };

    public String getMenuAsJson() {

        String result = "[";

        for (int i = 0; i < menu.length; i++) {
            MenuItem item = menu[i];

            if (i > 0) {
                result += ",";
            }

            result += "{\"id\":" + item.getId()
                + ",\"name\":\"" + item.getName()
                + "\",\"price\":" + item.getPrice()
                + ",\"category\":\"" + item.getCategory()
                + "\",\"type\":\"" + item.getType() + "\"}";
        }

        result += "]";
        return result;
    }

    public String placeOrder(String customer, String cart)
        throws Exception {

        if (cart == null || cart.isEmpty()) {
            throw new Exception("Your cart is empty.");
        }

        String itemsJson = "";
        double total = 0;

        String[] orders = cart.split(",");

        for (String order : orders) {

            String[] details = order.split(":");

            if (details.length != 2) {
                throw new Exception("Invalid order.");
            }

            int id = Integer.parseInt(details[0]);
            int quantity = Integer.parseInt(details[1]);

            if (quantity < 1 || quantity > 20) {
                throw new Exception("Invalid quantity.");
            }

            MenuItem selectedItem = null;

            for (MenuItem item : menu) {
                if (item.getId() == id) {
                    selectedItem = item;
                    break;
                }
            }

            if (selectedItem == null) {
                throw new Exception("Menu item not found.");
            }

            double amount = selectedItem.calculatePrice(quantity);
            total += amount;

            if (!itemsJson.isEmpty()) {
                itemsJson += ",";
            }

            itemsJson += "{\"name\":\"" + selectedItem.getName()
                + "\",\"quantity\":" + quantity
                + ",\"amount\":" + amount + "}";
        }

        String orderId = "BB" + System.currentTimeMillis();

        return "{\"orderId\":\"" + orderId
            + "\",\"customer\":\"" + customer
            + "\",\"items\":[" + itemsJson
            + "],\"total\":" + total + "}";
    }
}
