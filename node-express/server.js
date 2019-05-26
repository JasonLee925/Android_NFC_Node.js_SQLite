// Create express app
const express = require('express');
const app = express();
const db = require("./database.js");
const bodyParser = require('body-parser');

// Parse body
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));

// Server port
var HTTP_PORT = 8000;

// Start server
app.listen(HTTP_PORT, () => {
	console.log("Server running on port %PORT%".replace("%PORT%",HTTP_PORT))
	});

// Root endpoint
app.get("/", (req, res, next) => {
	res.sendFile(__dirname + '/client/html/index.html');
});

// Get all cards from db
app.get("/nfc/cards", (req, res, next) => {
	var sql = "select * from cards"
	var params = []
	db.all(sql, params, (err, rows) => {
		if (err) {
		  res.status(400).json({"error":err.message});
		  return;
		}
		console.clear()
		console.log(rows)
		res.send(rows);
	  });
});

// Get last updated card from db
app.get("/nfc/lastuid", (req, res, next) => {
    var sql = "select * from cards where is_last = ?"
    db.get(sql, true, (err, row) => {
        if (err) {
          res.status(400).json({"error":err.message});
          return;
        }
		
		if(row){
			res.send({row});
		}else{
			res.json({
				"message":"nothing here"});
		}
      });
	  
});

// Get the card by uid
app.get("/nfc/cards/:uid", (req, res, next) => {
    var sql = "select * from cards where uid = ?"
    db.get(sql, req.params.uid, (err, row) => {
        if (err) {
          res.status(400).json({"error":err.message});
          return;
        }
		
		if(row){
			//console.log(row)
			res.json({row});
		}else{
			res.json({
				"message":"nothing here"});
		}
      });
});

// Post card
app.post("/nfc/uid", (req, res, next) => {
    var errors=[];
    const reg = /^([0-9A-F]{8})$/i;	// 4bytes UID正規表達式
    const now = () => new Date(Date.now()).toLocaleString('zh-TW'); // get current time
    if (!req.body.uid){
        errors.push("No uid specified");
    }
	
    if (!reg.test(req.body.uid)) {
        errors.push("UID format error!");
	}
	
	//// return error if it was exist ////
    if (errors.length){
        console.log(errors.join(","));
        res.status(400).json({"error":errors.join(",")});
        return;
    }
	
	
	//// SELECT => get card by uid ///
	db.get("select * from cards where uid = ?", req.body.uid, (err, row) => {
		if (err) {
          res.status(400).json({"error":err.message});
          return console.error(err.message);
        }
		
		var data = {};
		
		// Default setting 
		data[req.body.uid] = {
			"create_time" : now(),
			"update_time" : now(),
			"count" : 1,
			"is_last" : true
		}
		
		var sql ='INSERT INTO cards '+
				 '(uid, create_time, update_time, count, is_last) '+
				 'VALUES (?,?,?,?,?)';
				 
		var params =[req.body.uid, 
					data[req.body.uid]['create_time'], 
					data[req.body.uid]['update_time'],
					data[req.body.uid]['count'],
					data[req.body.uid]['is_last']];
					
		//// if the card is old ////
		if(row){ 
			console.log("Old card.");
			sql ='UPDATE cards'+
				 ' SET update_time = ?, count = ?, is_last = ?'+
				 ' WHERE uid = ?';
				 
			data[req.body.uid] = {
					"update_time" : now(),
					"count" : row.count + 1,
					"is_last" : true
				}
				
			params = [data[req.body.uid]['update_time'], 
					data[req.body.uid]['count'], 
					data[req.body.uid]['is_last'], req.body.uid];
		}
		
		
		//// UPDATE => Reset is_last column in db ////
		db.run("UPDATE cards SET is_last = ?", [false], function (err, result) { 
			if (err){
				res.status(400).json({"error": err.message})
				return;
			}
			
			//// INSERT/UPDATE => table ////
			db.run(sql, params, function (err, result) {
				if (err){
					res.status(400).json({"error": err.message})
					return;
				}
				
				//// SELECT => return all cards ////
				db.all("select * from cards", [], (err, rows) => {
					if (err) {
					  res.status(400).json({"error":err.message});
					  return;
					}
					console.clear()
					console.log(rows)
					return res.send({rows})
				});
			});
		});
    });
})

// Default response for any other request
app.use(function(req, res){
	res.status(404);
});