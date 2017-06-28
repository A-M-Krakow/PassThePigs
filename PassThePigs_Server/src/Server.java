import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by anna on 09.05.2017.
 */
public class Server {
    private static ServerSocket server; // deklaracja zmiennej przechowującej socket na którym nasłuchuje serwer
    private static final  int PORT = 22; // definicja portu, na którym nasłuchuje serwer

    public static void main(String args[]){
        int maxIloscGraczy = 0;    // definicja zmiennej przechowującej ilość graczy na początkowe 0

        while (maxIloscGraczy < 2 || maxIloscGraczy>5)                  // dopóki ilość graczy nie będzie z zakresu 2 do 5
        {
            System.out.print("Podaj ilość graczy (od 2 do 5):");  // serwer prosi o jej wprowadzenie
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextInt()) maxIloscGraczy = sc.nextInt();        //dopisywanie liczby do zmiennej maxIloscGraczy jeśli jest liczbą całkowitą
        }
        try
        {
            server = new ServerSocket(PORT); // definicja socketu serwera na odpowiednim porcie
            System.out.println("Serwer gry uruchomiony na porcie: " + PORT);

            while (true){
                Socket socket = server.accept(); // włączenie akceptowania nowego połączenia
                InetAddress addr = socket.getInetAddress(); // definicja zmiennej przechowującej adres połączonego klienta
                System.out.println("Połączenie z adresu: "+ addr.getHostName() + " [" + addr.getHostAddress() + "]");
                new Uczestnik(socket, maxIloscGraczy).start(); // uruchomienie obsługi gry dla uczestnika
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Uczestnik extends Thread {
    static Vector<Uczestnik> uczestnicy = new Vector<Uczestnik>(); // definicja zmiennej przechowującej wszystkich uczestnikow;
    static int polaczeniGracze = 0;  //definicja zmiennej przechowującej ilość podłączonych uczestników
    static Uczestnik aktualnyUczestnik;
    static int maxIloscGraczy;
    static int ktoZacznie = 0;
    boolean pierwszy = false;
    int punkty = 0;
    private Socket socket; // deklaracja socketu dla połączenia z uczestnikiem
    private BufferedReader in; // deklaracja strumienia danych otrzymanych od uczestnika
    private PrintWriter out; // deklaracja strumienia danych wysyłanych do uczestnika
    private String nick; // deklaracja nazwy uczestnika

    public Uczestnik(Socket socket, int maxIloscGraczy) { // konstruktor obsługi nowego połączenia)
        this.socket = socket;
        if (this.maxIloscGraczy == 0)
        {
        this.maxIloscGraczy = maxIloscGraczy; }
    }

    class Rzut {

        String [] ulozeniaFigurek = new String[2];
        boolean stykajaSie = false;
        String[] mozliweUlozenia = {
                "ucho",
                "ryjek",
                "nogi",
                "plecy",
                "prawy bok",
                "lewy bok"
        };

        public Rzut() {
            ulozeniaFigurek[0] = mozliweUlozenia[2];
            ulozeniaFigurek[1] =  mozliweUlozenia[5];

            int losowanie;
         for (int i=0; i<ulozeniaFigurek.length; i++) {
                losowanie = (int) (Math.random() * 100);
                if (losowanie >= 0 && losowanie < 5) ulozeniaFigurek[i] = mozliweUlozenia[0];
                if (losowanie >= 5 && losowanie < 15) ulozeniaFigurek[i] = mozliweUlozenia[1];
                if (losowanie >= 15 && losowanie < 30) ulozeniaFigurek[i] = mozliweUlozenia[2];
                if (losowanie >= 30 && losowanie < 50) ulozeniaFigurek[i] = mozliweUlozenia[3];
                if (losowanie >= 50 && losowanie < 75) ulozeniaFigurek[i] = mozliweUlozenia[4];
                if (losowanie >= 75 && losowanie < 100) ulozeniaFigurek[i] = mozliweUlozenia[2];
            }

                losowanie = (int) (Math.random() * 100);
                if (losowanie >= 0 && losowanie < 10) stykajaSie = true;

        }


         public void pokazUlozenie() {
            wyslijDoWszystkich("Figurka 1 spadła na: " + ulozeniaFigurek[0]);
            wyslijDoWszystkich("Figurka 2 spadła na: " + ulozeniaFigurek[1]);

            if (stykajaSie) wyslijDoWszystkich("Figurki stykają się!");

         }




        }


    private void wyslijDoJednego(Uczestnik uczestnik, String text)
    {
        uczestnik.out.println(text);
    }

    private synchronized void wyslijDoWszystkich(String tekst) { // metoda wysyłająca dane do wszytkich uczestników
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
                    uczestnik.out.println(tekst); //do wszystkich  wysyłamy  tekst
        }
    }

    private synchronized  void wyslijDoInnych(Uczestnik zWyjatkiem, String tekst) { // metoda wysyłająca dane do pozostałych uczetników
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
                if (uczestnik != zWyjatkiem)  // jeżeli uczestnik nie jest wykluczonym uczestnikiem
                uczestnik.out.println(tekst); //do wszystkich innych wysyłamy  tekst

        }
    }

    private void graj() {

        Rzut rzut = new Rzut();
        rzut.pokazUlozenie();

        out.println("\nZdobyłeś punkt!");

        punkty++;
        out.println("\nMasz pu2nktów:" + punkty);
        out.println("[enter] - rzucaj");
        out.println("/p - punkty");
        out.println("/r - rezygnuj");
        out.println("/q - wyjdź\n");
        wyslijDoInnych(this, this.nick + " +1 punkt!");
    }

    private void pokazPunkty() {
        out.println("\n*****************");
        out.println("TABELA PUNKTÓW:");

        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników

            out.println(uczestnik.nick + ": " + uczestnik.punkty + "pkt" );
        }
        out.println("*****************");
    }

    private void users() { // metoda wyświetlająca informacje na temat użytkowników
        out.println("Witaj " + nick + ", aktualnie grają: ");
        for (Uczestnik uczestnik : uczestnicy) { // dla wszystkich uczestników z listy uczestników
            synchronized(uczestnicy) { // synchronizowane działanie
                if (uczestnik != this) // jeżeli uczestnik nie jest bieżącym uczestnikiem
                    out.print(uczestnik.nick + " "); // wyświetlamy nick tego uczestnika.
            }
        }
        out.println();
    }

    private synchronized void dolaczDoGry(){

            uczestnicy.add(this); // dodanie bieżącego uczestnika do listy uczestników

        System.out.println("Do gry dołączył: " + nick);                         // to wyświetlamy na serwerze
        System.out.println("Połączonych graczy: " + uczestnicy.size()+ "/" + maxIloscGraczy);      // to też
        wyslijDoInnych(this, "Do gry dołączył: " + nick);                         // wysyłamy do wszystkich
        wyslijDoJednego(this, "Dołączyłeś do gry!");
    }

    private synchronized void opuscGre(){
        wyslijDoWszystkich("Grę opuścił: " + nick);                              // jeżeli wpisał /q to opuszcza grę
        if (this == aktualnyUczestnik) {
            zakonczTure();
        }
            uczestnicy.removeElement(this);
            maxIloscGraczy--;
        System.out.println("Grę opuścił: " + nick);
        System.out.println("Połączonych graczy: " + uczestnicy.size()+ "/" + maxIloscGraczy);

    }

    private synchronized void zakonczTure() {

            if ((uczestnicy.indexOf(this) + 1 == uczestnicy.size())) {
                ustawAktualnego(0);
            } else {
                ustawAktualnego(uczestnicy.indexOf(this) + 1);
            }
    }

    private int losujKtoZacznie()
    {
        int ktoZacznie = (int)(Math.random() * maxIloscGraczy);
        return ktoZacznie;
    }

    private synchronized  void ustawAktualnego(int numerAktualnego) {
        aktualnyUczestnik = uczestnicy.get(numerAktualnego) ;
        wyslijDoJednego(uczestnicy.get(numerAktualnego), "\n\nTWOJA KOLEJ!\n\n");
        wyslijDoJednego(uczestnicy.get(numerAktualnego), "[enter] - rzucaj");
        wyslijDoJednego(uczestnicy.get(numerAktualnego),"/r - rezygnuj");
        wyslijDoInnych(aktualnyUczestnik, "\n\nTeraz rzuca: " + aktualnyUczestnik.nick + "\n\n");
        wyslijDoWszystkich("/p - punkty");
        wyslijDoWszystkich("/q - wyjscie");
    }



    public void run() {
        String linia; // deklaracja napisu wpisanego przez użytkownika

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // definicja strumienia wejściowego
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // definicja strumienia wyjściowego
            out.println("Połączony z serwerem. Komenda /q kończy połączenie.");   // to wysyłamy do klienta
            out.println("Podaj swój nick: ");                                       // to też
            nick = in.readLine();                                                   // odbieramy od klienta nick

            if (uczestnicy.size() < maxIloscGraczy) {
                dolaczDoGry();
                if (uczestnicy.size() == 1)
                {
                  pierwszy = true;
                }

                if (uczestnicy.size() != maxIloscGraczy) {
                    out.println("Czekaj na dołączenie wszystkich graczy!");
                    while (uczestnicy.size() != maxIloscGraczy) {
                    }
                }

                if (pierwszy)
                {
                    ktoZacznie = losujKtoZacznie();
                    ustawAktualnego(ktoZacznie);
                }



                    while ((linia = in.readLine()) != null) {
                        if (linia.equalsIgnoreCase("/q")) {
                            break;
                        }
                        if (linia.equalsIgnoreCase("/p")) {
                            pokazPunkty();
                        }
                        else {
                            if (aktualnyUczestnik == this) {

                                if ((linia.equalsIgnoreCase("/r"))) {
                                    zakonczTure();
                                } else {
                                    graj();
                                }
                            }
                        }
                    }
                    opuscGre();
            }
            else {
                out.println("Serwer jest pełny! Komenda /q kończy połączenie.");   // to wysyłamy do klienta
                while ((linia = in.readLine())!= null) {
                    if (linia.equalsIgnoreCase("/q")) break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                in.close(); // zamknięcie wejścia
                out.close(); // zamknięcie wyjścia
                socket.close(); // zamknięcie socketu
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
