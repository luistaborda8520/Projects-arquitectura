from abc import ABC, abstractmethod
from typing import List
 
# ✅ ARCHIVO: refactored/domain/order_item.py
class OrderItem:
    def __init__(self, product_id: str, price: float, quantity: int):
        self.product_id = product_id
        self.price = price
        self.quantity = quantity
 
    @property
    def subtotal(self) -> float:
        return self.price * self.quantity
 
 
# ✅ ARCHIVO: refactored/domain/order.py
class Order:
    def __init__(self):
        self.items: List[OrderItem] = []
        self.status: str = 'PENDING'  # 'PENDING' | 'PAID'
 
    # ✅ APLICACIÓN DE CREATOR (GRASP)
    # Pregunta: ¿Quién debe crear a OrderItem?
    # Respuesta: Order. Porque Order "agrega" y contiene a los items.
    # Order tiene la información necesaria para inicializarlos correctamente.
    def add_item(self, product_id: str, price: float, quantity: int) -> None:
        new_item = OrderItem(product_id, price, quantity)
        self.items.append(new_item)
 
    def get_total(self) -> float:
        return sum(item.subtotal for item in self.items)
 
    def mark_as_paid(self) -> None:
        self.status = 'PAID'
 
 
# ✅ ARCHIVO: refactored/services/payment_service.py (SRP: Solo Pagos)
# Definimos una clase abstracta (Interface) para desacoplar
class PaymentProcessor(ABC):
    @abstractmethod
    def process(self, amount: float, currency: str) -> bool:
        pass
 
# Implementación concreta (oculta la lógica de Stripe)
class StripePaymentProcessor(PaymentProcessor):
    def process(self, amount: float, currency: str) -> bool:
        print(f"Procesando pago de {amount} {currency} via Stripe...")
        return True  # Simulado
 
 
# ✅ ARCHIVO: refactored/services/notification_service.py
class NotificationService(ABC):
    @abstractmethod
    def send_receipt(self, order: Order):
        pass
 
class EmailNotifier(NotificationService):
    def send_receipt(self, order: Order):
        print(f"Enviando recibo por email para orden con total: {order.get_total()}")
 
 
# ✅ ARCHIVO: refactored/order_service.py (El Orquestador)
# Este servicio une las piezas. Evita que Order tenga que crear cosas que no le tocan.
class OrderService:
    # Inyección de dependencias (Adiós a los 'new' hardcodeados dentro de la clase)
    # Recibimos las implementaciones concretas, pero tipamos con las abstracciones.
    def __init__(self, 
                 payment_processor: PaymentProcessor, 
                 notifier: NotificationService, 
                 repo: any): # Repo simulado
        self.payment_processor = payment_processor
        self.notifier = notifier
        self.repo = repo
 
    def process_order(self, order: Order) -> None:
        total = order.get_total()
 
        # 1. Delegar pago (SRP - Responsabilidad del procesador)
        success = self.payment_processor.process(total, 'USD')
        if success:
            order.mark_as_paid()
            # 2. Delegar persistencia (SRP - Responsabilidad del repo)
            if self.repo:
                self.repo.save(order)
            else:
                print("Guardando orden en base de datos...")
 
            # 3. Delegar notificación (SRP - Responsabilidad del notificador)
            self.notifier.send_receipt(order)
 
# --- EJEMPLO DE USO (Main) ---
if __name__ == "__main__":
    # 1. Configuración (Inyección de Dependencias)
    # Aquí es donde decidimos qué tecnologías usar, NO dentro de la clase Order.
    stripe_processor = StripePaymentProcessor()
    email_notifier = EmailNotifier()
    repo_mock = None
 
    # 2. Instanciación del servicio
    service = OrderService(stripe_processor, email_notifier, repo_mock)
 
    # 3. Ciclo de vida del Pedido
    my_order = Order()
    my_order.add_item("Laptop", 1200.00, 1)  # Order crea internamente el Item (Creator)
    my_order.add_item("Mouse", 25.50, 2)
 
    # 4. Procesamiento
    print(f"Total antes de procesar: {my_order.get_total()}")
    service.process_order(my_order)
    print(f"Estado final de la orden: {my_order.status}")