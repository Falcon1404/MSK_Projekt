package model;

public class Klient
{
    public int ID;
    public double iloscGotowki;
    public int nrKasy;
    public int iloscTowarow;
    public double wejscieDoKolejki;
    public double rozpoczecieObslugi;
    public double zakonczenieObslugi;
    public double czasObslugi;
    public double czasRobieniaZakupow;
    public boolean czySkonczylRobicZakupy;
    public boolean czyZostalObsluzony;
    public boolean czyVIP = false;

    public Klient(double czasRobieniaZakupow, int iloscTowarow, double iloscGotowki)
    {
        this.iloscGotowki = iloscGotowki;
        this.iloscTowarow = iloscTowarow;
        this.czasRobieniaZakupow = czasRobieniaZakupow;
        this.czasObslugi = iloscTowarow * 500.0;
    }

    public Klient(double czasRobieniaZakupow, int iloscTowarow, double iloscGotowki, boolean czyVIP)
    {
        this.iloscGotowki = iloscGotowki;
        this.iloscTowarow = iloscTowarow;
        this.czasRobieniaZakupow = czasRobieniaZakupow;
        this.czasObslugi = iloscTowarow * 500.0;
        this.czyVIP = czyVIP;
    }

    public void czySkonczylRobicZakupy(double federateTime)
    {
        if(federateTime >= czasRobieniaZakupow)
        {
            czySkonczylRobicZakupy = true;
        }
    }

    public boolean czyZostalObsluzony(double federateTime)
    {
        if(federateTime >= rozpoczecieObslugi + czasObslugi)
        {
            czyZostalObsluzony = true;
            return true;
        }
        return false;
    }

}
