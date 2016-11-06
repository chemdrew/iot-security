const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const fs = require('fs');
const crypto = require('crypto');

const userFile = __dirname+'/fixtures/userData.json';

// do some middleware check for auth token
app.use(bodyParser.json());

app.use((req, res, next) => {
  console.log(JSON.stringify({
    time: new Date().toISOString(),
    method: req.method,
    hostname: req.hostname,
    path: req.path,
    query: req.query,
    headers: req.headers,
    body: req.body
  }));
  next();
});

app.get('/users/:id/paymentToken', (req, res) => {
  var userId = req.params.id;

  var users = JSON.parse(fs.readFileSync(userFile));
  if (!users[userId]) return res.status(404).send({'err': 'user not found'});

  crypto.randomBytes(32, (err, buffer) => {
    if (err) return res.status(500).send({'err': err});
    var token = buffer.toString('base64');
    users[userId].paymentToken.value = token;
    users[userId].paymentToken.expires = "none - just for this demo";
    fs.writeFileSync(userFile, JSON.stringify(users, null, 2));
    return res.send({'token': token});
  });
});

app.get('/users/:id/creditCard', (req, res) => {
  var userId = req.params.id;
  var token = req.headers['x-token'];

  var users = JSON.parse(fs.readFileSync(userFile));
  if (users[userId].paymentToken.value === token) {
    return res.send(users[userId].creditCard);  
  } else {
    return res.status(403).send({'err': 'not authorized'});
  }
});

app.listen(3000, () => {
  console.log('Example app listening on port 3000!');
});
