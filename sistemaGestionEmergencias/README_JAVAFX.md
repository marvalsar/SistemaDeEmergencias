# 🚨 Sistema de Gestión de Emergencias Médicas v2.0

Sistema avanzado de simulación de gestión de emergencias médicas con interfaz JavaFX animada, patrones de diseño, y
concurrencia optimizada.

## ✨ Características Principales

### 🎨 Interfaz Gráfica JavaFX Interactiva

- **Menú Principal Animado**: Gradientes dinámicos, efectos de brillo y glass morphism
- **Configuración Personalizable**: Sliders interactivos para ajustar parámetros
- **Simulación en Tiempo Real**: Mapa animado con ambulancias y equipos médicos moviéndose
- **Dashboard de Estadísticas**: Panel lateral con métricas actualizadas
- **Log de Eventos**: Visualización en tiempo real de todos los eventos
- **Animaciones Suaves**: Transiciones, efectos de pulso y sombras animadas

### 🏗️ Arquitectura y Patrones

- **Singleton**: SimulacionManager para gestión centralizada
- **Factory**: RecursoFactory para creación de recursos
- **Observer**: Sistema de notificación de eventos desacoplado
- **Concurrencia**: ExecutorService, BlockingQueue, sincronización thread-safe

### 🎮 Modos de Ejecución

#### 1. Interfaz Gráfica JavaFX (Recomendado)

```bash
mvn clean javafx:run
```

O ejecutar la clase principal:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.iudigital.emergencias.MainJavaFX"
```

#### 2. Consola con Gráficos ANSI

```bash
mvn clean compile exec:java -Dexec.mainClass="org.iudigital.emergencias.Main"
```

## 🎯 Parámetros Configurables

### En JavaFX:

- **Ambulancias**: 1-10 (predeterminado: 4)
- **Equipos Médicos**: 1-5 (predeterminado: 2)
- **Operadores**: 1-5 (predeterminado: 2)
- **Duración**: 30-300 segundos (predeterminado: 60)
- **Modo Turbo**: Simulación acelerada

### En Consola:

```bash
java -cp target/classes org.iudigital.emergencias.Main <ambulancias> <equipos> <operadores> <duracion_seg>
```

## 📦 Requisitos

- **Java**: 17 o superior
- **Maven**: 3.6+
- **JavaFX**: 21.0.1 (incluido en dependencias)

## 🚀 Inicio Rápido

1. **Clonar y compilar**:

```bash
mvn clean install
```

2. **Ejecutar JavaFX**:

```bash
mvn javafx:run
```

3. **En el menú**:
   - Ajusta los parámetros con los sliders
   - Activa "Modo Turbo" si deseas simulación rápida
   - Presiona "🚀 INICIAR SIMULACIÓN"
   - Observa el mapa animado, ambulancias y estadísticas

## 🎨 Características Visuales

### Menú Principal

- Fondo con gradiente animado (pulso azul)
- Título con efecto de brillo pulsante
- Panel de configuración con efecto glass morphism
- Sliders con retroalimentación visual (escala al interactuar)
- Botones con hover effects y sombras dinámicas

### Vista de Simulación

- **Mapa**: Grid animado con 3 hospitales
- **Ambulancias**: Sprites animados que se mueven hacia emergencias
  - Verde: Disponible
  - Amarillo: En ruta
  - Rojo: Ocupada
  - Azul: Retornando
- **Emergencias**: Círculos pulsantes con colores por severidad
  - Rojo: Crítico
  - Naranja: Grave
  - Amarillo: Moderado
  - Verde: Leve
- **Hospitales**: Edificios con cruz roja y efecto de brillo
- **Panel de Estadísticas**: Indicadores en tiempo real con colores
- **Log de Eventos**: Lista con scroll de últimos 50 eventos

## 🛠️ Tecnologías

- **JavaFX 21.0.1**: Interfaz gráfica moderna
- **Java 17**: Características modernas del lenguaje
- **Maven**: Gestión de dependencias
- **SLF4J + Logback**: Logging profesional
- **JUnit 5 + Mockito**: Testing (preparado)
- **H2 Database**: Base de datos (preparada)

## 📊 Arquitectura del Proyecto

```
src/main/java/org/iudigital/emergencias/
├── domain/              # Entidades del dominio
│   ├── CasoEmergencia.java
│   ├── Ambulancia.java
│   └── EquipoMedico.java
├── manager/             # Gestión de simulación
│   └── SimulacionManager.java (Singleton)
├── factory/             # Creación de recursos
│   └── RecursoFactory.java (Factory)
├── observer/            # Sistema de eventos
│   ├── EventPublisher.java
│   ├── EmergenciaObserver.java
│   ├── ConsoleObserver.java
│   └── VisualObserver.java
├── worker/              # Hilos trabajadores
│   ├── Despachador.java
│   ├── OperadorLlamadas.java
│   ├── MonitorTiempoReal.java
│   └── MonitorVisual.java
├── ui/                  # Interfaz JavaFX
│   ├── EmergenciasApp.java
│   ├── model/
│   │   └── SimulacionConfig.java
│   ├── view/
│   │   ├── MainMenuView.java
│   │   └── SimulacionView.java
│   └── observer/
│       └── JavaFXObserver.java
├── util/                # Utilidades
│   ├── AnsiColors.java
│   └── ConsoleUI.java
└── Main.java / MainJavaFX.java

src/main/resources/
└── styles/              # CSS para JavaFX
    ├── main.css
    └── simulation.css
```

## 🎓 Conceptos Implementados

### Concurrencia

- `ExecutorService` para gestión de threads
- `BlockingQueue` para casos de emergencia
- `PriorityBlockingQueue` con scoring dinámico
- `ReentrantLock` para sincronización
- `volatile` y `synchronized` para thread-safety
- `AtomicLong` y `AtomicInteger` para contadores

### JavaFX

- `AnimationTimer` para animaciones de 60fps
- `Timeline` para animaciones basadas en tiempo
- `FadeTransition`, `ScaleTransition` para efectos
- `Canvas` y `GraphicsContext` para dibujo 2D
- `LinearGradient` para fondos animados
- `DropShadow`, `GaussianBlur` para efectos visuales

### Patrones de Diseño

- **Singleton**: Instancia única de SimulacionManager
- **Factory**: Creación centralizada de recursos
- **Observer**: Desacoplamiento mediante eventos
- **Strategy**: Cálculo de prioridades (preparado)

## 📈 Métricas de Simulación

El sistema calcula y muestra:

- Casos atendidos vs en cola
- Estado de cada ambulancia y equipo
- Tiempo de espera y tiempo total por caso
- Eventos en tiempo real
- Progreso de simulación

## 🔮 Próximas Características

- [ ] Base de datos H2 para persistencia
- [ ] Unit tests con JUnit 5
- [ ] Gráficos de barras/líneas con estadísticas
- [ ] Exportación de reportes PDF
- [ ] Modo replay de simulaciones
- [ ] Configuración avanzada (severidades, tiempos)
- [ ] Sonidos para eventos críticos
- [ ] Mapas reales con OpenStreetMap

## 👨‍💻 Autor

**Luis Toro** - IUDigital  
Sistema de Gestión de Emergencias Médicas

## 📝 Licencia

Proyecto educativo - IUDigital 2024

---

**¡Disfruta la simulación! 🚑⚕️**
