package com.example.puissance4_avec_ia;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ImageButton AddJeton[] = new ImageButton[7];
    private ImageButton grille[][] = new ImageButton[6][7];
    private String joueur="JAUNE";
    private TextView tv;
    private Spinner spinnerMode;

    LinearLayout Ajouter;
    GridLayout Grille;

    private int modeJeu = 0; // 0: 2 Joueurs, 1: Débutant, 2: Intermédiaire, 3: Expert
    private Random random = new Random();
    private boolean partieEnCours = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv = findViewById(R.id.textView2);
        spinnerMode = findViewById(R.id.spinnerMode);

        String[] modes = {
                "Mode : 2 Joueurs (Humain vs Humain)",
                "Mode : IA - Débutant",
                "Mode : IA - Intermédiaire ",
                "Mode : IA - Expert "
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(adapter);

        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modeJeu = position;
                reinitgrille();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Ajouter = findViewById(R.id.ajouter);
        for (int i=0; i<Ajouter.getChildCount(); i++) {
            AddJeton[i] = (ImageButton) Ajouter.getChildAt(i);
            AddJeton[i].setImageResource(R.drawable.addjetonjaune);
            AddJeton[i].setTag(i);
        }

        Grille = findViewById(R.id.grille);
        for(int l=0; l<6; l++) {
            for (int c = 0; c < 7; c++) {
                grille[l][c] = (ImageButton) Grille.getChildAt(l*7 + c);
                grille[l][c].setImageResource(R.drawable.p24blanc);
                grille[l][c].setTag("BLANC");
            }
        }
    }

    void Bloquerboutons(boolean b) {
        for (int i=0; i<Ajouter.getChildCount(); i++) {
            AddJeton[i].setEnabled(b);
        }
    }

    public void AjouterJeton(View view) {
        if (!partieEnCours) return;
        if (modeJeu > 0 && joueur.compareTo("ROUGE") == 0) return;

        int colonne = Integer.valueOf(view.getTag().toString());
        jouerColonneInterne(colonne);
    }

    private void jouerColonneInterne(int colonne) {
        int lignelibre = obtenirLigneLibre(colonne);
        if (lignelibre != -1) {
            if (joueur.compareTo("JAUNE") == 0) {
                grille[lignelibre][colonne].setImageResource(R.drawable.p24jaune);
                grille[lignelibre][colonne].setTag("JAUNE");

                if (TesterGagnant(lignelibre, colonne, "JAUNE").compareTo("JAUNE") == 0) {
                    partieEnCours = false;
                    if (modeJeu == 0) tv.setText("JOUEUR JAUNE A GAGNE");
                    else tv.setText("VOUS AVEZ GAGNE !");
                    Bloquerboutons(false);
                } else if (verifierGrillePleine()) {
                    partieEnCours = false;
                    tv.setText("MATCH NUL !");
                    Bloquerboutons(false);
                } else {
                    joueur = "ROUGE";
                    imageboutonadd("ROUGE");
                    if (modeJeu == 0) {
                        tv.setText("AU JOUEUR ROUGE DE JOUER");
                    } else {
                        tv.setText("L'IA REFLECHIT...");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (partieEnCours) jouerCoupIA();
                            }
                        }, 600);
                    }
                }
            } else { // Tour de l'IA (ROUGE)
                grille[lignelibre][colonne].setImageResource(R.drawable.p24rouge);
                grille[lignelibre][colonne].setTag("ROUGE");

                if (TesterGagnant(lignelibre, colonne, "ROUGE").compareTo("ROUGE") == 0) {
                    partieEnCours = false;
                    if (modeJeu == 0) tv.setText("JOUEUR ROUGE A GAGNE");
                    else tv.setText("L'IA A GAGNE !");
                    Bloquerboutons(false);
                } else if (verifierGrillePleine()) {
                    partieEnCours = false;
                    tv.setText("MATCH NUL !");
                    Bloquerboutons(false);
                } else {
                    joueur = "JAUNE";
                    imageboutonadd("JAUNE");
                    if (modeJeu == 0) {
                        tv.setText("AU JOUEUR JAUNE DE JOUER");
                    } else {
                        tv.setText("A VOTRE TOUR");
                    }
                }
            }
        } else {
            if (modeJeu == 0 || joueur.compareTo("JAUNE") == 0) {
                Toast.makeText(this, "La colonne est pleine", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void jouerCoupIA() {
        if (!partieEnCours) return;

        int colChoisie = -1;

        if (modeJeu == 1) {
            // Mode Débutant : Bloque les victoires imminentes du joueur, sinon aléatoire
            colChoisie = trouverCoupDebutant();
        } else if (modeJeu == 2) {
            // Mode Intermédiaire : Gagne ou bloque activement
            colChoisie = trouverCoupIntermediaire();
        } else if (modeJeu == 3) {
            // Mode Expert : Algorithme Minimax
            colChoisie = trouverCoupExpert();
        }

        if (colChoisie != -1) {
            jouerColonneInterne(colChoisie);
        } else {
            colChoisie = trouverColonneAleatoire();
            if (colChoisie != -1) jouerColonneInterne(colChoisie);
        }
    }

    private int trouverColonneAleatoire() {
        boolean valide = false;
        int col = 0;
        int essais = 0;
        do {
            col = random.nextInt(7);
            if (grille[0][col].getTag().toString().compareTo("BLANC") == 0) {
                valide = true;
            }
            essais++;
            if (essais > 50) break;
        } while (!valide);
        return col;
    }

    // Le mode Débutant bloque désormais les alignements de 4 imminents (horizontal, vertical, diagonal)
    private int trouverCoupDebutant() {
        for (int c = 0; c < 7; c++) {
            int l = obtenirLigneLibre(c);
            if (l != -1) {
                grille[l][c].setTag("JAUNE");
                if (TesterGagnant(l, c, "JAUNE").compareTo("JAUNE") == 0) {
                    grille[l][c].setTag("BLANC");
                    return c; // Bloque votre tentative de 4 alignés
                }
                grille[l][c].setTag("BLANC");
            }
        }
        return trouverColonneAleatoire();
    }

    private int trouverCoupIntermediaire() {
        // 1. L'IA cherche si elle peut gagner immédiatement en un coup
        for (int c = 0; c < 7; c++) {
            int l = obtenirLigneLibre(c);
            if (l != -1) {
                grille[l][c].setTag("ROUGE");
                if (TesterGagnant(l, c, "ROUGE").compareTo("ROUGE") == 0) {
                    grille[l][c].setTag("BLANC");
                    return c;
                }
                grille[l][c].setTag("BLANC");
            }
        }

        // 2. L'IA analyse si vous (JAUNE) êtes sur le point de gagner et vous BLOQUE impérativement
        for (int c = 0; c < 7; c++) {
            int l = obtenirLigneLibre(c);
            if (l != -1) {
                grille[l][c].setTag("JAUNE");
                if (TesterGagnant(l, c, "JAUNE").compareTo("JAUNE") == 0) {
                    grille[l][c].setTag("BLANC");
                    return c; // Bloque votre alignement de 4 !
                }
                grille[l][c].setTag("BLANC");
            }
        }

        // 3. Sinon, privilégie le centre ou joue aléatoirement
        if (grille[0][3].getTag().toString().compareTo("BLANC") == 0 && random.nextDouble() < 0.7) {
            return 3;
        }

        return trouverColonneAleatoire();
    }

    // ==========================================
    // IA EXPERT : Minimax
    // ==========================================
    private int trouverCoupExpert() {
        String[][] board = new String[6][7];
        for(int l=0; l<6; l++) {
            for(int c=0; c<7; c++) {
                board[l][c] = grille[l][c].getTag().toString();
            }
        }

        int bestScore = -1000000;
        int bestCol = 3;
        int[] colOrder = {3, 2, 4, 1, 5, 0, 6};

        for (int col : colOrder) {
            int row = obtenirLigneLibreSimulee(board, col);
            if (row != -1) {
                board[row][col] = "ROUGE";
                int score = minimax(board, 5, false, -1000000, 1000000);
                board[row][col] = "BLANC";

                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                }
            }
        }
        return bestCol;
    }

    private int minimax(String[][] board, int depth, boolean isMaximizing, int alpha, int beta) {
        if (verifierVictoireSimulee(board, "ROUGE")) return 100000 + depth;
        if (verifierVictoireSimulee(board, "JAUNE")) return -100000 - depth;
        if (depth == 0 || estGrillePleineSimulee(board)) return evaluerPlateau(board);

        int[] colOrder = {3, 2, 4, 1, 5, 0, 6};

        if (isMaximizing) {
            int maxEval = -1000000;
            for (int col : colOrder) {
                int row = obtenirLigneLibreSimulee(board, col);
                if (row != -1) {
                    board[row][col] = "ROUGE";
                    int eval = minimax(board, depth - 1, false, alpha, beta);
                    board[row][col] = "BLANC";
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            }
            return maxEval;
        } else {
            int minEval = 1000000;
            for (int col : colOrder) {
                int row = obtenirLigneLibreSimulee(board, col);
                if (row != -1) {
                    board[row][col] = "JAUNE";
                    int eval = minimax(board, depth - 1, true, alpha, beta);
                    board[row][col] = "BLANC";
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;
                }
            }
            return minEval;
        }
    }

    private int obtenirLigneLibreSimulee(String[][] board, int col) {
        for (int l = 5; l >= 0; l--) {
            if (board[l][col].equals("BLANC")) return l;
        }
        return -1;
    }

    private boolean estGrillePleineSimulee(String[][] board) {
        for (int c = 0; c < 7; c++) {
            if (board[0][c].equals("BLANC")) return false;
        }
        return true;
    }

    private boolean verifierVictoireSimulee(String[][] board, String coul) {
        for (int l = 0; l < 6; l++) {
            for (int c = 0; c < 7; c++) {
                if (board[l][c].equals(coul)) {
                    if (c + 3 < 7 && board[l][c+1].equals(coul) && board[l][c+2].equals(coul) && board[l][c+3].equals(coul)) return true;
                    if (l + 3 < 6 && board[l+1][c].equals(coul) && board[l+2][c].equals(coul) && board[l+3][c].equals(coul)) return true;
                    if (l + 3 < 6 && c + 3 < 7 && board[l+1][c+1].equals(coul) && board[l+2][c+2].equals(coul) && board[l+3][c+3].equals(coul)) return true;
                    if (l - 3 >= 0 && c + 3 < 7 && board[l-1][c+1].equals(coul) && board[l-2][c+2].equals(coul) && board[l-3][c+3].equals(coul)) return true;
                }
            }
        }
        return false;
    }

    private int evaluerPlateau(String[][] board) {
        int score = 0;
        int centerCount = 0;
        for (int l = 0; l < 6; l++) {
            if (board[l][3].equals("ROUGE")) centerCount++;
        }
        score += centerCount * 20;

        for (int l = 0; l < 6; l++) {
            for (int c = 0; c < 7; c++) {
                if (c + 3 < 7) {
                    String[] fenetre = {board[l][c], board[l][c+1], board[l][c+2], board[l][c+3]};
                    score += evaluerFenetre(fenetre);
                }
                if (l + 3 < 6) {
                    String[] fenetre = {board[l][c], board[l+1][c], board[l+2][c], board[l+3][c]};
                    score += evaluerFenetre(fenetre);
                }
                if (l + 3 < 6 && c + 3 < 7) {
                    String[] fenetre = {board[l][c], board[l+1][c+1], board[l+2][c+2], board[l+3][c+3]};
                    score += evaluerFenetre(fenetre);
                }
                if (l - 3 >= 0 && c + 3 < 7) {
                    String[] fenetre = {board[l][c], board[l-1][c+1], board[l-2][c+2], board[l-3][c+3]};
                    score += evaluerFenetre(fenetre);
                }
            }
        }
        return score;
    }

    private int evaluerFenetre(String[] fenetre) {
        int score = 0;
        int countRouge = 0;
        int countJaune = 0;
        int countBlanc = 0;

        for (String s : fenetre) {
            if (s.equals("ROUGE")) countRouge++;
            else if (s.equals("JAUNE")) countJaune++;
            else if (s.equals("BLANC")) countBlanc++;
        }

        if (countRouge == 3 && countBlanc == 1) score += 200;
        else if (countRouge == 2 && countBlanc == 2) score += 20;

        if (countJaune == 3 && countBlanc == 1) score -= 180;
        return score;
    }
    // ==========================================

    private boolean verifierGrillePleine() {
        for (int c = 0; c < 7; c++) {
            if (grille[0][c].getTag().toString().compareTo("BLANC") == 0) return false;
        }
        return true;
    }

    private void reinitgrille() {
        joueur="JAUNE";
        partieEnCours = true;
        if (modeJeu == 0) {
            tv.setText("AU JOUEUR JAUNE DE JOUER");
        } else {
            tv.setText("A VOTRE TOUR");
        }
        imageboutonadd("JAUNE");
        for (int i=0; i<=5; i++) {
            for (int j = 0; j <= 6; j++) {
                grille[i][j].setImageResource(R.drawable.p24blanc);
                grille[i][j].setTag("BLANC");
            }
        }
        Bloquerboutons(true);
    }

    public void init(View v) {
        reinitgrille();
    }

    private int obtenirLigneLibre(int colonne) {
        for (int l = 5; l >= 0; l--) {
            if (grille[l][colonne].getTag().toString().compareTo("BLANC") == 0) {
                return l;
            }
        }
        return -1;
    }

    private void imageboutonadd(String couleur) {
        if (couleur.compareTo("JAUNE")==0) {
            for (int i = 0; i<=6; i++)
                AddJeton[i].setImageResource(R.drawable.addjetonjaune);
        } else {
            for (int i = 0; i<=6; i++)
                AddJeton[i].setImageResource(R.drawable.addjetonrouge);
        }
    }

    private String TesterGagnant(int li, int co, String couleurCible) {
        if(!(TesterGagnantvertical(li, co, couleurCible).isEmpty())) return couleurCible;
        else if(!(TesterGagnanthorizontal(li, co, couleurCible).isEmpty())) return couleurCible;
        else if(!(TesterGagnantdiag1(li, co, couleurCible).isEmpty())) return couleurCible;
        else if(!(TesterGagnantdiag2(li, co, couleurCible).isEmpty())) return couleurCible;
        else return "";
    }

    private String TesterGagnantvertical(int li, int co, String couleurCible) {
        int c = 0;
        int lig = li;
        while(lig < 6 && grille[lig][co].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            lig++;
        }
        if (c >= 4) return couleurCible;
        else return "";
    }

    private String TesterGagnanthorizontal(int li, int co, String couleurCible) {
        int c = 0;
        int col = co;
        while (col < 7 && grille[li][col].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            col++;
        }
        col = co - 1;
        while (col >= 0 && grille[li][col].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            col--;
        }
        if (c >= 4) return couleurCible;
        else return "";
    }

    private String TesterGagnantdiag1(int li, int co, String couleurCible) {
        int c = 0;
        int col = co;
        int lig = li;
        while (col < 7 && lig < 6 && grille[lig][col].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            col++;
            lig++;
        }
        col = co - 1;
        lig = li - 1;
        while (col >= 0 && lig >= 0 && grille[lig][col].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            col--;
            lig--;
        }
        if (c >= 4) return couleurCible;
        else return "";
    }

    private String TesterGagnantdiag2(int li, int co, String couleurCible) {
        int c = 0;
        int col = co;
        int lig = li;
        while (col < 7 && lig >= 0 && grille[lig][col].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            col++;
            lig--;
        }
        col = co - 1;
        lig = li + 1;
        while (col >= 0 && lig < 6 && grille[lig][col].getTag().toString().compareTo(couleurCible) == 0) {
            c++;
            col--;
            lig++;
        }
        if (c >= 4) return couleurCible;
        else return "";
    }
}