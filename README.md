<div align="center">

  <img src="GoClas_logo.jpeg" alt="GoClass Logo" width="180"/>

  # GoClass (Go-Classroom Call System)
  **Sistem Panggilan Darurat Kelas dan Manajemen Guru Real-Time Berbasis Android**

  ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
  ![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2023%20--%2034-3DDC84?style=for-the-badge&logo=android&logoColor=white)
  ![Firebase](https://img.shields.io/badge/Firebase-Realtime%20Database-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
  ![Material Design](https://img.shields.io/badge/Material%20Design-3-757575?style=for-the-badge&logo=materialdesign&logoColor=white)

  <p align="center">
    <b>Solusi otomatisasi panggilan guru piket/pembimbing langsung mendobrak layar HP dari kelas secara instan.</b>
  </p>

</div>

---

## Tentang Proyek

GoClass adalah aplikasi Android native yang dibangun menggunakan Kotlin dan Firebase Realtime Database. Aplikasi ini memecahkan masalah keterlambatan respon guru saat berada di luar kelas melalui sistem panggilan darurat real-time.

Dengan memanfaatkan Foreground Service dan Full-Screen Intent, GoClass mampu membangunkan layar HP Guru yang sedang dalam kondisi terkunci/mati secara otomatis ketika ada sinyal panggilan dari siswa.

---

## Fitur-Fitur Utama

### Panel Guru (DashboardGuruActivity)
* **Satpam Latar Belakang (PemantauPanggilanService):** Berjalan secara mandiri di latar belakang tanpa harus membuka aplikasi.
* **Dobrak Layar Kunci (Full-Screen Call Intent):** Panggilan masuk akan langsung menampilkan dialog respon full-screen meskipun layar terkunci.
* **Pengecekan Izin Otomatis Bertingkat:** Deteksi mandiri untuk izin Overlay, Penghemat Baterai, dan Auto-Start di Android versi baru (Android 11 – 15).
* **Menu Cepat Profil (PopupMenu):** Opsi Ganti Password dan tombol pintas Perbaiki Izin Auto-Buka untuk bypass pembatasan sistem HP.
* **Riwayat Panggilan:** Memantau rekap jejak panggilan masuk yang telah direspon maupun ditolak.

### Panel Siswa (DashboardSiswaActivity)
* **Pemicu Panggilan Instan:** Mengirimkan sinyal panggil ke guru mata pelajaran / guru piket yang dituju.
* **Status Respons Real-Time:** Menampilkan indikator perubahan status (Menunggu, OTW, Ditolak).
* **Catatan Riwayat Siswa:** Menyimpan log riwayat panggilan yang pernah dilakukan oleh siswa.
* **Keamanan Akun:** Fitur ganti password profil mandiri menggunakan dialog terproteksi.



## Stack Teknologi dan Arsitektur

* **Language:** Kotlin 1.9+
* **UI & Components:** Material Design 3, XML Layouts, CardView, Custom Dialogs, PopupMenu, Fragments.
* **Database & Backend:** Firebase Realtime Database
* **State Management & Storage:** SharedPreferences (Sesi Login "KTP Digital" & Flag Izin)
* **Background Architecture:** 
  * Service (Foreground Listener)
  * NotificationManager (High-Priority Channels)
  * WindowManager Flags (Wake & Keep Screen On)

---

## Persyaratan Sistem (System Requirements)

| Parameter | Spesifikasi Minimum |
| :--- | :--- |
| **Minimum SDK** | Android 6.0 (API Level 23 / Marshmallow) |
| **Target SDK** | Android 14 / 15 (API Level 34+) |
| **Koneksi** | Internet (TCP/IP ke Firebase Node) |
| **Izin Kunci (Permissions)** | SYSTEM_ALERT_WINDOW, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, FOREGROUND_SERVICE, USE_FULL_SCREEN_INTENT |

---

## Panduan Instalasi dan Pengaturan

### 1. Clone Repositori
```bash
git clone [https://github.com/arkazedy/GoClass.git](https://github.com/arkazedy/GoClass.git)
cd GoClass
