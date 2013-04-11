<?


// including the same class in two different files;
// don't crash!

$x = 'test117b.php';

include $x;
include $y;

echo $f->blob();

?>