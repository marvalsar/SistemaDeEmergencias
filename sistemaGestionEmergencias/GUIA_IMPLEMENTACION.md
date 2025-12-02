# Sistema de Gestión de Emergencias Médicas - Guía de Implementación

## 🚀 Inicio Rápido para Implementar las Mejoras

Este documento te guiará paso a paso para implementar las mejoras propuestas en el `PLAN_DE_MEJORAMIENTO.md`.

---

## Fase 0: Corrección de Bugs Críticos (INMEDIATO)

### Bug #1: Monitor de Tiempo Real no funciona

**Ubicación**: `MonitorTiempoReal.java` línea 21

**Problema actual**:

```java
while (!corriendo) {  // ❌ ESTO ESTÁ INVERTIDO
    TimeUnit.SECONDS.sleep(2);
    // ...
}
```

**Corrección**:

```java
while (corriendo) {  // ✅ CORRECTO
    TimeUnit.SECONDS.sleep(2);
    // ...
}
```

**Impacto**: El monitor nunca se ejecuta porque la condición está invertida.

---

## Fase 1: Mejoras Incrementales (Comenzar Aquí)

### Paso 1.1: Actualizar `pom.xml` con dependencias básicas

**Tiempo estimado**: 15 minutos

Reemplaza tu `pom.xml` actual con esta versión mejorada (ver archivo completo en el plan):

```bash
# Después de actualizar pom.xml, ejecuta:
mvn clean install
```

**Verifica que funcione**:

```bash
mvn compile
mvn test
```

### Paso 1.2: Corregir bugs identificados

**Tiempo estimado**: 10 minutos

1. Corregir el bug en `MonitorTiempoReal.java`
2. Mejorar manejo de interrupciones en todos los workers

**Comandos de verificación**:

```bash
mvn compile
# Ejecutar y verificar que el monitor ahora funcione
```

---

## Fase 2: Refactorización de Arquitectura

### Paso 2.1: Crear nueva estructura de paquetes

**Tiempo estimado**: 30 minutos

```bash
# Desde la raíz del proyecto
mkdir -p src/main/java/org/iudigital/emergencias/{domain,service,repository,worker,manager,factory,observer,strategy,config,util,ui}
mkdir -p src/main/java/org/iudigital/emergencias/service/impl
mkdir -p src/main/java/org/iudigital/emergencias/repository/impl
mkdir -p src/main/java/org/iudigital/emergencias/strategy/impl
mkdir -p src/main/java/org/iudigital/emergencias/ui/{controller,view,component}
mkdir -p src/main/resources/{css,view,config}
mkdir -p src/test/java/org/iudigital/emergencias
```

### Paso 2.2: Mover clases existentes a nuevos paquetes

**Orden recomendado**:

1. **Domain** (sin dependencias):

   ```
   CasoEmergencia.java → domain/
   Ambulancia.java → domain/
   EquipoMedico.java → domain/
   Recursos.java → domain/ (renombrar a Recurso.java)
   Stoppable.java → worker/
   ```

2. **Workers** (dependen de domain):

   ```
   OperadorLlamadas.java → worker/OperadorLlamadasWorker.java
   Despachador.java → worker/DespachadorWorker.java
   MonitorTiempoReal.java → worker/MonitorWorker.java
   ```

3. **Manager** (coordinador principal):
   ```
   Main.java → Dividir en:
     - manager/SimulacionManager.java (lógica de simulación)
     - ui/ConsoleApp.java (entrada para modo consola)
   ```

**Script de ayuda** (PowerShell):

```powershell
# Navegar al directorio src/main/java/org/iudigital/emergencias
cd "c:\Luis Toro\IUDigital\SistemaDeEmergencias\sistemaGestionEmergencias\src\main\java\org\iudigital\emergencias"

# Mover archivos manteniendo imports
Move-Item CasoEmergencia.java domain/
Move-Item Ambulancia.java domain/
Move-Item EquipoMedico.java domain/
Move-Item Recursos.java domain/Recurso.java
Move-Item Stoppable.java ../worker/

# Ajustar packages en cada archivo después de mover
```

**IMPORTANTE**: Después de mover cada archivo, actualiza la declaración del package en la primera línea.

### Paso 2.3: Implementar Singleton para SimulacionManager

**Crear**: `src/main/java/org/iudigital/emergencias/manager/SimulacionManager.java`

```java
package org.iudigital.emergencias.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulacionManager {
    private static volatile SimulacionManager instance;
    private ExecutorService mainExecutor;
    private boolean enEjecucion = false;

    private SimulacionManager() {
        // Constructor privado
    }

    public static SimulacionManager getInstance() {
        if (instance == null) {
            synchronized (SimulacionManager.class) {
                if (instance == null) {
                    instance = new SimulacionManager();
                }
            }
        }
        return instance;
    }

    public void iniciar() {
        if (enEjecucion) {
            throw new IllegalStateException("Simulación ya está en ejecución");
        }
        enEjecucion = true;
        mainExecutor = Executors.newFixedThreadPool(10);
        // ... resto de lógica
    }

    public void detener() {
        enEjecucion = false;
        if (mainExecutor != null) {
            mainExecutor.shutdown();
        }
    }
}
```

### Paso 2.4: Implementar Factory para Recursos

**Crear**: `src/main/java/org/iudigital/emergencias/factory/RecursoFactory.java`

```java
package org.iudigital.emergencias.factory;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.EquipoMedico;

public class RecursoFactory {

    public static Ambulancia crearAmbulancia(int id) {
        return new Ambulancia(id);
    }

    public static EquipoMedico crearEquipoMedico(int id) {
        return new EquipoMedico(id);
    }
}
```

---

## Fase 3: Implementación de JavaFX (UI Básica)

### Paso 3.1: Crear aplicación JavaFX mínima

**Crear**: `src/main/java/org/iudigital/emergencias/ui/EmergenciasApp.java`

```java
package org.iudigital.emergencias.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmergenciasApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);

        TextArea logsArea = new TextArea();
        logsArea.setEditable(false);
        logsArea.setPrefRowCount(20);

        Button iniciarBtn = new Button("Iniciar Simulación");
        Button detenerBtn = new Button("Detener Simulación");

        iniciarBtn.setOnAction(e -> {
            logsArea.appendText("Simulación iniciada...\n");
            // TODO: Conectar con SimulacionManager
        });

        detenerBtn.setOnAction(e -> {
            logsArea.appendText("Simulación detenida.\n");
            // TODO: Detener simulación
        });

        root.getChildren().addAll(iniciarBtn, detenerBtn, logsArea);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Sistema de Gestión de Emergencias");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

**Ejecutar**:

```bash
mvn clean javafx:run
```

### Paso 3.2: Conectar UI con Simulación

Implementar patrón Observer para que la UI se actualice automáticamente:

**Crear**: `src/main/java/org/iudigital/emergencias/observer/EmergenciaObserver.java`

```java
package org.iudigital.emergencias.observer;

import org.iudigital.emergencias.domain.CasoEmergencia;

public interface EmergenciaObserver {
    void onNuevaEmergencia(CasoEmergencia caso);
    void onEmergenciaAsignada(CasoEmergencia caso);
    void onEmergenciaCompletada(CasoEmergencia caso);
}
```

**Modificar** `EmergenciasApp.java` para implementar el observer:

```java
public class EmergenciasApp extends Application implements EmergenciaObserver {
    private TextArea logsArea;

    @Override
    public void onNuevaEmergencia(CasoEmergencia caso) {
        // IMPORTANTE: Actualizar UI en el hilo de JavaFX
        Platform.runLater(() -> {
            logsArea.appendText(String.format(
                "📞 Nueva emergencia #%d: %s\n",
                caso.getCasoId(),
                caso.getSeveridad()
            ));
        });
    }

    // ... otros métodos
}
```

---

## Fase 4: Base de Datos (Opcional pero Recomendado)

### Paso 4.1: Configurar H2 Database

**Crear**: `src/main/java/org/iudigital/emergencias/config/DatabaseConfig.java`

```java
package org.iudigital.emergencias.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String JDBC_URL = "jdbc:h2:./data/emergencias;AUTO_SERVER=TRUE";
    private static HikariDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(10);

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static void inicializarEsquema() throws SQLException {
        try (Connection conn = getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS emergencias (
                    id BIGINT PRIMARY KEY,
                    severidad VARCHAR(20),
                    lugar VARCHAR(255),
                    hora_recibido TIMESTAMP
                )
            """);
        }
    }
}
```

### Paso 4.2: Crear repositorio básico

**Crear**: `src/main/java/org/iudigital/emergencias/repository/EmergenciaRepository.java`

```java
package org.iudigital.emergencias.repository;

import org.iudigital.emergencias.config.DatabaseConfig;
import org.iudigital.emergencias.domain.CasoEmergencia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergenciaRepository {

    public void guardar(CasoEmergencia caso) {
        String sql = "INSERT INTO emergencias (id, severidad, lugar, hora_recibido) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, caso.getCasoId());
            pstmt.setString(2, caso.getSeveridad().name());
            pstmt.setString(3, caso.getLugar());
            pstmt.setTimestamp(4, new Timestamp(caso.getHoraRecibido()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CasoEmergencia> buscarTodos() {
        List<CasoEmergencia> casos = new ArrayList<>();
        String sql = "SELECT * FROM emergencias";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Reconstruir CasoEmergencia desde BD
                // ... (necesitarás agregar un constructor apropiado)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return casos;
    }
}
```

---

## Fase 5: Testing

### Paso 5.1: Crear tests básicos

**Crear**: `src/test/java/org/iudigital/emergencias/domain/CasoEmergenciaTest.java`

```java
package org.iudigital.emergencias.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CasoEmergenciaTest {

    @Test
    void deberiaCompararPorPrioridad() {
        CasoEmergencia critico = new CasoEmergencia(
            CasoEmergencia.Severity.CRITICO,
            "Lugar-1"
        );

        CasoEmergencia leve = new CasoEmergencia(
            CasoEmergencia.Severity.LEVE,
            "Lugar-2"
        );

        // El caso crítico debe tener mayor prioridad
        assertTrue(critico.compareTo(leve) < 0);
    }

    @Test
    void deberiaTenerIdUnico() {
        CasoEmergencia caso1 = new CasoEmergencia(
            CasoEmergencia.Severity.MODERADO,
            "Lugar-3"
        );

        CasoEmergencia caso2 = new CasoEmergencia(
            CasoEmergencia.Severity.MODERADO,
            "Lugar-4"
        );

        assertNotEquals(caso1.getCasoId(), caso2.getCasoId());
    }
}
```

**Ejecutar tests**:

```bash
mvn test
```

---

## Checklist de Implementación Incremental

### ✅ Fase 0: Correcciones Inmediatas (30 min)

- [ ] Corregir bug en `MonitorTiempoReal.java`
- [ ] Actualizar `pom.xml` con dependencias básicas
- [ ] Verificar que compile: `mvn clean compile`

### ✅ Fase 1: Refactorización Básica (2-3 horas)

- [ ] Crear nueva estructura de paquetes
- [ ] Mover clases a sus paquetes correspondientes
- [ ] Actualizar imports en todas las clases
- [ ] Implementar `SimulacionManager` (Singleton)
- [ ] Implementar `RecursoFactory` (Factory Pattern)
- [ ] Verificar que funcione igual que antes

### ✅ Fase 2: JavaFX Básico (2-3 horas)

- [ ] Crear `EmergenciasApp` básica
- [ ] Conectar botones con `SimulacionManager`
- [ ] Implementar patrón Observer básico
- [ ] Mostrar logs en TextArea
- [ ] Ejecutar: `mvn javafx:run`

### ✅ Fase 3: UI Avanzada (4-6 horas)

- [ ] Crear archivos FXML para vistas
- [ ] Implementar controladores separados
- [ ] Agregar ListView para emergencias
- [ ] Agregar ListView para recursos
- [ ] Mostrar estadísticas en tiempo real
- [ ] Agregar CSS para estilos

### ✅ Fase 4: Base de Datos (2-3 horas)

- [ ] Configurar H2 Database
- [ ] Crear esquema inicial
- [ ] Implementar `EmergenciaRepository`
- [ ] Guardar casos automáticamente
- [ ] Agregar funcionalidad de carga de histórico

### ✅ Fase 5: Testing (2-3 horas)

- [ ] Tests unitarios para domain
- [ ] Tests para services
- [ ] Tests de concurrencia
- [ ] Alcanzar 70%+ cobertura

### ✅ Fase 6: Pulido Final (1-2 horas)

- [ ] Logging estructurado (SLF4J + Logback)
- [ ] Archivo de configuración externalizado
- [ ] README con instrucciones
- [ ] Documentación JavaDoc
- [ ] Preparar video demostrativo

---

## Comandos Útiles

### Maven

```bash
# Limpiar y compilar
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar aplicación JavaFX
mvn javafx:run

# Generar JAR ejecutable
mvn clean package

# Ejecutar JAR
java -jar target/sistema-gestion-emergencias-2.0.0-jar-with-dependencies.jar
```

### Git (Control de Versiones)

```bash
# Inicializar repositorio
git init

# Agregar archivos
git add .

# Commit
git commit -m "Implementar fase 1: Refactorización de arquitectura"

# Crear branch para feature
git checkout -b feature/javafx-ui

# Ver cambios
git status
git diff
```

---

## Recursos de Ayuda

### Documentación

- [JavaFX Documentation](https://openjfx.io/javadoc/21/)
- [Maven Guide](https://maven.apache.org/guides/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [H2 Database Tutorial](https://www.h2database.com/html/tutorial.html)

### Ejemplos de Código

- Ver `PLAN_DE_MEJORAMIENTO.md` para ejemplos completos
- Consultar carpeta `doc/` para especificaciones

---

## Solución de Problemas Comunes

### Error: "JavaFX runtime components are missing"

```bash
# Asegúrate de que pom.xml incluye las dependencias de JavaFX
# Ejecuta con el plugin de JavaFX:
mvn javafx:run
```

### Error: "Cannot find symbol" después de mover clases

```bash
# Actualiza los imports en todas las clases afectadas
# IntelliJ IDEA: Alt+Enter sobre el error → Import class
# VS Code: Ctrl+. sobre el error → Import
```

### Tests no se ejecutan

```bash
# Verifica que las clases de test terminen en "Test.java"
# Verifica que uses @Test de JUnit 5 (jupiter)
mvn test -X  # modo debug para ver detalles
```

---

## Contacto y Soporte

Si encuentras problemas durante la implementación:

1. Revisa el `PLAN_DE_MEJORAMIENTO.md` completo
2. Consulta la documentación oficial de las tecnologías
3. Revisa los requisitos en `doc/Caso_Estudio_Emergencias.pdf`

**¡Éxito con la implementación! 🚀**
