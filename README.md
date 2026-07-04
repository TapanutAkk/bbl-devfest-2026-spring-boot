# BBL Tech Dev Fest 2026 — Java Spring Boot

เว็บแอปตัวอย่างสร้างด้วย Spring Boot 3.5 มี REST API 2 ชุด:

- **`/users`** — User management API ตามโจทย์ Backend API Development Test (เก็บข้อมูล **in-memory** ไม่ใช้ฐานข้อมูล มีข้อมูลตัวอย่าง 3 คนตอนเริ่มแอป)
- **`/api/items`** — Items API พร้อมหน้าเว็บสำหรับเพิ่ม/ลบรายการ ใช้ฐานข้อมูล H2 แบบฝังในตัว (ไม่ต้องติดตั้งอะไรเพิ่ม) และรองรับ PostgreSQL ผ่าน Docker Compose

## สิ่งที่ต้องมี (Prerequisites)

| เครื่องมือ | เวอร์ชัน | หมายเหตุ |
|---|---|---|
| Java (JDK) | 17 ขึ้นไป | เช็คด้วย `java -version` |
| Maven | ❌ ไม่ต้องติดตั้ง | ใช้ Maven Wrapper (`./mvnw`) ที่มากับโปรเจกต์ ดาวน์โหลดเองอัตโนมัติ |
| Docker | ไม่บังคับ | ใช้เฉพาะกรณีอยากรันด้วย PostgreSQL |

## ตั้งค่า (ครั้งแรกครั้งเดียว)

ค่า config ทั้งหมด (port, database) อยู่ในไฟล์ `.env` ที่ root ของโปรเจกต์ — ถ้ายังไม่มีให้ copy จาก template:

```bash
cp .env.example .env
```

ค่า default ใช้งานได้ทันทีโดยไม่ต้องแก้อะไร (port 8080 + H2)

## วิธีรัน (แบบง่ายสุด)

```bash
./mvnw spring-boot:run
```

รอจนขึ้น `Started DevfestApplication` แล้วเปิด:

- **หน้าเว็บ**: http://localhost:8080
- **API**: http://localhost:8080/api/items
- **Health check**: http://localhost:8080/actuator/health

ข้อมูลถูกเก็บเป็นไฟล์ H2 ในโฟลเดอร์ `./data/` (สร้างอัตโนมัติ ปิดแอปแล้วข้อมูลไม่หาย)

> ถ้าเจอ `Permission denied` ให้รัน `chmod +x mvnw` ก่อนหนึ่งครั้ง

## แก้ไขหน้าเว็บ (HTML/CSS/JS) แล้วต้องรันอะไร?

ไฟล์หน้าเว็บอยู่ที่ `src/main/resources/static/index.html` — โดยปกติ Spring Boot เสิร์ฟไฟล์จากตัวที่ compile ไปแล้ว (`target/classes`) **การแก้ไฟล์ใน `src` เฉย ๆ จะยังไม่เห็นผลบนเว็บ** มี 2 วิธี:

**วิธีที่ 1: restart แอป (ตรงไปตรงมา)**

```bash
# กด Ctrl+C ใน terminal ที่รันแอปอยู่ แล้วรันใหม่
./mvnw spring-boot:run
```

**วิธีที่ 2 (แนะนำตอน dev): รันด้วยโหมด addResources — แก้ HTML แล้วแค่ refresh browser**

```bash
./mvnw spring-boot:run -Dspring-boot.run.addResources=true
```

โหมดนี้ Spring จะเสิร์ฟไฟล์จาก `src/main/resources` ตรง ๆ แก้ HTML/CSS/JS เสร็จกด refresh (`Cmd+R`) เห็นผลทันที ไม่ต้อง restart

> ⚠️ ใช้ได้กับไฟล์ static เท่านั้น — ถ้าแก้โค้ด **Java** ต้อง restart แอปเสมอ (ไม่ว่าโหมดไหน)

## วิธีรันเทส

```bash
./mvnw test                                       # รันเทสทั้งหมด
./mvnw test -Dtest=UserControllerTest             # รันทีละคลาส
./mvnw test -Dtest=UserControllerTest#createReturns201   # รันทีละเมธอด
```

มีเทส 3 คลาส: `UserControllerTest` (MockMvc + mock service), `UserServiceTest` (service layer ตรง ๆ), `ItemControllerTest` — ทั้งหมดไม่ต้องใช้ฐานข้อมูลหรือ Docker ใด ๆ

## Users API (โจทย์ Backend Test)

จัดการข้อมูล user (โครงสร้างอ้างอิงจาก [jsonplaceholder.typicode.com/users](https://jsonplaceholder.typicode.com/users)) — เก็บ **in-memory** ปิดแอปแล้วข้อมูลรีเซ็ตกลับเป็น 3 คนตัวอย่าง

| Method | Path | คำอธิบาย | Response |
|---|---|---|---|
| GET | `/users` | รายชื่อ user ทั้งหมด | 200 |
| GET | `/users/{userId}` | user รายคน | 200 / 404 |
| POST | `/users` | สร้าง user ใหม่ | 201 / 400 |
| PUT | `/users/{userId}` | แก้ไข user เดิม | 200 / 400 / 404 |
| DELETE | `/users/{userId}` | ลบ user | 204 / 404 |

ฟิลด์ของ user: `id`, `name`, `username`, `email`, `phone`, `website` — โดย `name`, `username`, `email` **ห้ามว่าง** (ส่งไม่ครบได้ 400 พร้อมข้อความบอกว่า field ไหนผิด) และ `email` ต้องเป็นรูปแบบอีเมลที่ถูกต้อง

```bash
# ดู user ทั้งหมด
curl http://localhost:8080/users

# ดู user รายคน
curl http://localhost:8080/users/1

# สร้าง user ใหม่ (201)
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Patricia Lebsack","username":"Karianne","email":"Julianne.OConner@kory.org","phone":"493-170-9623","website":"kale.biz"}'

# แก้ไข user (200)
curl -X PUT http://localhost:8080/users/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Leanne G.","username":"Bret","email":"leanne@example.com"}'

# ลบ user (204)
curl -X DELETE http://localhost:8080/users/1

# ตัวอย่าง validation error (400)
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"phone":"123"}'
# → {"status":400,"error":"Bad Request","messages":["email must not be blank","name must not be blank","username must not be blank"],...}
```

## ทดสอบ API ผ่าน VSCode (ง่ายกว่า curl)

ที่ root โปรเจกต์มีไฟล์ [api-tests.http](api-tests.http) รวม request ของทั้ง Users API และ Items API ไว้ครบ (GET/POST/PUT/DELETE รวมเคส 404, 400)

1. ติดตั้ง VSCode extension **REST Client** (id: `humao.rest-client`) — ตัวเดียวพอ
2. รันแอปให้ทำงานอยู่ (`./mvnw spring-boot:run`)
3. เปิดไฟล์ `api-tests.http` — เหนือแต่ละ request จะมีลิงก์ **"Send Request"** กดเพื่อยิงแล้วดู response (status + JSON) เป็น tab ใหม่ทันที ไม่ต้องพิมพ์ curl เอง

## Items API (ทดลองยิง)

```bash
# ดูรายการทั้งหมด
curl http://localhost:8080/api/items

# เพิ่มรายการใหม่
curl -X POST http://localhost:8080/api/items \
  -H 'Content-Type: application/json' \
  -d '{"name":"my first item"}'

# ดูรายการตาม id
curl http://localhost:8080/api/items/1

# ลบรายการ
curl -X DELETE http://localhost:8080/api/items/1
```

| Method | Path | คำอธิบาย | Response |
|---|---|---|---|
| GET | `/api/items` | รายการทั้งหมด | 200 |
| GET | `/api/items/{id}` | รายการเดียว | 200 / 404 |
| POST | `/api/items` | เพิ่มรายการ (`{"name":"..."}` ห้ามว่าง) | 201 / 400 |
| DELETE | `/api/items/{id}` | ลบรายการ | 204 / 404 |

## วิธีรันด้วย Docker + PostgreSQL (ทางเลือก)

ถ้าอยากรันทั้งระบบใน container (แอป + PostgreSQL) โดยไม่ใช้ Java ในเครื่อง:

```bash
docker compose up -d --build   # build + รันแอปที่ :8080 และ Postgres ที่ :5432
docker compose logs -f app     # ดู log ของแอป
docker compose down            # ปิดทั้งหมด (เติม -v ถ้าต้องการล้างข้อมูลใน DB ด้วย)
```

Compose จะตั้งค่า `SPRING_DATASOURCE_*` ให้แอปชี้ไป PostgreSQL อัตโนมัติ — ไม่ต้องแก้โค้ดหรือ config ใด ๆ (username/password ของ Postgres ก็อ่านจาก `.env` ไฟล์เดียวกัน)

หรือถ้าอยากรันแอปในเครื่องแต่ใช้ PostgreSQL ใน Docker: รัน `docker compose up -d db` แล้วสลับ comment ส่วน database ใน `.env` ตามที่เขียนไว้ในไฟล์

## CI/CD

มี GitHub Actions workflow ที่ [.github/workflows/ci.yml](.github/workflows/ci.yml) — ทุก push/PR จะ:

1. Build + รันเทสทั้งหมดด้วย `./mvnw clean verify` (JDK 17, cache Maven dependencies)
2. Build Docker image จาก `Dockerfile` เพื่อยืนยันว่า containerize ได้ (ขั้น push ไป registry / deploy คอมเมนต์ไว้เป็นตัวอย่าง)

## โครงสร้างโปรเจกต์

```
src/main/java/com/bbl/devfest/
├── controller/   # REST endpoints (+ GlobalExceptionHandler สำหรับ validation error)
├── service/      # business logic (UserService = in-memory store, ItemService = JPA)
├── repository/   # Spring Data JPA (เฉพาะ items)
├── model/        # User (record ธรรมดา) + Item (JPA entity)
└── dto/          # request DTOs + validation
src/main/resources/
├── application.yml      # config (default H2, override ด้วย env vars)
└── static/index.html    # หน้าเว็บ
src/test/java/           # เทส (MockMvc + service tests, ไม่ต้องใช้ DB)
.github/workflows/ci.yml # CI: build + test + docker build
api-tests.http           # ทดสอบ API ผ่าน VSCode REST Client extension
```

## แก้ปัญหาที่พบบ่อย

- **Port 8080 ถูกใช้อยู่** — อาจมีแอปตัวเก่ารันค้าง หา process ด้วย `lsof -i :8080` แล้ว kill ก่อน
- **อยากล้างฐานข้อมูล H2** — ปิดแอปแล้วลบโฟลเดอร์ `./data/`
- **แก้ entity แล้ว schema เพี้ยน** — โปรเจกต์ใช้ `ddl-auto: update` ยังไม่มี migration tool ให้ลบ `./data/` เพื่อเริ่มใหม่
