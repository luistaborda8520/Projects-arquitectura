import java.util.ArrayList;
import java.util.List;
 
/**
* ✅ ESTADO: REFACTORIZADO / CLEAN ARCHITECTURE
* Este archivo demuestra la separación de responsabilidades.
* * NOTA DE ARQUITECTURA:
* En un proyecto real, cada clase (class) e interfaz (interface) 
* estaría en su propio archivo .java dentro de paquetes (packages).
* Aquí se han agrupado para facilitar la copia y ejecución.
*/
 
// ==========================================
// CAPA 1: DOMINIO (Entidades y Lógica Pura)
// package com.myapp.domain;
// ==========================================
 
class OrderItem {
    // ✅ ENTIDAD / VALUE OBJECT
    // SRP: Su única responsabilidad es gestionar los datos de una línea de pedido.
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
    // ✅ AGGREGATE ROOT
    private List<OrderItem> items = new ArrayList<>();
    private String status = "PENDING";
 
    // ✅ APLICACIÓN CORRECTA DE CREATOR (GRASP)
    // PREGUNTA CLAVE: ¿Quién crea los OrderItems?
    // RESPUESTA: La clase Order.
    // JUSTIFICACIÓN: Order "agrega" (contiene) a los items y posee la información
    // necesaria para crearlos. Es una composición fuerte.
    public void addItem(String productId, double price, int quantity) {
        // Aquí usamos 'new' porque es una relación de pertenencia directa del dominio.
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
 
// ==========================================
// CAPA 2: INTERFACES (Contratos de Servicio)
// package com.myapp.services;
// ==========================================
 
// ✅ SRP & DIP: Definimos QUÉ se hace, no CÓMO se hace.
interface PaymentProcessor {
    boolean process(double amount, String currency);
}
 
interface NotificationService {
    void sendReceipt(Order order);
}
 
interface OrderRepository {
    void save(Order order);
}
 
// ==========================================
// CAPA 3: INFRAESTRUCTURA (Implementaciones)
// package com.myapp.infrastructure;
// ==========================================
 
class StripePaymentAdapter implements PaymentProcessor {
    @Override
    public boolean process(double amount, String currency) {
        System.out.println("[Infraestructura] Conectando a Stripe API...");
        System.out.println("[Infraestructura] Cobrando $" + amount + " " + currency);
        return true; // Simulación de éxito
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
 
// ==========================================
// CAPA 4: SERVICIO DE APLICACIÓN (Orquestador)
// package com.myapp.application;
// ==========================================
 
class OrderService {
    // ✅ INYECCIÓN DE DEPENDENCIAS
    // SRP: Este servicio solo ORQUESTA el flujo. No sabe de SQL ni de sockets.
    // Creator: NO crea sus dependencias, las pide en el constructor.
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
 
        // 1. Delegación del Pago (Polimorfismo)
        if (paymentProcessor.process(total, "USD")) {
            // 2. Lógica de Dominio
            order.markAsPaid();
            // 3. Persistencia
            repository.save(order);
            // 4. Notificación
            notifier.sendReceipt(order);
            System.out.println("--- Orden Finalizada Exitosamente ---");
        } else {
            System.out.println("--- Error: El pago fue rechazado ---");
        }
    }
}
 
// ==========================================
// MAIN (Composition Root)
// ==========================================
 
public class RefactoredSystem {
    public static void main(String[] args) {
        System.out.println(">>> EJECUTANDO SISTEMA REFACTORIZADO (JAVA) <<<\n");
 
        // 1. CONFIGURACIÓN (Wiring)
        // Aquí decidimos qué implementaciones usar. Podríamos cambiar Stripe por PayPal 
        // simplemente cambiando la instancia aquí, sin tocar la lógica de Order.
        PaymentProcessor payment = new StripePaymentAdapter();
        NotificationService email = new EmailNotifier();
        OrderRepository db = new SqlDatabaseRepo();
 
        // 2. INYECCIÓN
        OrderService service = new OrderService(payment, email, db);
 
        // 3. USO DEL DOMINIO
        Order myOrder = new Order();
        // Creator en acción: Order crea sus items
        myOrder.addItem("Monitor 4K", 300.00, 1); 
        myOrder.addItem("Cable HDMI", 15.00, 2);
 
        // 4. EJECUCIÓN
        service.processOrder(myOrder);
    }
}