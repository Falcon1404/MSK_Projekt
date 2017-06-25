package ambasador;

import federaci.AbstractFederat;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import model.MyInteraction;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;


public abstract class AbstractAmbassador extends NullFederateAmbassador
{
    public static final String FEDERATION_NAME = "ShopFederation";
    public static final String AMBASSADOR_NAME = "AbstractAmbsassor";
    public static final String READY_TO_RUN = "ReadyToRun";
    public double federateTime = 0.0;
    public double grantedTime = 0.0;
    public double federateLookahead = 1.0;

    public boolean isRegulating = false;
    public boolean isConstrained = false;
    public boolean isAdvancing = false;

    public boolean isAnnounced = false;
    public boolean isReadyToRun = false;

    public boolean running = true;

    private boolean czyTworzycKlienta = false;
    private boolean czyTworzycVIP = false;
    private boolean czyTworzycKase = false;
    public boolean czyAktualizowacKase = false;
    public int IDAktualizowanejKasy = -1;
    public int dlugoscKolejki = -1;
    public boolean czyPrzepelniona = false;

    public ArrayList<MyInteraction> listaInterakcji = new ArrayList<>();

    //------------Interakcje----------------------------
    //Klient
    public int nowyKlientInteractionHandle;

    public int klientIDAttributeHandle;
    public int klientIDAttributeValue;

    public int klientCzasUtworzeniaAttributeHandle;
    public double klientCzasUtworzeniaAttributeValue;

    public int klientCzasZakonczeniaZakupowHandle;
    public double klientCzasZakonczeniaZakupowValue;

    public int klientIloscGotowkiAttributeHandle;
    public int klientIloscGotowkiAttributeValue;

    public int klientIloscTowarowAttributeHandle;
    public int klientIloscTowarowAttributeValue;

    public int klientCzyVIPAttributeHandle;
    public boolean klientCzyVIPAttributeValue;

    //Kasa
    public int kasaInteractionHandle;

    public int kasaIDAttributeHandle;
    public int kasaIDAttributeValue;

    public int kasaLiczbaKlientowWKolejceAttributeHandle;
    public int kasaLiczbaKlientowWKolejceAttributeValue;

    public int kasaCzyPrzepelnionaAttributeHandle;
    public boolean kasaCzyPrzepelnionaAttributeValue;

    //Wejście do kolejki
    public int wejscieDoKolejkiInteractionHandle;
    public int IDKlientWejscieDoKolejkiInteractionAttributeHandle;
    public int IDKlientWejscieDoKolejkiInteractionAttributeValue;
    public int IDKasaWejscieDoKolejkiInteractionAttributeHandle;
    public int IDKasaWejscieDoKolejkiInteractionAttributeValue;
    private boolean czyKlientWszedlDoKolejki = false;

    //Rozpoczęcie obsługi
    public int rozpoczecieObslugiInteractionHandle;
    public int IDKlientRozpoczecieObslugiHandle;
    public int IDKlientRozpoczecieObslugiValue;
    public int IDKasaRozpoczecieObslugiHandle;
    public int IDKasaRozpoczecieObslugiValue;
    private boolean czyKlientJestObslugiwany = false;

    //Zakończenie obsługi
    public int zakonczenieObslugiInteractionHandle;
    public int IDKlientZakonczenieObslugiHandle;
    public int IDKlientZakonczenieObslugiValue;
    public int IDKasaZakonczenieObslugiHandle;
    public int IDKasaZakonczenieObslugiValue;
    private boolean czyKlientZostalObsluzony = false;

    //Otwórz Kasę
    public int otworzKaseInteractionHandle;
    private boolean czyOtworzycKase = false;

    //Początek symulacji
    public int startSymulacjiHandle;
    private boolean czyStartSymulacji = false;

    //Koniec symulacji
    public int stopSymulacjiHandle;
    private boolean czyStopSymulacji = false;

    //Sredni czas zakupow
    public int sredniCzasZakupowHandle;
    public int czasZakupowHandle;
    public int czasZakupowValue;
    private boolean czySredniCzasZakupow = false;

    //Sredni czas obslugi
    public int sredniCzasObslugiHandle;
    public int czasObslugiHandle;
    public int czasObslugiValue;
    private boolean czySredniCzasObslugi = false;

    //Sredni czas w kolejce
    public int sredniCzasWKolejceHandle;
    public int czasWKolejceHandle;
    public int czasWKolejceValue;
    private boolean czySredniCzasWKolejce = false;

    public AbstractAmbassador() {}

    public double convertTime(LogicalTime logicalTime)
    {
        return ((DoubleTime)logicalTime).getTime();
    }

    public double getFederateTime()
    {
        return federateTime;
    }

    public double getFederateLookahead()
    {
        return federateLookahead;
    }

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }

    public void synchronizationPointRegistrationFailed(String label)
    {
        log("Failed to register sync point: " + label);
    }

    public byte[] generateTag()
    {
        return ("" + System.currentTimeMillis()).getBytes();
    }

    public void synchronizationPointRegistrationSucceeded(String label)
    {
        log("Successfully registered sync point: " + label);
    }

    public void announceSynchronizationPoint(String label, byte[] tag)
    {
        log("Synchronization point announced: " + label);
        if(label.equals(AbstractFederat.READY_TO_RUN))
        {
            this.isAnnounced = true;
        }
    }

    public void federationSynchronized(String label)
    {
        log("Federation Synchronized: " + label);
        if(label.equals(AbstractFederat.READY_TO_RUN))
        {
            this.isReadyToRun = true;
        }
    }

    public void timeRegulationEnabled(LogicalTime theFederateTime)
    {
        this.federateTime = convertTime(theFederateTime);
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled(LogicalTime theFederateTime)
    {
        this.federateTime = convertTime(theFederateTime);
        this.isConstrained = true;
    }

    public void timeAdvanceGrant(LogicalTime theTime)
    {
        this.federateTime = convertTime(theTime);
        this.isAdvancing = false;
    }

    public void obsluzStartSymulacji(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        setCzyStartSymulacji(true);
    }

    public void obsluzStopSymulacji(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        setCzyStopSymulacji(true);
    }

    public void obsluzNowyKlientInteractionHandle(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == klientIDAttributeHandle)
                {
                    klientIDAttributeValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == klientCzasUtworzeniaAttributeHandle)
                {
                    klientCzasUtworzeniaAttributeValue = EncodingHelpers.decodeDouble(value);
                }
                if (theInteraction.getParameterHandle(i) == klientCzasZakonczeniaZakupowHandle)
                {
                    klientCzasZakonczeniaZakupowValue = EncodingHelpers.decodeDouble(value);
                }
                if (theInteraction.getParameterHandle(i) == klientIloscGotowkiAttributeHandle)
                {
                    klientIloscGotowkiAttributeValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == klientIloscTowarowAttributeHandle)
                {
                    klientIloscTowarowAttributeValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == klientCzyVIPAttributeHandle)
                {
                    klientCzyVIPAttributeValue = EncodingHelpers.decodeBoolean(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }

            if (klientCzyVIPAttributeValue)
            {
                setCzyTworzycKlienta(false);
                setCzyTworzycVIP(true);
            }
            else
            {
                setCzyTworzycKlienta(true);
                setCzyTworzycVIP(false);
            }
        }
    }

    public void obsluzNowaKasa(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == kasaIDAttributeHandle)
                {
                    kasaIDAttributeValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == kasaLiczbaKlientowWKolejceAttributeHandle)
                {
                    kasaLiczbaKlientowWKolejceAttributeValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == kasaCzyPrzepelnionaAttributeHandle)
                {
                    kasaCzyPrzepelnionaAttributeValue = EncodingHelpers.decodeBoolean(value);
                }

            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
            setCzyTworzycKase(true);
        }
    }

    public void obsluzOtoworzKase(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        setCzyOtworzycKase(true);
    }

    public void obsluzWejscieDoKolejki(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == IDKlientWejscieDoKolejkiInteractionAttributeHandle)
                {
                    IDKlientWejscieDoKolejkiInteractionAttributeValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == IDKasaWejscieDoKolejkiInteractionAttributeHandle)
                {
                    IDKasaWejscieDoKolejkiInteractionAttributeValue = EncodingHelpers.decodeInt(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        setCzyKlientWszedlDoKolejki(true);
    }

    public void obsluzRozpoczecieObslugi(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == IDKlientRozpoczecieObslugiHandle)
                {
                    IDKlientRozpoczecieObslugiValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == IDKasaRozpoczecieObslugiHandle)
                {
                    IDKasaRozpoczecieObslugiValue = EncodingHelpers.decodeInt(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        setCzyKlientJestObslugiwany(true);
    }

    public void obsluzZakonczenieObslugi(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == IDKlientZakonczenieObslugiHandle)
                {
                    IDKlientZakonczenieObslugiValue = EncodingHelpers.decodeInt(value);
                }
                if (theInteraction.getParameterHandle(i) == IDKasaZakonczenieObslugiHandle)
                {
                    IDKasaZakonczenieObslugiValue = EncodingHelpers.decodeInt(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        setCzyKlientZostalObsluzony(true);
    }

    public void receiveInteraction(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle)
    {
        listaInterakcji.add(new MyInteraction(interactionClass, theInteraction, theTime));
    }

    public void obsluzSredniCzasZakupow(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == czasZakupowHandle)
                {
                    czasZakupowValue = EncodingHelpers.decodeInt(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        setCzySredniCzasZakupow(true);
    }

    public void obsluzSredniCzasWKolejce(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == czasWKolejceHandle)
                {
                    czasWKolejceValue = EncodingHelpers.decodeInt(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        setCzySredniCzasWKolejce(true);
    }

    public void obsluzSredniCzasObslugi(ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        for (int i = 0; i < theInteraction.size(); i++)
        {
            try
            {
                byte[] value = theInteraction.getValue(i);
                if (theInteraction.getParameterHandle(i) == czasObslugiHandle)
                {
                    czasObslugiValue = EncodingHelpers.decodeInt(value);
                }
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        setCzySredniCzasObslugi(true);
    }

    public boolean getCzyTworzycKlienta()
    {
        return czyTworzycKlienta;
    }

    public boolean getCzyTworzycVIP()
    {
        return czyTworzycVIP;
    }

    public boolean getCzyTworzycKase()
    {
        return czyTworzycKase;
    }

    public void setCzyTworzycKlienta(boolean czyTworzycKlienta)
    {
        this.czyTworzycKlienta = czyTworzycKlienta;
    }

    public void setCzyTworzycVIP(boolean czyTworzycVIP)
    {
        this.czyTworzycVIP = czyTworzycVIP;
    }

    public void setCzyTworzycKase(boolean czyTworzycKase)
    {
        this.czyTworzycKase = czyTworzycKase;
    }

    public boolean getCzyStartSymulacji()
    {
        return czyStartSymulacji;
    }

    public void setCzyStartSymulacji(boolean czyStartSymulacji)
    {
        this.czyStartSymulacji = czyStartSymulacji;
    }

    public boolean getCzyStopSymulacji()
    {
        return czyStopSymulacji;
    }

    public void setCzyStopSymulacji(boolean czyStopSymulacji)
    {
        this.czyStopSymulacji = czyStopSymulacji;
    }

    public boolean getCzyKlientWszedlDoKolejki()
    {
        return czyKlientWszedlDoKolejki;
    }

    public void setCzyKlientWszedlDoKolejki(boolean czyKlientWszedlDoKolejki)
    {
        this.czyKlientWszedlDoKolejki = czyKlientWszedlDoKolejki;
    }

    public boolean getCzyKlientJestObslugiwany()
    {
        return czyKlientJestObslugiwany;
    }

    public void setCzyKlientJestObslugiwany(boolean czyKlientJestObslugiwany)
    {
        this.czyKlientJestObslugiwany = czyKlientJestObslugiwany;
    }

    public boolean getCzyKlientZostalObsluzony()
    {
        return czyKlientZostalObsluzony;
    }

    public void setCzyKlientZostalObsluzony(boolean czyKlientZostalObsluzony)
    {
        this.czyKlientZostalObsluzony = czyKlientZostalObsluzony;
    }

    public boolean getCzyOtworzycKase()
    {
        return czyOtworzycKase;
    }

    public void setCzyOtworzycKase(boolean czyOtworzycKase)
    {
        this.czyOtworzycKase = czyOtworzycKase;
    }

    public boolean getCzySredniCzasZakupow()
    {
        return czySredniCzasZakupow;
    }

    public void setCzySredniCzasZakupow(boolean czySredniCzasZakupow)
    {
        this.czySredniCzasZakupow = czySredniCzasZakupow;
    }

    public boolean getCzySredniCzasObslugi()
    {
        return czySredniCzasObslugi;
    }

    public void setCzySredniCzasObslugi(boolean czySredniCzasObslugi)
    {
        this.czySredniCzasObslugi = czySredniCzasObslugi;
    }

    public boolean getCzySredniCzasWKolejce()
    {
        return czySredniCzasWKolejce;
    }

    public void setCzySredniCzasWKolejce(boolean czySredniCzasWKolejce)
    {
        this.czySredniCzasWKolejce = czySredniCzasWKolejce;
    }
}
