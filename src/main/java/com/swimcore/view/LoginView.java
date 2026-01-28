/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - DAYANA GUEDEZ SWIMWEAR
 * ARCHIVO: LoginView.java
 * VERSIÓN: 2.0.0 (i18n Integration)
 * DESCRIPCIÓN: Login V2.0 (Internacionalización y Refresco Dinámico)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Locale;

public class LoginView extends JFrame {

    // --- CONSTANTES VISUALES ---
    private final Color COLOR_FONDO = new Color(18, 18, 18);
    private final Color COLOR_CAMPOS = new Color(35, 35, 35);
    private final Color COLOR_TEXTO = new Color(200, 200, 200);
    private final Color COLOR_DORADO = new Color(200, 160, 51);

    // --- COMPONENTES UI ---
    private JLabel lblUser, lblPass; // Ahora son variables de instancia para poder actualizarlas
    private JLabel lblSecurity;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    public LoginView() {
        // El título se carga en updateTexts()
        setSize(480, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(COLOR_FONDO);
        add(panel);

        int centerX = 240;

        // 1. BRANDING
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

        // 2. IDIOMA
        int flagW = 85, flagH = 55, gap = 20;
        JButton btnVe = new JButton();
        btnVe.setBounds(centerX - flagW - (gap/2), 160, flagW, flagH);
        estilizarBotonImagen(btnVe, "/images/ve.png");
        btnVe.addActionListener(e -> {
            LanguageManager.setLocale(new Locale("es"));
            updateTexts(); // Actualiza los textos al cambiar idioma
        });
        panel.add(btnVe);

        JButton btnUs = new JButton();
        btnUs.setBounds(centerX + (gap/2), 160, flagW, flagH);
        estilizarBotonImagen(btnUs, "/images/us.png");
        btnUs.addActionListener(e -> {
            LanguageManager.setLocale(new Locale("en"));
            updateTexts(); // Actualiza los textos al cambiar idioma
        });
        panel.add(btnUs);

        // 3. INPUTS
        int startY = 240, inputWidth = 320, inputX = centerX - (inputWidth / 2);

        lblUser = new JLabel(); // Se inicializa vacío
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

        lblPass = new JLabel(); // Se inicializa vacío
        lblPass.setForeground(COLOR_TEXTO);
        lblPass.setBounds(inputX, startY + 90, 200, 20);
        panel.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setBounds(inputX, startY + 115, inputWidth, 45);
        txtPass.setBackground(COLOR_CAMPOS);
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(COLOR_DORADO);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.addActionListener(e -> validarYAnimar());
        panel.add(txtPass);

        // 4. CANDADO
        lblSecurity = new JLabel();
        int lockSize = 80;
        lblSecurity.setBounds(centerX - (lockSize/2), startY + 180, lockSize, lockSize);
        lblSecurity.setHorizontalAlignment(SwingConstants.CENTER);
        ajustarImagen(lblSecurity, "/images/lock_closed.jpg", lockSize);
        panel.add(lblSecurity);

        // 5. BOTÓN ENTRAR
        btnLogin = new JButton(); // Se inicializa vacío
        int btnWidth = 220;
        btnLogin.setBounds(centerX - (btnWidth/2), startY + 280, btnWidth, 50);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBackground(new Color(220, 0, 115));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1));
        agregarEfectoClick(btnLogin);
        btnLogin.addActionListener(e -> validarYAnimar());
        panel.add(btnLogin);

        // Footer
        JLabel firma = new JLabel("Desarrollado por Johanna Guédez © 2026");
        firma.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        firma.setForeground(Color.GRAY);
        firma.setHorizontalAlignment(SwingConstants.CENTER);
        firma.setBounds(centerX - 150, 610, 300, 20);
        panel.add(firma);

        // Carga inicial de textos en el idioma por defecto
        updateTexts();
    }

    /**
     * NUEVO MÉTODO: Centraliza la actualización de todos los textos de la UI.
     */
    private void updateTexts() {
        setTitle(LanguageManager.get("login.title"));
        lblUser.setText(LanguageManager.get("login.userLabel"));
        lblPass.setText(LanguageManager.get("login.passLabel"));
        btnLogin.setText(LanguageManager.get("login.button"));
        // Repintar para asegurar que los cambios se muestren
        repaint();
    }

    private void validarYAnimar() {
        String u = txtUser.getText();
        String p = new String(txtPass.getPassword());
        UserDAO dao = new UserDAO();

        boolean accesoBD = (dao.login(u, p) != null);
        boolean accesoEmergencia = (u.equals("admin") && p.equals("1234"));

        if (accesoBD || accesoEmergencia) {
            if (!accesoBD && accesoEmergencia) {
                if (dao.findByUsername("admin") == null) {
                    dao.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
                }
            }

            txtUser.setEnabled(false);
            txtPass.setEnabled(false);
            btnLogin.setText(LanguageManager.get("login.success")); // Texto de éxito
            btnLogin.setBackground(new Color(46, 204, 113));

            try {
                URL gifUrl = getClass().getResource("/images/lock_animation.gif");
                if (gifUrl != null) {
                    ImageIcon gifIcon = new ImageIcon(gifUrl);
                    Image img = gifIcon.getImage().getScaledInstance(lblSecurity.getWidth(), lblSecurity.getHeight(), Image.SCALE_DEFAULT);
                    lblSecurity.setIcon(new ImageIcon(img));
                }
            } catch (Exception ex) { }

            Timer t = new Timer(1500, e -> {
                dispose();
                new WelcomeView().setVisible(true);
            });
            t.setRepeats(false);
            t.start();

        } else {
            SoundManager.getInstance().playError();
            // Mensaje de error también multilenguaje
            JOptionPane.showMessageDialog(this,
                    LanguageManager.get("login.error.message"),
                    LanguageManager.get("login.error.title"),
                    JOptionPane.ERROR_MESSAGE);
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