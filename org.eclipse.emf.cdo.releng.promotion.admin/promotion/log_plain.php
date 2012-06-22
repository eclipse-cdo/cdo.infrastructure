<?php
// ini_set('display_errors', true);

echo "<tt>\n\n";

$logfile = "/tmp/promotion.emf.cdo/promoter.log";
$lines = file($logfile);
echo $lines;

echo "</tt>\n";

?>
