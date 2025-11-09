import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.sound.sampled.*;

public class SnakeGame extends JFrame {
    public SnakeGame() {
        setTitle("🐍 Snake Game - by Sanket Ghorai");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int DELAY = 75;

    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts = 6;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private int highScore = 0;

    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private Random random;

    // 🎨 Theme Variables
    private int themeIndex = 0;
    private final Color[][] themes = {
            {new Color(30, 30, 60), new Color(70, 0, 100)},  // Purple
            {new Color(0, 50, 100), new Color(0, 150, 255)}, // Ocean Blue
            {new Color(100, 30, 30), new Color(255, 100, 0)} // Sunset Orange
    };

    public GamePanel() {
        random = new Random();
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());
        loadHighScore();
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void drawBackground(Graphics2D g2d) {
        Color c1 = themes[themeIndex][0];
        Color c2 = themes[themeIndex][1];
        GradientPaint gradient = new GradientPaint(0, 0, c1, SCREEN_WIDTH, SCREEN_HEIGHT, c2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        drawBackground(g2d);

        if (running) {
            // Draw apple
            g.setColor(new Color(255, 80, 80));
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) g.setColor(new Color(0, 255, 120));
                else g.setColor(new Color(0, 200, 100));
                g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
            }

            // Score
            g.setColor(Color.WHITE);
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten + "  |  High Score: " + highScore,
                    (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten + "  |  High Score: " + highScore)) / 2,
                    g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            playSound("eat.mp3");
            if (applesEaten > highScore) {
                highScore = applesEaten;
                saveHighScore();
            }
            newApple();
        }
    }

    public void checkCollisions() {
        // Head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                playSound("gameover.mp3");
                break;
            }
        }

        // Borders
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
            playSound("gameover.mp3");
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics1.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 35));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2, SCREEN_HEIGHT / 2 + 60);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 25));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Press R to Restart | Press T to Change Theme",
                (SCREEN_WIDTH - metrics3.stringWidth("Press R to Restart | Press T to Change Theme")) / 2,
                SCREEN_HEIGHT / 2 + 120);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    // 🔊 Sound Effect Function
    private void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            if (file.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            System.out.println("Sound error: " + e.getMessage());
        }
    }

    // 🏆 High Score Save/Load
    private void saveHighScore() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"))) {
            bw.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Error saving high score.");
        }
    }

    private void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = br.readLine();
            if (line != null) highScore = Integer.parseInt(line.trim());
        } catch (IOException ignored) {
        }
    }

    // ⌨️ Key Controls
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') direction = 'L';
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') direction = 'R';
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') direction = 'U';
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') direction = 'D';
                }
                case KeyEvent.VK_R -> {
                    if (!running) {
                        bodyParts = 6;
                        applesEaten = 0;
                        direction = 'R';
                        for (int i = 0; i < bodyParts; i++) {
                            x[i] = 0;
                            y[i] = 0;
                        }
                        startGame();
                    }
                }
                case KeyEvent.VK_T -> {
                    themeIndex = (themeIndex + 1) % themes.length;
                    repaint();
                }
            }
        }
    }
}
