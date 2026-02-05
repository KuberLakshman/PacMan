// Import necessary Java libraries for graphics, events, and data structures
import java.awt.*;              // Graphics, Color, Font, Image
import java.awt.event.*;        // ActionListener, KeyListener
import java.util.HashSet;       // Collection framework (stores unique objects)
import java.util.Random;        // Random number generation
import javax.swing.*;           // Swing GUI components


// Main class that extends JPanel (for drawing) and implements ActionListener (for timer) and KeyListener (for keyboard input)
public class PacMan extends JPanel implements ActionListener, KeyListener {
    // ===================== INNER CLASS =====================
    // OOPS: ENCAPSULATION
    // Inner class to represent all game objects (walls, ghosts, pacman, food)
	// OOPS: ENCAPSULATION
	class Block {
        int x;              // X position on screen
        int y;              // Y position on screen
        int width;          // Width of the block
        int height;         // Height of the block
        Image image;        // Image to display for this block
        int startX;         // Starting X position (for reset)
        int startY;         // Starting Y position (for reset)
        char direction = 'U'; // Current direction: U=Up, D=Down, L=Left, R=Right
        int velocityX = 0;  // Horizontal speed (pixels per frame)
        int velocityY = 0;  // Vertical speed (pixels per frame)

        // Constructor to create a new block with image and position
        Block(Image image, int x, int y, int width, int height) {
            this.image = image;      // Store the image
            this.x = x;              // Set X position
            this.y = y;              // Set Y position
            this.width = width;      // Set width
            this.height = height;    // Set height
            this.startX = x;         // Remember starting X for reset
            this.startY = y;         // Remember starting Y for reset
        }

        // Method to change the direction of a block (used for pacman and ghosts)
        void updateDirection(char direction) {
            char prevDirection = this.direction;  // Save current direction in case we need to revert
            this.direction = direction;            // Update to new direction
            updateVelocity();                      // Calculate new velocity based on direction
            this.x += this.velocityX;              // Try moving in new direction
            this.y += this.velocityY;              // Try moving in new direction
            
            // Check if the new position collides with any wall
            for (Block wall : walls) {
                if (collision(this, wall)) {       // If collision detected
                    this.x -= this.velocityX;      // Undo the X movement
                    this.y -= this.velocityY;      // Undo the Y movement
                    this.direction = prevDirection; // Revert to previous direction
                    updateVelocity();               // Recalculate velocity for old direction
                }
            }
        }

        // Calculate velocity based on current direction
        void updateVelocity() {
            if (this.direction == 'U') {          // Moving Up
                this.velocityX = 0;                // No horizontal movement
                this.velocityY = -tileSize/4;      // Move up (negative Y)
            }
            else if (this.direction == 'D') {     // Moving Down
                this.velocityX = 0;                // No horizontal movement
                this.velocityY = tileSize/4;       // Move down (positive Y)
            }
            else if (this.direction == 'L') {     // Moving Left
                this.velocityX = -tileSize/4;      // Move left (negative X)
                this.velocityY = 0;                // No vertical movement
            }
            else if (this.direction == 'R') {     // Moving Right
                this.velocityX = tileSize/4;       // Move right (positive X)
                this.velocityY = 0;                // No vertical movement
            }
        }

        // Reset block to its starting position
        void reset() {
            this.x = this.startX;  // Return to starting X
            this.y = this.startY;  // Return to starting Y
        }
    }

    // ===================== GAME SETTINGS =====================
    // OOPS: ENCAPSULATION (private variables)
    // Game board dimensions
    private int rowCount = 21;           // Number of rows in the game board
    private int columnCount = 19;        // Number of columns in the game board
    private int tileSize = 32;           // Size of each tile in pixels
    private int boardWidth = columnCount * tileSize;   // Total width: 19 * 32 = 608 pixels
    private int boardHeight = rowCount * tileSize;     // Total height: 21 * 32 = 672 pixels

    // ===================== IMAGES =====================
    // Abstraction: ImageIcon hides file loading complexity
    // Image variables for all game sprites
    private Image wallImage;           // Wall block image
    private Image blueGhostImage;      // Blue ghost image
    private Image orangeGhostImage;    // Orange ghost image
    private Image pinkGhostImage;      // Pink ghost image
    private Image redGhostImage;       // Red ghost image

    // Pacman images for each direction
    private Image pacmanUpImage;       // Pacman facing up
    private Image pacmanDownImage;     // Pacman facing down
    private Image pacmanLeftImage;     // Pacman facing left
    private Image pacmanRightImage;    // Pacman facing right

    // ===================== MAP =====================
    // Abstraction: Game board defined using characters
    // X = wall, O = empty space (outside play area), P = pac man starting position, ' ' = food
    // Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",  // Row 0: Top border
        "X        X        X",  // Row 1: Open space with center wall
        "X XX XXX X XXX XX X",  // Row 2: Obstacle pattern
        "X                 X",  // Row 3: Open corridor
        "X XX X XXXXX X XX X",  // Row 4: More obstacles
        "X    X       X    X",  // Row 5: Open space
        "XXXX XXXX XXXX XXXX",  // Row 6: Wall before ghost house
        "OOOX X       X XOOO",  // Row 7: Ghost house entrance
        "XXXX X XXrXX X XXXX",  // Row 8: Ghost house with red ghost
        "X      bpo        X",  // Row 9: Center row with 3 ghosts (blue, pink, orange)
        "XXXX X XXXXX X XXXX",  // Row 10: Ghost house exit
        "OOOX X       X XOOO",  // Row 11: Below ghost house
        "XXXX X XXXXX X XXXX",  // Row 12: More walls
        "X        X        X",  // Row 13: Open space
        "X XX XXX X XXX XX X",  // Row 14: Obstacle pattern
        "X  X     P     X  X",  // Row 15: Pacman starting position (P)
        "XX X X XXXXX X X XX",  // Row 16: Complex obstacles
        "X    X   X   X    X",  // Row 17: Open corridors
        "X XXXXXX X XXXXXX X",  // Row 18: Large wall sections
        "X                 X",  // Row 19: Bottom corridor
        "XXXXXXXXXXXXXXXXXXX"   // Row 20: Bottom border
    };

    // ===================== OBJECT COLLECTIONS =====================
    // OOPS: COMPOSITION (PacMan HAS-A Block)
    // Collections to store game objects
    HashSet<Block> walls;   // Set of all wall blocks
    HashSet<Block> foods;   // Set of all food pellets
    HashSet<Block> ghosts;  // Set of all ghost blocks
    Block pacman;           // The pacman object

    // ===================== GAME STATE =====================
    // Game control variables
    Timer gameLoop;                           // Timer to control game updates
    char[] directions = {'U', 'D', 'L', 'R'}; // Array of possible directions
    Random random = new Random();             // Random number generator for ghost movement
    int score = 0;                            // Current game score
    int lives = 3;                            // Number of lives remaining
    boolean gameOver = false;                 // Game over flag
    boolean gameStarted = false;              // Game started flag

    // ===================== CONSTRUCTOR =====================
    // Constructor - initializes the game
    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));  // Set panel size
        setBackground(Color.BLACK);                                 // Black background
        addKeyListener(this);                                       // Listen for keyboard input
        setFocusable(true);                                         // Allow panel to receive focus

        // Load all image files from resources
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();  // Load the game map and create all objects
        
        // Give each ghost a random starting direction
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];  // Pick random direction (0-3)
            ghost.updateDirection(newDirection);                 // Set the direction
        }

        // OOPS: INTERFACE POLYMORPHISM
        // Create game timer: 50ms delay = 20 frames per second (1000ms / 50ms = 20fps)
        gameLoop = new Timer(50, this);
        // Don't start the timer yet - wait for player to press a key
    }

    // ===================== MAP LOADING =====================
    // OOPS: ABSTRACTION
    // Load the map from the tileMap string array and create game objects
    public void loadMap() {
        walls = new HashSet<Block>();   // Initialize empty set for walls
        foods = new HashSet<Block>();   // Initialize empty set for food
        ghosts = new HashSet<Block>();  // Initialize empty set for ghosts

        // Loop through each row of the map
        for (int r = 0; r < rowCount; r++) {
            // Loop through each column of the map
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];          // Get the current row string
                char tileMapChar = row.charAt(c); // Get the character at this position

                int x = c * tileSize;  // Calculate X pixel position (column * 32)
                int y = r * tileSize;  // Calculate Y pixel position (row * 32)

                // Create appropriate object based on the character
                if (tileMapChar == 'X') {  // 'X' = wall block
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);  // Add to walls set
                }
                else if (tileMapChar == 'b') {  // 'b' = blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);  // Add to ghosts set
                }
                else if (tileMapChar == 'o') {  // 'o' = orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);  // Add to ghosts set
                }
                else if (tileMapChar == 'p') {  // 'p' = pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);  // Add to ghosts set
                }
                else if (tileMapChar == 'r') {  // 'r' = red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);  // Add to ghosts set
                }
                else if (tileMapChar == 'P') {  // 'P' = pacman starting position
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') {  // ' ' (space) = food pellet
                    // Food is smaller (4x4) and centered in the tile (+14 offset to center it)
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);  // Add to foods set
                }
            }
        }
    }

    // Override paintComponent to draw the game
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // Call parent method to clear panel
        draw(g);                   // Call our custom draw method
    }

    // Draw all game elements
    public void draw(Graphics g) {
        // Draw pacman
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // Draw all ghosts
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Draw all walls
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Draw all food pellets as white rectangles
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Draw score and status messages
        g.setFont(new Font("Arial", Font.PLAIN, 18));  // Set default font

        if (gameOver) {  // If game is over
            // Display large "GAME OVER" text in center
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.RED);
            String gameOverText = "GAME OVER";
            FontMetrics fm = g.getFontMetrics();                      // Get font measurements
            int textWidth = fm.stringWidth(gameOverText);            // Calculate text width
            g.drawString(gameOverText, (boardWidth - textWidth) / 2, boardHeight / 2 - 30);  // Center it

            // Display final score
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.setColor(Color.WHITE);
            String scoreText = "Final Score: " + String.valueOf(score);
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(scoreText);
            g.drawString(scoreText, (boardWidth - textWidth) / 2, boardHeight / 2 + 20);

            // Display restart instruction
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String restartText = "Press any key to restart";
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(restartText);
            g.drawString(restartText, (boardWidth - textWidth) / 2, boardHeight / 2 + 60);
        }
        else if (!gameStarted) {  // If game hasn't started yet
            // Display large "GAME START" text
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.YELLOW);
            String startText = "GAME START";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(startText);
            g.drawString(startText, (boardWidth - textWidth) / 2, boardHeight / 2 - 20);

            // Display instructions
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.setColor(Color.WHITE);
            String instructionText = "Press WASD or Arrow keys to begin";
            fm = g.getFontMetrics();
            textWidth = fm.stringWidth(instructionText);
            g.drawString(instructionText, (boardWidth - textWidth) / 2, boardHeight / 2 + 30);
        }
        else {  // Normal gameplay
            // Display lives and score at top of screen
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    // Move all game objects and check collisions
    public void move() {
        // Move pacman based on current velocity
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Check if pacman collided with any wall
        for (Block wall : walls) {
            if (collision(pacman, wall)) {  // If collision detected
                pacman.x -= pacman.velocityX;  // Undo X movement
                pacman.y -= pacman.velocityY;  // Undo Y movement
                break;  // Stop checking walls
            }
        }

        // Check ghost collisions and move ghosts
        for (Block ghost : ghosts) {
            // Check if ghost touched pacman
            if (collision(ghost, pacman)) {
                lives -= 1;  // Lose a life
                if (lives == 0) {  // If no lives left
                    gameOver = true;  // Game over
                    return;  // Exit method
                }
                resetPositions();  // Reset all positions after death
            }

            // Special behavior: if ghost is at row 9 and not moving vertically, make it go up
            // This forces ghosts to leave the ghost house
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            
            // Move ghost
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // Check if ghost hit a wall or board edge
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;  // Undo movement
                    ghost.y -= ghost.velocityY;  // Undo movement
                    char newDirection = directions[random.nextInt(4)];  // Pick random new direction
                    ghost.updateDirection(newDirection);  // Change direction
                }
            }
        }

        // Check if pacman ate any food
        Block foodEaten = null;  // Track which food was eaten
        for (Block food : foods) {
            if (collision(pacman, food)) {  // If pacman touched food
                foodEaten = food;  // Mark this food for removal
                score += 10;       // Increase score by 10
            }
        }
        foods.remove(foodEaten);  // Remove the eaten food from the set

        // If all food is gone, reload the map (new level)
        if (foods.isEmpty()) {
            loadMap();          // Reload all food and positions
            resetPositions();   // Reset character positions
        }
    }

    // Check if two blocks are colliding using rectangle collision detection
    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&      // A's left edge is left of B's right edge
               a.x + a.width > b.x &&      // A's right edge is right of B's left edge
               a.y < b.y + b.height &&     // A's top edge is above B's bottom edge
               a.y + a.height > b.y;       // A's bottom edge is below B's top edge
    }

    // Reset all character positions to their starting locations
    public void resetPositions() {
        pacman.reset();         // Reset pacman position
        pacman.velocityX = 0;   // Stop pacman movement
        pacman.velocityY = 0;   // Stop pacman movement
        
        // Reset all ghosts
        for (Block ghost : ghosts) {
            ghost.reset();  // Reset ghost position
            char newDirection = directions[random.nextInt(4)];  // Give random direction
            ghost.updateDirection(newDirection);  // Apply the direction
        }
    }

    // Called by the timer every 50ms (game loop)
    @Override
    public void actionPerformed(ActionEvent e) {
        move();    // Update all positions and check collisions
        repaint(); // Redraw the screen
        if (gameOver) {  // If game ended
            gameLoop.stop();  // Stop the timer
        }
    }

    // ===================== INPUT =====================
    // KeyListener methods - required but not used
    @Override
    public void keyTyped(KeyEvent e) {}  // Not used in this game

    @Override
    public void keyPressed(KeyEvent e) {}  // Not used - we use keyReleased instead

    // Handle keyboard input when keys are released
    @Override
    public void keyReleased(KeyEvent e) {
        // If game is over, any key restarts the game
        if (gameOver) {
            loadMap();            // Reload the map
            resetPositions();     // Reset positions
            lives = 3;            // Reset lives to 3
            score = 0;            // Reset score to 0
            gameOver = false;     // Clear game over flag
            gameStarted = false;  // Show start message again
            return;               // Don't process as movement key
        }

        // Get the key code that was pressed
        int keyCode = e.getKeyCode();
        
        // Ignore all keys except WASD and arrow keys
        if (keyCode != KeyEvent.VK_W && keyCode != KeyEvent.VK_A && 
            keyCode != KeyEvent.VK_S && keyCode != KeyEvent.VK_D &&
            keyCode != KeyEvent.VK_UP && keyCode != KeyEvent.VK_DOWN && 
            keyCode != KeyEvent.VK_LEFT && keyCode != KeyEvent.VK_RIGHT) {
            return;  // Exit if not a movement key
        }

        // Start the game when first movement key is pressed
        if (!gameStarted) {
            gameStarted = true;  // Mark game as started
            gameLoop.start();    // Start the timer
        }

        // Update pacman direction based on key pressed
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {  // W or Up arrow
        	pacman.updateDirection('U');  // Move up
        }
        else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {  // S or Down arrow
            pacman.updateDirection('D');  // Move down
        }
        else if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {  // A or Left arrow
            pacman.updateDirection('L');  // Move left
        }
        else if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {  // D or Right arrow
            pacman.updateDirection('R');  // Move right
        }

        // Update pacman's image to match the direction
        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;     // Use up-facing image
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;   // Use down-facing image
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;   // Use left-facing image
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;  // Use right-facing image
        }
    }
}