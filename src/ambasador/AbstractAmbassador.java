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
        String message = "12. Interaction received handle = " + interactionClass + ", tag " + EncodingHelpers.decodeString(tag) + " ";
        if (theTime != null)
        {
            message += ", time=" + convertTime(theTime);
        }
        log(message);

        if (interactionClass == startSymulacjiHandle)
        {
//            log("receiveInteraction setCzyStartSymulacji " + getCzyStartSymulacji());
            setCzyStartSymulacji(true);
//            log("receiveInteraction setCzyStartSymulacji " + getCzyStartSymulacji());
        }

        if (interactionClass == stopSymulacjiHandle)
        {
//            log("receiveInteraction setCzyStopSymulacji " + getCzyStopSymulacji());
            setCzyStopSymulacji(true);
//            log("receiveInteraction setCzyStopSymulacji " + getCzyStopSymulacji());
        }

        if (interactionClass == federatKlientInteractionHandle)
        {
            log("11. Interaction federatKlientInteractionHandle received");
            for (int i = 0; i < theInteraction.size(); i++)
            {
                try
                {
                    byte[] value = theInteraction.getValue(i);
                    if (theInteraction.getParameterHandle(i) == federatKlientIDAttributeHandle)
                    {
//                        log("federatKlientIDAttributeValue = " + federatKlientIDAttributeValue);
                        federatKlientIDAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientCzasUtworzeniaAttributeHandle)
                    {
                        federatKlientCzasUtworzeniaAttributeValue = EncodingHelpers.decodeDouble(value);
//                        log("federatKlientCzasUtworzeniaAttributeValue = " + federatKlientCzasUtworzeniaAttributeValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientCzasZakonczeniaZakupowHandle)
                    {
                        federatKlientCzasZakonczeniaZakupowValue = EncodingHelpers.decodeDouble(value);
//                        log("federatKlientCzasZakonczeniaZakupowValue = " + federatKlientCzasZakonczeniaZakupowValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientIloscGotowkiAttributeHandle)
                    {
                        federatKlientIloscGotowkiAttributeValue = EncodingHelpers.decodeInt(value);
//                        log("federatKlientIloscGotowkiAttributeValue = " + federatKlientIloscGotowkiAttributeValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientIloscTowarowAttributeHandle)
                    {
                        federatKlientIloscTowarowAttributeValue = EncodingHelpers.decodeInt(value);
//                        log("federatKlientIloscTowarowAttributeValue = " + federatKlientIloscTowarowAttributeValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKlientCzyVIPAttributeHandle)
                    {
                        federatKlientCzyVIPAttributeValue = EncodingHelpers.decodeBoolean(value);
//                        log("federatKlientCzyVIPAttributeValue = " + federatKlientCzyVIPAttributeValue);
                    }
                }
                catch (Exception e)
                {
                    log(e.getMessage());
                }
            }
            if (federatKlientCzyVIPAttributeValue)
            {
//                log("1. receiveInteraction get czyTworzycKlienta = " + getCzyTworzycKlienta());
//                log("2. receiveInteraction get czyTworzycVIP = " + getCzyTworzycVIP());
                setCzyTworzycKlienta(false);
                setCzyTworzycVIP(true);
//                log("3. receiveInteraction set czyTworzycKlienta = " + getCzyTworzycKlienta());
//                log("4. receiveInteraction set czyTworzycVIP = " + getCzyTworzycVIP());
            }
            else
            {
//                log("5. receiveInteraction get czyTworzycKlienta = " + getCzyTworzycKlienta());
//                log("6. receiveInteraction get czyTworzycVIP = " + getCzyTworzycVIP());
                setCzyTworzycKlienta(true);
                setCzyTworzycVIP(false);
//                log("7. receiveInteraction set czyTworzycKlienta = " + getCzyTworzycKlienta());
//                log("8. receiveInteraction set czyTworzycVIP = " + getCzyTworzycVIP());
            }
        }

        if (interactionClass == federatKasaInteractionHandle)
        {
            log("19. Interaction federatKlientInteractionHandle received");
            for (int i = 0; i < theInteraction.size(); i++)
            {
                try
                {
                    byte[] value = theInteraction.getValue(i);
                    if (theInteraction.getParameterHandle(i) == federatKasaIDAttributeHandle)
                    {
                        federatKasaIDAttributeValue = EncodingHelpers.decodeInt(value);
//                        log("federatKasaIDAttributeValue = " + federatKasaIDAttributeValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKasaLiczbaKlientowWKolejceAttributeHandle)
                    {
                        federatKasaLiczbaKlientowWKolejceAttributeValue = EncodingHelpers.decodeInt(value);
//                        log("federatKasaLiczbaKlientowWKolejceAttributeValue = " + federatKasaLiczbaKlientowWKolejceAttributeValue);
                    }
                    if (theInteraction.getParameterHandle(i) == federatKasaCzyPrzepelnionaAttributeHandle)
                    {
                        federatKasaCzyPrzepelnionaAttributeValue = EncodingHelpers.decodeBoolean(value);
//                        log("federatKasaCzyPrzepelnionaAttributeValue = " + federatKasaCzyPrzepelnionaAttributeValue);
                    }

                }
                catch (Exception e)
                {
                    log(e.getMessage());
                }
//                log("9. receiveInteraction get czyTworzycKase = " + getCzyTworzycKase());
                setCzyTworzycKase(true);
//                log("10. receiveInteraction set czyTworzycKase = " + getCzyTworzycKase());
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
