import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ChessGame extends Frame implements MouseListener, KeyListener {
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = TILE_SIZE * 8;
    private static final String[] INITIAL_PIECES = {
        "r", "n", "b", "q", "k", "b", "n", "r",
        "p", "p", "p", "p", "p", "p", "p", "p",
        ".", ".", ".", ".", ".", ".", ".", ".",
        ".", ".", ".", ".", ".", ".", ".", ".",
        ".", ".", ".", ".", ".", ".", ".", ".",
        ".", ".", ".", ".", ".", ".", ".", ".",
        "P", "P", "P", "P", "P", "P", "P", "P",
        "R", "N", "B", "Q", "K", "B", "N", "R"
    };

    private String[][] board;
    private String selectedPiece;
    private int selectedX, selectedY;
    private boolean isWhiteTurn = true;
    private Set<String> whitePieces;
    private Set<String> blackPieces;
    private Set<String> validMoves;
    private Map<String, Image> pieceImages;
    private boolean playWithComputer;

    // Undo and redo stacks
    private Stack<String[][]> undoStack = new Stack<>();
    private Stack<String[][]> redoStack = new Stack<>();

    // Constructor to initialize the game
    public ChessGame(boolean playWithComputer) {
        this.playWithComputer = playWithComputer;
        board = new String[8][8];
        whitePieces = new HashSet<>(Arrays.asList("P", "R", "N", "B", "Q", "K"));
        blackPieces = new HashSet<>(Arrays.asList("p", "r", "n", "b", "q", "k"));
        initializeBoard();

        // Load piece images
        loadPieceImages();

        setSize(BOARD_SIZE, BOARD_SIZE);
        setTitle("Chess Game");
        setVisible(true);
        addMouseListener(this);
        addKeyListener(this); // For undo/redo key bindings
        setFocusable(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });
    }

    // Initialize the chessboard
    private void initializeBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = INITIAL_PIECES[i * 8 + j];
            }
        }
    }

    // Load piece images
    private void loadPieceImages() {
        pieceImages = new HashMap<>();
        // Images should be named wp.png, wr.png, etc., and located in the working directory
        pieceImages.put("wP", Toolkit.getDefaultToolkit().getImage("wp.png"));
        pieceImages.put("wR", Toolkit.getDefaultToolkit().getImage("wr.png"));
        pieceImages.put("wN", Toolkit.getDefaultToolkit().getImage("wn.png"));
        pieceImages.put("wB", Toolkit.getDefaultToolkit().getImage("wb.png"));
        pieceImages.put("wQ", Toolkit.getDefaultToolkit().getImage("wq.png"));
        pieceImages.put("wK", Toolkit.getDefaultToolkit().getImage("wk.png"));
        pieceImages.put("bP", Toolkit.getDefaultToolkit().getImage("bp.png"));
        pieceImages.put("bR", Toolkit.getDefaultToolkit().getImage("br.png"));
        pieceImages.put("bN", Toolkit.getDefaultToolkit().getImage("bn.png"));
        pieceImages.put("bB", Toolkit.getDefaultToolkit().getImage("bb.png"));
        pieceImages.put("bQ", Toolkit.getDefaultToolkit().getImage("bq.png"));
        pieceImages.put("bK", Toolkit.getDefaultToolkit().getImage("bk.png"));
    }

    // Paint the board and pieces
    public void paint(Graphics g) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // Draw the board tiles
                g.setColor((i + j) % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Draw the pieces
                String piece = board[i][j];
                if (!piece.equals(".")) {
                    Image pieceImage = pieceImages.get(getPieceKey(piece));
                    if (pieceImage != null) {
                        g.drawImage(pieceImage, j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                    }
                }
            }
        }
    }

    // Utility method to map the piece letter to a key for loading images
    private String getPieceKey(String piece) {
        return (Character.isUpperCase(piece.charAt(0)) ? "w" : "b") + piece.toUpperCase();
    }

    // Mouse click event handler to select and move pieces
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / TILE_SIZE;
        int y = e.getY() / TILE_SIZE;

        if (selectedPiece == null) {
            // Select a piece
            if (!board[y][x].equals(".")) {
                String piece = board[y][x];
                if (isWhiteTurn && whitePieces.contains(piece) || !isWhiteTurn && blackPieces.contains(piece)) {
                    selectedPiece = piece;
                    selectedX = x;
                    selectedY = y;
                }
            }
        } else {
            // Save the current state before the move
            undoStack.push(copyBoard(board));
            redoStack.clear(); // Clear redo stack after a new move

            // Move the piece
            board[y][x] = selectedPiece;
            board[selectedY][selectedX] = ".";
            selectedPiece = null;
            isWhiteTurn = !isWhiteTurn;
            repaint();
        }
    }

    // Copy the current board state
    private String[][] copyBoard(String[][] board) {
        String[][] newBoard = new String[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, 8);
        }
        return newBoard;
    }

    // Undo the last move
    private void undoMove() {
        if (!undoStack.isEmpty()) {
            redoStack.push(copyBoard(board)); // Save the current state to redo stack
            board = undoStack.pop(); // Restore the previous state
            isWhiteTurn = !isWhiteTurn;
            repaint();
        } else {
            System.out.println("No moves to undo!");
        }
    }

    // Redo the last undone move
    private void redoMove() {
        if (!redoStack.isEmpty()) {
            undoStack.push(copyBoard(board)); // Save the current state to undo stack
            board = redoStack.pop(); // Restore the next state
            isWhiteTurn = !isWhiteTurn;
            repaint();
        } else {
            System.out.println("No moves to redo!");
        }
    }

    // Key bindings for undo (Ctrl+Z) and redo (Ctrl+Y)
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            undoMove();
        } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
            redoMove();
        }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    // Unused mouse events
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // Main method to start the game
    public static void main(String[] args) {
        new ChessGame(false); // Change to new cg(true) for Human vs Computer
    }
}
