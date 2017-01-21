# python car_detector.py --image images/1.jpg

import argparse
import cv2
import requests

ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True)
ap.add_argument("-c", "--cascade",
	default="cascade.xml")
args = vars(ap.parse_args())

image = cv2.imread(args["image"])
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

detector = cv2.CascadeClassifier(args["cascade"])
rects = detector.detectMultiScale(gray, scaleFactor=1.5,
	minNeighbors=10, minSize=(75, 75))

for (i, (x, y, w, h)) in enumerate(rects):
	cv2.rectangle(image, (x, y), (x + w, y + h), (0, 0, 255), 2)
	cv2.putText(image, "Car #{}".format(i + 1), (x, y - 10),
		cv2.FONT_HERSHEY_SIMPLEX, 0.55, (0, 0, 255), 2)

cv2.imshow("Cars", image)
r = requests.post('https://bitirmeservis.herokuapp.com/durum_guncelle', data = {'id':'5880e8650e35800c988299b0','durum':'1'})
print(r.text)
cv2.waitKey(0)
