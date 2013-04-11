<?

foo();
echo 'hi_main';
bar();

function foo() {
    include 'test01b.php';
    echo 'hi_foo';
}

function bar() {
    $GLOBALS['x'] = 'good';
    include 'test01b.php';
    echo 'hi_bar';
}



?>