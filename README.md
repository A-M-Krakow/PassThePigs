# 1. Opis gry
Jest to wykonana w ramach treningu programowania  w javie implementacja gry losowej Pass the Pigs.
Pass thepigs to gra losowa (znana w Polsce jako "Świńska Gra"),podobna do gry w kości. Rzuca się w niej dwiema figurkami w kształcie świnek.
Moja wersja przewiduje uczestnictwo od 2 do 5 graczy. 
Gracz w swojej turze ma możliwość podjęcia kilku decyzji: rzut dwoma wirtualnymi figurkami, rezygnacja z dalszych rzutów oraz zakończenie połączenia z serwerem równoznaczne z zakończeniem gry. Punkty do tabeli punktów przypisywane są po każdej rundzie gracza (w momencie rezygnacji z dalszych rzutów lub utraty kolejki). Gra rozpoczyna się, gdy do serwera dołączy zdefiniowana na początku liczba graczy (od 2 do 5). Wtedy też losowo wybierany jest gracz, który zacznie kolejkę. Odpowiednimi komunikatami gracze są informowani o wszystkich zmianach na serwerze (dołączenie/wyjście graczy), który gracz aktualnie rzuca wirtualnymi figurkami, wylosowany układ figurek przez gracza, ile punktów zdobył rzucający gracz, sumę punktów w danej rundzie. 
 
# 2. Zasady gry 
 
Cel gry 
 
Gracz który jako pierwszy uzyska 100 pkt. zostaje zwycięzcą. 
 
 
Jak grać 
 
 1. Naciśnij przycisk Rzucaj (rzut figurkami) Figurki wylądują w losowej pozycji, dając wynik. Punktacja zamieszczona jest w tabeli z punktami.  2. Wybierz dalsze działanie:  Naciśnij ponownie przycisk Rzucaj Gdy chcesz spróbować zdobyć więcej punktów w danej rundzie. 
 
Lub 
 
 a) Naciśnij przycisk Rezygnuj W celu zakończenia rundy i przekazania wirtualnych figurek kolejnemu graczowi. 
 
 
Przebieg gry: Gracz może w swojej kolejce rzucać tyle razy, ile tylko chce do momentu gdy: 
 
a) zadecyduje, że przerywa rzucanie i zapisuje sumę pkt. zdobytych w tej kolejce; 
 
Lub: 
 
b) wyrzucił układ "Dwa różne boczki" co oznacza, że zdobył w tej kolejce 0 pkt.; 
 
Lub: 
 
c) wyrzucił układ w którym figurki stykają się ze sobą i traci WSZYSTKIE punkty zdobyte od początku gry. 
 
 
# 3. Punktacja
 
Plecy 5 pkt. 
2 x Plecy 20 pkt. 
Nóżki 5 pkt. 
2 x Nóżki 20 pkt. 
Ryjek 10 pkt. 
2 x Ryjek 40 pkt. 
Uszko 15 pkt. 
2 x Uszko 60 pkt. 
Dwa różne boczki 0 pkt. 
2 x ten sam bokczek 1 pkt. 
Fig. stykają się Tracisz wszystkie pkt. 
Kombinacja ułożeń Suma pkt. obu figurek 
 
 
# 4. Protokół Pass The Pigs: 
 
Gra do komunikacji pomiędzy klientami a serwerem wykorzystuje specjalnie stworzony do tego celu protokół (plik PtpProtocol.java): 
 
CONNECTED_COMMAND informacja od serwera do klienta o nawiązaniu połączenia 
QUIT_COMMAND   informacja od klienta do serwera o opuszczeniu gry 
RESIGN_COMMAND   informacja od klienta do serwera o rezygnacji z tury 
NICK_COMMAND   informacja od klienta do serwera o podaniu nazwy użytkownika 
USERS_LIST_COMMAND informacja od serwera do klienta z listą graczy i ich punktami 
YOU_WON_COMMAND    informacja od serwera do klienta o wygranej grze 
YOU_LOST_ COMMAND   informacja od serwera do klienta o przegranej grze 
YOUR_TURN_COMMAND informacja od serwera do klienta o rozpoczęciu się tury gracza 
THROWS_COMMAND   informacja od serwera do klienta o wykonaniu rzutu przez gracza 
WAIT_COMMAND   informacja od serwera do klienta o tym, że nie ma wszystkich graczy 
JOINED_COMMAND  informacja od serwera do klienta o dołączeniu gracza do gry 
END_TURN_COMMAND  informacja od serwera do klienta o zakończeniu tury 
RESULT_COMMAND  informacja od serwera do klienta z rezultatem ostatniego rzutu 
TURN_LOST_COMMAND informacja od serwera do klienta o przegraniu ostatniej tury 
GOT_POINTS_COMMAND informacja od serwera do klienta o otrzymaniu punktów 
ELSE_POINTS_COMMAND informacja od serwera do klienta o otrzymaniu punktów przez innego gracza 
TURN_POINTS_COMMAND informacja od serwera do klienta o ilości punktów w całej turze 
TOUCHING_COMMAND  informacja od serwera do klienta o tym, że figurki się stykają 
BAD_LUCK_COMMAND  informacja od serwera do klienta, że gracz stracił kolejkę 
 
# 5. Serwer Pass The Pigs 
 
Serwer gry, w trakcie działania nie wymaga ingerencji administratora. Dlatego jego obsługa ogranicza się do wprowadzenia numeru portu, na którym będzie działał serwer oraz ilości graczy, którzy będą brali udział w grze (od 2 do 5 graczy).
Następnie serwer jest uruchamiany na wskazanym porcie. 
 
### Serwer Pass The Pigs składa się z następujących klas: 
 
* Server – klasa główna
* Gra – obiekt tej klasy to gra uruchomiona na serwerze 
* Rzut – obiekt tej klasy to rzut wykonany przez gracza; Uczestnik – dziedzicząca po Thread, obiekt tej klasy to uczestnik gry podłączony do serwera.    Obsługuje połączenia klientów i realizuje komunikację z nimi. 
 
## 5.1 Klasa Server: 
### Klasa składa się z następujących pól: 
 
• Obiekt  klasy ServerSocket, który przechowuje socket, na którym nasłuchuje serwer • int port – przechowuje numer portu, na którym nasłuchuje serwer 
• int maxIloscGraczy – przechowuje maksymalną ilość graczy w grze (do wprowadzenia    przez użytkownika); 
• Obiekt klasy Gra – przechowuje uruchomioną na serwerze grę. Klasa zawiera następujące metody: 

### Klasa zawiera następujące metody: 
• Server() – konstruktor. Przy tworzeniu nowego obiektu klasy Server wywoływane są    metody potrzebne do uruchomienia serwera (poniżej) oraz tworzony jest nowy    obiekt klasy Gra działającej na serwerze; 
• wprowadzNumerPortu() – prosi użytkownika o wprowadzenie numeru portu, na którym   będzie nasłuchiwał serwer. Następnie przypisuje tą wartość do zmiennej numerPortu; 
• wprowadzIloscGraczy() – prosi użytkownika o wprowadzenie maksymalnej ilości graczy w   grze dopóki ilość ta nie zostanie wprowadzona poprawnie. Następnie ustawia    zmienną maxIloscGraczy serwera na podaną liczbę; 
• ustanowPolaczenie() – uruchamia socket serwera na odpowiednim porcie i dopóki istnieje   połączenie, akceptuje połączenia klientów. 
 
## 3.2 Klasa Gra: 

### Klasa składa się z następujących pól: 
 
• Vector uczestnicy – przechowuje listę wszystkich uczestników w grze; 
• Uczestnik aktualny – przechowuje obiekt uczestnika, który aktualnie gra (jest jego kolejka); 
• Uczestnik pierwszyWKolejce – przechowuje obiekt uczestnika, który grał jako pierwszy. Od   niego zaczęła się gra i przy każdej jego kolejce gra będzie sprawdzała, czy nie    wyłonił się zwycięzca; 
• int poczatkowaMaxIloscGraczy – maksymalna ilość graczy, ustawiona przez użytkownika   przy uruchamianiu serwera. Jest ona potrzebna do prawidłowego resetu gry po jej   zakończeniu. Zawsze pozostaje taka sama; 
• int maxIloscGraczy – maksymalna ilość graczy. W trakcie gry zmienia się po to, aby nowi   gracze nie dołączyli do gry w czasie jej trwania; 
• boolean czyJestZwyciezca – informacja, czy wyłonił się już zwycięzca gry. 
 
### Klasa zawiera następujące metody: 
 
• Gra() - konstruktor. Obiekt klasy gra tworzony jest przez podanie maksymalnej ilości    graczy; • dodajUczestnika() – dodaje uczestnika do listy uczestników gry;
• podajIloscGraczy() – zwraca aktualną ilość graczy w grze (rozmiar listy); 
• podajMaxIlocGraczy() – zwraca aktualną maksymalną ilość graczy w grze; 
• ustawAktualnego() – wskazuje gracza, który będzie oznaczony, jako aktualnie grający; • podajAktualnego() – zwraca gracza, który aktualnie gra; 
• szukajZwyciezcy() – jest uruchamiana, aby wykonać analizę wyników gry pod kątem    wyłonienia zwycięzcy. Dzieje się to przy każdym powrocie do pierwszego gracza w   kolejce; 
• czySaMiejsca() – zwraca informację, czy do gry mogą dołączyć się nowi gracze; 
• czySaWszyscy() – zwraca informację, czy zapełnione są wszystkie miejsca; 
• wskazCzyJestZwyciezca() – zwraca informację, czy wyłoniony został zwycięzca; 
• przekazKolejke() – jest uruchamiana po zakończeniu tury gracza. Ustawia aktualnego gracza   na następnego w kolejce. Jeżeli kolejka dotarła do pierwszego gracza w kolejce i   dodatkowo wyłonił się już zwycięzca gry , metoda ta również analizuje wyniki i    wysyła do graczy informacje o przegranej lub wygranej; 
• zakonczTure() – kończy turę gracza; 
• opuscGre() – usuwa gracza z listy graczy. Aktualizuje też informacje o pierwszym graczu w   kolejce, oraz o aktualnym graczu, jeżeli opuszczający był któryś z nich;
• wyslijDoWszystkich() – wysyła łańcuch tekstowy do wszystkich graczy; 
• wyslijDoJednego() – wysyła łańcuch tekstowy do wskazanego gracza;
• wyslijDoInnych() – wysyła łańcuch tekstowy do wszystkich graczy poza wskazanym; 
• graj() – uruchamia turę gracza; 
• pokazPunkty() – wysyła do graczy listę podłączonych graczy oraz ich punktów; 
• ustawPierwszego() – aktualizuje informacje o pierwszym graczu w kolejce; 
• ustawMaxIloscGraczy() – jest używana w sytuacji, kiedy gracz opuści grę. Aktualizuje    maksymalną ilość graczy po to, aby nowi  nie dołączyli do gry w trakcie jej trwania; 
• podajListeUczestnikow() – zwraca listę zawierającą wszystkich graczy podłączonych do   gry. 
## 5.3 Klasa Rzut: 
 ### Klasa składa się z następujących pól: 
 
• Gra gra – obiekt gry w której zaistniał rzut; 
• Int punktyWRzucie – punkty zdobyte w aktualnym rzucie; 
• boolean stykajaSie – informacja, czy rzucone kostki stykają się; 
• boolean pechowy – informacja, czy wyrzucony został układ kończący turę; 
• String[] ulozeniaFigurek – tablica przechowująca ułożenia dwóch rzuconych figurek po   rzucie; 
• String[] mozliweUlozenia – tablica przechowująca wszystkie możliwe ułożenia figurki po   rzucie. 
 
### Klasa zawiera następujące metody: 
 
• Rzut() – konstruktor. Nowy rzut tworzony jest poprzez przypisanie go do odpowiedniej gry.   Następnie losowane jest ułożenie każdej figurki po jej rzuceniu oraz to, czy figurki   się stykają; 
• czyPechowy() – zwraca informację, czy rzut był pechowy (kończący turę); 
• podajUlozenie() – wysyła do graczy informację o ułożeniu figurek po rzucie; 
• podliczPunkty() – podlicza punkty zdobyte w rzucie i wysyła tą informację do graczy. 

## 3.4 Klasa Uczestnik: 
##  Klasa składa się z następujących pól: 
• Gra gra – gra, w której gra uczestnik; 
• String linia –tekst komunikatu od użytkownika (od klienta gry); 
• int wszystkiePunkty – wszystkie punkty gracza; • punktyWTurze – punkty zdobyte w aktualnej turze; 
• Socket socket – socket, na którym podłączony jest klient; 
• BufferedReader in – strumień wejścia danych; 
• PrintWriter out – strumień wyjścia danych; 
• String nick – nazwa użytkownika; • boolean zalogowany – informacja, czy użytkownik jest zalogowany w grze. 
 
### Klasa zawiera następujące metody: 
 
• Uczestnik() – konstruktor. Nowy obiekt klasy Uczestnik tworzony jest przez podanie gry, w   której uczestnik ma brać udział oraz socketu, na którym jest połączony; 
• przyznajPunkty() – dodaje punkty zdobyte w danej turze do wszystkich punktów gracza; • wyslijWiadomosc() – wysyła łańcuch tekstowy do tego gracza; 
• podajNick() – zwraca nazwę użytkownika; 
• podajWszystkiePunkty() – zwraca wszystkie punkty gracza; 
• ustawWzystkiePunkty() – aktualizuje wszystkie punkty gracza; 
• wyloguj() – oznacza gracza jako wylogowanego z gry; 
• run() – uruchamia komunikację z graczem i nadzoruje przebieg gry. 

6. Klient Pass The Pigs 
 
 
Pole serwer i port służą do wpisania parametrów serwera, na którym uruchomiona jest gra. Przyciski rozłącz i połącz służą do nawiązywania i zrywania tego połączenia. Przyciski rzucaj i rezygnuj są aktywne tylko wtedy, kiedy gra jest rozpoczęta i akurat trwa tura gracza połączonego poprzez tego klienta. Po lewej stronie wyświetlane są interpretacje komunikatów otrzymanych od serwera – opis przebiegu rozgrywki. Prawa strona okna programu zawiera listę połączonych graczy oraz ilość punktów, które zdobył każdy z nich. 
 
Klient Pass The Pigs składa się z klas: 
 
Client – klasa główna, dziedzicząca po JFrame; ObslugaZdarzen – klasa wewnętrzna odpowiedzialna za obsługę przycisków; Klient – klasa wewnętrzna, dziedzicząca po Thread, która jest odpowiedzialna za nawiązanie    połączenia z serwerem i przyjmowanie od niego komunikatów. 
 
 
 
 
# 6.Klient Pass The Pigs 
## 6.1 Klasa Client: 
### Klasa składa się z następujących pól: 
 
• String MESSAGE_PREFIX – jest to stała przechowująca prefiks dla informacji (tekstów),   które nie pochodzą z serwera a mają być wyświetlone w oknie gry; 
• JButton rzucaj – przycisk służący do wykonania rzutu w grze; 
• JPanel panelDol – panel zawierający przyciski Rzucaj i Rezygnuj; 
• JPanekGora – panel zawierający pola z adresem i numerem portu serwera oraz przyciski   Połącz i Rozłącz; 
• JTextField host – pole do wpisania adresu serwera; 
• JTextArea komunikaty – pole, które wyświetla przebieg rozgrywki; 
• JList zalogowani – wyświetla listę zalogowanych użytkowników razem z ich punktami;
• DefaultListModel listaZalogowanych – zalogowanych użytkowników razem z ich    punktami; 
• String nazwaSerwera – przechowuje adres serwera; 
• Boolean polaczony – zawiera informację, czy gracz jest połączony z serwerem; 
• Klient WatekKlienta – przechowuje aktualnie uruchomiony wątek klienta. 
 
### Klasa zawiera następujące metody: 
 
• Client() – konstruktor. Podczas tworzenia nowego obiektu klasy klient, tworzone są    elementy graficzne i umieszczane są na odpowiednich miejscach w oknie gry; 
• obsluzKomunikat() – obsługuje komunikaty odbierane od serwera i wykonuje odpowiednie   działania, które są z nimi związane. Wyświetla także informacje na ekranie. 
## 6.2 Klasa ObsługaZdarzeń: 
 Klasa zawiera jedynie metodę actionPerformed(), która jest odpowiedzialna za przypisanie odpowiednich akcji do przycisków. 
 
## 6.3 Klasa Klient: 
### Klasa zawiera następujące pola: 
• Socket socket – przechowuje socket, na którym istnieje połączenie z serwerem; 
• BuffererReader wejście – strumień danych wejściowych; 
• PrintWriter wyjście – strumień danych wyjściowych. 
 
### Klasa zawiera następujące metody: 
• run() – uruchamia komunikację z serwerem i nadzoruje przepływ komunikatów; 
• wyślij() –wysyła komunikat do serwera. 
