# Spec: Spatial Corner HUD for XREAL Air

## 1. Concept

แอพสำหรับ XREAL Air ที่แสดง “หน้าต่างเล็กแบบ HUD” ลอยอยู่ที่มุมขวาบนของมุมมองแว่น

ใช้สำหรับแสดงข้อมูลเสริม เช่น

* เวลา
* Battery
* Notification
* AI Assistant
* Subtitle
* Chat overlay
* Mini browser
* System monitor

เป้าหมายคือให้ผู้ใช้ยังดูจอหลักได้ตามปกติ แต่มีจอเล็กอยู่มุมขวาบนเสมอ

---

## 2. Target Platform

### MVP

* Android Phone
* XREAL Air / Air 2
* XREAL SDK / NRSDK
* Native Android หรือ Unity

### Optional

* Flutter สำหรับหน้าตั้งค่า
* Unity สำหรับ Render Spatial HUD
* Android Native สำหรับ Screen Capture / Notification / Service

---

## 3. Main Features

### 3.1 Floating Spatial HUD

แสดงหน้าต่าง HUD ขนาดเล็กที่มุมขวาบนของแว่น

ค่าพื้นฐาน:

* Position: top-right
* Size: 20–30% ของมุมมอง
* Opacity: 80–100%
* Distance: 1–1.5 เมตรจากผู้ใช้
* Mode: Head-locked

---

### 3.2 HUD Modes

#### Mode 1: Head Locked

HUD ติดตามหัวผู้ใช้ตลอดเวลา

เหมาะกับ:

* Notification
* AI Chat
* Clock
* Battery

#### Mode 2: World Locked

HUD ล็อกอยู่กับตำแหน่งในโลกจริง

เหมาะกับ:

* Workspace
* Multi monitor
* Dashboard

#### Mode 3: Smooth Follow

HUD ตามหัวแบบหน่วงเล็กน้อย

เหมาะกับ:

* ใช้งานนาน
* ลดอาการเวียนหัว

---

## 4. MVP Features

### 4.1 Clock Widget

แสดงเวลา วันที่ และ timezone

ตัวอย่าง:

```text
14:32
Thu, 2 Jul
```

---

### 4.2 Battery Widget

แสดงแบตมือถือ

```text
Phone 82%
```

---

### 4.3 Notification Widget

แสดง notification ล่าสุดจากมือถือ

```text
LINE
New message from Boss
```

ต้องใช้ Android Notification Listener Service

---

### 4.4 AI Assistant Widget

ผู้ใช้สามารถเปิดกล่อง AI แบบเล็ก ๆ ได้

ฟีเจอร์ MVP:

* พิมพ์คำถามจากมือถือ
* แสดงคำตอบบนแว่น
* รองรับ OpenAI / OpenRouter / Ollama ภายหลัง

---

### 4.5 HUD Settings

ตั้งค่าจากมือถือ:

* เปิด/ปิด HUD
* เลือกตำแหน่ง
* ปรับขนาด
* ปรับ opacity
* เลือก mode
* เลือก widget ที่จะแสดง

---

## 5. Screen Layout

```text
XREAL View

┌────────────────────────────────────┐
│                                    │
│                         ┌────────┐ │
│                         │ 14:32  │ │
│                         │ 🔋82%  │ │
│                         │ LINE   │ │
│                         └────────┘ │
│                                    │
│                                    │
└────────────────────────────────────┘
```

---

## 6. App Architecture

```text
Android App
│
├── Settings UI
│   └── Flutter / Native Android
│
├── Background Services
│   ├── Notification Listener
│   ├── Battery Monitor
│   └── AI Client
│
├── HUD Data Layer
│   └── State Stream / WebSocket / Event Bus
│
└── Spatial Renderer
    ├── XREAL SDK / NRSDK
    ├── Unity Scene
    └── HUD Canvas
```

---

## 7. Recommended Tech Stack

### Option A: Fastest Prototype

* Unity
* XREAL SDK
* Android Plugin
* C# UI Canvas

เหมาะกับทดสอบ HUD เร็วที่สุด

---

### Option B: Production App

* Flutter: Settings UI
* Android Native Kotlin: Services
* Unity as Library: Spatial HUD
* XREAL SDK: Spatial rendering

เหมาะกับแอพจริง

---

## 8. Core Components

### 8.1 HUD Renderer

หน้าที่:

* Render panel
* Update position
* Follow head pose
* Apply opacity
* Animate show/hide

---

### 8.2 Widget Manager

หน้าที่:

* จัดการ widget
* เปิด/ปิด widget
* เรียงลำดับข้อมูล
* จำกัดจำนวน notification

---

### 8.3 Notification Service

หน้าที่:

* อ่าน notification
* Filter app
* ส่งข้อมูลเข้า HUD

Permission:

* Notification Access

---

### 8.4 AI Client

หน้าที่:

* ส่ง prompt ไปยัง AI provider
* รับคำตอบ
* แสดงใน HUD

Provider:

* OpenAI
* OpenRouter
* Ollama
* Local API

---

## 9. User Flow

### First Launch

1. เปิดแอพบนมือถือ
2. ขอ permission ที่จำเป็น
3. ต่อ XREAL Air
4. เปิด Spatial HUD
5. HUD แสดงที่มุมขวาบน

---

### Change Position

1. เปิด Settings
2. เลือก HUD Position
3. เลือก Top Right
4. HUD ขยับตำแหน่งทันที

---

### Receive Notification

1. มือถือได้รับ notification
2. Notification Service อ่านข้อมูล
3. ส่งเข้า Widget Manager
4. HUD แสดง popup สั้น ๆ
5. Popup หายหลัง 5 วินาที

---

## 10. Permissions

Android permissions ที่ต้องใช้:

```text
Notification Access
Foreground Service
Internet
Bluetooth / USB optional
Media Projection optional
```

ถ้ามี screen capture:

```text
MediaProjection
Foreground Service Media Projection
```

---

## 11. MVP Scope

### Included

* HUD มุมขวาบน
* Clock
* Battery
* Notification
* Basic AI box
* Settings
* Head-locked mode
* Opacity / size setting

### Not Included ใน MVP

* Screen capture
* Multi monitor
* Full browser
* Voice control
* Hand tracking
* Eye tracking
* Drag HUD ด้วย gesture

---

## 12. Version Plan

### Version 1

* Android + XREAL Air
* Top-right HUD
* Clock / Battery / Notification
* Basic settings

### Version 2

* AI Assistant
* Subtitle overlay
* Chat overlay
* Smooth follow mode
* Widget customization

### Version 3

* Mini browser
* Screen capture preview
* Discord / Telegram / LINE integration
* Voice command

### Version 4

* Spatial multi-window
* Multi monitor
* World-locked workspace
* Cloud sync settings

---

## 13. Risks

### 13.1 XREAL SDK Limitation

บางฟีเจอร์อาจขึ้นกับรุ่นแว่นและ SDK

แนวทางลดความเสี่ยง:

* เริ่มจาก Unity + XREAL SDK
* ทำ prototype HUD ก่อน
* อย่าเริ่มจาก Flutter ล้วน

---

### 13.2 Battery Drain

HUD + background service อาจกินแบต

แนวทางแก้:

* จำกัด FPS ของ HUD
* Update เฉพาะตอนข้อมูลเปลี่ยน
* ใช้ dark UI
* ปิด animation ที่ไม่จำเป็น

---

### 13.3 Motion Sickness

HUD ที่ติดหัวเกินไปอาจทำให้เวียนหัว

แนวทางแก้:

* เพิ่ม Smooth Follow
* ลดขนาด HUD
* ตั้ง opacity
* ให้ผู้ใช้ปรับตำแหน่งเอง

---

## 14. Success Criteria

MVP ถือว่าสำเร็จเมื่อ:

* ต่อ XREAL Air แล้วเห็น HUD ได้
* HUD อยู่มุมขวาบน
* HUD ไม่บังจอหลักมากเกินไป
* แสดงเวลาและแบตได้
* แสดง notification ได้
* ปรับขนาดและ opacity ได้
* ใช้งานต่อเนื่อง 30 นาทีโดยไม่ crash

---

## 15. Suggested App Name

ชื่อที่เหมาะ:

* AirHUD
* Spatial Peek
* CornerGlass
* XPanel
* FloatLens
* SideHUD
* GlassDock

ชื่อแนะนำ: **AirHUD**
