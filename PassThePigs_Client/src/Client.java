import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Client extends JFrame {

    //GUI
    private JButton rzucaj, rezygnuj, polacz, rozlacz;
    private JPanel panelDol;
    private JPanel panelGora;
    private JTextField host, port, wiadomosc;
    private JTextArea komunikaty;
    private JList zalogowani;
    private DefaultListModel listaZalogowanych;

    //Klient
    private String nazwaSerwera = "localhost";
    private int numerPortu = 23;
    private boolean polaczony = false;
    private Klient watekKlienta;

    public Client(){
        super("Klient");
        setSize(600, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panelDol = new JPanel(new FlowLayout());
        panelGora = new JPanel(new FlowLayout());
        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        wiadomosc = new JTextField();
        rzucaj = new JButton("Rzucaj");
        rezygnuj = new JButton("Rezygnuj");
        host = new JTextField(nazwaSerwera ,12);
        port = new JTextField((new Integer(numerPortu)).toString() ,8);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        rozlacz.setEnabled(false);

        listaZalogowanych = new DefaultListModel();
        zalogowani = new JList(listaZalogowanych);
        zalogowani.setFixedCellWidth(120);

        ObslugaZdarzen obsluga = new ObslugaZdarzen();

        rzucaj.addActionListener(obsluga);
        rezygnuj.addActionListener(obsluga);
        polacz.addActionListener(obsluga);
        rozlacz.addActionListener(obsluga);


        wiadomosc.addKeyListener(obsluga);
        panelDol.add(rzucaj);
        panelDol.add(rezygnuj);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                rozlacz.doClick();
                setVisible(false);
                System.exit(0);
            }
        });

        panelGora.add(new JLabel("Serwer: "));
        panelGora.add(host);
        panelGora.add(new JLabel("Port: "));
        panelGora.add(port);
        panelGora.add(polacz);
        panelGora.add(rozlacz);

        add(panelGora, BorderLayout.NORTH);
        add(panelDol, BorderLayout.SOUTH);
        add(new JScrollPane(zalogowani), BorderLayout.EAST);
        add(new JScrollPane(komunikaty), BorderLayout.CENTER);
        setVisible(true);

    }

    private class ObslugaZdarzen extends KeyAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("Połącz")) {
                wyswietlKomunikat("Łączę z: " + nazwaSerwera + " na porcie: " + numerPortu + "...");
                polaczony = true;
                polacz.setEnabled(false);
                rozlacz.setEnabled(true);
                host.setEnabled(false);
                port.setEnabled(false);
                watekKlienta = new Klient();
                watekKlienta.start();
                //repaint();
            }
            if (e.getActionCommand().equals("Rozłącz")){
                watekKlienta.wyslij("/q");
                polaczony = false;
                rozlacz.setEnabled(false);
                polacz.setEnabled(true);
                host.setEnabled(true);
                port.setEnabled(true);
                setVisible(false);
            }

            if (e.getActionCommand().equals("Rzucaj")) {
                watekKlienta.wyslij("");
            }
            if (e.getActionCommand().equals("Rezygnuj")) {
                watekKlienta.wyslij("/r");
            }

        }

        public void keyReleased(KeyEvent e){
            if(e.getKeyCode() == 10) {
                watekKlienta.wyslij(wiadomosc.getText());
            }
        }
    }

    private class Klient extends Thread{
        private Socket socket;
        private BufferedReader wejscie;
        private PrintWriter wyjscie;

        public void run(){

            try {
                socket = new Socket(host.getText(), new Integer(port.getText()));
                wyswietlKomunikat("Połączono.");
                polaczony = true;

                wejscie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                wyjscie = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                String lancuch = null;
                String nick = JOptionPane.showInputDialog(null, "Podaj nick: ");
                wyslij(nick);

                while(polaczony &&  (lancuch = wejscie.readLine()) != null){
                    wyswietlKomunikat(lancuch);
                }

            }

            catch (UnknownHostException e) {
                wyswietlKomunikat("Błąd połączenia!");
            }
            catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    wejscie.close(); // zamknięcie wejścia
                    wyjscie.close(); // zamknięcie wyjścia
                    socket.close(); // zamknięcie socketu
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void wyslij(String tekst) {
            wyjscie.println(tekst);
            wiadomosc.setText("");
        }
    }

    private void wyswietlKomunikat(String tekst){

        if(tekst.startsWith("USERS:")){
            //Aktualizacja listy
            StringTokenizer gracze = new StringTokenizer(tekst.substring("USERS:".length()), ",");

            listaZalogowanych.clear();
            //while(uzytkownicy.hasMoreTokens())
            //	listaZalogowanych.addElement(uzytkownicy.nextToken());

            System.out.println(gracze.countTokens());
            System.out.println(listaZalogowanych);

            int iloscOsob = gracze.countTokens();
            for(int i = 0; i < iloscOsob; i++) {
                listaZalogowanych.add(i, gracze.nextToken());
            }

            System.out.println(listaZalogowanych);
        }

        komunikaty.append(tekst + "\n");
        komunikaty.setCaretPosition(komunikaty.getDocument().getLength());

    }

    public static void main (String[] args) {
        new Client();
    }
}