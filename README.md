# ⛅ Lightweight Samsung One UI Weather (Android)

Reeder S19 Max ve benzeri düşük sistem kaynaklı Android cihazlar için sıfırdan, reklamsız ve tamamen gizlilik odaklı olarak geliştirilmiş **hafik, sade ve güvenli** bir hava durumu uygulamasıdır.

---

## 🎯 Projenin Amacı

Piyasadaki standart hava durumu uygulamalarının aksine arka planda gereksiz servisler çalıştıran, RAM tüketen, batarya harcayan ve kişisel verileri toplayan şişkin yazılımlara alternatif olarak tasarlanmıştır. Temel prensibi; **maksimum gizlilik, minimum kaynak tüketimi ve anında performans** sunmaktır.

---

## ✨ Öne Çıkan Özellikler

* **Ultra Düşük RAM ve Batarya Tüketimi:** Ağır grafikler, animasyon paketleri veya üçüncü parti reklam motorları içermez. Sadece sistem emojileri ve hafif yerel arayüz bileşenleri kullanarak cihazınızı asla yormaz.
* **%100 Güvenli ve Gizli (Sandbox Mimarisi):** Cihazınızda sadece gerekli olan internet (`INTERNET`) iznini kullanır. Konumunuzu gizlice takip etmez, rehberinize veya kişisel dosyalarınıza erişemez.
* **Gerçek ve Kesintisiz Canlı Veriler:** Küresel çapta güvenilir meteoroloji merkezlerinin uydularından beslenen **Open-Meteo API** altyapısını kullanarak anlık ve 7 günlük gerçek hava tahminlerini saniyeler içinde ekrana getirir.
* **Samsung One UI Tarzı Modern Arayüz:** Gece mavisi geçişli şık tasarımı, yuvarlatılmış kartları, yatay kaydırmalı 24 saatlik akışı ve günlere dokunulduğunda açıldığında aktifleşen **Lazy Accordion** saatlik detay panelleriyle son derece akıcı bir deneyim sunar.

---

## 🔍 Şehir Arama ve Çalışma Mantığı

Uygulama **kesinlikle sadece IP adresine veya otomatik konuma bağımlı değildir.**

* **Dünya Çapında Şehir Arama:** Üst kısımda yer alan arama çubuğuna dilediğiniz şehri veya ilçeyi yazarak anlık olarak aratabilir, o bölgenin canlı hava durumunu ve 7 günlük tahminini saniyeler içinde görüntüleyebilirsiniz.
* **Akıllı Yerel Hafıza:** Uygulama içerisinde aradığınız son konum cihazınızın yerel güvenli alanında saklanır; arka planda asla dış sunuculara aktarılmaz.

---

## 🛠️ Kullanılan Teknolojiler

* **Dil:** Saf Kotlin
* **Mimari:** Native Android Views, `HttpURLConnection`, `Executors` (Arka plan iş parçacığı yönetimi)
* **Veri Kaynağı:** [Open-Meteo API](https://open-meteo.com/) (Ücretsiz, anahtarsız ve açık kaynaklı hava durumu JSON servisi)

---

## 🚀 Kurulum ve Çalıştırma

Projeyi klonlayın veya ZIP olarak indirin: [https://github.com/Edna1616/HavaDurumu]

Bu proje tamamen açık kaynaklıdır ve dilediğiniz gibi geliştirip inceleyebilirsiniz
