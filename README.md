# Principios de Diseño Aplicados – Creator (GRASP) + SRP

## Dominio
Sistema de procesamiento de pedidos en un contexto **e-commerce**.

El sistema permite:
- Agregar productos a una orden.
- Procesar pagos.
- Guardar la orden.
- Enviar notificaciones al cliente.

El repositorio incluye:
- `legacySystem.java` → Diseño con problemas de arquitectura.
- `RefactoredSystem.java` → Diseño refactorizado aplicando SRP y Creator (GRASP).

---

## Descripción del problema de diseño inicial
En la versión Legacy, la clase Order concentra lógica de negocio, pagos, persistencia y envío de correos, además de crear directamente dependencias externas.
Esto provoca violaciones de SRP y Creator (GRASP), alto acoplamiento, baja testabilidad y una clase tipo God Class.

Como resultado, cualquier cambio en la base de datos, el proveedor de pagos o el sistema de correos obliga a modificar Order, ya que tiene múltiples razones para cambiar.

## Principios aplicados
**SRP (Single Responsibility Principle)**
Una clase debe tener una sola razón para cambiar.

En la versión refactorizada:
Order → Solo gestiona lógica del dominio.
OrderItem → Solo representa una línea de pedido.
OrderService → Orquesta el flujo.
PaymentProcessor → Procesa pagos.
NotificationService → Envía notificaciones.
OrderRepository → Guarda en base de datos.

Cada clase tiene una responsabilidad clara y aislada.

**Creator (GRASP)** Se aplicó asignando la creación de objetos a la clase con mayor relación.
Order crea OrderItem porque los contiene y controla su ciclo de vida, aumentando la cohesión.
Las dependencias de OrderService no se crean internamente, sino que se reciben por constructor.

## Decisiones de diseño relevantes y justificación
El sistema se organizó mediante separación por capas (dominio, servicios, infraestructura y composition root), lo que mejora la mantenibilidad, testabilidad y extensibilidad.

Se utilizó inyección de dependencias en lugar de creación directa de objetos, permitiendo cambiar implementaciones sin modificar la lógica principal.

Además, se definieron interfaces para los servicios clave, promoviendo bajo acoplamiento, polimorfismo y mayor flexibilidad en el diseño.

**Costos reales:**
Más clases.
Mayor estructura inicial.
Más archivos en un proyecto real.

**Beneficios**:
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
