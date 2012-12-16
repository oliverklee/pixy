<? //


// TRANSDUCER TESTS *********************************************************
// if you want these tests to work, you have to set the corresponding flag
// in SQLAnalysis.java


/*
// not supported yet: ".*" transition in the $subject automaton
$a = 'abcde' . $get . 'fgh';
$b = str_replace('cd', 'xy', $a);
mysql_query($b);
*/


/*
// example for transducing a non-trivial automaton
if ($get) {
    $a = 'abc';
} else {
    $a = 'def';
}
$b = str_replace('bc', 'xy', $a);
mysql_query($b);
*/



/*
// complete example for str_replace transducer
$a = "SELECT * FROM table WHERE a = 'a'";
$b = str_replace('table', 'maple', $a);
mysql_query($b);
*/


/*
// whitespace example for str_replace transducer
$a = 'abcd ef';
$b = str_replace('cd', 'xy', $a);
mysql_query($b);
*/


/*
// easy example for str_replace transducer
$a = 'abcde';
$b = str_replace('cd', 'xy', $a);
mysql_query($b);
*/








?>
