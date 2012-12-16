<? //

// evil example:
// with call-string-analysis (k=1), the dataflow fact 
// "$x depends on mysql_query" 
// flows back to the FIRST call of foo(),
// resulting in a CYCLE in the StringGraph;
// the effect is that not even an obvious prefix such as the "SELECT"
// below can be detected

echo foo();
// $a = mysql_fetch_row($x);
$a = 'SELECT' . $a;    // the "SELECT" prefix is not detected
$x = mysql_query($a);
echo foo();

function foo() {
 bar();
}

function bar() {
}





?>
