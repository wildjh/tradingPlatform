# Contexto Órdenes y Emparejamiento (`com.g1.trading.ordenes`)

## Alcance implementado
- **Entidad `Transaccion`:** Registro inmutable del cruce entre una orden de compra y una de venta (identificadores, participantes, instrumento, cantidad, precio de ejecución y marca de tiempo).
- **Motor `EmparejamientoService` y `Orden`:** Soporte completo para validación y ejecución tanto de órdenes **Límite** como órdenes **a Mercado** (priorizando el precio límite de la contraparte pasiva o el precio de mercado de referencia cuando ambas órdenes son a mercado).
- **Repositorios en memoria:** `OrdenRepository` / `OrdenRepositoryEnMemoria` (libro de órdenes con prioridad FIFO de llegada) y `TransaccionRepository` / `TransaccionRepositoryEnMemoria`.
- **Servicio de aplicación `OrdenService`:** Orquesta el ciclo de vida de las órdenes (creación límite y a mercado, validación previa de tenencias con `PortafolioService.validarVenta`, cruce automático, liquidación mediante `PortafolioService.aplicarTransaccion` y cancelación controlada por propietario).

## Historias de usuario cubiertas
- **HU-03 / HU-04:** Creación de órdenes de compra y venta (límite y a mercado) con validación de saldo en portafolio.
- **HU-05:** Emparejamiento automático FIFO y actualización atómica de tenencias en los portafolios del comprador y vendedor.
- **HU-07:** Consulta del libro de órdenes pendientes, historial de transacciones por inversionista y cancelación de órdenes pendientes.

## Verificación
- **78 pruebas unitarias y de integración en verde** (`mvn test`), incluyendo `OrdenTest`, `PrecioLimiteTest`, `EmparejamientoServiceTest` y `OrdenServiceTest`.
