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

        start = new JButton("Start");
        start.setEnabled(true);
        start.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyStartSymulacji(true);
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
            }
        });
        panel.add(stop);
        stop.setSize(100, 30);
        stop.setLocation(200, 20);

        nowyKlientZwykly = new JButton("Zwykly");
        nowyKlientZwykly.setEnabled(true);
        nowyKlientZwykly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlienta(true);
                fedamb.setCzyTworzycVIP(false);
            }
        });
        panel.add(nowyKlientZwykly);
        nowyKlientZwykly.setSize(100, 30);
        nowyKlientZwykly.setLocation(350, 20);


        nowyVIP = new JButton("VIP");
        nowyVIP.setEnabled(true);
        nowyVIP.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlienta(false);
                fedamb.setCzyTworzycVIP(true);
            }
        });
        panel.add(nowyVIP);
        nowyVIP.setSize(100, 30);
        nowyVIP.setLocation(500, 20);


        nowaKasa = new JButton("Nowa Kasa");
        nowaKasa.setEnabled(true);
        nowaKasa.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKase(true);
            }
        });
        panel.add(nowaKasa);
        nowaKasa.setSize(100, 30);
        nowaKasa.setLocation(650, 20);


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
                fedamb.setCzyStartSymulacji(false);
                sendStartInteraction();
            }
            if(fedamb.getCzyStopSymulacji())
            {
                fedamb.setCzyStopSymulacji(false);
                sendStopInteraction();
            }
            if(fedamb.getCzyTworzycKlienta())
            {
                fedamb.setCzyTworzycKlienta(false);
                sendNowyKlientInteraction(generateAndAddKlient());
            }
            if(fedamb.getCzyTworzycVIP())
            {
                fedamb.setCzyTworzycVIP(false);
                sendNowyKlientInteraction(generateAndAddKlientVIP());
            }
            if(fedamb.getCzyTworzycKase())
            {
                fedamb.setCzyTworzycKase(false);
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
