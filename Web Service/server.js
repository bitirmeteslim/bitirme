//Express.js modülünü kullanabilmek için çağırıyoruz
var express = require('express');
//Mongodb modülünü kullanabilmek için çağırıyoruz
var mongodb = require('mongodb');
//Mongoose modülünü kullanabilmek için çağırıyoruz
var mongoose = require('mongoose');
//Bcrypt modülünü kullanabilmek için çağırıyoruz
var bcrypt = require('bcrypt');
//Morgan modülünü kullanabilmek için çağırıyoruz
var morgan = require('morgan');
//Body-parser modülünü kullanabilmek için çağırıyoruz
var bodyParser = require('body-parser');
//Blue-bird modülünü kullanabilmek için çağırıyoruz
var bluebird = require('bluebird');
//Request modülünü kullanabilmek için çağırıyoruz
var request = require('request');
//User modülünü kullanabilmek için çağırıyoruz
var User = require('./lib/User');
//mongodb'nin object id modülünü kullanabilmek için çağırıyoruz.
var ObjectID = mongodb.ObjectID;

//Mongodb bağlantısının açık kalmasını sağlamak için keepAlive ve connectTimeoutMS sürelerini 
//ayarlıyoruz.
var options = {
  server: { socketOptions: { keepAlive: 1, connectTimeoutMS: 30000 } },
  replset: { socketOptions: { keepAlive: 1, connectTimeoutMS: 30000 } }
};

//Online Database'imizin connection string'ini yazıp bağlanıyoruz.
mongoose.connect('mongodb://user:123456@ds159237.mlab.com:59237/bitirme',options);
//bağlantımızı db nesnemize refere ediyoruz
var db = mongoose.connection;
//Google'dan aldığımız api key'imizi tanımlıyoruz
var GOOGLE_API_KEY = "AIzaSyDdQdjDionZ2clgJNgZ4AQcNxgAToGO1Co";
//Google direction api url'ini tanımlıyoruz
var DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
//Port tanımlıyoruz
var PORT = process.env.PORT || 3000;
var koordinatCevap;
//BodyParser modülünün json metodunu refere ediyoruz.
var jsonParser = bodyParser.json();
var cevap;
global.destinationDizi = [];
global.latLngDizi = [];
var distanceDizi = [];
global.mantiksal;
global.sonucDizisi = [];

//Express modülünü refere ediyoruz
var app = express();
//Console ekranından service'imizin log'larını görüyoruz
app.use(morgan('dev'));

app.use(bodyParser.urlencoded({extended:false}));
app.use(bodyParser.json());

//Kayıt olabilmek için gerekli endpoint
app.post('/register',function(req,res){
	//request body'den email'i alıyoruz.
	var email = req.body.email;
	//request body'den password'u alıyoruz.
	var password = req.body.password;
	//Yeni user oluşturuyoruz.
	var newUser = new User();
	//user'ımızın email'ini tanımlıyoruz
	newUser.email = email;
	//user'ımızın password'unu tanımlıyoruz
	newUser.password = password;
	//user'imizi kaydediyoruz.
	newUser.save(function(err,savedUser){
		//herhangi bir hata olması durumunda statü kodunu 406 olarak döndürüyoruz
		//burda bir hata olması kullanıcının zaten kayıtlı olduğu anlamına gelmektedir.
		if(err) return res.status(406).send(err);
		//hata olmaması durumunda kaydın başarıyla gerçekleştiğine dair statü kodunu 200
		//olarak döndürüyoruz.
		return res.status(200).send();
	});
});

//giriş yapabilmek için gerekli endpoint'i tanımlıyoruz.
app.post('/login',function(req,res){
	//request body'den email'i alıyoruz.
	var email = req.body.email;
	//request body'den password'u alıyoruz.
	var password = req.body.password;
	
	//user'ile kullanıcının kayıtlı olup olmadığını kontrol ediyoruz.
	User.findOne({email:email},function(err,user){
		//herhangi bir hata varsa 500 statü kodunu döndürüyoruz.
		if(err) return res.status(500).send();
		//kullanıcı bulunamazsa 404 statü kodunu döndürüyoruz
		if(!user) return res.status(404).send();
		//kullanıcı bulunduysa parolanın doğru olup olmadığını kontrol ediyoruz.
		user.comparePassword(password,function(err,isMatch){
			//isMatch var ve true olarak geldiyse kullanıcı bulundu ve girilen parola
			//doğru demektir ve 200 statü kodunu döndürüyoruz
			if(isMatch && isMatch == true)
				return res.status(200).send();
			//kullanıcı bulundu ama parola yanlış ise statü kodunu 401(unauthorized) olarak 
			//döndürüyoruz
			return res.status(401).send();
		});
	});
});

//Origin bilgilerimizi göndereceğimiz endpoint
app.post('/get_distance',function(req,res){
	//request body'den origin'i alıyoruz
	var origin = req.body.origin;
	//request body'den email'i alıyoruz
	var email = req.body.email;
	//getAdreslist fonksiyonu ile database'de kayıtlı bilgileri çekiyoruz
	getAdresList(function(result) {
		//gelen result'u parse ediyoruz.
		koordinatCevap = JSON.parse(result);
		if(koordinatCevap == undefined)
			//gelen json tanımsız ise uyarı gönderiyoruz
			return res.send('Tanımsız');
		else{
			//gelen response tanımlı ise koordinatCevap'ın uzunluğu kadar for döngüsü döndürüyoruz
			for(var i = 0; i < koordinatCevap.length; i++){
				//gelen response'dan Adres value'sini alıyoruz
				var Adres = koordinatCevap[i]['Adres'];
				//gelen response'dan lat ve lng value'lerini alıp destination türüne dönüştürmek 
				//için aralarına virgül ekleyip hedef değişkenine atıyorut
				var hedef = koordinatCevap[i]['lat'] + ',' + koordinatCevap[i]['lng'];
				//hedef değişkenini origin ve destination olarak getDistanceFromGoogle
				//fonksiyonuna gönderiyoruz.
				var durum = koordinatCevap[i]['durum'];
				if(durum == 1){
					getDistanceFromGoogle(hedef,origin,i,Adres,email,function(sonuc){
					
				});
				}
			}
		}
		//işlemler bittikten sonra 200 statü koduyla birlikte Success mesajını geri döndürüyoruz.
		return res.status(200).send('Success');
	});
	
});

//Google üzerinden mesafeleri hesapladıktan sonra gecici olarak database'e kaydediyoruz.
function mesafeGeciciKayıt(mesafe,email,callback){
	//mesafe_gecici isimli collection'a kaydediyoruz.
	db.collection('mesafe_gecici').insert({mesafe,email},function(err,result){
		//Hata varsa callback ile false döndürülüyor
		if(err) return callback(false);
		//Hata yoksa callback ile true döndürülüyor
		else return callback(true);
	})
}

//Database'e mesafe,Adres,destination ve email bilgileri ile tüm kayıtlar ilişkilendirilip
//Kaydediliyor.
function dbkayit(mesafe,Adres,destination,email,callback){
	//gecici isimli collection'a insert yapılıyor.
	db.collection('gecici').insert({mesafe,Adres,destination,email},function(err,result){
		//Hata varsa callback ile false döndürülüyor
		if(err) return callback(false);
		//Hata yoksa callback ile true döndürülüyor
		else return callback(true);
	})
}

//Origin'imiz ve for döngüsüyle her defasında gelen destination arasındaki mesafe hesaplanıyor
function getDistanceFromGoogle(destination,origin,indis,Adres,email,callback){
	var URL  = DIRECTION_URL_API + 'origin=' + origin + '&destination=' + destination + '&key=' + GOOGLE_API_KEY;
	request(URL,function(err,response,body){
		//Hata yoksa ve respose statü kodu 200 ise bilgiler parse ediliyor
		if(!err && response.statusCode == 200){
			//km yazısının indisi alınıyor
			var indis = body.indexOf('km');
			//text yazısının indisi alınıyor
			var indis2 = body.indexOf('text');
			//iki indis arasındaki fark hesaplanıyor
			var fark = indis-indis2-10;
			//substr ile iki değer arasındaki ifade alınıyor(kaç km olduğu burdan alınıyor)
			var mesafe = body.substr(indis2+9,fark);
			//alınan mesafe latLngDizi'sine push ediliyor.
			global.latLngDizi.push(mesafe);
			//mesafe email ile ilişkilendirilip gecici kayıt isimli fonksiyona gönderiliyor.
			mesafeGeciciKayıt(mesafe,email,function(sonuc){
				if(!sonuc){
					//hata varsa callback ile false döndürülüyor.
					return callback(false);
				}
				else{
					//dbKayit fonksiyonuna mesafe,Adres,destination,email bilgileri
					//ilişkilendirilip kaydediliyor.
					dbkayit(mesafe,Adres,destination,email,function(argument) {
						if(!argument){
							//hata varsa callback ile false döndürülüyor.
							return callback(false);
						}
						//Hata yoksa callback ile true döndürülüyor.
						else return callback(true);
					})
				}
			})
			//işlem bittikten sonra callback ile birlikte latLngDizi'sinin o anki indisindeki
			//eleman döndürülüyor.
			return callback(global.latLngDizi[indis],mesafe);
		}
	})
}

//bu fonksiyon mesafeler hesaplandıktan sonra en yakın olan 4 elemanı geri döndürüyor.
function ilkUcGetir(hedef1,hedef2,hedef3,hedef4,callback){	
	//gecici isimli collection'dan hedef1,hedef2,hedef3,hedef4 değerleri çekiliyor.
	db.collection('gecici').find({
		'mesafe':{
			$in:[hedef1,hedef2,hedef3,hedef4]
		}
	}).toArray(function(err,result){
		//Hata varsa callback ile false döndürülüyor.
		if(err) return callback(false);
		else{
			//Hata yoksa callback ile result döndürülüyor.
			return callback(result);
		}
	})
}


//Mesafeler hesaplandıktan sonra ilk 4 ünü almak için sıralama yapıyoruz.
app.get('/mesafe_sirala',function(req,res){
	//mesafe_gecici tablosundaki tüm değerler çekiliyor.
	db.collection('mesafe_gecici').find().toArray(function(err,result){
		//Hata varsa statü kodu 404 olarak geri döndürülüyor.
		if(err) return res.status(404).send();
		else{
			//hata yoksa mesafeler dizisine tüm mesafeler alınıyor.
			var mesafelerDizisi = [];
			for(var i = 0; i < result.length; i++){
				mesafelerDizisi.push(result[i]['mesafe']);
			}
			//Alınan mesafeler sıralanıyor
			mesafelerDizisi.sort();
			//Dizinin ilk 4 elemanı ilkUcGetir fonksiyonu ile gecici tablosundan çekilmek
			//için gönderiliyor.
			ilkUcGetir(mesafelerDizisi[0],mesafelerDizisi[1],mesafelerDizisi[2],mesafelerDizisi[3],function(sonuc){
				if(sonuc == false){
					//Hata varsa statü kodu 500 olarak döndürülüyor.
					return res.status(500).send();
				}
				else{
					//Hata yoksa statü kodu 200 ile birlikte sonuç döndürülüyor.
					return res.status(200).send(sonuc);
				}
			});
		}
	})
});

//Kayıt eklemek için gerekli endpoint
app.post('/insert_data', function(req,res){
	//koordinat isimli collection'a direk request body insert ediliyor.
	db.collection('koordinat').insert(req.body,function (err,result){
		//hata varsa response ile false gönderiliyor
		if(err) res.send(false)
			//hata yoksa response ile true gönderiliyor
			else res.send(true)
		
	})
})

//koordinat collection'undaki tüm kayıtları çekiyoruz.
app.get('/get_all',function(req,res){
	db.collection('koordinat').find().toArray(function (err,result){
		//hata varsa response ile birlikte hata gönderiliyor
		if(err) return res.send(err);
		//hata yoksa statü kodu 200 ile birlikte result gönderiliyor.
		else return res.status(200).send(result);
	});
});

//koordinat collection'undaki kayıtlı tüm değerleri çekiyoruz.
function getAdresList(callback){
	var yeniURL = 'https://bitirmeservis.herokuapp.com/get_all';
	//yukarıda tanımlı endpoint'e request ile istek atıyoruz
	request.get(yeniURL,function(err,response,body){
		if(!err && response.statusCode == 200){
			//hata yok ve statü kodu 200 ise bu scope'a giriyoruz
			//body null undefined veya '' ise callback ile birlikte döndürülüyor
			if(body == null || body == undefined || body == ''){
				return callback(body);
			}
			//body dolu ise callback ile döndürülüyor.
			else return callback(body);
		}
	})
};

app.post('/durum_guncelle',function(req,res){
	var durum = req.body.durum;
	var gelen_id = req.body.id;
	db.collection('koordinat').updateOne({_id: ObjectID(gelen_id)},{
		$set:{
			'durum': durum
		}
	},function(err,result){
		if(err) return res.status(500).send();
		return res.status(200).send('Success');
	});
});

//kullanıcının email adresiyle kendi origin bilgileriyle daha önceden hesaplanan kayıtlar
//kullanıcı sisteme giriş yaptıktan sonra siliniyor (eski kayıtlarda karışıklık olmaması için).
app.post('/delete_colls',function(req,res){
	var email = req.body.email;
	//gecici collection'unda email'i gönderilen email'e eşit olan tüm bilgiler siliniyor.
	db.collection('gecici').remove({'email':email},function(err,result){
	});
	//mesafe_gecici collection'unda email'i gönderilen email'e eşit olan tüm bilgiler siliniyor.
	db.collection('mesafe_gecici').remove({'email':email},function(err,result){
	});

	//bittikten sonra response ile success mesajı döndürülüyor.
	return res.send('success');
});

//app.listen ile 3000 veya hosting aldığımız adresin açık port'u dinleniyor.
app.listen(PORT,function(){
	//hangi port'un dinlendiği console'a yazdırılıyor.
	console.log('App listening on port: '+PORT);
});