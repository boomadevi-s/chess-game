import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class test extends Frame implements MouseListener {
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
    public test(boolean playWithComputer) {
        this.playWithComputer = playWithComputer;
        board = new String[8][8];
        whitePieces = new HashSet<>(Arrays.asList("P", "R", "N", "B", "Q", "K"));
        blackPieces = new HashSet<>(Arrays.asList("p", "r", "n", "b", "q", "k"));
        initializeBoard();
        loadPieceImages();
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
                g.setColor((i + j) % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                String piece = board[i][j];
                if (!piece.equals(".")) {
                    Image pieceImage = pieceImages.get(getPieceKey(piece));
                    if (pieceImage != null) {
                        g.drawImage(pieceImage, j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                    }
                }
            }
        }

        if (selectedPiece != null) {
            g.setColor(Color.GREEN);
            for (String move : validMoves) {
                int x = Integer.parseInt(move.split(",")[0]);
                int y = Integer.parseInt(move.split(",")[1]);
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private String getPieceKey(String piece) {
        return (Character.isUpperCase(piece.charAt(0)) ? "w" : "b") + piece.toUpperCase();
    }

    // Required MouseListener methods
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / TILE_SIZE;
        int y = e.getY() / TILE_SIZE;

        if (selectedPiece == null) {
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
            if (validMoves.contains(x + "," + y)) {
                board[y][x] = selectedPiece;
                board[selectedY][selectedX] = ".";
                selectedPiece = null;
                isWhiteTurn = !isWhiteTurn;
                validMoves.clear();
                repaint();
            } else {
                selectedPiece = null;
                validMoves.clear();
                repaint();
            }
        }

        if (playWithComputer && !isWhiteTurn && !isGameOver) makeComputerMove();
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    private void makeComputerMove() {
        if (currentPlayer instanceof ComputerPlayer) {
            System.out.println("Computer is making a move...");
            isWhiteTurn = true;
            repaint();
        }
    }

    private Set<String> calculateValidMoves(String piece, int x, int y) {
        Set<String> moves = new HashSet<>();
        boolean isWhite = Character.isUpperCase(piece.charAt(0));
    
        switch (piece.toUpperCase()) {
            case "P":
                int direction = isWhite ? -1 : 1;
                // Regular move
                if (y + direction >= 0 && y + direction < 8 && board[y + direction][x].equals(".")) {
                    moves.add(x + "," + (y + direction));
                    // Check for two-square advance
                    if ((isWhite && y == 6 || !isWhite && y == 1) && board[y + direction * 2][x].equals(".")) {
                        moves.add(x + "," + (y + direction * 2));
                    }
                }
                // Diagonal captures
                if (x + 1 < 8 && y + direction >= 0 && y + direction < 8 && isOpponentPiece(board[y + direction][x + 1], isWhite)) {
                    moves.add((x + 1) + "," + (y + direction));
                }
                if (x - 1 >= 0 && y + direction >= 0 && y + direction < 8 && isOpponentPiece(board[y + direction][x - 1], isWhite)) {
                    moves.add((x - 1) + "," + (y + direction));
                }
                break;
    
            case "R":
                // Handle Rook movement
                for (int i = 1; i < 8; i++) {
                    if (addMoveIfValid(moves, x + i, y, isWhite)) break;
                    if (addMoveIfValid(moves, x - i, y, isWhite)) break;
                    if (addMoveIfValid(moves, x, y + i, isWhite)) break;
                    if (addMoveIfValid(moves, x, y - i, isWhite)) break;
                }
                break;
    
            case "N":
                // Handle Knight movement
                int[][] knightMoves = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
                for (int[] move : knightMoves) {
                    addMoveIfValid(moves, x + move[0], y + move[1], isWhite);
                }
                break;
    
            case "B":
                // Handle Bishop movement
                for (int i = 1; i < 8; i++) {
                    if (addMoveIfValid(moves, x + i, y + i, isWhite)) break;
                    if (addMoveIfValid(moves, x - i, y + i, isWhite)) break;
                    if (addMoveIfValid(moves, x + i, y - i, isWhite)) break;
                    if (addMoveIfValid(moves, x - i, y - i, isWhite)) break;
                }
                break;
    
            case "Q":
                // Handle Queen movement (combination of Rook and Bishop)
                for (int i = 1; i < 8; i++) {
                    if (addMoveIfValid(moves, x + i, y, isWhite)) break;
                    if (addMoveIfValid(moves, x - i, y, isWhite)) break;
                    if (addMoveIfValid(moves, x, y + i, isWhite)) break;
                    if (addMoveIfValid(moves, x, y - i, isWhite)) break;
                    if (addMoveIfValid(moves, x + i, y + i, isWhite)) break;
                    if (addMoveIfValid(moves, x - i, y + i, isWhite)) break;
                    if (addMoveIfValid(moves, x + i, y - i, isWhite)) break;
                    if (addMoveIfValid(moves, x - i, y - i, isWhite)) break;
                }
                break;
    
            case "K":
                // Handle King movement
                int[][] kingMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
                for (int[] move : kingMoves) {
                    addMoveIfValid(moves, x + move[0], y + move[1], isWhite);
                }
                break;
        }
        return moves;
    }
    
    private boolean isOpponentPiece(String target, boolean isWhite) {
        // Return true if the piece is an opponent's piece (White and Black are opposites)
        if (isWhite) {
            return target.equals("p") || target.equals("r") || target.equals("n") || target.equals("b") || target.equals("q") || target.equals("k");
        } else {
            return target.equals("P") || target.equals("R") || target.equals("N") || target.equals("B") || target.equals("Q") || target.equals("K");
        }
    }
    
    private boolean addMoveIfValid(Set<String> moves, int x, int y, boolean isWhite) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) return true;
        String target = board[y][x];
        if (target.equals(".")) {
            moves.add(x + "," + y);
            return false;
        }
        if (isWhite && blackPieces.contains(target) || !isWhite && whitePieces.contains(target)) {
            moves.add(x + "," + y);
        }
        return true;
    }

    // Start the game
    public static void main(String[] args) {
        new test(false);
    }
}

interface Player {}

class HumanPlayer implements Player {
    private boolean isWhite;

    public HumanPlayer(boolean isWhite) {
        this.isWhite = isWhite;
    }
}

class ComputerPlayer implements Player {
    private boolean isWhite;

    public ComputerPlayer(boolean isWhite) {
        this.isWhite = isWhite;
    }
}
