# TradeSim Microservices + API Gateway (`com.g1`)

Arquitectura distribuida en 3 microservicios Spring Boot + 1 API Gateway (Java 25), 100% compatible con `tradesim-frontend` sin modificar el frontend:

1. `api-gateway` (Puerto `8082` · Punto de entrada único para el frontend, CORS centralizado)
2. `mercado-service` (Puerto `8081` · BD `mercado_db` en `5433` · Catálogo, Cotizaciones y Auth)
3. `portafolio-service` (Puerto `8083` · BD `portafolio_db` en `5435` · Posiciones y P&L)
4. `ordenes-service` (Puerto `8084` · BD `ordenes_db` en `5434` · Órdenes y Motor de Emparejamiento)

## Orden de ejecución
1. `docker compose up -d`
2. Arrancar `mercado-service` (`mvn spring-boot:run`)
3. Arrancar `portafolio-service` (`mvn spring-boot:run`)
4. Arrancar `ordenes-service` (`mvn spring-boot:run`)
5. Arrancar `api-gateway` (`mvn spring-boot:run`)
6. Arrancar el frontend (`npm run dev` en `http://localhost:5173`)
