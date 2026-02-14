
# Imaginemos que estas librerías existen
# import smtp_lib, sql_lib, stripe_api (simulados)
 
class DatabaseConnection:
    def __init__(self, host, user, password):
        print(f"Connecting to DB at {host}...")
 
    def query(self, sql):
        print(f"Executing: {sql}")
 
class SMTPClient:
    def __init__(self, server, port):
        print(f"Connecting to SMTP at {server}:{port}...")
 
    def send(self, to_email, subject, body):
        print(f"Sending email to {to_email}...")
 
class StripeSDK:
    def __init__(self, api_key):
        print("Initializing Stripe SDK...")
 
    def charge(self, amount, card_number):
        print(f"Charging {amount} to card ending in {card_number[-4:]}")
        return {"status": "success"}
 
class Order:
    def __init__(self):
        self.items = []
        # 💀 VIOLACIÓN CREATOR & SRP (Smell: Hardcoded Dependencies)
        # ¿Por qué un Pedido debe saber cómo configurar la base de datos?
        # Esto crea un ACOPLAMIENTO FUERTE. Si cambiamos la DB, rompemos Order.
        self.db = DatabaseConnection("localhost", "root", "password")
        # ¿Por qué un Pedido debe saber el puerto del servidor de correo?
        self.mailer = SMTPClient("smtp.gmail.com", 587)
        # Instanciación directa = Dificultad para Testear (No se puede mockear fácilmente)
        self.payment = StripeSDK("api_key_secret_123")
 
    # 💀 VIOLACIÓN CREATOR (Parcial): 
    # Recibe tipos primitivos y lógica sucia en lugar de objetos de dominio
    def add_item(self, name: str, price: float, qty: int):
        # Lógica de validación mezclada
        if qty <= 0:
            raise ValueError("Invalid quantity")
        # Creación anémica (diccionario sin comportamiento)
        self.items.append({"name": name, "price": price, "qty": qty, "total": price * qty})
 
    # 💀 VIOLACIÓN SRP: Método "God Method"
    # Este método hace TODO: Lógica de negocio, Pagos, Persistencia y Notificación.
    def checkout(self, credit_card_number: str, user_email: str):
        # 1. Calcular total (Lógica de Negocio)
        total = sum(item["total"] for item in self.items)
 
        # 2. Procesar Pago (Infraestructura externa)
        print("Conectando con Stripe...")
        try:
            charge = self.payment.charge(total, credit_card_number)
            if charge["status"] != 'success':
                raise Exception("Pago fallido")
        except Exception as e:
            print(f"Error en pago: {e}")
            return False
 
        # 3. Guardar en BD (Persistencia)
        print("Guardando en SQL...")
        self.db.query("INSERT INTO orders VALUES (...)")
 
        # 4. Enviar Correo (Notificación)
        print("Enviando email...")
        self.mailer.send(user_email, "Gracias por tu compra", f"Tu total fue {total}")
 
        return True