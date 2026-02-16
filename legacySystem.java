import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
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
 
 
class Order {
    private List<Map<String, Object>> items = new ArrayList<>();
    private DatabaseConnection db;
    private SMTPClient mailer;
    private StripeSDK payment;
 
    public Order() {
        this.db = new DatabaseConnection("localhost", "root", "secret123");
        this.mailer = new SMTPClient("smtp.gmail.com", 587);
        this.payment = new StripeSDK("sk_test_12345");
    }
 
    public void addItem(String name, double price, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Cantidad inválida");
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("qty", qty);
        item.put("total", price * qty);
        this.items.add(item);
    }
 
    public boolean checkout(String creditCard, String email) {
        double total = 0;
        for (Map<String, Object> item : items) {
            total += (double) item.get("total");
        }
 
        System.out.println("--- Procesando Pago ---");
        try {
            boolean success = this.payment.charge(total, creditCard);
            if (!success) throw new RuntimeException("Pago fallido");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
 
        System.out.println("--- Guardando en BD ---");
        this.db.execute("INSERT INTO orders VALUES (" + total + ", '" + email + "')");
 
        System.out.println("--- Enviando Email ---");
        this.mailer.send(email, "Recibo de Compra", "Total pagado: " + total);
 
        return true;
    }
}
 
public class LegacySystem {
    public static void main(String[] args) {
        System.out.println(">>> EJECUTANDO SISTEMA LEGACY <<<");
        Order order = new Order();
        order.addItem("Teclado Mecánico", 150.00, 1);
        order.addItem("Mouse Gamer", 50.00, 2);
        order.checkout("4242424242424242", "cliente@email.com");
    }
}
