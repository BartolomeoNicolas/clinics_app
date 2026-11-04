# clinics_app

Aplicación en KMP del Sistema de Clínicas para Android.

## Requisitos

| Qué | Versión / valor |
|---|---|
| JDK | **25** — el que trae Android Studio: `~/Facultad/android-studio/jbr` |
| Android SDK | platform `android-37.0` y build-tools `36.0.0` |
| Emulador | AVD `Pixel_8` (API 37) |
| Gradle | el wrapper del repo (**9.6.0**) — no hace falta instalar Gradle |

**No hacen falta `cmdline-tools`**: AGP 9.4.1 no pide instalar ningún componente de SDK
adicional con estas versiones.

### `JAVA_HOME`

El Java del sistema es un JRE sin `javac`, y el build falla con
`does not provide the required capabilities: [JAVA_COMPILER]`. Exportalo antes de cada
build:

```bash
export JAVA_HOME="$HOME/Facultad/android-studio/jbr"
```

### `local.properties`

Apunta al Android SDK y **no se versiona** (está en el `.gitignore`). Si clonas el repo en
otra máquina, crealo:

```properties
sdk.dir=/home/<usuario>/Android/Sdk
lan.host=192.168.1.100
```

`lan.host` es la IP de la máquina sobre la que escuchan los backend. **No está hardcodeada**:
se lee de este archivo al hacer el build y solo la usa el destino `dispositivo`. Si no existe,
cae a `192.168.1.100` y hay que cambiarla por la IP real de la red local.

## Estructura

Van **dos módulos**, porque AGP 9 ya no permite aplicar `org.jetbrains.kotlin.multiplatform`
junto con `com.android.application` en el mismo módulo:

| Módulo | Plugin | Qué contiene |
|---|---|---|
| `:app` | `com.android.application` | `MainActivity`, `AndroidManifest.xml`, tema, recursos. El entry point |
| `:shared` | `com.android.kotlin.multiplatform.library` | `commonMain` (UI y lógica) y `androidMain` (`expect`/`actual`) |

`:app` depende de `:shared`, así que `./gradlew :app:assembleDebug` compila todo.

Las versiones están centralizadas en **`gradle/libs.versions.toml`** y las fija el
enunciado §3.2: AGP **9.4.1** · Kotlin **2.4.20** · Compose Multiplatform **1.9.3** ·
`compileSdk 37`, `minSdk 24`, `targetSdk 37`.

Detalle no obvio de `:shared`: el plugin Android-KMP trae los tests deshabilitados, así que
hay que habilitarlos con `withHostTest { }`. Sin eso `commonTest` queda sin compilación que
lo absorba y Gradle avisa en cada build.

### Paquetes en `:shared`

`commonMain` concentra la UI y la lógica; `androidMain` solo aporta lo que es de plataforma.

| Paquete | Contenido |
|---|---|
| `ui.theme` · `ui.components` · `ui.screens` | tema Material 3, componentes compartidos y las pantallas del flujo |
| `navigation` | rutas y grafo de navegación |
| `domain.error` · `domain.port` | errores de dominio y puertos que el dominio declara |
| `infrastructure.network` | cliente Ktor, mapeo del `problem+json` y el `actual` del motor |

La regla es que `domain` no mire hacia afuera: el cliente HTTP implementa el contrato que el
dominio declara, no al revés.

## Destinos y red

Hay dos **flavors** (dimensión `destino`), porque el emulador y el celular físico no llegan a
los backend por la misma dirección:

| Flavor | Booking | Sync | Motivo |
|---|---|---|---|
| `emulador` | `http://10.0.2.2:8082` | `http://10.0.2.2:8081` | `10.0.2.2` es el host de la máquina según el emulador Android |
| `dispositivo` | `http://<lan.host>:8082` | `http://<lan.host>:8081` | el celular sí está en la red local y necesita la IP real |

Los puertos los fija el enunciado: booking en **8082**, sync en **8081**.

Las dos URLs llegan a la app como `BuildConfig.BOOKING_BASE_URL` y `BuildConfig.SYNC_BASE_URL`
(declaradas en `app/build.gradle.kts`). **En el código Kotlin no hay ninguna URL escrita**: el
módulo `:shared` recibe un `AppConfig` desde el entry point.

### Tráfico sin TLS

`AndroidManifest.xml` declara `android:networkSecurityConfig` y ese archivo permite tráfico
claro para todo el dispositivo. Es a propósito y está justificado en el propio XML:

- los dos backend escuchan por HTTP simple, sin TLS configurado;
- no hay deploy de producción, son servicios de desarrollo.

En cuanto aparezca HTTPS hay que restringirlo a los dominios concretos y sacar el permiso
global.

## Compilar y ejecutar

```bash
export JAVA_HOME="$HOME/Facultad/android-studio/jbr"

# APK de depuración de un destino
./gradlew :app:assembleEmuladorDebug
./gradlew :app:assembleDispositivoDebug

# build completo: compila, lint y tests de los dos destinos
./gradlew clean build
```

Salida: `app/build/outputs/apk/<destino>/debug/app-<destino>-debug.apk`, por ejemplo
`app/build/outputs/apk/emulador/debug/app-emulador-debug.apk`.

### Emulador

```bash
# arrancar (headless, sin ventana)
$HOME/Android/Sdk/emulator/emulator -avd Pixel_8 -no-window -no-snapshot &

# instalar y abrir
$HOME/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/emulador/debug/app-emulador-debug.apk
$HOME/Android/Sdk/platform-tools/adb shell am start -n com.bartolomeo.clinics.app/.MainActivity
```

## Tests

Viven en `shared/src/commonTest`, que es donde van a estar los de ViewModel y casos de uso:

```bash
./gradlew :shared:testAndroidHostTest
```

Ahora cubren el contrato del cliente HTTP contra un `MockEngine`: que un error del backend
llegue como excepción tipada con su `code`, que el interceptor inyecte el JWT de sesión, y que
los reintentos queden acotados (3 intentos ante un 5xx, 1 solo ante un 4xx).

## Estado

M0 completo: toolchain, módulos, tema Material 3, cliente Ktor con manejo de errores
`problem+json` y el grafo de navegación con las diez pantallas del flujo, todas con contenido
de placeholder. Faltan los formularios de sesión, la búsqueda contra el catálogo local y el
almacenamiento seguro del token.

## Convenciones de commits

Los commits siguen [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/):

```
tipo(ámbito): descripción
```

| Tipo | Para qué |
|---|---|
| `feat` | funcionalidad nueva |
| `fix` | corrección de defecto |
| `docs` | documentación |
| `test` | pruebas automatizadas |
| `refactor` | cambio sin alterar comportamiento |
| `chore` | tooling, configuración, dependencias |

**Ámbitos sugeridos en este repo**: app, kmp, infra, docs, adrs

Ejemplos:

```
    feat(app): agrega la pantalla de disponibilidad de turnos
    feat(kmp-android): permite HTTP claro solo en builds de desarrollo
    fix(app): no muestra acciones cuando el proceso ya expiró
```

Reglas adicionales:

- El tipo va en inglés (lo entienden las herramientas estándar); la descripción en español.
- La línea de asunto tiene máximo 72 caracteres y no termina en punto.
- Si un commit cierra una issue, se indica `Closes #N` en el cuerpo; si solo la menciona, `Refs #N`.
- Un `!` antes de los dos puntos (`feat(api)!:`) marca un cambio incompatible del contrato.
- Commits generados por Git (`Merge`, `Revert`, `squash!`, `fixup!`) se aceptan sin validar.

### Validación automática

El hook `scripts/git-hooks/commit-msg` rechaza los commits que no cumplen el formato.
Está versionado en este repo y se activa en cada clon con:

```bash
git config core.hooksPath scripts/git-hooks
```

Para saltearlo a propósito: `git commit --no-verify` (no se recomienda).
