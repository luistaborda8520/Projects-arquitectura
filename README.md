# Principios de Diseño Aplicados – Creator (GRASP) + SRP

Dominio elegido
Sistema de procesamiento y aprobación de pedidos en un contexto tipo e-commerce.

## El sistema permite:

Agregar productos a una orden.
Procesar el pago.
Guardar la orden.
Enviar notificación al cliente.

Se presentan dos versiones del sistema:
legacySystem.java → Diseño incorrecto.
RefactoredSystem.java → Diseño refactorizado aplicando SRP + Creator (GRASP).

## Descripción del problema de diseño inicial
En la versión Legacy, la clase Order concentra múltiples responsabilidades:
Cálculo del total.
Procesamiento de pagos.
Persistencia en base de datos.
Envío de correos.

Creación directa de dependencias externas.
Problemas identificados:
Violación de SRP (múltiples razones de cambio).
Violación de Creator (GRASP).
Alto acoplamiento.
Código difícil de testear.
Dependencia directa de infraestructura.
Clase tipo “God Class”.

## Ejemplo de problemas concretos:
Si cambia la base de datos → se modifica Order.
Si cambia el proveedor de pagos → se modifica Order.
Si cambia el sistema de correo → se modifica Order.

Una sola clase tiene múltiples razones para cambiar.

## Principios aplicados
SRP (Single Responsibility Principle)

Una clase debe tener una sola razón para cambiar.

En la versión refactorizada:
Order → Solo gestiona lógica del dominio.
OrderItem → Solo representa una línea de pedido.
OrderService → Orquesta el flujo.
PaymentProcessor → Procesa pagos.
NotificationService → Envía notificaciones.
OrderRepository → Guarda en base de datos.

Cada clase tiene una responsabilidad clara y aislada.
Creator (GRASP)
El principio Creator establece que una clase debe crear objetos cuando:
Los contiene o agrega.
Tiene los datos necesarios.
Es responsable del ciclo de vida.

## Aplicación en el sistema:

Order crea OrderItem porque:
Los contiene.
Controla su ciclo de vida.
Posee la información necesaria.
Esto aumenta cohesión y reduce acoplamiento innecesario.

Además:
OrderService no crea sus dependencias.
Las recibe por constructor (inyección).
La creación ocurre en el main (Composition Root).

Decisiones de diseño relevantes y justificación
Separación por capas

El sistema fue dividido en:
Dominio
Interfaces (contratos)
Infraestructura
Servicio de aplicación
Composition Root (main)

Esto mejora:
Mantenibilidad
Testabilidad
Extensibilidad
Inyección de dependencias

En lugar de crear dependencias dentro de la clase:
this.payment = new StripeSDK();

Ahora se inyectan:
public OrderService(PaymentProcessor paymentProcessor, ...)
Esto permite cambiar implementaciones sin modificar la lógica principal.

Uso de interfaces
Se definieron contratos:
PaymentProcessor
NotificationService
OrderRepository
Permitiendo:
Polimorfismo.
Bajo acoplamiento.
Mayor extensibilidad.

## Costos reales
Más clases.
Mayor estructura inicial.
Más archivos en un proyecto real.
Beneficios:
Mejor organización.
Cambios localizados.
Mayor claridad conceptual.
Más alineado con arquitectura limpia.

## Conclusión
La refactorización demuestra cómo aplicar correctamente:
SRP para aislar responsabilidades.
Creator (GRASP) para asignar correctamente la creación de objetos.
El sistema resultante es:

Más mantenible.
Más extensible.
Más profesional.
Más preparado para cambios futuros.
