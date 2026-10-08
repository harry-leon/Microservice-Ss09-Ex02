# Ss09-Ex02 - Cập nhật giá thuốc "Nóng" không Restart (Dynamic Refresh)

## 1. Mục tiêu
- **Kiến thức**: Hiểu cơ chế làm mới cấu hình trong bộ nhớ bằng annotation `@RefreshScope` của Spring Cloud Context.
- **Kỹ năng**: Sử dụng Spring Boot Actuator (`/actuator/refresh`) để cập nhật tham số thuế VAT (`pharmacy.vat-rate`) mà không làm gián đoạn hệ thống hoặc phải khởi động lại (restart) server bán thuốc.

---

## 2. Kiến trúc giải pháp
- **`config-server`** (Port: `8888`): Spring Cloud Config Server kết nối tới Git repository chứa cấu hình tập trung.
- **`config-repo`**: Thư mục chứa cấu hình tập trung (`pharmacy-service.properties`) với thuộc tính `pharmacy.vat-rate`.
- **`pharmacy-service`** (Port: `8081`): Microservice xử lý tính tiền hóa đơn thuốc:
  - Tích hợp `spring-cloud-starter-config` để lấy cấu hình từ Config Server.
  - Tích hợp `spring-boot-starter-actuator` và mở endpoint `/actuator/refresh`.
  - `@RefreshScope` được đặt trên `BillController` để tái tạo bean khi nhận được sự kiện refresh.

---

## 3. Cấu hình chi tiết

### 3.1. pharmacy-service.properties (trong Git/config-repo)
```properties
pharmacy.vat-rate=0.08
```

### 3.2. application.properties (pharmacy-service)
```properties
spring.application.name=pharmacy-service
server.port=8081
spring.config.import=optional:configserver:http://localhost:8888
pharmacy.vat-rate=0.08
management.endpoints.web.exposure.include=health,info,refresh
management.endpoint.health.show-details=always
```

---

## 4. API Endpoints

### 4.1. Tính tiền hóa đơn thuốc
- **Endpoint**: `POST /api/v1/bill`
- **Request Body**:
```json
{
  "totalMedicineAmount": 100000
}
```
- **Công thức**: `Tổng tiền = totalMedicineAmount + (totalMedicineAmount * vatRate)`
- **Response**:
```json
{
  "totalMedicineAmount": 100000,
  "vatRate": 0.08,
  "vatAmount": 8000.00,
  "finalAmount": 108000.00,
  "message": "Bill calculated successfully with current VAT rate: 8.0%"
}
```

### 4.2. Kiểm tra tỷ lệ thuế VAT hiện tại
- **Endpoint**: `GET /api/v1/bill/vat-rate`
- **Response**:
```json
{
  "vatRate": 0.08,
  "vatRatePercent": "8.0%"
}
```

### 4.3. Cập nhật cấu hình "Nóng" (Hot Reload)
- **Endpoint**: `POST http://localhost:8081/actuator/refresh`
- **Header**: `Content-Type: application/json`
- **Response**: Danh sách các thuộc tính vừa được refresh (ví dụ: `["pharmacy.vat-rate"]`).

---

## 5. Hướng dẫn chạy và kiểm thử

### Bước 1: Khởi động Config Server
```bash
cd config-server
mvn spring-boot:run
```

### Bước 2: Khởi động Pharmacy Service
```bash
cd pharmacy-service
mvn spring-boot:run
```

### Bước 3: Tính tiền ban đầu (VAT = 8%)
```bash
curl -X POST http://localhost:8081/api/v1/bill \
  -H "Content-Type: application/json" \
  -d '{"totalMedicineAmount": 100000}'
```
Kết quả trả về tổng tiền là `108000.00`.

### Bước 4: Cập nhật VAT trên Git và gọi Refresh
1. Thay đổi giá trị trên Git / repo thành `pharmacy.vat-rate=0.10` và commit/push lên Git.
2. Gọi Actuator refresh:
```bash
curl -X POST http://localhost:8081/actuator/refresh
```

### Bước 5: Kiểm tra lại hóa đơn thuốc
```bash
curl -X POST http://localhost:8081/api/v1/bill \
  -H "Content-Type: application/json" \
  -d '{"totalMedicineAmount": 100000}'
```
Kết quả trả về tổng tiền ngay lập tức là `110000.00` với VAT mới 10% mà **không cần khởi động lại server**.
