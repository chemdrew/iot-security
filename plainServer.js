const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const fs = require('fs');

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

app.get('/users/:id/creditCard', (req, res) => {
  var userId = req.params.id;

  var users = JSON.parse(fs.readFileSync(userFile));
  if (!users[userId]) return res.status(404).send({'err': 'user not found'});

  res.send(users[userId].creditCard);
});

app.listen(3000, () => {
  console.log('Example app listening on port 3000!');
});