<?php
class Foo {
  var $taintedField;
  var $untaintedField;
}

$untaintedVariable = 42;
echo $untaintedVariable;

$taintedVariable = $_GET['x'];
echo $taintedVariable;

$foo = new Foo();

$foo->untaintedField = 'Hello world';
echo $foo->untaintedField;

$foo->taintedField = $_GET['x'];
echo $foo->taintedField;
?>