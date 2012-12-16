<?

// was a bug

foreach ($get as $x1) {
    ~_hotspot0;  // main.x1:T/D
}

a($get);

function a($ap1) {
    foreach ($ap1 as $a1) {
        ~_hotspot1;  // a.a1:T/D
    }
}



?>
