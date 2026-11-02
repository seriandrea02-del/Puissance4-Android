package com.example.puissance4_sans_ia;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;

public class MainActivity extends AppCompatActivity {
    private ImageButton AddJeton[] = new ImageButton[7];
    private ImageButton grille[][] = new ImageButton[6][7];
    private String joueur="JAUNE";
    private TextView tv;


    LinearLayout Ajouter;

    GridLayout Grille;


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

        Ajouter =  findViewById(R.id.ajouter);
        for (int i=0;i<Ajouter.getChildCount();i++)
        {
            AddJeton[i] = (ImageButton) Ajouter.getChildAt(i);
            AddJeton[i].setImageResource(R.drawable.addjetonjaune);
            AddJeton[i].setTag(i);
        }

        Grille = findViewById(R.id.grille);
        for(int l=0;l<6;l++)
        {
            for (int c = 0; c < 7; c++)
            {
                grille[l][c] = (ImageButton) Grille.getChildAt(l*7+ c);
                grille[l][c].setImageResource(R.drawable.p24blanc);
                grille[l][c].setTag("BLANC");
            }
        }
    }

    void Bloquerboutons(boolean b)
    {
        for (int i=0;i<Ajouter.getChildCount();i++)
        {
            AddJeton[i].setEnabled(b);
        }
    }

    public void AjouterJeton(View view)
    {
        int colonne = Integer.valueOf(view.getTag().toString());
        int lignelibre = TrouverCelluleLibre(colonne);
        if (lignelibre != -1)
        {
            if (joueur.compareTo("JAUNE") == 0)
            {
                grille[lignelibre][colonne].setImageResource(R.drawable.p24jaune);
                grille[lignelibre][colonne].setTag("JAUNE");
                if (TesterGagnant(lignelibre,colonne).compareTo("JAUNE")==0)
                {
                    tv.setText("JOUEUR JAUNE A GAGNE");
                    Bloquerboutons(false);
                }
                else
                {
                    joueur = "ROUGE";
                    imageboutonadd("ROUGE");
                    tv.setText("AU JOUEUR ROUGE DE JOUER");
                }
            } else
            {
                grille[lignelibre][colonne].setImageResource(R.drawable.p24rouge);
                grille[lignelibre][colonne].setTag("ROUGE");
                if (TesterGagnant(lignelibre,colonne).compareTo("ROUGE")==0)
                {
                    tv.setText("JOUEUR ROUGE A GAGNE");
                    Bloquerboutons(false);
                }
                else
                {
                    joueur = "JAUNE";
                    imageboutonadd("JAUNE");
                    tv.setText("AU JOUEUR JAUNE DE JOUER");
                }
            }
        }
        else
        {
            Toast.makeText(this, "La colonne est pleine", Toast.LENGTH_SHORT).show(); // in Activity
        }

    }

    private void reinitgrille()
    {
        joueur="JAUNE";
        tv.setText("AU JOUEUR JAUNE DE JOUER");
        imageboutonadd("JAUNE");
        for (int i=0;i<=5;i++)
        {
            for (int j = 0; j <= 6; j++) {
                grille[i][j].setImageResource(R.drawable.p24blanc);
                grille[i][j].setTag("BLANC");
            }
        }
        Bloquerboutons(true);
    }

    public void init(View v)
    {
        reinitgrille();
    }

    private int TrouverCelluleLibre(int colonne)
    {
        int i = 0;
        boolean trouver = false;
        while (!trouver && i < 6)
        {
            String couleur = grille[i][colonne].getTag().toString();
            if (couleur.compareTo("BLANC") != 0)
                trouver = true;
            else
                i++;
        }
        return (i - 1);
    }

    private void imageboutonadd(String couleur)
    {
        if (couleur.compareTo("JAUNE")==0)
        {
            for (int i = 0;i<=6;i++)
                AddJeton[i].setImageResource(R.drawable.addjetonjaune);
        }
        else
        {
            for (int i = 0;i<=6;i++)
                AddJeton[i].setImageResource(R.drawable.addjetonrouge);
        }
    }

    private String TesterGagnant(int li,int co)
    {
        if(!(TesterGagnantvertical(li,co).compareTo("")==0))
        {
            return joueur;
        }
        else if(!(TesterGagnanthorizontal(li,co).compareTo("")==0))
        {
            return joueur;
        }
        else if(!(TesterGagnantdiag1(li,co).compareTo("")==0))
        {
            return joueur;
        }
        else if(!(TesterGagnantdiag2(li,co).compareTo("")==0))
        {
            return joueur;
        }
        else
            return "";
    }

    private String TesterGagnantvertical(int li,int co)
    {
        String couleur;
        int c = 0;
        int lig = li;
        boolean encore = true;
        while(encore==true)
        {
            if (grille[lig][co].getTag().toString().compareTo(joueur)==0) {
                c++;
                encore = true;
            }
            else
                encore = false;
            lig++;
            if (lig==6)
                encore = false;
        }

        if (c>=4)
            return joueur;
        else
            return "";
    }
    private String TesterGagnanthorizontal(int li,int co)
    {
        String couleur;
        int c = 0;
        int col=co;
        boolean encore=true;
        //test à droite
        while (encore)
        {
            if ((grille[li][col].getTag().toString().compareTo(joueur)==0))
            {
                c++;
                encore=true;
            }
            else
                encore=false;

            col++;
            if (col==7)
                encore = false;
        }

        //test vers la gauche
        encore=true;
        col = co;
        while (encore)
        {
            if ((grille[li][col].getTag().toString().compareTo(joueur)==0))
            {
                c++;
                encore=true;
            }
            else
                encore=false;

            col--;
            if (col==-1)
                encore = false;
        }
        if (c>=5)
            return joueur;
        else
            return "";
    }

    private String TesterGagnantdiag1(int li,int co) {
        String couleur;
        int c = 0;
        int col = co;
        int lig = li;
        boolean encore = true;
        //test vers le bas à droite
        while (encore)
        {
            if ((grille[lig][col].getTag().toString().compareTo(joueur) == 0))
            {
                c++;
                encore = true;
            } else
                encore = false;
            col++;
            lig++;
            if ((col == 7) || (lig == 6))
                encore = false;
        }

        col = co;
        lig = li;
        encore = true;
        while (encore)
        {
            if ((grille[lig][col].getTag().toString().compareTo(joueur)==0))
            {
                c++;
                encore=true;
            }
            else
                encore=false;
            col--;
            lig--;
            if ((col==-1) || (lig==-1))
                encore = false;
        }
        if (c>=5)
            return joueur;
        else
            return "";
    }



    private String TesterGagnantdiag2(int li,int co)
    {
        String couleur;
        int c = 0;
        int col = co;
        int lig = li;
        boolean encore = true;
        //test vers le bas à droite
        while (encore)
        {
            if ((grille[lig][col].getTag().toString().compareTo(joueur) == 0))
            {
                c++;
                encore = true;
            } else
                encore = false;
            col++;
            lig--;
            if ((col == 7) || (lig == -1))
                encore = false;
        }

        col = co;
        lig = li;
        encore = true;
        while (encore)
        {
            if ((grille[lig][col].getTag().toString().compareTo(joueur)==0))
            {
                c++;
                encore=true;
            }
            else
                encore=false;
            col--;
            lig++;
            if ((col==-1) || (lig==6))
                encore = false;
        }
        if (c>=5)
            return joueur;
        else
            return "";
    }


}
