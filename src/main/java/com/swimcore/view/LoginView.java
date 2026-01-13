/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: LoginView.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Vista (View Layer) encargada de la interfaz de autenticación.
 * Implementa un contenedor de alto nivel (JFrame) utilizando un esquema de posicionamiento absoluto (Null Layout)
 * para garantizar una fidelidad visual estricta ("Pixel-Perfect") respecto al diseño UX/UI propuesto.
 *
 * Responsabilidades:
 * 1. Captura y sanitización de credenciales de acceso.
 * 2. Orquestación de la validación contra la Capa de Acceso a Datos (DAO).
 * 3. Gestión de feedback visual asíncrono (animaciones y temporizadores).
 * 4. Control de navegación hacia el contenedor principal (DashboardView).
 *
 * PRINCIPIOS DE PROGRAMACIÓN ORIENTADA A OBJETOS (POO):
 * 1. HERENCIA: Especialización de la clase `javax.swing.JFrame` para el comportamiento de ventana.
 * 2. ENCAPSULAMIENTO: Restricción de acceso a componentes de UI mediante modificadores `private`.
 * 3. COMPOSICIÓN: Inyección de dependencia temporal de `UserDAO` para la lógica de validación.
 *
 * PATRONES DE DISEÑO IMPLEMENTADOS:
 * - Observer: Implementación implícita mediante Listeners (ActionListener, MouseAdapter) para
 * el manejo de eventos dirigidos por el usuario.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.util.LanguageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Locale;

/**
 * Interfaz Gráfica de Usuario (GUI) para el inicio de sesión.
 * Gestiona el ciclo de vida de la autenticación y la transición de estados de la aplicación.
 */
public class LoginView extends JFrame {

    // --- DEFINICIÓN DE CONSTANTES VISUALES (PALETA CORPORATIVA) ---
    private final Color COLOR_FONDO = new Color(18, 18, 18);
    private final Color COLOR_CAMPOS = new Color(35, 35, 35);
    private final Color COLOR_TEXTO = new Color(200, 200, 200);
    private final Color COLOR_DORADO = new Color(200, 160, 51);

    // --- COMPONENTES SWING ---
    private JLabel lblSecurity;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    /**
     * Constructor.
     * Inicializa el árbol de componentes, configura el Layout Manager nulo y establece los manejadores de eventos.
     */
    public LoginView() {
        // Configuración de propiedades del contenedor raíz
        setTitle("SICONI - Acceso al Sistema");
        setSize(480, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Finaliza el proceso JVM al cerrar
        setLocationRelativeTo(null); // Centrado automático en viewport
        setResizable(false); // Restricción de redimensionamiento

        // Panel base con Layout Nulo para posicionamiento absoluto (x, y)
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(COLOR_FONDO);
        add(panel);

        // Cálculo de eje central para alineación simétrica
        int centerX = 240;

        // 1. MÓDULO DE IDENTIDAD VISUAL (BRANDING)
        JLabel lblLogo = new JLabel();
        lblLogo.setBounds(centerX - 140, 30, 280, 90);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        ajustarImagen(lblLogo, "/images/logo.png", 260); // Renderizado escalado
        panel.add(lblLogo);

        JLabel lblSub = new JLabel("DAYANA GUÉDEZ | SWIMWEAR");
        lblSub.setForeground(COLOR_DORADO);
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(centerX - 150, 125, 300, 20);
        panel.add(lblSub);

        // 2. MÓDULO DE INTERNACIONALIZACIÓN (I18N)
        int flagW = 85, flagH = 55, gap = 20;

        // Selector de Locale: Español
        JButton btnVe = new JButton();
        btnVe.setBounds(centerX - flagW - (gap/2), 160, flagW, flagH);
        estilizarBotonImagen(btnVe, "/images/ve.png");
        btnVe.setToolTipText("Español");
        agregarEfectoClick(btnVe);
        // Lambda Expression para cambio de contexto de idioma
        btnVe.addActionListener(e -> LanguageManager.setLocale(new Locale("es")));
        panel.add(btnVe);

        // Selector de Locale: Inglés
        JButton btnUs = new JButton();
        btnUs.setBounds(centerX + (gap/2), 160, flagW, flagH);
        estilizarBotonImagen(btnUs, "/images/us.png");
        btnUs.setToolTipText("English");
        agregarEfectoClick(btnUs);
        btnUs.addActionListener(e -> LanguageManager.setLocale(new Locale("en")));
        panel.add(btnUs);

        // 3. MÓDULO DE CREDENCIALES (INPUTS)
        int startY = 240, inputWidth = 320, inputX = centerX - (inputWidth / 2);

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setForeground(COLOR_TEXTO);
        lblUser.setBounds(inputX, startY, 200, 20);
        panel.add(lblUser);

        txtUser = new JTextField();
        txtUser.setBounds(inputX, startY + 25, inputWidth, 45);
        txtUser.setBackground(COLOR_CAMPOS);
        txtUser.setForeground(Color.WHITE);
        txtUser.setCaretColor(COLOR_DORADO);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtUser);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(COLOR_TEXTO);
        lblPass.setBounds(inputX, startY + 90, 200, 20);
        panel.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setBounds(inputX, startY + 115, inputWidth, 45);
        txtPass.setBackground(COLOR_CAMPOS);
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(COLOR_DORADO);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Listener de teclado para UX (Submit on Enter)
        txtPass.addActionListener(e -> validarYAnimar());
        panel.add(txtPass);

        // 4. INDICADOR DE ESTADO DE SEGURIDAD
        lblSecurity = new JLabel();
        int lockSize = 80;
        lblSecurity.setBounds(centerX - (lockSize/2), startY + 180, lockSize, lockSize);
        lblSecurity.setHorizontalAlignment(SwingConstants.CENTER);
        ajustarImagen(lblSecurity, "/images/lock_closed.jpg", lockSize);
        panel.add(lblSecurity);

        // 5. CONTROL DE ACCESO (TRIGGER)
        btnLogin = new JButton("INGRESAR AL SISTEMA");
        int btnWidth = 220;
        btnLogin.setBounds(centerX - (btnWidth/2), startY + 280, btnWidth, 50);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBackground(new Color(220, 0, 115)); // Acento Visual (Fucsia)
        btnLogin.setForeground(Color.WHITE);
        agregarEfectoClick(btnLogin);
        btnLogin.addActionListener(e -> validarYAnimar());
        panel.add(btnLogin);

        // Footer Informativo
        JLabel firma = new JLabel("Desarrollado por Johanna Guédez © 2026");
        firma.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        firma.setForeground(Color.GRAY);
        firma.setHorizontalAlignment(SwingConstants.CENTER);
        firma.setBounds(centerX - 150, 610, 300, 20);
        panel.add(firma);
    }

    /**
     * Lógica Core de Autenticación y Feedback.
     * Realiza validación híbrida (BD + Hardcoded Fallback), gestiona la integridad
     * del usuario administrador y coordina la transición de vistas.
     */
    private void validarYAnimar() {
        String u = txtUser.getText();
        String p = new String(txtPass.getPassword());
        UserDAO dao = new UserDAO();

        // VALIDACIÓN HÍBRIDA:
        // 1. Verificación persistente contra SQLite (accesoBD).
        // 2. Verificación en memoria 'Hardcoded' (accesoEmergencia) para recuperación de acceso.
        boolean accesoBD = (dao.login(u, p) != null);
        boolean accesoEmergencia = (u.equals("admin") && p.equals("1234"));

        if (accesoBD || accesoEmergencia) {

            // LÓGICA DE AUTORECUPERACIÓN (Self-Healing):
            // Si el acceso fue por emergencia y no existe registro en BD, se inyecta el usuario admin.
            if (!accesoBD && accesoEmergencia) {
                if (dao.findByUsername("admin") == null) {
                    dao.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
                }
            }

            // FEEDBACK VISUAL ASÍNCRONO
            txtUser.setEnabled(false); // Bloqueo de inputs para prevenir reenvíos
            txtPass.setEnabled(false);
            btnLogin.setText("ACCEDIENDO...");
            btnLogin.setBackground(new Color(46, 204, 113)); // Estado: Éxito (Verde)

            // Carga dinámica de recurso animado (GIF)
            try {
                URL gifUrl = getClass().getResource("/images/lock_animation.gif");
                if (gifUrl != null) {
                    ImageIcon gifIcon = new ImageIcon(gifUrl);
                    Image img = gifIcon.getImage().getScaledInstance(lblSecurity.getWidth(), lblSecurity.getHeight(), Image.SCALE_DEFAULT);
                    lblSecurity.setIcon(new ImageIcon(img));
                }
            } catch (Exception ex) { }

            // RETARDO DE TRANSICIÓN (UX)
            // Timer ejecuta la transición en el Event Dispatch Thread después de 1500ms
            Timer t = new Timer(1500, e -> {
                dispose(); // Liberación de recursos de la ventana actual
                // Navegación hacia el contenedor principal (Dashboard)
                new DashboardView().setVisible(true);
            });
            t.setRepeats(false);
            t.start();

        } else {
            // Manejo de excepción de negocio: Credenciales inválidas
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
            txtPass.setText("");
            txtPass.requestFocus(); // Retorno de foco para reintento rápido
        }
    }

    // --- MÉTODOS UTILITARIOS DE RENDERIZADO (HELPERS) ---

    /**
     * Carga y escala imágenes de recursos con suavizado (Anti-aliasing).
     * @param label Componente destino.
     * @param path Ruta relativa en el classpath.
     * @param w Ancho objetivo.
     */
    private void ajustarImagen(JLabel label, String path, int w) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                // SCALE_SMOOTH prioriza calidad visual sobre velocidad de renderizado
                Image img = icon.getImage().getScaledInstance(w, -1, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) { }
    }

    /**
     * Aplica estilos de botón transparente para iconos interactivos.
     */
    private void estilizarBotonImagen(JButton boton, String path) {
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(boton.getWidth(), -1, Image.SCALE_SMOOTH);
                boton.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) { }
    }

    /**
     * Implementa un efecto táctil de "presión" modificando las coordenadas del componente.
     * @param btn Botón objetivo.
     */
    private void agregarEfectoClick(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // Desplazamiento positivo en ejes X/Y para simular profundidad
                btn.setLocation(btn.getX()+2, btn.getY()+2);
            }
            public void mouseReleased(MouseEvent e) {
                // Retorno a posición original (Elasticidad)
                btn.setLocation(btn.getX()-2, btn.getY()-2);
            }
        });
    }
}