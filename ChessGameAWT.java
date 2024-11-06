import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChessGameAWT extends Frame implements MouseListener {

    // Board settings
    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 80; // Size of each tile on the board
    private Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE]; // 2D array for pieces

    // Variables to track selected piece
    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessGameAWT() {
        setTitle("Chess Game: User vs Computer");

        // Get screen size and adjust window size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowSize = Math.min(screenSize.width, screenSize.height); // Make it square
        setSize(windowSize, windowSize);

        // Make sure you can see the window
        setResizable(false); // Prevent resizing to maintain aspect ratio
        setVisible(true); // Make the window visible
        addMouseListener(this); // Add the mouse listener for interaction

        initializeBoard(); // Initialize the board with pieces

        // Close window action
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
                System.exit(0);
            }
        });
    }

    // Initialize the chessboard with pieces
    private void initializeBoard() {
        // Initialize pieces for the user (red)
        board[0][0] = new Rook(true);
        board[0][7] = new Rook(true);
        board[0][1] = new Knight(true);
        board[0][6] = new Knight(true);
        board[0][2] = new Bishop(true);
        board[0][5] = new Bishop(true);
        board[0][3] = new Queen(true);
        board[0][4] = new King(true);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(true); // Red Pawns
        }

        // Initialize pieces for the computer (black)
        board[7][0] = new Rook(false);
        board[7][7] = new Rook(false);
        board[7][1] = new Knight(false);
        board[7][6] = new Knight(false);
        board[7][2] = new Bishop(false);
        board[7][5] = new Bishop(false);
        board[7][3] = new Queen(false);
        board[7][4] = new King(false);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[6][i] = new Pawn(false); // Black Pawns
        }
    }

    public void paint(Graphics g) {
        int offsetX = (getWidth() - BOARD_SIZE * TILE_SIZE) / 2;
        int offsetY = (getHeight() - BOARD_SIZE * TILE_SIZE) / 2;

        // Draw the board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Alternate between white and gray tiles
                g.setColor((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                g.fillRect(offsetX + col * TILE_SIZE, offsetY + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Draw pieces if any
                if (board[row][col] != null) {
                    board[row][col].draw(g, offsetX + col * TILE_SIZE, offsetY + row * TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        int offsetX = (getWidth() - BOARD_SIZE * TILE_SIZE) / 2;
        int offsetY = (getHeight() - BOARD_SIZE * TILE_SIZE) / 2;

        // Calculate the row and column of the click
        int clickedRow = (e.getY() - offsetY) / TILE_SIZE;
        int clickedCol = (e.getX() - offsetX) / TILE_SIZE;

        // Check if click is within board boundaries
        if (clickedRow < 0 || clickedRow >= BOARD_SIZE || clickedCol < 0 || clickedCol >= BOARD_SIZE) {
            return; // Ignore clicks outside the board
        }

        // If no piece is selected, select the clicked piece (if it belongs to the player)
        if (selectedRow == -1 && selectedCol == -1) {
            Piece clickedPiece = board[clickedRow][clickedCol];
            if (clickedPiece != null && clickedPiece.isWhite) { // Player is red
                selectedRow = clickedRow;
                selectedCol = clickedCol;
            }
        } else {
            // Try to move the selected piece
            Piece selectedPiece = board[selectedRow][selectedCol];
            if (selectedPiece != null && selectedPiece.isValidMove(selectedRow, selectedCol, clickedRow, clickedCol, board)) {
                // Move the piece
                board[clickedRow][clickedCol] = selectedPiece;
                board[selectedRow][selectedCol] = null; // Clear the old position

                // Reset the selection
                selectedRow = -1;
                selectedCol = -1;

                // Repaint the board after moving
                repaint();

                // Check for win condition
                if (isCheckMate(false)) {
                    showWinSymbol();
                } else {
                    // Make the computer move after the player
                    computerMove();
                }
            } else {
                // Invalid move, deselect the piece
                selectedRow = -1;
                selectedCol = -1;
            }
        }
    }

    private boolean isCheckMate(boolean isWhite) {
        // Check if the opponent has no valid moves left
        List<Move> validMoves = getAllValidMoves(!isWhite); // Opponent's turn
        return validMoves.isEmpty(); // Checkmate condition
    }

    private void showWinSymbol() {
        Graphics g = getGraphics();
        g.setColor(Color.GREEN);
        g.drawString("You Win!", 20, 20); // Display a win message
        g.dispose();
    }

    private void computerMove() {
        List<Move> validMoves = getAllValidMoves(false); // Get all valid moves for the computer (black pieces)

        if (!validMoves.isEmpty()) {
            // Pick a random move
            Move move = validMoves.get((int) (Math.random() * validMoves.size()));

            // Move the piece
            board[move.endRow][move.endCol] = board[move.startRow][move.startCol];
            board[move.startRow][move.startCol] = null; // Clear the old position

            // Repaint the board after the move
            repaint();

            // Check for win condition
            if (isCheckMate(true)) {
                showWinSymbol();
            }
        }
    }

    // Method to get all valid moves for a player (true for red, false for black)
    private List<Move> getAllValidMoves(boolean isWhite) {
        List<Move> validMoves = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.isWhite == isWhite) {
                    // Check all possible moves for this piece
                    for (int newRow = 0; newRow < BOARD_SIZE; newRow++) {
                        for (int newCol = 0; newCol < BOARD_SIZE; newCol++) {
                            if (piece.isValidMove(row, col, newRow, newCol, board)) {
                                validMoves.add(new Move(row, col, newRow, newCol));
                            }
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    // Inner class to represent a move
    private class Move {
        int startRow, startCol, endRow, endCol;

        public Move(int startRow, int startCol, int endRow, int endCol) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
        }
    }

    // Abstract class for pieces
    abstract class Piece {
        boolean isWhite; // true for red, false for black

        public Piece(boolean isWhite) {
            this.isWhite = isWhite;
        }

        abstract void draw(Graphics g, int x, int y, int tileSize);

        abstract boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board);

        boolean isOwnPiece(int row, int col, Piece[][] board) {
            Piece piece = board[row][col];
            return piece != null && piece.isWhite == board[selectedRow][selectedCol].isWhite; // Compare with the selected piece's color
        }
    }

    // Rook class extending Piece
    class Rook extends Piece {
        public Rook(boolean isWhite) {
            super(isWhite);
        }

        void draw(Graphics g, int x, int y, int tileSize) {
            g.setColor(isWhite ? Color.RED : Color.BLACK);
            g.fillRect(x + 10, y + 10, tileSize - 20, tileSize - 20); // Rook representation
        }

        boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
            if (startX == endX || startY == endY) { // Horizontal or vertical move
                int stepX = (endX == startX) ? 0 : (endX > startX ? 1 : -1);
                int stepY = (endY == startY) ? 0 : (endY > startY ? 1 : -1);

                for (int x = startX + stepX, y = startY + stepY; x != endX || y != endY; x += stepX, y += stepY) {
                    if (board[x][y] != null) {
                        return false; // Path is blocked
                    }
                }
                return !isOwnPiece(endX, endY, board); // No own piece at the destination
            }
            return false;
        }
    }

    // Knight class extending Piece
    class Knight extends Piece {
        public Knight(boolean isWhite) {
            super(isWhite);
        }

        void draw(Graphics g, int x, int y, int tileSize) {
            g.setColor(isWhite ? Color.RED : Color.BLACK);
            g.fillOval(x + 10, y + 10, tileSize - 20, tileSize - 20); // Knight representation
        }

        boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
            // Knight moves in L-shape
            return (Math.abs(startX - endX) == 2 && Math.abs(startY - endY) == 1) ||
                   (Math.abs(startX - endX) == 1 && Math.abs(startY - endY) == 2);
        }
    }

    // Bishop class extending Piece
    class Bishop extends Piece {
        public Bishop(boolean isWhite) {
            super(isWhite);
        }

        void draw(Graphics g, int x, int y, int tileSize) {
            g.setColor(isWhite ? Color.RED : Color.BLACK);
            g.drawLine(x + tileSize / 2, y + 10, x + tileSize / 2, y + tileSize - 10); // Bishop representation
        }

        boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
            if (Math.abs(startX - endX) == Math.abs(startY - endY)) { // Diagonal move
                int stepX = (endX - startX) / Math.abs(endX - startX); // Direction of movement
                int stepY = (endY - startY) / Math.abs(endY - startY);

                for (int x = startX + stepX, y = startY + stepY; x != endX || y != endY; x += stepX, y += stepY) {
                    if (board[x][y] != null) {
                        return false; // Path is blocked
                    }
                }
                return !isOwnPiece(endX, endY, board); // No own piece at the destination
            }
            return false;
        }
    }

    // Queen class extending Piece
    class Queen extends Piece {
        public Queen(boolean isWhite) {
            super(isWhite);
        }

        void draw(Graphics g, int x, int y, int tileSize) {
            g.setColor(isWhite ? Color.RED : Color.BLACK);
            g.fillRect(x + 10, y + 10, tileSize - 20, tileSize - 20); // Queen representation
        }

        boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
            return (new Rook(isWhite).isValidMove(startX, startY, endX, endY, board) ||
                    new Bishop(isWhite).isValidMove(startX, startY, endX, endY, board)); // Queen can move like both rook and bishop
        }
    }

    // King class extending Piece
    class King extends Piece {
        public King(boolean isWhite) {
            super(isWhite);
        }

        void draw(Graphics g, int x, int y, int tileSize) {
            g.setColor(isWhite ? Color.RED : Color.BLACK);
            g.fillOval(x + 10, y + 10, tileSize - 20, tileSize - 20); // King representation
        }

        boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
            return (Math.abs(startX - endX) <= 1 && Math.abs(startY - endY) <= 1); // King moves one square in any direction
        }
    }

    // Pawn class extending Piece
    class Pawn extends Piece {
        public Pawn(boolean isWhite) {
            super(isWhite);
        }

        void draw(Graphics g, int x, int y, int tileSize) {
            g.setColor(isWhite ? Color.RED : Color.BLACK);
            g.fillRect(x + tileSize / 4, y + 10, tileSize / 2, tileSize - 20); // Pawn representation
        }

        boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
            if (isWhite) { // Red Pawns
                return (startY == endY && endX == startX + 1 && board[endX][endY] == null) || // Move forward
                       (startY != endY && endX == startX + 1 && Math.abs(endY - startY) == 1 && board[endX][endY] != null); // Capture
            } else { // Black Pawns
                return (startY == endY && endX == startX - 1 && board[endX][endY] == null) || // Move forward
                       (startY != endY && endX == startX - 1 && Math.abs(endY - startY) == 1 && board[endX][endY] != null); // Capture
            }
        }
    }

    public static void main(String[] args) {
        new ChessGameAWT(); // Start the chess game
    }

   
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {}
  
    public void mouseEntered(MouseEvent e) {}
 
    public void mouseExited(MouseEvent e) {}
}
