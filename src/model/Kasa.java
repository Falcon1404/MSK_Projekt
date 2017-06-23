package model;


import java.util.ArrayList;

public class Kasa
{
    public Klient aktualnieObslugiwanyKlient;
    public ArrayList<Klient> kolejkaKlientow = new ArrayList<>();
    public int ID;
    public int liczbaKlientowWKolejce;
    public boolean czyPrzepelniona;
    public final int MAX_LICZBA_KLIENTOW = 5;

    public Kasa(int ID, int liczbaKlientowWKolejce, boolean czyPrzepelniona)
    {
        this.ID = ID;
        this.liczbaKlientowWKolejce = liczbaKlientowWKolejce;
        this.czyPrzepelniona = czyPrzepelniona;
        kolejkaKlientow = new ArrayList<>();
    }

    public void addKlient(Klient klient)
    {
        if(klient.czyVIP == true)
        {
            int naKtoraPozycje = 0;
            for(; naKtoraPozycje < kolejkaKlientow.size(); naKtoraPozycje++)
            {
                if(!kolejkaKlientow.get(naKtoraPozycje).czyVIP)
                {
                    break;
                }
            }
            kolejkaKlientow.add(naKtoraPozycje, klient);
        }
        else
        {
            kolejkaKlientow.add(klient);
        }
    }

}
