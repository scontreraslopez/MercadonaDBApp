# Proyecto MercadonaDB

## Descripción General

Este proyecto es una aplicación Android de ejemplo que demuestra cómo construir una app simple pero robusta utilizando tecnologías modernas de Android. La aplicación carga un catálogo de productos desde un fichero CSV (`mercadonia.csv`), lo almacena en una base de datos local (Room) y permite al usuario buscar productos basados en una categoría y una "estrategia de compra" (ej. el más barato, el de mejor valor, etc.).

El objetivo principal es servir como material didáctico para entender conceptos clave como la arquitectura MVVM, la persistencia de datos con Room, la gestión de estado en Jetpack Compose y el manejo de dependencias con Gradle Version Catalogs.

## Estructura de Ramas

El repositorio está organizado en dos ramas principales para facilitar el aprendizaje.

### Rama `main` (Punto de Partida)

La rama `main` contiene la estructura inicial del proyecto y el código base. Es el punto de partida ideal para que los alumnos implementen la funcionalidad principal por sí mismos.

**Tareas a realizar desde esta rama:**

*   Conectar la interfaz de usuario (`ShopScreen`) con el `ShopScreenViewModel`.
*   Implementar la lógica en el `ViewModel` para observar los cambios del repositorio y actualizar el estado de la UI (`UiState`).
*   Completar las consultas (`Queries`) necesarias en el `ProductDao` para obtener las categorías y buscar productos.
*   Asegurar que la base de datos Room se inicializa y se puebla correctamente a partir del fichero CSV la primera vez que se ejecuta la aplicación.
*   Implementar la lógica de las "estrategias de compra" para seleccionar un producto según el criterio elegido.

### Rama `develop` (Solución Propuesta)

La rama `develop` contiene una solución completa y funcional que implementa todas las características descritas. Sirve como una guía de referencia para comparar y entender una posible implementación.

**Características implementadas:**

*   **Arquitectura MVVM:** Separación clara de responsabilidades entre la UI (`ShopScreen`), el `ShopScreenViewModel` y la capa de datos (`ProductsRepository`).
*   **Base de Datos Room:** Se utiliza `MercadoniaDatabase` y `ProductDao` para la persistencia local. La base de datos se autocompleta desde `assets/database/mercadonia.csv` al crearse por primera vez gracias a un `RoomDatabase.Callback`.
*   **Patrón Repositorio (Singleton):** `ProductsRepository` actúa como única fuente de verdad (`Single Source of Truth`), abstrayendo el origen de los datos (Room) del resto de la aplicación.
*   **UI Reactiva con Jetpack Compose:** La `ShopScreen` observa un `StateFlow` del `ViewModel` para reaccionar a los cambios de estado (`ShopScreenUiState`), creando una UI declarativa y eficiente.
*   **Gestión de Dependencias Moderna:** Todas las dependencias del proyecto se gestionan a través de un **Gradle Version Catalog** (`gradle/libs.versions.toml`), centralizando las versiones y mejorando la legibilidad de los scripts de `build.gradle.kts`.

## Arquitectura y Tecnologías

*   **UI:** 100% Jetpack Compose.
*   **Arquitectura:** MVVM (Model-View-ViewModel).
*   **Gestión de Estado:** `ViewModel`, `StateFlow` y una clase `UiState`.
*   **Capa de Datos:** Patrón Repositorio implementado como un objeto Singleton.
*   **Persistencia de Datos:** Room Database.
*   **Inyección de Dependencias:** Manual (a través del método `initialize()` del repositorio).
*   **Asincronía:** Corrutinas de Kotlin (`CoroutineScope`, `viewModelScope`).
*   **Build System:** Gradle con scripts en Kotlin (KTS) y Version Catalogs.

## Posibles Mejoras (Para Alumnos Avanzados)

Aunque la solución en la rama `develop` es funcional, siempre hay espacio para mejorar. Aquí hay algunas ideas:

1.  **Inyección de Dependencias con Hilt/Koin:** Reemplazar el repositorio Singleton con inicialización manual por un sistema de inyección de dependencias como Hilt/Koin para un código más desacoplado y fácil de testear.
2.  **Manejo de Errores Avanzado:** Implementar un sistema más robusto para notificar al usuario si el fichero CSV no se encuentra, si la base de datos falla o si una categoría no tiene productos.
3.  **Tests Unitarios y de UI:** Escribir tests unitarios para el `ViewModel` y el `Repository`, y tests de instrumentación para verificar el correcto funcionamiento de la base de datos y la UI.
4.  **Mejoras de UX/UI:** Añadir animaciones de transición, un indicador de carga visual mientras se busca un producto y una pantalla de detalle al pulsar sobre el resultado.
5.  **Navegación:** Utilizar `Navigation-Compose` para expandir la aplicación con más pantallas, como una lista completa de productos por categoría.
6.  **Crear un Contenedor de Dependencias:** Implementar un `AppContainer` a nivel de la clase `Application` para gestionar el ciclo de vida del `ProductsRepository` y otras dependencias, en lugar de usar un `object` singleton.

## Cómo Empezar

1.  Clona el repositorio: `git clone <url-del-repositorio>`.
2.  Abre el proyecto con una versión reciente de Android Studio.
3.  Selecciona la rama que deseas explorar (`git checkout main` o `git checkout develop`).
4.  Deja que Android Studio sincronice el proyecto con Gradle.
5.  Ejecuta la aplicación en un emulador o dispositivo físico.