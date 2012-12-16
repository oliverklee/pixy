<? //

// nice example:
// output tells you that $fn comes either from socket_read, or from fread,
// or that it is uninitialized
$a = socket_read();
$b = fread();
if ($r) {
    $x1 = $a;
} else if ($r) {
    $x1 = $b;
}
//fopen($fn);
~_hotspot0;     // x1:{(socket_fread,6)(fread,7)(uninit)}



?>
