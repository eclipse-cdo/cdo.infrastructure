<?php
ini_set('display_errors', true);

$publicFolder = "/tmp/promotion.emf.cdo/public";

echo '<font face="Helvetica,Arial">';
echo "<h1>Resetting $publicFolder</h1>";
echo '</font>';

deleteFolder("$publicFolder/tasks");
deleteFolder("$publicFolder/tasks.tmp");
deleteFolder("$publicFolder/tasks.inprogress");

function deleteFolder($tmpPath)
{
	if (!is_dir($tmpPath))
	{
		return;
	}

	if (!is_writeable($tmpPath))
	{
		chmod($tmpPath,0777);
	}

	$handle = opendir($tmpPath);
	while ($tmp = readdir($handle))
	{
		if($tmp != '..' && $tmp != '.' && $tmp != '')
		{
			if (is_writeable($tmpPath."/".$tmp) && is_file($tmpPath."/".$tmp))
			{
				unlink($tmpPath."/".$tmp);
			}
			elseif (!is_writeable($tmpPath."/".$tmp) && is_file($tmpPath."/".$tmp))
			{
				chmod($tmpPath."/".$tmp, 0666);
				unlink($tmpPath."/".$tmp);
			}

			if (is_writeable($tmpPath."/".$tmp) && is_dir($tmpPath."/".$tmp))
			{
				deleteFolder($tmpPath."/".$tmp);
			}
			elseif (!is_writeable($tmpPath."/".$tmp) && is_dir($tmpPath."/".$tmp))
			{
				chmod($tmpPath."/".$tmp, 0777);
				deleteFolder($tmpPath."/".$tmp);
			}
		}
	}

	closedir($handle);
	rmdir($tmpPath);
}

?>
