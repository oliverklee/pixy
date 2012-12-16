<?php


// was a bug:
// forgot to specify a transfer function for cfgnodecallbuiltin,
// so Pixy thought that we were trying to define a constant
// with the name "_null"

foo();

function foo()
{
    $x = array('one');

    foreach ($x as $y) {
        ~_hotspot0;
        define($y, 'lala');
    }
}


?>
