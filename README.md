# room-booking-flavors

Одна предметна область і п'ять архітектур. Поведінка однакова: `scripts/smoke.sh` проганяє той самий сценарій
на всіх п'яти й порівнює вивід з `scripts/expected.txt`. Відрізняється лише те, як код розкладено й куди дивляться залежності.

## Функціонал

```
rooms                                                   список аудиторій
book <ауд> <yyyy-MM-ddTHH:mm> <хв> <хто> <осіб> [projector]
cancel <id> <хто>                                       скасувати може лише автор
list [ауд]                                              усі бронювання або однієї аудиторії
exit
```

Аудиторії: `101` (30 місць, проєктор), `214` (25, без проєктора), `305` (15, проєктор).

Правила бронювання, у такому порядку:

1. Аудиторія існує.
2. Не в минулому.
3. Тривалість від 1 до 180 хв.
4. Кількість осіб додатна.
5. Не більше, ніж місткість аудиторії.
6. Якщо потрібен проєктор — він в аудиторії є.
7. Бронювання однієї аудиторії не перетинаються (впритул — можна).

Скасування: бронювання має існувати; скасувати може лише його автор.

## П'ять смаків

| Модуль | Стиль | Чому саме він | Вхід |
|---|---|---|---|
| `layered` | Класичні 3 шари, by layer | Треба швидко й просто; правил мало | CLI |
| `layered-by-feature` | Шари всередині фіч | Фічі ростуть і змінюються незалежно; межі стереже компілятор | CLI |
| `layered-ddd` | 4 шари Еванса, інвертована інфраструктура | Правил стало багато, їх треба тестувати без файлу | CLI |
| `hexagonal` | Порти й адаптери | Кілька входів (CLI, HTTP) і кілька сховищ (CSV, пам'ять) | CLI, HTTP |
| `clean` | Clean Architecture | Багато use case і різні формати виводу (текст, JSON) | CLI, HTTP (JSON) |

### 1. `layered` — класичні шари

**Запит:** «Кафедрі потрібна проста система бронювання до понеділка».

```
model/         Room, Booking — анемічні записи, спільні для всіх шарів
presentation/  ConsoleUi
business/      BookingService (усі правила), RoomService (sinkhole)
data/          BookingDao (CSV), RoomDao, EmailSender
```

- Плюси: найменше коду, зрозуміло з першого погляду.
- Ціна: сервіс сам створює DAO і бере `LocalDateTime.now()` — правила не протестувати без файлу й реального часу.
  `RoomService.listRooms()` — architecture sinkhole. Модель у зоні болю (I = 0, A = 0).
- Тест архітектури: шари закриті (presentation не імпортує data).

### 2. `layered-by-feature` — шари всередині фіч

**Запит:** «Аудиторіями займається одна людина, бронюваннями — інша, і вони постійно конфліктують у спільних пакетах».

```
room/          Room, RoomCatalog (публічний API), RoomCommands, RoomRepository (package-private)
booking/       BookingCommands (єдиний публічний клас), BookingService, BookingRepository, Booking
notification/  Mailer
shell/         ConsoleUi — віддає команду потрібній фічі
```

- Плюси: зміна в бронюваннях не торкається аудиторій (CCP). Внутрішні класи фіч package-private,
  тож обійти сервіс і піти в репозиторій напряму не дасть компілятор.
- Ціна: усередині фічі — ті самі класичні шари з тими самими проблемами тестування.
- Тест архітектури: `room` нічого не знає про `booking`.

### 3. `layered-ddd` — чотири шари Еванса з інвертованою інфраструктурою

**Запит:** «Правил уже сім, вони плутаються, а кожна помилка в розкладі — скарга від деканату».

```
ui/              ConsoleUi
application/     BookingApplicationService — тонкий сценарій; Notifier
domain/          Booking, Room — правила тут; BookingRepository, RoomRepository — інтерфейси
infrastructure/  CsvBookingRepository, SeedRoomRepository, ConsoleMailer
```

- Плюси: правила в домені (`Booking.create`, `Room.checkCanHost`, `checkCanBeCancelledBy`),
  тестуються з фейковими репозиторіями й фіксованим годинником.
- Ціна: більше класів; домен містить інтерфейси сховища.
- Тест архітектури: domain не знає нікого; application не знає infrastructure і ui.

### 4. `hexagonal` — порти й адаптери

**Запит:** «Деканату потрібен сайт, адмін лишається в консолі. А ІТ-відділ попередив: CSV скоро замінять на базу».

```
domain/                  Booking, Room, BookingException
application/port/in/     BookRoomUseCase, CancelBookingUseCase, ListBookingsUseCase, ListRoomsUseCase
application/port/out/    BookingRepository, RoomRepository, Notifier
application/             BookingService, RoomQueryService
adapter/in/cli|http      ConsoleUi, HttpBookingController
adapter/out/persistence  CsvBookingRepository, InMemoryBookingRepository, SeedRoomRepository
adapter/out/notification ConsoleEmailNotifier
```

- Плюси: новий вхід чи нове сховище — новий адаптер, ядро не змінюється.
- Ціна: удвічі більше файлів, ніж у класичних шарах.
- Тест архітектури: application не знає адаптерів; вхідні адаптери бачать лише вхідні порти.

### 5. `clean` — Clean Architecture

**Запит:** «Сайту потрібен JSON, боту — текст. Use case додаються щомісяця, і над ними працює нова команда».

```
entity/                     Booking, Room — критичні бізнес-правила
usecase/<use case>/         InputBoundary (+ Request), OutputBoundary (+ Response), Interactor
usecase/gateway/            BookingGateway, RoomGateway, Notifier
adapter/cli/                CliController, CliPresenter (текст)
adapter/http/               HttpController, JsonPresenter (JSON)
adapter/gateway/            CsvBookingGateway, InMemoryBookingGateway, SeedRoomGateway
framework/                  Main, ConsoleLoop, WebServer, UseCases
```

- Плюси: один use case — одна папка (screaming architecture). Use case не повертає результат, а віддає
  його presenter-у, тож формат виводу змінюється без зміни use case. Controller і presenter не бачать entity.
- Ціна: найбільше файлів і церемонії; для CRUD це надмірно.
- Тест архітектури: entity ← usecase ← adapter ← framework; `adapter/cli` і `adapter/http` не імпортують entity.

## Запуск

Потрібні JDK 21; Maven — лише для тестів.

```bash
bash scripts/smoke.sh      # той самий сценарій на всіх п'яти — без Maven
bash scripts/metrics.sh    # Ca, Ce, I, A, D і цикли для кожного смаку
mvn test                   # unit-тести правил і тести архітектури

mvn -q compile
java -cp layered/target/classes ua.edu.booking.App
java -cp hexagonal/target/classes ua.edu.booking.App http memory     # HTTP на :8080
java -cp clean/target/classes ua.edu.booking.framework.Main http      # HTTP з JSON
```

HTTP (hexagonal, clean):

```bash
curl localhost:8080/rooms
curl -X POST "localhost:8080/bookings?room=214&start=2027-03-03T10:00&minutes=90&who=Petrenko&attendees=20"
curl "localhost:8080/bookings?room=214"
curl -X POST "localhost:8080/bookings/cancel?id=1&who=Petrenko"
```

## Порівняння (вивід `scripts/metrics.sh`)

| Смак | Файлів | Рядків | Тестів | Найстабільніший компонент | I | A | D |
|---|---|---|---|---|---|---|---|
| layered | 10 | 243 | 4 | model | 0.00 | 0.00 | 1.00 |
| layered-by-feature | 12 | 277 | 3 | room | 0.00 | 0.25 | 0.75 |
| layered-ddd | 12 | 303 | 14 | domain | 0.00 | 0.40 | 0.60 |
| hexagonal | 21 | 519 | 14 | domain / port.out | 0.00 / 0.22 | 0.00 / 1.00 | 1.00 / 0.22 |
| clean | 33 | 721 | 14 | entity / usecase.gateway | 0.00 / 0.17 | 0.00 / 1.00 | 1.00 / 0.17 |

Рядки — непорожні рядки `src/main`. Циклів немає в жодному смаку.
