package federaci;


import ambasador.GUIAmbassador;
import hla.rti.LogicalTime;
import hla.rti.RTIexception;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RtiFactoryFactory;
import model.Dane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FederatGUI extends AbstractFederat
{
    public static final String federateName = "FederatGUI";
    //GUI
    private JFrame frame;
    private JButton start;
    private JButton stop;
    private JButton nowyKlientZwykly;
    private JButton nowyVIP;
    private JButton nowaKasa;
    private JTextArea textArea;
    private JScrollPane scrollPane;

    private void createWindow() {
        frame = new JFrame("I6B2S4 Joanna Bednarko i Joanna_Koszela - " + federateName);
        JPanel panel = new JPanel();

        nowyKlientZwykly = new JButton("Zwykly");
        nowyKlientZwykly.setEnabled(false);
        nowyKlientZwykly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlienta(true);
                //log("32. button setCzyTworzycKlienta = " + fedamb.getCzyTworzycKlienta());
                fedamb.setCzyTworzycVIP(false);
                //log("33. button setCzyTworzycVIP = " + fedamb.getCzyTworzycVIP());
            }
        });
        panel.add(nowyKlientZwykly);
        nowyKlientZwykly.setSize(100, 30);
        nowyKlientZwykly.setLocation(350, 20);


        nowyVIP = new JButton("VIP");
        nowyVIP.setEnabled(false);
        nowyVIP.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlienta(false);
//                log("34. button setCzyTworzycKlienta = " + fedamb.getCzyTworzycKlienta());
                fedamb.setCzyTworzycVIP(true);
//                log("35. button setCzyTworzycVIP = " + fedamb.getCzyTworzycVIP());
            }
        });
        panel.add(nowyVIP);
        nowyVIP.setSize(100, 30);
        nowyVIP.setLocation(500, 20);


        nowaKasa = new JButton("Nowa Kasa");
        nowaKasa.setEnabled(false);
        nowaKasa.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //log("35. button setCzyTworzycKase = " + fedamb.getCzyTworzycKase());
                fedamb.setCzyTworzycKase(true);
                //log("36. button setCzyTworzycKase = " + fedamb.getCzyTworzycKase());
            }
        });
        panel.add(nowaKasa);
        nowaKasa.setSize(100, 30);
        nowaKasa.setLocation(650, 20);

        start = new JButton("Start");
        start.setEnabled(true);
        start.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyStartSymulacji(true);
                nowyKlientZwykly.setEnabled(true);
                nowyVIP.setEnabled(true);
                nowaKasa.setEnabled(true);
                start.setEnabled(false);
                stop.setEnabled(true);
                //log("30. button setCzyStartSymulacji = " + fedamb.getCzyStartSymulacji());
            }
        });
        panel.add(start);
        start.setSize(100, 30);
        start.setLocation(50, 20);


        stop = new JButton("Stop");
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyStopSymulacji(true);
                nowyKlientZwykly.setEnabled(false);
                nowyVIP.setEnabled(false);
                nowaKasa.setEnabled(false);
                start.setEnabled(true);
                stop.setEnabled(false);
                //log("31. button setCzyStopSymulacji = " + fedamb.getCzyStopSymulacji());
            }
        });
        panel.add(stop);
        stop.setSize(100, 30);
        stop.setLocation(200, 20);

        textArea = new JTextArea();
        textArea.setEnabled(true);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);
        scrollPane.setBounds(30, 65, 475, 180);
        panel.add(scrollPane);
        scrollPane.setSize(700, 400);
        scrollPane.setLocation(50, 100);
        panel.add(scrollPane);


        frame.add(panel);
        frame.setContentPane(panel);
        panel.setLayout(null);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
    }

    public static void main(String[] args)
    {
        try
        {
            new FederatGUI().runFederate();
            log("Wystartowal " + federateName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
        createWindow();
        fedamb = new GUIAmbassador();
        createFederation();

        joinFederation(federateName);
        registerSyncPoint();
        waitForUser();
        achieveSyncPoint();
        enableTimePolicy();
        publishAndSubscribe();
        run();
    }

    public void run() throws RTIexception
    {
        while (fedamb.running)
        {
            if(fedamb.getCzyStartSymulacji())
            {
                //log("20. getCzyStartSymulacji = " + fedamb.getCzyStartSymulacji());
                fedamb.setCzyStartSymulacji(false);
                //log("21. getCzyStartSymulacji = " + fedamb.getCzyStartSymulacji());
                sendStartInteraction();
            }
            if(fedamb.getCzyStopSymulacji())
            {
                //log("22. getCzyStopSymulacji = " + fedamb.getCzyStopSymulacji());
                fedamb.setCzyStopSymulacji(false);
                //log("23. getCzyStopSymulacji = " + fedamb.getCzyStopSymulacji());
                sendStopInteraction();
            }
            if(fedamb.getCzyTworzycKlienta())
            {
                //log("24. getCzyTworzycKlienta = " + fedamb.getCzyTworzycKlienta());
                fedamb.setCzyTworzycKlienta(false);
                //log("25. getCzyTworzycKlienta = " + fedamb.getCzyTworzycKlienta());
                sendNowyKlientInteraction(generateAndAddKlient());
            }
            if(fedamb.getCzyTworzycVIP())
            {
                //log("26. getCzyTworzycVIP = " + fedamb.getCzyTworzycVIP());
                fedamb.setCzyTworzycVIP(false);
                //log("27. getCzyTworzycVIP = " + fedamb.getCzyTworzycVIP());
                sendNowyKlientInteraction(generateAndAddKlientVIP());
            }
            if(fedamb.getCzyTworzycKase())
            {
                //log("28. getCzyTworzycKase = " + fedamb.getCzyTworzycKase());
                fedamb.setCzyTworzycKase(false);
                //log("29. getCzyTworzycKase = " + fedamb.getCzyTworzycKase());
                sendNowaKasaInteraction(generateAndAddKasa());
            }
            advanceTime(timeStep);
        }
        disableTimePolicy();
    }

    private void sendStartInteraction() throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        fedamb.startSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_START_SYMULACJI);
        rtiamb.sendInteraction(fedamb.startSymulacjiHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log("sendStartInteraction");
    }

    private void sendStopInteraction() throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        fedamb.stopSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_STOP_SYMULACJI);
        rtiamb.sendInteraction(fedamb.stopSymulacjiHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log("sendStopInteraction");
    }

    private void publishAndSubscribe()
    {
        try
        {
            publishStartSymulacji();
            publishStopSymulacji();
            publishKlient();
            publishKasa();
        }
        catch(RTIexception e)
        {
            e.printStackTrace();
        }
    }
}
