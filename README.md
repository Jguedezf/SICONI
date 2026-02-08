# SICONI 🩱 | Sistema de Control de Negocio e Inventario
> **Proyecto Académico: Técnicas de Programación III**

---

## 👤 Información del Autor
* **Estudiante:** Johanna Gabriela Guédez Flores
* **Cédula de Identidad:** V-14.089.807
* **Carrera:** Ingeniería en Informática (IV Semestre)
* **Institución:** Universidad Nacional Experimental de Guayana (UNEG)
* **Profesora:** Ing. Dubraska Roca
* **Fecha de Entrega:** Febrero 2026

---

## 1. Descripción del Proyecto
SICONI es un software de escritorio desarrollado para la gestión operativa de **Dayana Guédez | Swimwear**. El sistema soluciona la problemática de control de inventarios de materia prima y productos terminados, permitiendo además la gestión de pedidos personalizados para atletas y el seguimiento financiero de abonos y ventas multimoneda.

## 2. Especificaciones Técnicas (Stack)
* **Lenguaje:** Java 17 (LTS).
* **Paradigma:** Programación Orientada a Objetos (POO).
* **Arquitectura:** Modelo-Vista-Controlador (MVC) con Capa de Acceso a Datos (DAO).
* **Persistencia:** SQLite (Motor relacional embebido).
* **Interfaz Gráfica:** Java Swing + FlatLaf (Dark Luxury Theme).
* **Generación de Reportes:** iText PDF 5.x.
* **Control de Fechas:** LGoodDatePicker.

---

## 3. Matriz de Funcionalidades Significativas
El sistema implementa 10 requerimientos de ingeniería basados en el análisis orientado a objetos:
1.  **Seguridad:** Control de acceso por roles (ADMIN/VENDEDOR) mediante `LoginController`.
2.  **Finanzas:** Gestión de tasa cambiaria (BCV) dinámica y persistente.
3.  **Logística:** Alerta automática de stock crítico basada en umbrales de seguridad.
4.  **CRM:** Gestión de perfiles de clientes y especificaciones de tallaje para atletas.
5.  **Trazabilidad:** Generación automática de IDs correlativos para pedidos y facturación.
6.  **Integridad:** Manejo de transacciones atómicas (`Commit/Rollback`) en el módulo de abonos.
7.  **UX/UI:** Búsqueda predictiva y filtrado dinámico mediante `Stream API` y Lambdas.
8.  **i18n:** Soporte de internacionalización completo (Español/Inglés).
9.  **Reporting:** Motor de exportación de recibos y auditorías a formato vectorial PDF.
10. **Auditoría:** Registro inmutable de movimientos de inventario (Kardex Técnico).

---

## 4. Estructura del Proyecto (Source Tree)
```text
src/com/swimcore/
├── controller/     # Lógica de Negocio y Controladores (MVC)
├── dao/            # Capa de Persistencia y Consultas SQL (DAO)
├── model/          # Entidades y Objetos de Transferencia (DTO)
├── util/           # Clases Utilitarias (PDF, Moneda, DB, Sonido)
└── view/           # Interfaces Gráficas y Componentes Swing
    ├── components/ # Widgets Personalizados (Botones, Títulos)
    └── dialogs/    # Ventanas Modales y Formularios
resources/          # Imágenes, Archivos .properties e Iconografía
siconi.db           # Base de Datos SQLite principal

5. Instrucciones de Configuración y Ejecución
Importación: Clonar el repositorio y abrir en IntelliJ IDEA.

SDK: Asegurarse de tener configurado el JDK 17 en Project Structure.

Librerías: Vincular los archivos JAR ubicados en la carpeta /lib (FlatLaf, SQLite, iText).

Persistencia: La base de datos siconi.db se inicializa automáticamente al primer arranque mediante DatabaseSetup.java.

Arranque: Ejecutar la clase Main.java ubicada en com.swimcore.

Repositorio: 
https://github.com/Jguedezf/SICONI.git
Credenciales de Acceso:
Usuario: admin | Clave: 1234