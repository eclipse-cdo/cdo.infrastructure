<?php
@ini_set('display_errors', true);

@apache_setenv('no-gzip', 1);
@ini_set('zlib.output_compression', 0);
@ini_set('implicit_flush', 1);
for ($i = 0; $i < ob_get_level(); $i++)
{
	ob_end_flush();
}

ob_implicit_flush(1);

require_once 'Project.php';
require_once 'Drop.php';

echo '<center><font face="Helvetica,Arial">';
echo "<h1>CDO Promotion Admin</h1>";

$cdo = new Project("/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops", array("indigo", "juno"));

if (isset($_GET["action"]))
{
	$action = $_GET["action"];
	$drop = $cdo->getDrop($_GET["drop"]);

	if ($action($drop))
	{
		$cdo->generate();
	}
}
else
{
	$cdo->generate();
}

echo '</font></center>';


function status($msg)
{
	echo "$msg<br>";
	for ($i = 0; $i < 150; $i++)
	{
		echo "          ";
	}

	echo "\n";
	flush();
}

function scheduleTask($task, $args)
{
	status("Scheduling task $task (".str_replace("\n", ", ", $args).")");

	$workingArea = "/tmp/promotion.emf.cdo";
	$publicFolder = "$workingArea/public";
	$tmpFolder = "$publicFolder/tasks.tmp";

	$attempt = 0;
	while (!mkdir($tmpFolder))
	{
		sleep(1);
		status("Attempt to create $tmpFolder (".(++$attempt).")");
	}

	chmod($tmpFolder, 0777);
	status("Created $tmpFolder");

	$start = time();
	$taskFile = "$tmpFolder/$start.task";
	file_put_contents($taskFile, "$task\n$args");
	chmod($taskFile, 0666);

	clearstatcache();
	$touchpoint = "$workingArea/touchpoint";
	$timestamp = filemtime($touchpoint);

	$attempt = 0;
	while (!rename($tmpFolder, "$publicFolder/tasks"))
	{
		sleep(1);
		status("Attempt to rename $tmpFolder (".(++$attempt).")");
	}

	status("Renamed $tmpFolder");

	$attempt = 0;
	while (!isFinished($touchpoint, $timestamp))
	{
		sleep(1);
		status("Waiting for promoter to finish (".(++$attempt).")");
	}

	status("Promoter finished");
	status("");
	status('<a href="index.html" target="_parent">Return</a>');
	return false;
}

function isFinished($touchpoint, $timestamp)
{
	clearstatcache();
	if (!$timestamp)
	{
		return is_file($touchpoint);
	}

	return filemtime($touchpoint) != $timestamp;
}

function Cancel($drop)
{
	return true;
}

function EditLabel($drop)
{
	echo '<h2>Drop '.$drop->qualifier.'</h2>';
	echo '<form method="GET">';
	echo '<input name="drop" type="hidden" value="'.$drop->qualifier.'"></input>';
	echo '<p>Label: <input name="value" type="text" value="'.$drop->label.'"></input></p>';
	echo '<input name="action" type="submit" value="ChangeLabel"></input>';
	echo '&nbsp;';
	echo '<input name="action" type="submit" value="Cancel"></input>';
	echo '</form>';
	return false;
}

function ChangeLabel($drop)
{
	$value = $_GET["value"];
	return scheduleTask("ChangeLabel", "$drop->qualifier\n$value");
}

function Show($drop)
{
	return scheduleTask("Show", "$drop->qualifier");
}

function Hide($drop)
{
	return scheduleTask("Hide", "$drop->qualifier");
}

function AskStage($drop)
{
	echo '<h2>Drop '.$drop->qualifier.'</h2>';
	echo '<form method="GET">';
	echo '<input name="drop" type="hidden" value="'.$drop->qualifier.'"></input>';
	echo '<input name="train" type="hidden" value="'.$_GET["train"].'"></input>';
	echo '<input name="old" type="hidden" value="'.$_GET["old"].'"></input>';
	echo '<p>Are you sure to stage drop '.$drop->qualifier.' for '.$_GET["train"].'?</p>';
	echo '<input name="action" type="submit" value="Stage"></input>';
	echo '&nbsp;';
	echo '<input name="action" type="submit" value="Cancel"></input>';
	echo '</form>';
	return false;
}

function Stage($drop)
{
	return scheduleTask("Stage", "$drop->qualifier\n".$_GET["train"]."\n".$_GET["old"]);
}

function AskDelete($drop)
{
	echo '<h2>Drop '.$drop->qualifier.'</h2>';
	echo '<form method="GET">';
	echo '<input name="drop" type="hidden" value="'.$drop->qualifier.'"></input>';
	echo '<p>Are you sure to delete drop '.$drop->qualifier.'?</p>';
	echo '<input name="action" type="submit" value="Delete"></input>';
	echo '&nbsp;';
	echo '<input name="action" type="submit" value="Cancel"></input>';
	echo '</form>';
	return false;
}

function Delete($drop)
{
	return scheduleTask("Delete", "$drop->qualifier");
}

?>
