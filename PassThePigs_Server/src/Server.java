import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by anna on 09.05.2017.
 */
public class Server {
    private static ServerSocket server; // deklaracja zmiennej przechowującej socket na którym nasłuchuje serwer
    private static final  int PORT = 22; // definicja portu, na którym nasłuchuje serwer

    public static void main(String args[]){
        try
        {
            server = new ServerSocket(PORT); // definicja socketu serwera na odpowiednim porcie
            System.out.println("Serwer gry uruchomiony na porcie: " + PORT);

            while (true){
                Socket socket = server.accept(); // włączenie akceptowania nowego połączenia

                InetAddress addr = socket.getInetAddress(); // definicja zmiennej przechowującej adres połączonego klienta
                System.out.println("Połączenie z adresu: "+ addr.getHostName() + " [" + addr.getHostAddress() + "]");

                new Uczestnik(socket).start(); // uruchomienie obsługi gry dla uczestnika
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Uczestnik extends Thread {
    static Vector<Uczestnik> uczestnicy = new Vector<Uczestnik>(); // definicja zmiennej przechowującej wszystkich uczestnikow;
    private Socket socket; // deklaracja socketu dla połączenia z uczestnikiem
    private BufferedReader in; // deklaracja strumienia danych otrzymanych od uczestnika
    private PrintWriter out; // deklaracja strumienia danych wysyłanych do uczestnika
    private String nick; // deklaracja nazwy uczestnika

    public Uczestnik(Socket socket) { // konstruktor obsługi nowego połączenia - tworzy się go podając socket)
        this.socket = socket;
    }

    private void wyslijDoWszystkich(String tekst) { // metoda wysyłająca dane do wszytkich obecnych
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
            synchronized(uczestnicy) { // synchronizowane działanie (może być robione tylko przez jeden wątek na raz)
                if (uczestnik != this)  // jeżeli gra nie jest bieżącą grą
                    uczestnik.out.println("<" + nick + ">" + tekst); //do wszystkich innych wysyłamy napisany tekst
            }
        }
    }

    private void users() { // metoda wyświetlająca informacje na temat użytkowników
        out.print("Witaj " + nick + ", aktualnie grają: ");
        for (Uczestnik uczestnik : uczestnicy) { // dla wszystkich uczestników z listy uczestników
            synchronized(uczestnicy) { // synchronizowane działanie
                if (uczestnik != this) // jeżeli uczestnik nie jest bieżącym uczestnikiem
                    out.print(uczestnik.nick + " "); // wyświetlamy nick tego uczestnika.
            }
        }
        out.println();
    }

    public void run() {
        String linia; // deklaracja napisu wpisanego przez użytkownika
        synchronized (uczestnicy) { //synchronizowane działanie
            uczestnicy.add(this); // dodanie bieżącego uczestnika do listy uczestników
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // definicja strumienia wejściowego
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // definicja strumienia wyjściowego

            out.println("Połączony z serwerem. Komenda /end kończy połączenie.");   // to wysyłamy do klienta
            out.println("Podaj swój nick: ");                                       // to też
            nick = in.readLine();                                                   // odbieramy od klienta nick
            System.out.println("Do gry dołączył: " + nick);
            wyslijDoWszystkich("Pojawił się w grze");                      // wysyłamy do wszystkich
            users();                                                                 // wyświetlamy klientowi info o użytkownikach
            while (!(linia = in.readLine()).equalsIgnoreCase("/end")){ //dopóki klient nie wpisze /end
                wyslijDoWszystkich(linia);                                          // to co napisał wysyłamy do wszystkich
            }
            wyslijDoWszystkich("Opuścił grę");                              // jeżeli wpisał /end to opuszcza grę

            System.out.println("Grę opuścił: " + nick);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                in.close(); // zamknięcie wejścia
                out.close(); // zamknięcie wyjścia
                socket.close(); // zamknięcie socketu
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                synchronized(uczestnicy) {
                    uczestnicy.removeElement(this); // usunięcie bieżącego uczestnika z listy uczestników
                }
            }
        }
    }
}
