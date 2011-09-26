<?php

require_once 'Drop.php';

class Project
{
	public $name;
	public $path;
	public $drops = array();
	private $dropsByQualifier = array();

	function __construct($name, $path)
	{
		$this->name = $name;
		$this->path = $path;

		$d = dir($this->path);
		while (false !== ($entry = $d->read()))
		{
			if (strpos($entry, ".") !== 0)
			{
				$this->addDrop($entry);
			}
		}

		$d->close();
	}

	function getDrop($qualifier)
	{
		return $this->dropsByQualifier[$qualifier];
	}

	function generate()
	{
		echo "<h1>".$this->name." Promotion Admin</h1>";
		echo '<table border="1" cellspacing="3">';
		foreach ($this->drops as $drop)
		{
			$drop->generate();
		}

		echo '</table>';
	}

	private function addDrop($qualifier)
	{
		$drop = new Drop($this, $qualifier);
		$this->drops[count($this->drops)] = $drop;
		$this->dropsByQualifier[$drop->qualifier] = $drop;
		return $drop;
	}
}

?>
