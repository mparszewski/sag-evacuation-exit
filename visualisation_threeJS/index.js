var express = require('express');
var app = express();
var path = require('path');
const PORT = 3000

app.get('/', function (req, res) {
    res.sendFile(path.join(__dirname + '/index.html'));
});

app.listen(PORT);

console.log('Visualisation server is up and running at port:', PORT)