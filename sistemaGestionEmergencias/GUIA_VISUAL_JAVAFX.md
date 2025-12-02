# 🎨 Guía Visual de la Aplicación JavaFX

## 🚀 Estado de Implementación: COMPLETO ✅

La aplicación JavaFX ha sido implementada exitosamente con **animaciones de alta calidad**, interfaz interactiva y
gráficos espectaculares.

---

## 📱 Vistas Implementadas

### 1. MENÚ PRINCIPAL (MainMenuView)

#### Características Visuales:

- ✨ **Fondo Animado**: LinearGradient de azul (#1a237e → #283593) con animación de pulso
- 🌟 **Título Brillante**: "SISTEMA DE GESTIÓN DE EMERGENCIAS" con DropShadow animado (radio 15-25px, ciclo 1s)
- 🔮 **Panel Glass Morphism**: Fondo rgba(255,255,255,0.15) con GaussianBlur(5px)

#### Controles Interactivos:

1. **Slider Ambulancias** (1-10)

   - Valor por defecto: 4
   - Color: Verde (#4caf50)
   - Animación: ScaleTransition al interactuar (1.0 → 1.2 → 1.0)

2. **Slider Equipos Médicos** (1-5)

   - Valor por defecto: 2
   - Feedback visual instantáneo

3. **Slider Operadores** (1-5)

   - Valor por defecto: 2
   - Label con valor actualizado en tiempo real

4. **Slider Duración** (30-300 segundos)

   - Valor por defecto: 60
   - Muestra segundos dinámicamente

5. **CheckBox Modo Turbo**
   - Activa simulación acelerada
   - Efecto hover

#### Botones:

- **🚀 INICIAR SIMULACIÓN**: Verde gradient (#4caf50 → #45a049)

  - Hover: Scale 1.05 + DropShadow intensificada
  - Transición: FadeOut 0.5s al cambiar de vista

- **❌ SALIR**: Rojo gradient (#f44336 → #e53935)
  - Cierra la aplicación con Platform.exit()

#### Animación de Entrada:

- FadeTransition: opacity 0 → 1 (1s)
- TranslateTransition: translateY 50 → 0 (1s)
- Efecto de "flotación suave"

---

### 2. VISTA DE SIMULACIÓN (SimulacionView)

#### Panel Superior:

- **Título**: "🚨 SIMULACIÓN EN VIVO" (28px, bold)
- **Timer**: "Tiempo: Xs / Ys" (20px, color verde #64ff64)
- **ProgressBar**: Ancho 300px, color accent #4caf50
- **Label Casos**: "X atendidos | Y en cola"

#### Mapa Central (Canvas 900x700):

##### Grid Animado:

- Líneas verticales/horizontales cada 50px
- Color: rgba(255,255,255,0.05)
- Fondo: rgb(30,40,60)

##### 3 Hospitales Fijos:

1. **Hospital Central** (150, 150)
2. **Hospital Norte** (750, 150)
3. **Hospital Sur** (450, 550)

Cada hospital:

- Edificio gris (80x80px)
- Cruz roja grande (60x10px + 10x60px)
- Brillo pulsante: strokeOval con radio variable (40-80px)
- Label con nombre debajo

##### Ambulancias Animadas:

- **Sprite**: RoundRect 30x20px
- **Ventanas**: 2 rectángulos celestes
- **Cruz Roja**: 4x16 + 16x4 pixels
- **Rotación**: Dinámica según dirección de movimiento
- **Velocidad**: 3 pixels/frame
- **Sombra Pulsante**: Cuando está activa (sin(time)\*5+25)

**Colores según estado:**

- 🟢 Verde: DISPONIBLE
- 🟡 Amarillo: EN_RUTA
- 🔴 Rojo: OCUPADA
- 🔵 Azul: RETORNANDO

##### Casos de Emergencia:

- **Círculos Pulsantes**: size = 20 + sin(pulse)\*5
- **Ícono**: "!" en blanco
- **Colores por severidad:**
  - 🔴 Rojo: CRITICO
  - 🟠 Naranja: GRAVE
  - 🟡 Amarillo: MODERADO
  - 🟢 Verde Claro: LEVE

#### Panel Derecho (350px):

##### Título:

- "📊 ESTADÍSTICAS" (22px, bold)

##### Panel de Recursos:

- Fondo: rgba(255,255,255,0.1)
- Border-radius: 10px
- Lista de recursos con indicadores:
  - 🚑 AMB-X: [●] STATUS
  - ⚕️ EQM-X: [●] STATUS
- Círculos de estado con colores matching

##### Log de Eventos:

- "📝 EVENTOS RECIENTES" (18px)
- ListView con scroll
- Altura: 300px
- Fondo: rgba(0,0,0,0.5)
- Máximo 50 eventos
- Formato: `[HH:mm:ss] 🔔 Evento descripción`

#### Panel Inferior (Controles):

- **⏸️ Pausar**: Amarillo (#ffc107)
- **⏹️ Detener**: Rojo (#f44336)
- **🏠 Menú**: Gris (#607d8b)

Todos con:

- Hover: Scale 1.05
- Shadow animada
- Border-radius: 8px

---

## 🎭 Animaciones Implementadas

### AnimationTimer (60 FPS):

```
handle(now) {
  - clearCanvas()
  - drawGrid()
  - drawHospitales() → pulso concéntrico
  - drawCasos() → size pulsante
  - updateAmbulances() → movimiento smooth
  - drawAmbulances() → rotación + sombra
}
```

### Timeline Updates:

- **UI Updater**: 0.5s interval
  - Actualizar timer
  - Actualizar progressBar
  - Actualizar casos label
  - Refrescar panel de estadísticas

### Transiciones:

- **FadeTransition**: Menú → Simulación (0.5s)
- **ScaleTransition**: Sliders (100ms, 1.0→1.2→1.0)
- **ScaleTransition**: Botones hover (100ms, 1.0→1.05)
- **ParallelTransition**: Intro del menú (fade + translate)

---

## 🎨 Sistema de Colores

### Paleta Principal:

- **Azul Oscuro**: #1a237e (fondo primario)
- **Verde**: #4caf50 (éxito, disponible)
- **Amarillo**: #ffc107 (advertencia, en ruta)
- **Rojo**: #f44336 (crítico, ocupado)
- **Azul Claro**: #2196f3 (retornando)
- **Gris**: #607d8b (neutral)

### Transparencias:

- Paneles: rgba(0,0,0,0.3-0.4)
- Glass effect: rgba(255,255,255,0.15)
- Borders: rgba(255,255,255,0.2)
- Sombras ambulancia: rgba(color,0.3)

---

## 💻 Ejecución

### Compilar y Ejecutar:

```bash
mvn clean compile
mvn javafx:run
```

### Lo que verás:

1. **Ventana inicial**: 1200x800, maximizada
2. **Menú animado** con intro suave
3. **Sliders interactivos** con feedback visual
4. **Al presionar START**: Transición fade → Simulación
5. **Mapa en vivo**:
   - Grid estático
   - 3 hospitales con brillo pulsante
   - Ambulancias moviéndose suavemente
   - Casos apareciendo como círculos pulsantes
   - Ambulancias rotando hacia su destino
6. **Panel de estadísticas** actualizándose cada 0.5s
7. **Log de eventos** scrolleando automáticamente

---

## 🏆 Logros Técnicos

✅ **60 FPS** con AnimationTimer  
✅ **Thread-safe** con Platform.runLater()  
✅ **Observer Pattern** integrado con JavaFX  
✅ **Canvas 2D** con transformaciones y rotaciones  
✅ **CSS styling** profesional  
✅ **Animaciones complejas** sincronizadas  
✅ **Responsive UI** con actualización en tiempo real  
✅ **Glass morphism** y efectos modernos  
✅ **Gradientes animados** con Timeline  
✅ **Sombras dinámicas** con DropShadow

---

## 🎯 Próximos Pasos Sugeridos

1. **Ejecutar**: `mvn javafx:run`
2. **Experimentar** con diferentes configuraciones
3. **Observar** las animaciones fluidas
4. **Monitorear** el log de eventos en tiempo real
5. **Ajustar** parámetros con sliders para ver comportamiento

---

## 📸 Descripción Visual Esperada

### Menú Principal:

```
┌──────────────────────────────────────────────────────────┐
│                    [Gradiente Azul Animado]              │
│                                                          │
│         ✨ SISTEMA DE GESTIÓN DE EMERGENCIAS ✨          │
│                  [con brillo pulsante]                   │
│                                                          │
│  ╔════════════════════════════════════════════════════╗  │
│  ║   [Panel Glass Morphism - fondo translúcido]      ║  │
│  ║                                                    ║  │
│  ║   🚑 Ambulancias:     [====●====] 4               ║  │
│  ║   ⚕️  Equipos Médicos: [==●======] 2               ║  │
│  ║   📞 Operadores:      [==●======] 2               ║  │
│  ║   ⏱️  Duración (seg):  [====●====] 60              ║  │
│  ║                                                    ║  │
│  ║   ☑️ Modo Turbo                                    ║  │
│  ║                                                    ║  │
│  ║        [🚀 INICIAR SIMULACIÓN]  [❌ SALIR]        ║  │
│  ╚════════════════════════════════════════════════════╝  │
└──────────────────────────────────────────────────────────┘
```

### Vista Simulación:

```
┌────────────────────────────────────────────────────────────────┐
│ 🚨 SIMULACIÓN EN VIVO │ Tiempo: 15s/60s [█████████░░] 25%     │
│                       │ Casos: 5 atendidos | 2 en cola         │
├────────────────────────────────────┬───────────────────────────┤
│                                    │ 📊 ESTADÍSTICAS           │
│  [Grid 50x50]                      │                           │
│                                    │ 🚑 AMB-1: ● EN_RUTA       │
│    🏥 Hospital Central             │ 🚑 AMB-2: ● OCUPADA       │
│      [edificio+cruz]               │ 🚑 AMB-3: ● DISPONIBLE    │
│                                    │ 🚑 AMB-4: ● RETORNANDO    │
│         🚑→                         │ ⚕️ EQM-1: ● ASIGNADO      │
│           (ambulancia)             │ ⚕️ EQM-2: ● DISPONIBLE    │
│                                    │                           │
│    ⚠️ (caso pulsante)               │ 📝 EVENTOS RECIENTES      │
│                🏥 Hospital Norte   │ ┌─────────────────────┐   │
│                 [edificio+cruz]    │ │[19:40:52] 🆕 Nueva..│   │
│                                    │ │[19:40:50] 🚑 Amb...  │   │
│  🚑↗                                │ │[19:40:48] ⚕️ Eq...   │   │
│                                    │ │[19:40:45] ✅ Caso...│   │
│         🏥 Hospital Sur             │ │[19:40:43] 🚨 Amb... │   │
│          [edificio+cruz]           │ │            ...       │   │
│                                    │ └─────────────────────┘   │
├────────────────────────────────────┴───────────────────────────┤
│      [⏸️ Pausar]  [⏹️ Detener]  [🏠 Menú]                      │
└────────────────────────────────────────────────────────────────┘
```

---

**🎉 ¡APLICACIÓN JAVAFX COMPLETAMENTE FUNCIONAL CON ANIMACIONES DE ALTA CALIDAD! 🎉**

La simulación está lista para ejecutarse con:

- Menú interactivo con efectos visuales modernos
- Mapa animado con ambulancias moviéndose en tiempo real
- Dashboard de estadísticas actualizado dinámicamente
- Log de eventos con scroll automático
- Transiciones suaves entre vistas
- Efectos visuales profesionales (glass morphism, sombras, brillos)
