//mongoose modülünü kullanabilmek için çağırıyoruz
var mongoose = require('mongoose');
//bcrypt modülünü kullanabilmek için çağırıyoruz
var bcrypt = require('bcrypt');
//mongoose'un Schema modülünü kullabilmek için çağırıyoruz.
var Schema = mongoose.Schema;
//hash için Salt factor belirliyoruz. (ben 10 olarak belirledim optimal olduğu ve tavsiye edildiği için)
var SALT_WORK_FACTOR = 10;

//user kaydı için schema oluşturuyoruz
var UserSchema =  Schema({
	email:{
		//email özelliklerini belirliyoruz. type string, unique true ve required olarak belirlendi
		type: String,
		unique: true,
		required: true
	},
	password:{
		//password özelliklerini belirliyoruz. type string, required olarak belirlendi
		type: String,
		required: true
	}
});

UserSchema.pre('save',function(next){
	//gelen kullanıcı nesnesini alıyoruz.
	var user = this;	
	//parolasının değiştirilip değiştirilmediğini kontrol ediyoruz.
	if(!user.isModified('password')) return next();
	//gensalt ile parolamızı çözebilmek için salt oluşturuyoruz.
	bcrypt.genSalt(SALT_WORK_FACTOR,function(err,salt){
		//hata oluşursa hatayı geri döndürüyoruz.
		if(err) return next(err);
		//hata yoksa hash oluşturuyoruz. ve salt ile ilişkilendiriyoruz.
		bcrypt.hash(user.password, salt, function(err,hash){
			//hata oluşursa hatayı geri döndürüyoruz.
			if(err) return next(err);
			//hata yoksa parolamızı oluşturduğumuz hash olarak belirliyoruz.
			user.password = hash;
			next();
		});
	});
});

//parolamızın hash'li haliyle karşılaştırılabilmesini burda sağlıyoruz.
UserSchema.methods.comparePassword = function(canditatePassword, cb){
	//gönderilen parolayı karşılaştırıyoruz.
	bcrypt.compare(canditatePassword, this.password,function(err, isMatch){
		//hata varsa cb(callback) ile hatayı döndürüyoruz.
		if(err) return cb(err);
		//hata yoksa isMathc'i true olarak döndürüyoruz.
		cb(null,isMatch);
	});
};

//User model'imizi tanımlıyoruz.
var User = mongoose.model('Users',UserSchema);

//modülümüzü başka yerlerde kullanabilmek için export ediyoruz.
module.exports = User;
