# Bitácora de Desarrollo y Cambios para dominio Portafolio

## Rama de trabajo: feat/portafolio-

## Resumen Ejecutivo

Como dueño del Bounded Context Portafolio (BC3), mi responsabilidad es gestionar las posiciones financieras simuladas de cada inversionista, calcular la valoración total y la ganancia/pérdida no realizada (P&L), y garantizar la regla de negocio crítica de que ningún usuario puede vender instrumentos que no posee o en cantidades mayores a su saldo.

## Detalle de Cambios Realizados

### Evidencia de Diseño Táctico DDD Implementado

La implementación en `com.g1.trading.portafolio` traduce directamente los acuerdos del *Event Storming* en código limpio y desacoplado:

**Contrato Transversal (`com.g1.trading.compartido`):**
Creación del Value Object `InversionistaId.java` para referenciar al actor inversionista entre contextos sin acoplar entidades completas, respetando los límites de Agregado.
**Lenguaje Ubicuo Explícito:**
Documentado en código mediante `LenguajeUbicuoPortafolio.java`, asegurando que términos como *Portafolio, Posición, Cantidad, ResumenPortafolio* tengan un significado único e inequívoco en el paquete.
**Value Objects e Inmutabilidad:**
`Cantidad.java`: Encapsula las unidades de instrumentos financieros, bloqueando valores negativos ($< 0$) y garantizando operaciones aritméticas seguras al acreditar compras o debitar ventas.
`ResumenPortafolio.java` : Objeto de proyección/lectura inmutable para reportar el estado consolidado del portafolio (`HU-06`).
**Raíz de Agregado y Entidad Interna:**
`Portafolio.java` : *Aggregate Root* que gobierna el ciclo de vida de las tenencias y protege las invariantes del dominio.
`Posicion.java` : Entidad subordinada al agregado que registra la tenencia por `InstrumentoId` y su valoración.
**Factory de Dominio:**
`PortafolioFactory.java` : Encapsula la creación consistente de instancias iniciales de `Portafolio` para un `InversionistaId`.
**Servicio de Dominio y Excepciones de Negocio:**
`ValidacionVentaService.java` y `VentaNoPermitidaException.java` : Implementan la regla crítica de negocio de la **HU-03** (*"Una orden de venta se rechaza si el inversionista no tiene esa cantidad del instrumento en su portafolio"*).
**Orquestación de Aplicación y Persistencia Desacoplada:**
`PortafolioService.java` : Coordina los casos de uso de consulta de estado (`HU-06`) y actualización de posiciones tras cruces de órdenes (`HU-05`).
`PortafolioRepository.java` e implementación `PortafolioRepositoryEnMemoria.java` : Aíslan el dominio de la infraestructura mediante inversión de dependencias. 
