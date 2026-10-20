# Guía de prueba: ACOMPAV (DAW II)

Esta versión cumple los requisitos obligatorios de PDF1. Todos los comandos son para **Windows (PowerShell)**. No hay commits: los cambios quedan sin confirmar en Git.

## Qué incluye esta versión

| # | Cambio | Requisito |
|---|---|---|
| 1 | Las contraseñas se cifran con BCrypt. El login valida los campos y responde 401 (credenciales incorrectas), 403 (usuario inactivo) o 400 (campos vacíos). | RQ-07, RQ-08, EV-02 |
| 2 | Spring Security quedó integrado. **Todas las rutas siguen abiertas**, como decidimos. | RQ-01 (parcial) |
| 3 | Script único de base de datos: `app_comerciantes_backend/db/mercado_db.sql`. Las contraseñas vienen cifradas y hay 5 deudas pendientes para probar el cobro. | B-01 |
| 4 | **Pruebas de la capa de datos**: insertar, actualizar, eliminar y listar en socios, puestos, motivos de cobro y deudas. Usan una base de datos H2 en memoria, así que no necesitan MySQL. | EV-03 |
| 5 | Al guardar un comprobante se eligen las deudas del puesto. El total se calcula solo (pago total) y las deudas pasan a PAGADA. | RQ-10, BR-07, BR-08 |
| 6 | El registro de pagos ya funciona: antes fallaba. No deja pagar un monto menor al total ni pagar dos veces el mismo comprobante. | RQ-10, BR-09 |
| 7 | El reporte de caja agrupa por método de pago (EFECTIVO, YAPE, PLIN, TRANSFERENCIA). Antes agrupaba por tipo de comprobante. | BR-10 |
| 8 | Al editar un puesto ya no se pierde su estado. Borrar un socio ya no borra sus puestos. | BR-03 |
| 9 | Las 8 pruebas del frontend compilan y pasan. | EV-03 |
| 10 | Colección de Postman: `ACOMPAV.postman_collection.json`. | EV-06, EV-07, ENT-02 |

**Para ver los cambios exactos:**

```powershell
cd app_comerciantes_backend
git diff --ignore-cr-at-eol -w
git status
```

Haz lo mismo en `app_comerciantes_frontend`. Git marcará muchos archivos como modificados por diferencias de fin de línea que ya existían; el comando `diff` de arriba muestra solo los cambios reales.

---

## Paso 0 · Requisitos

```powershell
java -version      # 17 o superior
node -v            # 20.19 o superior
mysql --version    # 8.x
```

- MySQL encendido, con usuario `root` sin contraseña (como en `application.yml`).
- Internet la primera vez, para descargar dependencias.

## Paso 1 · Respalda tu base de datos actual

```powershell
mysqldump -u root mercado_db > respaldo_mercado_db.sql
```

## Paso 2 · Descomprime el zip en una carpeta nueva

No lo descomprimas encima de tu repositorio.

## Paso 3 · Crea la base de datos

En MySQL Workbench, abre y ejecuta `app_comerciantes_backend\db\mercado_db.sql`. Este script **borra y recrea** `mercado_db`.

O desde la consola:

```powershell
mysql -u root < app_comerciantes_backend\db\mercado_db.sql
```

**Resultado esperado:** la última consulta muestra 10 filas en cada tabla, salvo `deuda`, que muestra 15.

**Usuarios de prueba:** `admin1`, `cajero1`, …, `invitado1`. Todos con la contraseña `123456`.

## Paso 4 · Pruebas del backend (no necesitan MySQL)

```powershell
cd app_comerciantes_backend
.\mvnw.cmd clean test
```

**Resultado esperado:** `Tests run: 29, Failures: 0, Errors: 0` y `BUILD SUCCESS`.

| Clase | Pruebas | Qué prueba |
|---|---|---|
| `AuthServiceTest` | 6 | Login correcto, contraseña incorrecta, usuario inexistente, usuario inactivo, contraseña cifrada al registrar y al actualizar |
| `SocioRepositoryTest` | 4 | Insertar, actualizar, eliminar y listar |
| `PuestoRepositoryTest` | 5 | Lo mismo + listar los puestos de un socio |
| `MotivoCobroRepositoryTest` | 4 | Insertar, actualizar, eliminar y listar |
| `DeudaRepositoryTest` | 4 | Insertar, actualizar, eliminar y listar |
| `FlujoCobroTest` | 5 | Comprobante que paga deudas, no pagar dos veces, pago + reporte por método, pago insuficiente, comprobante pagado dos veces |
| `ComerciantesApplicationTests` | 1 | La aplicación arranca |

**Guarda una captura de esta salida: es evidencia para el informe.** El detalle por clase queda en `target\surefire-reports\`.

## Paso 5 · Levanta el backend

```powershell
.\mvnw.cmd spring-boot:run
```

**Resultado esperado:** `Started ComerciantesApplication` en el puerto 9090.

> Puede aparecer `Using generated security password`. Es normal y no se usa.
>
> Si usas una base de datos antigua en lugar del script nuevo, la primera vez verás `[PasswordMigrationRunner] Contrasenas cifradas con BCrypt: N`.

## Paso 6 · Pruebas en Postman

En Postman: **Import** → `ACOMPAV.postman_collection.json`. Cada petición dice en su descripción qué resultado esperar.

**Ejecuta las carpetas en orden: 1, 2 y 3.**
- Antes de la petición 1.5, ejecuta en MySQL: `UPDATE usuario SET estado=0 WHERE username='invitado1';`
- Antes de la 3.5, cambia la variable `hoy` de la colección por la fecha de hoy (`AAAA-MM-DD`).

| Petición | Resultado esperado |
|---|---|
| 1.1 | 200 |
| 1.2 | 401 |
| 1.3 | 401 |
| 1.4 | 400 |
| 1.5 | 403 |
| 1.6 | 200 |
| 2.x | GET 200 · POST 201 · PUT 200 · DELETE 204 (2.9) · 400 al borrar una deuda pagada (2.10) |
| 3.1 | 200, con `total = 70.00`. Después, en `GET /deudas` las deudas 11 y 12 aparecen como PAGADA. |
| 3.2 | 409, deuda ya pagada |
| 3.3 | 200, pago YAPE registrado. Si el comprobante de 3.1 no tiene id 11, usa el id que devolvió 3.1. |
| 3.4 | 400 |
| 3.5 | `totalPorMetodoPago` = `{"YAPE": 70.00}` |
| 3.6 | Total de abril = 690.00. Desglose: EFECTIVO 185, PLIN 120, TRANSFERENCIA 145, YAPE 240. |

Comprobación en la base de datos:

```sql
SELECT username, LEFT(password,7), LENGTH(password) FROM mercado_db.usuario;   -- $2a$10$ y 60
SELECT id_deuda, estado, fecha_pago FROM mercado_db.deuda WHERE id_deuda IN (11,12);
```

Al terminar, vuelve a activar el usuario: `UPDATE usuario SET estado=1 WHERE username='invitado1';`

## Paso 7 · Frontend

```powershell
cd ..\app_comerciantes_frontend
npm install
npm test          # esperado: Test Files 7 passed, Tests 8 passed
npm start
```

Abre `http://localhost:4200` y haz este recorrido:

1. **Login** con `admin1` / `123456`. Debe entrar.
2. **Login con una contraseña incorrecta.** Debe mostrar la alerta de error.
3. **Comprobantes → Nuevo:**
   - número `COMP-0100`;
   - puesto **P002**. Aparecen sus 2 deudas pendientes (S/ 50 y S/ 15);
   - marca las dos. El total cambia solo a 65.

   Guarda. Debe mostrar el mensaje "deudas marcadas como pagadas".
4. **Deudas:** las 2 deudas del puesto P002 aparecen como PAGADA, con Editar y Eliminar deshabilitados.
5. **Comprobantes → Nuevo** otra vez, con el puesto P002: ya no aparece ninguna deuda pendiente.
6. **Pagos → Nuevo:** método YAPE, monto 65, comprobante = id del `COMP-0100` (lo ves en la lista de comprobantes). Guarda.
7. **Pagos → Nuevo** con el mismo comprobante. Debe mostrar el error "ya tiene un pago registrado".
8. **Reportes → rango con la fecha de hoy:** el desglose muestra YAPE 65. Descarga también el PDF.
9. **Puestos → Editar** un puesto y guarda. Su estado no cambia.
10. **Usuarios, Socios, Motivos, Dashboard:** confirma que siguen funcionando.

## Paso 8 · Qué enviarme

1. La salida final de `mvnw.cmd clean test` (paso 4).
2. El código HTTP de cada petición de Postman, o una captura del Runner de Postman.
3. El resultado de `npm test` y del recorrido del paso 7 (qué funcionó y qué no).
4. Cualquier error completo.

## Si algo falla

| Síntoma | Qué hacer |
|---|---|
| Error de compilación en `mvnw test` | Envíame el primer `[ERROR]` completo. No pude compilar el backend en mi entorno, así que este es el punto de mayor riesgo. |
| `Could not resolve dependencies` | Revisa tu conexión a internet o el proxy. |
| Las pruebas de repositorio fallan con un error de H2 | Envíame el error. Probablemente sea un ajuste del archivo `src/test/resources/application-test.yml`. |
| `insertsBDComerciantes.sql` da error | Es normal: quedó obsoleto. Usa `db/mercado_db.sql`. |
