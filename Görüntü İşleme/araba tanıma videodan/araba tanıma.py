# -*- coding: utf-8 -*-

import cv2    #araba tanımlama işlemini yapabilmek için gerekli sınıfın projeye eklenmesi.
import requests  #service işlemlerini yapabilmek için gerekli sınıfın projeye eklenmesi.

cascade_src = 'cars.xml'  #araba tanıma işlemini yapmak için arabanın özelliklerinin belirtildiği xml dosyasının yeri.
video_src = 'dataset/video1.avi' #videonun yerinin belirtildiği satır.

cap = cv2.VideoCapture(video_src) #videonun opencv kütüphanesinin VideoCapture methoduna gönderildiği satır. Burada kameradan alınan görüntüyü göndermek için (video_src) yerine (1) yazıp kamerayı 1 numaralı usb slota takmak gerekmektedir.
car_cascade = cv2.CascadeClassifier(cascade_src) #xml dosyasında ki özellikleri opencv kütüphanesinin CascadeClassifier metoduna gönderildiği satır.

while True:
    ret, img = cap.read()  #videoda ki görüntüyü image image almak için gerekli satır.
    if (type(img) == type(None)):  #video yüklenirken veya kameraya bağlanırken bir sıkıntı olur ve bağlanma işlemi gerçekleştirilemezse döngüden çıkmak için gerekli koşul.
        break
    
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)  #alınan resimleri griye çevirme.
    
    cars = car_cascade.detectMultiScale(gray, 1.1, 1) # tanımlanan nesneleri kareye alan satır.

    for (x,y,w,h) in cars:
        cv2.rectangle(img,(x,y),(x+w,y+h),(0,0,255),2)      
    
    cv2.imshow('video', img)
    
    if cv2.waitKey(33) == 27:
        break 
r = requests.post('https://bitirmeservis.herokuapp.com/durum_guncelle', data = {'id':'5880e8650e35800c988299b0','durum':'0'})  # servise ölçüm sonrası uygunluğa göre durumun güncellenmesi.
print(r.text)  #response text'i ekrana yazdırma
cv2.destroyAllWindows() #bütün pencereleri kapatılması.
