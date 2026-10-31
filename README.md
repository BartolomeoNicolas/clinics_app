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

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

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

## Compilar y ejecutar

```bash
export JAVA_HOME="$HOME/Facultad/android-studio/jbr"

# APK de depuración
./gradlew :app:assembleDebug

# build completo: compila, lint y tests
./gradlew clean build
```

Salida: `app/build/outputs/apk/debug/app-debug.apk`.

### Emulador

```bash
# arrancar (headless, sin ventana)
$HOME/Android/Sdk/emulator/emulator -avd Pixel_8 -no-window -no-snapshot &

# instalar y abrir
$HOME/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$HOME/Android/Sdk/platform-tools/adb shell am start -n com.bartolomeo.clinics.app/.MainActivity
```

## Estado

Esqueleto base: toolchain, estructura de módulos y una pantalla de placeholder que se compone
desde `commonMain`. Faltan la navegación, el tema Material 3, el cliente HTTP y la sesión.

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
