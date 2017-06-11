package ambasador;

import federaci.AbstractFederat;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla1516.types.DoubleTime;

import java.util.Random;


public class Ambasador extends NullFederateAmbassador
{
    public static final String FEDERATION_NAME = "ShopFederation";
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
    public boolean czyTworzycKlienta = false;
    public boolean czyTworzycKase = false;
    public boolean czyAktualizowacKase = false;
    public int IDAktualizowanejKasy = -1;
    public int dlugoscKolejki = -1;
    public boolean czyPrzepelniona = false;

    public boolean czyAktulizowacCzasWejsciaDoKolejki = false;
    public int IDKlientWKolejce = -1;
    public double czasWejsciaDoKoljeki;

    public AbstractFederat federat;


    //----------------Klasy------------------
    //Klient
    public int federatKlientClassHandle;

    public int federatKlientIDAttributeHandle;
    public int federatKlientIloscTowarowAttributeHandle;
    public int federatKlientCzyVIPAttributeHandle;
    public int federatKlientNrKasyAttributeHandle;
    public int federatKlientPozycjaWKolejceAttributeHandle;

    //Kasa
    public int federatKasaClassHandle;

    public int federatKasaIDAttributeHandle;
    public int federatKasaLiczbaKlientowWKolejceAttributeHandle;
    public int federatKasaCzyPrzepelnionaAttributeHandle;

    //------------Interakcje----------------------------
    public int wejscieDoKolejkiInteractionHandle;
    public int IDKlientWejscieDoKolejkiInteractionAttributeHandle;
    public int IDKlientWejscieDoKolejkiInteractionAttributeValue;
    public int IDKasaWejscieDoKolejkiInteractionAttributeHandle;
    public int IDKasaWejscieDoKolejkiInteractionAttributeValue;
    public double czasWejsciaDoKolejki;

    public int rozpoczecieObslugiInteractionHandle;
    public int IDKlientRozpoczecieObslugiHandle;
    public int IDKlientRozpoczecieObslugiValue;
    public double czasRozpoczeciaObslugi;

    public int zakonczenieObslugiInteractionHandle;
    public int IDKlientZakonczenieObslugiHandle;
    public int IDKlientZakonczenieObslugiValue;
    public double czasZakonczeniaObslugi;

    public int otworzKaseInteractionHandle;

    public Ambasador(AbstractFederat federat)
    {
        this.federat = federat;
    }

    private double convertTime(LogicalTime logicalTime)
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

    private void log(String message)
    {
        System.out.println("FederateAmbassador: " + message);
    }

    public void synchronizationPointRegistrationFailed(String label)
    {
        log("Failed to register sync point: " + label);
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

    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName)
    {
        log("New object of class " + theObjectClass + " created " + theObject + " " + objectName);
        if(theObject == federatKlientClassHandle)
        {
            czyTworzycKlienta = true;
        }

        if(theObjectClass == federatKasaClassHandle)
        {
            czyTworzycKase = true;
        }
    }

    public void receiveInteraction(int interactionClass, ReceivedInteraction theInteraction, byte[] tag)
    {
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction(int interactionClass, ReceivedInteraction theInteraction, byte[] tag, LogicalTime theTime, EventRetractionHandle eventRetractionHandle)
    {
        String message = "Interaction received handle= " + interactionClass + ", tag " + EncodingHelpers.decodeString(tag) + " ";
        if( theTime != null )
        {
            message += ", time=" + convertTime(theTime);
        }
        log(message);

        if(interactionClass == wejscieDoKolejkiInteractionHandle)
        {
            for(int i = 0; i < theInteraction.size(); i++)
            {
                try
                {
                    byte[] value = theInteraction.getValue(i);
                    if(theInteraction.getParameterHandle(i) == IDKlientWejscieDoKolejkiInteractionAttributeHandle)
                    {
                        IDKlientWejscieDoKolejkiInteractionAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                    if(theInteraction.getParameterHandle(i) == IDKasaWejscieDoKolejkiInteractionAttributeHandle)
                    {
                        IDKasaWejscieDoKolejkiInteractionAttributeValue = EncodingHelpers.decodeInt(value);
                    }
                }
                catch(Exception e)
                {
                    log(e.getMessage());
                }
            }
            czasWejsciaDoKolejki = convertTime(theTime);
            federat.aktualizujCzasWejsciaDoKolejki(IDKlientWejscieDoKolejkiInteractionAttributeValue, czasWejsciaDoKolejki);
        }
        if(interactionClass == rozpoczecieObslugiInteractionHandle)
        {
            try
            {
                byte[] value = theInteraction.getValue(0);
                IDKlientRozpoczecieObslugiValue = EncodingHelpers.decodeInt(value);
                czasRozpoczeciaObslugi = convertTime(theTime);
                federat.aktualizujCzasRozpoczeciaObslugi(IDKlientZakonczenieObslugiValue, czasRozpoczeciaObslugi);
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        if(interactionClass == zakonczenieObslugiInteractionHandle)
        {
            try
            {
                byte[] value = theInteraction.getValue(0);
                IDKlientZakonczenieObslugiValue = EncodingHelpers.decodeInt(value);
                czasZakonczeniaObslugi = convertTime(theTime);
                federat.aktualizujCzasZakonczeniaObslugi(IDKlientZakonczenieObslugiValue, czasZakonczeniaObslugi);
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
        if(interactionClass == otworzKaseInteractionHandle)
        {
            czyTworzycKase = true;
        }
    }

    private void sendInteraction(double timeStep) throws RTIexception
    {
        //TODO
        //SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        //Random random = new Random();
        //byte[] quantity = EncodingHelpers.encodeInt(random.nextInt(10) + 1);

        //int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.AddProduct");
        //int quantityHandle = rtiamb.getParameterHandle( "quantity", interactionHandle );

        //parameters.add(quantityHandle, quantity);

        //LogicalTime time = convertTime( timeStep );
        //rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
    }

    public void reflectAttributeValues(int theObject, ReflectedAttributes theAttributes, byte[] tag)
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        reflectAttributeValues(theObject, theAttributes, tag, null, null);
    }

    public void reflectAttributeValues(int theObject, ReflectedAttributes theAttributes, byte[] tag, LogicalTime theTime, EventRetractionHandle retractionHandle)
    {
        if(theObject == federatKasaClassHandle)
        {
            this.IDAktualizowanejKasy = theObject;
            this.czyAktualizowacKase = true;
            for (int i = 0; i < theAttributes.size(); i++)
            {
                try
                {
                    byte[] value = theAttributes.getValue(i);
                    if (theAttributes.getAttributeHandle(i) == federatKasaLiczbaKlientowWKolejceAttributeHandle)
                    {
                        dlugoscKolejki = EncodingHelpers.decodeInt(value);
                    }
                    if (theAttributes.getAttributeHandle(i) == federatKasaCzyPrzepelnionaAttributeHandle)
                    {
                        czyPrzepelniona = EncodingHelpers.decodeBoolean(value);
                    }
                }
                catch (Exception e)
                {
                    log(e.getMessage());
                }
            }
            federat.aktualizacjaKasy(IDAktualizowanejKasy, dlugoscKolejki, czyPrzepelniona);
        }

        if(theObject == federatKlientClassHandle)
        {
            //TODO
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
}
