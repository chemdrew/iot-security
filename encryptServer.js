const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const fs = require('fs');
const crypto = require('crypto');

const privateKeyFile = __dirname+'/fixtures/id_rsa';
const publicKeyFile = __dirname+'/fixtures/id_rsa.pub';
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

app.get('/users/:id/encryptedCard', (req, res) => {
  var userId = req.params.id;

  var key = fs.readFileSync(publicKeyFile).toString();
  var users = JSON.parse(fs.readFileSync(userFile));
  if (!users[userId]) return res.status(404).send({'err': 'user not found'});

  var buffer = new Buffer(JSON.stringify(users[userId].creditCard));
  var encrypted = crypto.publicEncrypt(key, buffer);
  res.send({encryptedCard: encrypted.toString('base64')});
});

app.put('/decryptCard', (req, res) => {
  var key = fs.readFileSync(privateKeyFile);

  var buffer = new Buffer(req.body.encryptedCard, 'base64');
  var decrypted = crypto.privateDecrypt(key, buffer);
  try {
    decrypted = JSON.parse(decrypted);
  } catch (e) {
    return res.status(500).send({'err': 'could not decrypt'});
  }
  return res.send(decrypted);
});

app.listen(3000, () => {
  console.log('Example app listening on port 3000!');
});