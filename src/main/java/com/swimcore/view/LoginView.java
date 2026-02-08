/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: LoginView.java
 * VERSIÓN: 2.7.1 (Footer Translation Fix)
 * FECHA: 07 de Febrero de 2026
 * HORA: 11:30 AM (Hora de Venezuela)
 * * DESCRIPCIÓN: Ventana de acceso principal con soporte de internacionalización completo.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.SoundManager;
import com.swimcore.view.dialogs.UserManagementDialog;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

/**
 * [VISTA - MVC] Clase que implementa la interfaz gráfica para el módulo de autenticación.
 * [POO - HERENCIA] Se extiende de la clase JFrame para heredar las propiedades de una ventana.
 * * FUNCIONALIDAD 1: Control de Acceso y Seguridad del Sistema.
 */
public class LoginView extends JFrame {

    // [POO - ENCAPSULAMIENTO] Se definen atributos privados y finales (constantes) para el estilo visual.
    private final Color COLOR_FONDO = new Color(18, 18, 18);
    private final Color COLOR_CAMPOS = new Color(35, 35, 35);
    private final Color COLOR_TEXTO = new Color(200, 200, 200);
    private final Color COLOR_DORADO = new Color(200, 160, 51);

    // [POO - AGREGACIÓN] Componentes de la interfaz de usuario contenidos en la clase.
    private JLabel lblUser, lblPass;
    private JLabel lblSecurity;
    private JLabel lblAdminShield;

    // [CORRECCIÓN] Etiqueta de pie de página movida al ámbito de clase para permitir su traducción dinámica
    private JLabel lblFooter;

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    private Image backgroundImage;

    /**
     * [POO - CONSTRUCTOR] Método encargado de la inicialización del objeto.
     * Se configuran las propiedades de la ventana y se instancian los componentes gráficos.
     */
    public LoginView() {
        setSize(480, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Gestión de Recursos: Se realiza la carga de la imagen de fondo con manejo de excepciones.
        try {
            URL url = getClass().getResource("/images/login_bg.png");
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de fondo: " + e.getMessage());
        }

        // [POO - POLIMORFISMO] Clase anónima que sobreescribe el método paintComponent.
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(COLOR_FONDO);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panel.setLayout(null);
        add(panel);

        int centerX = 240;

        // 1. IDENTIDAD CORPORATIVA (BRANDING)
        JLabel lblLogo = new JLabel();
        lblLogo.setBounds(centerX - 140, 30, 280, 90);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        ajustarImagen(lblLogo, "/images/logo.png", 260);
        panel.add(lblLogo);

        JLabel lblSub = new JLabel("DAYANA GUÉDEZ | SWIMWEAR");
        lblSub.setForeground(COLOR_DORADO);
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(centerX - 150, 125, 300, 20);
        panel.add(lblSub);

        // 2. INTERNACIONALIZACIÓN (I18N)
        int flagW = 85, flagH = 55, gap = 20;
        JButton btnVe = new JButton();
        btnVe.setBounds(centerX - flagW - (gap/2), 160, flagW, flagH);
        estilizarBotonImagen(btnVe, "/images/ve.png");
        // [POO] Se utilizan Expresiones Lambda para la gestión de eventos de acción.
        btnVe.addActionListener(e -> {
            LanguageManager.setLocale(new Locale("es", "VE"));
            updateTexts();
        });
        panel.add(btnVe);

        JButton btnUs = new JButton();
        btnUs.setBounds(centerX + (gap/2), 160, flagW, flagH);
        estilizarBotonImagen(btnUs, "/images/us.png");
        btnUs.addActionListener(e -> {
            LanguageManager.setLocale(new Locale("en", "US"));
            updateTexts();
        });
        panel.add(btnUs);

        // 3. CAMPOS DE ENTRADA DE DATOS
        int startY = 240, inputWidth = 320, inputX = centerX - (inputWidth / 2);

        lblUser = new JLabel();
        lblUser.setForeground(COLOR_TEXTO);
        lblUser.setBounds(inputX, startY, 200, 20);
        panel.add(lblUser);

        // --- CAMPO USUARIO ---
        txtUser = new JTextField();
        txtUser.setBounds(inputX, startY + 25, inputWidth, 45);
        txtUser.setBackground(COLOR_CAMPOS);
        txtUser.setForeground(Color.WHITE);
        txtUser.setCaretColor(COLOR_DORADO);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Se aplica un borde compuesto para estilizar el campo.
        txtUser.setBorder(new CompoundBorder(
                new LineBorder(COLOR_DORADO, 1),
                new EmptyBorder(0, 10, 0, 0)
        ));
        panel.add(txtUser);

        lblPass = new JLabel();
        lblPass.setForeground(COLOR_TEXTO);
        lblPass.setBounds(inputX, startY + 90, 200, 20);
        panel.add(lblPass);

        // --- CAMPO CONTRASEÑA ---
        txtPass = new JPasswordField();
        txtPass.setBounds(inputX, startY + 115, inputWidth, 45);
        txtPass.setBackground(COLOR_CAMPOS);
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(COLOR_DORADO);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBorder(new CompoundBorder(
                new LineBorder(COLOR_DORADO, 1),
                new EmptyBorder(0, 10, 0, 0)
        ));
        txtPass.addActionListener(e -> validarYAnimar());
        panel.add(txtPass);

        // 4. COMPONENTE VISUAL DE SEGURIDAD
        lblSecurity = new JLabel();
        int lockSize = 105;
        lblSecurity.setBounds(centerX - (lockSize/2), startY + 180, lockSize, lockSize);
        lblSecurity.setHorizontalAlignment(SwingConstants.CENTER);
        ajustarImagen(lblSecurity, "/images/lock_closed.jpg", lockSize);
        panel.add(lblSecurity);

        // 5. BOTÓN DE INGRESO
        btnLogin = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                // [POO - POLIMORFISMO] Se personaliza el renderizado del botón mediante Graphics2D.
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color colorTop = new Color(255, 50, 150);
                Color colorBottom = new Color(180, 0, 100);
                if (getModel().isRollover()) {
                    colorTop = colorTop.brighter();
                    colorBottom = colorBottom.brighter();
                }
                GradientPaint gp = new GradientPaint(0, 0, colorTop, 0, getHeight(), colorBottom);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(COLOR_DORADO);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        int btnWidth = 220;
        btnLogin.setBounds(centerX - (btnWidth/2), startY + 285, btnWidth, 50);
        btnLogin.setText(LanguageManager.get("login.button"));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        agregarEfectoClick(btnLogin);
        btnLogin.addActionListener(e -> validarYAnimar());
        panel.add(btnLogin);

        // 6. ACCESO ADMINISTRATIVO (ESCUDO)
        lblAdminShield = new JLabel("🛡️");
        lblAdminShield.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblAdminShield.setForeground(new Color(200, 160, 51, 100));
        lblAdminShield.setBounds(centerX - 15, startY + 345, 40, 40);
        lblAdminShield.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAdminShield.setToolTipText("Seguridad y Usuarios");
        lblAdminShield.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { abrirGestionSeguridad(); }
            @Override
            public void mouseEntered(MouseEvent e) { lblAdminShield.setForeground(COLOR_DORADO); }
            @Override
            public void mouseExited(MouseEvent e) { lblAdminShield.setForeground(new Color(200, 160, 51, 100)); }
        });
        panel.add(lblAdminShield);

        // Pie de página (Footer)
        lblFooter = new JLabel(LanguageManager.get("dashboard.footer")); // Uso inicial del Gestor de Idiomas
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(Color.GRAY);
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        lblFooter.setBounds(centerX - 150, 610, 300, 20);
        panel.add(lblFooter);

        updateTexts();
    }

    /**
     * Metodo encargado de gestionar el acceso privilegiado al módulo de Usuarios.
     * FUNCIONALIDAD 2: Gestión de Usuarios y Roles (Acceso Administrador).
     */
    private void abrirGestionSeguridad() {
        SoundManager.getInstance().playClick();

        JPasswordField pf = new JPasswordField();

        // 1. Estilo Visual (Borde Dorado)
        pf.setBorder(new CompoundBorder(
                new LineBorder(COLOR_DORADO, 1),
                new EmptyBorder(5, 10, 5, 0)
        ));

        // 2. CORRECCIÓN VISUAL: Se fuerza la dimensión del componente.
        // Esto evita deformaciones en el JOptionPane.
        pf.setPreferredSize(new Dimension(250, 40));
        pf.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Se despliega el cuadro de diálogo modal
        int accion = JOptionPane.showConfirmDialog(this, pf,
                "Ingrese Clave Maestra de Seguridad:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (accion == JOptionPane.OK_OPTION) {
            String pass = new String(pf.getPassword());
            // Validación de credencial maestra
            if ("1234".equals(pass)) {
                new UserManagementDialog(this).setVisible(true);
            } else if (!pass.isEmpty()) {
                SoundManager.getInstance().playError();
                JOptionPane.showMessageDialog(this, "Clave Incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTexts() {
        setTitle(LanguageManager.get("login.title"));
        lblUser.setText(LanguageManager.get("login.userLabel"));
        lblPass.setText(LanguageManager.get("login.passLabel"));
        btnLogin.setText(LanguageManager.get("login.button"));

        // [CORRECCIÓN] Actualización dinámica del texto del footer
        if (lblFooter != null) {
            lblFooter.setText(LanguageManager.get("dashboard.footer"));
        }

        repaint();
    }

    /**
     * Método principal de validación de credenciales.
     * [PATRÓN DAO]: Se interactúa con la capa de datos para verificar la existencia del usuario.
     * FUNCIONALIDAD 3: Autenticación segura y validación contra Base de Datos.
     */
    private void validarYAnimar() {
        String u = txtUser.getText();
        String p = new String(txtPass.getPassword());

        // Instancia del Modelo de Datos (DAO)
        UserDAO dao = new UserDAO();

        // Verificación de credenciales en Base de Datos (Persistencia)
        boolean accesoBD = (dao.login(u, p) != null);
        boolean accesoEmergencia = (u.equals("admin") && p.equals("1234"));

        if (accesoBD || accesoEmergencia) {
            // Garantía de integridad: Se crea usuario admin si no existe físicamente
            if (!accesoBD && accesoEmergencia) {
                if (dao.findByUsername("admin") == null) {
                    dao.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
                }
            }
            txtUser.setEnabled(false);
            txtPass.setEnabled(false);
            btnLogin.setText(LanguageManager.get("login.success"));
            btnLogin.setBackground(new Color(46, 204, 113));
            SoundManager.getInstance().playClick();

            // Animación de transición (Feedback visual)
            try {
                URL gifUrl = getClass().getResource("/images/lock_animation.gif");
                if (gifUrl != null) {
                    ImageIcon gifIcon = new ImageIcon(gifUrl);
                    Image img = gifIcon.getImage().getScaledInstance(lblSecurity.getWidth(), lblSecurity.getHeight(), Image.SCALE_DEFAULT);
                    lblSecurity.setIcon(new ImageIcon(img));
                }
            } catch (Exception ex) { }

            Timer t = new Timer(5000, e -> {
                dispose(); // Se liberan recursos de la ventana actual
                TimeZone.setDefault(TimeZone.getTimeZone("America/Caracas"));
                new WelcomeView().setVisible(true);
                // Carga dinámica del Dashboard mediante Reflexión (Optimización)
                new Thread(() -> { try { Class.forName("com.swimcore.view.DashboardView"); } catch(Exception ex){} }).start();
            });
            t.setRepeats(false);
            t.start();
        } else {
            SoundManager.getInstance().playError();
            JOptionPane.showMessageDialog(this, LanguageManager.get("login.error.message"), LanguageManager.get("login.error.title"), JOptionPane.ERROR_MESSAGE);
            txtPass.setText("");
            txtPass.requestFocus();
        }
    }

    private void ajustarImagen(JLabel label, String path, int w) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(w, -1, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) { }
    }

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

    private void agregarEfectoClick(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                SoundManager.getInstance().playClick();
                btn.setLocation(btn.getX()+2, btn.getY()+2);
            }
            public void mouseReleased(MouseEvent e) {
                btn.setLocation(btn.getX()-2, btn.getY()-2);
            }
            public void mouseEntered(MouseEvent e) {
                SoundManager.getInstance().playHover();
            }
        });
    }
}