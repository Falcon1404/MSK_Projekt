package ambasador;

import federaci.AbstractFederat;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RtiFactoryFactory;
import model.Dane;
import org.portico.impl.hla13.types.DoubleTime;


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

    public int IDKlientWKolejce = -1;
    public int NrKasyKlientaWKolejce = -1;
    public double czasWejsciaDoKoljeki;

    //------------Interakcje----------------------------
    //Klient
    public int federatKlientInteractionHandle;

    public int federatKlientIDAttributeHandle;
    public int federatKlientIDAttributeValue;

    public int federatKlientCzasUtworzeniaAttributeHandle;
    public double federatKlientCzasUtworzeniaAttributeValue;

    public int federatKlientCzasZakonczeniaZakupowHandle;
    public double federatKlientCzasZakonczeniaZakupowValue;

    public int federatKlientIloscGotowkiAttributeHandle;
    public int federatKlientIloscGotowkiAttributeValue;

    public int federatKlientIloscTowarowAttributeHandle;
    public int federatKlientIloscTowarowAttributeValue;

    public int federatKlientCzyVIPAttributeHandle;
    public boolean federatKlientCzyVIPAttributeValue;

    //Kasa
    public int federatKasaInteractionHandle;

    public int federatKasaIDAttributeHandle;
    public int federatKasaIDAttributeValue;

    public int federatKasaLiczbaKlientowWKolejceAttributeHandle;
    public int federatKasaLiczbaKlientowWKolejceAttributeValue;

    public int federatKasaCzyPrzepelnionaAttributeHandle;
    public boolean federatKasaCzyPrzepelnionaAttributeValue;

    //Wejście do kolejki
    public int wejscieDoKolejkiInteractionHandle;
    public int IDKlientWejscieDoKolejkiInteractionAttributeHandle;
    public int IDKlientWejscieDoKolejkiInteractionAttributeValue;
    public int IDKasaWejscieDoKolejkiInteractionAttributeHandle;
    public int IDKasaWejscieDoKolejkiInteractionAttributeValue;
    public double czasWejsciaDoKolejki;

    //Rozpoczęcie obsługi
    public int rozpoczecieObslugiInteractionHandle;
    public int IDKlientRozpoczecieObslugiHandle;
    public int IDKlientRozpoczecieObslugiValue;
    public double czasRozpoczeciaObslugi;

    //Zakończenie obsługi
    public int zakonczenieObslugiInteractionHandle;
    public int IDKlientZakonczenieObslugiHandle;
    public int IDKlientZakonczenieObslugiValue;
    public double czasZakonczeniaObslugi;

    //Otwórz Kasę
    public int otworzKaseInteractionHandle;
    public boolean czyJakasKasaJestPrzepelniona = false; //dla menago

    //Początek symulacji
    public int startSymulacjiHandle;
    private boolean czyStartSymulacji = false;

    //Koniec symulacji
    public int stopSymulacjiHandle;
    private boolean czyStopSymulacji = false;


    public AbstractAmbassador()
    {
    }

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

//    public void receiveInteraction(int interactionClass, ReceivedInteraction theInteraction, byte[] tag)
//    {
//        receiveInteraction(interactionClass, theInteraction, tag, null, null);
//    }

    public void receiveInteraction(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle)
    {
        String message = "Interaction received handle= " + interactionClass + ", tag " + EncodingHelpers.decodeString(tag) + " ";
        if (theTime != null)
        {
            message += ", time=" + convertTime(theTime);
        }
        log(message);

        if (interactionClass == startSymulacjiHandle)
        {
            setCzyStartSymulacji(true);
        }

        if (interactionClass == stopSymulacjiHandle)
        {
            setCzyStopSymulacji(true);
        }

        if (interactionClass == federatKlientInteractionHandle)
        {
            log("Interaction federatKlientInteractionHandle received");
            for (int i = 0; i < theInteraction.size(); i++)
            {
                try
                {
                    byte[] value = theInteraction.getValue(i);
                    if (theInteraction.getParameterHandle(i) == federatKlientIDAttributeHandle)
                    {
                        federatKlientIDAttributeValue = EncodingHelpers.decodeInt(value);
                        log("Interaction received ID Value " + federatKlientIDAttributeValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientCzasUtworzeniaAttributeHandle)
                    {
                        federatKlientCzasUtworzeniaAttributeValue = EncodingHelpers.decodeDouble(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientCzasZakonczeniaZakupowHandle)
                    {
                        federatKlientCzasZakonczeniaZakupowValue = EncodingHelpers.decodeDouble(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientIloscGotowkiAttributeHandle)
                    {
                        federatKlientIloscGotowkiAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientIloscTowarowAttributeHandle)
                    {
                        federatKlientIloscTowarowAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientCzyVIPAttributeHandle)
                    {
                        federatKlientCzyVIPAttributeValue = EncodingHelpers.decodeBoolean(value);
                    }
                }
                catch (Exception e)
                {
                    log(e.getMessage());
                }
            }
            if (federatKlientCzyVIPAttributeValue)
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

        if (interactionClass == federatKasaInteractionHandle)
        {
            for (int i = 0; i < theInteraction.size(); i++)
            {
                try
                {
                    byte[] value = theInteraction.getValue(i);
                    if (theInteraction.getParameterHandle(i) == federatKasaIDAttributeHandle)
                    {
                        federatKasaIDAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKasaLiczbaKlientowWKolejceAttributeHandle)
                    {
                        federatKasaLiczbaKlientowWKolejceAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKasaCzyPrzepelnionaAttributeHandle)
                    {
                        federatKasaCzyPrzepelnionaAttributeValue = EncodingHelpers.decodeBoolean(value);
                    }

                }
                catch (Exception e)
                {
                    log(e.getMessage());
                }
                setCzyTworzycKase(true);
            }
        }
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
}
