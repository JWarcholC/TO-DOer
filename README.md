# TO-DOer

## Opis

Aplikacja ma za zadanie uprościć organizację zadań. Są one przechowywane na zdalnym serwerze(`Firebase`). Zadania(`TO-DO`) po skończeniu są oznaczane jako `DONE` i nie wyświetlają się więcej na liście. 
Aplikacja umożliwia również współdzielenie zadań z innymi osobami.

### Dla developerów

W Trello dzielimy projekt na podzadania. Po dodaniu nowego, pod jego nazwą pojawia się numer (np #1)

![alt TO-DO list](https://firebasestorage.googleapis.com/v0/b/to-do-pwsz.appspot.com/o/Zrzut%20ekranu%20z%202019-10-10%2010-34-24.png?alt=media&token=d996d81b-171d-4751-b069-7fbf3c0db92b)

Biorąc zadanie na siebie, przenosimy je do kategorii `In progress`, po czym tworzymy u siebie branch o nazwie feature-numer_zadania
(np. dla zadania 'podpięcie firebase do aplikacji' o numerze #1, tworzymy branch feature-1). 
Gdy zadanie uznajemy za skończone, pushujemy je na GitHuba.
> Nazwa brancha jest ważna ze względu na **pull request**!

*Pull request* jest to sprawdzenie dodanej funkcjonalności przez innego członka zespołu. Gdy ten nie znajdzie żadnych błędów, dokonuje pull request na branch `develop`, klikając na *zielony przycisk*:

![alt Pull request](https://help.github.com/assets/images/help/repository/req-status-check-all-passed.png)

I od tej chwili zmiany z brancha feauture są dostępne na `develop'ie`. Pozostaje jeszcze na Trello przesunąć zadanie ma `DONE`
Na *GitHub'ie* można zdefiniować reguły dla branchy i właśnie dla gałęzi `feature-*` został nałożony obowiązek pull requestów. 
