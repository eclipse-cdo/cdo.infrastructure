<?php
ini_set('display_errors', true);

$logfile = "/tmp/promotion.emf.cdo/promoter.log";

echo '<center><font face="Helvetica,Arial">';
echo "<h1>$logfile</h1>";
echo '</font></center>';

echo '<pre>';
echo htmlspecialchars(file_get_contents($logfile));
echo '</pre>';

?>
