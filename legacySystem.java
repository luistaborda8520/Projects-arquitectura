import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
* ❌ ESTADO: LEGACY 
* Este archivo simula un sistema monolítico donde todo está acoplado.
*/
 
// --- SIMULACIÓN DE LIBRERÍAS EXTERNAS (Drivers, SDKs) ---
// En un proyecto real, estas clases vendrían de archivos .jar o Maven.
 
class DatabaseConnection {
    public DatabaseConnection(String host, String user, String pass) {
        System.out.println("[DB Driver] Conectando a " + host + "...");
    }
    public void execute(String sql) {
        System.out.println("[DB Driver] Ejecutando SQL: " + sql);
    }
}
 
class SMTPClient {
    public SMTPClient(String server, int port) {
        System.out.println("[Email Client] Conectando a SMTP " + server + ":" + port);
    }
    public void send(String to, String subject, String body) {
        System.out.println("[Email Client] Enviando correo a " + to + ": " + subject);
    }
}
 
class StripeSDK {
    public StripeSDK(String apiKey) {
        System.out.println("[Stripe SDK] Inicializando pasarela...");
    }
    public boolean charge(double amount, String cardNum) {
        System.out.println("[Stripe SDK] Cobrando $" + amount + " a tarjeta *" + cardNum.substring(cardNum.length() - 4));
        return true;
    }
}
 
// --- CLASE PROBLEMÁTICA (GOD CLASS) ---
 
class Order {
    // Usamos Map<String, Object> simulando una estructura de datos pobre/anémica
    private List<Map<String, Object>> items = new ArrayList<>();
    // Dependencias directas (Tight Coupling)
    private DatabaseConnection db;
    private SMTPClient mailer;
    private StripeSDK payment;
 
    public Order() {
        // 💀 VIOLACIÓN CREATOR (GRASP) & DIP (SOLID)
        // PROBLEMA: La clase Order está creando ("new") sus propias dependencias de infraestructura.
        // CONSECUENCIA: 
        // 1. Order sabe demasiado (conoce passwords, puertos, IPs).
        // 2. Es imposible de testear unitariamente (no se pueden burlar/mockear la DB o Stripe).
        // 3. Si cambia la DB, hay que recompilar la clase Order.
        this.db = new DatabaseConnection("localhost", "root", "secret123");
        this.mailer = new SMTPClient("smtp.gmail.com", 587);
        this.payment = new StripeSDK("sk_test_12345");
    }
 
    // 💀 VIOLACIÓN CREATOR (Parcial - Mala implementación)
    // Recibe primitivos y crea estructuras de datos genéricas sin comportamiento.
    public void addItem(String name, double price, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Cantidad inválida");
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("qty", qty);
        item.put("total", price * qty); // Lógica de negocio dispersa
        this.items.add(item);
    }
 
    // 💀 VIOLACIÓN SRP (SINGLE RESPONSIBILITY PRINCIPLE)
    // El "Método Dios". Hace cálculo, pagos, base de datos y correos.
    public boolean checkout(String creditCard, String email) {
        // 1. Lógica de Negocio
        double total = 0;
        for (Map<String, Object> item : items) {
            total += (double) item.get("total");
        }
 
        // 2. Pagos (Infraestructura)
        System.out.println("--- Procesando Pago ---");
        try {
            boolean success = this.payment.charge(total, creditCard);
            if (!success) throw new RuntimeException("Pago fallido");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
 
        // 3. Persistencia (SQL)
        System.out.println("--- Guardando en BD ---");
        // SQL Injection potential + Código SQL hardcodeado en Java
        this.db.execute("INSERT INTO orders VALUES (" + total + ", '" + email + "')");
 
        // 4. Notificación
        System.out.println("--- Enviando Email ---");
        this.mailer.send(email, "Recibo de Compra", "Total pagado: " + total);
 
        return true;
    }
}
 
// --- MAIN (Para ejecutar el ejemplo) ---
public class LegacySystem {
    public static void main(String[] args) {
        System.out.println(">>> EJECUTANDO SISTEMA LEGACY <<<");
        Order order = new Order();
        order.addItem("Teclado Mecánico", 150.00, 1);
        order.addItem("Mouse Gamer", 50.00, 2);
        order.checkout("4242424242424242", "cliente@email.com");
    }
}
