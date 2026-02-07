# Aplikasi Penumpang Angkot

Selamat datang di repositori aplikasi penumpang angkot! Aplikasi ini dikembangkan menggunakan **Kotlin** dan **Android SDK** dengan tujuan memberikan informasi **posisi angkot secara real-time** kepada penumpang. Aplikasi ini membantu penumpang memantau pergerakan angkot, melakukan pemesanan perjalanan.

- **Bahasa Pemrograman**: Kotlin  
- **Framework**: Android Jetpack, Retrofit, Google Maps SDK, Pusher  

## Fitur Utama

Aplikasi penumpang menyediakan berbagai fitur untuk meningkatkan pengalaman dan kepercayaan pengguna terhadap layanan angkot, antara lain:

- **Autentikasi**:
  - Login dan logout pengguna.
  - Registrasi akun penumpang.
- **Pemantauan Angkot Real-Time**:
  - Menampilkan peta dengan posisi angkot secara real-time menggunakan GPS.
  - Memantau pergerakan angkot yang sedang menuju penumpang.
- **Pemesanan Perjalanan**:
  - Melakukan pemesanan angkot berdasarkan lokasi pengguna.
  - Melihat estimasi tarif perjalanan.
- **Status Perjalanan**:
  - Melihat status perjalanan (menunggu, dijemput, dalam perjalanan, selesai).
- **Riwayat Perjalanan**:
  - Menampilkan riwayat perjalanan yang pernah dilakukan.

### Highlight Teknologi

Aplikasi ini memanfaatkan beberapa komponen utama untuk mendukung fitur real-time dan pengalaman pengguna:

1. **Real-time Location Visualization**:
   - Menampilkan posisi angkot secara langsung pada peta menggunakan Google Maps SDK.
   - Data lokasi diperoleh dari backend yang menerima pembaruan lokasi driver secara berkala.
   - Menggunakan WebSocket (Pusher) untuk menerima pembaruan status pesanan dan pergerakan angkot.

3. **Location-Based Service**:
   - Menggunakan lokasi pengguna untuk menentukan titik penjemputan dan estimasi perjalanan.
   - Mendukung pengambilan keputusan penumpang berdasarkan posisi angkot terdekat.

Aplikasi ini difokuskan pada **fungsionalitas sistem dan alur layanan**, bukan pada desain antarmuka akhir, sesuai dengan konteks penelitian dan pengembangan sistem pada skripsi.

## Struktur Proyek

Aplikasi ini dibangun menggunakan arsitektur **Clean Architecture**, yang terdiri dari:

- **Presentation Layer**: Activity/Fragment dan ViewModel untuk antarmuka pengguna.
- **Domain Layer**: Use Case dan Repository (interface) untuk logika bisnis.
- **Data Layer**: Data Source, Repository Implementasi, dan ApiService untuk komunikasi dengan backend.
