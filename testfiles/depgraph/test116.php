<? //


// including one-and-the-same file (with class)
// several times; don't crash!


$x = 'test116b.php';

include 'test116b.php';
include $x;
include $y;

echo $f->blob();





?>
