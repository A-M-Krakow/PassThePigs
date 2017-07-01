import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by anna on 09.05.2017.
 */
public class Server implements PtpProtocol{
    private static ServerSocket server; // deklaracja zmiennej przechowującej socket na którym nasłuchuje serwer
    private static final int PORT = 23; // definicja portu, na którym nasłuchuje serwer
    private int maxIloscGraczy = 0;   // definicja zmiennej przechowującej ilość graczy na początkowe 0
    private Gra gra;


    public Server() {
        wprowadzIloscGraczy();
        this.gra = new Gra(maxIloscGraczy);
        ustanowPolaczenie();
        /* Utworzenie nowej gry */
    }

    private void wprowadzIloscGraczy() {
        while (maxIloscGraczy < 2 || maxIloscGraczy > 5)                  // dopóki ilość graczy nie będzie z zakresu 2 do 5
        {
            System.out.print("Podaj ilość graczy (od 2 do 5):");  // serwer prosi o jej wprowadzenie
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextInt())
                maxIloscGraczy = sc.nextInt();        //dopisywanie liczby do zmiennej maxIloscGraczy jeśli jest liczbą całkowitą
        }
        this.maxIloscGraczy = maxIloscGraczy;
    }
    private void ustanowPolaczenie() {
        try {
            server = new ServerSocket(PORT); // definicja socketu serwera na odpowiednim porcie
            System.out.println("Serwer gry uruchomiony na porcie: " + PORT);

            while (true) {
                Socket socket = server.accept(); // włączenie akceptowania nowego połączenia
                InetAddress addr = socket.getInetAddress(); // definicja zmiennej przechowującej adres połączonego klienta
                System.out.println("Połączenie z adresu: " + addr.getHostName() + " [" + addr.getHostAddress() + "]");
                new Uczestnik(socket, gra).start(); // uruchomienie obsługi gry dla uczestnika na jego sockecie
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




/*Obiekty klasy Gra to gry uruchomione na serwerze (w przyszłości będzie mogło być ich więcej niż jedna) */
class Gra {
    private Vector<Uczestnik> uczestnicy = new Vector<>(); // definicja zmiennej przechowującej wszystkich uczestnikow gry
    private Uczestnik aktualny = null; // deklaracja uczestnika, który gra w danym momencie
    private Uczestnik pierwszyWKolejce; //deklaracja uczestnika, który rozpoczyna kolejkę
    private int poczatkowaMaxIloscGraczy; //deklaracja maksymalnej ilości graczy na początku gry
    private int maxIloscGraczy; //deklaracja maksymalnej ilości graczy, która będzie aktualizowana jeżeli gracze opuszczą grę
    private boolean czyJestZwyciezca = false;

    public Gra(int maxIloscGraczy)
    {
        this.maxIloscGraczy = poczatkowaMaxIloscGraczy = maxIloscGraczy;
    }

    /* metoda dodająca uczestnika do danej gry */
    public synchronized void dodajUczestnika(Uczestnik uczestnik){
        uczestnicy.add(uczestnik);
        System.out.println("Do gry dołączył: " + "<" + uczestnik.podajNick() + ">");                         // to wyświetlamy na serwerze
        System.out.println("Połączonych graczy: " + this.podajIloscGraczy() + "/" + maxIloscGraczy);
        wyslijDoWszystkich(JOINED_COMMAND +  uczestnik.podajNick());
    }

    /* Metoda zwracająca ilość graczy w grze */
    public int podajIloscGraczy() {
        return uczestnicy.size();
    }

    /* Metoda zwracająca maksymalną ilość graczy w grze */
    public int podajMaxIloscGraczy() {
        return maxIloscGraczy;
    }


    /* Metoda zwracająca uczestnika, który aktualnie gra */
    public synchronized void ustawAktualnego(Uczestnik nowyAktualny) {

        this.aktualny = nowyAktualny;
    }

    /* Metoda zwracająca uczestnika, który aktualnie gra */
    public synchronized Uczestnik podajAktualnego() {
        return aktualny;
    }

    /* Metoda sprawdzająca, czy jest już zwycięzca gry */
    public void szukajZwyciezcy() {
        for (Uczestnik uczestnik : uczestnicy) {
            /* dla wszystkich uczestników */
            if (uczestnik.podajWszystkiePunkty() > 99) {
                /*jeżeli punkty uczestnika przekroczyły 99 */
                this.czyJestZwyciezca = true;
                /* oznaczamy, że wyłoniony został zwycięzca */
            }
        }
    }

    /*Metoda zwracająca info czy są wolne miejsca */
    public boolean czySaMiejsca() {
        if (podajIloscGraczy() < podajMaxIloscGraczy()) return true;
        else return false;
    }

    public boolean czySaWszyscy() {
        if  (podajIloscGraczy() == podajMaxIloscGraczy()) return true;
        else return false;
    }

    /*Metoda zwracająca info czy jest zwycięzca */
    public boolean wskazCzyJestZwyciezca()
    {
        return czyJestZwyciezca;
    }

    /* Metoda przekazująca kolejkę do następnego uczestnika */
    public synchronized  void przekazKolejke(Uczestnik nowyAktualny) {
        ustawAktualnego(nowyAktualny) ;
        /*aktualny gracz zmienia się na następnego gracza */

        if(aktualny==pierwszyWKolejce) szukajZwyciezcy();
        /* jeżeli nowy aktualny gracz jest pierwszy w kolejce, sprawdzamy, czy nie wyłonił się zwycięzca */

        if (!wskazCzyJestZwyciezca() && podajIloscGraczy()>1) {
            /* jeżeli nie wyłonił się zwycięzca i jest więcej niż jeden gracz*/
            wyslijDoJednego(this.aktualny, YOUR_TURN_COMMAND);
            wyslijDoInnych(this.aktualny, THROWS_COMMAND +  this.aktualny.podajNick());
        }
        else {
            /* jeżeli wyłonił się zwycięzca */

            Vector<Uczestnik> ostateczneWyniki = uczestnicy;
            for (Uczestnik uczestnik : ostateczneWyniki) {
            /*dla każdego z uczestników gry */
                    for (Uczestnik gracz : ostateczneWyniki) {
                    /*porównujemy jego punkty z każdym innym uczestnikiem gry */
                        boolean przegrany = false; // zmienna przechowująca informację o przegranej gracza
                        if (uczestnik.podajWszystkiePunkty() < gracz.podajWszystkiePunkty()) {
                        /*jeżeli gracz ma mniej punktów niż którykolwiek z innych graczy*/
                            przegrany = true;
                        }
                        if (!przegrany) {
                            wyslijDoJednego(uczestnik, YOU_WON_COMMAND); // wysłanie komendy jeżeli wygrał
                        } else wyslijDoJednego(uczestnik, YOU_LOST_COMMAND); // wysłanie komendy jeżeli przegrał
                        przegrany = false;
                    }
            }
        }
    }

    /* Metoda zakańczająca turę gracza */
    public synchronized void zakonczTure() {
        wyslijDoWszystkich(END_TURN_COMMAND + aktualny.podajNick());
        aktualny.przyznajPunkty();
        /*dopisujemy graczowi punkty, które zdobył w turze */
        pokazPunkty();
        if ((uczestnicy.indexOf(aktualny) + 1 == uczestnicy.size())) {
                        /*jeżeli aktualny gracz jest ostatni w kolejce */
            przekazKolejke(uczestnicy.get(0));
                        /*przekazuje kolejkę pierwszemu w kolejce graczowi */
        }
        else {
            przekazKolejke(uczestnicy.get(uczestnicy.indexOf(aktualny) + 1));
                        /*w przeciwnym wypadku przekazuje kolejkę graczowi, który jest w kolejce po nim */
        }

    }


    /*Metoda usuwająca gracza z gry */
    public synchronized void opuscGre(Uczestnik uczestnik){
        if(pierwszyWKolejce == uczestnik && podajIloscGraczy()>1)
        /*jeżeli opuszczający był pierwszym w kolejce i jest więcej niż jeden gracz*/
        {
            if (uczestnicy.indexOf(uczestnik)+1 == podajIloscGraczy())
                /*jeżeli opuszczający był ostatni na liście */
                ustawPierwszego(uczestnicy.get(0));
                /*pierwszym w kolejce zostaje pierwszy na liście */
            else ustawPierwszego(uczestnicy.get(uczestnicy.indexOf(uczestnik) +1));
                 /* w przeciwnym wypadku, pierwszym w kolejce zostaje następny na liście */
        }
        if (uczestnik == aktualny) {
            zakonczTure();
        }
        uczestnicy.removeElement(uczestnik);
        /*usuwamy uczestnika z listy uczestników */
        maxIloscGraczy--;
        /*zmniejszamy maksymalną ilość graczy - żeby inni nie dołączyli w środku gry */
        System.out.println("Grę opuścił: " + "<" + uczestnik.podajNick() + ">");
        System.out.println("Połączonych graczy: " + uczestnicy.size()+ "/" + maxIloscGraczy);
        if (maxIloscGraczy ==0) {
            /* jeżeli wszyscy gracze opuścili grę to gra rozpoczyna się na nowo */
            maxIloscGraczy = poczatkowaMaxIloscGraczy;
            /*maksymalna ilość graczy jest ustawiana na tą, która była na początku */
            czyJestZwyciezca = false;
            /*dodawana jest informacja o braku zwycięzcy w grze */
        }
        uczestnik.wyloguj();
    }

    /* Metoda wysyłająca wiadomość do wszystkich graczy w grze */
    public synchronized void wyslijDoWszystkich(String wiadomosc) { // metoda wysyłająca tekst do wszytkich uczestników
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
            uczestnik.wyslijWiadomosc(wiadomosc);
        }
    }

    /*Metoda wysyłająca wiadomość do jednego gracza w grze */
    public void wyslijDoJednego(Uczestnik uczestnik, String wiadomosc)    {
        uczestnik.wyslijWiadomosc(wiadomosc);
    }

    /* Metoda wysyłająca wiadomość do wszystkich graczy poza podanym */
    public synchronized  void wyslijDoInnych(Uczestnik zWyjatkiem, String wiadomosc) {
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
            if (uczestnik != zWyjatkiem)  // jeżeli uczestnik nie jest wykluczonym uczestnikiem
                uczestnik.wyslijWiadomosc(wiadomosc); //do wszystkich innych wysyłamy  wiadomosc

        }
    }

    /* Metoda rozpoczynająca turę gracza */
    public void graj() {
        Rzut rzut = new Rzut(this);
        /* rzucamy figurkami */
        rzut.podajUlozenie();
        /*odczytujemy ułożenie figurek */
        rzut.podliczPunkty();
        if (rzut.czyPechowy()) this.zakonczTure();
        /*jeżeli rzut był pechowy to gracz traci turę */
    }

    /*Metoda wyśtlająca podanemu graczowi tabelę z punktami */
    public void pokazPunkty() {

        StringBuilder lista = new StringBuilder();

        for (Uczestnik gracz : uczestnicy) {
            lista.append(gracz.podajNick() +  ": " + gracz.podajWszystkiePunkty() + "pkt" + ",");
        }
        wyslijDoWszystkich(USERS_LIST_COMMAND + lista.toString());
    }

    /* Metoda ustawiająca pierwszego gracza w kolejce */
    public void ustawPierwszego(Uczestnik uczestnik)
    {
        this.pierwszyWKolejce = uczestnik;
    }

    /*Metoda ustawiająca maksymalną ilość graczy w grze */
    public void ustawMaxIloscGraczy(int maxIloscGraczy) {
        this.maxIloscGraczy = maxIloscGraczy;
    }

    /*Metoda zwracająca listę graczy */
    public Vector<Uczestnik> podajListeUczestnikow()
    {
        return uczestnicy;
    }

}


/*Każdy obiekt klasy Rzut to rzut dwiema figurkami wykonywany przez uczestnika*/
class Rzut {
    Gra gra; //gra, do której należy rzut
    int punktywRzucie = 0;  //punkty zdobyte przy rzucie
    boolean stykajaSie = false; // informacja, czy wyrzucone figurki się stykają
    boolean pechowy = false; // informacja, czy rzut był pechowy (kończący turę)
    String [] ulozeniaFigurek = new String[2]; // tablica przechowująca ułożenia 2 wyrzuconych figurek */

    String[] mozliweUlozenia = {  // tablica przechowująca wszystkie możliwe ułożenia figurki
            "ucho",
            "ryjek",
            "nogi",
            "plecy",
            "prawy bok",
            "lewy bok"
    };

    public Rzut(Gra gra) {
        this.gra = gra;
        int losowanie;
        for (int i=0; i<ulozeniaFigurek.length; i++) { //dla każdej figurki losujemy jej ułożenie po wyrzuceniu */
            losowanie = (int) (Math.random() * 100);
            if (losowanie >= 0 && losowanie < 5) ulozeniaFigurek[i] = mozliweUlozenia[0];           // wylosowano ucho
            else if (losowanie >= 5 && losowanie < 15) ulozeniaFigurek[i] = mozliweUlozenia[1];     // wylosowano ryjek
            else if (losowanie >= 15 && losowanie < 30) ulozeniaFigurek[i] = mozliweUlozenia[2];    // wylosowano nogi
            else if (losowanie >= 30 && losowanie < 50) ulozeniaFigurek[i] = mozliweUlozenia[3];    // wylosowano plecy
            else if (losowanie >= 50 && losowanie < 75) ulozeniaFigurek[i] = mozliweUlozenia[4];    // wylosowano prawy bok
            else if (losowanie >= 75 && losowanie < 100) ulozeniaFigurek[i] = mozliweUlozenia[5];   // wylosowano lewy bok
        }

        losowanie = (int) (Math.random() * 100); // losujemy, czy figurki się stykają
        if (losowanie >= 0 && losowanie < 10) stykajaSie = true;

    }

    /* Metoda zwracająca informację, czy rzut był pechowy */
    public boolean czyPechowy(){
        return pechowy;
    }

    /* Metoda wyświetlająca ułożenie rzuconych figurek */
    public void podajUlozenie() {
        gra.wyslijDoWszystkich(RESULT_COMMAND + ulozeniaFigurek[0] + "-" +   ulozeniaFigurek[1]);
    }

    /* Metoda zwracająca ilość zdobytych w rzucie punktów*/
    public void podliczPunkty() {

        if (!stykajaSie) {
            /* jeżeli figurki się nie stykają */
            if ( (ulozeniaFigurek[0].equals(mozliweUlozenia[4]) && (ulozeniaFigurek[1].equals(mozliweUlozenia[5]))) || (ulozeniaFigurek[0].equals(mozliweUlozenia[5]) && (ulozeniaFigurek[1].equals(mozliweUlozenia[4])))) {
                /*jeżeli na jednej figurce jest lewy a na drugiej prawy bok (albo odwrotnie */
                gra.wyslijDoWszystkich(TURN_LOST_COMMAND);
                pechowy = true;   // rzut oznaczamy jako pechowy (kończący turę)
                gra.podajAktualnego().ustawPunktyWTurze(0); // zerujemy punkty w turze
            }
            else {
                if (ulozeniaFigurek[0].equals(ulozeniaFigurek[1])) //jeżeli figurki mają takie samo ułożenie
                {
                    if(ulozeniaFigurek[0].equals(mozliweUlozenia[0])) punktywRzucie=60;         // punkty za 2 x  ucho
                    else if(ulozeniaFigurek[0].equals(mozliweUlozenia[1])) punktywRzucie=40;    // punkty za 2 x ryjek
                    else if(ulozeniaFigurek[0].equals(mozliweUlozenia[2])) punktywRzucie=20;    // punkty za 2 x nogi
                    else if(ulozeniaFigurek[0].equals(mozliweUlozenia[3])) punktywRzucie=20;    // punkty za 2 x plecy
                    else if(ulozeniaFigurek[0].equals(mozliweUlozenia[4])) punktywRzucie=1;     // punkty za 2 x prawy bok
                    else if(ulozeniaFigurek[0].equals(mozliweUlozenia[5])) punktywRzucie=1;     // punkty za 2 x lewy bok
                }
                else { /*jeżeli ułożenia figurek są różne */
                    for (int i=0; i<ulozeniaFigurek.length; i++) {  /*dla każdej figurki osobno */
                        if (ulozeniaFigurek[i].equals(mozliweUlozenia[0])) punktywRzucie+=15;        // punkty za ucho
                        else if (ulozeniaFigurek[i].equals(mozliweUlozenia[1])) punktywRzucie+=10;   // punkty za ryjek
                        else if (ulozeniaFigurek[i].equals(mozliweUlozenia[2])) punktywRzucie+=5;    // punkty za nogi
                        else if (ulozeniaFigurek[i].equals(mozliweUlozenia[3])) punktywRzucie+=5;    // punkty za plecy

                    }
                }
                gra.wyslijDoJednego(gra.podajAktualnego(), GOT_POINTS_COMMAND  + punktywRzucie);
                gra.podajAktualnego().ustawPunktyWTurze(gra.podajAktualnego().podajPunktyWTurze()+punktywRzucie);
                /* dodanie  punktów w rzucie do punktów w turze */

                gra.wyslijDoJednego(gra.podajAktualnego(), TURN_POINTS_COMMAND + gra.podajAktualnego().podajPunktyWTurze());
                gra.wyslijDoInnych(gra.podajAktualnego(), ELSE_POINTS_COMMAND + gra.podajAktualnego().podajNick() + "-"  + punktywRzucie);

            }
        } else {  /*jeżeli figurki się stykały */
            gra.wyslijDoWszystkich(TOUCHING_COMMAND);
            pechowy = true; // rzut oznaczamy jako pechowy (kończący turę)
            gra.podajAktualnego().ustawPunktyWTurze(0);  // zerujemy punkty w turze
            gra.podajAktualnego().ustawWszystkiePunkty(0); // wszystkie punkty gracza również zerujemy
        }
    }

}

/*Obiekty klasy Uczestnik to gracze podłączeni do serwera gry */
class Uczestnik extends Thread implements PtpProtocol{
    private Gra gra; // deklaracja gry, w której bierze udział uczestnik
    private String linia; // deklaracja napisu wpisanego przez użytkownika
    private int wszystkiePunkty = 0;  // wszystkie punkty gracza
    private int punktyWTurze = 0;     // punkty gracza w aktualnej turze
    private Socket socket; // deklaracja socketu dla połączenia z uczestnikiem
    private BufferedReader in; // deklaracja strumienia danych otrzymanych od uczestnika
    private PrintWriter out; // deklaracja strumienia danych wysyłanych do uczestnika
    private String nick; // deklaracja nazwy uczestnika
    private boolean zalogowany = false;

    public Uczestnik(Socket socket, Gra gra) { // konstruktor nowego gracza (oznaczenie gry oraz socketu, na którym jest jego połączenie)
        this.gra = gra;
        this.socket = socket;
    }

    /* Metoda dodająca punkty z tury do wszystkich punktów */
    public void przyznajPunkty() {
        this.wszystkiePunkty+=this.punktyWTurze;
        this.punktyWTurze = 0;
    }

    /*Metoda wyświetlająca tekst tylko temu uczetnikowi */
    public void wyslijWiadomosc(String wiadomosc)
    {
        this.out.println(wiadomosc);
    }

    /* Metoda zwracająca nick uczestnika */
    public String podajNick() {
        return nick;
    }

    /* Metoda zwracająca wszystkie punkty uczestnika */
    public int podajWszystkiePunkty()    {
        return wszystkiePunkty;
    }

    /*Metoda zwracająca punkty uczestnika w aktualnej turze */
    public int podajPunktyWTurze()    {
        return punktyWTurze;
    }

    /*Metoda aktualizująca wszystkie punkty uczestnika*/
    public void ustawWszystkiePunkty(int punkty)    {
        wszystkiePunkty = punkty;
    }

    /*Metoda aktualizująca punkty uczestnika w aktualnej turze */
    public void ustawPunktyWTurze(int punkty)    {
        punktyWTurze = punkty;
    }

    /*Metoda zaznaczająca, że gracz nie jest zalogowany */
    public void wyloguj(){
        this.zalogowany = false;
    }


    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // definicja strumienia wejściowego
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // definicja strumienia wyjściowego
            this.wyslijWiadomosc(CONNECTED_COMMAND);

            if ((linia = in.readLine()).startsWith(NICK_COMMAND)){
                nick = linia.substring(NICK_COMMAND.length()); // pobranie nicku od użytkownika

                if (gra.czySaMiejsca()) {
                /* jeżeli w grze są jeszcze wolne miejsca */
                    gra.dodajUczestnika(this);
                    gra.pokazPunkty();
                    zalogowany = true;
                    if (!gra.czySaWszyscy()) this.wyslijWiadomosc("WAIT_COMMAND");
                    else {
                        int wylosowany = (int) (Math.random() * gra.podajMaxIloscGraczy() - 1);
                        gra.ustawPierwszego(gra.podajListeUczestnikow().get(wylosowany));
                        gra.przekazKolejke(gra.podajListeUczestnikow().get(wylosowany));
                    }
                    while ((linia = in.readLine()) != null) {
                    /*dopóki jest dostępna linia tekstu przysłana od strony gracza */
                        if (linia.equals(QUIT_COMMAND)) {
                        /*jeżeli gracz wybrał wyjście z gry*/
                            break;
                        /*przestajemy odczytywać jego dane i przechodzimy do opuszczania gry */
                        } else {
                        /*jeżeli gracz nie wybrał wyjścia z gry */
                            if (gra.podajAktualnego() == this) {
                            /*jeżeli gracz jest aktualnie grającym */
                                if (!linia.equals(RESIGN_COMMAND))
                            /*jeżeli nie wybrał rezygnacji z kolejki */ {
                                    gra.graj();
                                /*rozpoczyna swoją kolejkę */
                                } else gra.zakonczTure();
                            /*w przeciwnym wypadku kończy się jego kolejka */
                            }
                        }
                    }
                    gra.opuscGre(this);
                } else {
                    out.println("Serwer jest pełny!");   /* gra nie miała wolnych miejsc, gracz nie został wpuszczony */
                    while ((linia = in.readLine()) != null) {
                        if (linia.equalsIgnoreCase(QUIT_COMMAND)) break;
                    }
                }
            }
        } catch (IOException e) {
        }finally {
            try {
                if (zalogowany) gra.opuscGre(this);
                in.close(); // zamknięcie wejścia
                out.close(); // zamknięcie wyjścia
                socket.close(); // zamknięcie socketu
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
    public static void main (String[] args) {
        new Server();
    }
}
