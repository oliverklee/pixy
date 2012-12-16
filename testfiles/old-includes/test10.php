<?

// was a bug in a previous version;
// occurs in PhpBB

$root_path = './';
include($root_path . 'test10a.php');        // sets $phpEx to 'php'
include($root_path . 'test10b.' . $phpEx);  // untaints $x1
~_hotspot0;     // $x1:U/C

?>
