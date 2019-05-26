var sqlite3 = require('sqlite3').verbose()
var md5 = require('md5')

const DBSOURCE = "db.sqlite"

let db = new sqlite3.Database(DBSOURCE, (err) => {
	
    if (err) {
      // Cannot open database
      console.error(err.message)
      throw err
    }else{
        console.log('Connected to the SQLite database.')
		//id INTEGER PRIMARY KEY AUTOINCREMENT,
        db.run(`CREATE TABLE IF NOT EXISTS cards (
            uid text NOT NULL UNIQUE, 
            create_time text, 
            update_time text,
            count INTEGER,
			is_last INTEGER
            )`,
        (err) => {
            if (err) {
                console.log(err);
            }else{
                /*Table just created, creating some rows
				/var data = {
					uid 		: "12345678",
                    create_time : "1",
                    update_time : "2",
                    count       : 1,
					is_last 	: false
				}
                var insert = 'INSERT INTO cards (uid, create_time, update_time, count, is_last) VALUES (?,?,?,?,?)'
                //db.run(insert, ["admin","admin@example.com",md5("admin123456")])
                db.run(insert, [data.uid, data.create_time, 
								data.update_time, data.count,
								data.is_last])*/
            }
        });  
    }
});

module.exports = db

