<? //

// uninitialized local function variables are untainted

foo();
function foo() {
    mysql_query('a' . $x . 'b');
}





?>
