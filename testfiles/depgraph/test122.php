<? //

// was a bug in include file resolution

$inc = 'test122a.php';
include($inc);          // includes class definition
$x = new MyClass();     // constructor defines MYCONST
include(MYCONST);       // should be resolvable



?>