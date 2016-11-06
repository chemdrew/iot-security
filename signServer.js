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

app.put('/signAction', (req, res) => {
  var sign = crypto.createSign('RSA-SHA256');
  var action = req.body.action;

  var key = fs.readFileSync(privateKeyFile).toString();

  sign.write(action);
  sign.end();

  var signedAction = sign.sign(key, 'base64');
  res.send({signedAction: signedAction});
});

app.put('/verifyAction', (req, res) => {
  var signature = req.body.signature;
  var action = req.body.action;

  var key = fs.readFileSync(publicKeyFile).toString();

  var verify = crypto.createVerify('RSA-SHA256');
  
  verify.write(action);
  verify.end();

  return res.send({result: verify.verify(key, signature, 'base64')});
});

app.listen(3000, () => {
  console.log('Example app listening on port 3000!');
});