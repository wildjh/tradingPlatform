# G1 · Plataforma de trading / inversión bursátil simplificada
---
## 1. Lista de eventos de dominio (ordenados cronológicamente)

- `Instrumento registrado en el catálogo` — (Admin de mercado carga un instrumento nuevo)
- `Precio cotizado actualizado` — (Admin simula o el sistema actualiza el precio de un instrumento)
- `Histórico de precios consultado` — (Inversionista solicita serie histórica de un instrumento)
- `Lista de instrumentos con cotización actual consultada` — (Inversionista ve el panel de cotizaciones)
- `Orden de compra/venta colocada` — (Inversionista expresa intención de operar a mercado o con precio límite)
- `Orden rechazada por validación` — (precio ≤ 0 o cantidad insuficiente en portafolio)
- `Orden quedada en estado pendiente` — (la orden superó validaciones y espera emparejamiento)
- `Órdenes emparejadas` — (Motor cruza compra y venta compatibles: lados opuestos y precio compra ≥ precio venta)
- `Transacción generada` — (se registra el cruce y se actualizan ambos portafolios en la misma operación)
- `Portafolio actualizado` — (posiciones, valor total y ganancia/pérdida no realizada recalculados)
- `Orden ejecutada notificada` — (inversionista recibe aviso de ejecución o fallo)
- `Orden cancelada` — (inversionista cancela una orden que aún no se ha ejecutado)
- `Historial de transacciones consultado` — (inversionista audita sus propias decisiones)
- `Portafolio consultado` — (inversionista ve posiciones, valor total y P&L no realizada)

## 2. Eventos Pivote

- `Precio cotizado actualizado`: El administrador (o el simulador) produce el cambio; a partir de ahí la responsabilidad pasa a quien consulta cotizaciones.
- `Orden de compra/venta colocada (y su validación/rechazo)`: Involucra reglas de precio > 0 y de cantidad disponible en portafolio. Cruza el mundo de las órdenes con el del portafolio; no puede resolverse solo dentro de una Orden.
- `Órdenes emparejadas → Transacción generada`: El motor de emparejamiento necesita órdenes de lados opuestos y precios compatibles; al ejecutarse actualiza dos portafolios en la misma operación. Es el corazón del dominio y el pivote más fuerte.
- `Portafolio actualizado`: Tras una transacción, la responsabilidad de mantener posiciones, valor y P&L recae en el contexto de portafolio, no en el de órdenes.

## 3. Bounded Contexts Candidatos
**Criterios aplicados**: cada término del Lenguaje Ubicuo significa una sola cosa dentro del contexto; el corte se apoya en eventos pivote reales; un contexto puede evolucionar sin forzar cambios inmediatos en los demás.
**BC1 · Mercado (catálogo de instrumentos y cotizaciones)**
**Eventos pivote de origen:** “Instrumento registrado en el catálogo” y “Precio cotizado actualizado”.
**Justificación:** Concentra todo lo relacionado con el catálogo de instrumentos, la cotización actual y el histórico de precios. Las lecturas son mucho más frecuentes que las escrituras (RNF). Debe poder consultarse aunque el motor de emparejamiento no esté disponible. Lenguaje Ubicuo propio: Instrumento, Cotización, PrecioCotizado, Histórico.
Anotación de William (dueño propuesto): Este contexto es principalmente de consulta + simulación periódica de precios (HU-08). No concentra lógica de emparejamiento ni de portafolio.

**BC2 · Órdenes y emparejamiento**
**Eventos pivote de origen:** “Orden de compra/venta colocada”, “Orden rechazada por validación”, “Órdenes emparejadas” y “Transacción generada”.
**Justificación:** Agrupa el ciclo de vida de la Orden (pendiente → ejecutada / cancelada) y el proceso del Motor de emparejamiento. El EmparejamientoService no pertenece a una sola Orden: necesita comparar múltiples órdenes de lados opuestos. Lenguaje Ubicuo propio: Orden, PrecioLimite, EstadoOrden, Transacción, Emparejamiento.
Anotación de David (dueño propuesto): Aquí vive la regla “dos órdenes solo se emparejan si son de lados opuestos y el precio de compra ≥ precio de venta”. También la invariante de que una orden ejecutada no puede cancelarse.

**BC3 · Portafolio**
**Eventos pivote de origen:** “Portafolio actualizado”, “Portafolio consultado” y la validación de cantidad disponible al colocar una venta.
**Justificación:** Mantiene las posiciones del inversionista, el valor total y la ganancia/pérdida no realizada. La regla “no se puede vender lo que no se posee” es de este contexto. Lenguaje Ubicuo propio: Portafolio, Posición, Cantidad, ValorTotal, GananciaNoRealizada.
Anotación de David Cruz (dueño propuesto): Las referencias a Instrumento u Orden desde este contexto serán solo por id, nunca por objeto completo, para respetar los límites de Agregado entre contextos.

## 4. Asignación de Subdominios
1. Dev 1 - William Huertas: Subdominio de Mercado (catálogo)
2. Dev 2 - David: Subdominio de Órdenes y emparejamiento
3. Dev 3 - David Cruz: Subdominio de Portafolio

## 5. Repositorio de GitHub

