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
    public double czasUtworzeniaKlienta;
    public double czasZakoczeniaZakupow;

    public boolean czyJestWKolejce = false;
    public boolean czySkonczylRobicZakupy = false;
    public boolean czyJestObslugiwany = false;
    public boolean czyZostalObsluzony = false;
    public boolean czyVIP = false;


    //1. czasUtworzeniaKlienta 1800
    //2. czasZakoczeniaZakupow 2400
    //3. wejscieDoKolejki 2600 (2600 - 2400) + (2400 - 1800)

    public Klient(int ID, double czasUtworzeniaKlienta, double czasZakoczeniaZakupow, int iloscTowarow, double iloscGotowki)
    {
        this.ID = ID;
        this.czasUtworzeniaKlienta = czasUtworzeniaKlienta;
        this.iloscGotowki = iloscGotowki;
        this.iloscTowarow = iloscTowarow;
        this.czasZakoczeniaZakupow = czasZakoczeniaZakupow;
//        this.czasObslugi = iloscTowarow * 500.0;
        this.czasObslugi = 600.0;
    }

    public Klient(int ID, double czasUtworzeniaKlienta, double czasZakoczeniaZakupow, int iloscTowarow, double iloscGotowki, boolean czyVIP)
    {
        this.ID = ID;
        this.czasUtworzeniaKlienta = czasUtworzeniaKlienta;
        this.iloscGotowki = iloscGotowki;
        this.iloscTowarow = iloscTowarow;
        this.czasZakoczeniaZakupow = czasZakoczeniaZakupow;
        //        this.czasObslugi = iloscTowarow * 500.0;
        this.czasObslugi = 600.0;
        this.czyVIP = czyVIP;
    }

    public void czySkonczylRobicZakupy(double federateTime)
    {
        if(federateTime >= czasZakoczeniaZakupow)
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
