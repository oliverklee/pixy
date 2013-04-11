<? //

// fixed globals mixing due to cs limitation (2)


foo();
echo $a;        // harmless

function foo() {
    global $a;
    bar();
    $a = 'harmless';
    bar();
}

function bar() {
    hoo();
}

function hoo() {
}




?>