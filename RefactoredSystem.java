import java.util.ArrayList;
import java.util.List;


class OrderItem {
    private String productId;
    private double price;
    private int quantity;
 
    public OrderItem(String productId, double price, int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }
 
    public double getSubtotal() {
        return this.price * this.quantity;
    }
}
 
class Order {
    private List<OrderItem> items = new ArrayList<>();
    private String status = "PENDING";
 
    public void addItem(String productId, double price, int quantity) {
        OrderItem newItem = new OrderItem(productId, price, quantity);
        this.items.add(newItem);
    }
 
    public double getTotal() {
        return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }
 
    public void markAsPaid() {
        this.status = "PAID";
    }
    public String getStatus() { return status; }
}
 
interface PaymentProcessor {
    boolean process(double amount, String currency);
}
 
interface NotificationService {
    void sendReceipt(Order order);
}
 
interface OrderRepository {
    void save(Order order);
}
 
 
class StripePaymentAdapter implements PaymentProcessor {
    @Override
    public boolean process(double amount, String currency) {
        System.out.println("[Infraestructura] Conectando a Stripe API...");
        System.out.println("[Infraestructura] Cobrando $" + amount + " " + currency);
        return true;
    }
}
 
class EmailNotifier implements NotificationService {
    @Override
    public void sendReceipt(Order order) {
        System.out.println("[Infraestructura] Conectando a SMTP Server...");
        System.out.println("[Infraestructura] Enviando recibo por total: $" + order.getTotal());
    }
}
 
class SqlDatabaseRepo implements OrderRepository {
    @Override
    public void save(Order order) {
        System.out.println("[Infraestructura] Generando SQL: INSERT INTO orders...");
        System.out.println("[Infraestructura] Guardando orden con estado: " + order.getStatus());
    }
}
 
class OrderService {
    private PaymentProcessor paymentProcessor;
    private NotificationService notifier;
    private OrderRepository repository;
 
    public OrderService(PaymentProcessor paymentProcessor, 
                        NotificationService notifier, 
                        OrderRepository repository) {
        this.paymentProcessor = paymentProcessor;
        this.notifier = notifier;
        this.repository = repository;
    }
 
    public void processOrder(Order order) {
        System.out.println("--- Iniciando Servicio de Orden ---");
        double total = order.getTotal();
 
        if (paymentProcessor.process(total, "USD")) {
            order.markAsPaid();
            repository.save(order);
            notifier.sendReceipt(order);
            System.out.println("--- Orden Finalizada Exitosamente ---");
        } else {
            System.out.println("--- Error: El pago fue rechazado ---");
        }
    }
}
 
 
public class RefactoredSystem {
    public static void main(String[] args) {
        System.out.println(">>> EJECUTANDO SISTEMA REFACTORIZADO (JAVA) <<<\n");
 
        PaymentProcessor payment = new StripePaymentAdapter();
        NotificationService email = new EmailNotifier();
        OrderRepository db = new SqlDatabaseRepo();
 
        OrderService service = new OrderService(payment, email, db);
 
        Order myOrder = new Order();
        myOrder.addItem("Monitor 4K", 300.00, 1); 
        myOrder.addItem("Cable HDMI", 15.00, 2);
 
        service.processOrder(myOrder);
    }
}