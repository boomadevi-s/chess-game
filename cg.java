import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class cg extends Frame implements MouseListener {
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
    private boolean isGameOver = false;
    private Player currentPlayer;
    private Player whitePlayer;
    private Player blackPlayer;

    // Constructor to initialize the game
    public cg(boolean playWithComputer) {
        this.playWithComputer = playWithComputer;
        board = new String[8][8];
        whitePieces = new HashSet<>(Arrays.asList("P", "R", "N", "B", "Q", "K"));
        blackPieces = new HashSet<>(Arrays.asList("p", "r", "n", "b", "q", "k"));
        initializeBoard();

        // Load piece images from the same directory as the source code
        loadPieceImages();

        // Initialize players (Human vs Human or Human vs Computer)
        whitePlayer = new HumanPlayer(true);
        blackPlayer = playWithComputer ? new ComputerPlayer(false) : new HumanPlayer(false);
        currentPlayer = whitePlayer;

        setSize(BOARD_SIZE, BOARD_SIZE);
        setTitle("Chess Game");
        setVisible(true);
        addMouseListener(this);
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

        // Highlight valid moves (if a piece is selected)
        if (selectedPiece != null) {
            g.setColor(Color.GREEN);
            for (String move : validMoves) {
                int x = Integer.parseInt(move.split(",")[0]);
                int y = Integer.parseInt(move.split(",")[1]);
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    // Utility method to map the piece letter to a key for loading images
    private String getPieceKey(String piece) {
        if (piece.equals("P") || piece.equals("p")) {
            return (piece.equals("P") ? "wP" : "bP");
        }
        if (piece.equals("R") || piece.equals("r")) {
            return (piece.equals("R") ? "wR" : "bR");
        }
        if (piece.equals("N") || piece.equals("n")) {
            return (piece.equals("N") ? "wN" : "bN");
        }
        if (piece.equals("B") || piece.equals("b")) {
            return (piece.equals("B") ? "wB" : "bB");
        }
        if (piece.equals("Q") || piece.equals("q")) {
            return (piece.equals("Q") ? "wQ" : "bQ");
        }
        if (piece.equals("K") || piece.equals("k")) {
            return (piece.equals("K") ? "wK" : "bK");
        }
        return null; // For empty tiles
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
                    validMoves = calculateValidMoves(selectedPiece, selectedX, selectedY);
                    repaint();
                }
            }
        } else {
            // Move the piece
            if (validMoves.contains(x + "," + y)) {
                String capturedPiece = board[y][x];
                board[y][x] = selectedPiece;
                board[selectedY][selectedX] = ".";
                selectedPiece = null;
                isWhiteTurn = !isWhiteTurn;
                validMoves.clear();
                repaint();

                // Handle capture (capturing the opponent's piece)
                if (!capturedPiece.equals(".")) {
                    System.out.println("Captured: " + capturedPiece);
                }
            } else {
                selectedPiece = null;
                validMoves.clear();
                repaint();
            }
        }

        // Handle Computer's Move (if playing against the computer)
        if (playWithComputer && !isWhiteTurn && !isGameOver) {
            makeComputerMove();
        }
    }

    private void makeComputerMove() {
        if (currentPlayer instanceof ComputerPlayer) {
            // Simulate computer move (this is simplified, you would implement a real AI here)
            System.out.println("Computer is making a move...");
            // In a real implementation, you would select a valid move for the computer here
            isWhiteTurn = true; // Switch back to human player after the computer move
            repaint();
        }
    }

    private Set<String> calculateValidMoves(String piece, int x, int y) {
        Set<String> moves = new HashSet<>();
        if (piece.equals("P") || piece.equals("p")) {
            int direction = piece.equals("P") ? -1 : 1;  // White moves up, black moves down
            // Regular move one square forward
            if (y + direction >= 0 && y + direction < 8 && board[y + direction][x].equals(".")) {
                moves.add(x + "," + (y + direction));
            }
            // Capture diagonally
            if (x + 1 < 8 && y + direction >= 0 && y + direction < 8 && !board[y + direction][x + 1].equals(".") &&
                (Character.isLowerCase(board[y + direction][x + 1].charAt(0)) != (piece.equals("P")))) {
                moves.add((x + 1) + "," + (y + direction));
            }
            if (x - 1 >= 0 && y + direction >= 0 && y + direction < 8 && !board[y + direction][x - 1].equals(".") &&
                (Character.isLowerCase(board[y + direction][x - 1].charAt(0)) != (piece.equals("P")))) {
                moves.add((x - 1) + "," + (y + direction));
            }
            // Special move (two squares forward on first move)
            if ((piece.equals("P") && y == 6) || (piece.equals("p") && y == 1)) {
                if (board[y + direction][x].equals(".") && board[y + direction * 2][x].equals(".")) {
                    moves.add(x + "," + (y + direction * 2));
                }
            }
        }
        else if (piece.equals("N") || piece.equals("n")) {  // Knight
            int[] dx = {-2, -1, 1, 2, 2, 1, -1, -2};
            int[] dy = {1, 2, 2, 1, -1, -2, -2, -1};
            for (int i = 0; i < 8; i++) {
                int newX = x + dx[i];
                int newY = y + dy[i];
                if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
                    String target = board[newY][newX];
                    if (target.equals(".") || (Character.isLowerCase(target.charAt(0)) != Character.isLowerCase(piece.charAt(0)))) {
                        moves.add(newX + "," + newY);
                    }
                }
            }
        }
        else if (piece.equals("R") || piece.equals("r")) {  // Rook
            moves.addAll(calculateStraightMoves(x, y, piece));
        }
        else if (piece.equals("B") || piece.equals("b")) {  // Bishop
            moves.addAll(calculateDiagonalMoves(x, y, piece));
        }
        else if (piece.equals("Q") || piece.equals("q")) {  // Queen
            moves.addAll(calculateStraightMoves(x, y, piece));
            moves.addAll(calculateDiagonalMoves(x, y, piece));
        }
        else if (piece.equals("K") || piece.equals("k")) {  // King
            int[] dx = {-1, 0, 1, 1, 1, 0, -1, -1};
            int[] dy = {-1, -1, -1, 0, 1, 1, 1, 0};
            for (int i = 0; i < 8; i++) {
                int newX = x + dx[i];
                int newY = y + dy[i];
                if (newX >= 0 && newX < 8 && newY >= 0 && newY < 8) {
                    String target = board[newY][newX];
                    if (target.equals(".") || (Character.isLowerCase(target.charAt(0)) != Character.isLowerCase(piece.charAt(0)))) {
                        moves.add(newX + "," + newY);
                    }
                }
            }
        }
        return moves;
    }

    // Helper methods to calculate valid moves for straight-moving pieces (Rook, Queen)
    private Set<String> calculateStraightMoves(int x, int y, String piece) {
        Set<String> moves = new HashSet<>();
        String[] directions = {"up", "down", "left", "right"};
        for (String direction : directions) {
            int i = 1;
            while (true) {
                int newX = x, newY = y;
                if (direction.equals("up")) newY -= i;
                if (direction.equals("down")) newY += i;
                if (direction.equals("left")) newX -= i;
                if (direction.equals("right")) newX += i;
                if (newX < 0 || newY < 0 || newX >= 8 || newY >= 8) break;

                String target = board[newY][newX];
                if (target.equals(".")) {
                    moves.add(newX + "," + newY);
                } else {
                    if ((Character.isLowerCase(target.charAt(0)) != Character.isLowerCase(piece.charAt(0)))) {
                        moves.add(newX + "," + newY);
                    }
                    break;
                }
                i++;
            }
        }
        return moves;
    }

    // Helper methods to calculate diagonal-moving pieces (Bishop, Queen)
    private Set<String> calculateDiagonalMoves(int x, int y, String piece) {
        Set<String> moves = new HashSet<>();
        String[] directions = {"up-left", "up-right", "down-left", "down-right"};
        for (String direction : directions) {
            int i = 1;
            while (true) {
                int newX = x, newY = y;
                if (direction.equals("up-left")) {
                    newX -= i;
                    newY -= i;
                }
                if (direction.equals("up-right")) {
                    newX += i;
                    newY -= i;
                }
                if (direction.equals("down-left")) {
                    newX -= i;
                    newY += i;
                }
                if (direction.equals("down-right")) {
                    newX += i;
                    newY += i;
                }
                if (newX < 0 || newY < 0 || newX >= 8 || newY >= 8) break;

                String target = board[newY][newX];
                if (target.equals(".")) {
                    moves.add(newX + "," + newY);
                } else {
                    if ((Character.isLowerCase(target.charAt(0)) != Character.isLowerCase(piece.charAt(0)))) {
                        moves.add(newX + "," + newY);
                    }
                    break;
                }
                i++;
            }
        }
        return moves;
    }

    // Unused mouse events
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // Player class and subclasses for Human and Computer players
    abstract class Player {
        boolean isWhite;

        Player(boolean isWhite) {
            this.isWhite = isWhite;
        }

        abstract void makeMove();
    }

    class HumanPlayer extends Player {
        HumanPlayer(boolean isWhite) {
            super(isWhite);
        }

        @Override
        void makeMove() {
            // Human player makes move (already handled by mouse events)
        }
    }

    class ComputerPlayer extends Player {
        ComputerPlayer(boolean isWhite) {
            super(isWhite);
        }

        @Override
        void makeMove() {
            // Implement simple AI for computer move here
        }
    }

    // Main method to start the game
    public static void main(String[] args) {
        new cg(true); // Change to new cg(false) for Human vs Human
    }
}