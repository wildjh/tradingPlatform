Esta es la bitacora de la rama de mercado, se anexarán ciertas evidencias en esta carpeta acerca de lo trabajado en este módulo.

## Estado actual del módulo Mercado (`rama-mercado`)

**Responsable:** William (Dev 1) · Bounded Context Mercado, según `Requisitos_G1.docx`.

En la narrativa del proyecto G1, el inversionista consulta cotizaciones de acciones y ETF en un mercado simulado (sin dinero real). El **administrador de mercado** carga instrumentos y precios. Este módulo cubre esa parte del catálogo; no implementa órdenes, emparejamiento, portafolio ni notificaciones (HU-03 a HU-07 y HU-09, a cargo de otros subdominios).

### Qué requisitos atiende

| Origen | Qué cubre Mercado |
|--------|-------------------|
| HU-01 | Lista de instrumentos con precio actual |
| HU-02 | Histórico de precios de un instrumento |
| HU-08 | Simulación periódica de precios (admin de mercado) |
| Entidades | `Instrumento`, `Cotización` (y el histórico asociado) |
| RNF | Lecturas de cotización separadas del motor de órdenes; catálogo e histórico consultables aunque el emparejamiento no esté disponible; cada cambio de precio queda trazable (quién / cuándo / qué) |

Hacia Órdenes o Portafolio solo se publica el símbolo (`InstrumentoId` como `String`), nunca el objeto completo.

### Cómo está organizado

Arquitectura hexagonal sobre el dominio DDD:

| Capa | Paquete | Rol |
|------|---------|-----|
| Dominio | `mercado.dominio` | Reglas puras: `Instrumento` (raíz), `PrecioCotizado`, `Cotizacion`, `HistoricoPrecios`, `InstrumentoFactory`, `SimulacionPreciosService` |
| Aplicación | `mercado.aplicacion` | Casos de uso (`InstrumentoService` / `InstrumentoUseCase`) y puerto `RepositorioInstrumentos` |
| Entrada | `mercado.infraestructura.entrada.web` | REST: `InstrumentoController` |
| Salida | `mercado.infraestructura.salida.persistencia` | JPA: entidad, repositorio Spring Data y adaptador |

### Cómo funciona el flujo

1. El controlador recibe HTTP y llama solo al puerto de aplicación (nunca al JPA directo).
2. La aplicación usa la factory / el servicio de dominio y persiste vía `RepositorioInstrumentos`.
3. El adaptador JPA traduce entre el agregado `Instrumento` y las tablas H2.

Endpoints actuales (`/instrumentos`):

- `GET /instrumentos` — HU-01: lista con cotización actual  
- `GET /instrumentos/{simbolo}/historico` — HU-02: histórico de precios  
- `POST /instrumentos` — admin: registrar instrumento en el catálogo  
- `POST /instrumentos/simulacion` — HU-08: aplicar un factor de variación a todo el catálogo  

Invariantes del dominio: precio siempre > 0 con marca de tiempo; el histórico solo crece hacia adelante; cada cotización indica origen (`CARGA_INICIAL` / `SIMULACION`) y quién la generó; la simulación valida el lote completo antes de guardar.

