<?php

print "<h1>CDO Promotion Admin</h1>";

$d = dir("/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops");
while (false !== ($entry = $d->read()))
{
	echo "test: ".$entry."<br>\n";
}

$d->close();

$cdo = new Project("CDO", "/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops");
foreach ($cdo->getDrops() as $drop)
{
	echo $drop->getQualifier() . "<br>\n";
}

class Project
{
	private $name;
	private $path;
	private $drops = array();
	private $dropsByQualifier = array();

	function __construct($name, $path)
	{
		echo "Project";
		$this->name = $name;
		$this->path = $path;

		$d = dir($path);
		while (false !== ($entry = $d->read()))
		{
			// 			if (strpos($entry, ".") != 0)
			echo $entry;
			addDrop($this, $entry);
		}

		$d->close();
	}

	function getName()
	{
		return $this->name;
	}

	function getPath()
	{
		return $this->path;
	}

	function getDrops()
	{
		return $this->drops;
	}

	function getDrop($qualifier)
	{
		return $this->dropsByQualifier[$qualifier];
	}

	function addDrop($qualifier)
	{
		echo 1;
		$drop = new Drop($this, $qualifier);
		echo 2;
		$this->drops[count($this->drops)] = $drop;
		echo 3;
		$this->dropsByQualifier[$drop->getQualifier()] = $drop;
		echo 4;
		return $drop;
	}
}

class Drop
{
	private $project;
	private $qualifier;

	function __construct($project, $qualifier)
	{
		$this->project = $project;
		$this->qualifier = $qualifier;
	}

	function getProject()
	{
		return $this->project;
	}

	function getQualifier()
	{
		return $this->qualifier;
	}
}

?>
