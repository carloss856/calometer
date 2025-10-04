# Calometer

Calometer es un MVP Kotlin Multiplatform (Android + iOS) para seguimiento integral de nutrición, entrenamientos y pasos con soporte IA.

## Arquitectura

```
.
├─ shared/                # Lógica multiplataforma
│   ├─ core/              # Modelos, utilidades y DI (Koin)
│   ├─ ai/                # Parser heurístico asistido por IA
│   ├─ data/              # Repositorios SQLDelight (offline-first)
│   ├─ domain/            # Casos de uso
│   └─ features/          # Stores MVI para nutrición, pasos y entrenos
├─ androidApp/            # UI Compose, permisos y sensores Android
└─ iosApp/                # UI SwiftUI + bootstrap Koin/SQLDelight
```

- **Persistencia**: SQLDelight con base local cifrable (SQLite).
- **IA**: `SimpleMealNlpParser` realiza parsing heurístico (regex + fuzzy match) y calcula macros apoyándose en un catálogo local in-memory.
- **DI**: Koin inicializado desde `CalometerApp` (Android) y `IOSApp` (iOS). El módulo común expone `initializeSharedKoin`.
- **Flujos**: Stores MVI basados en `StateFlow` (`NutritionStore`, `WorkoutStore`, `StepsStore`).
- **Pasos**: `StepCounter` multiplataforma con implementación Android (sensor `TYPE_STEP_COUNTER`) e iOS (stub CoreMotion listo para extender). `StepsStore` sincroniza automáticamente los snapshots con la base local.

## Requisitos previos

- JDK 17
- Android Studio Giraffe+ o IntelliJ IDEA con soporte KMP
- Xcode 15 para el proyecto `iosApp`
- Kotlin 1.9.24 (configurada vía Gradle)

> Nota: el *Gradle wrapper JAR* no se incluye por restricciones del entorno. Ejecuta `gradle wrapper --gradle-version 8.5` una vez en tu máquina para generarlo antes de compilar.

## Construcción

```bash
# Generar wrapper (si es la primera vez)
gradle wrapper --gradle-version 8.5

# Android (desde raíz)
./gradlew :androidApp:assembleDebug

# Tests comunes
./gradlew :shared:allTests

# Tests de UI Android
./gradlew :androidApp:connectedAndroidTest
```

Para iOS:

1. Abre o crea un proyecto Xcode apuntando a `iosApp/` y enlaza el framework `Shared` generado por Gradle (`./gradlew :shared:syncFramework` recomendado).
2. Ejecuta el esquema `IOSApp`.

## Funcionalidades clave del MVP

- Registro de comidas mediante texto libre con confirmación de calorías/macros sugeridas.
- Catálogo local se precarga con alimentos base (manzana, avena). Puedes ampliarlo con `NutritionRepository.upsertFoods`.
- Creación y guardado de rutinas (`WorkoutStore`) y logging de sesiones (`SaveWorkoutSessionUseCase`).
- Sincronización continua de pasos con almacenamiento offline.
- UI accesible (labels claros, campos editables) con soporte para VoiceOver/TalkBack al seguir las guías Compose/SwiftUI.

## Pruebas

- `SimpleMealNlpParserTest` cubre parsing con unidades mixtas.
- `SaveMealUseCaseTest` verifica la persistencia de comidas.
- `MainActivityTest` (Android) confirma render básico con Compose Testing.

Amplía la suite con pruebas adicionales para repositorios (usando drivers en memoria) y stores según evolucione el producto.

## Observabilidad y métricas

- Logging mediante Napier (lista para enrutar a Crashlytics / Sentry en futuro).
- Hooks previstos para métricas de activación y precisión (añadir interceptores en stores / use cases).

## Roadmap sugerido

1. **Fase 1 (actual)**: Skeleton multiplataforma, parsers IA heurísticos, rutinas y pasos offline.
2. **Fase 2**: Conectar con catálogo ampliado (Open Food Facts), añadir Health Connect/CoreMotion real, historial avanzado.
3. **Fase 3**: Visión por computadora, recomendaciones de IA, widgets y social.

## Decisiones destacadas

- **Compose Android + SwiftUI iOS** para respetar lineamientos de plataforma manteniendo lógica compartida.
- **Parser heurístico** en vez de LLM remoto para MVP offline-first (fácil de intercambiar vía interfaz `MealNlpParser`).
- **Seed de datos** en app start para garantizar experiencia out-of-the-box sin conexión.
- **Sin backend** en MVP: repositorios implementan patrón offline-first listo para sincronización eventual.

## Próximos pasos

- Completar módulos de workout UI (temporizadores, detalles de sets) tanto en Compose como SwiftUI.
- Integrar Health Connect/CoreMotion reales con manejo de permisos fine-grained.
- Añadir cifrado (SQLCipher) y más casos de prueba (goldens IA, repositorio en memoria, stores MVI).
