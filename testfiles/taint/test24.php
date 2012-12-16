<?php

// "global" statement (2):
// what happens in case of non-existent globals?
// does not happen here, since our Converter is smart enough
// to enter variable x1 into the main symbol table :)

a();

function a() {
    global $x1;
    ~_hotspot0;     // a.x1:T/D
}


?>
