#!/usr/bin/perl

use File::Basename;
$mypath = dirname($0);

require("$mypath/config/mem.pl");
$classpath="${mypath}/lib${classpath_separator}${mypath}/build/class";

system("java -Xms$mem_min -Xmx$mem_max -Dpixy.home=\"${mypath}\" -classpath \"${classpath}\" at.ac.tuwien.infosys.www.pixy.Checker -a -y xss:sql @ARGV");

