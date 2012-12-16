<?

// checks whether the dominator analysis also works for non-main functions

foo();
echo 'hi_main';

function foo() {
    include 'test04b.php';
    echo 'hi_foo';
    ~_hotspot1;         // 2 entries
}

?>
